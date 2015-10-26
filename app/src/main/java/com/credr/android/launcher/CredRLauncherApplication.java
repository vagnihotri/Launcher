package com.credr.android.launcher;

import android.content.Intent;

import com.credr.android.launcher.Utils.Utils;
import com.credr.android.launcher.service.AppListenerService;
import com.credr.android.library.CredRApplication;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vijayagnihotri on 25/09/15.
 */
public class CredRLauncherApplication extends CredRApplication {

    @Override
    public void onCreate() {
        super.onCreate();
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
}
