package com.sun.tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import com.sun.tracker.parser.City;
import com.sun.tracker.parser.CityDistanceComparator;
import com.sun.tracker.parser.CityNameComparator;
import com.sun.tracker.parser.CityTempComparator;
import com.sun.tracker.parser.CityWeatherComparator;
import com.sun.tracker.tabhost.ScrollableTabActivity;
import com.sun.tracker.tabhost.SolTabhost;
import com.sun.tracker.utils.SolMessageManager;
import com.sun.tracker.utils.SolUtils;

public class SolCities extends Activity{

	private ListView SolCitiesListView;
	private TextView SolCitiesTextView;
	private ArrayList<City> currentSolcities;
	private Bundle extras;

	// CURRENT LOCATION
	private String current_latitude;
	private String current_longitude;

	// MESSAGE MANAGER
	private SolMessageManager alertManager;

	// SORT 
	private int CURRENT_SORT = 0;
	
	// PREFERENCES
	private IntentFilter updatePrefIntentFilter;
	private UpdatePrefBroadcastReceiver updatePrefBroadcastReceiver;
	public static String ACTION_UPDATE_PREF = "com.solinvictus.updatepref";


	// Comparaor
	CityNameComparator nameComparator = new CityNameComparator();
	CityDistanceComparator distanceComparator = new CityDistanceComparator();
	CityTempComparator tempComparator = new CityTempComparator();
	CityWeatherComparator weatherComparator = new CityWeatherComparator();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.solcities_layout);

		alertManager = new SolMessageManager(this);
		
		//0. Initialisation des composants
		initHMI();
		
		// preferences
		updatePrefIntentFilter = new IntentFilter(ACTION_UPDATE_PREF);
    	updatePrefBroadcastReceiver = new UpdatePrefBroadcastReceiver();
    	registerReceiver(updatePrefBroadcastReceiver, updatePrefIntentFilter);

		showResults(this.getIntent());
	}

	private void initHMI(){
		
		// background
		BitmapFactory.Options opts = new BitmapFactory.Options();    	
		opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		opts.inDither = true;
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.sol_background_black, opts);
		BitmapDrawable bitmapDrawable = new BitmapDrawable(bmp);
		bitmapDrawable.setDither(true);
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.solcities_layout);	        
		linearLayout.setBackgroundDrawable(bitmapDrawable);
		
		SolCitiesListView = (ListView)findViewById(R.id.SolCities);
		SolCitiesListView.setOnItemClickListener(solItemClickListener);
		
		SolCitiesTextView = (TextView)findViewById(R.id.SolCitiesMessage);
	}
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);

		if(intent.getAction().equals(SolTabhost.ACTION_VIEW_RESULTS))
			checkResults(intent);
		if(intent.getAction().equals(SolTabhost.ACTION_UPDATE_RESULTS))
			showResults(intent);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		// hide toast
		if(alertManager!=null)
			alertManager.hide();
	}

	/*
	 * 
	 * SHOW RESULTS
	 * 
	 */

	private boolean checkResults(Intent intent){

		extras = intent.getExtras();
		// 0. if extras null, not yet results : need to search
		if(extras==null){
			SolCitiesListView.setVisibility(View.GONE);
			SolCitiesTextView.setText(getString(R.string.alert_not_yet_results));
			SolCitiesTextView.setVisibility(View.VISIBLE);
			return false;
		}

		currentSolcities = extras.getParcelableArrayList("solcities");
		//0. if currentSolcities is empty, no results : change criterias
		if(currentSolcities==null || currentSolcities.size()==0){
			SolCitiesListView.setVisibility(View.GONE);
			SolCitiesTextView.setText(getString(R.string.alert_no_results));
			SolCitiesTextView.setVisibility(View.VISIBLE);			
			return false;
		}

		return true;
	}


	private void showResults(Intent intent){

		// get ArrayList from intent
		if(checkResults(intent)){

			SolCitiesListView.setVisibility(View.VISIBLE);
			SolCitiesTextView.setVisibility(View.GONE);
			
			current_latitude = extras.getString("current_latitude");
			current_longitude = extras.getString("current_longitude");

			//1. sort by distance (default)
			sortCitiesByDistance();
		}
		else{
			SimpleAdapter sa = new SimpleAdapter (this.getBaseContext(), new  ArrayList<HashMap<String, String>>(), 
					R.layout.custom_solcities,null, null);
			SolCitiesListView.setAdapter(sa);
		}
		return;
	}

	private void updateSolCities(){

		//Création d'un SimpleAdapter qui se chargera de mettre les items présent dans notre list (listItem) dans la vue affichageitem
		SimpleAdapter saSolcities = createSimpleAdapter();

		if(saSolcities!=null){		
			//	On attribut à notre listView l'adapter que l'on vient de créer
			SolCitiesListView.setAdapter(saSolcities);
			SolCitiesListView.setClickable(true);	
		}
	}

	private SimpleAdapter createSimpleAdapter(){

		//Création de la ArrayList qui nous permettra de remplire la listView
		ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
		//On déclare la HashMap qui contiendra les informations pour un item
		HashMap<String, String> map;

		for(City current:currentSolcities){
			//On refait la manip plusieurs fois avec des données différentes pour former les items de notre ListView
			map = new HashMap<String, String>();
			map.put("CodeImg",getImgFromCode(current.code));
			map.put("CityTemp",updateTempWithPreferences(current.temp));
			map.put("CityCurrentTemp",Integer.toString(current.temp));
			map.put("CityName", current.name);
			map.put("CityCountry",current.country);
			map.put("CityDistance",updateDistWithPreferences(current.distance));
			map.put("CityCurrentDistance",Double.toString(current.distance));
			map.put("CityLatitude",current.latitude);
			map.put("CityLongitude",current.longitude);
			map.put("CityCode",getString(SolUtils.loadTextFromYahooCode(current.yahoo_code)));

			listItem.add(map);
		}

		String[] from = new String[] {"CodeImg","CityTemp", "CityName", "CityCountry", "CityDistance", "CityCode"};
		int to[] = new int[] {R.id.CodeImg, R.id.CityTemp, R.id.CityName, R.id.CityCountry, R.id.CityDistance, R.id.CityCode};

		//Création d'un SimpleAdapter qui se chargera de mettre les items présent dans notre list (listItem) dans la vue affichageitem
		return new SimpleAdapter (this.getBaseContext(), listItem, R.layout.custom_solcities,from, to);
	}

	private String getImgFromCode(int code){

		switch(code){
		case SolInvictus.ALL_WEATHER:
			return  String.valueOf(R.drawable.sol_local_code_3);
		case SolInvictus.CLOUD_WEATHER:
			return  String.valueOf(R.drawable.sol_local_code_2);
		case SolInvictus.SUN_WEATHER:
			return  String.valueOf(R.drawable.sol_local_code_1);
		default:
			return String.valueOf(R.drawable.sol_local_code_3);
		}
	}



	/*
	 * 
	 *			MENU
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.solcities_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle all of the possible menu actions.
		switch (item.getItemId()) {
		case R.id.SortItem:{
			sortCities(item);
			break;
		}
		case R.id.QuitItem:{
			this.finish();
			break;
		}
		}
		//return super.onOptionsItemSelected(item);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		if(menu!=null)
			menu.clear();
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.solcities_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		// TODO Auto-generated method stub

		MenuItem currentitem = menu.getItem(featureId);
		switch (CURRENT_SORT) {
		case 0:
			currentitem.setTitle(getString(R.string.menu_sort_temp));
			currentitem.setIcon(android.R.drawable.ic_menu_sort_by_size);
			// update menu
			break;
		case 1:
			currentitem.setTitle(getString(R.string.menu_sort_name));
			currentitem.setIcon(android.R.drawable.ic_menu_sort_alphabetically);
			break;
		case 2:
			currentitem.setTitle(getString(R.string.menu_sort_weather));
			currentitem.setIcon(R.drawable.ic_menu_sort_by_weather);
			break;
		case 3:
			currentitem.setTitle(getString(R.string.menu_sort_dist));
			currentitem.setIcon(android.R.drawable.ic_menu_mylocation);
			break;
		default:
			currentitem.setTitle(getString(R.string.menu_sort_dist));
			break;
		}
		return super.onMenuOpened(featureId, menu);
	}

	/*
	 * 
	 * COMPARATOR
	 * 
	 */

	private void sortCities(MenuItem menu){

		if(currentSolcities==null || currentSolcities.size()==0)
			return;

		switch (CURRENT_SORT) {
		case 0:
			sortCitiesByTemp();
			// update menu
			break;
		case 1:
			sortCitiesByName();
			break;
		case 2:
			sortCitiesByWeather();
			break;
		case 3:
			sortCitiesByDistance();
			break;
		default:
			sortCitiesByDistance();
			break;
		}

		CURRENT_SORT++;
		if(CURRENT_SORT>3)
			CURRENT_SORT = 0;
	}


	private void sortCitiesByName(){

		Collections.sort(currentSolcities, nameComparator);
		// http://stackoverflow.com/questions/3166796/java-sorting-multiple-arraylists-synchronously-or-a-single-mapped-arraylist
		updateSolCities();
	}

	private void sortCitiesByDistance(){

		Collections.sort(currentSolcities, distanceComparator);
		updateSolCities();
	}

	private void sortCitiesByTemp(){

		Collections.sort(currentSolcities, tempComparator); 
		updateSolCities();
	}

	private void sortCitiesByWeather(){

		Collections.sort(currentSolcities, weatherComparator); 
		updateSolCities();
	}


	/*
	 * 
	 * 	EVENT
	 */

	public OnItemClickListener solItemClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			//callGoogleMaps();
			HashMap<String, String> map = (HashMap<String, String>) arg0.getAdapter().getItem(arg2);
			callGoogleMaps(map.get("CityLatitude"),map.get("CityLongitude"));
			return;
		}
	};

	private void callGoogleMaps(String center_on_latitude, String center_on_longitude){

		// load map in new activity to center
		Bundle extras = new Bundle();
		extras.putString("center_on_latitude", center_on_latitude);
		extras.putString("center_on_longitude", center_on_longitude);
		extras.putInt(ScrollableTabActivity.CURRENT_TAB_INDEX, 3);

		Intent intent = new Intent(ScrollableTabActivity.ACTION_CHANGE_TAB);
		//intent.setAction(ScrollableTabActivity.ACTION_CENTER_MAP);
		intent.putExtras(extras);
		sendBroadcast(intent);
	}
	
	/*
	 * 			PREFERENCES
	 * 
	 */
	
	/*
     * Broadcast receiver to change pref
     */
    
    private class UpdatePrefBroadcastReceiver extends BroadcastReceiver
    {
    	@Override
    	public void onReceive(Context context, Intent intent)
    	{
    		updateHMIWithPreferences();
    	}
    }
    
    private void updateHMIWithPreferences(){
    	
    	SimpleAdapter listItem = (SimpleAdapter) SolCitiesListView.getAdapter();
    	int size = listItem.getCount();

		for(int i=0;i<size;i++){
			
			HashMap<String, String> map = (HashMap<String, String>) listItem.getItem(i);
			
			int current_temp = Integer.valueOf(map.get("CityCurrentTemp"));	
			map.put("CityTemp",updateTempWithPreferences(current_temp));
			
			double current_dist = Double.valueOf(map.get("CityCurrentDistance"));	
			map.put("CityDistance",updateDistWithPreferences(current_dist));
		}
		
		listItem.notifyDataSetChanged();
    }
    
    private String updateTempWithPreferences(int temp_value){
    
    	String temp = String.valueOf(temp_value);
		if(SolInvictus.PREF_temp_unit.equals("f"))
			temp = String.valueOf(SolUtils.CelciusToFahrenheit(temp_value));
		
		temp += "°" + SolInvictus.PREF_temp_unit.toUpperCase();
		
		return temp;
    }
    
    private String updateDistWithPreferences(double dist_value){
        
    	String dist = String.valueOf(dist_value);
		if(SolInvictus.PREF_dist_unit.equals("mi"))
			dist = String.valueOf(SolUtils.KmToMiles((int)dist_value));
		dist += SolInvictus.PREF_dist_unit;
		return dist;
    }
    
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if(updatePrefBroadcastReceiver!=null)
			unregisterReceiver(updatePrefBroadcastReceiver);
	}
}