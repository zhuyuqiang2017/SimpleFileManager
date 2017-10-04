package com.android.simplefilemanager.ui;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v7.app.AlertDialog;

import com.android.simplefilemanager.R;
import com.android.simplefilemanager.utils.LogUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/10/3 0003.
 */

public class DialogFactory implements View.OnClickListener{
    private Context mContext;
    private View mDialogView;
    private Button mCancel;
    private Button mEnsure;
    private TextView mTitle;
    private LinearLayout mTitleContainer;
    private TextView mContent;
//    private EditText mDialogEdit;
    private TextInputLayout mEditContainer;
    private String FILE_NAME = "^[0-9a-zA-Z_]+(\\.)?[a-zA-Z]+$";
    private Pattern mPattern = Pattern.compile(FILE_NAME);
    private Matcher mMatcher;
    private AlertDialog.Builder mBuilder = null;
    private AlertDialog mDialog;
    private int mColor;
    private CountDownTimer mTimer = new CountDownTimer(5000,1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            if(mEditContainer != null){
                mEditContainer.setErrorEnabled(false);
            }
        }
    };
    public interface OnDialogButtonListener{
        void onEnsureClicked(String fileName);
        void onCancelClicked();
    }
    private OnDialogButtonListener mListener;

    public DialogFactory(Context context){
        this.mContext = context;
        mDialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_layout,null);
        mTitle = (TextView) mDialogView.findViewById(R.id.dialog_title);
        mTitleContainer = (LinearLayout) mDialogView.findViewById(R.id.dialog_title_container);
        mEditContainer = (TextInputLayout) mDialogView.findViewById(R.id.dialog_input_layout);
        mCancel = (Button) mDialogView.findViewById(R.id.dialog_cancel);
        mEnsure = (Button) mDialogView.findViewById(R.id.dialog_ensure);
        mContent = (TextView)mDialogView.findViewById(R.id.dialog_content);
        if(mBuilder == null){
            mBuilder = new AlertDialog.Builder(mContext);
            mBuilder.setView(mDialogView);
        }
        mColor = mContext.getResources().getColor(R.color.color_main);
    }

    public AlertDialog getDialog(ACTIONS type){
        updateDialogContent(type);
        if (mDialog == null){
            mDialog = mBuilder.create();
        }
        return mDialog;
    }

    public void updateDialogContent(ACTIONS type){
        if(mBuilder == null){
            mBuilder = new AlertDialog.Builder(mContext);
            mBuilder.setView(mDialogView);
        }
        mTitle.setText(getDialogTitle(type));
        mTitleContainer.setBackgroundColor(mColor);
        switch (type){
            case ACTION_CREATE:
            case ACTION_RENAME:
            case ACTION_SEARCH:
                mContent.setVisibility(View.GONE);
                mEditContainer.setVisibility(View.VISIBLE);
                mEditContainer.setErrorEnabled(true);
                mEditContainer.setHintEnabled(true);
                mEditContainer.setHint(getDialogMessage(type));
                break;
            case ACTION_COPY:
            case ACTION_DELETE:
            case ACTION_MOVE:
            case ACTION_SEND:
                mContent.setVisibility(View.VISIBLE);
                mContent.setText(getDialogMessage(type));
                mEditContainer.setVisibility(View.GONE);
                mEditContainer.setErrorEnabled(false);
                mEditContainer.setHint(getDialogMessage(type));
                break;
        }
        mCancel.setOnClickListener(this);
        mEnsure.setOnClickListener(this);
    }

    public void setOnDialogButtonListener(OnDialogButtonListener listener){
        this.mListener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dialog_cancel:
                if(mListener != null){
                    mListener.onCancelClicked();
                }
                break;
            case R.id.dialog_ensure:
                if(mListener != null){
                    if(mEditContainer!= null && mEditContainer.getVisibility() == View.VISIBLE){
                        mMatcher = mPattern.matcher(mEditContainer.getEditText().getText().toString().trim());
                        if(mMatcher.matches()){
                            mListener.onEnsureClicked(mEditContainer.getEditText().getText().toString());
                            mEditContainer.getEditText().setText("");
                        }else{
                            mEditContainer.setError(mContent.getResources().getString(R.string.remind_file_name));
                            mTimer.start();
                        }
                    }else{
                        mListener.onEnsureClicked("");
                    }
                }
                break;
        }
    }

    public void setTitleBackground(int color){
        mColor = color;
    }


    private int getDialogTitle(final ACTIONS type){
        switch (type){
            case ACTION_CREATE:
                return R.string.action_create;
            case ACTION_RENAME:
                return R.string.action_rename;
            case ACTION_SEARCH:
                return R.string.action_search;
            case ACTIONS_PASTE:
                return R.string.action_copy;
            case ACTION_DELETE:
                return R.string.action_delete;
            case ACTION_MOVE:
                return R.string.action_move;
            case ACTION_SEND:
                return R.string.action_send;
            case ACTION_COPY:
                return R.string.action_copy;
        }
        return -1;
    }

    private CharSequence getDialogMessage(final ACTIONS type){
        switch (type){
            case ACTION_CREATE:
                return mContext.getString(R.string.action_create_message);
            case ACTION_RENAME:
                return mContext.getString(R.string.action_rename_message);
            case ACTION_SEARCH:
                return mContext.getString(R.string.action_search_message);
            case ACTIONS_PASTE:
                return mContext.getString(R.string.action_paste_message);
            case ACTION_DELETE:
                return mContext.getString(R.string.action_delete_message);
            case ACTION_MOVE:
                return mContext.getString(R.string.action_move_message);
            case ACTION_COPY:
                return mContext.getString(R.string.action_copy_message);
            case ACTION_SEND:
                return mContext.getString(R.string.action_send_message);
        }
        return "";
    }


    public enum ACTIONS{
        ACTION_CREATE,
        ACTION_RENAME,
        ACTION_DELETE,
        ACTION_MOVE,
        ACTION_COPY,
        ACTION_SEND,
        ACTION_SEARCH,
        ACTIONS_PASTE
    }
}
