package com.example.dogelauncher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dogelauncher.viewModel.AppListViewModel;

public class DogeView extends ViewGroup {
    private static final String TAG = "DogeView";


    private static final int DISPLAY_MODE_SURROUNDING = 0;
    private static final int DISPLAY_MODE_LIST = 1;
    private static final int DISPLAY_MODE_EDIT = 2;
    private static int displayMode = DISPLAY_MODE_SURROUNDING;


//    private int touchMode;
//    private static final int TOUCH_MODE_EMPTY = 0;
//    private static final int TOUCH_MODE_LIST = 1;
//    private static final int TOUCH_MODE_EDIT = 2;

    private int status;
    private static final int STATUS_FREE = 0;
    private static final int STATUS_UNAVAILABLE = 1;

    //view
    private MainView mainView;
    private ViewGroup ListingView;
    private ViewGroup editingView;


    //view model
    private AppListViewModel viewModel;

    public void setViewModel (AppListViewModel viewModel) {
        this.viewModel = viewModel;
        mainView.generateViews(viewModel.getData().subList(0, 6));
    }

    private GestureDetector gestureDetector;
    private class FlingListener extends GestureDetector.SimpleOnGestureListener {
        private final int minimumVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();

        private FlingListener() {

        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            if (status == STATUS_UNAVAILABLE) return false;
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
            if (status == STATUS_FREE) return false;

            if (velocityY > velocityX &&
                    velocityY >= minimumVelocity &&
                    e1 != null && e2 != null &&
                    e2.getY() - e1.getY() > 0) {
                return true;
            }
            return false;
        }
    }
    private class DogeViewTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (status == STATUS_UNAVAILABLE) return true;
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    switch (displayMode) {
                        case DISPLAY_MODE_SURROUNDING:
                            mainView.showSurroundingApps(event.getX(), event.getY());
                            break;
                    }
                    break;
            }
            return true;
        }
    }

    public DogeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }


    public void initView () {
        FlingListener flingListener = new FlingListener();
        gestureDetector = new GestureDetector(getContext(), flingListener);
        mainView = new MainView(getContext());
        addView(mainView);
        setOnTouchListener(new DogeViewTouchListener());

        /*
         * ViewGroup在没有背景时不会走onDraw方法，但可以走dispatchDraw
         * 原因在于View对onDraw的控制时做了限定：[if (!dirtyOpaque) onDraw(canvas)]
         * 你可以使用onDraw，在之前设个透明色即可:setBackgroundColor(0x00000000);
         * */
        setBackgroundColor(0x00000000);
    }




    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        [1].必须onMeasure中测量孩子的尺寸，否则无法显示
//        [2].必须onLayout中布局孩子的位置，否则无法显示
//        [3].在onLayout中孩子不能用view.getHeight()获取尺寸(因为为0)，只能用view.getMeasuredHeight

//        int childCount = getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            View childView = getChildAt(i);
//            int childW = childView.getMeasuredWidth();
//            int childH = childView.getMeasuredHeight();
//            Log.e(TAG, "onLayout: childW "+childW + " childH "+ childH );
//            int topPos = 0;
//            int leftPos = 0;
//            childView.layout(leftPos, topPos, leftPos + childW, topPos + childH);
//        }
        int measuredWidth = mainView.getMeasuredWidth();
        int measuredHeight = mainView.getMeasuredHeight();
        mainView.layout(0,0, measuredWidth, measuredHeight);
    }



}
