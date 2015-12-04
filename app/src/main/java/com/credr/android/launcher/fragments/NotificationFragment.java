package com.credr.android.launcher.fragments;

import android.app.DialogFragment;
import android.app.PendingIntent;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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
        RelativeLayout containerView = (RelativeLayout)cView.findViewById(R.id.notifcnContainer);
        List<StatusBarNotification> notificationList = NotificationStore.getInstance().getNotifications();
        int index = Integer.MAX_VALUE;
        for(final StatusBarNotification statusBarNotification : notificationList) {
            if(statusBarNotification.getNotification() != null) {
                RemoteViews remoteView = statusBarNotification.getNotification().contentView;
                View notfView = remoteView.apply(getActivity(), container);
                notfView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            statusBarNotification.getNotification().contentIntent.send();
                            NotificationStore.getInstance().removeNotification(statusBarNotification);
                            NotificationFragment.this.dismiss();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                });
                notfView.setId(index);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                if(index < Integer.MAX_VALUE ) {
                    params.addRule(RelativeLayout.BELOW, index + 1);
                }
                containerView.addView(notfView, params);
            }
            index--;
        }
        return cView;
    }
}
