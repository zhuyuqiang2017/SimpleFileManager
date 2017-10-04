package com.android.simplefilemanager.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.simplefilemanager.R;
import com.android.simplefilemanager.list.FileAdapter;
import com.android.simplefilemanager.list.ViewHolder;
import com.android.simplefilemanager.ui.DialogFactory;
import com.android.simplefilemanager.utils.FileUtil;
import com.android.simplefilemanager.utils.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.android.simplefilemanager.utils.FileUtil.getExternalStorage;
import static com.android.simplefilemanager.utils.FileUtil.getInternalStorage;
import static com.android.simplefilemanager.utils.FileUtil.getMimeType;
import static com.android.simplefilemanager.utils.FileUtil.getPublicDirectory;
import static com.android.simplefilemanager.utils.FileUtil.getStorageUsage;

public class MainActivity extends AppCompatActivity {

    private static final String SAVED_DIRECTORY = "com.android.simplefilemanager.SAVED_DIRECTORY";
    private static final String SAVED_SELECTION = "com.android.simplefilemanager.SAVED_SELECTION";
    private static final String EXTRA_NAME = "com.android.simplefilemanager.EXTRA_NAME";
    private static final String EXTRA_TYPE = "com.android.simplefilemanager.EXTRA_TYPE";
    private Toolbar mToolBar;
    private FloatingActionButton mFloatButton;
    private AppBarLayout mAppBarLayout;
//    private android.support.v7.app.AlertDialog mDialog;
    private CoordinatorLayout mCoordinatorLayout;
    private CollapsingToolbarLayout mCollapsing;
    private DrawerLayout mDrawerLayout;
    private RecyclerView mRecyclerView;
    private NavigationView mNavigationView;
    private FileAdapter mAdapter;
    private File mCurrentFile;
    private String mCurrentPath;
    private final int REQUEST_PERMISSION_CODE = 98;
    private String mType;
    private DialogFactory mDialogFactory;
    boolean hasFileSelected = false;
    boolean isOnlyFileSelected = false;
    boolean canUsePaste = false;
    private final int UPDATE_APP_MENU = 98;
    private AlertDialog mDialog = null;
    private boolean cancelFinish = false;
    private ContentResolver mResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initActivityFromIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDialogFactory = new DialogFactory(this);
        mResolver = getContentResolver();
        initUI();
        initRecyclerView();
        initNavigationView();

    }

    private void initUI() {
        mCollapsing = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        mToolBar = (Toolbar) findViewById(R.id.tool_bar);
        mFloatButton = (FloatingActionButton)findViewById(R.id.floating_action_button);
        mFloatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionCreate();
            }
        });
        setSupportActionBar(mToolBar);
        if (mType != null && !("".equals(mType))) {
            switch (mType) {
                case "AUDIO":
                    mCollapsing.setTitle(getString(R.string.navigation_audio));
                    break;
                case "IMAGE":
                    mCollapsing.setTitle(getString(R.string.navigation_image));
                    break;
                case "VIDEO":
                    mCollapsing.setTitle(getString(R.string.navigation_video));
                    break;
            }
        } else {
            mCollapsing.setTitle(getString(R.string.app_name));
        }
    }

    private void initNavigationView() {
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (mNavigationView == null) {
            return;
        }
        MenuItem menuItem = mNavigationView.getMenu().findItem(R.id.navigation_external);
        menuItem.setEnabled(getExternalStorage() != null);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_audio:
                        setType("AUDIO");
                        return true;

                    case R.id.navigation_image:
                        setType("IMAGE");
                        return true;

                    case R.id.navigation_video:
                        setType("VIDEO");
                        return true;

                    case R.id.navigation_feedback:
//                        gotoFeedback();
                        return true;

                    case R.id.navigation_settings:
//                        gotoSettings();
                        return true;

                    case R.id.navigation_directory_0:
                        setPath(getPublicDirectory("DCIM"));
                        return true;

                    case R.id.navigation_directory_1:
                        setPath(getPublicDirectory("Download"));
                        return true;

                    case R.id.navigation_directory_2:
                        setPath(getPublicDirectory("Movies"));
                        return true;

                    case R.id.navigation_directory_3:
                        setPath(getPublicDirectory("Music"));
                        return true;

                    case R.id.navigation_directory_4:
                        setPath(getPublicDirectory("Pictures"));
                        return true;
                    default:
                        return true;
                }
            }
        });
        if (mType != null && !("".equals(mType))) {
            switch (mType) {
                case "AUDIO":
                    mNavigationView.getHeaderView(0).setBackgroundColor(getColor(R.color.color_audio));
                    break;
                case "IMAGE":
                    mNavigationView.getHeaderView(0).setBackgroundColor(getColor(R.color.color_image));
                    break;
                case "VIDEO":
                    mNavigationView.getHeaderView(0).setBackgroundColor(getColor(R.color.color_video));
                    break;
            }
        } else {
            mNavigationView.getHeaderView(0).setBackgroundColor(getColor(R.color.color_main));
        }
        TextView textView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.header);
        textView.setText(getStorageUsage(this));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS));
            }
        });
    }

    private void gotoFeedback() {

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();

        builder.setToolbarColor(ContextCompat.getColor(this, R.color.color_main));

        builder.build().launchUrl(this, Uri.parse("https://github.com/calintat/Explorer/issues"));
    }

    private void setType(String type) {
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra(EXTRA_TYPE, type);

        if (Build.VERSION.SDK_INT >= 21) {

            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        startActivity(intent);
        finish();
    }

    private void initActivityFromIntent() {
        mType = getIntent().getStringExtra(EXTRA_TYPE);
        if (mType != null) {
            switch (mType) {
                case "AUDIO":
                    setTheme(R.style.AppTheme_Audio);
                    break;
                case "IMAGE":
                    setTheme(R.style.AppTheme_Image);
                    break;
                case "VIDEO":
                    setTheme(R.style.AppTheme_Video);
                    break;
            }
        }
    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new FileAdapter(this, mListener);
        checkStoragePermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void checkStoragePermission() {
        String WritePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String ReadPermission = Manifest.permission.READ_EXTERNAL_STORAGE;
        int canWrite = checkSelfPermission(WritePermission);
        int canRead = checkSelfPermission(ReadPermission);
        if (canRead != PackageManager.PERMISSION_GRANTED || canWrite != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{WritePermission, ReadPermission}, REQUEST_PERMISSION_CODE);
        } else {
            initRootStorage();
        }
    }

    private void initRootStorage(){
        ArrayList<File> mFiles = null;
        if (mType != null && !("".equals(mType))) {
            switch (mType) {
                case "AUDIO":
                    mFiles = FileUtil.getAudioLibrary(this);
                    break;
                case "IMAGE":
                    mFiles = FileUtil.getImageLibrary(this);
                    break;
                case "VIDEO":
                    mFiles = FileUtil.getVideoLibrary(this);
                    break;
            }
            mAdapter.addAll(mFiles);
        }else{
            File path = FileUtil.getInternalStorage();
            mCurrentFile = path;
            File[] files = FileUtil.getChildren(path);
            mAdapter.addAll(files);
        }
        mRecyclerView.setAdapter(mAdapter);
        updateNavigationIcon();
    }

    private void updateNavigationIcon(){
        if(mAdapter != null && mAdapter.canSelectable()){
            mToolBar.setNavigationIcon(R.drawable.ic_clear);
            mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAdapter.setCanSelectable();
                    mAdapter.clearAllItemSelected();
                    mMainHandler.sendEmptyMessageDelayed(UPDATE_APP_MENU,200);
                    updateNavigationIcon();
                }
            });
            return;
        }
        if(mCurrentFile != null && !mCurrentFile.getAbsolutePath().equals(getInternalStorage().getAbsolutePath())){
            mToolBar.setNavigationIcon(R.drawable.ic_back);
            mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File parent = mCurrentFile.getParentFile();
                    setPath(parent);
                }
            });
            return ;
        }
        if(mType != null && !"".equals(mType)){
            mToolBar.setNavigationIcon(R.drawable.ic_back);
            mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mType = null;
                    setType(mType);
                }
            });
            return ;
        }
        mToolBar.setNavigationIcon(R.drawable.ic_menu);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mNavigationView, true);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean haveStoragePermission = false;
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int result : grantResults) {
                haveStoragePermission = result == PackageManager.PERMISSION_GRANTED;
            }
        }
        if (haveStoragePermission) {
            initRootStorage();
        } else {
            Snackbar.make(mCoordinatorLayout, getString(R.string.request_permission),
                    Snackbar.LENGTH_LONG).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelFinish = true;
                    try {
                        gotoApplicationSettings();
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtil.e(e.getMessage());
                    }
                }
            }).show();
            CountDownTimer timer = new CountDownTimer(5 * 1000, 2000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (cancelFinish) {
                        this.cancel();
                    }
                }
                @Override
                public void onFinish() {
                    finish();
                }
            }.start();
        }
    }

    private void gotoApplicationSettings() {

        Intent intent = new Intent();

        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);

        intent.setData(Uri.fromParts("package", "com.android.simplefilemanager", null));

        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mAdapter != null) {
            int count = mAdapter.getCurrentDirectoryItemCount();
            menu.findItem(R.id.action_create_file).setVisible(true);
            menu.findItem(R.id.action_delete).setVisible(count >= 1);
            menu.findItem(R.id.action_rename).setVisible(count >= 1);
            menu.findItem(R.id.action_search).setVisible(count >= 0);
            menu.findItem(R.id.action_move).setVisible(count >= 1);
            menu.findItem(R.id.action_send).setVisible(count >= 1);
            menu.findItem(R.id.action_paste).setVisible(count >= 0);
            menu.findItem(R.id.action_delete).setEnabled(hasFileSelected);
            menu.findItem(R.id.action_rename).setEnabled(isOnlyFileSelected);
            menu.findItem(R.id.action_move).setEnabled(hasFileSelected);
            menu.findItem(R.id.action_send).setEnabled(isOnlyFileSelected);
            menu.findItem(R.id.action_copy).setEnabled(hasFileSelected);
            menu.findItem(R.id.action_paste).setEnabled(canUsePaste);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mAdapter.setToggle(savedInstanceState.getIntegerArrayList(SAVED_SELECTION));
        String path = savedInstanceState.getString(SAVED_DIRECTORY, getInternalStorage().getPath());
        if (mCurrentPath != null) setPath(new File(path));
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putIntegerArrayList(SAVED_SELECTION, mAdapter.getSelectedPosition());
        outState.putString(SAVED_DIRECTORY, mCurrentPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        switch (menuId) {
            case R.id.action_delete:
                actionDelete();
                return true;
            case R.id.action_rename:
                actionRename();
                return true;
            case R.id.action_search:
                actionSearch();
                return true;
            case R.id.action_copy:
                actionCopy();
                return true;
            case R.id.action_move:
                actionMove();
                return true;
            case R.id.action_send:
                actionSend();
                return true;
            case R.id.action_paste:
                actionPaste();
                return true;
            case R.id.action_create_file:
                actionCreateFile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private ViewHolder.OnViewHolderClickListener mListener = new ViewHolder.OnViewHolderClickListener() {
        @Override
        public void onClick(int position) {
            File file = mAdapter.getFileByPosition(position);
            if (mAdapter.canSelectable()) {
                mAdapter.setToggle(position);
                mMainHandler.sendEmptyMessageDelayed(UPDATE_APP_MENU,200);
                return;
            }
            if (file.isDirectory()) {
                file.setReadable(true);
                setPath(file);
                updateNavigationIcon();
                return ;
            }
            tryToOpenFile(file);
        }

        @Override
        public boolean onLongClick(int position) {
            mAdapter.setToggle(position);
            mAdapter.setCanSelectable();
            updateNavigationIcon();
            mMainHandler.sendEmptyMessageDelayed(0,200);
            return true;
        }
    };

    private Handler mMainHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            hasFileSelected = mAdapter.getSelectedFileCount()>0;
            isOnlyFileSelected = mAdapter.getSelectedFileCount() == 1;
            canUsePaste = mAdapter.getCopyToPaste() || mAdapter.getMoveToPaste();
            invalidateOptionsMenu();
        }
    };

    private void tryToOpenFile(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = getMimeType(file);
        intent.setDataAndType(Uri.fromFile(file), type);
        try {
           startActivity(intent);
        } catch (Exception e) {
            String secondType = FileUtil.getMIMEType(file);
            intent.setDataAndType(Uri.fromFile(file), secondType);
            try{
                startActivity(intent);
            }catch (Exception e1){
                Toast.makeText(MainActivity.this,String.format("Cannot open %s", FileUtil.getName(file)),
                        Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void setPath(File file) {
        mAdapter.removeAllFile();
        mAdapter.addAll(FileUtil.getChildren(file));
        mCurrentFile = file;
        mCurrentPath = mCurrentFile.getAbsolutePath();
        updateAppBarTitle();
        updateNavigationIcon();
    }

    private void updateAppBarTitle() {
        if (mCurrentFile != null && !mCurrentFile.getAbsoluteFile().equals(getInternalStorage().getAbsoluteFile())) {
            String title = FileUtil.getName(mCurrentFile);
            mCollapsing.setTitle(title);
        }else{
            mCollapsing.setTitle(getString(R.string.app_name));
        }
    }

    private void actionCreate() {
        mDialogFactory.setOnDialogButtonListener(new DialogFactory.OnDialogButtonListener() {
            @Override
            public void onEnsureClicked(String fileName) {
                try {
                    mCurrentFile.setReadable(true);
                    mCurrentFile.setWritable(true);
                    final File directory = FileUtil.createDirectory(mCurrentFile, fileName);
                    LogUtil.e("zyq"+directory.exists());
                    mAdapter.clearSelection();
                    mAdapter.add(directory);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            insertNewFileToMediaStore(directory);
                        }
                    }).start();
                } catch (Exception e) {
                    LogUtil.e(e.getMessage());
                }finally {
                    if(mDialog != null){
                        mDialog.dismiss();
                        mDialog = null;
                    }
                }
            }

            @Override
            public void onCancelClicked() {
                if(mDialog != null){
                    mDialog.dismiss();
                    mDialog = null;
                }
            }
        });
        mDialog = mDialogFactory.getDialog(DialogFactory.ACTIONS.ACTION_CREATE);
        mDialog.show();
    }

    private void actionCreateFile(){
        mDialogFactory.setOnDialogButtonListener(new DialogFactory.OnDialogButtonListener() {
            @Override
            public void onEnsureClicked(String fileName) {
                try {
                    mCurrentFile.setReadable(true);
                    mCurrentFile.setWritable(true);
                    final File file = FileUtil.createFile(mCurrentFile, fileName);
                    LogUtil.e("zyq"+file.exists());
                    mAdapter.clearSelection();
                    mAdapter.add(file);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            insertNewFileToMediaStore(file);
                        }
                    }).start();
                } catch (Exception e) {
                    LogUtil.e(e.getMessage());
                }finally {
                    if(mDialog != null){
                        mDialog.dismiss();
                        mDialog = null;
                    }
                }
            }

            @Override
            public void onCancelClicked() {
                if(mDialog != null){
                    mDialog.dismiss();
                    mDialog = null;
                }
            }
        });
        mDialog = mDialogFactory.getDialog(DialogFactory.ACTIONS.ACTION_CREATE);
        mDialog.show();
    }

    private void actionDelete() {

        actionDelete(mAdapter.getSelectedItems());
    }

    private void actionDelete(final ArrayList<File> files) {

        mDialogFactory.setOnDialogButtonListener(new DialogFactory.OnDialogButtonListener() {
            @Override
            public void onEnsureClicked(String fileName) {
                try {
                    LogUtil.e("delete files");
                    mAdapter.clearSelection();
                    mAdapter.setCanSelectable();
                    mAdapter.removeAllFile(files);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (File f : files){
                                try {
                                    FileUtil.deleteFile(f);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                } catch (Exception e) {
                    LogUtil.e(e.getMessage());
                }finally {
                    if(mDialog != null){
                        mDialog.dismiss();
                        mDialog = null;
                    }
                    updateNavigationIcon();
                    mMainHandler.sendEmptyMessageDelayed(UPDATE_APP_MENU,200);
                }
            }

            @Override
            public void onCancelClicked() {
                LogUtil.e("cancel delete");
                mAdapter.clearSelection();
                mAdapter.setCanSelectable();
                if(mDialog != null){
                    mDialog.dismiss();
                    mDialog = null;
                }
                updateNavigationIcon();
            }
        });
        mDialog = mDialogFactory.getDialog(DialogFactory.ACTIONS.ACTION_DELETE);
        mDialog.show();
    }

    private void actionRename() {

        final List<File> selectedItems = mAdapter.getSelectedItems();
        mDialogFactory.setOnDialogButtonListener(new DialogFactory.OnDialogButtonListener() {
            @Override
            public void onEnsureClicked(String fileName) {
                try {
                    mAdapter.clearSelection();
                    mAdapter.setCanSelectable();
                    File file = selectedItems.get(0);
                    int index = mAdapter.indexOfFile(file);
                    mAdapter.updateItemAt(index, FileUtil.renameFile(file, fileName));
                } catch (Exception e) {
                    LogUtil.e(e.getMessage());
                }finally {
                    if(mDialog != null){
                        mDialog.dismiss();
                        mDialog = null;
                    }
                    updateNavigationIcon();
                    mMainHandler.sendEmptyMessageDelayed(UPDATE_APP_MENU,200);
                }
            }

            @Override
            public void onCancelClicked() {
                mAdapter.clearSelection();
                mAdapter.setCanSelectable();
                if(mDialog != null){
                    mDialog.dismiss();
                    mDialog = null;
                }
                updateNavigationIcon();
                mMainHandler.sendEmptyMessageDelayed(UPDATE_APP_MENU,200);
            }
        });
        mDialog = mDialogFactory.getDialog(DialogFactory.ACTIONS.ACTION_RENAME);
        mDialog.show();
    }

    private void actionSearch() {
        mDialogFactory.setOnDialogButtonListener(new DialogFactory.OnDialogButtonListener() {
            @Override
            public void onEnsureClicked(String fileName) {
                try {
                    mAdapter.removeAllFile();
                    mAdapter.addAll(FileUtil.searchFilesName(MainActivity.this,fileName));
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if(mDialog != null){
                        mDialog.dismiss();
                        mDialog = null;
                    }
                }
            }

            @Override
            public void onCancelClicked() {
                if(mDialog != null){
                    mDialog.dismiss();
                    mDialog = null;
                }
            }
        });
        mDialogFactory.setTitleBackground(getColorByType());
        mDialog = mDialogFactory.getDialog(DialogFactory.ACTIONS.ACTION_SEARCH);
        mDialog.show();
    }

    private void actionCopy() {
        mAdapter.setCopyToPaste(true);
        ArrayList<File> files = mAdapter.getSelectedItems();
        LogUtil.e("Copy : "+files.size());
        mAdapter.setCopyOrMoveFiles(files);
        mAdapter.clearSelection();
        mAdapter.setCanSelectable();
        updateNavigationIcon();
        mMainHandler.sendEmptyMessageDelayed(UPDATE_APP_MENU,200);
    }

    private void actionMove() {
        mAdapter.setMoveToPaste(true);
        ArrayList<File> files = mAdapter.getSelectedItems();
        LogUtil.e("Move : "+files.size());
        mAdapter.setCopyOrMoveFiles(files);
        mAdapter.clearSelection();
        mAdapter.setCanSelectable();
        updateNavigationIcon();
        mMainHandler.sendEmptyMessageDelayed(UPDATE_APP_MENU,200);
    }

    private void actionSend() {

        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);

        intent.setType("*/*");

        ArrayList<Uri> uris = new ArrayList<>();

        for (File file : mAdapter.getSelectedItems()) {

            if (file.isFile()) uris.add(Uri.fromFile(file));
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

        startActivity(intent);
    }

    private void actionPaste(){
        final ArrayList<File> files = mAdapter.getCopyOrMoveFiles();
        LogUtil.e("actionPaste : "+files.size());
        if (mAdapter.getCopyToPaste()){
            transferFiles(files,false);
            mAdapter.setCopyToPaste(false);
        }else if (mAdapter.getMoveToPaste()){
            transferFiles(files,true);
            mAdapter.setMoveToPaste(false);
        }
    }

    private void transferFiles(final List<File> files, final Boolean delete) {
        LogUtil.e("transferFiles : onEnsureClicked"+files.size());
        mDialogFactory.setOnDialogButtonListener(new DialogFactory.OnDialogButtonListener() {
            @Override
            public void onEnsureClicked(String fileName) {
                try {
                    LogUtil.e("paste : onEnsureClicked"+files.size());
                    for (File file : files) {
                        mAdapter.addAll(FileUtil.copyFile(file, mCurrentFile));
                        if (delete){
                            FileUtil.deleteFile(file);
                        }
                    }
                } catch (Exception e) {
                    LogUtil.e(e.getMessage());
                }finally {
                    if(mDialog != null){
                        mDialog.dismiss();
                        mDialog = null;
                    }
                    mAdapter.clearCopyOrMoveFiles();
                    updateNavigationIcon();
                    mMainHandler.sendEmptyMessageDelayed(UPDATE_APP_MENU,200);
                }
            }

            @Override
            public void onCancelClicked() {
                LogUtil.e("cancel delete");
                mAdapter.clearSelection();
                mAdapter.setCanSelectable();
                if(mDialog != null){
                    mDialog.dismiss();
                    mDialog = null;
                }
                mAdapter.clearCopyOrMoveFiles();
                updateNavigationIcon();
            }
        });
        if(delete){
            mDialog = mDialogFactory.getDialog(DialogFactory.ACTIONS.ACTION_MOVE);
        }else{
            mDialog = mDialogFactory.getDialog(DialogFactory.ACTIONS.ACTION_COPY);
        }
        mDialogFactory.setTitleBackground(getColorByType());
        mDialog.show();

    }

    private void insertNewFileToMediaStore(File file){
        ContentValues newValues = new ContentValues(6);
        String title = FileUtil.getName(file);
        newValues.put(MediaStore.Files.FileColumns.TITLE, title);
        newValues.put(MediaStore.Files.FileColumns.PARENT,
                file.getParent());
        newValues.put(MediaStore.Files.FileColumns.DATA,file.getAbsolutePath());
        newValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, title);
        newValues.put(MediaStore.Images.Media.DATE_MODIFIED,
                System.currentTimeMillis() / 1000);
        newValues.put(MediaStore.Files.FileColumns.SIZE, file.length());
        newValues.put(MediaStore.Images.Media.MIME_TYPE, FileUtil.getMimeType(file));
        mResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, newValues);

    }

    private int getColorByType(){
        if(mType != null){
            switch (mType) {
                case "AUDIO":
                    return getColor(R.color.color_audio);
                case "IMAGE":
                    return getColor(R.color.color_image);
                case "VIDEO":
                    return getColor(R.color.color_video);
            }
        }
        return getColor(R.color.color_main);
    }

}
