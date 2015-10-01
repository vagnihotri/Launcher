package com.credr.android.launcher.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by vijayagnihotri on 25/09/15.
 */
public class Utils {

    public static final String PREF_LOCKING_MODE = "LOCKING_MODE";
    public static final String PREF_ACCESS_LIST = "ACCESS_LIST";

    public static String loadPreferencesJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("prefs.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static void setPreferencesFromJSON(Context context, JSONObject jsonObject) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean lockingMode = false;
        Set<String> accessList = new HashSet<>();
        try {
            lockingMode = jsonObject.getBoolean("locking_mode");
            JSONArray accessListArray = jsonObject.getJSONArray("access_list");
            for (int index = 0; index < accessListArray.length(); index++) {
                Log.d("AppInfo","*** App: "+accessListArray.getString(index));
                accessList.add(accessListArray.getString(index));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        editor.putBoolean(PREF_LOCKING_MODE, lockingMode);
        editor.putStringSet(PREF_ACCESS_LIST, accessList);
        editor.commit();
    }

    public static boolean isLockingModeActive(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_LOCKING_MODE, false);
    }

    public static boolean isAppInAccessList(Context context, String packageName) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> accessList = sp.getStringSet(PREF_ACCESS_LIST, new HashSet<String>());
        if(accessList.contains(packageName) || packageName.equalsIgnoreCase("com.lenovo.keyguard.settings") || packageName.equalsIgnoreCase("com.android.settings") || packageName.equalsIgnoreCase("android") || packageName.equalsIgnoreCase("com.android.systemui"))
            return true;
        else
            return false;
    }

    public static boolean isKeyInPrefs(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.contains(key);
    }
}
