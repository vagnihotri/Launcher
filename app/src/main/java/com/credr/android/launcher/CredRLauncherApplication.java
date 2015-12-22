package com.credr.android.launcher;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.credr.android.launcher.Utils.Analytics;
import com.credr.android.launcher.Utils.Utils;
import com.credr.android.launcher.service.AppListenerService;
import com.credr.android.library.connection.REST;
import com.credr.android.library.db.DBContext;
import com.credr.android.library.utilities.Cache;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vijayagnihotri on 25/09/15.
 */
public class CredRLauncherApplication extends Application {

    private static Application appContext;
    Class<? extends com.credr.android.library.connection.REST> REST = REST.class;
    public CredRLauncherApplication() {

    }

    public void setup(Class<? extends REST> restClass) {
        if(appContext != null) {
            throw new IllegalStateException("Setup should be called only once before onCreate");
        } else {
            this.REST = restClass;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        DBContext.init(this);
        Cache.initialize(this);

        try {
            Object intent = this.REST.newInstance();
            ((REST)this.REST.newInstance()).initialize(this.getValueForManifestKey("SERVER"), this);
        } catch (Exception var2) {
            var2.printStackTrace();
        }

        Intent intent1 = new Intent();
        intent1.setAction("com.credr.android.intent.action.LAUNCH_COMPLETED");
        this.sendBroadcast(intent1);

        // init Analytics
        Analytics.initInstance(this).initAnalytics();

        //Fabric.with(this, new Crashlytics());
        startAppListenerService();
        loadPreferences();
    }

    private void loadPreferences() {
        if(Utils.isKeyInPrefs(this,Utils.PREF_LOCKING_MODE)) return;
        String stringPreferences = Utils.loadPreferencesJSONFromAsset(this);
        try {
            JSONObject jsonPreferences = new JSONObject(stringPreferences);
            Utils.setPreferencesFromJSON(this, jsonPreferences);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void startAppListenerService() {
        startService(new Intent(this, AppListenerService.class));
    }

    public void onTerminate() {
        super.onTerminate();
        DBContext.terminate();
    }

    public Application getAppContext() {
        return appContext;
    }

    public String getValueForManifestKey(String key) {
        PackageManager pm = appContext.getPackageManager();

        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(appContext.getPackageName(), 128);
            return applicationInfo.metaData.getString(key);
        } catch (Exception var4) {
            Log.d("LOG CredRApplication", "Couldn\'t find config value : " + key);
            return null;
        }
    }
}
