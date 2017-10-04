package com.android.simplefilemanager.list;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.ForwardingListener;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.simplefilemanager.R;
import com.android.simplefilemanager.utils.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/10/1 0001.
 */

public class FileAdapter extends RecyclerView.Adapter<CommonViewHolder> {

    private final SortedList<File> items;
    private Context mContext;
    private final FileCallBack mCallBack;
    private boolean mCanSelected = false;
    private final SparseBooleanArray mSelectedItems;
    private ViewHolder.OnViewHolderClickListener mOnViewHolderClickListener;
    private boolean copyToPaste = false;
    private boolean moveToPaste = false;
    private int mSelectedFileCount = 0;
    private final ArrayList<File> mCopyOrMoveItems;
    public FileAdapter(Context context, ViewHolder.OnViewHolderClickListener mListener){
        this.mContext = context;
        this.mCallBack = new FileCallBack(this);
        this.items = new SortedList<>(File.class,mCallBack);
        this.mSelectedItems = new SparseBooleanArray();
        this.mCopyOrMoveItems = new ArrayList();
        mOnViewHolderClickListener = mListener;
    }
    @Override
    public CommonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewItem = LayoutInflater.from(mContext).inflate(R.layout.item_layout_test,parent,false);
        return new CommonViewHolder(mContext,viewItem,mOnViewHolderClickListener);
    }

    @Override
    public void onBindViewHolder(CommonViewHolder holder, int position) {
        holder.setData(items.get(position),mSelectedItems.get(position),mCanSelected);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void add(File file) {

        items.add(file);
    }

    public void addAll(File... files) {
        items.addAll(files);
    }

    public void addAll(Collection<File> files) {
        items.addAll(files);
    }

    public void removeAllFile(){
        if (items != null){
            items.clear();
        }
    }

    public void removeAllFile(final ArrayList<File> files){
        if (items != null && files!= null && files.size()>0){
            for (File f : files){
                items.remove(f);
            }
        }
    }

    public void removeFile(int position){
        items.removeItemAt(position);
    }

    public void removeFile(File file){
        items.remove(file);
    }

    public void refresh() {
        for (int i = 0; i < getItemCount(); i++) {
            notifyItemChanged(i);
        }
    }

    public int getCurrentDirectoryItemCount(){
        if(items != null){
            return items.size();
        }
        return -1;
    }

    public File getFileByPosition(int position){
        if(position>=0 && position<items.size()){
            return items.get(position);
        }
        return null;
    };

    public boolean canSelectable(){
        return mCanSelected;
    }

    public void setToggle(int position){
        boolean lastValue = mSelectedItems.get(position,false);
        mSelectedItems.delete(position);
        if(!lastValue){
            mSelectedFileCount ++;
        }else{
            mSelectedFileCount--;
        }
        mSelectedItems.append(position,!lastValue);
        notifyItemChanged(position);
    }
    public void setToggle(ArrayList<Integer> positions){
            mSelectedItems.clear();
            for (int i : positions) {
                mSelectedItems.append(i, true);
                notifyItemChanged(i);
            }
    }

    public void setCanSelectable(){
        this.mCanSelected = !mCanSelected;
        refresh();
    }

    public void setAllItemSelected(){
        if (mSelectedItems != null){
            for (int i = 0;i<getItemCount();i++){
                mSelectedItems.delete(i);
                mSelectedItems.append(i,true);
                notifyItemChanged(i);
            }
        }
    }

    public void clearAllItemSelected(){
        if (mSelectedItems != null){
            for (int i = 0;i<getItemCount();i++){
                mSelectedItems.delete(i);
                mSelectedItems.append(i,false);
                notifyItemChanged(i);
            }
        }
        mSelectedFileCount = 0;
    }

    public boolean getSelected(int position){
        return mSelectedItems.get(position,false);
    }

    public ArrayList<File> getSelectedItems() {
        ArrayList<File> list = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {

            if (getSelected(i)) {
                list.add(items.get(i));
            }
        }
        return list;
    }

    public int getSelectedFileCount(){
        return mSelectedFileCount;
    }

    public ArrayList<Integer> getSelectedPosition(){
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0;i<getItemCount();i++){
            if(mSelectedItems.get(i,false)){
                arrayList.add(i);
            }
        }
        return arrayList;
    }

    public void clearSelection() {
        ArrayList<Integer> selectedPositions = getSelectedPosition();
        mSelectedItems.clear();
        for (int i : selectedPositions) notifyItemChanged(i);

    }

    public void updateItemAt(int index, File file) {

        items.updateItemAt(index, file);
    }

    public int indexOfFile(File file){
        return items.indexOf(file);
    }

    public boolean getCopyToPaste(){
        return copyToPaste;
    }
    public boolean getMoveToPaste(){
        return moveToPaste;
    }

    public void setCopyToPaste(boolean flag){
        this.copyToPaste = flag;
    }

    public void setMoveToPaste(boolean flag){
        this.moveToPaste = flag;
    }

    public void setCopyOrMoveFiles(final List<File> files){
        if(files != null){
            for (File f : files){
                LogUtil.e("add file to mCopyOrMoveItems");
                mCopyOrMoveItems.add(f);
            }
        }
    }

    public ArrayList<File> getCopyOrMoveFiles(){
        return mCopyOrMoveItems;
    }

    public void clearCopyOrMoveFiles(){
        mCopyOrMoveItems.clear();
    }
}
