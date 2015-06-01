package com.example.jayb.sunshine3.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by jayb on 6/1/15.
 */
public class SunshineSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static SunshineSyncAdapter sunshineSyncAdapter = null;


    @Override
    public void onCreate(){
        synchronized (sSyncAdapterLock){
            if(sunshineSyncAdapter == null){
                sunshineSyncAdapter = new SunshineSyncAdapter(getApplicationContext(),true);
;            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sunshineSyncAdapter.getSyncAdapterBinder();
    }
}
