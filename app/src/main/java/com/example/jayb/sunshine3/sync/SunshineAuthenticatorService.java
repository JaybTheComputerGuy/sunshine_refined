package com.example.jayb.sunshine3.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by jayb on 6/1/15.
 */
public class SunshineAuthenticatorService extends Service {
    private SunshineAuthenticator mAuthenticator;

    public void onCreate(){
        mAuthenticator = new SunshineAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public IBinder onBinder(){
        return mAuthenticator.getIBinder();
    }

}
