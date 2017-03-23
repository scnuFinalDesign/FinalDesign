package com.example.asus88.finaldesgin.activity;

import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.bean.DevBean;
import com.example.asus88.finaldesgin.fragment.ApplicationFragment;
import com.example.asus88.finaldesgin.fragment.BaseFragment;
import com.example.asus88.finaldesgin.fragment.FileFragment;
import com.example.asus88.finaldesgin.fragment.MusicFragment;
import com.example.asus88.finaldesgin.fragment.PhotoFragment;
import com.example.asus88.finaldesgin.fragment.TranslationFragment;
import com.example.asus88.finaldesgin.fragment.VideoFragment;
import com.example.asus88.finaldesgin.util.DimenUtil;
import com.example.asus88.finaldesgin.util.FileUtil;
import com.example.asus88.finaldesgin.util.SharePUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.asus88.finaldesgin.R.string.newDirectory;
import static com.example.asus88.finaldesgin.R.string.newFile;

/**
 * Created by asus88 on 2016/12/27.
 */

public class MainActivity extends EBaseActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private static final String TAG = "MainActivity";
    @BindView(R.id.radio_file)
    RadioButton mRadioFile;
    @BindView(R.id.radio_translate)
    RadioButton mRadioTranslate;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.main_content)
    FrameLayout mContent;
    @BindView(R.id.main_fab)
    FloatingActionButton mFab;
    @BindView(R.id.main_nav)
    NavigationView mNav;
    @BindView(R.id.main_drawer)
    DrawerLayout mDrawer;
    @BindView(R.id.main_search)
    ImageView mSearch;
    @BindView(R.id.main_view_stub)
    ViewStub mViewStub;

    private static final int DEFAULT_FILE_FRAGMENT_ID = R.id.nav_file;
    private ActionBarDrawerToggle toggle;

    private MenuItem selectedItem;


    //main content
    private ApplicationFragment mAppFragment;
    private MusicFragment mMusicFragment;
    private PhotoFragment mPhotoFragment;
    private VideoFragment mVideoFragment;
    private FileFragment mFileFragment;
    private FragmentTransaction transaction;
    private BaseFragment curFragment;
    private FragmentManager mManager;
    private int curFragmentNavId = DEFAULT_FILE_FRAGMENT_ID;

    //new pop window
    private String fileName;
    private String filePath;
    private String fileSuffix;
    private boolean result;


    private PopupWindow createFile;
    private TextView newTitle;
    private EditText suffix;
    private EditText newName;
    private ImageView point;
    private int type;


    private TranslationFragment traFragment;

    //viewStub
    private LinearLayout mLinearLayout;

    private long mExitTime = 0;
    private boolean isNavItemSelected = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initData();
        initEvents();
        //changeSize();
    }


    private void initViews() {
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void initData() {
        mFileFragment = new FileFragment();
        mManager = getSupportFragmentManager();
        transaction = mManager.beginTransaction();
        transaction.add(R.id.main_content, mFileFragment);
        transaction.commit();
    }

    private void initEvents() {
        mNav.setNavigationItemSelectedListener(this);
        mFab.setOnClickListener(this);
        mSearch.setOnClickListener(this);
        mRadioFile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !isNavItemSelected) {
                    replaceFragment(traFragment, getCurFragmentByNavId(curFragmentNavId));
                }
            }
        });
        mRadioTranslate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (traFragment == null) {
                        traFragment = new TranslationFragment();
                    }
                    replaceFragment(getCurFragmentByNavId(curFragmentNavId), traFragment);
                }
            }
        });
    }


    /**
     * 修改 drawerlayout 的响应范围
     */
    private void changeSize() {
        Field mDragger = null;
        try {
            mDragger = mDrawer.getClass().getDeclaredField(
                    "mLeftDragger"); //mRightDragger for right obviously
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mDragger.setAccessible(true);
        ViewDragHelper draggerObj = null;
        try {
            draggerObj = (ViewDragHelper) mDragger
                    .get(mDrawer);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Field mEdgeSize = null;
        try {
            mEdgeSize = draggerObj.getClass().getDeclaredField(
                    "mEdgeSize");
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mEdgeSize.setAccessible(true);
        int edge = 0;
        try {
            edge = mEdgeSize.getInt(draggerObj);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            mEdgeSize.setInt(draggerObj, edge * 5); //optimal value as for me, you may set any constant in dp
            //You can set it even to the value you want like mEdgeSize.setInt(draggerObj, 150); for 150dp
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void replaceFragment(Fragment from, Fragment to) {
        transaction = mManager.beginTransaction();
        transaction.hide(from);
        if (!to.isAdded()) {
            transaction.add(R.id.main_content, to);
        } else {
            transaction.show(to);
        }
        transaction.commit();
    }


    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_set_path) {
            if (curFragmentNavId != DEFAULT_FILE_FRAGMENT_ID) {
                replaceFragment(getCurFragmentByNavId(curFragmentNavId), mFileFragment);
                curFragmentNavId = DEFAULT_FILE_FRAGMENT_ID;
            }
            if (mViewStub.getParent() != null) {
                mLinearLayout = (LinearLayout) mViewStub.inflate();
                Button sure = (Button) mLinearLayout.findViewById(R.id.stub_sure);
                Button cancel = (Button) mLinearLayout.findViewById(R.id.stub_cancel);
                sure.setOnClickListener(this);
                cancel.setOnClickListener(this);
            } else {
                mLinearLayout.setVisibility(View.VISIBLE);
            }
            hideFab();
            mDrawer.closeDrawer(GravityCompat.START);
            mRadioTranslate.setEnabled(false);
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            if (selectedItem == null) {
                selectedItem = mNav.getMenu().findItem(0).getSubMenu().getItem(0);
                isNavItemSelected = true;
                selectedItem.setChecked(false);
            }
            Fragment fragment;
            if (mRadioTranslate.isChecked()) {
                fragment = traFragment;
                mRadioFile.setChecked(true);
            } else {
                fragment = getCurFragmentByNavId(curFragmentNavId);
            }
            switch (id) {
                case R.id.nav_file:
                    replaceFragment(fragment, mFileFragment);
                    break;
                case R.id.nav_photo:
                    if (mPhotoFragment == null) {
                        mPhotoFragment = new PhotoFragment();
                    }
                    replaceFragment(fragment, mPhotoFragment);
                    break;
                case R.id.nav_music:
                    if (mMusicFragment == null) {
                        mMusicFragment = new MusicFragment();
                    }
                    replaceFragment(fragment, mMusicFragment);
                    break;
                case R.id.nav_application:
                    if (mAppFragment == null) {
                        mAppFragment = new ApplicationFragment();
                    }
                    replaceFragment(fragment, mAppFragment);
                    break;
                case R.id.nav_video:
                    if (mVideoFragment == null) {
                        mVideoFragment = new VideoFragment();
                    }
                    replaceFragment(fragment, mVideoFragment);
                    break;
            }
            mDrawer.closeDrawer(GravityCompat.START);
            curFragmentNavId = id;
            if (mAppFragment != null && mAppFragment.getFabBtnMode()) {
                if (R.id.nav_application == id) {
                    changeFabBtnImage(false);
                } else {
                    changeFabBtnImage(true);
                }
            }
        }
        isNavItemSelected = false;
        return true;
    }


    /**
     * 显示新建文件夹窗口 type=1
     */
    public void showFileWindow(int t) {
        if (createFile == null) {
            View window = LayoutInflater.from(this).inflate(R.layout.popup_window_new_file, null);
            newTitle = (TextView) window.findViewById(R.id.pop_new_file_title);
            newName = (EditText) window.findViewById(R.id.pop_new_file_name);
            suffix = (EditText) window.findViewById(R.id.pop_new_file_suffix);
            point = (ImageView) window.findViewById(R.id.pop_new_file_point_image);
            Button sure = (Button) window.findViewById(R.id.pop_new_file_sure);
            Button cancel = (Button) window.findViewById(R.id.pop_new_file_cancel);

            createFile = new PopupWindow(window, DimenUtil.getRealWidth(this, 768, 660),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            createFile.setFocusable(true);
            createFile.setBackgroundDrawable(new ColorDrawable(0x000000));
            createFile.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    hideBackground();
                }
            });
            newName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        newFile();
                        return true;
                    }
                    return false;
                }
            });
            suffix.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        newFile();
                    }
                    return true;
                }
            });
            cancel.setOnClickListener(this);
            sure.setOnClickListener(this);
            createFile.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
            createFile.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        type = t;
        newName.setText("");
        suffix.setText("");
        if (type == 1) {
            newTitle.setText(getString(newDirectory));
            newName.setWidth(DimenUtil.getRealWidth(this, 768, 580));
            newName.setImeOptions(EditorInfo.IME_ACTION_DONE);
            suffix.setVisibility(View.GONE);
            point.setVisibility(View.GONE);
        } else {
            newTitle.setText(getString(newFile));
            newName.setWidth(DimenUtil.getRealWidth(this, 768, 470));
            newName.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            suffix.setVisibility(View.VISIBLE);
            point.setVisibility(View.VISIBLE);
        }
        createFile.showAtLocation(mContent, Gravity.CENTER, 0, 0);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.main_fab:
                if (R.id.nav_application == curFragmentNavId && mAppFragment.getFabBtnMode()) {
                    mAppFragment.finishDelMode();
                    changeFabBtnImage(true);
                } else {
                    curFragment = getCurFragmentByNavId(curFragmentNavId);
                    if (curFragment != null) {
                        setFabButtonSize(curFragment.getFabButtonNum());
                        showBackground();
                    }
                }
                break;
            case R.id.pop_new_file_cancel:
                createFile.dismiss();
                hideBackground();
                break;
            case R.id.pop_new_file_sure:
                newFile();
                break;
            case R.id.main_search:
                Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,
                            mSearch, mSearch.getTransitionName());
                    startActivity(searchIntent, options.toBundle());
                } else {
                    startActivity(searchIntent);
                }
                break;
            case R.id.stub_cancel:
                mLinearLayout.setVisibility(View.GONE);
                showFab();
                mRadioTranslate.setEnabled(true);
                mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                break;
            case R.id.stub_sure:
                String path = mFileFragment.getStorePath() + "/";
                conManager.setStorePath(path);
                SharePUtil.save("savePath", "path", path);
                mLinearLayout.setVisibility(View.GONE);
                showFab();
                mRadioTranslate.setEnabled(true);
                mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                break;
        }
    }


    /**
     * 新建文件/文件夹
     */
    private void newFile() {
        fileName = newName.getText().toString();
        if (TextUtils.isEmpty(fileName)) {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.file_name_can_not_empty), Toast.LENGTH_SHORT).show();
        } else {
            StringBuilder builder = new StringBuilder();
            filePath = mFileFragment.getCurrentPath();
            if (type == 1) {
                result = FileUtil.newDirectory(fileName, filePath);
            } else {
                fileSuffix = suffix.getText().toString();
                if (TextUtils.isEmpty(fileSuffix)) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.file_suffix_can_not_empty), Toast.LENGTH_SHORT).show();
                } else {
                    builder.append(fileName);
                    builder.append(".");
                    builder.append(fileSuffix);
                    fileName = builder.toString();
                    result = FileUtil.newFile(fileName, filePath);
                }
            }
            if (result) {
                mFileFragment.addFileBean(mFileFragment.newFileBean(
                        new File(filePath, fileName), System.currentTimeMillis()));
            } else {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.new_file_fail), Toast.LENGTH_SHORT).show();
            }
            createFile.dismiss();
            hideBackground();
        }
    }

    private BaseFragment getCurFragmentByNavId(int position) {
        switch (position) {
            case R.id.nav_file:
                return mFileFragment;
            case R.id.nav_photo:
                return mPhotoFragment;
            case R.id.nav_music:
                return mMusicFragment;
            case R.id.nav_video:
                return mVideoFragment;
            case R.id.nav_application:
                return mAppFragment;
            default:
                return null;
        }
    }


    public void changeFabBtnImage(boolean flag) {
        if (flag) {
            mFab.setImageResource(R.mipmap.fab_more);
        } else {
            mFab.setImageResource(R.mipmap.fab_finish);
        }
    }

    @Override
    public void deleteFile() {
        curFragment = getCurFragmentByNavId(curFragmentNavId);
        if (curFragment != null) {
            curFragment.deleteFile();
        }
    }

    @Override
    public int getSelectedSize() {
        return getCurFragmentByNavId(curFragmentNavId).getSelectedNum();
    }

    @Override
    public void sendFile(List<DevBean> list) {
        curFragment = getCurFragmentByNavId(curFragmentNavId);
        if (curFragment != null) {
            curFragment.sendFile(list);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - mExitTime > 2000) {
                Snackbar.make(mContent, getString(R.string.exit), Snackbar.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void hideFab() {
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) mFab.getLayoutParams();
        ObjectAnimator animator = ObjectAnimator.ofFloat(mFab, "translationY", 0, mFab.getHeight() + lp.bottomMargin);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    private void showFab() {
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) mFab.getLayoutParams();
        ObjectAnimator animator = ObjectAnimator.ofFloat(mFab, "translationY", mFab.getHeight() + lp.bottomMargin, 0);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

}
