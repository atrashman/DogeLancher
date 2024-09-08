package com.example.dogelauncher.test;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dogelauncher.R;
import com.example.dogelauncher.app.DogeApp;
import com.example.dogelauncher.view.DogeView;
import com.example.dogelauncher.view.MainView;
import com.example.dogelauncher.viewModel.AppListViewModel;

public class TestActivity extends AppCompatActivity {

    private DogeView dogeView;
    private AppListViewModel appListViewModel;
    private MainView mainView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //数据：
        appListViewModel = AppListViewModel.getInstance();


        mainView = findViewById(R.id.main_view);
        if (appListViewModel.isDataPrepared()) {
            mainView.generateViews(appListViewModel.getData().subList(0, 6));
        } else {
            DogeApp.getGlobalHandler().postDelayed(dataRunable, 2000);
        }
//        dogeView = (DogeView)findViewById(R.id.doge);
//        AppListViewModel appListViewModel = new AppListViewModel();
//        appListViewModel.loadPackageList();
//        dogeView.setViewModel(appListViewModel);

    }

    Runnable dataRunable = new Runnable() {
        @Override
        public void run() {
            if (appListViewModel.isDataPrepared()) {
                mainView.generateViews(appListViewModel.getData().subList(0, 6));
            } else {
                Toast.makeText(TestActivity.this, "working for data!", Toast.LENGTH_SHORT).show();
                DogeApp.getGlobalHandler().postDelayed(this, 2000);
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}