package com.credr.android.launcher.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.credr.android.launcher.R;

/**
 * Created by vijayagnihotri on 23/12/15.
 */
public class BrightnessFragment extends DialogFragment {

    public static final String BRIGHTNESS_FRAGMENT_TAG = "Brightness";
    private static final int MAX_BRIGHTNESS_VALUE = 255;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View cView = inflater.inflate(R.layout.brightness_layout, null);
        CheckBox mAutomatic = (CheckBox) cView.findViewById(R.id.automatic_checkbox);
        SeekBar mBrightnessBar = (SeekBar) cView.findViewById(R.id.brightness_seekbar);

        mBrightnessBar.setMax(MAX_BRIGHTNESS_VALUE);
        mAutomatic.setChecked(getBrightnessMode() == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        mBrightnessBar.setProgress(getBrightness());

        mAutomatic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setAutomaticBrightness();
                } else {
                    setManualBrightness();
                }
            }

        });
        mBrightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 10) {
                    setBrightness(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });
        return cView;
    }

    private void setBrightness(int value) {
        Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, value);
        Window window = getActivity().getWindow();
        WindowManager.LayoutParams layoutpars = window.getAttributes();
        layoutpars.screenBrightness = value / (float) MAX_BRIGHTNESS_VALUE;
        window.setAttributes(layoutpars);
    }

    private int getBrightness() {
        try {
            return Settings.System.getInt(getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return MAX_BRIGHTNESS_VALUE;
        }
    }

    private void setAutomaticBrightness() {
        Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }

    private void setManualBrightness() {
        Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    private int getBrightnessMode() {
        try {
            return Settings.System.getInt(getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        }
    }

}
