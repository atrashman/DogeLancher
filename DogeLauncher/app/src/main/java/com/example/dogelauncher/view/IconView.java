package com.example.dogelauncher.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.dogelauncher.R;

public class IconView extends View {

    private static final String TAG = "IconView";
    /*
    * onResume -> onAttachedToWindow ->  onMeasure -> onSizeChanged -> onLayout ->onDraw
    * */
    private Drawable drawable;


    private float mIconAlpha = 1;
    private String pkgName;


    public IconView(Context context, Drawable drawable) {
        super(context);
        this.drawable = drawable;
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDown(@NonNull MotionEvent e) {
                Log.e(TAG, "iconView onTouchEvent: " );
//                Toast.makeText(getContext(), "onTouchEvent", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    public void setIconAlpha(float iconAlpha) {
        this.mIconAlpha = iconAlpha;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
        invalidate(); // 请求重绘
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // 动画开始时可以设置 Drawable 为不透明
                drawable.setAlpha(255);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 动画结束后的一些逻辑
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(animation);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int selfWidth = MeasureSpec.getSize(widthMeasureSpec);
        int selfWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int selfHeight = MeasureSpec.getSize(heightMeasureSpec);
        int selfHeightMode = MeasureSpec.getMode(heightMeasureSpec);


        switch (selfWidthMode) {
            case MeasureSpec.EXACTLY:
                selfWidth = selfWidth;
                break;
            default:
                Toast.makeText(getContext(), "No mode", Toast.LENGTH_SHORT).show();

        }

        switch (selfHeightMode) {
            case MeasureSpec.EXACTLY:
                selfHeight = selfHeight;
                break;
            default:
                Toast.makeText(getContext(), "No mode", Toast.LENGTH_SHORT).show();
        }
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(selfWidth,selfWidthMode),MeasureSpec.makeMeasureSpec(selfHeight,selfHeightMode));
        drawable.setBounds(0,0,selfWidth,selfHeight);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawable.setAlpha( (int)(mIconAlpha * 255));
        drawable.draw(canvas);
    }


    GestureDetector gestureDetector;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                Log.e(TAG, "onTouchEvent: DOWN" );
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.e(TAG, "onTouchEvent: MOVE" );
                return false;
            case MotionEvent.ACTION_UP:
//                Log.e(TAG, "onTouchEvent: up" );
                break;
        }
        return true;
    }
}
