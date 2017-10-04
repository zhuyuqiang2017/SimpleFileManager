package com.android.simplefilemanager.list;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.simplefilemanager.R;

import java.io.File;

import static com.android.simplefilemanager.utils.FileUtil.getColorResource;
import static com.android.simplefilemanager.utils.FileUtil.getImageResource;
import static com.android.simplefilemanager.utils.FileUtil.getLastModified;
import static com.android.simplefilemanager.utils.FileUtil.getName;
import static com.android.simplefilemanager.utils.FileUtil.getSize;

/**
 * Created by Administrator on 2017/10/1 0001.
 */

public final class CommonViewHolder extends ViewHolder{
    private TextView mTitle;
    private TextView mData;
    public CommonViewHolder(Context context,View itemView,OnViewHolderClickListener listener) {
        super(context,itemView,listener);
    }

    @Override
    protected void loadIcon() {
        mIcon = (ImageView) itemView.findViewById(R.id.list_item_image);
    }

    @Override
    protected void loadName() {
        mTitle = (TextView) itemView.findViewById(R.id.list_item_name);
    }

    @Override
    protected void loadInfo() {
        mData = (TextView) itemView.findViewById(R.id.list_item_date);
        mSelectIcon = (ImageView)itemView.findViewById(R.id.list_item_select);
    }

    @Override
    protected void bindIcon(File file) {
        int color = ContextCompat.getColor(mContext, getColorResource(file));
        mIcon.setBackground(null);
        Drawable drawable = ContextCompat.getDrawable(mContext, getImageResource(file));
        DrawableCompat.setTint(drawable, color);
        mIcon.setImageDrawable(drawable);
    }

    @Override
    protected void bindName(File file) {
        if(mTitle != null){
            String title = getName(file);
            mTitle.setText(title);
        }
    }

    @Override
    protected void bindInfo(File file) {
        mData.setText(getLastModified(file));
    }
}
