package com.sun.tracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.os.AsyncTask;

import com.sun.tracker.parser.City;
import com.sun.tracker.yql.YQL;

public class HttpPostTask extends AsyncTask<String, Void, ArrayList<City>> 
{
	private Context context = null;
	private AsyncTaskCompleteListener<ArrayList<City>> callback;
	private ArrayList<City> response = null;
	
	private String TOP_25_URL ="http://www.lessismoremag.com/solinvictus/json_topcities.php";
	private String SOLCITIES_URL ="http://www.lessismoremag.com/solinvictus/json_solinvictus.php";
	
	//private String SOLCITIES_URL ="http://www.lessismoremag.com/solinvictus/solinvictus.php";
	//private String SOLME_URL ="http://www.lessismoremag.com/solinvictus/solme.php";
	//private String TOP_25_URL ="http://www.lessismoremag.com/solinvictus/topcities.php";
	/*
	private String SOLCITIES_URL ="http://www.punchlinemaster.fr/solinvictus/solinvictus.php";
	private String SOLME_URL ="http://www.punchlinemaster.fr/solinvictus/solme.php";
	private String TOP_25_URL ="http://www.punchlinemaster.fr/solinvictus/topcities.php";
	*/

	public HttpPostTask(Context context,AsyncTaskCompleteListener<ArrayList<City>> cb) {
		this.context = context;
		this.callback = cb;
	}

	protected void onPreExecute() 
	{
	}

	protected ArrayList<City> doInBackground(String... params) 
	{
		try 
		{
			int nbParams = params.length;

			if(nbParams == 2)
				// call top 25
				response = callTop25(params[0], params[1]);
			if(nbParams == 3)
				// call current weather
				response = callSolMe(params[0], params[1], params[2]); 
			else if(nbParams == 8)
				// call current weather
				response = callSolAroundMe(	params[0], params[1], params[2], 
											params[3], params[4], params[5], params[6], params[7] ); 

		}
		catch (Exception e) 
		{
			//error = e.getMessage();
			cancel(true);
			response = null;
		}

		return response;
	}

	protected void onPostExecute(ArrayList<City> result) 
	{
		callback.onTaskComplete(result);
	}  

	/*
	 * 		HTTP REQUEST
	 */

	private void setEADSProxy(DefaultHttpClient httpclient) {     
		final String PROXY_IP = "mandataire.elt.fr.ds.corp";     
		final int PROXY_PORT = 8080;     

		httpclient.getCredentialsProvider().setCredentials(     
				new AuthScope(PROXY_IP, PROXY_PORT),     
				new UsernamePasswordCredentials(     
						"frmain/yazzout", "P@ssw0rd11"));     

		HttpHost proxy = new HttpHost(PROXY_IP, PROXY_PORT);     

		httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,     
				proxy);     
	}
	
	private HttpParams setParameters(){
		
		HttpParams httpParameters = new BasicHttpParams();
		httpParameters.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		
		// Set the timeout in milliseconds until a connection is established.
		int timeoutConnection = 60000 * 5;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 60000 * 5;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		
		HttpConnectionParams.setSocketBufferSize(httpParameters, 51200);
		
		return httpParameters;
	}

	@SuppressWarnings("finally")
	private InputStream callHttpWithPost(String url, List<NameValuePair> postParameters ){

		StringBuffer stringBuffer = new StringBuffer("");
		BufferedReader bufferReader = null;
		InputStream xml_response = null;
		InputStream gzip_response = null;

		try{
			// send to url
			HttpClient httpClient = new DefaultHttpClient(setParameters());
			
			HttpPost httpPost = new HttpPost();
			httpPost.addHeader("Accept-Encoding", "gzip");
			httpPost.addHeader("Content-Type","application/x-www-form-urlencoded");
			
			// ----------
			// eads proxy
			//setEADSProxy((DefaultHttpClient) httpClient);
			// ----------

			URI address = new URI(url);			
			httpPost.setURI(address);

			// post values & request encoding
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParameters);
			httpPost.setEntity(formEntity);

			HttpResponse httpResponse = httpClient.execute(httpPost);
			gzip_response = httpResponse.getEntity().getContent();
			
			Header contentEncoding = httpResponse.getFirstHeader("Content-Encoding");
			if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip"))
				xml_response = new GZIPInputStream(gzip_response);
			else
				xml_response = null;
			
		} catch (ClientProtocolException e) {
			xml_response = null;

		} catch (IOException e) {
			xml_response = null;
		}
		catch(Exception e){
			xml_response = null;
		}
		finally{
			return xml_response;
		}

	}

	/*
	 * 		SPECIFIC HTTP REQUEST
	 */

	
	// local weather
	private ArrayList<City> callSolMe(String current_latitude, String current_longitude, String temp_unit) throws Exception{

		//geo fix 2.073889 48.771389

		// call url
		YQL yql_manager = new YQL();
		return yql_manager.WeatherFromLatLon(current_latitude, current_longitude);
	}
 
	// search cities around me
	private ArrayList<City> callSolAroundMe(	String current_latitude, String current_longitude, String temp_min, 
			String dist, String temp_unit, String temp_code,
			String num_results, String pop){


		YQL yql_manager = new YQL();
		return yql_manager.BestCitiesWeather(this.context,SOLCITIES_URL, current_latitude, current_longitude, temp_min, dist, temp_code, num_results, pop);
	}
	
	// search cities around me
	private ArrayList<City> callTop25(String month, String lang){

		YQL yql_manager = new YQL();
		return yql_manager.BestWorldWeather(TOP_25_URL,month,lang);
	}
}