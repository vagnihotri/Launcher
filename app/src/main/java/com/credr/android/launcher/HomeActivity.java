package com.credr.android.launcher;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.StatusBarNotification;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.credr.android.launcher.Utils.Analytics;
import com.credr.android.launcher.Utils.CustomViewGroup;
import com.credr.android.launcher.Utils.NotificationStore;
import com.credr.android.launcher.Utils.Utils;
import com.credr.android.launcher.fragments.LoginFragment;
import com.credr.android.launcher.fragments.NotificationFragment;
import com.credr.android.launcher.fragments.PreferencesFragment;
import com.credr.android.launcher.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class HomeActivity extends Activity implements DialogInterface.OnDismissListener {

    GridView appGridView;
    ImageButton infoView, notificnView, settingsView;
    TextView notificnText;
    SharedPreferences sharedPreferences;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Start session
        Analytics.getInstance().startSession();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(Utils.isLockingModeActive(HomeActivity.this)) {
            CustomViewGroup.getInstance(this).lock();
        } else {
            CustomViewGroup.getInstance(this).unlock();
        }
        appGridView = (GridView)findViewById(R.id.appGrid);
        infoView = (ImageButton) findViewById(R.id.infoView);
        settingsView = (ImageButton) findViewById(R.id.settingsView);
        notificnView = (ImageButton) findViewById(R.id.notificationView);
        notificnText = (TextView) findViewById(R.id.notificationsText);
        fragmentManager = getFragmentManager();
        infoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isLockingModeActive(HomeActivity.this)) {
                    fragmentManager.beginTransaction().add(R.id.relContainer, new LoginFragment(), LoginFragment.LOGIN_FRAGMENT_TAG).commit();
                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    infoView.setImageDrawable(getResources().getDrawable(R.drawable.launcher_icon, getTheme()));
                    editor.putBoolean(Utils.PREF_LOCKING_MODE, true);
                    editor.commit();
                    CustomViewGroup.getInstance(HomeActivity.this).lock();
                    Toast.makeText(HomeActivity.this, "Please set launcher as default for home immediately", Toast.LENGTH_SHORT).show();
                }
            }
        });


        View.OnClickListener notificationListener =  new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentManager.beginTransaction().add(R.id.relContainer, new NotificationFragment(), NotificationFragment.NOTIFICATION_FRAGMENT_TAG).commit();
            }
        };
        notificnText.setOnClickListener(notificationListener);
        notificnView.setOnClickListener(notificationListener);
        getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        settingsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentManager.beginTransaction().add(R.id.relContainer, new PreferencesFragment(), PreferencesFragment.PREFERENCES_FRAGMENT_TAG).commit();
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
    public boolean onOptionsItemSelected(MenuItem item) {
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
        for(ResolveInfo resolveInfo : availableApps) {
            AppInfo appInfo = new AppInfo();
            appInfo.resolveInfo = resolveInfo;
            appInfo.label = resolveInfo.loadLabel(manager);
            appInfo.name = resolveInfo.activityInfo.packageName;
            appInfo.icon = resolveInfo.activityInfo.loadIcon(manager);
            if(Utils.isAppInAccessList(HomeActivity.this, appInfo.name.toString()) && !appInfo.name.toString().equalsIgnoreCase("com.android.settings") && !appInfo.name.toString().equalsIgnoreCase("com.credr.android.launcher"))
                appInfoList.add(appInfo);
        }
        //Adding CredR homepage
        AppInfo appInfo = new AppInfo();
        appInfo.label = "CredR Homepage";
        appInfo.name = "com.android.chrome";
        appInfo.data = "http://www.credr.com/";
        appInfo.icon = getResources().getDrawable(R.drawable.credr,getTheme());
        appInfoList.add(appInfo);
    }

    @Override
    public void onBackPressed() {
        dismissNonSettingsFragments();
        dismissDialogFragmentByTag(PreferencesFragment.PREFERENCES_FRAGMENT_TAG);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        Analytics.getInstance().stopEventTracking();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Analytics.getInstance().startEventTracking();
        EventBus.getDefault().register(this);
        if(Utils.isLockingModeActive(HomeActivity.this)) {
            infoView.setImageDrawable(getResources().getDrawable(R.drawable.launcher_icon, getTheme()));
        } else {
            infoView.setImageDrawable(getResources().getDrawable(R.drawable.unlock, getTheme()));
        }
        dismissNonSettingsFragments();
        loadApps();
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
                        if (getItem(position).data != null) {
                            i = new Intent(HomeActivity.this, WebViewActivity.class);
                            i.putExtra(WebViewActivity.URL_EXTRA, getItem(position).data);
                        } else if (getItem(position).name.toString().contains("ideafriend")) {
                            startActivity(getItem(position).resolveInfo);
                            return;
                        }
                        HomeActivity.this.startActivity(i);
                    }
                });

                return convertView;
            }
        });
        updateNotifications();
    }

    private void dismissNonSettingsFragments() {
        dismissDialogFragmentByTag(NotificationFragment.NOTIFICATION_FRAGMENT_TAG);
        dismissDialogFragmentByTag(LoginFragment.LOGIN_FRAGMENT_TAG);
    }

    private void updateNotifications() {
        Integer notfNumber = NotificationStore.getInstance().getNotifications().size();
        if(notfNumber > 0) {
            notificnText.setVisibility(View.VISIBLE);
            notificnText.setText(""+notfNumber);
        } else {
            notificnText.setVisibility(View.GONE);
        }
    }

    private void dismissDialogFragmentByTag(String tag) {
        DialogFragment fragment = (DialogFragment)fragmentManager.findFragmentByTag(tag);
        if(fragment !=null && fragment.isVisible()) {
            updateNotifications();
            fragment.dismiss();
        }
    }

    public void onEvent(StatusBarNotification sbn) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateNotifications();
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        updateNotifications();
    }

    public void startActivity(ResolveInfo launchableActivity) {
        ActivityInfo activity=launchableActivity.activityInfo;
        ComponentName name=new ComponentName(activity.applicationInfo.packageName,
                activity.name);
        Intent i=new Intent(Intent.ACTION_MAIN);

        i.addCategory(Intent.CATEGORY_LAUNCHER);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        i.setComponent(name);

        startActivity(i);
    }

    @Override
    public void onDestroy(){
        Analytics.getInstance().endSession();
        super.onDestroy();
    }
}
