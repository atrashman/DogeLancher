package com.example.dogelauncher.view;

import static android.view.View.MeasureSpec.EXACTLY;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dogelauncher.R;
import com.example.dogelauncher.model.AppData;
import com.example.dogelauncher.utils.CalculateUtil;

import java.util.ArrayList;
import java.util.List;

public class MainView extends ViewGroup {

    private static final String TAG = "MainView";
    private int surroundingSize = 6;
    private List<AppData> mData;

    //动画
    private static final int DELAY_MILLISECONDS_SHORT = 16;// 短间隔时间刷新
    private static final int DELAY_MILLISECONDS_LONG = DELAY_MILLISECONDS_SHORT * 3;// 长间隔时间刷新
    private static final long DRAWING_TIME_SHORT = 500;
    private static final long DRAWING_TIME_LONG = DRAWING_TIME_SHORT * 2;




    private float curDrawingTime = DRAWING_TIME_SHORT;

    //ripple
    private boolean ripple = false;
    private int radius = 70; //中心圆半径
    private Paint spreadPaint; //扩散圆paint
    private float centerX;//圆心x
    private float centerY;//圆心y
    private int rippleCircleNum = 2;//圈数
    private float distance;//每次圆递增间距
    public static float maxRadius = 300; //最大圆半径


    private List<Float> spreadRadius = new ArrayList<>(rippleCircleNum);//扩散圆层级数，元素为扩散的距离
    private List<Float> alphas = new ArrayList<>(rippleCircleNum);//对应每层圆的透明度


    //iconView
    public static final int ICON_MAX_SIZE = 288;//px
    public static final int ICON_MIN_SIZE = 144;//可以用最小size来限制app个数
    public static final int ICON_MIN_MARGIN = 15;
    private boolean showApps = false;
    //touch


    //Invalidate mode
    private int invalidateMode = 2;
    private static final int INVALIDATE_MODE_POST_DELAY_SHORT = 2;
    private static final int INVALIDATE_MODE_POST_DELAY_LONG = 1;
    private static final int INVALIDATE_MODE_POST_ONCE = 0;


    //view模式
    private static final int DISPLAY_MODE_SURROUNDING = 0;
    private static final int DISPLAY_MODE_LIST = 1;
    private static final int DISPLAY_MODE_EDIT = 2;
    private static int displayMode = DISPLAY_MODE_SURROUNDING;
    private View touchingView;


    private void init() {
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

        //点击事件处理
        setOnTouchListener(new DogeViewTouchListener());

    }

    //构造
    public MainView(Context context) {
        super(context);
    }

    public MainView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MainView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    //点击事件处理
    private GestureDetector gestureDetector = new GestureDetector(getContext(), new FlingListener());
    private class FlingListener extends GestureDetector.SimpleOnGestureListener {
        private final int minimumVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();

        private FlingListener() {

        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            switch (displayMode) {
                case DISPLAY_MODE_LIST:
                    if (Math.abs(distanceX) > Math.abs(distanceY)) {
                        Toast.makeText(getContext(), "dragging X", Toast.LENGTH_SHORT).show();
                    }
            }
            return false;
        }

        //18898597241
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            if (velocityY > velocityX &&
                    velocityY >= minimumVelocity &&
                    e1 != null && e2 != null &&
                    e2.getY() - e1.getY() > 0) {
                return true;
            }
            return false;

        }
    }

    // onTouch作为onTouchEvent的替代
    private class DogeViewTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    //down事件信息传入detector
                    gestureDetector.onTouchEvent(event);
                    switch (displayMode) {
                        case DISPLAY_MODE_SURROUNDING:
                            if (showApps) {
                                //判断是否是点击到了子view
                                Toast.makeText(getContext(), "showingApps", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "main onTouch: " );//后于iconview的ontouchevent
                                touchingView = getTouchingView(event.getX(), event.getY());
                            } else {
                                shouldDraw = true;
                                showRipple(event.getX(),event.getY());
                                invalidate();
//                                showSurroundingApps(event.getX(), event.getY());
                            }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    switch (displayMode) {
                        case DISPLAY_MODE_SURROUNDING:
                            if (touchingView != null) {
                                if (isOnTouchingView(event.getX(), event.getY())) {
                                    Log.e(TAG, "onTouch: isOnTouchingView" );
                                }
                                touchingView = null;
                            } else {
                                showApps();
                                invalidate();
                            }
                            break;
                    }



                    break;
            }
            return true;
        }

    }


    //测量摆放绘制：
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //测量自己
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        Log.e(TAG, "onMeasure: " );
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

        int size = Math.min(selfWidth, selfHeight);
        //这里只是为了保险， 实际上基本上取不到selfWidth selfHeight任意一个 除非整个vg真的很小
        size = Math.min(CalculateUtil.calculateAppSize(childCount, maxRadius), size);

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.measure(MeasureSpec.makeMeasureSpec(size, EXACTLY), MeasureSpec.makeMeasureSpec(size, EXACTLY));
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.e(TAG, "onLayout: " );
        if (!showApps)
            return;

        int childCount = getChildCount();

        float perAngle = 360.f / childCount;

        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            int childW = childView.getMeasuredWidth();
            int childH = childView.getMeasuredHeight();
            double angleInRadians = Math.toRadians(90 + i * perAngle);
            int posX = (int) (centerX + maxRadius * Math.cos(angleInRadians));
            int posY = (int) (centerY + maxRadius * Math.sin(angleInRadians));
            childView.layout(posX - childW / 2, posY - childH / 2, posX + childW / 2, posY + childH / 2);
        }
    }


    //先resume 再 onMeasure  onSizeChanged onLayout onDraw
    private float progress = 1f; // 用于跟踪绘制进度
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        Log.e(TAG, "onDraw: progress = "+ progress );
        if (ripple) drawRipple(canvas);

        if(showApps) setIconAlpha();

        //统一选择更新模式和刷新更新进度,
        switch (invalidateMode) {
            case INVALIDATE_MODE_POST_DELAY_SHORT:
                progress += DELAY_MILLISECONDS_SHORT / curDrawingTime;

                if (progress >= 1) clearAllDrawingFlag();
                if (shouldDraw) {
                    postInvalidateDelayed(DELAY_MILLISECONDS_SHORT);
                }
                break;
            case INVALIDATE_MODE_POST_DELAY_LONG:
                progress += DELAY_MILLISECONDS_LONG / curDrawingTime;
                if (progress >= 1) clearAllDrawingFlag();
                if (shouldDraw) postInvalidateDelayed(DELAY_MILLISECONDS_LONG);
                break;
            case INVALIDATE_MODE_POST_ONCE:
                return;
            default:
                break;
        }

    }


    public void showSurroundingApps(float x, float y) {
        Log.e(TAG, "showSurroundingApps: ");
//        clearAllDrawingFlag();
        showApps = true;
        showApps();
        showRipple(x, y);
        requestLayout();
        invalidate();
    }

    List<View> mIconViews = new ArrayList<>();

    public void generateViews(List<AppData> mData) {
        removeAllViews();
        for (AppData appData : mData) {
            IconView iconView = new IconView(getContext(), appData.getIcon());
            iconView.setPkgName(appData.getPkgName());
            mIconViews.add(iconView);
        }
    }
    private void showApps() {
        showApps = true;
        for (View iconView : mIconViews) {
            addView(iconView);
        }
    }
    private void showRipple(float x, float y) {
        progress = 0;
        ripple = true;
        centerX = x;
        centerY = y;
        alphas.clear();
        spreadRadius.clear();
        alphas.add(255F);
        for (int i = 0; i < rippleCircleNum; i++) {
            spreadRadius.add(0F);
            alphas.add(255F);
        }
        distance= maxRadius / DRAWING_TIME_LONG * DELAY_MILLISECONDS_SHORT * rippleCircleNum;
    }

    private boolean shouldDraw = false;
    private void clearAllDrawingFlag() {
        Log.e(TAG, "clearAllDrawingFlag: " );
        progress = 0;
//        showApps = false;
        ripple = false;
        shouldDraw = false;
//        if (getChildCount() > 0) {
//            Log.e(TAG, "clearAllDrawingFlag: getChildCount()" + getChildCount() );
//            removeAllViewsInLayout();
//            getHandler().removeMessages(1);
//            postInvalidateDelayed(DELAY_MILLISECONDS_SHORT);
//        }

    }

    //水花
    private float perCircleProgress = 1f/(rippleCircleNum + 1);//每个圆需要消耗的progress
    private void drawRipple(Canvas canvas) {
        for (int i = 0; i < rippleCircleNum; i++) {
            if (spreadRadius.get(i) >= maxRadius) continue;
            float startPoint = perCircleProgress * i;
            float endPoint = startPoint + perCircleProgress * 2;
//            Log.e(TAG, "drawRipple: i = "+ i + " startpoint = "+startPoint);

            if (startPoint < progress && progress <= endPoint) {
                float curProgress = (progress - startPoint) /(perCircleProgress * 2);
                spreadRadius.set(i, maxRadius * curProgress);
                alphas.set(i, 255 * (1-curProgress));
            }

            float width = spreadRadius.get(i);
            float alpha = alphas.get(i);
//            Log.e(TAG, "drawRipple: width = "+ width );
//            Log.e(TAG, "drawRipple: alpha = "+ alpha );
            spreadPaint.setAlpha((int) alpha);
            canvas.drawCircle(centerX, centerY, width, spreadPaint);
            //延迟更新
            invalidateMode = Math.max(INVALIDATE_MODE_POST_DELAY_SHORT, invalidateMode);
        }
    }
    private void setIconAlpha () {
        float alpha;
        alpha = progress * rippleCircleNum;
//        if (progress > 0.5) {
//            alpha = (1 - progress) * rippleCircleNum;
//        } else {
//            //第一圈水波就显示完成图片
//            alpha = progress * rippleCircleNum;
//        }
        Log.e(TAG, "setIconAlpha: alpha " + alpha );
        for (int i = 0; i < getChildCount(); i++) {
            ((IconView) getChildAt(i)).setIconAlpha(alpha > 1 ? 1 : alpha);
        }
        //延迟更新
        invalidateMode = Math.max(INVALIDATE_MODE_POST_DELAY_SHORT, invalidateMode);
    }

    private View getTouchingView (float x, float y) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            // 判断触摸点是否在当前子 View 的范围内
            if (x >= child.getLeft() && x <= child.getRight() &&
                    y >= child.getTop() && y <= child.getBottom()) {
                // 触摸点在子 View 内
                return child;
            }
        }
        return null;
    }

    private boolean isOnTouchingView (float x, float y) {
        if (x >= touchingView.getLeft() && x <= touchingView.getRight() &&
                y >= touchingView.getTop() && y <= touchingView.getBottom()) {
            // 触摸点在子 View 内
            return true;
        }
        return false;
    }

}
