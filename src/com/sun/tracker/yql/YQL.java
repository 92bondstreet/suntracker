package com.sun.tracker.yql;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;
import android.content.Intent;

import com.sun.tracker.SolInvictus;
import com.sun.tracker.parser.City;
import com.sun.tracker.utils.SolUtils;

/**
 * Simple Demo to show the power of YQL
 * @see http://idojava.blogspot.com/
 * @author Green Ido
 */
public class YQL {

	private static final String[] SUN_CODE_1 = new String[] {"32","34","36"};
	private static final String[] SUN_CODE_2 = new String[] {"28","30","44"};

	private  int MAX_NB_RESULTS = 0;
	private  int CURRENT_NB_RESULTS = 0;
	private  int MAX_QUERIES = 20;

	private  ArrayList<City> SOLCITIES = new ArrayList<City>();

	private JsonNode json_cities_from_mysql;
	private ObjectMapper m;
	private int CURRENT_CITY_ID = 0;

	public YQL(){

		m = new ObjectMapper();
	}

	private  JsonNode JacksonJSON(InputStream istream) throws JsonParseException, IOException{


		// can either use mapper.readTree(JsonParser), or bind to JsonNode	
		return m.readValue(istream, JsonNode.class);
	}

	private  JsonParser StreamingAPI(InputStream istream) throws JsonParseException, IOException{

		JsonFactory f = new JsonFactory();
		return f.createJsonParser(istream);
	}

	public  JsonNode JSONfromURL(String request) throws Exception{

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet();

		URI uri = new URI(request);
		httpGet.setURI(uri);

		HttpResponse reponse = httpClient.execute(httpGet);

		InputStream rstream = null;
		rstream = reponse.getEntity().getContent();

		return JacksonJSON(rstream);

	}
	
	public  JsonParser JSONfromURL2(String request) throws Exception{

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet();

		URI uri = new URI(request);
		httpGet.setURI(uri);

		HttpResponse reponse = httpClient.execute(httpGet);

		InputStream rstream = null;
		rstream = reponse.getEntity().getContent();

		return StreamingAPI(rstream);

	}


	/**
	 * Find 'food' places for the JPR
	 * @param args
	 * @return 
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * @throws Exception
	 */

	public  ArrayList<City> WeatherFromLatLon(String lat, String lon){

		ArrayList<City> solme = new ArrayList<City>();

		try{
			String multiquery = "select * from weather.woeid where u='c' and w in (select Results.woeid from yahoo.maps.findLocation where q= \""+lat+", "+lon+"\" and gflags=\"R\"limit 1)";
			multiquery = java.net.URLEncoder.encode(multiquery.toString(), "ISO-8859-1");

			String root = "http://query.yahooapis.com/v1/public/yql?format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&diagnostics=false";

			String request = root + "&q=" + multiquery;

			// get json from request
			JsonNode jsonNode = JSONfromURL(request);
			JsonNode rss = jsonNode.get("query").get("results").get("rss");
			JsonNode condition = rss.get("channel").get("item").get("condition");
			JsonNode location = rss.get("channel").get("location");

			City solme_city = new City();

			solme_city.name = location.get("city").getTextValue();
			solme_city.country = location.get("country").getTextValue();
			solme_city.latitude = lat;
			solme_city.latitude = lon;
			solme_city.temp = Integer.parseInt(condition.get("temp").getTextValue());
			String yahoo_code = condition.get("code").getTextValue();
			solme_city.yahoo_code = Integer.parseInt(yahoo_code);
			solme_city.code = getAndroidCode(yahoo_code);

			solme.add(solme_city);
			return solme;

		} catch (Exception e) {
			return null;
		}

	}

	public  ArrayList<City> BestWorldWeather(String url, String month, String lang){

		ArrayList<City> top25 = new ArrayList<City>();

		try{
			// step 0: get 25 cities from mysql
			String request = url + "?android_month="+month;
			JsonNode json_from_mysql = JSONfromURL(request);

			// step 1: build multi query select
			String multiquery ="";
			for (JsonNode node : json_from_mysql)
				multiquery+="SELECT * FROM weather.woeid WHERE w='"+node.get("CityWoeid").getTextValue()+"' and u='c';";
			multiquery = "select * from query.multi where queries="+ "\"" + multiquery + "\"";

			String root = "http://query.yahooapis.com/v1/public/yql?format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&diagnostics=false";
			multiquery = java.net.URLEncoder.encode(multiquery.toString(), "ISO-8859-1");
			request = root + "&q=" + multiquery;


			// step 2: call yql and decode
			JsonNode json_from_yql =JSONfromURL(request);
			JsonNode resultsArray = json_from_yql.get("query").get("results").get("results");

			City top_city = new City();

			int id = 0;

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
				top_city.code = getAndroidCode(yahoo_code);

				top25.add(top_city.copy()); 

				id++;
			}
			return top25;
		}
		catch(Exception e){
			return null;
		}
	}

	public  ArrayList<City> BestCitiesWeather(Context context, String url, String current_latitude, String current_longitude, 
			String temp_min, 
			String dist, String temp_code,
			String num_results, String pop){

		SOLCITIES.clear();

		MAX_NB_RESULTS = Integer.parseInt(num_results);
		CURRENT_NB_RESULTS = 0;
		CURRENT_CITY_ID = 0;

		int ideal_temp = Integer.parseInt(temp_min);
		int ideal_weather = Integer.parseInt(temp_code);
		int percent_results = 1;

		Intent solinvictus_intent = new Intent(SolInvictus.ACTION_DOWNLOAD_CITIES);
		
		

		try{
			// step 0: get  mysql
			String request = url + "?android_lat="+current_latitude+"&android_lon="+current_longitude+"&android_dist="+dist+"&android_city_pop="+pop;
			json_cities_from_mysql =JSONfromURL(request);
			JsonNode json_data = null;

			int nb_cities = json_cities_from_mysql.size();

			// step 1: build multi query select

			int nb_requests =  nb_cities/MAX_QUERIES;
			int modulo_requests = nb_cities%MAX_QUERIES;
			int delta_percent_results = 50;
			if(nb_requests!=0)
				delta_percent_results = 100/nb_requests;

			String multiquery = "";
			String root = "http://query.yahooapis.com/v1/public/yql?format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&diagnostics=false";

			// 20 by 20 
			int i_request=0;
			for(i_request=0;i_request<nb_requests;i_request++){

				multiquery = "select * from query.multi where queries=\"";
				for(int j=0;j<MAX_QUERIES;j++){

					json_data = json_cities_from_mysql.get((i_request*MAX_QUERIES)+j);
					multiquery += "SELECT * FROM weather.woeid WHERE u='c' AND w IN (SELECT Results.woeid FROM yahoo.maps.findLocation where q='"+json_data.get("CityLatitude").getTextValue()+", "+json_data.get("CityLongitude").getTextValue()+"' and gflags='R' limit 1);";								
				}
				multiquery+="\"";
				multiquery = java.net.URLEncoder.encode(multiquery.toString(), "ISO-8859-1");
				request = root + "&q=" + multiquery;

				// step 2: call yql and decode
				JsonNode json_from_yql =JSONfromURL(request);
				
				solinvictus_intent.putExtra("download_progess", delta_percent_results);
				context.sendBroadcast(solinvictus_intent);

				if(!SubBestCitiesWeather(json_from_yql,ideal_temp,ideal_weather))
					break;
			}

			// finish with modulo
			if(CURRENT_NB_RESULTS < MAX_NB_RESULTS){

				multiquery = "select * from query.multi where queries=\"";
				for(int j=0;j<modulo_requests;j++){

					json_data = json_cities_from_mysql.get((i_request*MAX_QUERIES)+j);
					multiquery += "SELECT * FROM weather.woeid WHERE u='c' AND w IN (SELECT Results.woeid FROM yahoo.maps.findLocation where q='"+json_data.get("CityLatitude").getTextValue()+", "+json_data.get("CityLongitude").getTextValue()+"' and gflags='R' limit 1);";								
				}
				multiquery+="\"";
				multiquery = java.net.URLEncoder.encode(multiquery.toString(), "ISO-8859-1");
				request = root + "&q=" + multiquery;

				// step 2: call yql and decode
				JsonNode json_from_yql =JSONfromURL(request);
				
				solinvictus_intent.putExtra("download_progess", delta_percent_results);
				context.sendBroadcast(solinvictus_intent);

				SubBestCitiesWeather(json_from_yql,ideal_temp,ideal_weather);
			}

			return SOLCITIES;
		}catch(Exception e){
			return null;
		}
	}

	private  boolean SubBestCitiesWeather(JsonNode json_from_yql, int temp_min, int ideal_weather){

		// valeur de retour : 
		// true on continue le parcours
		// false on arrete

		try{

			JsonNode resultsArray = json_from_yql.get("query").get("results").get("results");

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
				sol_city.code = getAndroidCode(yahoo_code);

				if(codeIS_OK(ideal_weather, sol_city.code)){

					if(sol_city.temp >= temp_min){				
						sol_city.name = location.get("city").getTextValue();
						sol_city.country = location.get("country").getTextValue();
						sol_city.latitude = item.get("lat").getTextValue();
						sol_city.longitude = item.get("long").getTextValue();
						double distance = Double.parseDouble( json_cities_from_mysql.get(CURRENT_CITY_ID).get("distance").getTextValue() );							
						sol_city.distance = SolUtils.roundTwoDecimals(distance);
						sol_city.temp = Integer.parseInt(condition.get("temp").getTextValue());
						
						if(CURRENT_NB_RESULTS < MAX_NB_RESULTS){
							SOLCITIES.add(sol_city.copy());
							CURRENT_NB_RESULTS++;
						}
						else
							return false; // all max results
					}
				}

				CURRENT_CITY_ID++;
			}

			return true;
		}
		catch(Exception e){
			return false;
		}

	}

	public int getAndroidCode(String code){

		if(Arrays.asList(SUN_CODE_1).contains(code))
			return 1;
		else if(Arrays.asList(SUN_CODE_2).contains(code))
			return 2;
		else return 3;
	}

	private boolean codeIS_OK(int code_ok, int code_city){

		if(code_ok == 1){
			if(code_city==1)
				return true;
			else
				return false;
		}else 
			return true;
	}

}