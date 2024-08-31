package com.example.dogelauncher.app;

import android.app.Application;

public class DogeApp extends Application {
    private static Application INSTANCE = null;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }
    public static Application get() {
        return INSTANCE;
    }
}
