package com.example.jayb.sunshine3.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by jayb on 5/19/15.
 */
public class WeatherProvider extends ContentProvider {
    public static final int WEATHER = 100;
    public static final int WEATHER_WITH_LOCATION = 101;
    public static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    public static final int LOCATION = 300;
    public static final int LOCATION_ID = 301;

    public static final UriMatcher sUriMatcher = buildUriMatcher();

    private WeatherDbHelper  mOpenHelper;
    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    static{
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        sWeatherByLocationSettingQueryBuilder.setTables(
              WeatherContract.WeatherEntries.TABLE_NAME + " INNER JOIN " +
                      WeatherContract.LocationEntry.TABLE_NAME + " ON " +
                      WeatherContract.WeatherEntries.TABLE_NAME + "." +
                      WeatherContract.WeatherEntries.COL_LOC_KEY + " = " +
                      WeatherContract.LocationEntry.TABLE_NAME +
                      " . " + WeatherContract.LocationEntry._ID);
    }

    private static final String sLocationSettingSelection = WeatherContract.LocationEntry.TABLE_NAME+ "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";
    private static final String sLocationSettingWithStartDateLocationSelection = WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
            WeatherContract.WeatherEntries.COLUMN_DATEtEXT + " >= ?";
    private static final String sLocationSettingWithDaySelection = WeatherContract.LocationEntry.TABLE_NAME + "." +
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
            WeatherContract.WeatherEntries.COLUMN_DATEtEXT + " = ?";

    private Cursor getWeatherByLocationSetting(Uri uri,String[] projection,String sortOrder){
        String locationSetting = WeatherContract.WeatherEntries.getLocationSettingFromUri(uri);
        String startDate = WeatherContract.WeatherEntries.getStartDateFromUri(uri);

        String[]  selectionArgs;
        String selection;

        if(startDate == null){
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        }
        else {
            selectionArgs = new String[]{locationSetting,startDate};
            selection = sLocationSettingWithStartDateLocationSelection;
        }

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
                );

    }

    public Cursor getWeatherByLocationSettingWithDate(Uri uri,String[] projection,String sortOrder){
        String day = WeatherContract.WeatherEntries.getStartDateFromUri(uri);
        String locationSetting = WeatherContract.WeatherEntries.getLocationSettingFromUri(uri);

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingWithDaySelection,
                new String[]{locationSetting,day},
                null,
                null,
                sortOrder
        );
    }


    public static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;

        matcher.addURI(authority,WeatherContract.PATH_WEATHER,WEATHER);
        matcher.addURI(authority,WeatherContract.PATH_WEATHER + "/*",WEATHER_WITH_LOCATION);
        matcher.addURI(authority,WeatherContract.PATH_WEATHER + "/*/*",WEATHER_WITH_LOCATION_AND_DATE);

        matcher.addURI(authority,WeatherContract.PATH_LOCATION,LOCATION);
        matcher.addURI(authority,WeatherContract.PATH_LOCATION + "/#",LOCATION_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) throws UnsupportedOperationException {
        Cursor retCursor;

        switch(sUriMatcher.match(uri)){
            case WEATHER_WITH_LOCATION_AND_DATE:
            {
                retCursor = getWeatherByLocationSettingWithDate(uri,projection,sortOrder);
                break;
            }
            case WEATHER_WITH_LOCATION:
            {
                retCursor = getWeatherByLocationSetting(uri,projection,sortOrder);
                break;
            }
            case WEATHER:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntries.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case LOCATION_ID:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection,
                        WeatherContract.LocationEntry._ID + "=" + ContentUris.parseId(uri),
                        selectionArgs,
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case LOCATION:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unkown uri" + uri);

        }
        retCursor.setNotificationUri(getContext().getContentResolver(),uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match){
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntries.CONTENT_ITEM_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntries.CONTENT_TYPE;
            case WEATHER:
                return WeatherContract.WeatherEntries.CONTENT_TYPE;
            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_TYPE;
            case LOCATION_ID:
                return WeatherContract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        Uri returnUri;

        switch(match){
            case WEATHER: {
                long _id = db.insert(WeatherContract.WeatherEntries.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = WeatherContract.WeatherEntries.buildWeatherUri(_id);
                } else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LOCATION:{
                long _id = db.insert(WeatherContract.LocationEntry.TABLE_NAME,null,values);
                if(_id > 0){
                    returnUri = WeatherContract.LocationEntry.buildLocationUri(_id);
                }
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unkown uri:" + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return returnUri;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match){
            case WEATHER:{
                rowsDeleted = db.delete(WeatherContract.WeatherEntries.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case LOCATION:{
                rowsDeleted = db.delete(WeatherContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unkown uri " + uri);
        }

        if(null == selection && 0!= rowsDeleted){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match){
            case WEATHER:{
                rowsUpdated = db.update(WeatherContract.WeatherEntries.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case LOCATION:{
                rowsUpdated = db.update(WeatherContract.LocationEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unkown uri " + uri);
        }

        if(0 != rowsUpdated){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match){
            case WEATHER:{
                db.beginTransaction();
                int returnCount = 0;
                try{
                    for(ContentValues value:values){
                        long _id = db.insert(WeatherContract.WeatherEntries.TABLE_NAME,null,value);
                        if(-1 != returnCount){
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                    ;
                }finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri,null);
                return returnCount;
            }
            default:
                return super.bulkInsert(uri, values);
        }

    }
}
