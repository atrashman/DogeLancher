package com.example.dogelauncher.view;

import static android.view.View.MeasureSpec.EXACTLY;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.PopupMenu;
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
    private boolean hideApps = false;
    private boolean appsHalting = false;
    private boolean showAppMenu = false;
    private PopupMenu popupMenu;

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
    private int durationMillis = 300;


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
        emptyView = new View(getContext());
    }

    private void transferDisplayMode (int oldMode, int newMode) {
        /*
        * surrounding状态 :   showApps hideApps ripple
        * edit状态        :   全是画的 没有layout
        * */
        if (oldMode == DISPLAY_MODE_SURROUNDING && newMode == DISPLAY_MODE_EDIT) {
            showApps = false;
            hideApps = false;
            ripple = false;

            displayMode = DISPLAY_MODE_EDIT;

        }

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
        public boolean onSingleTapUp(@NonNull MotionEvent e) {

            switch (displayMode) {
                case DISPLAY_MODE_SURROUNDING:
                    if (touchingView != null) {
                        Log.e(TAG, "onSingleTapUp: no touchingView" );
                        //touchingView = null;
                    } else {
                        Log.e(TAG, "onSingleTapUp: show apps" );
                        showApps();
                    }
                    break;
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            switch (displayMode) {
                case DISPLAY_MODE_LIST:
                    if (Math.abs(distanceX) > Math.abs(distanceY)) {
                        Toast.makeText(getContext(), "dragging X", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DISPLAY_MODE_SURROUNDING:
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


    /**
     * 这里使用拦截是因为传递机制问题，具体可以看 【事件分发event】这个笔记
     * 需要达到子View自己处理完之后把事件重新传上来的效果 好像没办法做到（因为onInterceptTouchEvent好像不能执行手势控制器方法）
     * 这里用的搞一个不太常规的操作
     * 利用子view的只在move事件返回false时 不会走父view的onTouchEvent
     *
     * */
    private long lastDownTime = 0;
    private float lastDownX = 0;
    private float lastDownY = 0;
    private int curItemId;
    private int posId = -1;
    private boolean isMoving = false;
    private float lastX = 0;
    private float lastY = 0;


    //对icon的操作都写在这里
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        //发现在onInterceptTouchEvent 做不了动作，识别不了动作
//        gestureDetector.onTouchEvent(event);
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //记录
                lastDownTime = event.getEventTime();
                lastDownX = event.getX();
                lastDownY = event.getY();
                lastX = lastDownX;
                lastY = lastDownY;
                touchingView = null;
                switch (displayMode) {
                    case DISPLAY_MODE_SURROUNDING:
                        if (showApps || hideApps) {
                            //app还在显示中
                            //判断是否是点击到了子view
//                            Toast.makeText(getContext(), "showingApps", Toast.LENGTH_SHORT).show();
                            curItemId = getTouchingView(event.getX(), event.getY());
                            if (curItemId == -1 ) break;
                            touchingView = mIconViews.get(curItemId);
                            mIconViews.set(curItemId, emptyView);
                            //点击了某个app， 需要锁定展示全部app
                            clearhideAppsFlag();
                            clearShowAppsFlag();
                            appsHalting = true;
                            invalidate();

                        } else {
                            showRipple(event.getX(),event.getY());
                            invalidate();
                        }
                }
                return false;
            case MotionEvent.ACTION_MOVE:
                if (appsHalting  && touchingView != null) {

                    if (!isMoving(event) && !showAppMenu && event.getEventTime() - lastDownTime > 500) {
                        Log.e(TAG, "onInterceptTouchEvent: show menu");
                        showAppMenu = true;

                        //展示菜单
                        popupMenu = new PopupMenu(getContext(), touchingView);
                        popupMenu.getMenuInflater().inflate(R.menu.icon_menu, popupMenu.getMenu());
                        popupMenu.show();
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                // 控件每一个item的点击事件
                                int itemId = item.getItemId();
                                if (itemId == R.id.edit) {
                                    showAppMenu = false;
                                    // 处理状态转换
                                    //transferDisplayMode(displayMode, DISPLAY_MODE_EDIT);
                                    //开始绘制编辑模式
//                                    invalidate();
                                } else if (itemId == R.id.delete) {
                                    Toast.makeText(getContext(), "deleted", Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            }
                        });
                        popupMenu.setOnDismissListener(menu -> {
                            // 控件消失时的事件
                            appsHalting = false;
                            showApps = false;
                            showAppMenu = false;
//                            hideApps = true;
                            invalidate();
                        });
                    } else if (isMoving(event)) {
//                        Log.e(TAG, "onInterceptTouchEvent: isMoving");
                        if (popupMenu != null) popupMenu.dismiss();
                        float nowX = event.getX();
                        float nowY = event.getY();
                        int dx = (int) (nowX - lastX);
                        int dy = (int) (nowY - lastY);

                        int left = touchingView.getLeft() + dx;
                        int top = touchingView.getTop() + dy;
                        int right = touchingView.getRight() + dx;
                        int bottom = touchingView.getBottom() + dy;

                        touchingView.layout(left, top, right, bottom);

                        lastX = nowX;
                        lastY = nowY;
                        //判断位置 不能用list的位置 要用item位置
                        int halfAppSize = CalculateUtil.calculateAppSize(mIconViews.size(), maxRadius) / 2;
                        posId = getTouchingPos(nowX, nowY, halfAppSize, curItemId);

                        if (posId == -1 || mIconViews.get(posId) == emptyView) return false;
                        Log.e(TAG, "onInterceptTouchEvent: curItemId = "+ curItemId + "  posId = "+ posId );
                        //这里确保了curItemId 和 posId 即是位置id 也是mIconViewId
//                        if (!movingPos[posId])
                            animateInClockwise(curItemId, posId);


                    }
                }

                return false;
            case MotionEvent.ACTION_UP:
                //up和animation end 是不确定哪个先执行的！！！！ 所以不能touchingView = null;
                if (touchingView != null) {
                    Log.e(TAG, "onInterceptTouchEvent: touchingView !=null posId = "+ posId);
                    if (posId != -1) mIconViews.set(posId, touchingView);
                    curItemId = posId;
                    requestLayout();
//                    if (movingAnimationEnd) {
////                        touchingView = null;
//                    } else {
//
//                    }
                }

                isMoving = false;
                if (!showAppMenu) {
                    hideApps = true;
                    appsHalting = false;
                    invalidate();
                }

                break;
        }
        return true;

    }


    private void layoutChild (View view, int pos) {
        float perAngle = 360f / mIconViews.size();
        double angleInRadians = Math.toRadians(90 + pos * perAngle);
        int posX = (int) (centerX + maxRadius * Math.cos(angleInRadians));
        int posY = (int) (centerY + maxRadius * Math.sin(angleInRadians));
        int childW = view.getMeasuredWidth();
        int childH = view.getMeasuredHeight();
        view.layout(posX - childW / 2, posY - childH / 2, posX + childW / 2, posY + childH / 2);
    }

    private boolean movingAnimationEnd = true;

    private void animateInClockwise(int startPos, int toPos) {
        boolean clockwise;
        float perAngle = 360f / mIconViews.size();
        double angleInRadians = Math.toRadians(90 + startPos * perAngle);
        int originX = (int) (centerX + maxRadius * Math.cos(angleInRadians));
        int originY = (int) (centerY + maxRadius * Math.sin(angleInRadians));

        int lastPosX = originX;
        int lastPosY = originY;
        if (startPos < toPos) {
            clockwise = toPos - startPos <= mIconViews.size()/2;

            for (int i = 1; i <= (clockwise ? toPos - startPos : mIconViews.size() - (toPos - startPos) ); i ++) {
                int curPos;
                if (clockwise) {
                    curPos = startPos + i;
                } else {
                    curPos = startPos - i ;
                    curPos = curPos >= 0 ? curPos : curPos + mIconViews.size();
                }
                Log.e(TAG, "animateInClockwise: curPos " + curPos );
                angleInRadians = Math.toRadians(90 + curPos * perAngle);
                int curPosX = (int) (centerX + maxRadius * Math.cos(angleInRadians));
                int curPosY = (int) (centerY + maxRadius * Math.sin(angleInRadians));

                // 计算位移
                float deltaX = lastPosX - curPosX;
                float deltaY = lastPosY - curPosY;

                // 获取当前的 View
                View curView = mIconViews.get(curPos);
                float startX = curView.getTranslationX();
                float startY = curView.getTranslationY();
                // 创建 ObjectAnimator 对象，使用 translationX 和 translationY 来平滑移动
                ObjectAnimator animX = ObjectAnimator.ofFloat(curView, "translationX", startX, startX + deltaX);
                ObjectAnimator animY = ObjectAnimator.ofFloat(curView, "translationY", startY, startY + deltaY);

                // 设置动画时长
                animX.setDuration(durationMillis);
                animY.setDuration(durationMillis);

                // 创建 AnimatorSet 以便同时执行 X 和 Y 方向的动画
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(animX, animY);

                // 监听动画事件
                int finalCurPos = curPos;
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        // 将当前 View 设置为空视图，表示正在移动
                        mIconViews.set(finalCurPos, emptyView);
                        movingAnimationEnd = false;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // 计算新的位置
                        int pos = clockwise ? finalCurPos - 1 : (finalCurPos + 1) % mIconViews.size();

                        // 将移动后的 View 重新放回列表
                        mIconViews.set(pos, curView);

                        // 如果到达目标位置，更新目标 View 和状态
                        if (toPos == finalCurPos) {
                            mIconViews.set(toPos, touchingView);
                            curItemId = toPos;
                            movingAnimationEnd = true;
                        }
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        // 动画取消时的处理（如有需要）
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        // 动画重复时的处理（如有需要）
                    }
                });

// 开始动画
                animatorSet.start();

                lastPosX = curPosX;
                lastPosY = curPosY;
            }
        }
        else {
            clockwise = startPos - toPos <= mIconViews.size()/2;
            for (int i = 1; i <= (clockwise ? startPos - toPos : mIconViews.size() - (startPos - toPos)); i ++) {
                int curPos;
                if (clockwise) {
                    curPos = startPos - i;
                } else {
                    Log.e(TAG, "animateInClockwise: not clockwise" );
                    curPos = startPos + i ;
                    curPos = curPos < mIconViews.size() ? curPos : curPos % mIconViews.size();
                }
                angleInRadians = Math.toRadians(90 + curPos * perAngle);
                int curPosX = (int) (centerX + maxRadius * Math.cos(angleInRadians));
                int curPosY = (int) (centerY + maxRadius * Math.sin(angleInRadians));
//
                float deltaX = lastPosX - curPosX;
                float deltaY = lastPosY - curPosY;

                // 获取当前的 View
                View curView = mIconViews.get(curPos);
                float startX = curView.getTranslationX();
                float startY = curView.getTranslationY();
                // 创建 ObjectAnimator 对象，使用 translationX 和 translationY 来平滑移动
                ObjectAnimator animX = ObjectAnimator.ofFloat(curView, "translationX", startX, startX + deltaX);
                ObjectAnimator animY = ObjectAnimator.ofFloat(curView, "translationY", startY, startY + deltaY);

                // 设置动画时长
                animX.setDuration(durationMillis);
                animY.setDuration(durationMillis);

                // 创建 AnimatorSet 以便同时执行 X 和 Y 方向的动画
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(animX, animY);
                // 监听动画事件
                int finalCurPos = curPos;
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        // 将当前 View 设置为空视图，表示正在移动
                        mIconViews.set(finalCurPos, emptyView);
                        movingAnimationEnd = false;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // 计算新的位置
                        int pos = clockwise ? finalCurPos + 1 : (finalCurPos - 1) >= 0 ? (finalCurPos - 1) : (finalCurPos - 1) + mIconViews.size();

                        // 将移动后的 View 重新放回列表
                        mIconViews.set(pos, curView);

                        // 如果到达目标位置，更新目标 View 和状态
                        if (toPos == finalCurPos) {
                            mIconViews.set(toPos, touchingView);
                            curItemId = toPos;
                            movingAnimationEnd = true;
                        }

                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        // 动画取消时的处理（如有需要）
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        // 动画重复时的处理（如有需要）
                    }
                });
                animatorSet.start();

                lastPosX = curPosX;
                lastPosY = curPosY;

            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        super.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
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
//        if (!showApps)
//            return;
        int childCount = mIconViews.size();
//        Log.e(TAG, "onLayout: mIconViews" + mIconViews);
        float perAngle = 360.f / childCount;

        for (int i = 0; i < childCount; i++) {
//            Log.e(TAG, "onLayout: i = " + i );
            View childView = mIconViews.get(i);
//            if (childView == null || childView == emptyView) childView = touchingView;
            int childW = childView.getMeasuredWidth();
            int childH = childView.getMeasuredHeight();
            double angleInRadians = Math.toRadians(90 + i * perAngle);
            int posX = (int) (centerX + maxRadius * Math.cos(angleInRadians));
            int posY = (int) (centerY + maxRadius * Math.sin(angleInRadians));
            childView.setTranslationX(0);
            childView.setTranslationY(0);
//            Log.e(TAG, "onLayout: dist = " + CalculateUtil.calculateDistance(posX, posY, centerX, centerY) );
            childView.layout(posX - childW / 2, posY - childH / 2, posX + childW / 2, posY + childH / 2);
        }
    }


    //先resume 再 onMeasure  onSizeChanged onLayout onDraw
    private float rippleProgress = 1f; // 用于跟踪绘制进度
    private float showAppsProgress = 1f;
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (ripple) drawRipple(canvas);

        if(showApps) drawAppsToVisible();

        if(hideApps) drawAppsToInvisible();

        if (appsHalting) haltingApps();

        if (shouldDraw) {
            postInvalidateDelayed(DELAY_MILLISECONDS_SHORT);
        }
    }

    List<View> mIconViews = new ArrayList<>();
    View emptyView;
    public void generateViews(List<AppData> mData) {
        removeAllViews();
        int i = 0;
        for (AppData appData : mData) {
            IconView iconView = new IconView(getContext(), appData.getIcon());
            iconView.setPkgName(appData.getPkgName());
            mIconViews.add(iconView);
            iconView.setVisibility(View.GONE);
            addView(iconView);
        }
    }
    private void showApps() {
        shouldDraw = true;
        showApps = true;
        for (int i = 0 ;i < getChildCount();i ++ ) {
            View child = getChildAt(i);
            child.setVisibility(View.VISIBLE);
        }
        requestLayout();
        invalidate();
    }
    private void showRipple(float x, float y) {
        shouldDraw = true;
        rippleProgress = 0;
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
        invalidate();
    }
    private void hideApps() {
        shouldDraw = true;
        hideApps = true;
        hideAppsProgress = 0;
        invalidate();
    }

    private boolean shouldDraw = false;
    private void clearRippleFlag() {
//        Log.e(TAG, "clearRippleFlag: " );
        rippleProgress = 0;
//        showApps = false;
        ripple = false;
        shouldDraw = false;
    }

    private void clearShowAppsFlag() {
//        Log.e(TAG, "clearShowAppsFlag: " );
        showAppsProgress = 0;
        showApps = false;
        shouldDraw = false;
    }
    private void clearhideAppsFlag() {
//        Log.e(TAG, "clearhideAppsFlag: " );
        hideAppsProgress = 0;
        hideApps = false;
        shouldDraw = false;
    }

    //水花
    private float perCircleProgress = 1f/(rippleCircleNum + 1);//每个圆需要消耗的progress
    private void drawRipple(Canvas canvas) {
        for (int i = 0; i < rippleCircleNum; i++) {
            if (spreadRadius.get(i) >= maxRadius) continue;
            float startPoint = perCircleProgress * i;
            float endPoint = startPoint + perCircleProgress * 2;
//            Log.e(TAG, "drawRipple: i = "+ i + " startpoint = "+startPoint);

            if (startPoint < rippleProgress && rippleProgress <= endPoint) {
                float curProgress = (rippleProgress - startPoint) /(perCircleProgress * 2);
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
//        Log.e(TAG, "onDraw: rippleProgress = "+ rippleProgress);
        rippleProgress += DELAY_MILLISECONDS_SHORT / curDrawingTime;
        if (rippleProgress >= 1) clearRippleFlag();
        else {
            shouldDraw = true;
        }
    }



    private void drawAppsToVisible() {
        float alpha;
        alpha = showAppsProgress * rippleCircleNum;
//        Log.e(TAG, "setIconAlpha: alpha " + alpha );
        for (int i = 0; i < getChildCount(); i++) {
            ((IconView) getChildAt(i)).setIconAlpha(alpha > 1 ? 1 : alpha);
        }
        showAppsProgress += DELAY_MILLISECONDS_SHORT / curDrawingTime;
        if (showAppsProgress >= 1) {
            clearShowAppsFlag();
            //触发隐藏
            hideApps();
        } else {
            shouldDraw = true;
        }
    }

    private void haltingApps () {
        float alpha = 1;
        for (int i = 0; i < getChildCount(); i++) {
            ((IconView) getChildAt(i)).setIconAlpha(alpha);
        }
    }

    private float hideAppsProgress = 1f;
    private void drawAppsToInvisible(){
        float alpha;
        alpha = 1 - hideAppsProgress;
//        Log.e(TAG, "setIconAlpha: alpha " + alpha );
        for (int i = 0; i < getChildCount(); i++) {
            ((IconView) getChildAt(i)).setIconAlpha(alpha < 0 ? 0 : alpha);
        }
        hideAppsProgress += DELAY_MILLISECONDS_SHORT / curDrawingTime;

        if (hideAppsProgress >= 1) {
            clearhideAppsFlag();
            for (int i = 0 ;i < getChildCount();i ++ ) {
                View child = getChildAt(i);
                child.setVisibility(View.GONE);
            }
        }
        else {
            shouldDraw = true;
        }
    }


    private int getTouchingPos (float x, float y, int halfAppSize, int curPos){
        float perAngle = 360f / mIconViews.size();
        for (int i = 0; i < mIconViews.size(); i++) {
            if (i == curPos) continue;
            float angle = 90 + perAngle * i;
            double radians = Math.toRadians(angle);
            double xi = maxRadius * Math.cos(radians) + centerX;
            double yi = maxRadius * Math.sin(radians) + centerY;
            int dist = CalculateUtil.calculateDistance(x, y, xi, yi);
            if (halfAppSize > dist)
                return i;
        }
        return -1;

    }

    private int getTouchingView (float x, float y) {
        for (int i = 0; i < mIconViews.size(); i++) {
            View child = mIconViews.get(i);

            // 判断触摸点是否在当前子 View 的范围内
            if (x >= child.getLeft() && x <= child.getRight() &&
                    y >= child.getTop() && y <= child.getBottom()) {
                // 触摸点在子 View 内
                return i;
            }
        }
        return -1;
    }

    private int getViewIdFromAngel (float angle, float perAngle) {
        if (angle < 90) {
            angle = angle + 360;
        }
        return Math.round((angle - 90) / perAngle);
    }

    private boolean isOnTouchingView (float x, float y) {
        if (x >= touchingView.getLeft() && x <= touchingView.getRight() &&
                y >= touchingView.getTop() && y <= touchingView.getBottom()) {
            // 触摸点在子 View 内
            return true;
        }
        return false;
    }

    private boolean isMoving (MotionEvent event) {
        if (isMoving) return isMoving;
        float deltaX = lastDownX - event.getX();
        float deltaY = lastDownY - event.getY();
        if (Math.abs (deltaX + deltaY) > 50) isMoving = true;
        return isMoving;
    }


//    private boolean setMovingEnd () {
//        Arrays.fill(movingPos, false);
//    }

}
