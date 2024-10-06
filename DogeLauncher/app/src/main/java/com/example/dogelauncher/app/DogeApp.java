package com.example.dogelauncher.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;

import org.litepal.LitePalApplication;

public class DogeApp extends LitePalApplication {
    private static DogeApp INSTANCE = null;

    //msg.what
    public static final int GET_DATA = 0;


    private static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            super.handleMessage(msg);
        }
    };
    public static int dpiTimes;
    public static int widthPixels;
    public static int heightPixels;
    public static int statusBarHei;

    public static Handler getGlobalHandler () {
        return mHandler;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int densityDpi = displayMetrics.densityDpi;
        dpiTimes = densityDpi/160;

        widthPixels = displayMetrics.widthPixels;
        heightPixels = displayMetrics.heightPixels;
    }
    public static DogeApp get() {
        return INSTANCE;
    }

    public int getStatusBarHeight() {
        if (statusBarHei == 0) {
            statusBarHei = 0;
            Context context = this;
            int resourceId = context.getResources().getIdentifier("status_bar_background", "id", "android");
            if (resourceId > 0) {
                View statusBarView = ((Activity) context).getWindow().findViewById(resourceId);
                if (statusBarView != null) {
                    statusBarHei = statusBarView.getHeight();
                }
            }
        }
        return statusBarHei;
    }
}
