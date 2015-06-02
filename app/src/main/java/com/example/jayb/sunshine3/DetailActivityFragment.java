package com.example.jayb.sunshine3;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jayb.sunshine3.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private static final String LOCATION_KEY = "location";

    public static final String DATE_KEY = "forecast_date";

    private String mForecastStr;
    private String mLocation;
    private String mDateStr;

    private static final int DETAIL_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntries.TABLE_NAME + "." + WeatherContract.WeatherEntries._ID,
            WeatherContract.WeatherEntries.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntries.COLUMN_DATEtEXT,
            WeatherContract.WeatherEntries.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntries.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntries.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntries.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntries.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntries.COLUMN_PRESSURE,
            WeatherContract.WeatherEntries.COLUMN_DEGREES,
    };

    private TextView friendlyDateView;
    private TextView dateView;
    private TextView descriptionView;
    private TextView highTempView;
    private TextView lowTempView;
    private TextView humidityView;
    private TextView windView;
    private TextView pressureView;
    private ImageView iconView;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mDateStr = arguments.getString(DetailActivity.DATE_KEY);
        }

        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        friendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        dateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        descriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        highTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        lowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        humidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        windView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        pressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
        iconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragement, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        ShareActionProvider mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mShareActionProvider != null ) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mForecastStr + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }

        if (getArguments() != null && getArguments().containsKey(DetailActivity.DATE_KEY)) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null &&
                !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(LOCATION_KEY, mLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntries.COLUMN_DATEtEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());

        Uri weatherForLocationUri = WeatherContract.WeatherEntries.buildWeatherLocationWithDate(
                mLocation, mDateStr);
        Log.v(LOG_TAG, weatherForLocationUri.toString());

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
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
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) { return; }

        String unformattedDateStr = data.getString(data.getColumnIndex(WeatherContract.WeatherEntries.COLUMN_DATEtEXT));

        String dayName = Utility.getDayName(getActivity(), unformattedDateStr);
        friendlyDateView.setText(dayName);

        String dateString = Utility.getFormattedMonthDay(getActivity(), unformattedDateStr);
        dateView.setText(dateString);

        String weatherDescription =
                data.getString(data.getColumnIndex(WeatherContract.WeatherEntries.COLUMN_SHORT_DESC));
        descriptionView.setText(weatherDescription);

        boolean isMetric = Utility.isMetric(getActivity());

        String high = Utility.formatTemperature(getActivity(),
                data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntries.COLUMN_MAX_TEMP)), isMetric);
        highTempView.setText(high);

        String low = Utility.formatTemperature(getActivity(),
                data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntries.COLUMN_MIN_TEMP)), isMetric);
        lowTempView.setText(low);

        String humidity = getActivity().getString(
                R.string.format_humidity, data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntries.COLUMN_HUMIDITY)));
        humidityView.setText(humidity);

        String windSpeed = Utility.getFormattedWind(getActivity(),
                data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntries.COLUMN_WIND_SPEED)),
                data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntries.COLUMN_DEGREES)));
        windView.setText(windSpeed);

        String pressure = getActivity().getString(
                R.string.format_pressure, data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntries.COLUMN_PRESSURE)));
        pressureView.setText(pressure);

        int weatherId = data.getInt(data.getColumnIndex(WeatherContract.WeatherEntries.COLUMN_WEATHER_ID));
        iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));


        // We still need this for the share intent
        mForecastStr = String.format("%s - %s - %s/%s",
                dateString, weatherDescription, high, low);

        Log.v(LOG_TAG, "Forecast String: " + mForecastStr);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}