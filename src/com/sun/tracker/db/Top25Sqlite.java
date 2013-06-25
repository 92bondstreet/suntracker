package com.sun.tracker.db;
 
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
 
public class Top25Sqlite extends SQLiteOpenHelper {
	
	private static final String TOP_25 = "top25";
	private static final String COL_ID = "id";
	private static final String COL_NAME = "name";
	private static final String COL_COUNTRY = "country";
	private static final String COL_CONTINENT = "continent";
	private static final String COL_TEMP = "temp";
	private static final String COL_CODE = "code";
	private static final String COL_YAHOO_CODE = "yahoo_code";
 
	private static final String CREATE_BDD = "CREATE TABLE " + TOP_25 + " ("
	+ COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_NAME + " TEXT NOT NULL, "
	+ COL_COUNTRY + " TEXT NOT NULL, "
	+ COL_CONTINENT + " TEXT NOT NULL, "
	+ COL_TEMP + " TEXT NOT NULL, "
	+ COL_CODE + " TEXT NOT NULL, "
	+ COL_YAHOO_CODE + " TEXT NOT NULL);";
 
	public Top25Sqlite(Context context, String name,CursorFactory factory, int version) {
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
		db.execSQL("DROP TABLE " + TOP_25 + ";");
		onCreate(db);
	}
 
}