package com.example.jayb.sunshine3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.jayb.sunshine3.sync.SunshineSyncAdapter;


public class MainActivity extends ActionBarActivity implements MainActivityFragment.CallBack {
    boolean mTwopane;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainActivityFragment fragment = ((MainActivityFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast));
        fragment.setUseTodayLayout(mTwopane);

        if(findViewById(R.id.weather_detail_container) != null){
            mTwopane = true;
            fragment.setUseTodayLayout(false);



            Bundle args = new Bundle();
            DetailActivityFragment fragment2 = new DetailActivityFragment();
            fragment2.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container,fragment2).commit();

        }

        SunshineSyncAdapter.initializeSyncAdapter(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String location = sharedPrefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));

        Uri geolocation = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q",location).build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geolocation);

        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }
        else{
            Log.d("Location", "Could not call" + location + ". not supported");
        }
    }


    @Override
    public void onItemSelected(String date) {
        if(mTwopane){
            Bundle args = new Bundle();
            args.putString(DetailActivityFragment.DATE_KEY,date);

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container,fragment).commit();
        }
        else{
            Intent intent = new Intent(this,DetailActivity.class).putExtra(DetailActivityFragment.DATE_KEY,date);
            startActivity(intent);

        }
    }


}
