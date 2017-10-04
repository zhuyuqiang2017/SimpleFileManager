package com.android.simplefilemanager.list;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;

import com.android.simplefilemanager.utils.FileUtil;

import java.io.File;

/**
 * Created by Administrator on 2017/10/1 0001.
 */

public class FileCallBack extends SortedListAdapterCallback<File>{

    private int criteria;
    public FileCallBack(RecyclerView.Adapter adapter) {
        super(adapter);
        criteria = 0;
    }

    @Override
    public int compare(File file1, File file2) {
        boolean isDirectory1 = file1.isDirectory();

        boolean isDirectory2 = file2.isDirectory();

        if (isDirectory1 != isDirectory2) return isDirectory1 ? -1 : +1;

        switch (criteria) {

            case 0:
                return FileUtil.compareName(file1, file2);

            case 1:
                return FileUtil.compareDate(file1, file2);

            case 2:
                return FileUtil.compareSize(file1, file2);

            default:
                return 0;
        }
    }

    @Override
    public boolean areContentsTheSame(File oldItem, File newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areItemsTheSame(File item1, File item2) {
        return item1.equals(item2);
    }
}
