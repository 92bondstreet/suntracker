package com.sun.tracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SolPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);		
	}
	
	 @Override
    protected void onResume() {
        super.onResume();

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }

	//@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(SolInvictus.ACTION_UPDATE_PREF);
		sendBroadcast(intent);
		
		Intent intentcities = new Intent(SolCities.ACTION_UPDATE_PREF);
		sendBroadcast(intentcities);
		
		Intent intenttopcities = new Intent(SolCities.ACTION_UPDATE_PREF);
		sendBroadcast(intenttopcities);
	}
}