package com.example.dogelauncher.aty;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dogelauncher.R;


public class SplashAty extends AppCompatActivity {
    public static boolean DEBUG = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        hideActionStatusBar();
//        hideBottomStatusBar();
        setContentView(R.layout.activity_splash);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        //设置渐变效果
        setAlphaAnimation();
    }

    private void setAlphaAnimation() {
        ViewGroup splash = findViewById(R.id.splash_aty);
        Animation animation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.anim_alpha);
        splash.setAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (DEBUG) Toast.makeText(SplashAty.this, "Debugging mode", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent();
                ComponentName componentName = new ComponentName(getApplication().getPackageName(),
                        getApplication().getPackageName() + ".test.TestActivity");//.test.TestActivity   //.aty.MainActivity
                intent.setComponent(componentName);
                startActivity(intent);
                overridePendingTransition(R.anim.right_2_left, R.anim.left_2_right);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

//    //加载和注册广播 \ 读取数据
//    private void loadData () {
//        final ViewModelProvider provider = new ViewModelProvider(this);
//        mAppListViewModel = provider.get(AppListViewModel.class);
//        mAppDataAdapter = new AppDataAdapter2(mAppListViewModel);
//
//        //register
//        mIntentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
//        mIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
////        mIntentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
////        mIntentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
//        //来确保只接收应用的安装与卸载事件
//        mIntentFilter.addDataScheme("package");
//        mBroadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Log.d(TAG, "onReceive: refresh application list");
//                String action = intent.getAction();
//                if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
//                    String packageName = intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME);
//                    Log.d("PackageChangeReceiver", "Package added: " + packageName);
//                    if(!TextUtils.isEmpty(packageName)) {
////                        AppDataUtil.addData();
//                    }
//                } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
//                    String packageName = intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME);
//                    // 在某些情况下，packageName可能是null
//                    boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
//                    // 处理应用卸载事件
//                    Log.d("PackageChangeReceiver", "Package removed: " + (packageName != null ? packageName : "unknown") + ", replacing: " + replacing);
//                    if(!TextUtils.isEmpty(packageName)) {
////                        AppDataUtil.removeData();
//                    }
//                }
//                mAppListViewModel.loadPackageList();
//            }
//        };
//    }



    /**
     * 隐藏ActionBar和StatusBar
     */
    private void hideActionStatusBar() {
        //set no title bar 需要在setContentView之前调用
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //no status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //特殊情况下
        if (getSupportActionBar()!=null) getSupportActionBar().hide();
        if (getActionBar()!=null) getActionBar().hide();
    }

    /**
     * 隐藏 NavigationBar和StatusBar
     */
    protected void hideBottomStatusBar() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

}
