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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
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
import com.example.asus88.finaldesgin.adapter.SelectDevAdapter;
import com.example.asus88.finaldesgin.bean.DevBean;
import com.example.asus88.finaldesgin.bean.FabMenuButtonBean;
import com.example.asus88.finaldesgin.connection.Manager;
import com.example.asus88.finaldesgin.fragment.ApplicationFragment;
import com.example.asus88.finaldesgin.fragment.BaseFragment;
import com.example.asus88.finaldesgin.fragment.FileFragment;
import com.example.asus88.finaldesgin.fragment.MusicFragment;
import com.example.asus88.finaldesgin.fragment.PhotoFragment;
import com.example.asus88.finaldesgin.fragment.TranslationFragment;
import com.example.asus88.finaldesgin.fragment.VideoFragment;
import com.example.asus88.finaldesgin.itemDecoration.LineItemDecoration;
import com.example.asus88.finaldesgin.util.AnimationManager;
import com.example.asus88.finaldesgin.util.DimenUtil;
import com.example.asus88.finaldesgin.util.FileUtil;
import com.example.asus88.finaldesgin.util.WifiUitl;

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


    private static final int DEFAULT_FILE_FRAGMENT_ID = R.id.nav_file;
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
    private int curFragmentNavId = DEFAULT_FILE_FRAGMENT_ID;

    //new pop window
    private String fileName;
    private String filePath;
    private String fileSuffix;
    private boolean result;

    private PopupWindow isOpenWifi;

    private PopupWindow createFile;
    private TextView newTitle;
    private EditText suffix;
    private EditText newName;
    private ImageView point;
    private int type;

    private PopupWindow selectDev;
    private List<DevBean> devList;
    private SelectDevAdapter mSelectDevAdapter;
    private Manager conManager;
    private RecyclerView devRecycler;


    private WifiManager mWifiManager;

    private TranslationFragment traFragment;


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
        devList = new ArrayList<>();
    }

    private void initEvents() {
        mNav.setNavigationItemSelectedListener(this);
        mFab.setOnClickListener(this);
        mRadioFile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
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

    private void initFabMenuBtnData() {
        fabBtnList = new ArrayList<>();
        FabMenuButtonBean newDirectory = new FabMenuButtonBean("newDirectory", R.drawable.bg_fab_new_btn);
        newDirectory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeButtonFromBg();
                type = 1;
                showFileWindow();
            }
        });
        FabMenuButtonBean newFile = new FabMenuButtonBean("newFile", R.drawable.bg_fab_new_file_btn);
        newFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeButtonFromBg();
                type = 0;
                showFileWindow();
            }
        });
        FabMenuButtonBean delete = new FabMenuButtonBean("delete", R.drawable.bg_fab_delete_btn);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseFragment baseFragment = getCurFragmentByNavId(curFragmentNavId);
                if (baseFragment != null) {
                    baseFragment.deleteFile();
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
                int state = WifiUitl.getWifiApState(mWifiManager);
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
                BaseFragment baseFragment = getCurFragmentByNavId(curFragmentNavId);
                if (baseFragment != null && baseFragment.getSelectedNum() > 0) {
                    devList = getDevList();
                    if (devList.size() > 0) {
                        showSelectDevWindow();
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.no_link), Toast.LENGTH_SHORT).show();
                        hideBackground();
                    }
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.no_selected), Toast.LENGTH_SHORT).show();
                    hideBackground();
                }
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
        curFragmentNavId = id;
        mDrawer.closeDrawer(GravityCompat.START);
        if (mAppFragment != null && mAppFragment.getFabBtnMode()) {
            if (R.id.nav_application == id) {
                changeFabBtnImage(false);
            } else {
                changeFabBtnImage(true);
            }
        }
        return true;
    }

    /**
     * 算出第一个按钮的marginTop
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
        BaseFragment fragment = getCurFragmentByNavId(curFragmentNavId);
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
        BaseFragment fragment = getCurFragmentByNavId(curFragmentNavId);
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
     */
    private void showFileWindow() {
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
            createFile.setOnDismissListener(new popOnDismissListener());
            cancel.setOnClickListener(this);
            sure.setOnClickListener(this);
        }
        if (type == 1) {
            newTitle.setText(getString(newDirectory));
            newName.setWidth(DimenUtil.getRealWidth(this, 768, 580));
            suffix.setVisibility(View.GONE);
            point.setVisibility(View.GONE);
        } else {
            newTitle.setText(getString(newFile));
            newName.setWidth(DimenUtil.getRealWidth(this, 768, 470));
            suffix.setVisibility(View.VISIBLE);
            point.setVisibility(View.VISIBLE);
        }
        createFile.showAtLocation(mContent, Gravity.CENTER, 0, 0);
    }

    /**
     * 显示是否开启热点窗口
     */
    private void showIsOpenWifiWindow() {
        if (isOpenWifi == null) {
            View window = LayoutInflater.from(MainActivity.this).inflate(R.layout.popup_window_is_open_wifi, null);
            Button createHotspot = (Button) window.findViewById(R.id.pop_open_wifi_create_hotspot);
            Button openWifi = (Button) window.findViewById(R.id.pop_open_wifi_open_wifi);
            Button cancel = (Button) window.findViewById(R.id.pop_open_wifi_cancel);
            isOpenWifi = new PopupWindow(window, DimenUtil.getRealWidth(MainActivity.this, 768, 660),
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            isOpenWifi.setFocusable(true);
            isOpenWifi.setBackgroundDrawable(new ColorDrawable(0x000000));
            isOpenWifi.setOnDismissListener(new popOnDismissListener());
            createHotspot.setOnClickListener(this);
            openWifi.setOnClickListener(this);
            cancel.setOnClickListener(this);
        }
        isOpenWifi.showAtLocation(mContent, Gravity.CENTER, 0, 0);
    }

    private void showSelectDevWindow() {
  //      if (selectDev == null) {
            View window = LayoutInflater.from(MainActivity.this).inflate(R.layout.popup_window_select_dev_to_send, null);
            devRecycler = (RecyclerView) window.findViewById(R.id.pop_select_dev_recyclerView);
            mSelectDevAdapter = new SelectDevAdapter(MainActivity.this, devList);
            devRecycler.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            devRecycler.setAdapter(mSelectDevAdapter);
            devRecycler.addItemDecoration(new LineItemDecoration(MainActivity.this, 20, 20, R.drawable.line_item_decoration));
            Button cancel = (Button) window.findViewById(R.id.pop_select_dev_cancel);
            Button sure = (Button) window.findViewById(R.id.pop_select_dev_sure);
            cancel.setOnClickListener(this);
            sure.setOnClickListener(this);
            selectDev = new PopupWindow(window, DimenUtil.getRealWidth(MainActivity.this, 768, 600),
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            selectDev.setFocusable(true);
            selectDev.setBackgroundDrawable(new ColorDrawable(0x000000));
            selectDev.setOnDismissListener(new popOnDismissListener());
//        } else {
//            mSelectDevAdapter.notifyDataSetChanged();
//            Log.d(TAG, "showSelectDevWindow: " + devList.get(0).isSelected());
//        }
        selectDev.showAtLocation(mContent, Gravity.CENTER, 0, 0);
    }

    private List<DevBean> getDevList() {
        if (conManager == null) {
            conManager = Manager.getManager();
        }
        return conManager.getLinkingDev();
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
                    showBackground();
                    showFabMenu();
                }
                break;
            case R.id.pop_open_wifi_create_hotspot:
                isOpenWifi.dismiss();
                boolean flag = createHotspot();
                if (!flag) {
                    Toast.makeText(MainActivity.this, getString(R.string.create_hotspot_fail), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, LinkActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.pop_open_wifi_open_wifi:
                mWifiManager.setWifiEnabled(true);
                isOpenWifi.dismiss();
                break;
            case R.id.pop_open_wifi_cancel:
                isOpenWifi.dismiss();
                break;
            case R.id.pop_new_file_cancel:
                createFile.dismiss();
                hideBackground();
                break;
            case R.id.pop_new_file_sure:
                newFile();
                break;
            case R.id.pop_select_dev_cancel:
                selectDev.dismiss();
                break;
            case R.id.pop_select_dev_sure:
                boolean isSelectDev = false;
                for (int i = 0; i < devList.size(); i++) {
                    if (devList.get(i).isSelected()) {
                        isSelectDev = true;
                        break;
                    }
                }
                if (isSelectDev) {
                    BaseFragment baseFragment = getCurFragmentByNavId(curFragmentNavId);
                    baseFragment.sendFile(devList);
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.no_dev_selected), Toast.LENGTH_SHORT).show();
                }
                selectDev.dismiss();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fabBtnList != null) {
            fabBtnList.clear();
            fabBtnList = null;
        }
        if (devList != null) {
            devList.clear();
            devList = null;
        }
        if (fabButton != null) {
            fabButton = null;
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

    public void changeFabBtnImage(boolean flag) {
        if (flag) {
            mFab.setImageResource(R.mipmap.fab_more);
        } else {
            mFab.setImageResource(R.mipmap.fab_finish);
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
    private class popOnDismissListener implements PopupWindow.OnDismissListener {

        @Override
        public void onDismiss() {
            hideBackground();
        }
    }
}
