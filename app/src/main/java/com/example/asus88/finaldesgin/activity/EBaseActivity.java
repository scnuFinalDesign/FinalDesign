package com.example.asus88.finaldesgin.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.TextViewFactory;
import com.example.asus88.finaldesgin.bean.DevBean;
import com.example.asus88.finaldesgin.bean.FabMenuButtonBean;
import com.example.asus88.finaldesgin.connection.Dev;
import com.example.asus88.finaldesgin.connection.Manager;
import com.example.asus88.finaldesgin.connection.Transfer;
import com.example.asus88.finaldesgin.util.AnimationUtil;
import com.example.asus88.finaldesgin.util.DimenUtil;
import com.example.asus88.finaldesgin.util.WifiUtil;

import java.util.ArrayList;
import java.util.List;

import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLING;

/**
 * Created by asus88 on 2017/3/11.
 */

public class EBaseActivity extends BaseActivity implements Manager.onDevMapChangeListener {

    private static final String TAG = "EBaseActivity";
    public Manager conManager;
    public volatile WifiManager mWifiManager;
    private FrameLayout background;
    private TextView[] fabButton;
    private int fabButtonSize;
    private int oldSize;
    private List<FabMenuButtonBean> fabBtnList;
    private List<DevBean> devList;
    private List<Animator> mAnimatorList;
    private int type;
    private int marLeft;
    private boolean isBackgroundShow;
    private popOnDismissListener mOnDismissListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        conManager = Manager.getManager();
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        fabButton = new TextView[5];
        mOnDismissListener = new popOnDismissListener();
        devList = new ArrayList<>();
        mAnimatorList = new ArrayList<>();
        marLeft = DimenUtil.getRealWidth(this, 768, 84);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        conManager.setOnDevMapChangeListener(this);
    }

    @Override
    public void onDevNumChange(Dev dev, boolean isAdd) {

    }

    @Override
    public void onTransferStateChange(final Dev dev, boolean isEnabled) {
        Log.d(TAG, "onTransferStateChange: " + isEnabled);
        if (!isEnabled) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Transfer t = conManager.getTransferFromMap(dev);
                    if (t != null) {
                        Log.d(TAG, "run: close");
                        t.close();
                    }
                    Toast.makeText(EBaseActivity.this, "与" + dev.getName() + "链接已断开", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onNetWorkStateChange() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EBaseActivity.this, getString(R.string.check_your_network_setting), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreateTransferFail(Dev dev) {

    }

    public void setFabButtonSize(int fabButtonSize) {
        this.fabButtonSize = fabButtonSize;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        conManager.setOnDevMapChangeListener(null);
        if (devList != null) {
            devList.clear();
            devList = null;
        }
        if (fabBtnList != null) {
            fabBtnList.clear();
            fabBtnList = null;
        }
        if (mAnimatorList != null) {
            mAnimatorList.clear();
            mAnimatorList = null;
        }
    }

    public void showBackground() {
        if (background == null) {
            ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();
            background = new FrameLayout(this);
            background.setBackgroundColor(getResources().getColor(R.color.fab_menu_color));
            background.setLayoutParams(new FrameLayout.LayoutParams(rootView.getWidth(), rootView.getHeight()));
            background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideButton(mAnimatorList);
                }
            });
            initFabButton();
            rootView.addView(background);
        }
        addFabButtonToBg();
        showButton(mAnimatorList);
        background.setVisibility(View.VISIBLE);
        isBackgroundShow = true;
    }

    public void hideBackground() {
        background.setVisibility(View.GONE);
        isBackgroundShow = false;
    }

    public void initFabButton() {
        fabBtnList = new ArrayList<>();
        FabMenuButtonBean newDirectory = new FabMenuButtonBean("newDirectory", R.drawable.bg_fab_new_btn);
        newDirectory.setDrawableLeftId(R.mipmap.ic_folder_white);
        newDirectory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFabButtonFromBg();
                type = 1;
                showFileWindow(type);
            }
        });
        FabMenuButtonBean newFile = new FabMenuButtonBean("newFile", R.drawable.bg_fab_new_file_btn);
        newFile.setDrawableLeftId(R.mipmap.ic_file_white);
        newFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFabButtonFromBg();
                type = 0;
                showFileWindow(type);
            }
        });
        FabMenuButtonBean delete = new FabMenuButtonBean("delete", R.drawable.bg_fab_delete_btn);
        delete.setDrawableLeftId(R.mipmap.ic_delete_white);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFile();
                hideButton(mAnimatorList);
            }
        });
        FabMenuButtonBean link = new FabMenuButtonBean("link", R.drawable.bg_fab_link_btn);
        link.setDrawableLeftId(R.mipmap.ic_link_white);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFabButtonFromBg();
                int state = WifiUtil.getWifiApState(mWifiManager);
                if ((mWifiManager.getWifiState() == WIFI_STATE_DISABLING ||
                        mWifiManager.getWifiState() == WIFI_STATE_DISABLED) &&
                        (state == 10 || state == 11)) {
                    showIsOpenWifiWindow(EBaseActivity.this, background, mWifiManager, mOnDismissListener);
                } else {
                    Intent intent = new Intent(EBaseActivity.this, LinkActivity.class);
                    startActivity(intent);
                    hideBackground();
                }
            }
        });
        FabMenuButtonBean send = new FabMenuButtonBean("send", R.drawable.bg_fab_send_btn);
        send.setDrawableLeftId(R.mipmap.ic_send_white);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFabButtonFromBg();
                if (getSelectedSize() > 0) {
                    devList.clear();
                    devList.addAll(conManager.getLinkingDev());
                    if (devList.size() > 0) {
                        showSelectDevWindow(EBaseActivity.this, background, devList, mOnDismissListener);
                    } else {
                        Toast.makeText(EBaseActivity.this, getString(R.string.no_link), Toast.LENGTH_SHORT).show();
                        hideBackground();
                    }
                } else {
                    Toast.makeText(EBaseActivity.this, getString(R.string.no_selected), Toast.LENGTH_SHORT).show();
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

    private void addFabButtonToBg() {
        int start = 5 - fabButtonSize;
        int firMargin = getFirstBtnMarTop(fabButtonSize, 120, 40);
        boolean flag = false;
        mAnimatorList.clear();
        if (oldSize != fabButtonSize) {
            flag = true;
            oldSize = fabButtonSize;
        }
        for (int i = start; i < 5; i++) {
            if (fabButton[i] == null) {
                fabButton[i] = TextViewFactory.createTextView(EBaseActivity.this, fabBtnList.get(i));
            }
            if (flag) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fabButton[i].getLayoutParams();
                params.setMargins(marLeft, DimenUtil.getRealHeight(EBaseActivity.this, 1280, (firMargin + (i - start) * 160)), 0, 0);
            }
            mAnimatorList.add(AnimationUtil.createAnimator(fabButton[i], "elevation", 0, 20));
            mAnimatorList.add(AnimationUtil.createAnimator(fabButton[i], "alpha", 0, 1));
            background.addView(fabButton[i]);
        }
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
            return (1280 - btnNum * btnHeight - (btnNum - 1) * margin) / 2;
        } else {
            return (1280 - btnHeight - (btnNum - 1) * (btnHeight + margin)) / 2;
        }
    }

    private void removeFabButtonFromBg() {
        for (int i = 5 - fabButtonSize; i < 5; i++) {
            background.removeView(fabButton[i]);
        }
    }

    private void showButton(List<Animator> list) {
        AnimatorSet set = new AnimatorSet();
        set.setDuration(500);
        set.setInterpolator(new DecelerateInterpolator());
        set.playTogether(list);
        set.start();
    }

    private void hideButton(List<Animator> list) {
        int length = list.size();
        for (int i = 0; i < length; i++) {
            ObjectAnimator animator = (ObjectAnimator) list.get(i);
            if (i % 2 == 0) {
                animator.setFloatValues(20, 0);
            } else {
                animator.setFloatValues(1, 0);
            }
        }
        AnimatorSet set = new AnimatorSet();
        set.setDuration(500);
        set.setInterpolator(new DecelerateInterpolator());
        set.playTogether(list);
        set.start();
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                removeFabButtonFromBg();
                hideBackground();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public boolean isBackgroundShow() {
        return isBackgroundShow;
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

    public void showFileWindow(int type) {
        //main overwrite
    }

    public void deleteFile() {

    }

    public int getSelectedSize() {
        return 0;
    }
}
