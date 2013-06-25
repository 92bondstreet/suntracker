package com.sun.tracker;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.sun.tracker.parser.City;
import com.sun.tracker.tabhost.ScrollableTabActivity;
import com.sun.tracker.tabhost.SolTabhost;
import com.sun.tracker.utils.SolUtils;

public class SolMaps extends MapActivity 
{   
	private MapView mapView;
	private MapController mapController;
	private SolCitiesOverlay solCitiesOverlay;

	private ArrayList<City> currentSolcities;
	private String current_latitude;
	private String current_longitude;
	private GeoPoint currentPosition;

	private String action = ScrollableTabActivity.ACTION_UPDATE_MAP;


	private Bundle extras;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		try{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.solmaps_layout);

			Drawable marker = getResources().getDrawable(R.drawable.sol_local_code_1);
			marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicWidth());
			solCitiesOverlay = new SolCitiesOverlay(this,marker);

			initZoom();

			updateGoogleMaps(this.getIntent());
		}
		catch(Exception e)
		{
			return;
		}
	} 

	/*
	 * 
	 *			MENU
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.solmaps_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle all of the possible menu actions.
		switch (item.getItemId()) {
		case R.id.PositionItem:
			centerCurrentLocation();
			break;
		case R.id.QuitItem:
			this.finish();
			break;
		}
		//return super.onOptionsItemSelected(item);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		if(menu!=null)
			menu.clear();
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.solmaps_menu, menu);
		return true;
	}

	/*
	 * 
	 *			EVENT
	 */

	private void initZoom(){
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
	}

	private void updateGoogleMaps(Intent intent){
		
		// get ArrayList from intent
		extras = intent.getExtras();
		if(extras==null)
			return;

		action = intent.getAction();

		initCurrentLocation();

		if(	action.equals(ScrollableTabActivity.ACTION_UPDATE_CENTER_MAP) ||
				action.equals(ScrollableTabActivity.ACTION_UPDATE_MAP) )
			initSolCities();
	}

	private void initCurrentLocation(){

		mapController = mapView.getController();

		// 0. current position
		current_latitude = extras.getString("current_latitude");
		current_longitude = extras.getString("current_longitude");

		double lat = Double.parseDouble(current_latitude);
		double lon = Double.parseDouble(current_longitude);
		currentPosition = SolUtils.makeGeoPoint(lat, lon);

		// according action 
		if(action.equals(ScrollableTabActivity.ACTION_UPDATE_MAP) || action.equals(ScrollableTabActivity.ACTION_VIEW_MAP)) 
			centerCurrentLocation();	
		else if(action.equals(ScrollableTabActivity.ACTION_UPDATE_CENTER_MAP) || action.equals(ScrollableTabActivity.ACTION_CENTER_MAP)){
			centerLambdaLocation(extras.getString("center_on_latitude"), extras.getString("center_on_longitude"));
		}    	

		//mapView.invalidate();
	} 

	private void centerCurrentLocation(){

		if(currentPosition!=null){
			mapController.animateTo(currentPosition);
			mapController.setZoom(14);
		}

		mapView.invalidate();
	}

	private void centerLambdaLocation(String latitude, String longitude){

		double lat = Double.parseDouble(latitude);
		double lon = Double.parseDouble(longitude);

		GeoPoint lambdaPosition = SolUtils.makeGeoPoint(lat, lon);

		if(lambdaPosition!=null){
			mapController.animateTo(lambdaPosition);
			mapController.setZoom(14);
		}

		mapView.invalidate();
	}

	private void initSolCities(){

		// clear layers 
		mapView.getOverlays().clear();
		solCitiesOverlay.clearItems();
		
		// add current location
		solCitiesOverlay.addItem(currentPosition);
		mapView.getOverlays().add(solCitiesOverlay);
		
		currentSolcities = extras.getParcelableArrayList("solcities");
		if(currentSolcities==null)
			return;
		
		// add solcities
		solCitiesOverlay.addItems(currentSolcities);
		mapView.getOverlays().add(solCitiesOverlay);    	
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);

		if(	intent.getAction().equals(SolTabhost.ACTION_UPDATE_MAP) || 
			intent.getAction().equals(SolTabhost.ACTION_UPDATE_CENTER_MAP) ||
			intent.getAction().equals(SolTabhost.ACTION_CENTER_MAP))
			updateGoogleMaps(intent);

	}




}