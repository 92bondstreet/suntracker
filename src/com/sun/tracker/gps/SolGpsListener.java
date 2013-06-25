package com.sun.tracker.gps;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Toast;

public class SolGpsListener {

	private Timer timer1;
	private LocationManager lm;
	private LocationResult locationResult;
	private boolean gps_enabled=false;
	private boolean network_enabled=false;
	private int DELAY = 5000;

	private Handler mHandler = new Handler();


	public boolean getLocation(Context context, LocationResult result)
	{
		try{
			//I use LocationResult callback class to pass location value from MyLocation to user code.
			locationResult=result;
			if(lm==null)
				lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			
			//exceptions will be thrown if provider is not permitted.
			try{gps_enabled=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
			try{network_enabled=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}

			//don't start listeners if no provider is enabled
			if(!gps_enabled && !network_enabled)
				return false;

			if(gps_enabled)
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
			if(network_enabled)
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);

			mHandler.postDelayed(mUpdateTimeTask, DELAY);

			return true;
		}
		catch(Exception e){
			return false;
		}
	}

	LocationListener locationListenerGps = new LocationListener() {
		public void onLocationChanged(Location location) {
			mHandler.removeCallbacks(mUpdateTimeTask);
			lm.removeUpdates(this);
			lm.removeUpdates(locationListenerNetwork);
			
			locationResult.gotLocation(location);
		}
		public void onProviderDisabled(String provider) {}
		public void onProviderEnabled(String provider) {}
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	};

	LocationListener locationListenerNetwork = new LocationListener() {
		public void onLocationChanged(Location location) {
			mHandler.removeCallbacks(mUpdateTimeTask);
			lm.removeUpdates(this);
			lm.removeUpdates(locationListenerGps);
		
			locationResult.gotLocation(location);			
		}
		public void onProviderDisabled(String provider) {}
		public void onProviderEnabled(String provider) {}
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	};
	

	public static abstract class LocationResult{
		public abstract void gotLocation(Location location);
	}


	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {

			mHandler.removeCallbacks(mUpdateTimeTask);

			lm.removeUpdates(locationListenerGps);
			lm.removeUpdates(locationListenerNetwork);
			

			Location net_loc=null, gps_loc=null, passive_loc=null;
			if(gps_enabled)
				gps_loc=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(network_enabled)
				net_loc=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			
			//if there are both values use the latest one
			if(gps_loc!=null && net_loc!=null){
				if(gps_loc.getTime()>net_loc.getTime())
					locationResult.gotLocation(gps_loc);
				else
					locationResult.gotLocation(net_loc);
				return;
			}
		

			if(gps_loc!=null){
				locationResult.gotLocation(gps_loc);
				return;
			}
			if(net_loc!=null){
				locationResult.gotLocation(net_loc);
				return;
			}
			if(passive_loc!=null){
				locationResult.gotLocation(passive_loc);
				return;
			}
			
			locationResult.gotLocation(null);
		}

	};
}


