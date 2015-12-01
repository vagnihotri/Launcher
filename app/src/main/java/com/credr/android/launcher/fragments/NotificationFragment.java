package com.credr.android.launcher.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.credr.android.launcher.R;
import com.credr.android.launcher.Utils.NotificationStore;

import java.util.List;

/**
 * Created by vijayagnihotri on 27/11/15.
 */
public class NotificationFragment extends DialogFragment {

    public static final String NOTIFICATION_FRAGMENT_TAG = "Notification";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View cView = inflater.inflate(R.layout.notification_layout, null);
        RemoteViews remoteViews =  new RemoteViews(getActivity().getPackageName(), R.layout.notification_layout);
        List<StatusBarNotification> notificationList = NotificationStore.getInstance().getNotifications();
        for(StatusBarNotification statusBarNotification : notificationList) {
            if(statusBarNotification.getNotification() != null) {
                RemoteViews remoteView = statusBarNotification.getNotification().contentView;
                remoteViews.addView(R.id.notifcnContainer, remoteView);
            }
        }
        return cView;
    }
}
