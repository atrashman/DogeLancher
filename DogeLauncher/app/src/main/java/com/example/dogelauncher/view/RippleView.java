package com.example.dogelauncher.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.dogelauncher.R;

import java.util.ArrayList;
import java.util.List;

public class RippleView extends View {

    private static final String TAG = "RippleView";

    private Paint centerPaint; //中心圆paint
    private int radius = 100; //中心圆半径
    private Paint spreadPaint; //扩散圆paint
    private float centerX;//圆心x
    private float centerY;//圆心y
    private int distance = 5; //每次圆递增间距
    private int maxRadius = 80; //最大圆半径
    private int delayMilliseconds = 33;//扩散延迟间隔，越大扩散越慢

    private int repeatCount = 3;
    private int rippleCircleNum = 5;

    private List<Integer> spreadRadius = new ArrayList<> (rippleCircleNum);//扩散圆层级数，元素为扩散的距离
    private List<Integer>  alphas = new ArrayList<>(rippleCircleNum);//对应每层圆的透明度


    public RippleView(Context context) {
        super(context);
    }

    public RippleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RippleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SpreadView, defStyleAttr, 0);
        radius = a.getInt(R.styleable.SpreadView_spread_radius, radius);
        maxRadius = a.getInt(R.styleable.SpreadView_spread_max_radius, maxRadius);
        int spreadColor = a.getColor(R.styleable.SpreadView_spread_spread_color, ContextCompat.getColor(context, R.color.purple_200));
        distance = a.getInt(R.styleable.SpreadView_spread_distance, distance);
        rippleCircleNum = a.getInt(R.styleable.SpreadView_spread_ripple_circle_num, rippleCircleNum);
        a.recycle();
//        centerPaint = new Paint();
//        centerPaint.setColor(centerColor);
//        centerPaint.setAntiAlias(true);
        //最开始不透明且扩散距离为0
        alphas.add(255);
        for (int i =0;i<rippleCircleNum;i++){
            spreadRadius.add(0);
            alphas.add(255);
        }

        spreadPaint = new Paint();
        spreadPaint.setAntiAlias(true);
        spreadPaint.setAlpha(255);
        spreadPaint.setColor(spreadColor);
    }

    public void setRepeatCount(int num) {
        //最外圈波纹消散num次
        //-1 表示永久持续
        this.repeatCount = num;
    }


    //ondraw前会执行这个
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //圆心位置
        centerX = w / 2;
        centerY = h / 2;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < spreadRadius.size(); i++) {
            if (i == 0) {
                spreadRadius.set(i, (spreadRadius.get(i) + distance) % maxRadius);

            } else {
                //上一个圆走到maxRadius的一半，这个圆才开始动弹！
//                if(Math.abs(spreadRadius.get(i-1) - spreadRadius.get(i)) > maxRadius/2 ){
//                    spreadRadius.set(i, (spreadRadius.get(i) + distance) % maxRadius);
//                }
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
