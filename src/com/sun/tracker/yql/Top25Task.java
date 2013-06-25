package com.sun.tracker.yql;
import java.util.ArrayList;

import org.codehaus.jackson.JsonNode;

import com.sun.tracker.db.Top25DB;
import com.sun.tracker.db.WeatherLocalDB;
import com.sun.tracker.parser.City;

import android.content.Context;
import android.os.AsyncTask;


public class Top25Task extends AsyncTask<String, Void, Boolean> 
{
	private YQL yql_manager;
	private Top25CompleteListener<Boolean> callback;
	// SQL LITE DATABASE
	private Top25DB top25DB;
	
	private String TOP_25_URL ="http://www.lessismoremag.com/solinvictus/json_topcities.php";
	
	public Top25Task(Top25CompleteListener<Boolean> cb, Top25DB db) {
		
		this.callback = cb;
		top25DB = db;
		
		yql_manager = new YQL();
	}
	
	@Override
	protected Boolean doInBackground(String... params) {
		
		int nbParams = params.length;
		if(nbParams==1){
			return BestWorldWeather(TOP_25_URL,params[0]);
		}
		
		return false;
	}
	
	protected void onPostExecute(Boolean result) 
	{
		callback.onTop25Complete(result);
	}
	
	public Boolean BestWorldWeather(String url, String month){

		try{
			// step 0: get 25 cities from mysql
			String request = url + "?android_month="+month;
			JsonNode json_from_mysql = yql_manager.JSONfromURL(request);

			// step 1: build multi query select
			String multiquery ="";
			for (JsonNode node : json_from_mysql)
				multiquery+="SELECT * FROM weather.woeid WHERE w='"+node.get("CityWoeid").getTextValue()+"' and u='c';";
			multiquery = "select * from query.multi where queries="+ "\"" + multiquery + "\"";

			String root = "http://query.yahooapis.com/v1/public/yql?format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&diagnostics=false";
			multiquery = java.net.URLEncoder.encode(multiquery.toString(), "ISO-8859-1");
			request = root + "&q=" + multiquery;


			// step 2: call yql and decode
			JsonNode json_from_yql = yql_manager.JSONfromURL(request);
			JsonNode resultsArray = json_from_yql.get("query").get("results").get("results");

			City top_city = new City();
			int id=0;
			for (JsonNode node : resultsArray){
				JsonNode rss = node.get("rss");
				JsonNode condition = rss.get("channel").get("item").get("condition");
				JsonNode location = rss.get("channel").get("location");

				top_city.reset();

				top_city.name = location.get("city").getTextValue();
				top_city.country = location.get("country").getTextValue();
				top_city.continent = json_from_mysql.get(id).get("CityContinent").getTextValue();
				top_city.temp = Integer.parseInt(condition.get("temp").getTextValue());
				String yahoo_code = condition.get("code").getTextValue();
				top_city.yahoo_code = Integer.parseInt(yahoo_code);
				top_city.code = yql_manager.getAndroidCode(yahoo_code);

				top25DB.insertTopCity(top_city.copy()); 
				
				id++;
			}
			return true;
		}
		catch(Exception e){
			return false;
		}
	}

	
}