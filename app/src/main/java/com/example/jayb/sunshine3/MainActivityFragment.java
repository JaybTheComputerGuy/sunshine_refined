package com.example.jayb.sunshine3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jayb.sunshine3.data.WeatherContract;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private SimpleCursorAdapter mforeCastAdapter;
    private String mLocation;
    private static final int FORECAST_LOADER = 0;


    public static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntries.TABLE_NAME + "." + WeatherContract.WeatherEntries._ID,
            WeatherContract.WeatherEntries.COLUMN_DATEtEXT,
            WeatherContract.WeatherEntries.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntries.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntries.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;


    public MainActivityFragment() {
    }

    @Override
    public void  onActivityCreated(Bundle savedInstanceState){
            super.onActivityCreated(savedInstanceState);
            getLoaderManager().initLoader(FORECAST_LOADER,null,this);
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
       inflater.inflate(R.menu.forecastfragment, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh){
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateWeather(){
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());

        String location = Utility.getPrefferedLocation(getActivity());
        weatherTask.execute(location);
    }

    public void onStart(){
        super.onStart();
    }

    public void onResume(){
        super.onResume();
        if(mLocation != null && !Utility.getPrefferedLocation(this).equals(mLocation)){
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);



        //List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecast_array));

        mforeCastAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.list_item_forecast,dateText,
                null,
                new String[]{WeatherContract.WeatherEntries.COLUMN_DATEtEXT,
                WeatherContract.WeatherEntries.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntries.COLUMN_MAX_TEMP,
                        WeatherContract.WeatherEntries.COLUMN_MIN_TEMP
                },
                new int[]{
                        R.id.list_item_date_textview,
                        R.id.list_item_forecast_textview,
                        R.id.list_item_high_textview,
                        R.id.list_item_forecast
                },
                0
        );

        mforeCastAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                boolean isMetric = Utility.isMetric(getActivity());
                switch(columnIndex){
                    case COL_WEATHER_MAX_TEMP:
                    case COL_WEATHER_MIN_TEMP:{
                        ((TextView) view).setText(Utility.formatTemperature(getActivity(),cursor.getDouble(columnIndex)));
                        return true;
                    }
                    case COL_WEATHER_DATE:{
                        String dateString = cursor.getString(columnIndex);
                        TextView dateView = (TextView) view;
                        dateView.setText(Utility.formatDate(dateString));
                        return true;
                    }

                }
                return false;
            }
        });

        ListView listView = (ListView)rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mforeCastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //String forecast = mforeCastAdapter.getItem(i);
                //Toast.makeText(getActivity(),forecast,Toast.LENGTH_SHORT).show();
                SimpleCursorAdapter adapter = (SimpleCursorAdapter) adapterView.getAdapter();
                Cursor cursor = adapter.getCursor();

                if(null != cursor && cursor.moveToPosition(i)) {
                   Intent intent = new Intent(getActivity(),DetailActivity.class).
                           putExtra(DetailActivityFragment.DATE_KEY,cursor.getString(COL_WEATHER_DATE));
                    startActivity(intent);
                }
            }
        });




        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String startDate = WeatherContract.getDBdateString(new Date());

        String sortOrder = WeatherContract.WeatherEntries.COLUMN_DATEtEXT + "ASC";
        mLocation = Utility.getPrefferedLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntries.buildWeatherLocationWithDate(mLocation,startDate);

        Log.d("Fragement", "URI" + weatherForLocationUri.toString());


        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mforeCastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mforeCastAdapter.swapCursor(null);
    }
}
