package com.sun.tracker.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DecimalFormat;

import android.view.View;

import com.google.android.maps.GeoPoint;
import com.sun.tracker.R;

public class SolUtils{

	public static int CelciusToFahrenheit(int temp){

		double fahr = temp * 9/5 + 32;
		return (int) fahr;
	}

	public static int FahrenheitToCelcius(int temp){

		double celcius = (temp - 32) * 5/9;
		return (int) celcius;
	}

	public static int KmToMiles(int distance){

		double dist = distance * 0.621;
		return (int) dist;
	}

	public static GeoPoint makeGeoPoint(double latitude, double longitude){

		Double dLatitude = new Double(latitude * 1E6);
		Double dLongitude = new Double(longitude * 1E6);

		return new GeoPoint(dLatitude.intValue(), dLongitude.intValue());
	}

	public static int loadTextFromYahooCode(int code){

		switch(code){
		case 0:
			return R.string.yahoo_tornado;
		case 1:
			return R.string.yahoo_tropical_storm;
		case 2:
			return R.string.yahoo_hurricane;
		case 3:
			return R.string.yahoo_severe_thunderstorms;
		case 4:
			return R.string.yahoo_thunderstorms;
		case 5:
			return R.string.yahoo_mixed_rain_snow;
		case 6:
			return R.string.yahoo_mixed_rain_sleet;
		case 7:
			return R.string.yahoo_mixed_snow_sleet;
		case 8:
			return R.string.yahoo_freezing_drizzle;
		case 9:
			return R.string.yahoo_drizzle;
		case 10:
			return R.string.yahoo_freezing_rain;
		case 11:
			return R.string.yahoo_showers;
		case 12:
			return R.string.yahoo_showers;
		case 13:
			return R.string.yahoo_snow_flurries;
		case 14:
			return R.string.yahoo_light_snow_showers;
		case 15:
			return R.string.yahoo_blowing_snow;
		case 16:
			return R.string.yahoo_snow;
		case 17:
			return R.string.yahoo_hail;
		case 18:
			return R.string.yahoo_sleet;
		case 19:
			return R.string.yahoo_dust;
		case 20:
			return R.string.yahoo_foggy;
		case 21:
			return R.string.yahoo_haze;
		case 22:
			return R.string.yahoo_smoky;
		case 23:
			return R.string.yahoo_blustery;
		case 24:
			return R.string.yahoo_windy;
		case 25:
			return R.string.yahoo_cold;
		case 26:
			return R.string.yahoo_cloudy;
		case 27:
			return R.string.yahoo_mostly_cloudy;
		case 28:
			return R.string.yahoo_mostly_cloudy;
		case 29:
			return R.string.yahoo_partly_cloudy;
		case 30:
			return R.string.yahoo_partly_cloudy;
		case 31:
			return R.string.yahoo_clear;
		case 32:
			return R.string.yahoo_sunny;
		case 33:
			return R.string.yahoo_fair;
		case 34:
			return R.string.yahoo_fair;
		case 35:
			return R.string.yahoo_mixed_rain_hail;
		case 36:
			return R.string.yahoo_hot;
		case 37:
			return R.string.yahoo_isolated_thunderstorms;
		case 38:
			return R.string.yahoo_scattered_thunderstorms;
		case 39:
			return R.string.yahoo_scattered_thunderstorms;
		case 40:
			return R.string.yahoo_scattered_showers;
		case 41:
			return R.string.yahoo_heavy_snow;
		case 42:
			return R.string.yahoo_scattered_snow_showers;
		case 43:
			return R.string.yahoo_heavy_snow;
		case 44:
			return R.string.yahoo_partly_cloudy;
		case 45:
			return R.string.yahoo_thundershowers;
		case 46:
			return R.string.yahoo_snow_showers;
		case 47:
			return R.string.yahoo_isolated_thundershowers;
		}
		
		return R.string.yahoo_not_available;
	}

	public static int getTop(View myView){ 
		if(myView.getParent()==myView.getRootView())
			return myView.getTop();
		else
			return myView.getTop() + getTop((View)myView.getParent());
	}

	public static int getRelativeTop(View myView){
		return getTop(myView) - myView.getTop();
	}

	public static String formatToastTemp(String message,int value, String unit){
		if(unit.equals("c"))
			message+=": " + value + "°C";
		else
			message+=":" + CelciusToFahrenheit(value) + "°F";

		return message;
	}

	public static String formatToastDist(String message,int value, String unit){
		if(unit.equals("km"))
			message+=": " + value + " km";
		else
			message+=": " + KmToMiles(value) + " miles";

		return message;
	}

	public static String convertStreamToString(InputStream is) throws IOException {
		/*
		 * To convert the InputStream to String we use the
		 * Reader.read(char[] buffer) method. We iterate until the
		 * Reader return -1 which means there's no more data to
		 * read. We use the StringWriter class to produce the string.
		 */
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(
						new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {        
			return "";
		}
	}
	
	public static double roundTwoDecimals(double d) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(d));
	}
	
	public static double round(double what, int howmuch) {
		return (double)( (int)(what * Math.pow(10,howmuch) + .5) ) / Math.pow(10,howmuch);
	}
	
}