package com.example.dogelauncher.view;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class MyViewPager extends ViewGroup {

    private static final String TAG = "MyViewPager";
    private static final boolean DEBUG = false;

    //在onTouchEvent()把事件传递给手势识别器 控制横向移动
    private GestureDetector detector;

    //当前页面的下标位置
    private int currentIndex;

    private MyScroller scroller;

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(final Context context) {
        scroller = new MyScroller();
        //2.实例化手势识别器
        //长按 双击 滑动
        detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                scrollBy((int) distanceX, 0);
                return false;
            }

        });

    }

    /**
     * （l,t）为左上角坐标，（r,b）为右下角
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(DEBUG) Log.e(TAG, "onLayout: (" + l + "," + t + ") (" + r + "," + b + " )");//match_parent时是(0,0) (1080,1962 )
        //遍历孩子，给每个孩子指定在屏幕的坐标位置
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            childView.layout(i * getWidth(), 0, (i + 1) * getWidth(), getHeight());
        }
    }

    private float startX;

    private float downX;
    private float downY;

    @Override
    //左右移动归自己管，上下就不管
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //传给detector，但是其对上下滑动完全不做操作，而且我们忽略其返回值
        //为了将detector内部的变量自行设置好，防止突然的跳动（比如x默认值为0，一下子就执行onScroll）

        boolean result = false;//默认传递给孩子

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //down事件信息传入detector
                detector.onTouchEvent(ev);
                if(DEBUG) Log.e(TAG, "onInterceptTouchEvent==ACTION_DOWN");
                //1.记录坐标
                startX = ev.getX();
                downY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //2.记录结束值
                float endX = ev.getX();
                float endY = ev.getY();

                //3.计算绝对值
                float distanceX = Math.abs(endX - startX);
                float distanceY = Math.abs(endY - downY);

                if (distanceX > distanceY && distanceX > 5) {
                    //识别到横移就拦截下来
                    if(DEBUG) Log.e(TAG, "onInterceptTouchEvent==ACTION_MOVE - X");
                    result = true;
                } else {
                    if(DEBUG) Log.e(TAG, "onInterceptTouchEvent==ACTION_MOVE  - Y");
                }
//                else{
//                    scrollToPager(currentIndex);
//                }
                break;
            case MotionEvent.ACTION_UP:
                if(DEBUG) Log.e(TAG, "onInterceptTouchEvent==ACTION_UP");
                break;
        }
        return result;
    }

    //拦截下来就执行这个
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        //3.把事件传递给手势识别器  down就记录，move不管（由手势识别器做scroll），up为翻页扫尾
        detector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //1.记录开始move坐标
                startX = event.getX();
                if(DEBUG) Log.e(TAG, "onTouchEvent: startX:" + startX);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP://用于恢复
                //2.来到新的坐标
                float endX = event.getX();
                if(DEBUG) Log.e(TAG, "onTouchEvent up: startX:" + startX);
                if(DEBUG) Log.e(TAG, "onTouchEvent up: endX:" + endX);
                //下标位置
                int tempIndex = currentIndex;
                if ((startX - endX) > getWidth() / 2) {
                    //显示下一个页面
                    tempIndex++;
                } else if ((endX - startX) > getWidth() / 2) {
                    //显示上一个页面
                    tempIndex--;
                }
                //根据下标位置移动到指定的页面
                scrollToPager(tempIndex);

                break;
        }
        return true;
    }

    /**
     * 屏蔽非法值，根据位置移动到指定页面
     *
     * @param tempIndex
     */
    public void scrollToPager(int tempIndex) {
        //屏蔽非法值
        if (tempIndex < 0) {
            tempIndex = 0;
        }
        if (tempIndex > getChildCount() - 1) {
            tempIndex = getChildCount() - 1;
        }
        //当前页面的下标位置
        currentIndex = tempIndex;

        if(DEBUG) Log.e(TAG, "scrollToPager: currentIndex*getWidth(): " + currentIndex * getWidth());
        if(DEBUG) Log.e(TAG, "scrollToPager:getScrollX() " + getScrollX());
        /**
         * 逻辑：
         * getScrollX() 相当于获取滑动抬起时画布左上角和viewgroup左上角的X距离  （手指左滑为正）
         * currentIndex*getWidth()相当于滑动弹回后画布左上角和viewgroup左上角的X距离，也就是实际应该走的距离
         * 两者相减得到剩下该走的距离
         * */

        //剩余需要的偏移量
        int distanceX = currentIndex * getWidth() - getScrollX();
        //移动到指定的位置
        //getScrollX  getScrollY都是 从视图的滚动开始（即用户开始滑动）到当前时刻的总偏移量，而不是上一次滑动结束后的距离。
        //第一次调用可能返回100，然后用户继续滑动，第二次调用可能返回200，依此类推
        //scroller初始化当前值
        scroller.startScroll(getScrollX(), getScrollY(), distanceX, 0);

        invalidate();
        //会调computeScroll();
    }

    public void scrollToNextPager (boolean forward) {
        Log.e(TAG, "scrollToNextPager: forward = "+ forward );
    }


    @Override
    //原生View的computeScroll是空的
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            //目标X
            float currX = scroller.getCurrX();

            scrollTo((int) currX, 0);
            invalidate();//又会回来调用computeScroll
        }
        ;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int newsize = MeasureSpec.makeMeasureSpec(size, mode);

        System.out.println("widthMeasureSpec==" + widthMeasureSpec + "size==" + size + ",mode==" + mode);
        System.out.println("widthMeasureSpec==" + widthMeasureSpec + "sizeHeight==" + sizeHeight + ",modeHeight==" + modeHeight);

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, heightMeasureSpec);
        }

    }
}



class MyScroller {

    /**
     * X轴的起始坐标
     */
    private float startY;
    /**
     * Y轴的起始坐标
     */
    private float startX;

    /**
     * 在X轴要移动的距离
     */
    private int distanceX;
    /**
     * 在Y轴要移动的距离
     */
    private int distanceY;
    /**
     开始的时间
     */
    private long startTime;

    /**
     * 总时间
     */
    private long totalTime = 500;
    /**
     * 是否移动完成
     * false没有移动完成
     * true:移动结束
     */
    private boolean isFinish;
    private float currX;

    /**
     * 得到坐标
     */
    public float getCurrX() {
        return currX;
    }

    /**
     * 平均速度
     */
    private long avgSpeed;


    public void startScroll(float startX, float startY, int distanceX, int distanceY) {
        this.startY = startY;
        this.startX = startX;
        this.distanceX = distanceX;
        this.distanceY = distanceY;
        this.startTime = SystemClock.uptimeMillis();//系统开机时间

        this.avgSpeed = distanceX / totalTime;
        this.isFinish = false;
    }

    /**
     * 速度
     求移动一小段的距离
     求移动一小段对应的坐标
     求移动一小段对应的时间
     */
    public boolean computeScrollOffset(){
        if(isFinish){
            return  false;
        }
        long endTime = SystemClock.uptimeMillis();
        //这一小段所花的时间
        long passTime = endTime - startTime;
        if(passTime < totalTime){
            //计算平均速度
            //移动这个一小段对应的距离
            float segmentDistance = passTime * avgSpeed;
            //目标X
            currX = startX + segmentDistance;
        }else{
            //移动结束
            isFinish =true;
            currX = startX + distanceX;
        }

        return  true;
    }
}

