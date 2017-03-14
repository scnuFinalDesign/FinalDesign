package com.example.asus88.finaldesgin.activity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
    public WifiManager mWifiManager;
    private FrameLayout background;
    private TextView[] fabButton;
    private int fabButtonSize;
    private int oldSize;
    private List<FabMenuButtonBean> fabBtnList;
    private List<DevBean> devList;
    private int type;
    private int marLeft;
    private boolean isBackgroundShow;
    private popOnDismissListener mOnDismissListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        conManager = Manager.getManager();
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        conManager.setOnDevMapChangeListener(this);
        fabButton = new TextView[5];
        mOnDismissListener = new popOnDismissListener();
        devList = new ArrayList<>();
        marLeft = DimenUtil.getRealWidth(this, 768, 84);

    }

    @Override
    public void onDevNumChange(Dev dev, boolean isAdd) {

    }

    @Override
    public void onTransferStateChange(Dev dev, boolean isEnabled) {
        if (!isEnabled) {
            Toast.makeText(this, "与" + dev.getName() + "链接已断开", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNetWorkStateChange() {
        Toast.makeText(this, getString(R.string.check_your_network_setting), Toast.LENGTH_SHORT).show();
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
                    removeFabButtonFromBg();
                    hideBackground();
                }
            });
            initFabButton();
            rootView.addView(background);
        }
        addFabButtonToBg();
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
        newDirectory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFabButtonFromBg();
                type = 1;
                showFileWindow(type);
            }
        });
        FabMenuButtonBean newFile = new FabMenuButtonBean("newFile", R.drawable.bg_fab_new_file_btn);
        newFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFabButtonFromBg();
                type = 0;
                showFileWindow(type);
            }
        });
        FabMenuButtonBean delete = new FabMenuButtonBean("delete", R.drawable.bg_fab_delete_btn);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFile();
                removeFabButtonFromBg();
                hideBackground();
            }
        });
        FabMenuButtonBean link = new FabMenuButtonBean("link", R.drawable.bg_fab_link_btn);
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
        if (oldSize != fabButtonSize) {
            flag = true;
            oldSize = fabButtonSize;
        }
        for (int i = start; i < 5; i++) {
            Log.d(TAG, "addFabButtonToBg: i=" + i);
            if (fabButton[i] == null) {
                fabButton[i] = TextViewFactory.createTextView(EBaseActivity.this, fabBtnList.get(i));
            }
            if (flag) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fabButton[i].getLayoutParams();
                params.setMargins(marLeft, DimenUtil.getRealHeight(EBaseActivity.this, 1280, (firMargin + (i - start) * 160)), 0, 0);
            }
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
