package com.sun.tracker.yql;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.deser.FromStringDeserializer.CurrencyDeserializer;

import android.os.AsyncTask;
import android.util.Log;

import com.sun.tracker.db.CitiesDB;
import com.sun.tracker.parser.City;
import com.sun.tracker.utils.SolUtils;



public class SubCitiesTask extends CustomTask<String, Void, Boolean> 
{
	private YQL yql_manager;
	private SubCitiesCompleteListener<Boolean> callback;
	// SQL LITE DATABASE
	private CitiesDB citiesDB;
	private JsonNode json_cities_from_mysql;
	private int CURRENT_CITY_ID;


	public SubCitiesTask(SubCitiesCompleteListener<Boolean> cb, CitiesDB db,JsonNode json_cities, int id) {

		this.callback = cb;
		citiesDB = db;
		json_cities_from_mysql = json_cities;
		CURRENT_CITY_ID = id;

		yql_manager = new YQL();
	}

	@Override
	protected Boolean doInBackground(String... params) {

		int nbParams = params.length;
		if(nbParams == 1)
			return SubCitiesWeather(params[0]);

		return false;
	}

	protected void onPostExecute(Boolean result) 
	{
		callback.onSubCitiesComplete(result);
	}

	public  boolean SubCitiesWeather(String multiquery){

		try{

			String root = "http://query.yahooapis.com/v1/public/yql?format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&diagnostics=false";
			multiquery = java.net.URLEncoder.encode(multiquery.toString(), "ISO-8859-1");
			String request = root + "&q=" + multiquery;

			// step 2: call yql and decode
			JsonParser json_from_yql = yql_manager.JSONfromURL2(request);


			return SubBestCitiesWeather(json_from_yql);
		}
		catch(Exception e){
			return false;
		}
	}

	private  boolean SubBestCitiesWeather(JsonParser json_from_yql){//JsonNode json_from_yql){

		// valeur de retour : 
		// true on continue le parcours
		// false on arrete


		try{

			json_from_yql.nextToken();
			while (json_from_yql.nextToken() != JsonToken.END_OBJECT) {
				String fieldname = json_from_yql.getCurrentName();
				Log.d("",json_from_yql.getText());
				json_from_yql.nextToken();
				if(fieldname!=null){
					if(fieldname.equals("channel")){
						while (json_from_yql.nextToken() != JsonToken.END_OBJECT) {
							String namefield = json_from_yql.getCurrentName();
							Log.d("",json_from_yql.getText());
							json_from_yql.nextToken(); // move to value

						}

					}
				}
			}


			/*JsonNode resultsArray = json_from_yql.get("query").get("results").get("results");			

			City sol_city = new City();

			for (JsonNode node : resultsArray){
				JsonNode rss = node.get("rss");
				JsonNode item = rss.get("channel").get("item");
				JsonNode condition = item.get("condition");
				JsonNode location = rss.get("channel").get("location");

				sol_city.reset();
				sol_city.temp = Integer.parseInt(condition.get("temp").getTextValue());
				String yahoo_code = condition.get("code").getTextValue();
				sol_city.yahoo_code = Integer.parseInt(yahoo_code);
				sol_city.code = yql_manager.getAndroidCode(yahoo_code);

				sol_city.name = location.get("city").getTextValue();
				sol_city.country = location.get("country").getTextValue();
				sol_city.latitude = item.get("lat").getTextValue();
				sol_city.longitude = item.get("long").getTextValue();
				double distance = Double.parseDouble( json_cities_from_mysql.get(CURRENT_CITY_ID).get("distance").getTextValue() );							
				sol_city.distance = SolUtils.roundTwoDecimals(distance);
				sol_city.temp = Integer.parseInt(condition.get("temp").getTextValue());
				sol_city.pop = Integer.parseInt( json_cities_from_mysql.get(CURRENT_CITY_ID).get("CityPop").getTextValue() );

				citiesDB.insertCity(sol_city.copy());				
				CURRENT_CITY_ID++;

			}*/

			return true;
		}
		catch(Exception e){
			return false;
		}

	}

}