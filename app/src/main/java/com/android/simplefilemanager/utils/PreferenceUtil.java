package com.android.simplefilemanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2017/10/1 0001.
 */

public class PreferenceUtil {
    public final String KEY_SHOW_FILE_SIZE = "key_show_file_size";
    public final String KEY_SHOW_FILE_DATA = "key_show_file_data";
    /*public final String KEY_SHOW_FILE_SIZE = "key_show_file_size";
    public final String KEY_SHOW_FILE_SIZE = "key_show_file_size";
    public final String KEY_SHOW_FILE_SIZE = "key_show_file_size";
    public final String KEY_SHOW_FILE_SIZE = "key_show_file_size";
    public final String KEY_SHOW_FILE_SIZE = "key_show_file_size";*/

    private PreferenceUtil mUtil = new PreferenceUtil();
    private static SharedPreferences mSharedPreferences;
    private static SharedPreferences.Editor mEditor;

    private PreferenceUtil(){
    }
    private static void initPreferenceUtil(Context context){
        if(mSharedPreferences != null){
            mSharedPreferences = context.getSharedPreferences(context.getPackageName(),Context.MODE_PRIVATE);
            mEditor = mSharedPreferences.edit();
        }
    }

    public static boolean setPropertyForManager(Context context,String key,int value){
        if(mSharedPreferences != null){
           initPreferenceUtil(context);
        }
        if(mEditor != null){
            return mEditor.putInt(key,value).commit();
        }
        return false;
    }
    public static boolean setPropertyForManager(Context context,String key,boolean value){
        if(mSharedPreferences != null){
            initPreferenceUtil(context);
        }
        if(mEditor != null){
            return mEditor.putBoolean(key,value).commit();
        }
        return false;
    }
    public static boolean setPropertyForManager(Context context,String key,String value){
        if(mSharedPreferences != null){
            initPreferenceUtil(context);
        }
        if(mEditor != null){
            return mEditor.putString(key,value).commit();
        }
        return false;
    }

    public static int getIntProperty(Context context,String key){
        if(mSharedPreferences != null){
            initPreferenceUtil(context);
        }
        return mSharedPreferences.getInt(key,-1);
    }

    public static boolean getBolleanProperty(Context context,String key){
        if(mSharedPreferences != null){
            initPreferenceUtil(context);
        }
        return mSharedPreferences.getBoolean(key,false);
    }

    public static String getStringProperty(Context context,String key){
        if(mSharedPreferences != null){
            initPreferenceUtil(context);
        }
        return mSharedPreferences.getString(key,"");
    }


}
