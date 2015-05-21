package com.example.jayb.sunshine3;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by jayb on 5/21/15.
 */
public class Utility {
    public static String getPrefferedLocation(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),context.getString(R.string.pref_location_default));
    }
}
