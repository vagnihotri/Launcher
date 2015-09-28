package com.credr.android.launcher;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.credr.android.launcher.Utils.Utils;
import com.credr.android.launcher.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends Activity {

    GridView appGridView;
    TextView infoView;
    SharedPreferences sharedPreferences;

    /*List<String> accessList = Arrays.asList("com.whatsapp","com.android.dialer","com.android.mms",
            "com.android.gallery3d", "com.credr.android.library","com.credr.android.credr_inspector",
            "com.credr.android.app.connect", "com.credr.android.library", "com.credr.android.launcher");*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        appGridView = (GridView)findViewById(R.id.appGrid);
        infoView = (TextView) findViewById(R.id.infoView);
        loadApps();
        infoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if(Utils.isLockingModeActive(HomeActivity.this)) {
                    infoView.setText("Locking Mode Turned Off");
                    editor.putBoolean(Utils.PREF_LOCKING_MODE, false);
                    getPackageManager().clearPackagePreferredActivities(getPackageName());
                    //resetPreferredLauncherAndOpenChooser(HomeActivity.this);
                } else {
                    infoView.setText("Locking Mode Turned On");
                    editor.putBoolean(Utils.PREF_LOCKING_MODE, true);
                }
                editor.commit();
            }
        });
        appGridView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return appInfoList.size();
            }

            @Override
            public AppInfo getItem(int position) {
                return appInfoList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            class Holder {
                ImageView icon;
                TextView label;

                Holder(View view) {
                    icon = (ImageView) view.findViewById(R.id.icon);
                    label = (TextView) view.findViewById(R.id.label);
                    view.setTag(this);
                }
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.app_item, null);
                    new Holder(convertView);
                }

                Holder holder = (Holder) convertView.getTag();
                AppInfo appInfo = getItem(position);
                holder.icon.setImageDrawable(appInfo.icon);
                holder.label.setText(appInfo.label);

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = manager.getLaunchIntentForPackage(getItem(position).name.toString());
                        HomeActivity.this.startActivity(i);
                    }
                });

                return convertView;
            }
        });

        getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Toast.makeText(this, "Paused Launcher", Toast.LENGTH_LONG).show();
        logInfo("Paused Launcher");

        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> recentTasks = activityManager.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo runningAppProcessInfo : recentTasks) {
            /*String packageName = runningAppProcessInfo.pkgList;
            logInfo(packageName);
            if(!accessList.contains(packageName))
                activityManager.killBackgroundProcesses(packageName);*/
            //logInfo(runningAppProcessInfo.pkgList);
            for (String packageName : runningAppProcessInfo.pkgList) {
                logInfo(packageName);
                if(!Utils.isAppInAccessList(HomeActivity.this,packageName))
                    logInfo("Not Found In Access List");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private PackageManager manager;
    private List<AppInfo> appInfoList;
    private void loadApps() {
        manager = getPackageManager();
        appInfoList = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> availableApps = manager.queryIntentActivities(intent,0);
        logInfo("App count : " + availableApps.size());
        for(ResolveInfo resolveInfo : availableApps) {
            AppInfo appInfo = new AppInfo();
            appInfo.label = resolveInfo.loadLabel(manager);
            appInfo.name = resolveInfo.activityInfo.packageName;
            logInfo(appInfo.name);
            appInfo.icon = resolveInfo.activityInfo.loadIcon(manager);
            if(Utils.isAppInAccessList(HomeActivity.this, appInfo.name.toString()))
                appInfoList.add(appInfo);
        }
    }

    void logInfo(Object info) {
        //infoView.setText(infoView.getText() + " ## " + String.valueOf(info));
        Log.d("Apps", String.valueOf(info));
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_HOME){
            //Start Launcher
            if (!sharedPreferences.getBoolean(Utils.PREF_LOCKING_MODE, false)) {
                //openApp(HomeActivity.this, "com.android.launcher");
                Intent i = manager.getLaunchIntentForPackage("com.android.launcher");
                HomeActivity.this.startActivity(i);
            }

        }
        return super.onKeyDown(keyCode, event);
    }

//    public static boolean openApp(Context context, String packageName) {
//        PackageManager manager = context.getPackageManager();
//        try {
//            Intent i = manager.getLaunchIntentForPackage(packageName);
//            if (i == null) {
//                return false;
//                //throw new PackageManager.NameNotFoundException();
//            }
//            i.addCategory(Intent.CATEGORY_LAUNCHER);
//            context.startActivity(i);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }


    @Override
    protected void onResume() {
        super.onResume();
        if(Utils.isLockingModeActive(HomeActivity.this)) {
            infoView.setText("Locking Mode Turned On");
        } else {
            infoView.setText("Locking Mode Turned Off");
        }
    }

    public static void resetPreferredLauncherAndOpenChooser(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, "com.android.launcher");
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Intent selector = new Intent(Intent.ACTION_MAIN);
        selector.addCategory(Intent.CATEGORY_HOME);
        selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(selector);

        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
    }
}
