package com.example.asus88.finaldesgin.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.adapter.SelectDevAdapter;
import com.example.asus88.finaldesgin.bean.DevBean;
import com.example.asus88.finaldesgin.itemDecoration.LineItemDecoration;
import com.example.asus88.finaldesgin.util.DimenUtil;
import com.example.asus88.finaldesgin.util.WifiUtil;
import com.zhy.autolayout.AutoLayoutActivity;

import java.util.List;

/**
 * Created by asus88 on 2016/12/21.
 */

public class BaseActivity extends AutoLayoutActivity {
    private static final String TAG = "BaseActivity";

    private PopupWindow isOpenWifi;
    private PopupWindow selectDev;
    private RecyclerView devRecycler;
    private SelectDevAdapter mSelectDevAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 修改状态栏颜色
     *
     * @param activity
     * @param colorId
     */
    public void setStatusBarColor(Activity activity, int colorId) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(colorId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showIsOpenWifiWindow(final Context context, View view, final WifiManager mWifiManager, PopupWindow.OnDismissListener dismissListener) {
        if (isOpenWifi == null) {
            View window = LayoutInflater.from(context).inflate(R.layout.popup_window_is_open_wifi, null);
            Button createHotspot = (Button) window.findViewById(R.id.pop_open_wifi_create_hotspot);
            Button openWifi = (Button) window.findViewById(R.id.pop_open_wifi_open_wifi);
            Button cancel = (Button) window.findViewById(R.id.pop_open_wifi_cancel);
            isOpenWifi = new PopupWindow(window, DimenUtil.getRealWidth(context, 768, 660),
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            isOpenWifi.setFocusable(true);
            isOpenWifi.setBackgroundDrawable(new ColorDrawable(0x000000));
            isOpenWifi.setOnDismissListener(dismissListener);

            createHotspot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isOpenWifi.dismiss();
                    boolean flag = WifiUtil.createHotspot(mWifiManager);
                    if (!flag) {
                        Toast.makeText(context, getString(R.string.create_hotspot_fail), Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(context, LinkActivity.class);
                        startActivity(intent);
                    }
                }
            });

            openWifi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWifiManager.setWifiEnabled(true);
                    isOpenWifi.dismiss();
                    Intent intent = new Intent(context, LinkActivity.class);
                    startActivity(intent);
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isOpenWifi.dismiss();
                }
            });
        }
        isOpenWifi.showAtLocation(view, Gravity.CENTER, 0, 0);
    }


    public void showSelectDevWindow(final Context context, View view, final List<DevBean> devList, PopupWindow.OnDismissListener dismissListener) {
        if (selectDev == null) {
            View window = LayoutInflater.from(context).inflate(R.layout.popup_window_select_dev_to_send, null);
            devRecycler = (RecyclerView) window.findViewById(R.id.pop_select_dev_recyclerView);
            mSelectDevAdapter = new SelectDevAdapter(context, devList);
            devRecycler.setLayoutManager(new LinearLayoutManager(context));
            devRecycler.setAdapter(mSelectDevAdapter);
            if(devList.size()>3){
                devRecycler.setLayoutParams(new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,
                        DimenUtil.getRealHeight(context, 1280, 330)));
            }
            devRecycler.addItemDecoration(new LineItemDecoration(context, 20, 20, R.drawable.line_item_decoration));
            Button cancel = (Button) window.findViewById(R.id.pop_select_dev_cancel);
            Button sure = (Button) window.findViewById(R.id.pop_select_dev_sure);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectDev.dismiss();
                }
            });
            sure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isSelectDev = false;
                    for (int i = 0; i < devList.size(); i++) {
                        if (devList.get(i).isSelected()) {
                            isSelectDev = true;
                            break;
                        }
                    }
                    if (isSelectDev) {
                        sendFile(devList);
                    } else {
                        Toast.makeText(context, getString(R.string.no_dev_selected), Toast.LENGTH_SHORT).show();
                    }
                    selectDev.dismiss();
                }
            });
            selectDev = new PopupWindow(window, DimenUtil.getRealWidth(context, 768, 600),
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            selectDev.setFocusable(true);
            selectDev.setBackgroundDrawable(new ColorDrawable(0x000000));
            selectDev.setOnDismissListener(dismissListener);
        } else {
            mSelectDevAdapter.notifyDataSetChanged();
        if(devList.size()>3){
            devRecycler.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    DimenUtil.getRealHeight(context, 1280, 300)));
        }else{
            devRecycler.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT));
        }
        }
        selectDev.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    public void sendFile(List<DevBean> list) {

    }
}
