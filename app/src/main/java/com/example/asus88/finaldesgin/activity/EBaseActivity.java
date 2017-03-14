package com.example.asus88.finaldesgin.activity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
        marLeft = DimenUtil.getRealWidth(this, 1280, 140);

    }

    @Override
    public void onDevNumChange(Dev dev, boolean isAdd) {
// link
    }

    @Override
    public void onTransferStateChange(Dev dev, boolean isEnabled) {
// 3个页面
    }

    @Override
    public void onNetWorkStateChange() {
//// TODO: 2017/3/11 3个页面都重写
    }

    @Override
    public void onCreateTransferFail(Dev dev) {
// link
    }

    public void setFabButtonSize(int fabButtonSize) {
        this.fabButtonSize = fabButtonSize;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        conManager.setOnDevMapChangeListener(null);
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
    }

    public void hideBackground() {
        background.setVisibility(View.GONE);
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
        int start=5 - fabButtonSize;
        int firMargin=getFirstBtnMarTop(fabButtonSize, 70, 20);
        for (int i = start; i < 5; i++) {
            if (fabButton[i] == null) {
                fabButton[i] = TextViewFactory.createTextView(EBaseActivity.this, fabBtnList.get(i));
            }
            if (oldSize != fabButtonSize) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fabButton[i].getLayoutParams();
                params.setMargins(marLeft, DimenUtil.getRealHeight(EBaseActivity.this, 768, (firMargin + (i - start) * 90)), 0, 0);
                oldSize = fabButtonSize;
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
            return (768 - btnNum * btnHeight - (btnNum - 1) * margin) / 2;
        } else {
            return (768 - btnHeight - (btnNum - 1) * (btnHeight + margin)) / 2;
        }
    }

    private void removeFabButtonFromBg() {
        for (int i = 5 - fabButtonSize; i < 5; i++) {
            background.removeView(fabButton[i]);
        }
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
