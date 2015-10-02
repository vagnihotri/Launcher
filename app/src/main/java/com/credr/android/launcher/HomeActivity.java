package com.credr.android.launcher;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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

import com.credr.android.launcher.Utils.Utils;
import com.credr.android.launcher.fragments.LoginFragment;
import com.credr.android.launcher.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends Activity {

    GridView appGridView;
    ImageButton infoView;
    SharedPreferences sharedPreferences;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        appGridView = (GridView)findViewById(R.id.appGrid);
        infoView = (ImageButton) findViewById(R.id.infoView);
        loadApps();
        fragmentManager = getFragmentManager();
        infoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utils.isLockingModeActive(HomeActivity.this)) {

                    fragmentManager.beginTransaction().add(R.id.relContainer, new LoginFragment(),LoginFragment.LOGIN_FRAGMENT_TAG).commit();
                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    infoView.setImageDrawable(getResources().getDrawable(R.drawable.launcher_icon, getTheme()));
                    editor.putBoolean(Utils.PREF_LOCKING_MODE, true);
                    editor.commit();
                }
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
        for(ResolveInfo resolveInfo : availableApps) {
            AppInfo appInfo = new AppInfo();
            appInfo.label = resolveInfo.loadLabel(manager);
            appInfo.name = resolveInfo.activityInfo.packageName;
            logInfo(appInfo.name);
            appInfo.icon = resolveInfo.activityInfo.loadIcon(manager);
            if(Utils.isAppInAccessList(HomeActivity.this, appInfo.name.toString()) && !appInfo.name.toString().equalsIgnoreCase("com.android.settings"))
                appInfoList.add(appInfo);
        }
    }

    void logInfo(Object info) {
        Log.d("Apps", String.valueOf(info));
    }

    @Override
    public void onBackPressed() {
        dismissLoginFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Utils.isLockingModeActive(HomeActivity.this)) {
            infoView.setImageDrawable(getResources().getDrawable(R.drawable.launcher_icon, getTheme()));
        } else {
            infoView.setImageDrawable(getResources().getDrawable(R.drawable.unlock, getTheme()));
        }
        dismissLoginFragment();
    }

    private void dismissLoginFragment() {
        LoginFragment loginFragment = (LoginFragment)fragmentManager.findFragmentByTag(LoginFragment.LOGIN_FRAGMENT_TAG);
        if(loginFragment!=null && loginFragment.isVisible()) {
            loginFragment.dismiss();
        }
    }
}
