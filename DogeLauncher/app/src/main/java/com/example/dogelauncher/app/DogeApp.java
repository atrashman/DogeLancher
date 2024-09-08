package com.example.dogelauncher.app;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.litepal.LitePalApplication;

public class DogeApp extends LitePalApplication {
    private static Application INSTANCE = null;

    //msg.what
    public static final int GET_DATA = 0;


    private static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            super.handleMessage(msg);
        }
    };

    public static Handler getGlobalHandler () {
        return mHandler;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }
    public static Application get() {
        return INSTANCE;
    }

}
