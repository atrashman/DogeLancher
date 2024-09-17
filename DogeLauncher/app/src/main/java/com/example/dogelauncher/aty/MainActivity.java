package com.example.dogelauncher.aty;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.dogelauncher.R;
import com.example.dogelauncher.fragment.MainFragment;
import com.example.dogelauncher.viewModel.AppListViewModel;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private MainFragment mainFragment;

    GestureDetector gestureDetector;

    //view model
    private AppListViewModel appListViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        mainFragment = new MainFragment();
        fragmentManager.beginTransaction().add(R.id.frag_content, mainFragment).commit();

        final ViewModelProvider provider = new ViewModelProvider(this);
        appListViewModel = provider.get(AppListViewModel.class);

        //手势
        gestureDetector = new GestureDetector(getBaseContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                switch (appListViewModel.getMode()) {
                    case MainFragment.MODE_SURROUNDING:
                        appListViewModel.setMode(AppListViewModel.MODE_LISTING);
                        return true;
                }
                return false;
            }
        });

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
}