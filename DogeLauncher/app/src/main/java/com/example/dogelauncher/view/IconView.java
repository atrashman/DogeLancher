package com.example.dogelauncher.view;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.dogelauncher.R;

public class IconView extends View {

    private Drawable drawable;

    public IconView(Context context, Drawable drawable) {
        super(context);
        this.drawable = drawable;
        drawable.setAlpha(0);
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
        drawable.setAlpha(0);
        invalidate(); // 请求重绘
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        setAnimation(animation);
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
        drawable.draw(canvas);
    }
}
