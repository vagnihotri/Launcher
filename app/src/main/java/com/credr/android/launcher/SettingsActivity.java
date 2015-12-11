package com.credr.android.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.credr.android.launcher.Utils.CustomViewGroup;
import com.credr.android.launcher.Utils.Utils;
import com.credr.android.launcher.model.AppInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by vijayagnihotri on 26/10/15.
 */

public class SettingsActivity extends Activity implements View.OnClickListener{

    private LinearLayout whitelistLayout, playStoreLayout, exitLayout;
    private PackageManager packageManager;
    private List<AppInfo> appInfoList = new ArrayList<>();
    private Set<String> packageList = new HashSet<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        whitelistLayout = (LinearLayout)findViewById(R.id.whitelist_layout);
        playStoreLayout = (LinearLayout)findViewById(R.id.play_store_layout);
        exitLayout = (LinearLayout)findViewById(R.id.exit_layout);
        whitelistLayout.setOnClickListener(this);
        playStoreLayout.setOnClickListener(this);
        exitLayout.setOnClickListener(this);
        populateAppsList();
    }

    private void populateAppsList() {
        packageManager = getPackageManager();
        appInfoList = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> availableApps = packageManager.queryIntentActivities(intent,0);
        for(ResolveInfo resolveInfo : availableApps) {
            AppInfo appInfo = new AppInfo();
            appInfo.resolveInfo = resolveInfo;
            appInfo.label = resolveInfo.loadLabel(packageManager);
            appInfo.name = resolveInfo.activityInfo.packageName;
            appInfo.icon = resolveInfo.activityInfo.loadIcon(packageManager);
            if(!appInfo.name.toString().equalsIgnoreCase("com.android.settings") && !appInfo.name.toString().equalsIgnoreCase(getPackageName())) {
                appInfoList.add(appInfo);
                if(Utils.isAppInAccessList(this, appInfo.name.toString())){
                    packageList.add(appInfo.name.toString());
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.whitelist_layout:
                showModifyWhiteListDialog();
                break;
            case R.id.play_store_layout:
                launchPlayStore();
                break;
            case R.id.exit_layout:
                Utils.setLockingMode(this, false);
                Toast.makeText(this, "Locking mode turned off", Toast.LENGTH_SHORT).show();
                CustomViewGroup.getInstance(this).unlock();
                try {
                    getPackageManager().clearPackagePreferredActivities(getPackageName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finishAffinity();
                break;
        }
    }

    private void showModifyWhiteListDialog() {
        final Dialog whiteListDialog = new Dialog(this);
        whiteListDialog.setContentView(R.layout.apps_dialog);
        whiteListDialog.show();
        whiteListDialog.setTitle("Add or Remove Apps");
        RelativeLayout cancelLayout, okayLayout;
        cancelLayout = (RelativeLayout) whiteListDialog.findViewById(R.id.cancel_tab);
        okayLayout = (RelativeLayout) whiteListDialog.findViewById(R.id.okay_tab);
        cancelLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                whiteListDialog.dismiss();
            }
        });
        okayLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAccessList();
                whiteListDialog.dismiss();
            }
        });
        ListView wListView = (ListView) whiteListDialog.findViewById(R.id.apps_whitelist);
        wListView.setAdapter(new BaseAdapter() {
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
                CheckBox checkBox;

                Holder(View view) {
                    icon = (ImageView) view.findViewById(R.id.app_icon);
                    label = (TextView) view.findViewById(R.id.app_label);
                    checkBox = (CheckBox) view.findViewById(R.id.app_checkbox);
                }
            }

            @Override
            public View getView(int position, View convertView, ViewGroup viewGroup) {
                convertView = getLayoutInflater().inflate(R.layout.apps_list_item, null);
                final Holder holder = new Holder(convertView);
                final AppInfo appInfo = getItem(position);
                holder.icon.setImageDrawable(appInfo.icon);
                holder.label.setText(appInfo.label);
                holder.checkBox.setChecked(packageList.contains(appInfo.name.toString()));
                holder.checkBox.setTag(appInfo.name.toString());
                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        String appName = (String) holder.checkBox.getTag();
                        if (isChecked) {
                            if (appName != null) packageList.add(appName);
                        } else {
                            if (appName != null) packageList.remove(appName);
                        }
                    }
                });
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CheckBox checkBox = (CheckBox) view.findViewById(R.id.app_checkbox);
                        checkBox.setChecked(!checkBox.isChecked());
                    }
                });
                return convertView;
            }
        });
    }

    private void setAccessList() {
        packageList.add("com.android.settings");
        packageList.add(getPackageName());
        Utils.setAccessList(this, packageList);
    }

    private void launchPlayStore() {
        Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=dummy"));
        ComponentName playStoreComponentName=null;

        for(ResolveInfo resolveInfo : packageManager.queryIntentActivities(market, 0))
        {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            String packageName = activityInfo.applicationInfo.packageName;
            if (!packageName.contains("android"))
                continue;
            playStoreComponentName =  new ComponentName(packageName, activityInfo.name);
            break;
        }

        if(playStoreComponentName!=null)
        {
            Intent intent = new Intent();
            intent.setComponent(playStoreComponentName);
            intent.setData(Uri.parse("market://"));
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/"));
            startActivity(intent);
        }
    }
}
