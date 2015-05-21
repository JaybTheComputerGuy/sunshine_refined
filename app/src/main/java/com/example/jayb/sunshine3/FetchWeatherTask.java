package com.example.jayb.sunshine3;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.jayb.sunshine3.data.WeatherContract;
import com.example.jayb.sunshine3.data.WeatherContract.LocationEntry.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jayb on 5/21/15.
 */
public class FetchWeatherTask extends AsyncTask<String,Void,Void> {
    private final  String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    String forecastJsonStr = null;

    public FetchWeatherTask(Context context){

    }

    @Override
    protected Void doInBackground(String... params) {
        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "coord";

        final String OWM_LATTITUDE = "lat";
        final String OWM_LONGITUDE = "lon";
        final String OWM_LIST = "list";
        final String OWM_DATETIME = "dt";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_COORD = "cord";





        try {


            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            String cityName = cityJson.getString(OWM_CITY_NAME);

            JSONObject cityCord = cityJson.getJSONObject(OWM_COORD);
            double cityLattitude = cityCord.getDouble(OWM_LATTITUDE);
            double cityLongitude = cityCord.getDouble(OWM_LONGITUDE);
        }
        catch(JSONException e){
            Log.e(LOG_TAG,e.getMessage(),e);
            e.printStackTrace();
        }






        if(params.length == 0){
            return null;
        }

        String locationQuery = params[0];

        String format = "json";
        String units = "metric";
        int numDays = 14;

        try {
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon().
                    appendQueryParameter(QUERY_PARAM,params[0]).
                    appendQueryParameter(FORMAT_PARAM, format).
                    appendQueryParameter(UNITS_PARAM,units).
                    appendQueryParameter(DAYS_PARAM,Integer.toString(numDays)).build();


            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {// Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            forecastJsonStr = buffer.toString();
            Log.v(LOG_TAG,"ForecastJson String" + forecastJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);

            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null){
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }

            }
        }

        /*
        try{
            return getWeatherDataFromJson(forecastJsonStr,numDays);
        }
        catch(JSONException e){
            Log.e(LOG_TAG,e.getMessage(),e);
            e.printStackTrace();
        }*/

        return null;
    }

/*



    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    /*
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String unitType = sharedPrefs.getString(getString(R.string.pref_units_key),getString(R.string.pref_units_metric));

        if(unitType.equals(getString(R.string.pref_units_imperial))){
            high = (high * 1.8) * 32;
            low = (low * 1.8) * 32;
        }
        else if(!unitType.equals(getString(R.string.pref_units_metric))){
            Log.d(LOG_TAG, "Unit type not found :" + unitType);
        }

        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;

        return highLowStr;
    }
*/
    /*
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DATETIME = "dt";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        String[] resultStrs = new String[numDays];
        for(int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime = dayForecast.getLong(OWM_DATETIME);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);
            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        for(String s:resultStrs){
            Log.v(LOG_TAG,"Forecast Entry:" + s );

        }

        return resultStrs;
    }


*/

    private long addLocation(String locationSetting,String cityName,double lat,double lon){
        Log.v(LOG_TAG,"Inserting " + cityName +", with cord" + lon +", "+lat);

        Cursor cursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null
        );

        if(cursor.moveToFirst()){
            Log.v(LOG_TAG,"FOund it in database");
            int locationIdIndex = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            return cursor.getLong(locationIdIndex);
        }
        else{
            Log.v(LOG_TAG, "Dint find it in the database.. inserting now");
            ContentValues locationValues = new ContentValues();
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,locationSetting);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME,cityName);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT,lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG,lon);

            Uri locationInsertUri = mContext.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI,locationValues);

        }

        return ContentUris.parseId(locationInsertUri);
    }






 /*   @Override
    protected void onPostExecute(String[] result) {
        if(result != null){
            mforeCastAdapter.clear();

            for(String dayForecastStr:result){
                mforeCastAdapter.add(dayForecastStr);
            }
        }
    }*/
}
