package com.credr.android.launcher.Utils;

import android.service.notification.StatusBarNotification;

import java.util.ArrayList;

/**
 * Created by vijayagnihotri on 27/11/15.
 */
public class NotificationStore {

    private static NotificationStore mInstance = null;
    private ArrayList<StatusBarNotification> notificationList;

    public static NotificationStore getInstance() {
        if(mInstance == null) {
            mInstance = new NotificationStore();
        }
        return mInstance;
    }

    public NotificationStore() {
        notificationList = new ArrayList<>();
    }


    public void addNotification(StatusBarNotification statusBarNotification) {
        notificationList.add(statusBarNotification);
    }

    public void removeNotification(StatusBarNotification statusBarNotification) {
        notificationList.remove(statusBarNotification);
    }

    public void removeAllNotifications() {
        notificationList.clear();
    }
    public ArrayList<StatusBarNotification> getNotifications() {
        return notificationList;
    }
}
