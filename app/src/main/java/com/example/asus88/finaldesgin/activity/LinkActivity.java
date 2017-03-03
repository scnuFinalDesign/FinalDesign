package com.example.asus88.finaldesgin.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.adapter.LinkAdapter;
import com.example.asus88.finaldesgin.connection.Dev;
import com.example.asus88.finaldesgin.connection.Manager;
import com.example.asus88.finaldesgin.connection.Transfer;
import com.example.asus88.finaldesgin.itemDecoration.LineItemDecoration;
import com.example.asus88.finaldesgin.util.DimenUtil;
import com.example.asus88.finaldesgin.util.WifiUitl;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LinkActivity extends BaseActivity implements LinkAdapter.onItemClickListener, Manager.onDevMapChangeListener, View.OnClickListener {
    private static final String TAG = "LinkActivity";

    @BindView(R.id.link_act_back)
    ImageView mBack;
    @BindView(R.id.link_act_title)
    TextView mTitle;
    @BindView(R.id.link_act_recycler)
    RecyclerView mRecycler;
    @BindView(R.id.link_act_image)
    ImageView mImage;

    private List<Dev> mDevList;
    private LinkAdapter mAdapter;

    private FrameLayout background;

    private Manager conManager;
    private WifiManager mWifiManager;

    private boolean isCreater;
    private String wifiName;
    private String wifiPassWord;
    private String wifiType;
    private Bitmap qrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link);
        ButterKnife.bind(this);
        initData();
        initEvents();
    }

    private void initData() {
        conManager = Manager.getManager();
        conManager.setOnDevMapChangeListener(this);
        conManager.searchDevice();
        mDevList = new ArrayList<>();
        getDevDataFromMap();
        mAdapter = new LinkAdapter(this, mDevList);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.addItemDecoration(new LineItemDecoration(this, 0, 0, R.drawable.line_item_decoration));
        mRecycler.setAdapter(mAdapter);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        int state = WifiUitl.getWifiApState(mWifiManager);
        if (state == 12 || state == 13) {
            //state create hotspot
            //todo show hotspot message
            isCreater = true;
            mImage.setImageResource(R.mipmap.icon_qrcode);
            getWifiApNameAndPassWord();
            //// TODO: 2017/3/3 new thread to load
            qrCode = createQrCode(wifiName, wifiPassWord, wifiType, 500, 500);
        } else {
            // state link wifi
            // todo scan qrCode to link wifi
            isCreater = false;
            mImage.setImageResource(R.mipmap.icon_scan);
        }
    }

    private void initEvents() {
        mAdapter.setOnItemClickListener(this);
        mBack.setOnClickListener(this);
        mImage.setOnClickListener(this);
    }

    private void getDevDataFromMap() {
        Map<Dev, Transfer> map = conManager.getDevMap();
        Iterator<Map.Entry<Dev, Transfer>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Dev, Transfer> entry = iterator.next();
            Dev dev = entry.getKey();
            Transfer transfer = entry.getValue();
            if (transfer == null) {
                dev.setTransferState(0);
            } else if (transfer.isEnable()) {
                dev.setTransferState(1);
            } else {
                dev.setTransferState(2);
            }
            mDevList.add(dev);
        }

    }

    private void getWifiApNameAndPassWord() {
        try {
            Method method = mWifiManager.getClass().getDeclaredMethod("getWifiApConfiguration");
            WifiConfiguration configuration = (WifiConfiguration) method.invoke(mWifiManager);
            wifiName = configuration.SSID;
            wifiPassWord = configuration.preSharedKey;
            Log.d(TAG, "getWifiApNameAndPassWord: name:" + wifiName);
            Log.d(TAG, "getWifiApNameAndPassWord: pass:" + wifiPassWord);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param name     wifi name
     * @param passWord
     * @param type     wifi 加密类型
     * @return
     */
    private Bitmap createQrCode(String name, String passWord, String type, int w, int h) {
        StringBuilder builder = new StringBuilder("S:");
        builder.append(name);
        builder.append("P:");
        builder.append(passWord);
        builder.append("T:");
        builder.append(type);
        int width = DimenUtil.getRealWidth(LinkActivity.this, 768, w);
        int height = DimenUtil.getRealHeight(LinkActivity.this, 1280, h);
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, String> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix matrix = writer.encode(builder.toString(), BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (matrix.get(j, i)) {
                        pixels[i * width + j] = 0x000000;
                    } else {
                        pixels[i * width + j] = 0xffffff;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onItemClick(int position) {
        Dev dev = mDevList.get(position);
        int state = dev.getTransferState();
        if (state == 1) {
            showBreakLinkWindow(dev);
        } else {
            conManager.createTransfer(dev);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        conManager.stopSearchDevice();
        if (mDevList != null) {
            mDevList.clear();
            mDevList = null;
        }
        if (qrCode != null) {
            qrCode.recycle();
            qrCode = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        conManager.resumeSearch();
    }

    private void showBreakLinkWindow(final Dev dev) {
        changeBgAlpha(0.4f);
        View window = LayoutInflater.from(this).inflate(R.layout.popup_window_ask, null);
        TextView title = (TextView) window.findViewById(R.id.pop_ask_title);
        Button sure = (Button) window.findViewById(R.id.pop_ask_sure);
        Button cancel = (Button) window.findViewById(R.id.pop_ask_cancel);
        title.setText(getString(R.string.is_close_link));

        final PopupWindow popupWindow = new PopupWindow(window, DimenUtil.getRealWidth(LinkActivity.this, 768, 660),
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x000000));
        popupWindow.setFocusable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                changeBgAlpha(1f);
            }
        });
        popupWindow.showAtLocation(LinkActivity.this.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Transfer transfer = conManager.getTransferFromMap(dev);
                if (transfer != null) {
                    transfer.close();
                }
                popupWindow.dismiss();
            }
        });
    }

    /**
     * 搜索列表改变是触发
     *
     * @param dev
     * @param isAdd 增加或减少设备
     */
    @Override
    public void onDevNumChange(Dev dev, boolean isAdd) {
        if (isAdd) {
            dev.setTransferState(0);
            mDevList.add(dev);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //  mAdapter.notifyItemInserted(mDevList.size() - 1);
                    mAdapter.notifyDataSetChanged();
                }
            });
        } else {
            final int position = mDevList.indexOf(dev);
            mDevList.remove(position);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyItemRemoved(position);
                }
            });
        }
        Log.d(TAG, "onDevNumChange: size" + mDevList.size());
    }

    /**
     * 列表中的设备链接状态发生改变
     *
     * @param dev
     * @param isEnabled 链接是否可用
     */
    @Override
    public void onTransferStateChange(Dev dev, boolean isEnabled) {
        final int position = mDevList.indexOf(dev);
        if (isEnabled) {
            mDevList.get(position).setTransferState(1);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyItemChanged(position);
                }
            });
        } else {
            mDevList.get(position).setTransferState(2);
            mDevList.remove(position);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyItemRemoved(position);
                }
            });
        }
        Log.d(TAG, "onTransferStateChange: size:" + mDevList.size());
    }

    @Override
    public void onNetWorkStateChange() {
        Toast.makeText(LinkActivity.this, getString(R.string.check_your_network_setting), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateTransferFail(Dev dev) {
        StringBuilder builder = new StringBuilder("与" + dev.getName());
        builder.append(getString(R.string.create_transfer_fail));
        Toast.makeText(LinkActivity.this, builder.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.link_act_back:
                finish();
                break;
            case R.id.link_act_image:
                break;
        }
    }

    private void changeBgAlpha(float f) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = f;
        getWindow().setAttributes(lp);
    }
}
