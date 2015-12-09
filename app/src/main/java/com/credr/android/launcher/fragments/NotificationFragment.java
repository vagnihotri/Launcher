package com.credr.android.launcher.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;

import com.credr.android.launcher.R;
import com.credr.android.launcher.Utils.NotificationStore;
import com.credr.android.launcher.listener.SwipeDismissTouchListener;

import java.util.List;

/**
 * Created by vijayagnihotri on 27/11/15.
 */
public class NotificationFragment extends DialogFragment {

    public static final String NOTIFICATION_FRAGMENT_TAG = "Notification";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View cView = inflater.inflate(R.layout.notification_layout, null);
        final LinearLayout containerView = (LinearLayout)cView.findViewById(R.id.notifcnContainer);
        List<StatusBarNotification> notificationList = NotificationStore.getInstance().getNotifications();
        int index = Integer.MAX_VALUE;
        for(final StatusBarNotification statusBarNotification : notificationList) {
            if(statusBarNotification.getNotification() != null) {
                RemoteViews remoteView = statusBarNotification.getNotification().contentView;
                final View notfView = remoteView.apply(getActivity(), container);
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
                notfView.setOnTouchListener(new SwipeDismissTouchListener(notfView, null, new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        NotificationStore.getInstance().removeNotification(statusBarNotification);
                        containerView.removeView(notfView);
                    }
                }));
                notfView.setId(index);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                if(index < Integer.MAX_VALUE ) {
                    params.addRule(RelativeLayout.BELOW, index + 1);
                }
                containerView.addView(notfView, params);
            }
            index--;
        }
        ImageView dismissView = (ImageView) cView.findViewById(R.id.dismiss_icon);
        dismissView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationStore.getInstance().removeAllNotifications();
                containerView.removeAllViews();
            }
        });
        return cView;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }
}
