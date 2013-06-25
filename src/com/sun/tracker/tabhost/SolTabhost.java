package com.sun.tracker.tabhost;

import com.sun.tracker.R;
import com.sun.tracker.SolCities;
import com.sun.tracker.SolInvictus;
import com.sun.tracker.SolMaps;
import com.sun.tracker.SolPreferenceActivity;
import com.sun.tracker.SolTop25;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

/*
 * This activity demonstrates the use of ScrollableTabActivity by extending the class
 * 
 * Required files:
 * ScrollableTabActivity.java
 * RadioStateDrawable.java
 * TabBarButton.java
 * res/drawable/bottom_bar_highlight.9.png
 * res/drawable/bottom_bar.9.png
 * res/drawable/scrollbar_horizontal_thumb.xml
 * res/drawable/scrollbar_horizontal_track.xml
 * res/layout/customslidingtabhost.xml
 * res/layout/scrollgroupradiobuttonview.xml
 */
public class SolTabhost  extends ScrollableTabActivity{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * set this activity as the tab bar delegate
		 * so that onTabChanged is called when users tap on the bar
		 */
		setDelegate(new SliderBarActivityDelegateImpl());

		Intent searchIntent = new Intent(this, SolInvictus.class);

		/*
		 * This adds a title and an image to the tab bar button
		 * Image should be a PNG file with transparent background.
		 * Shades are opaque areas in on and off state are specific as parameters
		 */
		this.addTab(getString(R.string.tab_search), R.drawable.sol_tab_search, RadioStateDrawable.SHADE_GRAY, RadioStateDrawable.SHADE_SELECTED,searchIntent);

		Intent resultsIntent = new Intent(this, SolCities.class);
		this.addTab(getString(R.string.tab_results), R.drawable.sol_tab_results, RadioStateDrawable.SHADE_GRAY, RadioStateDrawable.SHADE_SELECTED,resultsIntent);

		Intent topIntent = new Intent(this, SolTop25.class);
		this.addTab(getString(R.string.tab_top), R.drawable.sol_tab_top, RadioStateDrawable.SHADE_GRAY, RadioStateDrawable.SHADE_SELECTED,topIntent);

		Intent mapIntent = new Intent(this, SolMaps.class);
		this.addTab(getString(R.string.tab_map), R.drawable.sol_tab_map, RadioStateDrawable.SHADE_GRAY, RadioStateDrawable.SHADE_SELECTED,mapIntent);

		/*
		 * commit is required to redraw the bar after add tabs are added
		 * if you know of a better way, drop me your suggestion please.
		 */
		commit();
	}

	private class SliderBarActivityDelegateImpl extends SliderBarActivityDelegate
	{
		/*
		 * Optional callback method
		 * called when users tap on the tab bar button
		 */
		protected void onTabChanged(int tabIndex) 
		{
			
		}
	}

	/*
	 * 		MENU
	 */
	public boolean onPrepareOptionsMenu(Menu menu) {
		//super.onPrepareOptionsMenu(menu);
		return getLocalActivityManager().getCurrentActivity()
		.onPrepareOptionsMenu(menu);
	}



	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		// TODO Auto-generated method stub
		return getLocalActivityManager().getCurrentActivity()
		.onMenuOpened(featureId, menu);
	}



	public boolean onCreateOptionsMenu(Menu menu) {
		//super.onCreateOptionsMenu(menu);
		return getLocalActivityManager().getCurrentActivity()
		.onCreateOptionsMenu(menu);
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		//super.onMenuItemSelected(featureId, item);
		return getLocalActivityManager().getCurrentActivity()
		.onMenuItemSelected(featureId, item);
	}
}