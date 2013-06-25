package com.sun.tracker.yql;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.sun.tracker.SolInvictus;
import com.sun.tracker.db.CitiesDB;
import com.sun.tracker.db.Top25DB;
import com.sun.tracker.db.WeatherLocalDB;
import com.sun.tracker.gps.SolGpsListener;
import com.sun.tracker.gps.SolGpsListener.LocationResult;
import com.sun.tracker.parser.City;
import com.sun.tracker.yql.CitiesTask.CitiesResult;


interface WeatherLocalCompleteListener<T> {
	public void onWeatherLocalComplete(T result);
}

interface Top25CompleteListener<T> {
	public void onTop25Complete(T result);
}


public class DatabaseManager implements WeatherLocalCompleteListener<Boolean>, 
Top25CompleteListener<Boolean>{


	private SolGpsListener locationListener;
	private Context current_context;
	private Intent solinvictus_intent;
	private DatabaseResult databaseResult;

	// CURRENT POSTION
	private Location current_location;
	private double current_latitude = 0;
	private double current_longitude = 0;

	private String population = "100000";

	// INTERNET - NETWORK - GPS
	private BroadcastReceiver internetBroadcastReceiver;
	private WeatherLocalTask weatherLocalTask;
	private Top25Task top25Task;
	private CitiesTask citiesTask;
	//private LocationManagerListener locationListener;
	private boolean NETWORK_STATUS_ON= false;

	// SQL LITE DATABASE
	private WeatherLocalDB weatherLocalDB;
	private Top25DB top25DB;
	private CitiesDB citiesDB;

	// ERROR 
	public static final int OK = 0;
	public static final int NO_POSITION = 1;
	public static final int NO_NETWORK = 2;
	public static final int NO_LOCAL_WEATHER = 3;
	public static final int NO_TOPCITIES = 4;
	public static final int NO_CITIES = 5;
	public static final int NOTHING = 6;
	public static int RETURN_CODE  = OK;

	// FINISH
	public int COUNT_TASK = 3;


	public DatabaseManager(Context context,String pop){
		locationListener = new SolGpsListener();
		current_context = context;
		population = pop;

		installInternetListener();
		solinvictus_intent = new Intent(SolInvictus.ACTION_DOWNLOAD_CITIES);

		//Création d'une instance de ma classe LivresBDD
		weatherLocalDB = new WeatherLocalDB(context);
		//On ouvre la base de données pour écrire dedans
		weatherLocalDB.open();

		top25DB = new Top25DB(context);
		top25DB.open();

		citiesDB = new CitiesDB(context);
		citiesDB.open();
	}

	public int download(DatabaseResult result){

		databaseResult = result;

		// 0. Get Current Position 
		if(!locationListener.getLocation(current_context, locationResult))
			return NO_POSITION;

		updateProgressInfo(5);


		return OK;
	}

	public void close(){
		weatherLocalDB.close();
		top25DB.close();
	}

	/*
	 * 		DATABASE RESULT
	 * 
	 */
	public static abstract class DatabaseResult{
		public abstract void gotDatabase(int getit);
	}

	public void databaseCompleted(){

		if(internetBroadcastReceiver!=null)
			current_context.unregisterReceiver(internetBroadcastReceiver);

		databaseResult.gotDatabase(RETURN_CODE);
	}


	/*
	 * 	GPS LOCATION
	 * 
	 */

	public LocationResult locationResult = new LocationResult(){

		@Override
		public void gotLocation(Location location) {
			// TODO Auto-generated method stub

			//if(!LOCAL_WEATHER_RUNNING){
			if(location != null){				
				// get Latitude/longitude
				current_location = location;
				current_latitude = location.getLatitude();
				current_longitude = location.getLongitude();

				updateProgressInfo(5);

				// call solme
				if(NETWORK_STATUS_ON){
					callWeatherLocal();
					callTop25();	
					callCities();
				}
			}

			// in case where : 
			// - no results
			// _ no network
			//alertManager.showCloseApp(	getString(R.string.alert_no_local_weather), 
			//		getString(R.string.alert_no_local_weather_title),
			//		getString(R.string.alert_no_local_weather_button));
			return;		
		}
		//		}

	};

	/*
	 * 	INTERNET
	 * 
	 */

	private void installInternetListener() {

		if (internetBroadcastReceiver == null) {
			internetBroadcastReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context arg0, Intent arg1) {
					// TODO Auto-generated method stub
					Bundle extras = arg1.getExtras();

					NetworkInfo info = (NetworkInfo) extras
					.getParcelable("networkInfo");

					android.net.NetworkInfo.State state = info.getState();

					if (state == android.net.NetworkInfo.State.CONNECTED)
						NETWORK_STATUS_ON = true;
					else 
						NETWORK_STATUS_ON = false;
				}
			};

			final IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			current_context.registerReceiver(internetBroadcastReceiver, intentFilter);
		}
	}

	/*
	 * 		NOTIFICATION
	 */

	private void updateProgressInfo(int value){

		solinvictus_intent.putExtra("download_progess", value);
		current_context.sendBroadcast(solinvictus_intent);
	}

	/*
	 * 		LOCAL WEATHER
	 */
	private void callWeatherLocal(){
		try{
			weatherLocalDB.cleanBDD();
			weatherLocalTask = new WeatherLocalTask(this, weatherLocalDB);
			weatherLocalTask.execute(String.valueOf(current_latitude),String.valueOf(current_longitude));
		}
		catch(Exception e){
			RETURN_CODE = NO_LOCAL_WEATHER;
			COUNT_TASK--;
			if(COUNT_TASK==0)
				databaseCompleted();
		}
	}

	public void onWeatherLocalComplete(Boolean result) {
		// TODO Auto-generated method stub
		updateProgressInfo(5);
		if(result)
			RETURN_CODE = OK;
		else
			RETURN_CODE = NO_LOCAL_WEATHER;
		COUNT_TASK--;
		if(COUNT_TASK==0)
			databaseCompleted();
		return;
	}

	public City getCurrentWeather(){
		try{
			return weatherLocalDB.getCurrentCity();
		}
		catch(Exception e){
			return null;
		}
	}

	/*
	 * 		TOP 25
	 */

	private void callTop25(){

		try{
			top25DB.cleanBDD();
			top25Task = new Top25Task(this, top25DB);
			String current_month = String.valueOf((Calendar.getInstance(Locale.getDefault())).get(Calendar.MONTH) + 1);
			top25Task.execute(current_month);
		}
		catch(Exception e){
			RETURN_CODE = NO_TOPCITIES;
			COUNT_TASK--;
			if(COUNT_TASK==0)
				databaseCompleted();
		}
	}

	public void onTop25Complete(Boolean result) {
		// TODO Auto-generated method stub
		updateProgressInfo(5);
		if(result)
			RETURN_CODE = OK;
		else
			RETURN_CODE = NO_TOPCITIES;
		COUNT_TASK--;
		if(COUNT_TASK==0)
			databaseCompleted();
		return;
	}

	public ArrayList<City> getTop25(){
		return top25DB.getTop25();
	}

	/*
	 * 		CITIES
	 */

	private void callCities(){

		try{
			citiesDB.cleanBDD();
			citiesTask = new CitiesTask(current_context, citiesDB);

			//citiesTask.execute(String.valueOf(current_latitude),String.valueOf(current_longitude),"2000",population);
			citiesTask.downloadCities(citiesResult, String.valueOf(current_latitude),String.valueOf(current_longitude),"2000",population);
		}
		catch(Exception e){
			RETURN_CODE = NO_TOPCITIES;
			COUNT_TASK--;
			if(COUNT_TASK==0)
				databaseCompleted();
		}
	}

	public CitiesResult citiesResult = new CitiesResult(){

		@Override
		public void gotCities(int getit) {

			updateProgressInfo(5);
			if(getit == OK)
				RETURN_CODE = OK;
			else
				RETURN_CODE = NO_CITIES;
			COUNT_TASK--;
			if(COUNT_TASK==0)
				databaseCompleted();

		}
	};

	public ArrayList<City> getCities(String distance, String temp, String code, String population, String results){
		return citiesDB.getCities(distance, temp, code, population, results);
	}
	
	public int getTempMax()
	{
		return citiesDB.getTempMax();
	}
	
	public int getDistMax()
	{
		return citiesDB.getDistMax();
	}

}