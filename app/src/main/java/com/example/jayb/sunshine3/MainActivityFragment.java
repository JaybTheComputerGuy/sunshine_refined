package com.example.jayb.sunshine3;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private ArrayAdapter<String> mforeCastAdapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] forecast_array = {
             "Today - Sunny 89/90",
                "Tommorrow - Sunny 89/90",
                "Mon - Sunny 89/90",
                "Tue - Sunny 89/90",
                "Wed - Sunny 89/90",
                "Thur - Sunny 89/90",
                "Fri - Sunny 89/90"
        };

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecast_array));

        mforeCastAdapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,weekForecast);
        ListView listView = (ListView)rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mforeCastAdapter);

        return rootView;
    }
}
