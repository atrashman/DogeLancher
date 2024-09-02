package com.example.dogelauncher.model;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import com.example.dogelauncher.R;
import com.example.dogelauncher.app.DogeApp;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.util.Objects;

public class AppData extends LitePalSupport {

    private static final String TAG = "AppData";

    @Column(ignore = true)
    private Context context;
    @Column(ignore = true)
    private ApplicationInfo applicationInfo;

    //id
    private Long id;

    // 标签
    private String appLabel;
    // 程序所在路径
    private String pkgPath;
    // 包名
    private String pkgName;
    // icon
    @Column(ignore = true)
    private Drawable icon;

    //位置 网格布局中的位置
    private int pos;


    public AppData() {
        context = DogeApp.get();
    }

    public AppData(Context context, ApplicationInfo applicationInfo) {
        this.context = context;
        this.applicationInfo = applicationInfo;
        pkgPath = applicationInfo.sourceDir;
        pkgName = applicationInfo.packageName;
        appLabel = loadAppLabel(context);
        icon = loadIcon();
    }

    private Drawable loadIcon() {
        loadApplicationInfo();
        if (icon == null) {
            if (new File(pkgPath).exists()) {
                icon = applicationInfo.loadIcon(context.getPackageManager());
            }
        }
        if (icon == null) {
            icon = context.getResources().getDrawable(R.mipmap.ic_launcher);
        }
        return icon;
    }

    private String loadAppLabel(Context context) {
        loadApplicationInfo();
        if (appLabel == null) {
            if (!new File(pkgPath).exists()) {
                return applicationInfo.packageName;
            } else {
                CharSequence label = applicationInfo.loadLabel(context.getPackageManager());
                return (label != null) ? label.toString() : applicationInfo.packageName;
            }
        }
        Log.i(TAG, "AppData存储的信息  标签： " + appLabel + "  包名： " + pkgName);
        return appLabel;
    }

    private void loadApplicationInfo() {
        if (applicationInfo == null) {
            if (!TextUtils.isEmpty(pkgName)) {
                PackageManager pm = context.getPackageManager();
                try {
                    applicationInfo = pm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                throw new RuntimeException("pkg Name is Empty");
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppLabel() {
        return appLabel;
    }

    public void setAppLabel(String appLabel) {
        this.appLabel = appLabel;
    }

    public String getPkgPath() {
        return pkgPath;
    }

    public void setPkgPath(String pkgPath) {
        this.pkgPath = pkgPath;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public Drawable getIcon() {
        if (icon == null) {
            icon = loadIcon();
        }
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppData)) return false;
        AppData appData = (AppData) o;
        return Objects.equals(getAppLabel(), appData.getAppLabel())
                && Objects.equals(getPkgPath(), appData.getPkgPath())
                && Objects.equals(getPkgName(), appData.getPkgName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAppLabel(), getPkgPath(), getPkgName());
    }
}
