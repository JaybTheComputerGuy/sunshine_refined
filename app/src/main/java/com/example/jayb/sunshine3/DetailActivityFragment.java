package com.example.jayb.sunshine3;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.jayb.sunshine3.DetailActivity.*;
import com.example.jayb.sunshine3.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks{
    private static final int DETAIL_LOADER = 0;
    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = "#Sunshine App";
    private String mForecastStr;
    private String mLocation;
    public static final String DATE_KEY = "date";
    public static final String LOCATION_KEY = "location";


    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        if(null != savedInstanceState) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
    }

    public void onResume(){
        super.onResume();
        if(null != mLocation && mLocation.equals(Utility.getPrefferedLocation(getActivity()))){
            getLoaderManager().restartLoader(DETAIL_LOADER,null,this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();
        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
            mForecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            ((TextView) rootView.findViewById(R.id.detail_text)).setText(mForecastStr);
        }
        return rootView;
    }

    private Intent createShareIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        if(null != mLocation) {
            outState.putString(LOCATION_KEY, mLocation);
        }
    }

    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater){
        inflater.inflate(R.menu.detailfragement,menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);

        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if(mShareActionProvider != null){
            mShareActionProvider.setShareIntent(createShareIntent());
        }
        else{
            Log.d(LOG_TAG,"Share Action provider is null");
        }



    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String[] columns = {
                WeatherContract.WeatherEntries.TABLE_NAME +"."+WeatherContract.WeatherEntries.COL_LOC_KEY,
                WeatherContract.WeatherEntries.COLUMN_DATEtEXT,
                WeatherContract.WeatherEntries.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntries.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntries.COLUMN_MIN_TEMP,
                WeatherContract.WeatherEntries.COLUMN_HUMIDITY,
                WeatherContract.WeatherEntries.COLUMN_PRESSURE,
                WeatherContract.WeatherEntries.COLUMN_WIND_SPEED,
                WeatherContract.WeatherEntries.COLUMN_DEGREES,
                WeatherContract.WeatherEntries.COLUMN_WEATHER_ID,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
        };

        mLocation = Utility.getPrefferedLocation(getActivity());
        Uri weatherUri = WeatherContract.WeatherEntries.buildWeatherLocationWithDate(mLocation,);
        return new CursorLoader(
                getActivity(),
                weatherUri,
                columns,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader,Cursor data) {
        if(data.moveToFirst()){
            String description = data.getString(data.getColumnIndex(WeatherContract.WeatherEntries.COLUMN_SHORT_DESC));
            String dateText = data.getString(data.getColumnIndex(WeatherContract.WeatherEntries.COLUMN_DATEtEXT));

            double high = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntries.COLUMN_MAX_TEMP));
            double  low = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntries.COLUMN_MIN_TEMP));

            boolean isMetric = Utility.isMetric(getActivity());

            TextView dateView = (TextView)getView().findViewById(R.id.detail_date_textview);
            TextView forecastView = (TextView)getView().findViewById(R.id.detail_forecast_textview);
            TextView highView = (TextView)getView().findViewById(R.id.detail_high_textview);
            TextView lowView = (TextView)getView().findViewById(R.id.detail_low_textview);

            dateView.setText(Utility.formatDate(dateText));
            forecastView.setText(description);
            highView.setText(Utility.formatTemperature(high,isMetric)+"\u00b0");
            lowView.setText(Utility.formatTemperature(low,isMetric)+"\u00b0");

            mForecastStr = String.format("%s - %s - %s/%s",dateView.getText(),
                    forecastView.getText(),highView.getText(),lowView.getText());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader,Cursor data) {

    }
}
