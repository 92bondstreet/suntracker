package com.sun.tracker;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sun.tracker.parser.City;
import com.sun.tracker.tabhost.ScrollableTabActivity;
import com.sun.tracker.utils.SolMessageManager;
import com.sun.tracker.utils.SolUtils;
import com.sun.tracker.yql.DatabaseManager;
import com.sun.tracker.yql.DatabaseManager.DatabaseResult;


interface AsyncTaskCompleteListener<T> {
	public void onTaskComplete(T result);
}

public class SolInvictus extends Activity implements OnClickListener,
android.widget.SeekBar.OnSeekBarChangeListener{

	// HMI Components
	private ImageView LocalCodeImageView;
	private TextView LocalTempTextView;
	private TextView LocalDescTextView;
	private Button SunWeatherButton;
	private Button AllWeatherButton;
	private TextView TempTextView;
	private TextView DistTextView;
	private SeekBar TempSeekBar;
	private SeekBar DistSeekBar;

	private SolAbout aboutDialog;

	// DATABASE
	private DatabaseManager DBManager;

	// PREFERENCES
	private SharedPreferences solPreferences;
	private Editor solPreferencesEditor;
	public static String PREF_temp_unit = "c";
	public static String PREF_dist_unit = "km";
	private String PREF_number_results = "20";
	private String PREF_pop = "100000";	
	private int PREF_default_distance = 499;
	private boolean PREF_refresh_database = true;
	private long PREF_time_refreshed = -1;
	public static final String MY_PREF = "SOL_INVICTUS_PREF";

	private IntentFilter updatePrefIntentFilter;
	private UpdatePrefBroadcastReceiver updatePrefBroadcastReceiver;
	public static String ACTION_UPDATE_PREF = "com.solinvictus.updatepref";

	// MESSAGE MANAGER
	private SolMessageManager alertManager;

	private int current_temp = 0;

	// RESPONSE : SolCities
	private ArrayList<City> top25;
	private ArrayList<City> solcities;
	
	private String current_latitude = null;
	private String current_longitude = null;

	// WEATHER CODE
	public static final int ALL_WEATHER = 3;
	public static final int CLOUD_WEATHER = 2;
	public static final int SUN_WEATHER = 1;

	// DOWNLOAD SOLCITIES
	private IntentFilter downloadCitiesIntentFilter;
	private DownloadCitiesBroadcastReceiver downloadCitiesBroadcastReceiver;
	public static String ACTION_DOWNLOAD_CITIES = "com.solinvictus.downloadcities";


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try{
			super.onCreate(savedInstanceState);

			// Full Screen
			setContentView(R.layout.solinvictus_layout);

			// 0. load preferences
			loadPreferences();

			// 1. init HMI
			initHMI();


			// 2. load current weather
			//loadCurrentWheather();
			OpenDatabase();

		}catch(Exception e){
			return;
		}
	}

	/*
	 * 		COMPONENTS / HMI
	 */

	private void initHMI(){

		// background
		BitmapFactory.Options opts = new BitmapFactory.Options();    	
		opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		opts.inDither = true;
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.sol_background, opts);
		BitmapDrawable bitmapDrawable = new BitmapDrawable(bmp);
		bitmapDrawable.setDither(true);
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.solinvictus_layout);	        
		linearLayout.setBackgroundDrawable(bitmapDrawable);

		LocalTempTextView = (TextView) findViewById(R.id.local_temperature);
		LocalDescTextView = (TextView) findViewById(R.id.local_description);
		LocalCodeImageView = (ImageView) findViewById(R.id.local_code_img);

		SunWeatherButton = (Button) findViewById(R.id.sun_weather_tracker);
		SunWeatherButton.setOnClickListener(this);
		AllWeatherButton = (Button) findViewById(R.id.all_weather_tracker);
		AllWeatherButton.setOnClickListener(this);

		TempTextView = (TextView)findViewById(R.id.sol_temp_text);
		TempTextView.setText(getString(R.string.sol_temp_text));
		TempSeekBar = (SeekBar) findViewById(R.id.sol_slider_temp);
		TempSeekBar.setOnSeekBarChangeListener(this);
		DistTextView = (TextView)findViewById(R.id.sol_dist_text);
		DistTextView.setText(getString(R.string.sol_dist_text));
		DistSeekBar = (SeekBar) findViewById(R.id.sol_slider_dist);
		DistSeekBar.setOnSeekBarChangeListener(this);

		alertManager = new SolMessageManager(this);

		initAbout();

		downloadCitiesIntentFilter = new IntentFilter(ACTION_DOWNLOAD_CITIES);
		downloadCitiesBroadcastReceiver = new DownloadCitiesBroadcastReceiver();
		registerReceiver(downloadCitiesBroadcastReceiver, downloadCitiesIntentFilter);

		DBManager = new DatabaseManager(this,PREF_pop);
	}

	private void updateHMITemp(){

		String stemp = String.valueOf(current_temp);
		if(PREF_temp_unit.equals("f"))
			stemp = String.valueOf(SolUtils.CelciusToFahrenheit(current_temp));

		stemp += "°" + PREF_temp_unit.toUpperCase();
		LocalTempTextView.setText(stemp);
	}

	private void updateHMICurrentWeather(City current_city, int temp_max, int dist_max){

		current_latitude = current_city.latitude;
		current_longitude = current_city.longitude;

		// Temperature
		current_temp = current_city.temp;
		updateHMITemp();

		switch(current_city.code) {
		case 1:
			LocalCodeImageView.setImageResource(R.drawable.sol_local_code_1);
			break;
		case 2:
			LocalCodeImageView.setImageResource(R.drawable.sol_local_code_2);
			break;
		case 3:
			LocalCodeImageView.setImageResource(R.drawable.sol_local_code_3);
			break;
		default:
			LocalCodeImageView.setImageResource(R.drawable.sol_local_code_3);
			break;
		}

		// Desc_code
		LocalDescTextView.setText(getString(SolUtils.loadTextFromYahooCode(current_city.yahoo_code)));

		// Temp slider
		TempSeekBar.setMax(temp_max);
		TempSeekBar.invalidate();
		TempSeekBar.setProgress(current_temp);

		// Dist slider
		PREF_default_distance = dist_max/2;
		DistSeekBar.setMax(dist_max);
		DistSeekBar.invalidate();
		DistSeekBar.setProgress(PREF_default_distance);
		
	}

	private void initAbout(){

		//0. Initialisation des composants
		String versionName = getString(R.string.about_version);
		try { 
			PackageInfo pInfo = getPackageManager().getPackageInfo("com.sol.invictus",PackageManager.GET_META_DATA);
			versionName = pInfo.versionName;
		} catch (NameNotFoundException e) { 
		} 

		aboutDialog = new SolAbout(this,versionName);
		aboutDialog.requestWindowFeature(aboutDialog.getWindow().FEATURE_NO_TITLE);
	}

	/*
	 * 		PREFERENCES
	 */

	private void loadPreferences(){

		solPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		solPreferencesEditor = solPreferences.edit();

		// temperature
		String unit = solPreferences.getString("PREF_temp_unit", null);
		if(unit!=null)
			PREF_temp_unit = unit;
		else{
			// default according language
			if((Locale.getDefault().getDisplayLanguage()).equals(Locale.ENGLISH.getDisplayLanguage()))
				PREF_temp_unit = "f";
			else
				PREF_temp_unit = "c";

			// sauvergarde des preferences
			solPreferencesEditor.putString("PREF_temp_unit", PREF_temp_unit);
			solPreferencesEditor.commit();
		}

		// distance
		unit = solPreferences.getString("PREF_dist_unit", null);
		if(unit!=null)
			PREF_dist_unit = unit;
		else{
			// default according language
			if((Locale.getDefault().getDisplayLanguage()).equals(Locale.ENGLISH.getDisplayLanguage()))
				PREF_dist_unit = "mi";
			else
				PREF_dist_unit = "km";

			// sauvergarde des preferences
			solPreferencesEditor.putString("PREF_dist_unit", PREF_dist_unit);
			solPreferencesEditor.commit();
		}

		// results
		String results = solPreferences.getString("PREF_number_results", null);
		if(results!=null){
			PREF_number_results = results;
		}
		else{
			PREF_number_results = "20";
		}

		// sauvergarde des preferences
		solPreferencesEditor.putString("PREF_number_results", PREF_number_results);
		solPreferencesEditor.commit();

		// results
		String pop = solPreferences.getString("PREF_pop", null);
		if(pop!=null){
			PREF_pop = pop;
		}
		else{
			PREF_pop = "50000";
		}

		// sauvergarde des preferences
		solPreferencesEditor.putString("PREF_pop", PREF_pop);
		solPreferencesEditor.commit();

		// refresh
		// check time to refresh
		long time = solPreferences.getLong("PREF_time_refreshed", -1);
		if(time==-1){
			PREF_time_refreshed = System.currentTimeMillis();
			PREF_refresh_database = true;
		}
		else{
			long delta = System.currentTimeMillis() - time;
			if(delta > 60000*30){
				PREF_time_refreshed = System.currentTimeMillis();
				PREF_refresh_database = true;
			}
			else{
				PREF_refresh_database = false;
				PREF_time_refreshed = time;
			}
		}

		// sauvergarde des preferences
		// -- Debug
		PREF_refresh_database = true;
		// --
		solPreferencesEditor.putLong("PREF_time_refreshed", PREF_time_refreshed);
		solPreferencesEditor.commit();


		updatePrefIntentFilter = new IntentFilter(ACTION_UPDATE_PREF);
		updatePrefBroadcastReceiver = new UpdatePrefBroadcastReceiver();
		registerReceiver(updatePrefBroadcastReceiver, updatePrefIntentFilter);
	}

	private void updatePreferences(){
		PREF_temp_unit = solPreferences.getString("PREF_temp_unit", null);
		PREF_dist_unit = solPreferences.getString("PREF_dist_unit", null);
		PREF_number_results = solPreferences.getString("PREF_number_results", null);
		PREF_pop = solPreferences.getString("PREF_pop", null);

		// in case where temp unit change
		updateHMITemp();
	}

	/*
	 * Broadcast receiver to change pref
	 */

	private class UpdatePrefBroadcastReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			updatePreferences();
		}
	}

	/*
	 * 
	 *			MENU
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.solinvictus_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle all of the possible menu actions.
		switch (item.getItemId()) {
		case R.id.OptionsItem:
			// Launch Prefs activity
			Intent i = new Intent(SolInvictus.this, SolPreferenceActivity.class);
			startActivity(i);
			break;
		case R.id.AboutItem:
			aboutDialog.show();
			break;
		case R.id.QuitItem:
			this.finish();
			break;
		}

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		if(menu!=null)
			menu.clear();
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.solinvictus_menu, menu);
		return true;
	}

	/*
	 * 			INTERNET - NETWORK - GPS
	 */

	private void OpenDatabase(){

		// 2 cases: download or load from sqllite
		// load from sqllite
		if(PREF_refresh_database){
			alertManager.showProgressDownload(getString(R.string.alert_download));

			if(DBManager.download(databaseResult)==DatabaseManager.NO_POSITION){
				alertManager.hideProgressDownload();
				alertManager.showCloseApp(getString(R.string.alert_no_geo), getString(R.string.alert_no_geo_title),getString(R.string.alert_no_geo_button));
			}

		}
		else
			loadDatabase();
	}

	public DatabaseResult databaseResult = new DatabaseResult(){

		@Override
		public void gotDatabase(int getit) {

			alertManager.hideProgressDownload();
			if(getit==DatabaseManager.OK){

				loadDatabase();

			}
			else{
				alertManager.showCloseApp(	getString(R.string.alert_download_weather), 
						getString(R.string.alert_download_weather_title),
						getString(R.string.alert_download_weather_button));
			}

		}
	};

	private void loadDatabase(){

		//0. Current Weather
		updateHMICurrentWeather(DBManager.getCurrentWeather(),DBManager.getTempMax(),DBManager.getDistMax());

		//1. update top25
		Intent intent = new Intent(ScrollableTabActivity.ACTION_UPDATE_TOP25);
		Bundle extras = null;
		top25 = DBManager.getTop25();
		if(top25!=null){
			extras = new Bundle();
			extras.putParcelableArrayList("top25", top25);
		}
		if(extras!=null)
			intent.putExtras(extras);
		sendBroadcast(intent);
			
		updateMapExtras();
	}


	private void callSolAroundMe(int search_code){

		try{
		
			String call_temp_min = Integer.toString(TempSeekBar.getProgress());
			String call_dist = Integer.toString(DistSeekBar.getProgress() + 1); 
			String call_temp_code = Integer.toString(search_code);
			
			
			if(search_code==SUN_WEATHER)
				alertManager.showProgress(getString(R.string.alert_fine_weather));
			else
				alertManager.showProgress(getString(R.string.alert_all_weather));
			
			
			solcities = DBManager.getCities(call_dist, call_temp_min, call_temp_code, PREF_pop, PREF_number_results);
		
			updateMapExtras();

			// load ListView in new activity
			Bundle extras = new Bundle();
			extras.putParcelableArrayList("solcities", solcities);
			extras.putString("current_latitude", String.valueOf(current_latitude));
			extras.putString("current_longitude", String.valueOf(current_longitude));
			extras.putInt(ScrollableTabActivity.CURRENT_TAB_INDEX, 1);

			Intent intent = new Intent(ScrollableTabActivity.ACTION_CHANGE_TAB);
			intent.putExtras(extras);
			sendBroadcast(intent);

			alertManager.hideProgress();
			
			return;
		} catch (Throwable t){
			alertManager.hideProgress();
			alertManager.showToast(getString(R.string.alert_decode_weather), Toast.LENGTH_SHORT);
		}
		
		
	}

	

	private void updateMapExtras(){

		// load ListView in new activity
		Bundle extras = new Bundle();
		if(solcities!=null)
			extras.putParcelableArrayList("solcities", solcities);
		extras.putString("current_latitude", current_latitude);
		extras.putString("current_longitude", current_longitude);

		Intent intent = new Intent(ScrollableTabActivity.ACTION_UPDATE_MAP);
		intent.putExtras(extras);
		sendBroadcast(intent);

	}

	/*
	 *		EVENT 
	 */

	//@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch(arg0.getId()){
		case R.id.sun_weather_tracker:{
			callSolAroundMe(SUN_WEATHER);
			return;
		}
		case R.id.all_weather_tracker:{
			callSolAroundMe(ALL_WEATHER);
			return;
		}
		default:
			return;
		}	
	}

	//@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub
		try{

			if(arg0.getId()==R.id.sol_slider_temp)
				TempTextView.setText(SolUtils.formatToastTemp(getString(R.string.sol_temp_text), arg1, PREF_temp_unit));
			else if(arg0.getId()==R.id.sol_slider_dist)
				DistTextView.setText(SolUtils.formatToastDist(getString(R.string.sol_dist_text), arg1 + 1, PREF_dist_unit));

		}
		catch(Exception e){
			return;
		}

	}

	//@Override
	public void onStartTrackingTouch(SeekBar arg0) {

		try{
			// TODO Auto-generated method stub
			if(arg0.getId()==R.id.sol_slider_temp)
				TempTextView.setText(SolUtils.formatToastTemp(getString(R.string.sol_temp_text), arg0.getProgress(), PREF_temp_unit));
			else if(arg0.getId()==R.id.sol_slider_dist)
				DistTextView.setText(SolUtils.formatToastDist(getString(R.string.sol_dist_text), arg0.getProgress() + 1, PREF_dist_unit));
		}
		catch(Exception e){
			return;
		}
	}

	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * 
	 *			DOWNLOAD
	 */

	/*
	 * Broadcast receiver to change pref
	 */

	private class DownloadCitiesBroadcastReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			int download_progess = intent.getExtras().getInt("download_progess");
			alertManager.updateProgressDownload(download_progess);
		}
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if(updatePrefBroadcastReceiver!=null)
			unregisterReceiver(updatePrefBroadcastReceiver);


		DBManager.close();

		if(downloadCitiesBroadcastReceiver!=null)
			unregisterReceiver(downloadCitiesBroadcastReceiver);
	}	
}