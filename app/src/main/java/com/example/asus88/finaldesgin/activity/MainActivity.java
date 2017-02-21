package com.example.asus88.finaldesgin.activity;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.TextViewFactory;
import com.example.asus88.finaldesgin.bean.Bean;
import com.example.asus88.finaldesgin.bean.FabMenuButtonBean;
import com.example.asus88.finaldesgin.fragment.ApplicationFragment;
import com.example.asus88.finaldesgin.fragment.BaseFragment;
import com.example.asus88.finaldesgin.fragment.FileFragment;
import com.example.asus88.finaldesgin.fragment.MusicFragment;
import com.example.asus88.finaldesgin.fragment.PhotoFragment;
import com.example.asus88.finaldesgin.fragment.VideoFragment;
import com.example.asus88.finaldesgin.util.AnimationManager;
import com.example.asus88.finaldesgin.util.DimenUtil;
import com.example.asus88.finaldesgin.util.FileUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLING;
import static com.example.asus88.finaldesgin.R.string.newDirectory;
import static com.example.asus88.finaldesgin.R.string.newFile;

/**
 * Created by asus88 on 2016/12/27.
 */

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
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


    private ActionBarDrawerToggle toggle;


    //fab menu
    private ViewGroup background;
    private boolean isAnimating;
    private int fabMenuBagColor;
    private TextView[] fabButton;
    private List<FabMenuButtonBean> fabBtnList;

    //main content
    private ApplicationFragment mAppFragment;
    private MusicFragment mMusicFragment;
    private PhotoFragment mPhotoFragment;
    private VideoFragment mVideoFragment;
    private FileFragment mFileFragment;
    private FragmentTransaction transaction;
    private FragmentManager mManager;
    private int curFragmentNavId = R.id.nav_file;

    //new pop window
    private String fileName;
    private String filePath;
    private String fileSuffix;
    private boolean result;

    private WifiManager mWifiManager;

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
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

    }

    private void initData() {
        mFileFragment = new FileFragment();
        mManager = getSupportFragmentManager();
        transaction = mManager.beginTransaction();
        transaction.add(R.id.main_content, mFileFragment);
        transaction.commit();

        // todo new thread to load
        // fab menu btn
        fabMenuBagColor = getResources().getColor(R.color.fab_menu_color);
        fabButton = new TextView[5];
        initFabMenuBtnData();

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

    }

    private void initEvents() {
        mNav.setNavigationItemSelectedListener(this);
        mFab.setOnClickListener(this);
    }

    private void initFabMenuBtnData() {
        fabBtnList = new ArrayList<>();
        FabMenuButtonBean newDirectory = new FabMenuButtonBean("newDirectory", R.drawable.bg_fab_new_btn);
        newDirectory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeButtonFromBg();
                showFileWindow(1);
            }
        });
        FabMenuButtonBean newFile = new FabMenuButtonBean("newFile", R.drawable.bg_fab_new_file_btn);
        newFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeButtonFromBg();
                showFileWindow(0);
            }
        });
        FabMenuButtonBean delete = new FabMenuButtonBean("delete", R.drawable.bg_fab_delete_btn);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseFragment baseFragment = (BaseFragment) getCurFragmentByNavId(curFragmentNavId);
                List<Bean> sList = baseFragment.getSelectedList(baseFragment.getDataList());
                if (sList == null || sList.size() <= 0) {
                    Toast.makeText(MainActivity.this, getString(R.string.no_selected), Toast.LENGTH_SHORT).show();
                } else {
                    for (int i = 0; i < sList.size(); i++) {
                        FileUtil.deleteFile(new File(sList.get(i).getPath()));
                    }
                    (baseFragment.getDataList()).removeAll(sList);
                    baseFragment.notifyRecyclerView();
                }
                removeButtonFromBg();
                hideBackground();
            }
        });
        FabMenuButtonBean link = new FabMenuButtonBean("link", R.drawable.bg_fab_link_btn);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeButtonFromBg();
                int state = isWifiApEnabled();
                if ((mWifiManager.getWifiState() == WIFI_STATE_DISABLING ||
                        mWifiManager.getWifiState() == WIFI_STATE_DISABLED) &&
                        (state == 10 || state == 11)) {
                    showIsOpenWifiWindow();
                } else {
                    Intent intent = new Intent(MainActivity.this, LinkActivity.class);
                    startActivity(intent);
                    hideBackground();
                }
            }
        });
        FabMenuButtonBean send = new FabMenuButtonBean("send", R.drawable.bg_fab_send_btn);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeButtonFromBg();
                hideBackground();
            }
        });

        fabBtnList.add(newDirectory);
        fabBtnList.add(newFile);
        fabBtnList.add(delete);
        fabBtnList.add(link);
        fabBtnList.add(send);
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
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = getCurFragmentByNavId(curFragmentNavId);
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
        curFragmentNavId = id;
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * \算出第一个按钮的marginTop
     *
     * @param btnNum
     * @param btnHeight
     * @param margin    按钮间的margin
     * @return
     */
    private int getFirstBtnMarTop(int btnNum, int btnHeight, int margin) {
        if (btnNum % 2 == 0) {
            return (768 - btnNum * btnHeight - (btnNum - 1) * margin) / 2;
        } else {
            return (768 - btnHeight - (btnNum - 1) * (btnHeight + margin)) / 2;
        }
    }

    private void showFabMenu() {
        BaseFragment fragment = (BaseFragment) getCurFragmentByNavId(curFragmentNavId);
        int num = fragment.getFabButtonNum();
        int start = 5 - fragment.getFabButtonNum();
        int firMargin = getFirstBtnMarTop(num, 70, 20);
        int marLeft = DimenUtil.getRealWidth(this, 1280, 140);
        for (int i = start; i < 5; i++) {
            if (fabButton[i] == null) {
                fabButton[i] = TextViewFactory.createTextView(MainActivity.this, fabBtnList.get(i));
            }
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fabButton[i].getLayoutParams();
            params.setMargins(marLeft, DimenUtil.getRealHeight(MainActivity.this, 768, (firMargin + (i - start) * 90)), 0, 0);
            background.addView(fabButton[i]);
        }
    }

    private void removeButtonFromBg() {
        BaseFragment fragment = (BaseFragment) getCurFragmentByNavId(curFragmentNavId);
        int start = 5 - fragment.getFabButtonNum();
        for (int i = start; i < 5; i++) {
            background.removeView(fabButton[i]);
        }
    }

    /**
     * 显示fabmenu 背景
     */
    private void showBackground() {
        if (background == null) {
            ViewGroup rootView = (ViewGroup) MainActivity.this.getWindow().getDecorView();
            background = new FrameLayout(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(rootView.getWidth(), rootView.getHeight());
            background.setLayoutParams(params);
            background.setBackgroundColor(Color.TRANSPARENT);
            background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isAnimating) return;
                    //收起动画
                    removeButtonFromBg();
                    hideBackground();
                }
            });
            rootView.addView(background);
        }
        background.setVisibility(View.VISIBLE);
        AnimationManager.animate(background, "backgroundColor", 0, 1, new ArgbEvaluator(),
                null, Color.TRANSPARENT, fabMenuBagColor);

    }

    private void hideBackground() {
        background.setVisibility(View.GONE);
        AnimationManager.animate(
                background,
                "backgroundColor",
                0,
                1,
                new ArgbEvaluator(), null,
                fabMenuBagColor,
                Color.TRANSPARENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.toolbar_search) {
            //共享动画，弹出搜索页面
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 显示新建文件夹窗口 type=1
     *
     * @param type
     */
    private void showFileWindow(final int type) {
        View window = LayoutInflater.from(this).inflate(R.layout.popup_window_new_file, null);
        TextView title = (TextView) window.findViewById(R.id.pop_new_file_title);
        final EditText name = (EditText) window.findViewById(R.id.pop_new_file_name);
        final EditText suffix = (EditText) window.findViewById(R.id.pop_new_file_suffix);
        Button sure = (Button) window.findViewById(R.id.pop_new_file_sure);
        Button cancel = (Button) window.findViewById(R.id.pop_new_file_cancel);
        if (type == 1) {
            title.setText(getString(newDirectory));
            name.setWidth(DimenUtil.getRealWidth(this, 768, 580));
        } else {
            title.setText(getString(newFile));
            name.setWidth(DimenUtil.getRealWidth(this, 768, 470));
            suffix.setVisibility(View.VISIBLE);
            ImageView point = (ImageView) window.findViewById(R.id.pop_new_file_point_image);
            point.setVisibility(View.VISIBLE);
        }

        final PopupWindow popupWindow = new PopupWindow(window, DimenUtil.getRealWidth(this, 768, 660),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x000000));
        popupWindow.showAtLocation(mContent, Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(new popOnDismissListener());
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                hideBackground();
            }
        });
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileName = name.getText().toString();
                if (TextUtils.isEmpty(fileName)) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.file_name_can_not_empty), Toast.LENGTH_SHORT).show();
                } else {
                    filePath = mFileFragment.getCurrentPath();
                    if (type == 1) {
                        result = FileUtil.newDirectory(fileName, filePath);
                    } else {
                        fileSuffix = suffix.getText().toString();
                        if (TextUtils.isEmpty(fileSuffix)) {
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.file_suffix_can_not_empty), Toast.LENGTH_SHORT).show();
                        } else {
                            result = FileUtil.newFile(fileName, filePath, fileSuffix);
                        }
                    }
                    if (result) {
                        mFileFragment.addFileBean(mFileFragment.newFileBean(
                                new File(filePath, fileName), System.currentTimeMillis()));
                    } else {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.new_file_fail), Toast.LENGTH_SHORT).show();
                    }
                    popupWindow.dismiss();
                    hideBackground();
                }
            }
        });
    }

    private void showIsOpenWifiWindow() {
        View window = LayoutInflater.from(MainActivity.this).inflate(R.layout.popup_window_is_open_wifi, null);
        Button createHotspot = (Button) window.findViewById(R.id.pop_open_wifi_create_hotspot);
        Button openWifi = (Button) window.findViewById(R.id.pop_open_wifi_open_wifi);
        Button cancel = (Button) window.findViewById(R.id.pop_open_wifi_cancel);
        final PopupWindow popupWindow = new PopupWindow(window, DimenUtil.getRealWidth(MainActivity.this, 768, 660),
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x000000));
        popupWindow.showAtLocation(mContent, Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(new popOnDismissListener());
        createHotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo create hotspot
                popupWindow.dismiss();
                boolean flag = createHotspot();
                if (!flag) {
                    Toast.makeText(MainActivity.this, getString(R.string.create_hotspot_fail), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, LinkActivity.class);
                    startActivity(intent);
                }
            }
        });
        openWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWifiManager.setWifiEnabled(true);
                popupWindow.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.main_fab:
                showBackground();
                showFabMenu();
                break;

        }
    }

    private Fragment getCurFragmentByNavId(int position) {
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
                return new Fragment();
        }
    }

    /**
     * 创建热点
     *
     * @return
     */
    private boolean createHotspot() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
        try {
            WifiConfiguration configuration = new WifiConfiguration();
            configuration.SSID = Build.MODEL;
            configuration.preSharedKey = createPassWord();
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class,
                    Boolean.TYPE);
            return (Boolean) method.invoke(mWifiManager, configuration, true);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @return
     */
    private int isWifiApEnabled() {
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApState");
            int state = (int) method.invoke(mWifiManager);
            return state;
        } catch (Exception e) {
            return 10;
        }
    }

    /**
     * 随机生成8位密码
     *
     * @return
     */
    private String createPassWord() {
        Random rd = new Random(); // 创建随机对象
        String passWord = ""; // 保存随机数
        int rdGet; // 取得随机数
        do {
            if (rd.nextInt() % 2 == 1) {
                rdGet = Math.abs(rd.nextInt()) % 10 + 48; // 产生48到57的随机数(0-9的键位值)
            } else {
                rdGet = Math.abs(rd.nextInt()) % 26 + 97; // 产生97到122的随机数(a-z的键位值)
            }
            char num1 = (char) rdGet; // int转换char
            String dd = Character.toString(num1);
            passWord += dd;
        } while (passWord.length() < 8);// 设定长度，此处假定长度小于8
        return passWord;
    }

    /**
     * 监听popWindow dismiss
     */
    class popOnDismissListener implements PopupWindow.OnDismissListener {

        @Override
        public void onDismiss() {
            hideBackground();
        }
    }
}
