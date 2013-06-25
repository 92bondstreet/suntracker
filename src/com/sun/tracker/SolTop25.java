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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sun.tracker.parser.City;
import com.sun.tracker.parser.CityContinentComparator;
import com.sun.tracker.parser.CityCountryComparator;
import com.sun.tracker.parser.CityNameComparator;
import com.sun.tracker.parser.CityTempComparator;
import com.sun.tracker.parser.CityWeatherComparator;
import com.sun.tracker.tabhost.SolTabhost;
import com.sun.tracker.utils.SolMessageManager;
import com.sun.tracker.utils.SolUtils;

public class SolTop25 extends Activity{

	private ListView Top25ListView;
	private TextView Top25TextView;
	private ArrayList<City> currentTopcities;
	private Bundle extras;


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
	CityCountryComparator countryComparator = new CityCountryComparator();
	CityContinentComparator continentComparator = new CityContinentComparator();
	CityTempComparator tempComparator = new CityTempComparator();
	CityWeatherComparator weatherComparator = new CityWeatherComparator();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.top25_layout);

		alertManager = new SolMessageManager(this);
		
		//0. Initialisation des composants
		initHMI();
		
		// preferences
		updatePrefIntentFilter = new IntentFilter(ACTION_UPDATE_PREF);
    	updatePrefBroadcastReceiver = new UpdatePrefBroadcastReceiver();
    	registerReceiver(updatePrefBroadcastReceiver, updatePrefIntentFilter);

		Intent intent = this.getIntent();
		if(intent.getAction().equals(SolTabhost.ACTION_RUNNING_TOP25)){
			Top25ListView.setVisibility(View.GONE);
			
			Top25TextView.setText(getString(R.string.alert_not_yet_top25));
			Top25TextView.setVisibility(View.VISIBLE);
			return;
		}
		else
			showResults(intent);
	}
	
	private void initHMI(){
		
		// background
		BitmapFactory.Options opts = new BitmapFactory.Options();    	
		opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		opts.inDither = true;
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.sol_background_black, opts);
		BitmapDrawable bitmapDrawable = new BitmapDrawable(bmp);
		bitmapDrawable.setDither(true);
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.top25_layout);	        
		linearLayout.setBackgroundDrawable(bitmapDrawable);
		
		Top25ListView = (ListView)findViewById(R.id.Top25);
		Top25TextView = (TextView)findViewById(R.id.Top25Message);
	}
	

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);

		String intent_action = intent.getAction();
		
		if(intent_action.equals(SolTabhost.ACTION_RUNNING_TOP25)){
			Top25ListView.setVisibility(View.GONE);
			
			Top25TextView.setText(getString(R.string.alert_not_yet_top25));
			Top25TextView.setVisibility(View.VISIBLE);
			return;
		}
		
		if(intent_action.equals(SolTabhost.ACTION_VIEW_TOP25))
			checkResults(intent);
		if(intent_action.equals(SolTabhost.ACTION_UPDATE_TOP25))
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
		
		// 2. finished 
		extras = intent.getExtras();
		// 0. if extras null, not yet results : need to search
		if(extras==null){
			Top25ListView.setVisibility(View.GONE);
			
			Top25TextView.setText(getString(R.string.alert_no_top25));
			Top25TextView.setVisibility(View.VISIBLE);			
			return false;
		}

		currentTopcities = extras.getParcelableArrayList("top25");
		//0. if currentSolcities is empty, no results : change criterias
		if(currentTopcities==null || currentTopcities.size()==0){
			Top25ListView.setVisibility(View.GONE);
			
			Top25TextView.setText(getString(R.string.alert_no_top25));
			Top25TextView.setVisibility(View.VISIBLE);	
			return false;
		}

		return true;
	}


	private void showResults(Intent intent){

		// get ArrayList from intent
		if(checkResults(intent)){
			//1. sort by city name
			Top25ListView.setVisibility(View.VISIBLE);
			Top25TextView.setVisibility(View.GONE);
			sortCitiesByName();
		}
		
		return;
	}

	private void updateTopCities(){

		//Création d'un SimpleAdapter qui se chargera de mettre les items présent dans notre list (listItem) dans la vue affichageitem
		SimpleAdapter saSolcities = createSimpleAdapter();

		if(saSolcities!=null){		
			//	On attribut à notre listView l'adapter que l'on vient de créer
			Top25ListView.setAdapter(saSolcities);
			Top25ListView.setClickable(true);	
		}
	}

	private SimpleAdapter createSimpleAdapter(){

		//Création de la ArrayList qui nous permettra de remplire la listView
		ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
		//On déclare la HashMap qui contiendra les informations pour un item
		HashMap<String, String> map;

		for(City current:currentTopcities){
			//On refait la manip plusieurs fois avec des données différentes pour former les items de notre ListView
			map = new HashMap<String, String>();
			map.put("TopCodeImg",getImgFromCode(current.code));
			map.put("TopCityTemp",updateTempWithPreferences(current.temp));
			map.put("TopCityCurrentTemp",Integer.toString(current.temp));
			map.put("TopCityName", current.name);
			map.put("TopCityCountry",current.country);
			map.put("TopCityContinent",getStringFromContinent(current.continent));
			map.put("TopCityCode",getString(SolUtils.loadTextFromYahooCode(current.yahoo_code)));
			
			
			listItem.add(map);
		}

		String[] from = new String[] {"TopCodeImg","TopCityTemp", "TopCityName", "TopCityCountry", "TopCityContinent", "TopCityCode"};
		int to[] = new int[] {R.id.TopCodeImg, R.id.TopCityTemp, R.id.TopCityName, R.id.TopCityCountry, R.id.TopCityContinent, R.id.TopCityCode};

		//Création d'un SimpleAdapter qui se chargera de mettre les items présent dans notre list (listItem) dans la vue affichageitem
		return new SimpleAdapter (this.getBaseContext(), listItem, R.layout.custom_top25,from, to);
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

	private String getStringFromContinent(String continent){

		if(continent.equals("america"))
			return getString(R.string.yahoo_america);
		else if(continent.equals("asia"))
			return getString(R.string.yahoo_asia);
		else if(continent.equals("africa"))
			return getString(R.string.yahoo_africa);
		else if(continent.equals("europa"))
			return getString(R.string.yahoo_europa);
		else return "";
	}


	/*
	 * 
	 *			MENU
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.soltop25_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle all of the possible menu actions.
		switch (item.getItemId()) {
		case R.id.TopSortItem:{
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
		inflater.inflate(R.menu.soltop25_menu, menu);
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
			currentitem.setTitle(getString(R.string.menu_sort_country));
			currentitem.setIcon(android.R.drawable.ic_menu_sort_alphabetically);
			break;
		case 2:
			currentitem.setTitle(getString(R.string.menu_sort_continent));
			currentitem.setIcon(android.R.drawable.ic_menu_mapmode);
			break;
		case 3:
			currentitem.setTitle(getString(R.string.menu_sort_weather));
			currentitem.setIcon(R.drawable.ic_menu_sort_by_weather);
			break;
		case 4:
			currentitem.setTitle(getString(R.string.menu_sort_name));
			currentitem.setIcon(android.R.drawable.ic_menu_sort_alphabetically);
			break;
		default:
			currentitem.setTitle(getString(R.string.menu_sort_name));
			currentitem.setIcon(android.R.drawable.ic_menu_sort_alphabetically);
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

		if(currentTopcities==null || currentTopcities.size()==0)
			return;

		switch (CURRENT_SORT) {
		case 0:
			sortCitiesByTemp();
			break;
		case 1:
			sortCitiesByCountry();
			break;
		case 2:
			sortCitiesByContinent();
			break;
		case 3:
			sortCitiesByWeather();
			break;
		case 4:
			sortCitiesByName();
			break;
		default:
			sortCitiesByName();
			break;
		}

		CURRENT_SORT++;
		if(CURRENT_SORT>4)
			CURRENT_SORT = 0;
	}


	private void sortCitiesByName(){

		Collections.sort(currentTopcities, nameComparator);
		// http://stackoverflow.com/questions/3166796/java-sorting-multiple-arraylists-synchronously-or-a-single-mapped-arraylist
		updateTopCities();
	}

	private void sortCitiesByCountry(){

		Collections.sort(currentTopcities, countryComparator);
		updateTopCities();
	}
	
	private void sortCitiesByContinent(){

		Collections.sort(currentTopcities, continentComparator);
		updateTopCities();
	}

	private void sortCitiesByTemp(){

		Collections.sort(currentTopcities, tempComparator); 
		updateTopCities();
	}

	private void sortCitiesByWeather(){

		Collections.sort(currentTopcities, weatherComparator); 
		updateTopCities();
	}
	
	/*
	 * 		PREFERENCES
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
    	
    	SimpleAdapter listItem = (SimpleAdapter) Top25ListView.getAdapter();
    	int size = listItem.getCount();

		for(int i=0;i<size;i++){
			
			HashMap<String, String> map = (HashMap<String, String>) listItem.getItem(i);
			
			int current_temp = Integer.valueOf(map.get("TopCityCurrentTemp"));	
			map.put("TopCityTemp",updateTempWithPreferences(current_temp));
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
    
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if(updatePrefBroadcastReceiver!=null)
			unregisterReceiver(updatePrefBroadcastReceiver);
	}
}