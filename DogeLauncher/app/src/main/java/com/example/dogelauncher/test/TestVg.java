package com.example.dogelauncher.test;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class TestVg extends LinearLayout {
    private static final String TAG = "TestVg";
    public TestVg(Context context) {
        super(context);
    }

    public TestVg(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG, "onInterceptTouchEvent: DOWN" );
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "onInterceptTouchEvent: MOVE" );
//                return true;
                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "onInterceptTouchEvent: up" );
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG, "onTouchEvent: DOWN" );
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "onTouchEvent: MOVE" );
                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "onTouchEvent: up" );
                break;
        }
        return true;
    }
}
