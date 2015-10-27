package com.credr.android.launcher.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
    public static final int AID_APP = 10000;
    public static final int AID_USER = 100000;

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
        if(accessList.contains(packageName) || packageName.equalsIgnoreCase("com.lenovo.keyguard.settings") || packageName.equalsIgnoreCase("com.android.settings") || packageName.equalsIgnoreCase("android") || packageName.equalsIgnoreCase("com.android.systemui") || packageName.equalsIgnoreCase("com.android.documentsui"))
            return true;
        else
            return false;
    }

    public static boolean isKeyInPrefs(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.contains(key);
    }

    public static void setAccessList (Context context, Set<String> accessList) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(PREF_ACCESS_LIST, accessList);
        editor.commit();
    }

    public static boolean isNetworkConnectionPresent(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public static String getForegroundApp() {
        File[] files = new File("/proc").listFiles();
        int lowestOomScore = Integer.MAX_VALUE;
        String foregroundProcess = null;

        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }

            int pid;
            try {
                pid = Integer.parseInt(file.getName());
            } catch (NumberFormatException e) {
                continue;
            }

            try {
                String cgroup = read(String.format("/proc/%d/cgroup", pid));

                String[] lines = cgroup.split("\n");

                if (lines.length != 2) {
                    continue;
                }

                String cpuSubsystem = lines[0];
                String cpuaccctSubsystem = lines[1];

                if (!cpuaccctSubsystem.endsWith(Integer.toString(pid))) {
                    // not an application process
                    continue;
                }

                if (cpuSubsystem.endsWith("bg_non_interactive")) {
                    // background policy
                    continue;
                }

                String cmdline = read(String.format("/proc/%d/cmdline", pid));

                if (cmdline.contains("com.android.systemui")) {
                    continue;
                }

                int uid = Integer.parseInt(
                        cpuaccctSubsystem.split(":")[2].split("/")[1].replace("uid_", ""));
                if (uid >= 1000 && uid <= 1038) {
                    // system process
                    continue;
                }

                int appId = uid - AID_APP;
                int userId = 0;
                // loop until we get the correct user id.
                // 100000 is the offset for each user.
                while (appId > AID_USER) {
                    appId -= AID_USER;
                    userId++;
                }

                if (appId < 0) {
                    continue;
                }

                // u{user_id}_a{app_id} is used on API 17+ for multiple user account support.
                // String uidName = String.format("u%d_a%d", userId, appId);

                File oomScoreAdj = new File(String.format("/proc/%d/oom_score_adj", pid));
                if (oomScoreAdj.canRead()) {
                    int oomAdj = Integer.parseInt(read(oomScoreAdj.getAbsolutePath().trim()));
                    if (oomAdj != 0) {
                        continue;
                    }
                }

                int oomscore = Integer.parseInt(read(String.format("/proc/%d/oom_score", pid)));
                if (oomscore < lowestOomScore) {
                    lowestOomScore = oomscore;
                    foregroundProcess = cmdline;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return foregroundProcess;
    }

    private static String read(String path) throws IOException {
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(path));
        output.append(reader.readLine());
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            output.append('\n').append(line);
        }
        reader.close();
        return output.toString();
    }
}
