package com.example.dogelauncher.view;

import static android.view.View.MeasureSpec.EXACTLY;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dogelauncher.R;
import com.example.dogelauncher.model.AppData;
import com.example.dogelauncher.utils.CalculateUtil;

import java.util.ArrayList;
import java.util.List;

public class SurroundingView extends ViewGroup {

    private static final String TAG = "SurroundingView";
    private int surroundingSize = 6;
    private List<AppData> mData;

    //ripple
    private boolean ripple = false;
    private int radius = 70; //中心圆半径
    private Paint spreadPaint; //扩散圆paint
    private float centerX;//圆心x
    private float centerY;//圆心y
    private int distance = 5; //每次圆递增间距
    public static int maxRadius = 300; //最大圆半径
    private int delayMilliseconds = 22;//扩散延迟间隔，越大扩散越慢
    private int repeatCount = 1;
    private int rippleCircleNum = 5;

    private List<Integer> spreadRadius = new ArrayList<>(rippleCircleNum);//扩散圆层级数，元素为扩散的距离
    private List<Integer>  alphas = new ArrayList<>(rippleCircleNum);//对应每层圆的透明度


    //iconView
    public static final int ICON_MAX_SIZE = 288;//px
    public static final int ICON_MIN_SIZE = 144;//可以用最小size来限制app个数
    public static final int ICON_MIN_MARGIN = 15;
    private boolean showApps = false;

    //touch
    private boolean isTouching;


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
        //测量自己
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!showApps) return;
//        measureChildren(widthMeasureSpec, heightMeasureSpec);
        /*
        * 这里因为view都是直接添加进来的，外面的人没办法设置子view的属性
        * 所以这里我们就基于圆环拿到基本测量即可
        * 我们的测量思路如下：
        * 我们在一个基于centerX centerY的圆上测量子view
        * 1 圆大小我们固定起来
        * 2 保证APP Icon均匀分布
        * 3 icon绘制大小跟app数量有关
        * */
        //这里逻辑是给子类测量限制
        int childCount = getChildCount();
        if (childCount == 0) {
            return;
        }

        int selfWidth = MeasureSpec.getSize(widthMeasureSpec);

        int selfHeight = MeasureSpec.getSize(heightMeasureSpec);

        int size = Math.min(selfWidth,selfHeight);
        //这里只是为了保险， 实际上基本上取不到selfWidth selfHeight任意一个 除非整个vg真的很小
        size =  Math.min(CalculateUtil.calculateAppSize(childCount, maxRadius), size);

        for (int i =0 ;i < childCount;i++){
            View child = getChildAt(i);
            child.measure(MeasureSpec.makeMeasureSpec(size, EXACTLY), MeasureSpec.makeMeasureSpec(size, EXACTLY));
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mIconViews == null || !showApps )
            return;
        int childCount = getChildCount();

        float perAngle = 360.f / childCount;

        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            int childW = childView.getMeasuredWidth();
            int childH = childView.getMeasuredHeight();
            double angleInRadians = Math.toRadians(90 + i * perAngle);
            int posX = (int)(centerX + maxRadius  * Math.cos(angleInRadians));
            int posY = (int) (centerY + maxRadius  *  Math.sin(angleInRadians));
            childView.layout(posX - childW/2 , posY - childH/2, posX + childW/2, posY + childH/2);
        }
    }


    public void showSurroundingApps(float x, float y) {
        //List<AppData> data = appListViewModel.getData();
        //mData = data.subList(0, Math.min(data.size(), surroundingSize));
        //generateViews(mData);
        showApps = true;
        showRipple(x, y);
        requestLayout();
        invalidate();

    }
    List<View> mIconViews = new ArrayList<>();

    public void generateViews(List<AppData> mData) {
        removeAllViews();
        for (AppData appData : mData) {
            IconView iconView = new IconView(getContext(),appData.getIcon());
            mIconViews.add(iconView);
            addView(iconView);
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
