package com.example.dogelauncher.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.method.Touch;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.dogelauncher.app.DogeApp;
import com.example.dogelauncher.model.AppData;
import com.example.dogelauncher.utils.BarUtils;
import com.example.dogelauncher.viewModel.AppListViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class CellView extends ViewGroup {

    private static final String TAG = "CellView";

    //默认为1
    public int row = 1;
    public int col = 1;

    //360dp * 240dp
    private static int SIZE_PER_OUTER_GRID = 72 * DogeApp.dpiTimes; // 160为每个内部格子
    private static int SIZE_PER_INNER_GRID = (72 - 6) * DogeApp.dpiTimes;
    public static final int ICON_MARGIN = 3 * DogeApp.dpiTimes;

    private boolean showBg;

    public static final int ICON_TAG_SINGLE = 0;
    public static final int ICON_TAG_LIST = 1;

    public int iconTag = ICON_TAG_SINGLE;

    private boolean touchable = true;

    private AppData appData;
    protected List<AppData> appDatas;

    protected List<CellView> mCellViews;

    private PopupMenu popupMenu;

    private static WindowManager.LayoutParams params;
    private CellView curTouchOn;

    public CellView (Context context) {
        super(context);
    }

    public CellView (Context context,AttributeSet attrs) {
        super(context,attrs);
//        setClipChildren(true);
    }

    public CellView(Context context, AppData appData) {
        super(context);
        iconTag = ICON_TAG_SINGLE;
        this.appData = appData;
        row = 1;
        col = 1;
        if (appData != null ) addView(new IconView(context, appData.getIcon()));
        touchable = false;
    }

    public CellView(Context context, List<AppData> appDatas) {
        super(context, null);
        this.appDatas = appDatas;
        init();
    }

    public static CellView createAppListView (Context context, List<AppData> appDatas, int row, int col) {
        CellView cellView = new CellView(context, appDatas);
        cellView.iconTag = ICON_TAG_LIST;
        cellView.row = row;
        cellView.col = col;
        cellView.init();
        return cellView;
    }

    private int perWidSize;
    private int perHeiSize;
    public void init() {
        // 设置背景，带透明度和圆角
        curTouchOn = this;
        showBg();
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams(
                DogeApp.widthPixels,
                DogeApp.heightPixels,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );// |WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,   //PixelFormat.OPAQUE
    }

    //通过矩阵来摆放
    private TouchableView[][] pos2TouchableView;
    private Map<CellView, TouchableView> view2PosInfo;

    public void createPosInfo () {
        view2PosInfo = new HashMap<>(row * col);
        pos2TouchableView = new TouchableView[row][col];
        int curRow = 0;
        int curCol = 0;
        for (int i = 0;i < col * row && i < mCellViews.size(); i ++) {
            CellView cellView = mCellViews.get(i);
            TouchableView touchableView = new TouchableView();
            touchableView.touchingView = cellView;
            touchableView.wid = cellView.getMeasuredWidth();
            touchableView.hei = cellView.getMeasuredHeight();
            boolean found = false;
            while (!found) {
                if (curRow + cellView.row - 1 >= row) {
                    //放不下去了
                    break;
                    //throw new RuntimeException("摆放不合理！！！！");
                }
                while (curCol + cellView.col - 1 < col ) {
                    int num = canLayout (curRow, curCol, cellView.col, pos2TouchableView);
                    if (num == 0) break;
                    curCol += num;
                }

                if (curCol + cellView.col - 1 >= col) {
                    curRow += 1;
                    curCol = 0;
                } else {
                    //can layout
                    touchableView.touchingViewPosRow = curRow;
                    touchableView.touchingViewPosCol = curCol;

                    calculateCenter(curRow, curCol, cellView.row, cellView.col, touchableView);
                    touchableView.pos = mCellViews.indexOf(touchableView.touchingView);
                    setPos(curRow, curCol, pos2TouchableView, touchableView);
                    curCol += cellView.col;

                    view2PosInfo.put(cellView, touchableView);
                    found = true;

                }
            }
            if (!found) {
                Log.e(TAG, "createPosInfo: " + "cant layout (too large)" );
            }
        }
    }

    private void  showBg () {
        if (showBg) setBackground(createRoundedBackground());
    }

    // 创建一个带圆角和透明度的灰色背景
    private Drawable createRoundedBackground() {
        float radius = 50.0f; // 圆角半径
        int backgroundColor = 0x80CCCCCC; // 半透明灰色

        // 创建一个 ShapeDrawable 来实现圆角背景
        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        backgroundDrawable.setColor(backgroundColor);  // 设置背景颜色
        backgroundDrawable.setCornerRadius(radius);    // 设置圆角半径

        return backgroundDrawable;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widSpec;
        int widSize = MeasureSpec.getSize(widthMeasureSpec);
        int perWidSize = widSize / col;
        int heiSpec;
        int heiSize = MeasureSpec.getSize(heightMeasureSpec);
        int perHeiSize = heiSize / row;
        int widMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widMode == MeasureSpec.UNSPECIFIED || widMode == MeasureSpec.AT_MOST) {
            widSpec = MeasureSpec.makeMeasureSpec(widSize, MeasureSpec.EXACTLY);
        } else {
            widSpec = widthMeasureSpec;
        }

        int heiMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heiMode == MeasureSpec.UNSPECIFIED || heiMode == MeasureSpec.AT_MOST) {
            heiSpec = MeasureSpec.makeMeasureSpec(perWidSize * row, MeasureSpec.EXACTLY);
        } else {
            heiSpec = heightMeasureSpec;
        }

        setMeasuredDimension(widSpec, heiSpec);

        //非AppListView都只能是方形
        int perGridSize = perWidSize;
        int marginSize = perGridSize / 4;

        switch (iconTag) {
            case ICON_TAG_SINGLE:
                View child = getChildAt(0);
//                Log.e(TAG, "onMeasure: child ="+ child );
                if (child != null) {
                    child.measure(widSpec, heiSpec);
                }
                break;
            case ICON_TAG_LIST:
                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childView = getChildAt(i);
                    if (childView instanceof CellView) {
                        CellView cellView = (CellView) childView;
                        childView.measure(MeasureSpec.makeMeasureSpec(perGridSize * cellView.col - 2 * marginSize, MeasureSpec.EXACTLY ),
                                MeasureSpec.makeMeasureSpec(cellView.row * perGridSize - 2 * marginSize, MeasureSpec.EXACTLY ));
                    } else {
                        childView.measure(widSpec, heiSpec);
                    }
                }
                break;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();

        //规定了子Cell只能是方形的
        perWidSize = measuredWidth / col;
        perHeiSize = measuredHeight / row;

        switch (iconTag) {
            case ICON_TAG_SINGLE:
                View child = getChildAt(0);
                if (child != null) {
                    //child.layout的参数是对父来说的
                    child.layout(0, 0,  measuredWidth, measuredHeight);
                }
                break;
            case ICON_TAG_LIST:
                if (mCellViews != null){
                    if (pos2TouchableView == null)  createPosInfo();
                    //layout from pos2TouchableView
                    for (int i = 0; i < mCellViews.size() && i < row * col; i++) {
                        CellView cellView = mCellViews.get(i);
                        cellView.setTranslationX(0);
                        cellView.setTranslationY(0);
                        int childL = view2PosInfo.get(cellView).touchingViewPosCol * perWidSize + perWidSize / 4;
                        int childT = view2PosInfo.get(cellView).touchingViewPosRow * perHeiSize + perHeiSize / 4;
                        int childR = childL + cellView.getMeasuredWidth();
                        int childB = childT + cellView.getMeasuredHeight();
                        cellView.layout(childL, childT, childR, childB);
                    }
                } else {
                    //单独的一个控件  cellview单独包裹一个控件 不管其他情况
                    View childView = getChildAt(0);
                    if (childView == null) return;
                    int childL = 0;
                    int childT = 0;
                    int childR = childL + childView.getMeasuredWidth();
                    int childB = childT + childView.getMeasuredHeight();
                    childView.layout(childL, childT, childR, childB);
                }

                break;
        }

    }

    private int canLayout (int r, int c, int size, TouchableView[][] pos) {
        int curR = r;
        int curC = c;

        while (curR < r + size) {
            while (curC < c + size) {
                if (pos[r][c] != null) return curC - c + 1;
                curC ++;
            }
            curR ++;
            curC = c;
        }
        return 0;
    }
    private void setPos(int curRow, int curCol, TouchableView[][] pos, TouchableView view) {
        for (int i = curRow; i < curRow + view.touchingView.row; i ++ ) {
            for (int j = curCol; j < curCol + view.touchingView.col; j ++) {
                pos[i][j] = view;
            }
        }
    }
    private void calculateCenter(int curRow, int curCol, int childRow, int childCol, TouchableView touchableView) {
        int perWidSize = getMeasuredWidth() / col;
        int perHeiSize = getMeasuredHeight() / row;

        float childRowF = (float) childRow;
        float childColF = (float) childCol;

        float centerX = perWidSize * curCol + childColF / 2 * perWidSize;
        float centerY = perHeiSize * curRow + childRowF /2 * perHeiSize;

        touchableView.centerX = centerX;
        touchableView.centerY = centerY;

    }


    private boolean iconDragging;
    private CellView emptyCellView;
    private CellView mDraggingView;

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (!iconDragging) return;
    }

    private GestureDetector gestureDetector = new GestureDetector(getContext(),new CellViewGestureDetector() );
    private class CellViewGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private final int minimumVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
        private CellViewGestureDetector() {

        }
        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            switch (iconTag) {
                case ICON_TAG_SINGLE:
                    Toast.makeText(getContext(), "start app", Toast.LENGTH_SHORT).show();
                    break;
                case  ICON_TAG_LIST:
                    if (curCellView != null) curCellView.touchingView.onTouchEvent(e);
                    break;
            }
            return true;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent e) {
            longPress = true;

            if (curCellView != null) {
                //根据MyViewPager的特性 长按后选定cellview，不能被其scroll拦截
                requestDisallowInterceptTouchEvent(true);

                if(overlay == null) {
                    overlay = new DraggingViewWindow(getContext());
                    overlay.setBackgroundColor(Color.TRANSPARENT);
                }
                if (!overlay.isAttachedToWindow()) {
                    curCellView.touchingView.getLocationInWindow(points);
                    mDraggingView = curCellView.touchingView;
                    mCellViews.set(mCellViews.indexOf(mDraggingView), emptyCellView = createEmptyCellView(mDraggingView));
                    curCellView.touchingView = emptyCellView;
                    addView(emptyCellView);
                    emptyCellView.layout(curCellView.touchingView.getLeft(),
                            curCellView.touchingView.getTop(),
                            curCellView.touchingView.getRight(),
                            curCellView.touchingView.getBottom());

                    removeView(mDraggingView);
                    overlay.setView(mDraggingView);
                    overlay.setSize(curCellView.wid, curCellView.hei);
                    overlay.setPosition(points[0], points[1]);
                    mWindowManager.addView(overlay, params);
                    //震动 弹出菜单
//                    popupMenu = new PopupMenu(getContext(), curCellView.touchingView);
//                    popupMenu.getMenuInflater().inflate(R.menu.icon_menu, popupMenu.getMenu());
//                    popupMenu.show();
//                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                        @Override
//                        public boolean onMenuItemClick(MenuItem item) {
//                            // 控件每一个item的点击事件
//                            int itemId = item.getItemId();
//                            if (itemId == R.id.edit) {
//                                Toast.makeText(getContext(), "edit", Toast.LENGTH_SHORT).show();
//                            } else if (itemId == R.id.delete) {
//                                Toast.makeText(getContext(), "delete", Toast.LENGTH_SHORT).show();
//                            }
//                            return true;
//                        }
//                    });
                }

            }
        }

        //18898597241
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            //这个可能会调到onScroll吗
            if (velocityX > velocityY &&
                    velocityX >= minimumVelocity &&
                    e1 != null && e2 != null &&
                    e2.getY() - e1.getY() > 5) {
                Log.e(TAG, "onFling: " );

                if (mOnFlingListener != null) {
                    mOnFlingListener.onFling(e2.getY());
                }
                return true;
            }
            return false;
        }
    }

    private CellView createEmptyCellView(CellView outerCellView) {
        switch (outerCellView.iconTag) {
            case ICON_TAG_SINGLE:
                CellView cellView  = new CellView(getContext());
                cellView.row = outerCellView.row;
                cellView.col = outerCellView.col;
                cellView.addView(new FrameLayout(getContext()));
                view2PosInfo.put(cellView, view2PosInfo.get(outerCellView));
                return cellView;
        }

        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (curCellView == null) {
                    //执行外层的scroll
                    if (mOnNoSelectedViewScrollListener != null) {
                        mOnNoSelectedViewScrollListener.onNoSelectedViewScroll(event.getX() - curX);
                    }
                } else {
                    switch (iconTag) {
                        case ICON_TAG_SINGLE:
                            Toast.makeText(getContext(), "icon scroll is unreasonable", Toast.LENGTH_SHORT).show();
                            break;
                        case ICON_TAG_LIST:
                            if (longPress) {
                                if (Math.abs(event.getX() - curX) + Math.abs(event.getY() - curY) >= 5) {
                                    //if (popupMenu != null) popupMenu.dismiss();
                                    //移动逻辑，因为要移出画面，采用透明window覆盖方法，给好参数外包出去

                                    if (mOnCellViewScrollListener != null) {
                                        mOnCellViewScrollListener.onCellViewScroll(points[0], points[1]);
                                    }

                                    float nowX = event.getRawX();
                                    float nowY = event.getRawY();
                                    int dx = (int) (nowX - curX);
                                    int dy = (int) (nowY - curY);

//                                    Log.e(TAG, "onTouchEvent: curX = "+curX + "  curY = "+curY );
//                                    Log.e(TAG, "onTouchEvent: nowX = "+nowX + "  nowY = "+nowY );

                                    int left = points[0] + dx;
                                    int top = points[1] + dy;
                                    overlay.setPosition(left, top);

                                    //动画结束前可以移动但不走逻辑，
                                    if (!isChanging) {
                                        // 判断边缘
                                        if (getMeasuredWidth() - nowX < curCellView.wid / 3 || nowX - 0 < curCellView.wid / 3) {
                                            if (mOnCellViewScrollListener != null) {
                                                if (mOnCellViewScrollListener.isScrollToEdgOpFinished()) {
                                                    curTouchOn = mOnCellViewScrollListener.onCellViewScrollToEdge(getMeasuredWidth() - nowX < curCellView.wid / 3);
                                                }
                                            }
                                        } else if (curTouchOn != null && curTouchOn != this) {
                                            //考虑到另一个ListView的移动
                                            curTouchOn.movingOnOtherCellView(nowX, nowY, mDraggingView);

                                        } else {
                                            //判断落在哪个格子,然后跟pos的那个比距离就好
                                            //边界没关系，不用纠结
                                            TouchableView touchableView = getTouchingViewFromDownEvent(nowX, nowY);

                                            //同个格子不管
                                            if (touchableView.touchingView == emptyCellView) break;

                                            //
                                            float distX = Math.abs(touchableView.centerX - nowX);
                                            float distY = Math.abs(touchableView.centerY - nowY);
                                            if (distX < touchableView.wid / 2 && distY < touchableView.hei) {
                                                //触发图标变大等绘制 这是上下插入

                                            } else if (distX < touchableView.wid && distY < touchableView.hei / 2) {
                                                //触发位移  左右插入
                                                //判断是否可以位移 顺便解决了位移最终样子
                                                ArrayList<CellView> cellViews = new ArrayList<>(mCellViews);
                                                TouchableView[][] newPosInfo = new TouchableView[row][col];
                                                HashMap<CellView, TouchableView> newView2Touchable = new HashMap<>();
                                                boolean canlayout = canLayout(curCellView.pos, touchableView.pos,
                                                        cellViews,
                                                        newPosInfo,
                                                        newView2Touchable);

                                                if (canlayout) {
                                                    animateChangingPos(cellViews, newPosInfo, newView2Touchable);
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                            break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                //这里清除标志位
                if (curTouchOn != null && curTouchOn != this) {
                    curTouchOn.draggingEnd();
                    curTouchOn = null;
                    mCellViews.remove(emptyCellView);
                    removeView(emptyCellView);
                    TouchableView[][] posInfo = new TouchableView[row][col];
                    HashMap<CellView, TouchableView> newView2Touchable = new HashMap<>();

                    canLayout(mCellViews,
                            posInfo,
                            newView2Touchable);
                    pos2TouchableView = posInfo;
                    view2PosInfo = newView2Touchable;
                    emptyCellView = null;
                    requestLayout();
                    invalidate();
                } else if (longPress) {
                    longPress = false;
                    iconDragging = false;
                    if (emptyCellView != null) {
                        mCellViews.set(mCellViews.indexOf(emptyCellView), mDraggingView);
                        view2PosInfo.get(emptyCellView).touchingView = mDraggingView;
                        view2PosInfo.put(mDraggingView, view2PosInfo.get(emptyCellView));
                        view2PosInfo.remove(emptyCellView);
                        curCellView = null;
                    }
                    //清除窗口
                    if (overlay != null && overlay.isAttachedToWindow()) {
                        mWindowManager.removeView(overlay);
                        overlay.clear();
                    }
                    if (mDraggingView != null) {
                        addView(mDraggingView);
                        mDraggingView = null;
                        requestLayout();
                        invalidate();
                    }
                }
                //归位
        }
        return true;
    }

    private void draggingEnd() {
        if (emptyCellView != null) {
            mCellViews.set(mCellViews.indexOf(emptyCellView), mDraggingView);
            view2PosInfo.get(emptyCellView).touchingView = mDraggingView;
            view2PosInfo.put(mDraggingView, view2PosInfo.get(emptyCellView));
            view2PosInfo.remove(emptyCellView);
        }
        //清除窗口
        if (overlay != null && overlay.isAttachedToWindow()) {
            mWindowManager.removeView(overlay);
            overlay.clear();
        }
        if (mDraggingView != null) {
            addView(mDraggingView);
            mDraggingView = null;
            requestLayout();
            invalidate();
        }
    }

    private void movingOnOtherCellView(float nowX, float nowY, CellView draggingView) {

        TouchableView overlayView = getTouchingViewFromDownEvent(nowX, nowY);
        if (overlayView == null) return;
        mDraggingView =  draggingView;
        int overlayPos = overlayView.pos;

        float distX = Math.abs(overlayView.centerX - nowX);
        float distY = Math.abs(overlayView.centerY - nowY);
        if (!isChanging) {
            if (distX < overlayView.wid / 2 && distY < overlayView.hei) {
                //触发图标变大等绘制 这是上下插入

            } else if (distX < overlayView.wid && distY < overlayView.hei / 2) {
                //触发位移  左右插入
                //判断是否可以位移 顺便解决了位移最终样子
                ArrayList<CellView> cellViews = new ArrayList<>(mCellViews);
                TouchableView[][] newPosInfo = new TouchableView[row][col];
                HashMap<CellView, TouchableView> newView2Touchable = new HashMap<>();
                boolean canlayout;
                if (emptyCellView == null) {
                    //create emptyCellView
                    emptyCellView = createEmptyCellView(draggingView);
//                        addView(emptyCellView);
                    cellViews.add(overlayPos, emptyCellView);
                    canlayout = canLayout(
                            cellViews,
                            newPosInfo,
                            newView2Touchable);
                } else {
                    if (overlayView.touchingView == emptyCellView) return;
                    canlayout = canLayout(overlayView.pos, view2PosInfo.get(emptyCellView).pos,
                            cellViews,
                            newPosInfo,
                            newView2Touchable);
                }
                if (canlayout) {
                    animateChangingPos(cellViews, newPosInfo, newView2Touchable);
                } else {
                    Toast.makeText(getContext(), "摆放不合理", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean canLayout(List<CellView> tempCellViews, TouchableView[][] newPosInfo, Map<CellView, TouchableView> newView2Touchable) {
        int curRow = 0;
        int curCol = 0;
        for (int i = 0; i < col * row && i < tempCellViews.size(); i ++) {
            CellView cellView = tempCellViews.get(i);
            boolean found = false;
            while (!found) {
                if (curRow + cellView.row - 1 >= row) {
                    //放不下去了
                    return false;
                    //throw new RuntimeException("摆放不合理！！！！");
                }
                while (curCol + cellView.col - 1 < col ) {
                    int num = canLayout (curRow, curCol, cellView.col, newPosInfo);
                    if (num == 0) break;
                    curCol += num;
                }

                if (curCol + cellView.col - 1 >= col) {
                    curRow += 1;
                    curCol = 0;
                } else {
                    //can layout
                    TouchableView touchableView = new TouchableView();
                    touchableView.touchingView = cellView;
                    touchableView.wid = cellView.getMeasuredWidth();
                    touchableView.hei = cellView.getMeasuredHeight();
                    touchableView.touchingViewPosRow = curRow;
                    touchableView.touchingViewPosCol = curCol;
                    calculateCenter(curRow, curCol, cellView.row, cellView.col, touchableView);
                    touchableView.pos = i;

                    setPos(curRow, curCol, newPosInfo, touchableView);
                    newView2Touchable.put(cellView, touchableView);
                    curCol += cellView.col;
                    found = true;
                }
            }

        }
        return true;

    }

    private boolean canLayout (int from, int to, List<CellView> tempCellViews,
                               TouchableView[][] posInfo, Map<CellView, TouchableView> view2Pos) {
        Log.e(TAG, "canLayout: from = "+ from + "  to = "+ to );
        CellView fromView = tempCellViews.get(from);
        int addNum = from < to ? 1 : -1;
        for (int i = from + addNum ; i != to + addNum ; i+=addNum ) {
            tempCellViews.set(i - addNum, tempCellViews.get(i));
        }
        tempCellViews.set(to, fromView);

        int curRow = 0;
        int curCol = 0;
        for (int i = 0; i < col * row && i < tempCellViews.size(); i ++) {
            CellView cellView = tempCellViews.get(i);
            boolean found = false;
            while (!found) {
                if (curRow + cellView.row - 1 >= row) {
                    //放不下去了
                    return false;
                    //throw new RuntimeException("摆放不合理！！！！");
                }
                while (curCol + cellView.col - 1 < col ) {
                    int num = canLayout (curRow, curCol, cellView.col, posInfo);
                    if (num == 0) break;
                    curCol += num;
                }

                if (curCol + cellView.col - 1 >= col) {
                    curRow += 1;
                    curCol = 0;
                } else {
                    //can layout
                    TouchableView touchableView = new TouchableView();
                    touchableView.touchingView = cellView;
                    touchableView.wid = cellView.getMeasuredWidth();
                    touchableView.hei = cellView.getMeasuredHeight();
                    touchableView.touchingViewPosRow = curRow;
                    touchableView.touchingViewPosCol = curCol;
                    calculateCenter(curRow, curCol, cellView.row, cellView.col, touchableView);
                    touchableView.pos = i;

                    setPos(curRow, curCol, posInfo, touchableView);
                    view2Pos.put(cellView, touchableView);
                    curCol += cellView.col;
                    found = true;
                }
            }

        }
        return true;
    }

    private void animateChangingPos(List<CellView> cellViews, TouchableView[][] newPosInfo, HashMap<CellView, TouchableView> newView2Touchable) {
        AnimatorSet animatorSet = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<>(2 * row * col);
        for (int i = 0; i < cellViews.size() && i < row * col; i ++) {
            CellView movingCellView = cellViews.get(i);
            TouchableView oldMovingTouchableView = view2PosInfo.get(movingCellView);
            TouchableView newMovingTouchableView = newView2Touchable.get(movingCellView);
            ObjectAnimator animX;
            ObjectAnimator animY;
            if (oldMovingTouchableView == null) continue;
            if (newMovingTouchableView.touchingViewPosRow == oldMovingTouchableView.touchingViewPosRow
                    && newMovingTouchableView.touchingViewPosCol == oldMovingTouchableView.touchingViewPosCol) {
                continue;
            }
            int deltaX = perWidSize * (newMovingTouchableView.touchingViewPosCol - oldMovingTouchableView.touchingViewPosCol);
            int deltaY = perHeiSize * (newMovingTouchableView.touchingViewPosRow - oldMovingTouchableView.touchingViewPosRow);
            float startX = movingCellView.getTranslationX();
            float startY = movingCellView.getTranslationY();
            animX = ObjectAnimator.ofFloat(movingCellView, "translationX", startX, startX + deltaX);
            animY = ObjectAnimator.ofFloat(movingCellView, "translationY", startY, startY + deltaY);

            animX.setDuration(300);
            animY.setDuration(300);
            // 创建 AnimatorSet 以便同时执行 X 和 Y 方向的动画
            animators.add(animX);
            animators.add(animY);
        }
        animatorSet.playTogether(animators);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                view2PosInfo = newView2Touchable;
                pos2TouchableView = newPosInfo;
                mCellViews = cellViews;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                curCellView = view2PosInfo.get(emptyCellView);
                isChanging = false;
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        isChanging = true;
        animatorSet.start();
    }

    private static DraggingViewWindow overlay; // 用于显示拖拽图标的覆盖层

    private static WindowManager mWindowManager;
    int[] points = new int[2];

    private boolean isDragging = false; // 是否正在拖拽
    private TouchableView curCellView = null;
    private boolean longPress = false;
    private boolean isChanging = false;
    private float curX;
    private float curY;

    //只在onInterceptTouchEvent做提供信息的事情
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!touchable) return true;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                curX = ev.getRawX();
                curY = ev.getRawY();
                curCellView = getTouchingViewFromDownEvent(curX, curY);
                return true;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        //直接拦 由list根据情况分发
        return true;
    }

    //改这个
    public TouchableView getTouchingViewFromDownEvent (float x, float y) {
//        int actionBarHeight = BarUtils.getActionBarHeight();
        int statusBarHeight = BarUtils.getStatusBarHeight();
        y -= statusBarHeight;
        int tarCol = 1;
        while (tarCol < col && tarCol * perWidSize < x) {
            tarCol++;
        }
        int tarRow = 1;
        while (tarRow < row && tarRow * perHeiSize < y) {
            tarRow++;
        }
        //拿落点view
        Log.e(TAG, "getTouchingViewFromDownEvent: x = "+ x + "  y= "+ y );
        Log.e(TAG, "getTouchingViewFromDownEvent: tarRow = "+ tarRow + "  "+ tarCol );
        return pos2TouchableView[tarRow - 1][tarCol - 1];
    }



    private OnCellViewScrollListener  mOnCellViewScrollListener;
    public void setOnCellViewScrollListener (OnCellViewScrollListener onCellViewScrollListener) {
        this.mOnCellViewScrollListener = onCellViewScrollListener;
    }

    private OnNoSelectedViewScrollListener mOnNoSelectedViewScrollListener;
    public void setOnNoSelectedViewScrollListener (OnNoSelectedViewScrollListener onNoSelectedViewScrollListener) {
        mOnNoSelectedViewScrollListener = onNoSelectedViewScrollListener;
    }

    private OnFlingListener mOnFlingListener;
    public void setOnFlingListener (OnFlingListener onFlingListener) {
        mOnFlingListener = onFlingListener;
    }

}

class TouchableView {
    public CellView touchingView;
    public float centerX;
    public float centerY;
    public int touchingViewPosRow;
    public int touchingViewPosCol;
    public int pos;
    public int wid;
    public int hei;
}

interface OnCellViewScrollListener {
    void onCellViewScroll (float x, float y);

    CellView onCellViewScrollToEdge (boolean foward);

    boolean isScrollToEdgOpFinished ();
}

interface OnNoSelectedViewScrollListener {
    void onNoSelectedViewScroll(float distanceX);
}

interface OnCellViewInsertedListener {
    void onCellViewInserted (int row, int col);
}

interface OnFlingListener {
    void onFling (float y);
}

class Point {
    int x;
    int y;
}