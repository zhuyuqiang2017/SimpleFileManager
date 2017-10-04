package com.android.simplefilemanager.utils;

/**
 * Created by Administrator on 2017/10/2 0002.
 */

public class LogUtil {
    private static final String TAG = "zyq";
    private static boolean mDebug = true;

    public static void e(String message){
        if(mDebug){
            android.util.Log.e(TAG,message);
        }
    }
}
