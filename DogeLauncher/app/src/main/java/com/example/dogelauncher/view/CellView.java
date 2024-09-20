package com.example.dogelauncher.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.example.dogelauncher.model.AppData;

import java.util.List;

public class CellView extends ViewGroup {

    private int row;
    private int col;

    private static int SIZE_PER_OUTER_GRID = 186; // 160为每个内部格子
    private static int SIZE_PER_INNER_GRID = 186;

    public static final int ICON_MARGIN = 10;

    private boolean showBg;

    public static final int ICON_TAG_SINGLE = 0;
    public static final int ICON_TAG_GROUP = 1;
    public int iconTag = ICON_TAG_SINGLE;



    private AppData appData;
    private List<AppData> appDatas;



    public CellView(Context context, AppData appData) {
        super(context);
        iconTag = ICON_TAG_SINGLE;
        this.appData = appData;

        init();
    }

    public CellView(Context context, List<AppData> appDatas) {
        super(context);
        iconTag = ICON_TAG_GROUP;
        this.appDatas = appDatas;


        init();
    }


    public CellView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    private void init() {
        // 设置背景，带透明度和圆角
        showBg();
    }

    private void  showBg () {
        if (showBg) setBackground(createRoundedBackground());
    }

    // 创建一个带圆角和透明度的灰色背景
    private Drawable createRoundedBackground() {
        float radius = 50.0f; // 圆角半径
        int backgroundColor = 0x80CCCCCC; // 半透明灰色

        // 创建一个 ShapeDrawable 来实现圆角背景
        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        backgroundDrawable.setColor(backgroundColor);  // 设置背景颜色
        backgroundDrawable.setCornerRadius(radius);    // 设置圆角半径

        return backgroundDrawable;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        switch (iconTag) {
            case ICON_TAG_SINGLE:
                View child = getChildAt(0);
                if (child != null) {
                    int measuredWidth = child.getMeasuredWidth();
                    int measuredHeight = child.getMeasuredHeight();

                    child.layout(l, t, l+measuredWidth, t+measuredHeight);
                }
                break;
            case ICON_TAG_GROUP:
                int childCount = getChildCount();
                for (int i = 0; i < 4 && i < childCount; i++) {

                }

        }
    }

}
