package com.example.dogelauncher.test;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class TestView extends View {
    private static final String TAG = "TestView";
    public TestView(Context context) {
        super(context);
    }

    public TestView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG, "onTouchEvent: DOWN" );
//                break;
                return false;
            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "onTouchEvent: MOVE" );
                break;
//                return false;
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "onTouchEvent: up" );
                break;
        }
        return true;
    }
}
