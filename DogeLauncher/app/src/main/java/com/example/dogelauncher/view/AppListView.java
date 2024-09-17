package com.example.dogelauncher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.example.dogelauncher.model.AppData;
import com.example.dogelauncher.viewModel.AppListViewModel;

import java.util.ArrayList;
import java.util.List;

public class AppListView extends ViewGroup {

    //屏幕分成：7 * 4
    public static final int GRID_ROW_NUM = 7;
    public static final int GRID_COLUMN_NUM = 4;

    private AppListViewModel appListViewModel;


    public AppListView(Context context) {
        super(context);
    }

    public AppListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    List<View> mCellViews;
    public void generateViews (AppListViewModel appListViewModel) {
        this.appListViewModel = appListViewModel;
        mCellViews = new ArrayList<>(appListViewModel.getData().size());
        List<AppData> data = appListViewModel.getData();
        /**
         * 根据data的属性设置摆放位置或者组
         * */

        for (int i = 0;i < data.size(); i ++) {
            mCellViews.add(new CellView(getContext(), data.get(i)));
            
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
