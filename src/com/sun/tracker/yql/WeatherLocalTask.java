package com.sun.tracker.yql;
import java.util.ArrayList;

import org.codehaus.jackson.JsonNode;

import com.sun.tracker.db.WeatherLocalDB;
import com.sun.tracker.parser.City;

import android.content.Context;
import android.os.AsyncTask;


public class WeatherLocalTask extends AsyncTask<String, Void, Boolean> 
{
	private YQL yql_manager;
	private WeatherLocalCompleteListener<Boolean> callback;
	// SQL LITE DATABASE
	private WeatherLocalDB weatherLocalDB;
	
	public WeatherLocalTask(WeatherLocalCompleteListener<Boolean> cb, WeatherLocalDB db) {
		
		this.callback = cb;
		weatherLocalDB = db;
		
		yql_manager = new YQL();
	}
	
	@Override
	protected Boolean doInBackground(String... params) {
		
		int nbParams = params.length;
		if(nbParams==2){
			return WeatherFromLatLon(params[0],params[1]);
		}
		
		return false;
	}
	
	protected void onPostExecute(Boolean result) 
	{
		callback.onWeatherLocalComplete(result);
	} 
	
	public boolean WeatherFromLatLon(String lat, String lon){

		try{
			String multiquery = "select * from weather.woeid where u='c' and w in (select Results.woeid from yahoo.maps.findLocation where q= \""+lat+", "+lon+"\" and gflags=\"R\"limit 1)";
			multiquery = java.net.URLEncoder.encode(multiquery.toString(), "ISO-8859-1");

			String root = "http://query.yahooapis.com/v1/public/yql?format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&diagnostics=false";

			String request = root + "&q=" + multiquery;

			// get json from request
			JsonNode jsonNode = yql_manager.JSONfromURL(request);
			JsonNode rss = jsonNode.get("query").get("results").get("rss");
			JsonNode condition = rss.get("channel").get("item").get("condition");
			JsonNode location = rss.get("channel").get("location");

			City solme_city = new City();

			solme_city.name = location.get("city").getTextValue();
			solme_city.country = location.get("country").getTextValue();
			solme_city.latitude = lat;
			solme_city.longitude = lon;
			solme_city.temp = Integer.parseInt(condition.get("temp").getTextValue());
			String yahoo_code = condition.get("code").getTextValue();
			solme_city.yahoo_code = Integer.parseInt(yahoo_code);
			solme_city.code = yql_manager.getAndroidCode(yahoo_code);

			weatherLocalDB.updateCity(solme_city);
			
			return true;

		} catch (Exception e) {
			return false;
		}

	}

	
}