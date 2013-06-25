/**
 * Copyright (c) 2009 Muh Hon Cheng
 * 
 * Permission is hereby granted, free of charge, to any person obtaining 
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to 
 * permit persons to whom the Software is furnished to do so, subject 
 * to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be 
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT 
 * WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT 
 * SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR 
 * IN CONNECTION WITH THE SOFTWARE OR 
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * @author 		Muh Hon Cheng <honcheng@gmail.com>
 * @copyright	2009	Muh Hon Cheng
 * @version
 * 
 */

package com.sun.tracker.tabhost;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.sun.tracker.R;
import com.sun.tracker.SolCities;
import com.sun.tracker.SolMaps;
import com.sun.tracker.SolTop25;
import com.sun.tracker.TopCitiesService;

public class ScrollableTabActivity extends ActivityGroup  implements RadioGroup.OnCheckedChangeListener{
	
	private LocalActivityManager activityManager;
	private LinearLayout contentViewLayout;
	private LinearLayout.LayoutParams contentViewLayoutParams;
	private HorizontalScrollView bottomBar;
	private RadioGroup.LayoutParams buttonLayoutParams;
	private RadioGroup bottomRadioGroup;
	private Context context;
	private List intentList;
	private List titleList;
	private List states;
	private SliderBarActivityDelegate delegate;
	private int defaultOffShade;
	private int defaultOnShade;
	
	// Change TAB Filter
	private IntentFilter changeTabIntentFilter;
	private ChangeTabBroadcastReceiver changeTabBroadcastReceiver;
	public static String CURRENT_TAB_INDEX;
	public static String ACTION_CHANGE_TAB = "com.mobyfactory.changeTab";
	
	public static int SOL_CITIES_TAB = 1;
	public static int SOL_TOP25_TAB = 2;
	public static int SOL_MAPS_TAB = 3;
	private Bundle SolCities_extras = null;
	private Bundle SolMaps_extras = null;
	private Bundle SolTop25_extras = null;
	
	public static String ACTION_VIEW_RESULTS = "com.solinvictus.viewResults";
	public static String ACTION_UPDATE_RESULTS = "com.solinvictus.updateResults";
	
	private String current_results_action = ACTION_VIEW_RESULTS;
	
	
	// Init MAP Filter
	private IntentFilter updateMapIntentFilter;
	private UpdateMapBroadcastReceiver updateMapBroadcastReceiver;
	
	public static String ACTION_VIEW_MAP = "com.solinvictus.viewMap";
	public static String ACTION_UPDATE_MAP = "com.solinvictus.updateMaps";
	public static String ACTION_UPDATE_CENTER_MAP = "com.solinvictus.updatecenterMaps";
	public static String ACTION_CENTER_MAP = "com.solinvictus.centerMaps";
	
	private String current_map_action = ACTION_VIEW_RESULTS;
	

	// Init Top25 Filter
	private Intent top25Service; 
	private IntentFilter top25IntentFilter;
	private Top25BroadcastReceiver top25BroadcastReceiver;
	
	public static String ACTION_RUNNING_TOP25 = "com.solinvictus.runningTop25";
	public static String ACTION_UPDATE_TOP25 = "com.solinvictus.updateTop25";
	public static String ACTION_VIEW_TOP25 = "com.solinvictus.viewTop25";
	
	private String current_top25_action = ACTION_RUNNING_TOP25;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        
        fullScreenLauncher();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        activityManager = getLocalActivityManager();
        setContentView(R.layout.customslidingtabhost);
        contentViewLayout = (LinearLayout)findViewById(R.id.contentViewLayout);
        bottomBar = (HorizontalScrollView)findViewById(R.id.bottomBar);
        bottomRadioGroup = (RadioGroup)findViewById(R.id.bottomMenu);
        contentViewLayoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT); 
        
        defaultOffShade = RadioStateDrawable.SHADE_GRAY;
        defaultOnShade = RadioStateDrawable.SHADE_YELLOW;
         
        bottomRadioGroup.setOnCheckedChangeListener(this);
        intentList  = new ArrayList();
        titleList	= new ArrayList();
        states 		= new ArrayList();
        
        buttonLayoutParams = new RadioGroup.LayoutParams(320/5, RadioGroup.LayoutParams.FILL_PARENT);     
        
       /* changeTabIntentFilter = new IntentFilter(ACTION_CHANGE_TAB);
    	changeTabBroadcastReceiver = new ChangeTabBroadcastReceiver();
    	registerReceiver(changeTabBroadcastReceiver, changeTabIntentFilter);
    	
    	updateMapIntentFilter = new IntentFilter(ACTION_UPDATE_MAP);
    	updateMapBroadcastReceiver = new UpdateMapBroadcastReceiver();
    	registerReceiver(updateMapBroadcastReceiver, updateMapIntentFilter);
    	
    	top25IntentFilter = new IntentFilter(ACTION_UPDATE_TOP25);
    	top25BroadcastReceiver = new Top25BroadcastReceiver();
    	registerReceiver(top25BroadcastReceiver, top25IntentFilter);*/
    }
    
    private void fullScreenLauncher(){
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		// Pour cacher la barre de statut et donc mettre votre application en plein écran
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
    
    public void onResume()
    {   	
    	changeTabIntentFilter = new IntentFilter(ACTION_CHANGE_TAB);
    	changeTabBroadcastReceiver = new ChangeTabBroadcastReceiver();
    	registerReceiver(changeTabBroadcastReceiver, changeTabIntentFilter);
    	
    	updateMapIntentFilter = new IntentFilter(ACTION_UPDATE_MAP);
    	updateMapBroadcastReceiver = new UpdateMapBroadcastReceiver();
    	registerReceiver(updateMapBroadcastReceiver, updateMapIntentFilter);
    	
    	top25IntentFilter = new IntentFilter(ACTION_UPDATE_TOP25);
    	top25BroadcastReceiver = new Top25BroadcastReceiver();
    	registerReceiver(top25BroadcastReceiver, top25IntentFilter);
    	super.onResume();
    }
    
    public void onPause()
    {
    	unregisterReceiver(changeTabBroadcastReceiver); 
    	unregisterReceiver(updateMapBroadcastReceiver);
    	unregisterReceiver(top25BroadcastReceiver);
    	super.onPause();
    }

    public void commit()
    {
    	bottomRadioGroup.removeAllViews();
    	
    	int optimum_visible_items_in_portrait_mode = 5;
    	try
    	{
    		WindowManager window = getWindowManager();
        	Display display = window.getDefaultDisplay();
        	int window_width = display.getWidth();
        	
        	optimum_visible_items_in_portrait_mode = (int) (window_width/64.0);
    	}
    	catch (Exception e)
    	{
    		optimum_visible_items_in_portrait_mode = 5;
    	}
    	
    	int screen_width = getWindowManager().getDefaultDisplay().getWidth();
    	int width;
    	if (intentList.size()<=optimum_visible_items_in_portrait_mode)
    	{
    		width = screen_width/intentList.size();
    	}
    	else
    	{
    		//width = screen_width/5;
    		width = 320/5; 
    	}
    	RadioStateDrawable.width = width;
		RadioStateDrawable.screen_width = screen_width;
		buttonLayoutParams = new RadioGroup.LayoutParams(width, RadioGroup.LayoutParams.FILL_PARENT);
    	
    	for (int i=0; i<intentList.size(); i++)
    	{
    		TabBarButton tabButton = new TabBarButton(this);
    		int[] iconStates = (int[]) states.get(i);
    		if (iconStates.length==1) tabButton.setState( titleList.get(i).toString(),iconStates[0]);
    		else if (iconStates.length==2) tabButton.setState(titleList.get(i).toString(), iconStates[0], iconStates[1]);
    		else if (iconStates.length==3) tabButton.setState(titleList.get(i).toString(), iconStates[0], iconStates[1], iconStates[2]);
        	tabButton.setId(i);
        	tabButton.setGravity(Gravity.CENTER);
        	bottomRadioGroup.addView(tabButton, i, buttonLayoutParams);
    	}
    	
    	setCurrentTab(0,null);
    	
    	// start top25 search service
    	 top25Service = new Intent(this, TopCitiesService.class);
        // startService(top25Service);
    	
    }
    
    /**
     * <b><i>protected void addTab(String title, int offIconStateId, int onIconStateId, Intent intent)</i></b> <p><p>
     * Add a tab to the navigation bar by specifying the title, 1 image for button on-state, and 1 image for button off-state<p>
     * @param title				a String that specifies that title of the tab button
     * @param onIconStateId		id of the on-state image
     * @param offIconStateId	id of the off-state image
     * @param intent			intent to start when button is tapped
     */
    protected void addTab(String title, int offIconStateId, int onIconStateId, Intent intent)
    {
    	int[] iconStates = {onIconStateId, offIconStateId};
    	states.add(iconStates);
    	intentList.add(intent);
    	titleList.add(title);
    	//commit();
    }
    
    /**
     * <b><i>protected void addTab(String title, int iconStateId, Intent intent)</i></b> <p><p>
     * Add a tab to the navigation bar by specifying the title, and 1 image for the button. Default yellow/gray shade is used for button on/off state<p>
     * @param title				a String that specifies that title of the tab button
     * @param iconStateId		id of the image used for both on/off state
     * @param intent			intent to start when button is tapped
     */
    protected void addTab(String title, int iconStateId, Intent intent)
    {
    	//int[] iconStates = {iconStateId};
    	int[] iconStates = {iconStateId, defaultOffShade, defaultOnShade};
    	states.add(iconStates);
    	intentList.add(intent);
    	titleList.add(title);
    	//commit();
    }
    
    /**
     * <b><i>protected void addTab(String title, int iconStateId, int offShade, int onShade, Intent intent)</i></b> <p><p>
     * Add a tab to the navigation bar by specifying the title, and 1 image for the button. Default yellow/gray shade is used for button on/off state<p>
     * @param title				a String that specifies that title of the tab button
     * @param iconStateId		id of the image used for both on/off state
     * @param offShade  		id for off-state color shades (e.g. RadioStateDrawable.SHADE_GRAY, RadioStateDrawable.SHADE_GREEN etc)
     * @param onShade			id for on-state color shades (e.g. RadioStateDrawable.SHADE_GRAY, RadioStateDrawable.SHADE_GREEN etc)
     * @param intent			intent to start when button is tapped
     */
    protected void addTab(String title, int iconStateId, int offShade, int onShade, Intent intent)
    {
    	int[] iconStates = {iconStateId, offShade, onShade};
    	states.add(iconStates);
    	intentList.add(intent);
    	titleList.add(title);
    	//commit();
    }

    /**
     * <b><i>protected void setDefaultShde(int offShade, int onShade)</i></b><p><p>
     * Sets the default on and off color shades of the bottom bar buttons<p>
     * If not specified, the default off shade is gray, and the default on shade is yellow
     * @param offShade			id for off-state color shades (e.g. RadioStateDrawable.SHADE_GRAY, RadioStateDrawable.SHADE_GREEN etc)
     * @param onShade			id for on-state color shades (e.g. RadioStateDrawable.SHADE_GRAY, RadioStateDrawable.SHADE_GREEN etc)
     */
    protected void setDefaultShade(int offShade, int onShade)
    {
    	defaultOffShade = offShade;
    	defaultOnShade = onShade;
    }
    
    public void onCheckedChanged(RadioGroup group, int checkedId) {
    	try
    	{
    		if (delegate!=null) delegate.onTabChanged(checkedId);
    	}
    	catch (Exception e){}
    	
    	if(checkedId==SOL_CITIES_TAB){
    		Intent launchSolCitiesList = new Intent(this,SolCities.class);
    		if(SolCities_extras!=null)
    			launchSolCitiesList.putExtras(SolCities_extras);
			launchSolCitiesList.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			launchSolCitiesList.setAction(current_results_action);
			startGroupActivity( titleList.get(checkedId).toString(), launchSolCitiesList);
			current_results_action = ACTION_VIEW_RESULTS;
    	}
    	else if(checkedId==SOL_MAPS_TAB){
    		Intent launchSolMap = new Intent(this,SolMaps.class);
    		if(SolMaps_extras!=null)
    			launchSolMap.putExtras(SolMaps_extras);
    		launchSolMap.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    		launchSolMap.setAction(current_map_action);
    		
    		// 3 cases : 
			// send all results + center on 1 particular result
			// center on 1 particular result
    		// just center my position
    		
			startGroupActivity( titleList.get(checkedId).toString(), launchSolMap);
			current_map_action = ACTION_VIEW_MAP;
    	}
    	else if(checkedId==SOL_TOP25_TAB){
    		Intent launchSolTop25 = new Intent(this,SolTop25.class);
    		if(SolTop25_extras!=null)
    			launchSolTop25.putExtras(SolTop25_extras);
    		launchSolTop25.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    		launchSolTop25.setAction(current_top25_action);
    		
    		// 2 cases : 
			// update 
    		// view
    		
			startGroupActivity( titleList.get(checkedId).toString(), launchSolTop25);
			if(!current_top25_action.equals(ACTION_RUNNING_TOP25))
				current_top25_action = ACTION_VIEW_TOP25;
    	}
    	else
    		startGroupActivity( titleList.get(checkedId).toString(), (Intent) intentList.get(checkedId));
    }
    
    public void startGroupActivity(String id, Intent intent)
    {
    	contentViewLayout.removeAllViews();
    	
    	View view = activityManager.startActivity(id, intent).getDecorView();
        contentViewLayout.addView(view, contentViewLayoutParams);
    }
    
    public void setCurrentTab(int index, Bundle extras)
    {
    	if(index==SOL_CITIES_TAB)
    		this.SolCities_extras = extras;
    	else if(index==SOL_MAPS_TAB){
    		if(this.SolMaps_extras!=null){
    			// add center lat/long
    			this.SolMaps_extras.putString("center_on_latitude", extras.getString("center_on_latitude"));
    			this.SolMaps_extras.putString("center_on_longitude", extras.getString("center_on_longitude"));
    		}
    		else
    			this.SolMaps_extras = extras;
    			
    	}
    	bottomRadioGroup.check(index);
    	/*if(index==SOL_CITIES_TAB && SolCities_extras!=null){
    		Intent launchSolCitiesList = new Intent(this,SolCities.class);
			launchSolCitiesList.putExtras(SolCities_extras);
			launchSolCitiesList.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	}*/
		//startGroupActivity(titleList.get(index).toString(), (Intent)intentList.get(index));
    }
    
    public int getCurrentTab()
    {
    	return bottomRadioGroup.getCheckedRadioButtonId();
    }
    
    /*
     * gets required R, not used
     */
    public boolean inflateXMLForContentView()
    {
    	/*
    	setContentView(R.layout.customslidingtabhost);
        contentViewLayout = (LinearLayout)findViewById(R.id.contentViewLayout);
        bottomBar = (HorizontalScrollView)findViewById(R.id.bottomBar);
        bottomRadioGroup = (RadioGroup)findViewById(R.id.bottomMenu);
    	*/
    	return false;
    }
    
    public int bottomBar()
    {
    	return -1;
    }
    
    /*
     * delegates
     */
    
    public void setDelegate(SliderBarActivityDelegate delegate_)
    {
    	delegate = delegate_;
    }
    
    public static abstract class SliderBarActivityDelegate {

        /*
         * Called when tab changed
         */
        protected void onTabChanged(int tabIndex) {}
    }
    
    /*
     * Broadcast receiver to set current tab
     */
    
    public class ChangeTabBroadcastReceiver extends BroadcastReceiver
    {
    	@Override
    	public void onReceive(Context context, Intent intent)
    	{
    		int index = intent.getExtras().getInt(CURRENT_TAB_INDEX);
    		
    		if(index==SOL_CITIES_TAB){
    			//Bundle extras = new Bundle();
    			//extras.putParcelableArrayList("solcities", intent.getExtras().getParcelableArrayList("solcities"));
    			//extras.putString("current_latitude", intent.getExtras().getString("current_latitude"));
    			//extras.putString("current_longitude", intent.getExtras().getString("current_longitude"));
    			current_results_action = ACTION_UPDATE_RESULTS;
    			setCurrentTab(index,intent.getExtras());
    		}
    		else if(index==SOL_MAPS_TAB){
    			// 2 cases : 
    			// send all results + center on 1 particular result
    			// center on 1 particular result
    			if(current_map_action==ACTION_UPDATE_MAP)
    				current_map_action=ACTION_UPDATE_CENTER_MAP;
    			else if(current_map_action==ACTION_VIEW_MAP)
    				current_map_action=ACTION_CENTER_MAP;
    			
    		
    			setCurrentTab(index,intent.getExtras());
    		}
    		else
    			setCurrentTab(index,null);
    	}
    }
    
    /*
     * Broadcast receiver to update map content
     */
    
    public class UpdateMapBroadcastReceiver extends BroadcastReceiver
    {
    	@Override
    	public void onReceive(Context context, Intent intent)
    	{
    		SolMaps_extras = intent.getExtras();
    		current_map_action = ACTION_UPDATE_MAP;
    	}
    }
    
    /*
     * Broadcast receiver to update map content
     */
    
    public class Top25BroadcastReceiver extends BroadcastReceiver
    {
    	@Override
    	public void onReceive(Context context, Intent intent)
    	{
    		// top 25 extras
    		SolTop25_extras = intent.getExtras();
    		stopService(top25Service);
    		current_top25_action = ACTION_UPDATE_TOP25;
    	}
    }
}
