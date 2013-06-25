package com.sun.tracker.db;

import java.util.ArrayList;

import com.sun.tracker.SolInvictus;
import com.sun.tracker.parser.City;
import com.sun.tracker.utils.SolUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class CitiesDB{

	private static final int VERSION_BDD = 1;
	private static final String NOM_BDD = "cities.db";

	private static final String CITIES = "cities";
	private static final String COL_ID = "id";
	private static final int NUM_COL_ID = 0;
	private static final String COL_NAME = "name";
	private static final int NUM_COL_NAME = 1;
	private static final String COL_COUNTRY = "country";
	private static final int NUM_COL_COUNTRY = 2;
	private static final String COL_LATITUDE = "latitude";
	private static final int NUM_COL_LATITUDE = 3;
	private static final String COL_LONGITUDE = "longitude";
	private static final int NUM_COL_LONGITUDE = 4;
	private static final String COL_TEMP = "temp";
	private static final int NUM_COL_TEMP = 5;
	private static final String COL_CODE = "code";
	private static final int NUM_COL_CODE = 6;
	private static final String COL_YAHOO_CODE = "yahoo_code";
	private static final int NUM_COL_YAHOO_CODE = 7;
	private static final String COL_DISTANCE = "distance";
	private static final int NUM_COL_DISTANCE = 8;
	private static final String COL_POP = "pop";
	private static final int NUM_COL_POP = 9;

	private SQLiteDatabase bdd;

	private CitiesSqlite maBaseSQLite;

	public CitiesDB(Context context){
		//On créer la BDD et sa table
		maBaseSQLite = new CitiesSqlite(context, NOM_BDD, null, VERSION_BDD);
	}

	public void open(){
		//on ouvre la BDD en écriture
		bdd = maBaseSQLite.getWritableDatabase();
	}

	public void close(){
		//on ferme l'accès à la BDD
		bdd.close();
	}

	public SQLiteDatabase getBDD(){
		return bdd;
	}

	public void cleanBDD(){
		try{
			bdd.delete(CITIES, null, null);
		}catch(Exception e)
		{
		}
	}


	public void insertCity(City current_city){
		try{
		//Création d'un ContentValues (fonctionne comme une HashMap)
		ContentValues values = new ContentValues();
		//on lui ajoute une valeur associé à une clé (qui est le nom de la colonne dans laquelle on veut mettre la valeur)
		values.put(COL_NAME, current_city.name);
		values.put(COL_COUNTRY, current_city.country);
		values.put(COL_LATITUDE, current_city.latitude);
		values.put(COL_LONGITUDE, current_city.longitude);
		values.put(COL_TEMP, current_city.temp);
		values.put(COL_CODE, current_city.code);
		values.put(COL_YAHOO_CODE, Integer.toString(current_city.yahoo_code));
		values.put(COL_DISTANCE, current_city.distance);
		values.put(COL_POP, current_city.pop);

		//on insère l'objet dans la BDD via le ContentValues
		 bdd.insert(CITIES, null, values);
		}
		catch(Exception e){
			return;
		}
	}


	/*
	 * 
	 * REQUEST
	 * 
	 */


	public int getCount(){
		Cursor c = bdd.query(CITIES, new String[]{COL_ID,COL_NAME, COL_COUNTRY, COL_LATITUDE,COL_LONGITUDE,
				COL_TEMP,COL_CODE,COL_YAHOO_CODE,COL_DISTANCE,COL_POP}, null, null, null, null, null);
		return c.getCount();
	}
	
	public int getTempMax(){

		try{
			Cursor c = bdd.query(CITIES, new String[]{COL_ID,COL_NAME, COL_COUNTRY, COL_LATITUDE,COL_LONGITUDE,
					COL_TEMP,COL_CODE,COL_YAHOO_CODE,COL_DISTANCE,COL_POP}, null, null, null, null, COL_TEMP + " DESC LIMIT 1");
			return cursorToTemp(c);
		}catch(Exception e){
			return 0;
		}
	}

	//Cette méthode permet de convertir un cursor en un livre
	private int cursorToTemp(Cursor c){
		//si aucun élément n'a été retourné dans la requête, on renvoie null
		if (c.getCount() == 0)
			return 0;


		//Sinon on se place sur le premier élément
		c.moveToFirst();		
		int temp_max = Integer.parseInt(c.getString(NUM_COL_TEMP));		
		//On ferme le cursor
		c.close();

		return temp_max;

	}

	public int getDistMax(){

		try{
			Cursor c = bdd.query(CITIES, new String[]{COL_ID,COL_NAME, COL_COUNTRY, COL_LATITUDE,COL_LONGITUDE,
					COL_TEMP,COL_CODE,COL_YAHOO_CODE,COL_DISTANCE,COL_POP}, null, null,null, null, COL_DISTANCE + " DESC LIMIT 1");
			return cursorToDist(c);
		}catch(Exception e){
			return 0;
		}
	}

	//Cette méthode permet de convertir un cursor en un livre
	private int cursorToDist(Cursor c){
		//si aucun élément n'a été retourné dans la requête, on renvoie null
		if (c.getCount() == 0)
			return 0;


		//Sinon on se place sur le premier élément
		c.moveToFirst();		
		int dist_max = (int) SolUtils.round( Double.parseDouble(c.getString(NUM_COL_DISTANCE)) ,0 );		
		//On ferme le cursor
		c.close();

		return dist_max;

	}


	public ArrayList<City> getCities(String distance, String temp, String code, String population, String results){

		try{
			// request 
			String distance_request = COL_DISTANCE + "<=" + distance;
			String temp_request = COL_TEMP + ">=" + temp;
			String pop_request = COL_POP + ">=" + population;

			String where_clause = distance_request + " AND " + temp_request + " AND " + pop_request;

			String code_request = "";
			if(code.equals(String.valueOf(SolInvictus.SUN_WEATHER))){
				code_request = COL_CODE + "=" + code;
				where_clause += " and " + code_request;
			}

			Cursor c = bdd.query(CITIES, new String[]{COL_ID,COL_NAME, COL_COUNTRY, COL_LATITUDE,COL_LONGITUDE,
					COL_TEMP,COL_CODE,COL_YAHOO_CODE,COL_DISTANCE,COL_POP}, where_clause, null, null, null, null);
			return cursorToCities(c);
		}catch(Exception e){
			return null;
		}

	}

	public ArrayList<City> getAllCities(){

		try{
			// request 

			Cursor c = bdd.query(CITIES, new String[]{COL_ID,COL_NAME, COL_COUNTRY, COL_LATITUDE,COL_LONGITUDE,
				COL_TEMP,COL_CODE,COL_YAHOO_CODE,COL_DISTANCE,COL_POP}, null, null, null, null, null);
			return cursorToCities(c);
		}catch(Exception e){
			return null;
		}

	}

	//Cette méthode permet de convertir un cursor en un livre
	private ArrayList<City> cursorToCities(Cursor c){
		//si aucun élément n'a été retourné dans la requête, on renvoie null
		if (c.getCount() == 0)
			return null;

		ArrayList<City> cities = new ArrayList<City>();
		//Sinon on se place sur le premier élément
		c.moveToFirst();
		//On créé un livre
		City current_city = new City();
		do{
			//on lui affecte toutes les infos grâce aux infos contenues dans le Cursor
			current_city.reset();

			current_city.name = c.getString(NUM_COL_NAME);
			current_city.country = c.getString(NUM_COL_COUNTRY);
			current_city.latitude = c.getString(NUM_COL_LATITUDE);
			current_city.longitude = c.getString(NUM_COL_LONGITUDE);
			current_city.temp = Integer.parseInt(c.getString(NUM_COL_TEMP));
			current_city.code = Integer.parseInt(c.getString(NUM_COL_CODE));
			current_city.yahoo_code = Integer.parseInt(c.getString(NUM_COL_YAHOO_CODE));
			current_city.distance = Double.parseDouble(c.getString(NUM_COL_DISTANCE));
			current_city.pop = Integer.parseInt(c.getString(NUM_COL_POP));

			cities.add(current_city.copy());
		}
		while(c.moveToNext());
		//On ferme le cursor
		c.close();

		//On retourne le livre
		return cities;
	}
}