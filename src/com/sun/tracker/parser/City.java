package com.sun.tracker.parser;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;


public class City implements Comparable<City>,Parcelable{

	public String name;
	public String country;
	public String continent;
	public String latitude;
	public String longitude;
	public int temp;
	public int code;
	public int yahoo_code;
	public double distance;
	public int pop;
	
	
	/**
	 * Standard basic constructor for non-parcel
	 * object creation
	 */
	public City(){
		name = "";
		country = "";
		continent = "";
		latitude = "";
		longitude = "";
		code = -1;
		yahoo_code = -1;
		distance = -1;
		pop=-1;
	}
	
	/**
	 *
	 * Constructor to use when re-constructing object
	 * from a parcel
	 *
	 * @param in a parcel from which to read this object
	 */
	public City(Parcel in) {
		readFromParcel(in);
	}
	
	public City copy(){
		City copy = new City();
		copy.name = this.name;
		copy.country = this.country;
		copy.continent = this.continent;
		copy.latitude = this.latitude;
		copy.longitude = this.longitude;
		copy.temp = this.temp;
		copy.code = this.code;
		copy.yahoo_code = this.yahoo_code;
		copy.distance = this.distance;
		copy.pop = this.pop;
			
		return copy;
	}
	
	public void reset(){
		name = "";
		country = "";
		continent = "";
		latitude = "";
		longitude = "";
		code = -1;
		yahoo_code = -1;
		distance = -1;
		pop = -1;
	}
	
	//@Override
	public int compareTo(City arg0) {
		
		final int BEFORE = -1;
		final int EQUAL = 0;
		    final int AFTER = 1;
		    
		double currentDistance = this.distance;
		double argDistance = arg0.distance;
		
		if(currentDistance<argDistance)
			return BEFORE;
		else if(currentDistance>argDistance)
			return AFTER;
			
		return EQUAL;
	}

	//@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	//@Override
	public void writeToParcel(Parcel out, int arg1) {
		// TODO Auto-generated method stub
		out.writeString(name);
		out.writeString(country);
		out.writeString(continent);
		out.writeString(latitude);
		out.writeString(longitude);
		out.writeInt(temp);
		out.writeInt(code);
		out.writeInt(yahoo_code);
		out.writeDouble(distance);
		out.writeInt(pop);
	}
	
	/**
	 *
	 * Called from the constructor to create this
	 * object from a parcel.
	 *
	 * @param in parcel from which to re-create object
	 */
	private void readFromParcel(Parcel in) {

		// We just need to read back each
		// field in the order that it was
		// written to the parcel
		name = in.readString();
		country = in.readString();
		continent = in.readString();
		latitude = in.readString();
		longitude = in.readString();
		temp = in.readInt();
		code = in.readInt();
		yahoo_code = in.readInt();
		distance = in.readDouble();
		pop = in.readInt();
	}
	
	/**
    *
    * This field is needed for Android to be able to
    * create new objects, individually or as arrays.
    *
    * This also means that you can use use the default
    * constructor to create the object and use another
    * method to hyrdate it as necessary.
    *
    * I just find it easier to use the constructor.
    * It makes sense for the way my brain thinks ;-)
    *
    */
   public static final Parcelable.Creator<City> CREATOR =
   	new Parcelable.Creator<City>() {
           public City createFromParcel(Parcel in) {
               return new City(in);
           }

           public City[] newArray(int size) {
               return new City[size];
           }
       };
}