package com.sun.tracker.gps;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.tracker.gps.SolGpsListener.LocationResult;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationManagerListener implements LocationListener{


	private LocationManager LOCATION_MANAGER;
	private LocationResult LOCATION_RESULT;
	private Timer TIMER;
	private int DELAY = 50000;
	private List <String> CURRENT_PROVIDERS;

	public boolean getLocation(Context context, LocationResult result){
		//On récupère le service de localisation
		LOCATION_MANAGER = (LocationManager)context.getSystemService(context.LOCATION_SERVICE);
		LOCATION_RESULT = result;

		//On demande au service la liste des sources disponibles.
		CURRENT_PROVIDERS = LOCATION_MANAGER.getProviders(true);

		if(CURRENT_PROVIDERS.size()==0)
			return false;

		int i =0;
		//on stock le nom de ces source dans un tableau de string
		for(String provider : CURRENT_PROVIDERS){
			//On demande au service de localisation de nous notifier tout changement de position
			//sur la source (le provider) choisie, toute les minutes (60000millisecondes).
			//Le paramètre this spécifie que notre classe implémente LocationListener et recevra
			//les notifications.
			LOCATION_MANAGER.requestLocationUpdates(provider, 0, 0, this);
		}

		TIMER = new Timer();
		TIMER.schedule(new GetLastLocation(), DELAY);

		return true;
	}

	//@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		TIMER.cancel();
		LOCATION_MANAGER.removeUpdates(this);
		LOCATION_RESULT.gotLocation(arg0);
	}

	//@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		TIMER.cancel();
		LOCATION_MANAGER.removeUpdates(this);
		LOCATION_RESULT.gotLocation(null);
	}

	//@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	//@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	class GetLastLocation extends TimerTask {
		@Override
		public void run() {

			LOCATION_MANAGER.removeUpdates(LocationManagerListener.this);
			Location last_loc = null;
			
			// try to get last position
			for(String provider : CURRENT_PROVIDERS){
				last_loc = LOCATION_MANAGER.getLastKnownLocation(provider);
				if(last_loc!=null)
					LOCATION_RESULT.gotLocation(last_loc);
					
			}	
			TIMER.cancel();
			// return null in case of nothing
			LOCATION_RESULT.gotLocation(null);
		}
	}


	public static abstract class LocationResult{
		public abstract void gotLocation(Location location);
	}
}