package com.credr.android.launcher.Utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * Created by vijayagnihotri on 26/10/15.
 */
public class CustomViewGroup extends ViewGroup {
    private Context context;
    private static CustomViewGroup mInstance = null;
    private WindowManager wManager;
    private CustomViewGroup lockView;

    public static CustomViewGroup getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new CustomViewGroup(context);
        }
        return mInstance;
    }

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

    public void lock() {
        if(lockView != null) return;
        //lock top notification/status bar
        wManager = ((WindowManager) context.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE));

        WindowManager.LayoutParams topBlockParams = new WindowManager.LayoutParams();
        topBlockParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        topBlockParams.gravity = Gravity.TOP;
        topBlockParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        topBlockParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        topBlockParams.height = (int) (50 * context.getResources()
                .getDisplayMetrics().scaledDensity);
        topBlockParams.format = PixelFormat.TRANSPARENT;

        lockView = new CustomViewGroup(context);
        wManager.addView(lockView, topBlockParams);
    }

    public void unlock() {
        if (lockView!=null) {
            if (lockView.isShown()) {
                wManager.removeView(lockView);
                lockView = null;
            }
        }
    }
}
