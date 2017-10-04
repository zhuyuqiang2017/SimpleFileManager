package com.android.simplefilemanager.list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.android.simplefilemanager.R;

import java.io.File;

/**
 * Created by Administrator on 2017/10/1 0001.
 */

public abstract class ViewHolder extends RecyclerView.ViewHolder {

    public interface OnViewHolderClickListener{
        void onClick(int position);
        boolean onLongClick(int position);
    }
    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;
    private OnViewHolderClickListener mListener;
    protected ImageView mIcon;
    protected ImageView mSelectIcon;
    protected final Context mContext;

    public ViewHolder(Context context,View itemView,OnViewHolderClickListener listener) {
        super(itemView);
        this.mContext = context;
        this.mListener = listener;
        loadIcon();
        loadInfo();
        loadName();
        applyListener();
    }

    protected abstract void loadIcon();

    protected abstract void loadName();

    protected abstract void loadInfo();

    protected abstract void bindIcon(File file);

    protected abstract void bindName(File file);

    protected abstract void bindInfo(File file);

    private void applyListener(){
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.onClick(getAdapterPosition());
                }
            }
        };
        mOnLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mListener != null){
                    return mListener.onLongClick(getAdapterPosition());
                }
                return false;
            }
        };
    }

    void setData(final File file, Boolean selected,boolean canSelectable) {

//        itemView.setSelected(selected);
        if(canSelectable){
            mSelectIcon.setVisibility(View.VISIBLE);
        }else{
            mSelectIcon.setVisibility(View.INVISIBLE);
        }
        mSelectIcon.setImageDrawable(selected?mContext.getDrawable(R.drawable.ic_selected):
                mContext.getDrawable(R.drawable.ic_unselected));
        itemView.setOnClickListener(mOnClickListener);
        itemView.setOnLongClickListener(mOnLongClickListener);
        bindIcon(file);
        bindName(file);
        bindInfo(file);
    }
}
