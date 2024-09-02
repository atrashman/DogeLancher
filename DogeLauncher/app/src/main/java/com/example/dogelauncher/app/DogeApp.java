package com.example.dogelauncher.app;

import android.app.Application;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

public class DogeApp extends LitePalApplication {
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
