package com.cghs.stresstest.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class NetworkUtils {
    private final static String TAG = "NetworkUtils";

    public static boolean isMobileConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mMobileNetworkInfo = cm
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (null != mMobileNetworkInfo) {
            return mMobileNetworkInfo.isAvailable()
                    && mMobileNetworkInfo.isConnected();
        }
        return false;
    }

    public static boolean networkTest() {
        boolean ret = false;
        URL serverURL;
        try {
            serverURL = new URL("http://www.baidu.com");
            Log.d(TAG, "-------- serverURL = " + serverURL.toString());
            // connect to server
            URLConnection uc2 = serverURL.openConnection();
            HttpURLConnection conn = (HttpURLConnection) uc2;
            Log.d(TAG, "--------00 serverURL = " + serverURL.toString());
            uc2.setAllowUserInteraction(true);
            uc2.setConnectTimeout(55000);
            uc2.setDoInput(true);
            uc2.setDoOutput(true);
            //conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            int numBytesRead = 0;
            int allBytesRead = 0;
            Log.d(TAG, "--------11 conn.getContentLength() = " + conn.getContentLength());
            InputStream in = conn.getInputStream();
            byte[] buffer = new byte[4096];
            do {
                numBytesRead = in.read(buffer);
                allBytesRead = allBytesRead + numBytesRead;
            } while (numBytesRead > 0);
            Log.d(TAG, " __________________-------- allBytesRead = " + allBytesRead + "   " + conn.getContentLength());
            if (allBytesRead > 10) {
                ret = true;
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }
}
