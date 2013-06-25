package com.sun.tracker.db;

import com.sun.tracker.parser.City;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class WeatherLocalDB{

	private static final int VERSION_BDD = 1;
	private static final String NOM_BDD = "weather_local.db";

	private static final String WEATHER_LOCAL = "weather_local";
	private static final String COL_ID = "id";
	private static final int NUM_COL_ID = 0;
	private static final String COL_LATITUDE = "latitude";
	private static final int NUM_COL_LATITUDE = 1;
	private static final String COL_LONGITUDE = "longitude";
	private static final int NUM_COL_LONGITUDE = 2;
	private static final String COL_TEMP = "temp";
	private static final int NUM_COL_TEMP = 3;
	private static final String COL_CODE = "code";
	private static final int NUM_COL_CODE = 4;
	private static final String COL_YAHOO_CODE = "yahoo_code";
	private static final int NUM_COL_YAHOO_CODE = 5;

	private SQLiteDatabase bdd;

	private WeatherLocalSqlite maBaseSQLite;

	public WeatherLocalDB(Context context){
		//On créer la BDD et sa table
		maBaseSQLite = new WeatherLocalSqlite(context, NOM_BDD, null, VERSION_BDD);
	}

	public void open(){
		//on ouvre la BDD en écriture
		bdd = maBaseSQLite.getWritableDatabase();
	}

	public void close(){
		//on ferme l'accès à la BDD
		bdd.close();
	}

	public void cleanBDD(){
		bdd.delete(WEATHER_LOCAL, null, null);
	}

	public SQLiteDatabase getBDD(){
		return bdd;
	}

	public long updateCity(City current_city){
		//Création d'un ContentValues (fonctionne comme une HashMap)
		ContentValues values = new ContentValues();
		//on lui ajoute une valeur associé à une clé (qui est le nom de la colonne dans laquelle on veut mettre la valeur)
		values.put(COL_LATITUDE, current_city.latitude);
		values.put(COL_LONGITUDE, current_city.longitude);
		values.put(COL_TEMP, Integer.toString(current_city.temp));
		values.put(COL_CODE, Integer.toString(current_city.code));
		values.put(COL_YAHOO_CODE, Integer.toString(current_city.yahoo_code));

		//on insère l'objet dans la BDD via le ContentValues
		return bdd.insert(WEATHER_LOCAL, null,values);
	}


	public City getCurrentCity(){

		try{
			Cursor c = bdd.query(WEATHER_LOCAL, new String[]{COL_ID,COL_LATITUDE, COL_LONGITUDE,
					COL_TEMP,COL_CODE,COL_YAHOO_CODE}, null, null, null, null, null);
			return cursorToCity(c);
		}catch(Exception e){
			return null;
		}

	}

	//Cette méthode permet de convertir un cursor en un livre
	private City cursorToCity(Cursor c){
		//si aucun élément n'a été retourné dans la requête, on renvoie null
		if (c.getCount() == 0)
			return null;

		//Sinon on se place sur le premier élément
		c.moveToFirst();
		//On créé un livre
		City current_city = new City();
		//on lui affecte toutes les infos grâce aux infos contenues dans le Cursor

		current_city.latitude = c.getString(NUM_COL_LATITUDE);
		current_city.longitude = c.getString(NUM_COL_LONGITUDE);
		current_city.temp = Integer.parseInt(c.getString(NUM_COL_TEMP));
		current_city.code = Integer.parseInt(c.getString(NUM_COL_CODE));
		current_city.yahoo_code = Integer.parseInt(c.getString(NUM_COL_YAHOO_CODE));

		//On ferme le cursor
		c.close();

		//On retourne le livre
		return current_city;
	}
}