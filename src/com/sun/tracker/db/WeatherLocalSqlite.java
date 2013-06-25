package com.sun.tracker.db;
 
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
 
public class WeatherLocalSqlite extends SQLiteOpenHelper {
	
	private static final String WEATHER_LOCAL = "weather_local";
	private static final String COL_ID = "id";
	private static final String COL_LATITUDE = "latitude";
	private static final String COL_LONGITUDE = "longitude";
	private static final String COL_TEMP = "temp";
	private static final String COL_CODE = "code";
	private static final String YAHOO_CODE = "yahoo_code";
 
	private static final String CREATE_BDD = "CREATE TABLE " + WEATHER_LOCAL + " ("
	+ COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_LATITUDE + " TEXT NOT NULL, "
	+ COL_LONGITUDE + " TEXT NOT NULL, "
	+ COL_TEMP + " TEXT NOT NULL, "
	+ COL_CODE + " TEXT NOT NULL, "
	+ YAHOO_CODE + " TEXT NOT NULL);";
 
	public WeatherLocalSqlite(Context context, String name,CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

 
	@Override
	public void onCreate(SQLiteDatabase db) {
		//on créé la table à partir de la requête écrite dans la variable CREATE_BDD
		db.execSQL(CREATE_BDD);
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//On peut fait ce qu'on veut ici moi j'ai décidé de supprimer la table et de la recréer
		//comme ça lorsque je change la version les id repartent de 0
		db.execSQL("DROP TABLE " + WEATHER_LOCAL + ";");
		onCreate(db);
	}
 
}