package com.example.dogelauncher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.example.dogelauncher.model.AppData;
import com.example.dogelauncher.viewModel.AppListViewModel;

import java.util.ArrayList;
import java.util.List;

public class AppListView extends CellView {

    //屏幕分成：7 * 4
    public static final int GRID_ROW_NUM = 6;
    public static final int GRID_COLUMN_NUM = 4;

    public static final int MARGIN_INVOKE_SCROLL = 10;


    public AppListView(Context context) {
        super(context);
    }

    public AppListView(Context context, List<AppData> appDatas) {
        super(context, appDatas);
        row = GRID_ROW_NUM;
        col = GRID_COLUMN_NUM;
        iconTag = ICON_TAG_LIST;
        setOnCellViewScrollListener(getParent() instanceof MyViewPager ? (MyViewPager)getParent() : null);
    }


    public void generateViews () {
        /**
         * 根据data的属性设置摆放位置或者组
         * */
        mCellViews = new ArrayList<>(appDatas.size());
        for (int i = 0;i < appDatas.size(); i ++) {
            CellView cellView = new CellView(getContext(), appDatas.get(i));
            mCellViews.add(cellView);
            addView(mCellViews.get(i));
        }
        setOnCellViewScrollListener(getParent() instanceof MyViewPager ? (MyViewPager)getParent() : null);
        setOnNoSelectedViewScrollListener(getParent() instanceof MyViewPager ? (MyViewPager)getParent() : null);
        requestLayout();
    }
}
