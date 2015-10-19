package com.credr.android.launcher;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    WindowManager wManager;
    CustomViewGroup lockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(Utils.isLockingModeActive(HomeActivity.this)) lock(this);
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
                    lock(HomeActivity.this);
                    Toast.makeText(HomeActivity.this, "Please set launcher as default for home immediately", Toast.LENGTH_SHORT).show();
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
                        if(getItem(position).data != null) {
                            String label = getItem(position).label.toString();
                            if(label.contains("WiFi")) {
                                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                return;
                            } else if(label.contains("Data")) {
                                startActivity(new Intent(Settings.ACTION_APN_SETTINGS));
                                return;
                            } else if(label.contains("GPS")) {
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                return;
                            } else {
                                i = new Intent(HomeActivity.this, WebViewActivity.class);
                                i.putExtra(WebViewActivity.URL_EXTRA, getItem(position).data);
                            }
                        }
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
        //Adding CredR homepage
        AppInfo appInfo = new AppInfo();
        appInfo.label = "CredR Homepage";
        appInfo.name = "com.android.chrome";
        appInfo.data = "http://www.credr.com";
        appInfo.icon = getResources().getDrawable(R.drawable.credr,getTheme());
        appInfoList.add(appInfo);

        appInfo = new AppInfo();
        appInfo.label = "WiFi Settings";
        appInfo.name = "com.android.chrome";
        appInfo.data = "http://www.credr.com";
        appInfo.icon = getResources().getDrawable(R.drawable.wifi,getTheme());
        appInfoList.add(appInfo);

        appInfo = new AppInfo();
        appInfo.label = "Data Settings";
        appInfo.name = "com.android.chrome";
        appInfo.data = "http://www.credr.com";
        appInfo.icon = getResources().getDrawable(R.drawable.apn,getTheme());
        appInfoList.add(appInfo);

        appInfo = new AppInfo();
        appInfo.label = "GPS Settings";
        appInfo.name = "com.android.chrome";
        appInfo.data = "http://www.credr.com";
        appInfo.icon = getResources().getDrawable(R.drawable.gps,getTheme());
        appInfoList.add(appInfo);
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

    private class CustomViewGroup extends ViewGroup {
        private Context context;

        public CustomViewGroup(Context context) {
            super(context);
            this.context = context;
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }
        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return true;
        }
    }

    public void lock(Activity activity) {

        //lock top notification/status bar
        wManager = ((WindowManager) activity.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE));

        WindowManager.LayoutParams topBlockParams = new WindowManager.LayoutParams();
        topBlockParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        topBlockParams.gravity = Gravity.TOP;
        topBlockParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        topBlockParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        topBlockParams.height = (int) (50 * activity.getResources()
                .getDisplayMetrics().scaledDensity);
        topBlockParams.format = PixelFormat.TRANSPARENT;

        lockView = new CustomViewGroup(activity);
        wManager.addView(lockView, topBlockParams);
    }

    public void unlock() {
        if (lockView!=null) {
            if (lockView.isShown()) {
                wManager.removeView(lockView);
            }
        }
    }
}
