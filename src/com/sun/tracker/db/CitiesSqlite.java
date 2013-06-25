package com.sun.tracker.db;
 
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
 
public class CitiesSqlite extends SQLiteOpenHelper {
	
	private static final String CITIES = "cities";
	private static final String COL_ID = "id";
	private static final String COL_NAME = "name";
	private static final String COL_COUNTRY = "country";
	private static final String COL_LATITUDE = "latitude";
	private static final String COL_LONGITUDE = "longitude";
	private static final String COL_TEMP = "temp";
	private static final String COL_CODE = "code";
	private static final String COL_YAHOO_CODE = "yahoo_code";
	private static final String COL_DISTANCE = "distance";
	private static final String COL_POP = "pop";
	 
	private static final String CREATE_BDD = "CREATE TABLE " + CITIES + " ("
	+ COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_NAME + " TEXT NOT NULL, "
	+ COL_COUNTRY + " TEXT NOT NULL, "
	+ COL_LATITUDE + " TEXT NOT NULL, "
	+ COL_LONGITUDE + " TEXT NOT NULL, "
	+ COL_TEMP + " integer, "
	+ COL_CODE + " integer, "
	+ COL_DISTANCE + " FLOAT, "
	+ COL_POP + " integer, "
	+ COL_YAHOO_CODE + " TEXT NOT NULL);";
 
	public CitiesSqlite(Context context, String name,CursorFactory factory, int version) {
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
		db.execSQL("DROP TABLE " + CITIES + ";");
		onCreate(db);
	}
 
}