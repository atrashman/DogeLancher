package com.example.dogelauncher.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dogelauncher.R;
import com.example.dogelauncher.model.AppData;
import com.example.dogelauncher.viewModel.AppListViewModel;

import java.util.ArrayList;
import java.util.List;

public class SurroundingView extends ViewGroup {

    private static final String TAG = "SurroundingView";
    private int surroundingSize = 8;
    private List<AppData> mData;

    //ripple
    private boolean ripple = false;
    private int radius = 70; //中心圆半径
    private Paint spreadPaint; //扩散圆paint
    private float centerX;//圆心x
    private float centerY;//圆心y
    private int distance = 5; //每次圆递增间距
    private int maxRadius = 300; //最大圆半径
    private int delayMilliseconds = 22;//扩散延迟间隔，越大扩散越慢
    private int repeatCount = 1;
    private int rippleCircleNum = 5;

    private List<Integer> spreadRadius = new ArrayList<>(rippleCircleNum);//扩散圆层级数，元素为扩散的距离
    private List<Integer>  alphas = new ArrayList<>(rippleCircleNum);//对应每层圆的透明度

    private void init () {
        spreadPaint = new Paint();
        spreadPaint.setAntiAlias(true);
        spreadPaint.setAlpha(255);
        spreadPaint.setColor(R.color.purple_200);

        /*
         * ViewGroup在没有背景时不会走onDraw方法，但可以走dispatchDraw
         * 原因在于View对onDraw的控制时做了限定：[if (!dirtyOpaque) onDraw(canvas)]
         * 你可以使用onDraw，在之前设个透明色即可:setBackgroundColor(0x00000000);
         * */
        setBackgroundColor(0x00000000);

        //不然不触发onDraw
        setWillNotDraw(false);
    }

    public SurroundingView(Context context) {
        super(context);
    }

    public SurroundingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SurroundingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mIconViews == null || mIconViews.size() ==0)
            return;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            int childW = childView.getMeasuredWidth();
            int childH = childView.getMeasuredHeight();
            Log.e(TAG, "onLayout: childW "+childW + " childH "+ childH );
            int topPos = (int) (childH * i*0.5f);
            int leftPos = 0;
            childView.layout(leftPos, topPos, leftPos + childW, topPos + childH);
        }
    }


    public void showSurroundingApps(float x, float y, AppListViewModel appListViewModel) {
        List<AppData> data = appListViewModel.getData();
        mData = data.subList(0, Math.min(data.size(), surroundingSize));
        generateViews(mData);

        showRipple(x, y);
        requestLayout();
        invalidate();

    }
    List<View> mIconViews = new ArrayList<>();

    private void generateViews(List<AppData> mData) {
        for (AppData appData : mData) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.icon_view, this, false);
            ImageView iv = view.findViewById(R.id.icon);
            iv.setImageDrawable(appData.getIcon());
            mIconViews.add(view);
            addView(view);
        }
    }

    private void showRipple (float x, float y) {
        ripple = true;
        centerX = x;
        centerY = y;
        alphas.clear();
        spreadRadius.clear();
        alphas.add(255);
        for (int i =0;i<rippleCircleNum;i++){
            spreadRadius.add(0);
            alphas.add(255);
        }
        repeatCount = rippleCircleNum;
    }
    private void showSurrounding (float x, float y) {

    }


    //先resume 再 onMeasure  onSizeChanged onLayout onDraw
    //这里不走onDraw是为什么
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (ripple) {
            int spinCount = repeatCount;
            for (int i = 0; i < spinCount; i++) {
                if (i == 0) {
                    if ((spreadRadius.get(i) + distance) > maxRadius) {
                        repeatCount --;
                        if (repeatCount <= 0) {
                            ripple = false;
                            return;
                        }
                    }
                    spreadRadius.set(i, (spreadRadius.get(i) + distance) % maxRadius);
                } else {
                    // 确保后续波纹在前一个波纹到达一半半径时开始扩散
                    if (spreadRadius.get(i - 1) > maxRadius / 2 && spreadRadius.get(i) == 0) {
                        spreadRadius.set(i, distance);
                    } else if (spreadRadius.get(i) > 0) {
                        spreadRadius.set(i, (spreadRadius.get(i) + distance) % maxRadius);
                    }
                }
                int width = spreadRadius.get(i);

                int alpha = (int)((1 - (width / (float)maxRadius)) * 255);

                spreadPaint.setAlpha(alpha);
                Log.d("RippleView", "Current color: " + spreadPaint.getColor());
                canvas.drawCircle(centerX, centerY, radius + width, spreadPaint);
                //延迟更新，达到扩散视觉差效果
                postInvalidateDelayed(delayMilliseconds);
            }
        }


    }

}
