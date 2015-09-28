package com.credr.android.launcher.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.credr.android.launcher.Utils.Utils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by vijayagnihotri on 25/09/15.
 */
public class AppListenerService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static final long INTERVAL = TimeUnit.SECONDS.toMillis(1); // periodic interval to check in seconds -> 2 seconds
    private static final String TAG = AppListenerService.class.getSimpleName();

    private Thread t = null;
    private Context mContext = null;
    private boolean running = false;

    @Override
    public void onDestroy() {
        Log.i(TAG, "Stopping service 'AppListenerService'");
        running =false;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting service 'AppListenerService'");
        running = true;
        mContext = this;

        // start a thread that periodically exits blocked apps
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    handleLockingMode();
                    try {
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                        Log.i(TAG, "Thread interrupted: 'AppListenerService'");
                    }
                }while(running);
                stopSelf();
            }
        });

        t.start();
        return Service.START_NOT_STICKY;
    }

    private void handleLockingMode() {
        // is Locking Mode active? 
        if(Utils.isLockingModeActive(mContext)) {
            killBlockedForegroundApp();
        }
    }

    private void killBlockedForegroundApp() {
        
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final int PROCESS_STATE_TOP = 2;
            ActivityManager.RunningAppProcessInfo currentInfo = null;
            Field field = null;
            try {
                field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
            } catch (Exception ignored) {
            }
            ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo app : appList) {
                if (app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        && app.importanceReasonCode == ActivityManager.RunningAppProcessInfo.REASON_UNKNOWN) {
                    Integer state = null;
                    try {
                        state = field.getInt(app);
                    } catch (Exception e) {
                    }
                    if (state != null && state == PROCESS_STATE_TOP) {
                        currentInfo = app;
                        break;
                    }
                }
            }
            if(currentInfo != null) {
                for (String packageName : currentInfo.pkgList) {
                    if(!Utils.isAppInAccessList(mContext,packageName) || packageName.equalsIgnoreCase("com.credr.credrlauncherapp")){
                        Log.d("AppFore","*** " + packageName + " detected ***");
                        minimizeApp();
                    }
                }
            }
        } else {
        /*ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        return (!mContext.getApplicationContext().getPackageName().equals(componentInfo.getPackageName()));*/
        }
    }



    private void minimizeApp() {
        Intent intent = new Intent();
        intent
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
