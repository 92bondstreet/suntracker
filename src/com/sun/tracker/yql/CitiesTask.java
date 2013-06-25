package com.sun.tracker.yql;
import org.codehaus.jackson.JsonNode;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.sun.tracker.SolInvictus;
import com.sun.tracker.db.CitiesDB;
import com.sun.tracker.yql.DatabaseManager.DatabaseResult;


interface SubCitiesCompleteListener<T> {
	public void onSubCitiesComplete(T result);
}


public class CitiesTask implements SubCitiesCompleteListener<Boolean>
{
	private YQL yql_manager;
	
	private Context current_context;
	private Intent solinvictus_intent;
	
	// SQL LITE DATABASE
	private CitiesDB citiesDB;
	
	private CitiesResult citiesResult;
	
	// ERROR 
	public static final int OK = 0;
	public static final int ERROR = -1;
	public static int RETURN_CODE  = OK;
	
	// QUERIES
	private  int MAX_QUERIES = 20;
	private String CITIES_URL = "http://www.lessismoremag.com/solinvictus/json_solinvictus.php";
	private int COUNT_TASK = 0;
	
	private int QUEUE_TASK_SIZE = 5;
	private int COUNT_QUEUE_TASK = 0;
	private int COUNT_CURRENT_QUEUE_TASK = 0;
	private int TO_DO = 0;
	
	private int I_BEGIN_REQUEST = 0;
	
	private JsonNode json_cities_from_mysql;
	
	public CitiesTask(Context context, CitiesDB db) {
		
		citiesDB = db;
		yql_manager = new YQL();
		current_context = context;
		
		solinvictus_intent = new Intent(SolInvictus.ACTION_DOWNLOAD_CITIES);
	}
	
	public void downloadCities(CitiesResult result,String current_latitude, String current_longitude, String dist, String pop) {

		citiesResult = result;
			
		CitiesWeather(CITIES_URL, current_latitude, current_longitude, dist, pop );
	
	}
	
	
	private boolean CitiesWeather(String url, String current_latitude, String current_longitude, String dist, String pop){

		try{
			
			// step 0: get  mysql
			String request = url + "?android_lat="+current_latitude+"&android_lon="+current_longitude+"&android_dist="+dist+"&android_city_pop="+pop;
			json_cities_from_mysql = yql_manager.JSONfromURL(request);
			JsonNode json_data = null;
			
			// step 1: build multi query select
			int nb_cities = json_cities_from_mysql.size();
			int nb_requests =  nb_cities/MAX_QUERIES;
			int modulo_requests = nb_cities%MAX_QUERIES;
			
			COUNT_TASK = nb_requests;
			if(modulo_requests!=0)
				COUNT_TASK += 1;
			
			COUNT_QUEUE_TASK = COUNT_TASK / QUEUE_TASK_SIZE;
			COUNT_CURRENT_QUEUE_TASK = QUEUE_TASK_SIZE;
			
			launchTask();
			
			/*int i_request=0;
			String multiquery = "";
			for(i_request=0;i_request<nb_requests;i_request++){

				multiquery = "select * from query.multi where queries=\"";
				for(int j=0;j<MAX_QUERIES;j++){

					json_data = json_cities_from_mysql.get((i_request*MAX_QUERIES)+j);
					multiquery += "SELECT * FROM weather.woeid WHERE u='c' AND w IN (SELECT Results.woeid FROM yahoo.maps.findLocation where q='"+json_data.get("CityLatitude").getTextValue()+", "+json_data.get("CityLongitude").getTextValue()+"' and gflags='R' limit 1);";								
				}
				multiquery+="\"";
				
				SubCitiesTask subcitiestask = new SubCitiesTask(this, citiesDB, json_cities_from_mysql, i_request * MAX_QUERIES);
				subcitiestask.execute(multiquery);
				updateProgressInfo(1);
			}
			
			multiquery = "select * from query.multi where queries=\"";
			for(int j=0;j<modulo_requests;j++){

				json_data = json_cities_from_mysql.get((nb_requests*MAX_QUERIES)+j);
				multiquery += "SELECT * FROM weather.woeid WHERE u='c' AND w IN (SELECT Results.woeid FROM yahoo.maps.findLocation where q='"+json_data.get("CityLatitude").getTextValue()+", "+json_data.get("CityLongitude").getTextValue()+"' and gflags='R' limit 1);";								
			}
			multiquery+="\"";
			
			SubCitiesTask subcitiestask = new SubCitiesTask(this, citiesDB, json_cities_from_mysql, nb_requests * MAX_QUERIES);
			subcitiestask.execute(multiquery);
			updateProgressInfo(1);*/
		
		return true;
		}
		catch(Exception e){
			return false;
		}
	}

	private void launchTask(){
		
		COUNT_CURRENT_QUEUE_TASK = QUEUE_TASK_SIZE;
		
		String multiquery = "";
		JsonNode json_data = null;
		int i = I_BEGIN_REQUEST;
		for(i=I_BEGIN_REQUEST;i<1;i++){
			
			TO_DO++;
			if(TO_DO>COUNT_TASK)
				break;
			
			multiquery = "select * from query.multi where queries=\"";
			for(int j=0;j<MAX_QUERIES;j++){

				json_data = json_cities_from_mysql.get((i*MAX_QUERIES)+j);
				multiquery += "SELECT * FROM weather.woeid WHERE u='c' AND w IN (SELECT Results.woeid FROM yahoo.maps.findLocation where q='"+json_data.get("CityLatitude").getTextValue()+", "+json_data.get("CityLongitude").getTextValue()+"' and gflags='R' limit 1);";								
			}
			multiquery+="\"";
			
			SubCitiesTask subcitiestask = new SubCitiesTask(this, citiesDB, json_cities_from_mysql, i * MAX_QUERIES);
			subcitiestask.execute(multiquery);
			updateProgressInfo(1);
		}
			
		I_BEGIN_REQUEST = i;
	}
	
	
	/*
	 * 		DATABASE RESULT
	 * 
	 */
	public static abstract class CitiesResult{
		public abstract void gotCities(int getit);
	}
	
	public void databaseCompleted(){
		
		citiesResult.gotCities(RETURN_CODE);
	}

	
	/*
	 * 		TASK RESULT
	 * 
	 */
	
	/*
	 * 		NOTIFICATION
	 */

	private void updateProgressInfo(int value){

		solinvictus_intent.putExtra("download_progess", value);
		current_context.sendBroadcast(solinvictus_intent);
	}
	
	public void onSubCitiesComplete(Boolean result) {
		// TODO Auto-generated method stub
		
		updateProgressInfo(2);
		
		if(result)
			RETURN_CODE = OK;
		else
			RETURN_CODE = ERROR;
		COUNT_TASK--;
		
		Log.d("--> COUNT_TASK : ", "" + COUNT_TASK);
		
		if(COUNT_TASK<=0)
			databaseCompleted();
		
		COUNT_CURRENT_QUEUE_TASK--;
		if(COUNT_CURRENT_QUEUE_TASK<=0){
			launchTask();
		}
		
	}
	
}