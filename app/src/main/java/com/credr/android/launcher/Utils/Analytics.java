package com.credr.android.launcher.Utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

import com.cloud.datagrinchsdk.api.DataGrinchApi;
import com.credr.android.launcher.R;

import java.util.HashMap;

/**
 * Created by vijayagnihotri on 21/12/15.
 */
public class Analytics {

    private static Analytics mInstance = null;

    private static final boolean LOCATION_TRACKING = true;
    private static final boolean AUTO_TRACKING = false;

    private Context context;

    public static class EventKeys {
        public static final String MINIMIZE_APP = "App minimized";
    }

    public Analytics(Context context) {
        this.context = context;
    }

    public static Analytics initInstance(Context context) {
        if(mInstance == null && context != null) {
            mInstance = new Analytics(context);
        }
        return mInstance;
    }

    public static Analytics getInstance() {
        return mInstance;
    }

    public void initAnalytics() {
        DataGrinchApi.getInstance().init(context.getString(R.string.datagrinch_api_key), context, LOCATION_TRACKING, -1, AUTO_TRACKING);
        try {
            TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            String imeiNumber = telephonyManager.getDeviceId();
            DataGrinchApi.getInstance().setUserName(imeiNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            DataGrinchApi.getInstance().setAppVersion(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void startSession() {
        DataGrinchApi.getInstance().startSession(context);
    }

    public void endSession() {
        DataGrinchApi.getInstance().endSession(context);
    }

    public void startEventTracking() {
        DataGrinchApi.getInstance().onStart();
    }

    public void stopEventTracking() {
        DataGrinchApi.getInstance().onStop();
    }

    public void logEvents(String eventName) {
        DataGrinchApi.getInstance().logEvents(context, eventName);
    }

    public void logData(String key, String value){
        HashMap<String, String> dataMap = new HashMap();
        dataMap.put(key, value);
        DataGrinchApi.getInstance().logCustomData(context, dataMap);
    }
}
