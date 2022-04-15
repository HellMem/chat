package com.project.chatapp;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by gcoronad on 27/07/2018.
 */

public class Connection {

    private Activity act;

    public Connection(Activity activity) {
        this.act = activity;
    }

    public boolean estaConectado() {
        return conectadoWifi() || conectadoRedMovil();
    }


    public boolean conectadoWifi() {
        ConnectivityManager connectivity = (ConnectivityManager) act.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null) {
                if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (info.isConnected()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean conectadoRedMovil() {
        ConnectivityManager connectivity = (ConnectivityManager) act.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null) {
                if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                    if (info.isConnected()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
