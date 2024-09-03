package com.example.dogelauncher.utils;



import android.util.Log;

import com.example.dogelauncher.view.SurroundingView;

public class CalculateUtil {
    private static final String TAG = "CalculateUtil";

    //圆外接方直径长度
    public static long CIRCLE_MAX_SIZE = SurroundingView.ICON_MAX_SIZE + SurroundingView.maxRadius * 2;



    public static int calculateAppSize (int expectNum, long radius) {
        if (expectNum == 0) {
            Log.e(TAG, "calculateAppSize: no app should be measure and layout" );
        }
        if (expectNum == 1) {
            return SurroundingView.ICON_MAX_SIZE;
        }
        //计算角度
        float angle =  360f / expectNum;

        //计算每个app的distX distY
        // 2 * R * sin(θ/2) 计算弦长
        double angleInRadians = Math.toRadians(angle);// 将角度从度转换为弧度
        double chordLength = 2 * radius * Math.sin(angleInRadians / 2); //也就是直线距离
        chordLength += SurroundingView.ICON_MIN_MARGIN; //加上这个间隔
        //icon 对角线长 应该小于 chordLength
        if (1.414 *  SurroundingView.ICON_MAX_SIZE <= chordLength) {
            return SurroundingView.ICON_MAX_SIZE;
        } else {
            return (int)chordLength/2;
        }
    }

}
