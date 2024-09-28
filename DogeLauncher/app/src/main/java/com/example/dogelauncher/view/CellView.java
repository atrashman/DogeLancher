package com.example.dogelauncher.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.BoolRes;
import androidx.annotation.NonNull;

import com.example.dogelauncher.R;
import com.example.dogelauncher.app.DogeApp;
import com.example.dogelauncher.model.AppData;
import com.example.dogelauncher.utils.CalculateUtil;

import java.util.HashSet;
import java.util.List;

public class CellView extends ViewGroup implements OnCellViewScrollListener {

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
    private List<AppData> appDatas;

    private boolean[][] pos;
    private PopupMenu popupMenu;

    public CellView (Context context) {
        super(context);
    }

    public CellView (Context context,AttributeSet attrs) {
        super(context,attrs);
        iconTag = ICON_TAG_LIST;
        setClipChildren(true);
    }

    public CellView(Context context, AppData appData) {
        super(context);
        iconTag = ICON_TAG_SINGLE;
        this.appData = appData;
        row = 1;
        col = 1;
        addView(new IconView(context, appData.getIcon()));
        touchable = false;
        init();
    }

    public CellView(Context context, List<AppData> appDatas) {
        super(context);
        iconTag = ICON_TAG_LIST;
        this.appDatas = appDatas;
        row = 2;
        col = 2;
        init();
    }


    public CellView(Context context, int row, int col) {
        super(context);
        iconTag = ICON_TAG_LIST;
        this.row = row;
        this.col = col;
        init();
    }


    private TouchableView[][] pos2TouchableView;
    public void init() {
        // 设置背景，带透明度和圆角
        pos = new boolean[row][col];
        showBg();
    }

    public void createPosInfo () {
        pos2TouchableView = new TouchableView[row][col];
        int childCount = getChildCount();

        int curRow = 0;
        int curCol = 0;
        for (int i = 0;i < childCount;i ++) {
            CellView cellView = (CellView)getChildAt(i);
            TouchableView touchableView = new TouchableView();
            touchableView.touchingView = cellView;

            boolean found = false;
            while (!found) {
                if (curRow + cellView.row - 1 >= row) {
                    //放不下去了
                    throw new RuntimeException("摆放不合理！！！！");
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
                    setPos(curRow, curCol, cellView.col, pos2TouchableView, touchableView);
                    curCol += cellView.col;
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
                Log.e(TAG, "onMeasure: child ="+ child );
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
        int perWidSize = measuredWidth / col;
        int perHeiSize = measuredHeight / row;

        switch (iconTag) {
            case ICON_TAG_SINGLE:
                View child = getChildAt(0);
                if (child != null) {
                    //child.layout的参数是对父来说的
                    child.layout(0, 0,  measuredWidth, measuredHeight);
                }
                break;
            case ICON_TAG_LIST:
                int childCount = getChildCount();

                int curRow = 0;
                int curCol = 0;

                for (int i = 0; i < childCount; i++) {
                    View childView = getChildAt(i);
//                    if (childView instanceof IconView) {
//                        if (curRow + 1 >= row) {
//                            //放不下去了
//                            return;
//                        }
//                        while (curCol + 1 >= col && curRow < row) {
//                            curRow += 1;
//                            curCol = 0;
//                        }
//                        if (curRow >= row) {
//                            //放不下了
//                            break;
//                        } else {
//                            int childL = curCol * perWidSize + ICON_MARGIN;
//                            int childT = curRow * perHeiSize + ICON_MARGIN;
//                            int childR = childL + childView.getMeasuredWidth();
//                            int childB = childT + childView.getMeasuredHeight();
//                            childView.layout(childL, childT, childR, childB);
//                            pos[curRow] [curCol] = true;
//                            curCol += 1;
//                        }
//                    } else
                    if (childView instanceof CellView) {
                        CellView cellView = (CellView) childView;

                        boolean found = false;
                        while (!found) {
                            if (curRow + cellView.row - 1 >= row) {
                                //放不下去了
                                break;
                            }

                            while (curCol + cellView.col - 1 < col && !canLayout (curRow, curCol, cellView.col, pos)) {
                                curCol ++;
                            }

                            if (curCol + cellView.col - 1 >= col) {
                                curRow += 1;
                                curCol = 0;
                            } else {
                                //can layout
                                int childL = curCol * perWidSize + perWidSize / 4;
                                int childT = curRow * perHeiSize + perHeiSize / 4;
                                int childR = childL + childView.getMeasuredWidth();
                                int childB = childT + childView.getMeasuredHeight();
                                childView.layout(childL, childT, childR, childB);
                                setPos(curRow, curCol, cellView.col);
                                curCol += cellView.col;
                                found = true;
                            }
                        }
                        if (!found) {
                            Log.e(TAG, "onLayout: " + "cant layout (too large)" );
                        }
                    } else {
                        //单独的一个控件  cellview单独包裹一个控件 不管其他情况
                        int childL = 0;
                        int childT = 0;
                        int childR = childL + childView.getMeasuredWidth();
                        int childB = childT + childView.getMeasuredHeight();
                        childView.layout(childL, childT, childR, childB);
                    }
                }
                break;
        }
    }

    private void setPos(int curRow, int curCol, int size) {
        for (int i = curRow; i < curRow + size; i ++ ) {
            for (int j = curCol; j < curCol + size; j ++) {
                pos[i][j] = true;
            }
        }
    }

    private boolean canLayout (int r, int c, int size, boolean[][] pos) {
        int curR = r;
        int curC = c;

        while (curR < r + size) {
            while (curC < c + size) {
                if (pos[r][c]) return false;
                curC ++;
            }
            curR ++;
            curC = c;
        }
        return true;
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
    private void setPos(int curRow, int curCol, int size, TouchableView[][] pos, TouchableView view) {
        for (int i = curRow; i < curRow + size; i ++ ) {
            for (int j = curCol; j < curCol + size; j ++) {
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


    private GestureDetector gestureDetector = new GestureDetector(getContext(), );


    private class DragDetector extends GestureDetector.SimpleOnGestureListener {
        private final int minimumVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();

        private DragDetector() {

        }

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            switch (iconTag) {
                case ICON_TAG_SINGLE:
                    Toast.makeText(getContext(), "start app", Toast.LENGTH_SHORT).show();
                    break;
                case  ICON_TAG_LIST:
                    break;
            }
            return true;
        }


        @Override
        public void onLongPress(@NonNull MotionEvent e) {
            longPress = true;
            requestDisallowInterceptTouchEvent(true);
            if (curCellView != null) {
                //震动 弹出菜单
                popupMenu = new PopupMenu(getContext(), curCellView.touchingView);
                popupMenu.getMenuInflater().inflate(R.menu.icon_menu, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // 控件每一个item的点击事件
                        int itemId = item.getItemId();
                        if (itemId == R.id.edit) {
                            Toast.makeText(getContext(), "edit", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.delete) {
                            Toast.makeText(getContext(), "delete", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
//            e1：表示手指 触摸屏幕时 的位置。
//            e2：表示手指 滑动过程中 的位置。
//            distanceX：表示手指在 水平方向上的滑动距离。
//            distanceY：表示手指在 垂直方向上的滑动距离。
            if (curCellView == null || !longPress) {
                if (mOnNoSelectedViewScrollListener != null) {
                    mOnNoSelectedViewScrollListener.onNoSelectedViewScroll(distanceX);
                }
            }
//
            switch (iconTag) {
                case ICON_TAG_SINGLE:
                    break;
                case ICON_TAG_LIST:
                    if (distanceX + distanceY >= ) {
                        popupMenu.dismiss();
                    }
                    break;
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


    private TouchableView curCellView = null;
    private boolean longPress = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!touchable) return true;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                curCellView = getTouchingViewFromDownEvent(ev.getX(), ev.getY());
                return true;
        }

        return true;
    }


    public TouchableView getTouchingViewFromDownEvent (float x, float y) {
        HashSet<TouchableView> searched = new HashSet<>();
        for (int i =0; i< pos2TouchableView.length; i++) {
            for (int j = 0; j < pos2TouchableView[0].length; j ++) {
                TouchableView touchableView = pos2TouchableView[i][j];
                if (searched.contains(touchableView)) continue;
                boolean yOk = Math.abs(touchableView.centerY - y) < touchableView.touchingView.getMeasuredHeight() / 2l ;
                boolean xOk = Math.abs(touchableView.centerX - x) < touchableView.touchingView.getMeasuredWidth() / 2l;
                if (xOk && yOk) curCellView = touchableView;
                else searched.add(touchableView);
            }
        }
        return curCellView;
    }



    private OnCellViewScrollListener  mOnCellViewScrollListener;
    public void setOnCellViewScrollListener (OnCellViewScrollListener onCellViewScrollListener) {
        this.mOnCellViewScrollListener = onCellViewScrollListener;
    }
    @Override
    public void onCellViewScroll(int x) {}

    private OnNoSelectedViewScrollListener mOnNoSelectedViewScrollListener;
    public void setOnNoSelectedViewScrollListener (OnNoSelectedViewScrollListener onNoSelectedViewScrollListener) {
        mOnNoSelectedViewScrollListener = onNoSelectedViewScrollListener;
    }

}

class TouchableView {
    public CellView touchingView;
    public float centerX;
    public float centerY;
    public int touchingViewPosRow;
    public int touchingViewPosCol;
    public int pos;
}

interface OnCellViewScrollListener {
    void onCellViewScroll (int y);
}

interface OnNoSelectedViewScrollListener {
    void onNoSelectedViewScroll(float distanceX);
}

interface OnCellViewInsertedListener {
    void onCellViewInserted (int row, int col);
}
class Point {
    int x;
    int y;
}