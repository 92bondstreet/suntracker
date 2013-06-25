package com.sun.tracker;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;

import com.sun.tracker.parser.City;
import com.sun.tracker.parser.ContainerData;
import com.sun.tracker.tabhost.ScrollableTabActivity;

public class TopCitiesService extends Service implements AsyncTaskCompleteListener<ArrayList<City>> {
	private static final String TAG = "TOP_CITIES";

	// INTERNET - NETWORK - GPS
	private BroadcastReceiver internetBroadcastReceiver;
	private HttpPostTask httpPostTask;	//
	private boolean NETWORK_STATUS_ON= false;

	// RESPONSE : SolCities
	private ArrayList<City> top25;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
	}

	@Override
	public void onDestroy() {
	}

	@Override
	public void onStart(Intent intent, int startid) {

		try{

			// step 1 : Check Internet Connection        
			//installInternetListener();

			// step 2 : search top 25
			callTop25();
		}
		catch(Exception e){
			sendToTabActivity(null);
			return;
		}

		// step 2 : call server
	}

	/*
	 * 			INTERNET - NETWORK - GPS
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
			registerReceiver(internetBroadcastReceiver, intentFilter);
		}
	}

	private void callTop25(){

		// get current month system

		String current_month = String.valueOf((Calendar.getInstance(Locale.getDefault())).get(Calendar.MONTH) + 1);
		
		String request_lang="en";
		if((Locale.getDefault().getDisplayLanguage()).equals(Locale.FRENCH.getDisplayLanguage()))
			request_lang = "fr";
		
		httpPostTask = new HttpPostTask(getBaseContext(), this);
		httpPostTask.execute(current_month,request_lang);
	}

	public void onTaskComplete(ArrayList<City> result) {
		// TODO Auto-generated method stub

		if(result!=null)
			loadTop25(result);
		else{
			sendToTabActivity(null);
		}

	}

	/*
	 * 			TOP 25 loading
	 */

	private void loadTop25(ArrayList<City> myresult){

		try{
			top25 = myresult;

			// load ListView in new activity
			Bundle extras = new Bundle();
			extras.putParcelableArrayList("top25", top25);
			
			sendToTabActivity(extras);

			return;
		} catch (Throwable t){
			sendToTabActivity(null);
			return;
		}
	}
	
	private void sendToTabActivity(Bundle extras){
		
		Intent intent = new Intent(ScrollableTabActivity.ACTION_UPDATE_TOP25);
		if(extras!=null)
			intent.putExtras(extras);
		sendBroadcast(intent);
	}
}