package com.example.dogelauncher.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.example.dogelauncher.app.DogeApp;
import com.example.dogelauncher.model.AppData;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 需要完成数据的增删查和修改排列顺序（也就是改）
 *
 * */
public class AppDataUtil {
    private static String TAG = "DataUtil";
    private static List<AppData> allData = null;

    public static List<AppData> getInstalledApps() {
        if (allData != null) return allData;
        List<AppData> dbData = getAppDataFromDB();
        allData = dbData.size() == 0 ? getAppDataByPMAndSave() : dbData;
        return allData;
    }

    //规定顺序:
    public static final Comparator<AppData> mDataComparator = (o1, o2)
            -> Integer.compare(o1.getPos(), o2.getPos());

    @NonNull
    private static List<AppData> getAppDataFromDB() {

        allData = LitePal.findAll(AppData.class);
        allData.sort(mDataComparator);

        return allData;
    }

    @NonNull
    public static List<AppData> getAppDataByPMAndSave() {

        Context context = DogeApp.get();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_META_DATA);

        List<AppData> appData = new ArrayList<>(packages.size());

//        appData.sort(mDataComparator);

        for (PackageInfo pkgInfo : packages) {
            ApplicationInfo appInfo = pkgInfo.applicationInfo;
            if(appInfo.loadIcon(context.getPackageManager())==null)
                continue;
            if((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                continue;
            }
            AppData data = new AppData(DogeApp.get(), appInfo);
            appData.add(data);
        }
        LitePal.saveAll(appData);
        return appData;
    }

    public static AppData findAppByPackageName(String packName) throws PackageManager.NameNotFoundException {
        Context context = DogeApp.get();
        ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packName, 0);
        return new AppData(context, applicationInfo);
    }

}