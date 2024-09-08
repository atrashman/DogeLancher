package com.example.dogelauncher.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

public class AppUtil {
    public static void startAppByPkgName (Context context, String pkgName) {
        PackageManager packageManager = context.getPackageManager();

        // 通过包名获取启动应用的 Intent
        Intent launchIntent = packageManager.getLaunchIntentForPackage(pkgName);

        // 如果 Intent 不为空，启动应用
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  // 启动新的任务栈
            context.startActivity(launchIntent);
        } else {
            // 如果找不到应用，提示用户
            Toast.makeText(context, "未找到该应用: " + pkgName, Toast.LENGTH_SHORT).show();
        }
    }
}
