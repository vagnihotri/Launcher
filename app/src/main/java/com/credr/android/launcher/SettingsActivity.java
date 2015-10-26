package com.credr.android.launcher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.credr.android.launcher.Utils.CustomViewGroup;
import com.credr.android.launcher.Utils.Utils;

/**
 * Created by vijayagnihotri on 26/10/15.
 */
public class SettingsActivity extends Activity implements View.OnClickListener{

    LinearLayout whitelistLayout, playStoreLayout, exitLayout;

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
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.whitelist_layout:
                break;
            case R.id.play_store_layout:
                break;
            case R.id.exit_layout:
                try {
                    getPackageManager().clearPackagePreferredActivities(getPackageName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putBoolean(Utils.PREF_LOCKING_MODE, false);
                editor.commit();
                Toast.makeText(this, "Locking mode turned off", Toast.LENGTH_SHORT).show();
                CustomViewGroup.getInstance(this).unlock();
                finishAffinity();
                break;
        }
    }
}
