package com.example.dogelauncher.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.example.dogelauncher.app.DogeApp;
import com.example.dogelauncher.model.AppData;

import java.util.List;

public class CellView extends ViewGroup {

    private static final String TAG = "CellView";

    //默认为1
    public int row = 1;
    public int col = 1;

    private static int SIZE_PER_OUTER_GRID = 180 / DogeApp.dpiTimes; // 160为每个内部格子
    private static int SIZE_PER_INNER_GRID = 160 / DogeApp.dpiTimes;

    public static final int ICON_MARGIN = 10 / DogeApp.dpiTimes;

    private boolean showBg;

    public static final int ICON_TAG_SINGLE = 0;
    public static final int ICON_TAG_GROUP = 1;
    public int iconTag = ICON_TAG_SINGLE;




    private AppData appData;
    private List<AppData> appDatas;

    private boolean[][] pos;

    public CellView (Context context) {
        super(context);
    }

    public CellView (Context context,AttributeSet attrs) {
        super(context,attrs);
    }

    public CellView(Context context, AppData appData) {
        super(context);
        iconTag = ICON_TAG_SINGLE;
        this.appData = appData;
        row = 1;
        col = 1;
        addView(new IconView(context, appData.getIcon()));
        init();
    }

    public CellView(Context context, List<AppData> appDatas) {
        super(context);
        iconTag = ICON_TAG_GROUP;
        this.appDatas = appDatas;
        row = 2;
        col = 2;
        init();
    }


    public CellView(Context context, int row, int col) {
        super(context);
        iconTag = ICON_TAG_GROUP;
        this.row = row;
        this.col = col;
        init();
    }


    private void init() {
        // 设置背景，带透明度和圆角
        pos = new boolean[col][row];
        showBg();
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
        int widMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widMode == MeasureSpec.UNSPECIFIED || widMode == MeasureSpec.AT_MOST) {
            widSpec = MeasureSpec.makeMeasureSpec(col * SIZE_PER_OUTER_GRID, MeasureSpec.EXACTLY);
        } else {
            widSpec = widthMeasureSpec;
        }

        int heiSpec;
        int heiMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heiMode == MeasureSpec.UNSPECIFIED || heiMode == MeasureSpec.AT_MOST) {
            heiSpec = MeasureSpec.makeMeasureSpec(row * SIZE_PER_OUTER_GRID, MeasureSpec.EXACTLY);
        } else {
            heiSpec = heightMeasureSpec;
        }

        setMeasuredDimension(widSpec, heiSpec);


        int perGridSizeSpec = MeasureSpec.makeMeasureSpec(SIZE_PER_INNER_GRID, MeasureSpec.EXACTLY);

        switch (iconTag) {
            case ICON_TAG_SINGLE:
                View child = getChildAt(0);
                if (child != null) {
                    child.measure(widSpec, heiSpec);
                }
                break;
            case ICON_TAG_GROUP:
                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childView = getChildAt(i);
                    if (childView instanceof IconView) {
                        childView.measure(perGridSizeSpec, perGridSizeSpec);
                    }// 其余组件类型的可能要换测量大小
                    else if (childView instanceof CellView) {
                        CellView cellView = (CellView) childView;
                        childView.measure(MeasureSpec.makeMeasureSpec(cellView.col * SIZE_PER_OUTER_GRID, MeasureSpec.EXACTLY ),
                                MeasureSpec.makeMeasureSpec(cellView.row * SIZE_PER_OUTER_GRID,MeasureSpec.EXACTLY ));
                    } else {
                        //装的是控件，如时钟等 一个控件套满一个cellview
                        int wid = col * SIZE_PER_OUTER_GRID - 2 * ICON_MARGIN;
                        int hei = row * SIZE_PER_OUTER_GRID - 2 * ICON_MARGIN;
                        childView.measure(MeasureSpec.makeMeasureSpec(wid, MeasureSpec.EXACTLY ), MeasureSpec.makeMeasureSpec(hei, MeasureSpec.EXACTLY ));
                    }
                }
                break;
        }

    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        switch (iconTag) {
            case ICON_TAG_SINGLE:
                View child = getChildAt(0);
                if (child != null) {
                    int measuredWidth = child.getMeasuredWidth();
                    int measuredHeight = child.getMeasuredHeight();

                    child.layout(l, t, l+measuredWidth, t+measuredHeight);
                }
                break;
            case ICON_TAG_GROUP:
                int childCount = getChildCount();

                int curRow = 0;
                int curCol = 0;

                for (int i = 0; i < 4 && i < childCount; i++) {
                    View childView = getChildAt(i);
                    if (childView instanceof IconView) {
                        if (curRow + 1 >= row) {
                            //放不下去了
                            return;
                        }
                        while (curCol + 1 >= col && curRow < row) {
                            curRow += 1;
                            curCol = 0;
                        }
                        if (curRow >= row) {
                            //放不下了
                            break;
                        } else {
                            int childL = l + curCol * SIZE_PER_OUTER_GRID + ICON_MARGIN;
                            int childT = t + curRow * SIZE_PER_OUTER_GRID + ICON_MARGIN;
                            int childR = childL + SIZE_PER_INNER_GRID;
                            int childB = childT + SIZE_PER_INNER_GRID;
                            childView.layout(childL, childT, childR, childB);
                            pos[curRow] [curCol] = true;
                            curCol += 1;
                        }
                    } else if (childView instanceof CellView) {
                        //为了方便 内部只允许cellView为正方形
                        CellView cellView = (CellView) childView;

                        boolean found = false;
                        while (!found) {
                            if (curRow + cellView.row >= row) {
                                //放不下去了
                                break;
                            }

                            while (curCol + cellView.col < col && !canLayout (curRow, curCol, cellView.col)) {
                                curCol ++;
                            }

                            if (curCol + cellView.col >= col) {
                                curRow += 1;
                                curCol = 0;
                            } else {
                                //can layout
                                int childL = l + curCol * SIZE_PER_OUTER_GRID;
                                int childT = t + curRow * SIZE_PER_OUTER_GRID;
                                int childR = childL + cellView.col * SIZE_PER_OUTER_GRID;
                                int childB = childT + cellView.row * SIZE_PER_OUTER_GRID;
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
                        int childL = l + ICON_MARGIN;
                        int childT = t * ICON_MARGIN;
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

    private boolean canLayout (int r, int c, int size) {
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

}
