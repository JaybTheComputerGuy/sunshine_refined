package com.example.jayb.sunshine3;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.jayb.sunshine3.data.WeatherContract;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by jayb on 5/21/15.
 */
public class Utility {
    public static String getPrefferedLocation(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    public static String formatTemperature(Context context, double temperature) {
        double temp = 0;
        String suffix = "\u00B0";
        if (!isMetric(context)) {
            temp = 9*temperature/5 + 32;
        }

        return String.format("%.0F", temp);
    }

    static String formatDate(String dateString) {
        Date date = WeatherContract.getDateFromDb(dateString);
        return DateFormat.getDateInstance().format(date);
    }
}
