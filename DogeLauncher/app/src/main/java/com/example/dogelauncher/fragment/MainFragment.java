package com.example.dogelauncher.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.dogelauncher.app.DogeApp;
import com.example.dogelauncher.databinding.FragmentMainBinding;
import com.example.dogelauncher.model.AppData;
import com.example.dogelauncher.view.AppListView;
import com.example.dogelauncher.view.CellView;
import com.example.dogelauncher.view.MainView;
import com.example.dogelauncher.view.MyViewPager;
import com.example.dogelauncher.viewModel.AppListViewModel;

import java.util.List;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    public static final int MODE_SURROUNDING = 0;
    public static final int MODE_LISTING = 1;
    public static final int MODE_EDIT = 2;

    //view model
    private AppListViewModel appListViewModel;

    //view
    private MainView mainView;
    private AppListView appListView;
    private com.example.dogelauncher.databinding.FragmentMainBinding mainBinding;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ViewModelProvider provider = new ViewModelProvider(requireActivity());
        appListViewModel = provider.get(AppListViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mainBinding = FragmentMainBinding.inflate(inflater, container, false);
        mainBinding.setLifecycleOwner(getViewLifecycleOwner());
        mainBinding.setMainViewModel(appListViewModel);

        View view = mainBinding.getRoot();
        Log.e(TAG, "onCreateView: app_view_pages " + mainBinding.appViewPages );

        if (appListViewModel.isDataPrepared()) {
            mainBinding.mainView.generateViews(appListViewModel.getData().subList(0, 6));
            generatePages();
            setObserver();
            appListViewModel.setMode(MODE_LISTING);
        } else {
            DogeApp.getGlobalHandler().postDelayed(loadData, 2000);
        }

        return view;
    }

    private void setObserver() {
        appListViewModel.mode.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
//                if(mainView == null || appListView == null) DogeApp.getGlobalHandler().postDelayed(loadData, 2000);
                Log.e(TAG, "onChanged: integer = " + integer );
                switch (integer) {
                    case MODE_SURROUNDING:
                        mainBinding.mainView.setVisibility(View.VISIBLE);
                        mainBinding.appViewPages.setVisibility(View.GONE);
                        break;
                    case MODE_LISTING:
                        mainBinding.mainView.setVisibility(View.GONE);
                        mainBinding.appViewPages.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }


    private Runnable loadData = new Runnable() {
        @Override
        public void run() {
            if (appListViewModel.isDataPrepared()) {
                mainBinding.mainView.generateViews(appListViewModel.getData().subList(0, 6));
//                mainBinding.appListView.generateViews(appListViewModel);
                generatePages();
                setObserver();
                appListViewModel.setMode(MODE_LISTING);
            } else {
                Toast.makeText(getActivity(), "working for data!", Toast.LENGTH_SHORT).show();
                DogeApp.getGlobalHandler().postDelayed(this, 2000);
            }
        }
    };


    private void generatePages () {
        List<AppData> data = appListViewModel.getData();
        int row = 4;
        int col = 6;
        int i = 0;
        int curLast = Math.min(row * col, data.size());
        while (true) {
            for (;i < curLast; i += row * col) {
                AppListView appList = new AppListView(getContext(), data.subList(i, curLast));
                mainBinding.appViewPages.addView(appList);
                appList.generateViews();
                appList.setOnCellViewScrollListener(mainBinding.appViewPages);
            }
            if (curLast >= data.size()) break;
            curLast = Math.min(curLast + row * col, data.size());
        }
    }

}
