package com.android.simplefilemanager.utils;

/**
 * Created by Administrator on 2017/10/1 0001.
 */

public class JNIUtil {

    static {
        System.loadLibrary("native-lib");
    }

    public static native String stringFromJNI();
}
