package com.example.dogelauncher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dogelauncher.R;
import com.example.dogelauncher.viewModel.AppListViewModel;

public class DogeView extends ViewGroup {
    private static final String TAG = "DogeView";

    private static int displayMode;
    private static final int DISPLAY_MODE_SURROUNDING = 0;
    private static final int DISPLAY_MODE_LIST = 1;
    private static final int DISPLAY_MODE_EDIT = 2;

//    private int touchMode;
//    private static final int TOUCH_MODE_EMPTY = 0;
//    private static final int TOUCH_MODE_LIST = 1;
//    private static final int TOUCH_MODE_EDIT = 2;

    private int status;
    private static final int STATUS_FREE = 0;
    private static final int STATUS_UNAVAILABLE = 0;

    //view
    private SurroundingView surroundingView;
    private ViewGroup ListingView;
    private ViewGroup editingView;


    //view model
    private AppListViewModel viewModel;
    public void setViewModel (AppListViewModel viewModel) {
        this.viewModel = viewModel;
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
            switch (displayMode) {
                case DISPLAY_MODE_SURROUNDING:
                    surroundingView.showSurroundingApps(event.getX(), event.getY(), viewModel);
                    break;
            }
        }
    }

    public DogeView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DogeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);



    }

    public void initView () {
        FlingListener flingListener = new FlingListener();
        gestureDetector = new GestureDetector(getContext(), flingListener);
        surroundingView = (SurroundingView) LayoutInflater.from(getContext()).inflate(R.layout.DogeView, this, false);

    }




    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
