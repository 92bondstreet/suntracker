package com.sun.tracker.db;
 
import java.util.ArrayList;

import com.sun.tracker.parser.City;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
 
public class Top25DB{
 
	private static final int VERSION_BDD = 1;
	private static final String NOM_BDD = "top25.db";
 
	private static final String TOP_25 = "top25";
	private static final String COL_ID = "id";
	private static final int NUM_COL_ID = 0;
	private static final String COL_NAME = "name";
	private static final int NUM_COL_NAME = 1;
	private static final String COL_COUNTRY = "country";
	private static final int NUM_COL_COUNTRY = 2;
	private static final String COL_CONTINENT = "continent";
	private static final int NUM_COL_CONTINENT = 3;
	private static final String COL_TEMP = "temp";
	private static final int NUM_COL_TEMP = 4;
	private static final String COL_CODE = "code";
	private static final int NUM_COL_CODE = 5;
	private static final String COL_YAHOO_CODE = "yahoo_code";
	private static final int NUM_COL_YAHOO_CODE = 6;
 
	private SQLiteDatabase bdd;
 
	private Top25Sqlite maBaseSQLite;
 
	public Top25DB(Context context){
		//On créer la BDD et sa table
		maBaseSQLite = new Top25Sqlite(context, NOM_BDD, null, VERSION_BDD);
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
		bdd.delete(TOP_25, null, null);
		}catch(Exception e)
		{
		}
	}
	
	
	public long insertTopCity(City current_city){
		//Création d'un ContentValues (fonctionne comme une HashMap)
		ContentValues values = new ContentValues();
		//on lui ajoute une valeur associé à une clé (qui est le nom de la colonne dans laquelle on veut mettre la valeur)
		values.put(COL_NAME, current_city.name);
		values.put(COL_COUNTRY, current_city.country);
		values.put(COL_CONTINENT, current_city.continent);
		values.put(COL_TEMP, Integer.toString(current_city.temp));
		values.put(COL_CODE, Integer.toString(current_city.code));
		values.put(COL_YAHOO_CODE, Integer.toString(current_city.yahoo_code));
		
		//on insère l'objet dans la BDD via le ContentValues
		return bdd.insert(TOP_25, null, values);
	}

	public ArrayList<City> getTop25(){
		
		try{
			Cursor c = bdd.query(TOP_25, new String[]{COL_ID,COL_NAME, COL_COUNTRY, COL_CONTINENT,
					COL_TEMP,COL_CODE,COL_YAHOO_CODE}, null, null, null, null, null);
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
 
		ArrayList<City> top25_cities = new ArrayList<City>();
		//Sinon on se place sur le premier élément
		c.moveToFirst();
		//On créé un livre
		City current_city = new City();
		do{
		//on lui affecte toutes les infos grâce aux infos contenues dans le Cursor
		current_city.reset();
			
		current_city.name = c.getString(NUM_COL_NAME);
		current_city.country = c.getString(NUM_COL_COUNTRY);
		current_city.continent = c.getString(NUM_COL_CONTINENT);
		current_city.temp = Integer.parseInt(c.getString(NUM_COL_TEMP));
		current_city.code = Integer.parseInt(c.getString(NUM_COL_CODE));
		current_city.yahoo_code = Integer.parseInt(c.getString(NUM_COL_YAHOO_CODE));
		
		top25_cities.add(current_city.copy());
		}
		while(c.moveToNext());
		//On ferme le cursor
		c.close();
 
		//On retourne le livre
		return top25_cities;
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
		
		current_city.name = c.getString(NUM_COL_NAME);
		current_city.country = c.getString(NUM_COL_COUNTRY);
		current_city.continent = c.getString(NUM_COL_CONTINENT);
		current_city.temp = Integer.parseInt(c.getString(NUM_COL_TEMP));
		current_city.code = Integer.parseInt(c.getString(NUM_COL_CODE));
		current_city.yahoo_code = Integer.parseInt(c.getString(NUM_COL_YAHOO_CODE));
		
		//On ferme le cursor
		c.close();
 
		//On retourne le livre
		return current_city;
	}
}