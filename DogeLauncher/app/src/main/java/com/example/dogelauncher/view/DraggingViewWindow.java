package com.example.dogelauncher.view;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class DraggingViewWindow extends FrameLayout {
    private  View view;
    private  float viewX, viewY, viewW, viewH;

    private static final String TAG = "DraggingViewWindow";

    public DraggingViewWindow(@NonNull Context context) {
        super(context);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (view != null)
                view.measure(MeasureSpec.makeMeasureSpec((int) viewW, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec((int) viewH, MeasureSpec.EXACTLY));

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (view != null)
            view.layout((int) viewX, (int) viewY, (int) (viewX + viewW), (int) (viewY + viewH));
//        for (int i = 0; i < getChildCount(); i++) {
//            View child = getChildAt(i);
//
//        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Toast.makeText(getContext(), "onAttached", Toast.LENGTH_SHORT).show();
    }

    public void setView(View v) {
        view = v;
        addView(v);
    }

    public void setPosition(float x, float y) {
        this.viewX = x;
        this.viewY = y;
        requestLayout();
    }

    public void setSize (int wid, int hei) {
        viewW = wid;
        viewH = hei;
    }

    public void clear () {
        view = null;
        removeAllViews();
    }

}
