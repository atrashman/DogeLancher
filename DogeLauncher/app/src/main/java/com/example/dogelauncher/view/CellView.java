package com.example.dogelauncher.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.example.dogelauncher.model.AppData;

public class CellView extends ViewGroup {

    private int row;
    private int col;

    private boolean showBg;

    AppData appData;

    public CellView(Context context, AppData appData) {
        super(context);
        this.appData = appData;
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
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
