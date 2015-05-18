package com.example.jayb.sunshine3.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.jayb.sunshine3.data.WeatherContract.LocationEntry;
import com.example.jayb.sunshine3.data.WeatherContract.WeatherEntries;

/**
 * Created by jayb on 5/18/15.
 */
public class WeatherDbHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "weather.db";
    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_LOCATION_TABLE =
                "CREATE TABLE " + LocationEntry.TABLE_NAME + " ("+
                        LocationEntry._ID + "INTEGER PRIMARY KEY," +

                        LocationEntry.COLUMN_LOCATION_SETTING + "TEXT UNIQUE NOT NULL," +
                        LocationEntry.COLUMN_CITY_NAME + "TEXT NOT NULL," +
                        LocationEntry.COLUMN_COORD_LAT + "REAL NOT NULL," +
                        LocationEntry.COLUMN_COORD_LONG + "REAL NOT NULL," +

                        "UNIQUE (" + LocationEntry.COLUMN_LOCATION_SETTING + ") ON CONFLICT IGNORE;";


        final String SQL_CREATE_WEATHER_TABLE =
                "CREATE TABLE " + WeatherEntries.TABLE_NAME + " ("+
                        WeatherEntries._ID + "INTEGER PRIMARY KEY AUTOINCREMENT," +

                        WeatherEntries.COL_LOC_KEY + "INTEGER NOT NULL," +
                        WeatherEntries.COLUMN_DATEtEXT + "TEXT NOT NULL," +
                        WeatherEntries.COLUMN_SHORT_DESC + "TEXT NOT NULL," +
                        WeatherEntries.COLUMN_WEATHER_ID + "INTEGER NOT NULL," +
                        WeatherEntries.COLUMN_MIN_TEMP + "REAL NOT NULL," +
                        WeatherEntries.COLUMN_MAX_TEMP + "REAL NOT NULL," +
                        WeatherEntries.COLUMN_HUMIDITY + "REAL NOT NULL," +
                        WeatherEntries.COLUMN_PRESSURE + "REAL NOT NULL," +
                        WeatherEntries.COLUMN_WIND_SPEED + "REAL NOT NULL," +
                        WeatherEntries.COLUMN_DEGREES + "REAL NOT NULL," +

                        "FOREIGN KEY (" + WeatherEntries.COL_LOC_KEY + ") REFERENCES " +
                         LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), "+

                        "UNIQUE (" + WeatherEntries.COLUMN_DATEtEXT + ", " + WeatherEntries.COL_LOC_KEY + ") ON CONFLICT REPLACE;";

        db.execSQL(SQL_CREATE_LOCATION_TABLE);
        db.execSQL(SQL_CREATE_WEATHER_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i1, int i2) {
        db.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WeatherEntries.TABLE_NAME);
        onCreate(db);

    }
}
