package com.example.dogelauncher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.example.dogelauncher.model.AppData;
import com.example.dogelauncher.viewModel.AppListViewModel;

import java.util.ArrayList;
import java.util.List;

public class AppListView extends CellView implements OnNoSelectedViewScrollListener {

    //屏幕分成：7 * 4
    public static final int GRID_ROW_NUM = 6;
    public static final int GRID_COLUMN_NUM = 4;

    public static final int MARGIN_INVOKE_SCROLL = 10;

    private AppListViewModel appListViewModel;
    private boolean[][] pos;

    public AppListView(Context context) {
        super(context);
    }

    public AppListView(Context context, AttributeSet attrs) {
        super(context,attrs);
        row = GRID_ROW_NUM;
        col = GRID_COLUMN_NUM;
        init();
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
            CellView cellView = new CellView(getContext(), data.get(i));
            mCellViews.add(cellView);
            addView(mCellViews.get(i));
            cellView.setOnCellViewScrollListener(this);
            setOnNoSelectedViewScrollListener(this);
        }
        createPosInfo ();
        requestLayout();
    }

    @Override
    public void onCellViewScroll(int x) {
        MyViewPager parent = (MyViewPager)getParent();
        int measuredWidth = getMeasuredWidth();
        if (x + MARGIN_INVOKE_SCROLL > measuredWidth) {
            parent.scrollToNextPager(true);
        } else if (x - MARGIN_INVOKE_SCROLL < 0){
            parent.scrollToNextPager(false);
        }
    }

    @Override
    public void onNoSelectedViewScroll(float distanceX) {
        MyViewPager parent = (MyViewPager)getParent();
        parent.scroll
    }
}
