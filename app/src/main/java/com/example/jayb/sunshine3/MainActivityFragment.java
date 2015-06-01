package com.example.jayb.sunshine3;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
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
import com.example.jayb.sunshine3.service.SunshineService;
import com.example.jayb.sunshine3.sync.SunshineSyncAdapter;

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
    private ForecastAdapter mforeCastAdapter;
    private String mLocation;
    private static final int FORECAST_LOADER = 0;
    private int mPosition;


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
    private boolean mUseTodayLayout;



    public interface CallBack{
        public void onItemSelected(String date);
    }
    public MainActivityFragment() {
    }

    @Override
    public void  onActivityCreated(Bundle savedInstanceState){
            super.onActivityCreated(savedInstanceState);
            getLoaderManager().initLoader(FORECAST_LOADER,null,this);
    }
    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;

        if(null != mforeCastAdapter){
            mforeCastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    @Override
    public void onSaveInstanceBundle(Bundle outState){
        if(mPosition != ListView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);

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
        SunshineSyncAdapter.syncImmediately(getActivity());
    }


    public void onStart(){
        super.onStart();
    }

    public void onResume(){
        super.onResume();
        if(mLocation != null && !Utility.getPreferredLocation(getActivity()).equals(mLocation)){
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mforeCastAdapter = new ForecastAdapter(getActivity(),null,0);
        mforeCastAdapter.setUseTodayLayout(mUseTodayLayout);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);


        //List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecast_array));

        mforeCastAdapter = new ForecastAdapter(getActivity(),null,0);
        listView.setAdapter(mforeCastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = mforeCastAdapter.getCursor();

                if(null != cursor && cursor.moveToPosition(position)){
                    ((CallBack) getActivity()).onItemSelected(cursor.getString(COL_WEATHER_DATE));
                }
                mPosition = position;
            }
        });


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

        if(savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY) ){
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }



        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String startDate = WeatherContract.getDBdateString(new Date());

        String sortOrder = WeatherContract.WeatherEntries.COLUMN_DATEtEXT + "ASC";
        mLocation = Utility.getPreferredLocation(getActivity());
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

        if(mPosition  != ListView.INVALID_POSITION){
            mListView.setSelection(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mforeCastAdapter.swapCursor(null);
    }
}
