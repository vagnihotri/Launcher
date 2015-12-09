package com.credr.android.launcher.service;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.credr.android.launcher.Utils.NotificationStore;
import com.credr.android.launcher.Utils.Utils;

import de.greenrobot.event.EventBus;

/**
 * Created by vijayagnihotri on 26/11/15.
 */
public class SbNotificationListenerService extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if(Utils.isAppInAccessList(getBaseContext(),sbn.getPackageName())) {
            if(sbn.getPackageName().equalsIgnoreCase("android")) return;
            NotificationStore.getInstance().addNotification(sbn);
            EventBus.getDefault().post(sbn);
        }
    }
}
