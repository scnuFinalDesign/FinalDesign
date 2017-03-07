package com.example.asus88.finaldesgin.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import static android.net.wifi.WifiConfiguration.Protocol.WPA;

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

    private static final int SACN_QR_CODE_REQUSET = 1;
    private List<Dev> mDevList;
    private LinkAdapter mAdapter;

    private FrameLayout background;

    private Manager conManager;
    private WifiManager mWifiManager;

    private boolean isCreate;
    private String wifiName;
    private String wifiPassWord;
    private String encodeType;
    private Bitmap qrCode;
    private ImageView qrImage;
    private int qrWidth;
    private int qrHeight;

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
            isCreate = true;
            mImage.setImageResource(R.mipmap.icon_qrcode);
        } else {
            // state link wifi
            // todo scan qrCode to link wifi
            isCreate = false;
            mImage.setImageResource(R.mipmap.icon_scan);
        }

        qrHeight = DimenUtil.getRealHeight(this, 1280, 500);
        qrWidth = DimenUtil.getRealWidth(this, 768, 500);
        mTitle.setText(getString(R.string.device));
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

    private void getWifiApInfo() {
        try {
            Method method = mWifiManager.getClass().getDeclaredMethod("getWifiApConfiguration");
            WifiConfiguration configuration = (WifiConfiguration) method.invoke(mWifiManager);
            wifiName = configuration.SSID;
            wifiPassWord = configuration.preSharedKey;
            if (configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)
                    || configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP)
                    || configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
                encodeType = "WPA";
            } else if (configuration.wepKeys[0] != null) {
                encodeType = "WEP";
            } else if (configuration.allowedKeyManagement.get(4)) {
                encodeType = "WPA2";
            } else {
                encodeType = "NONE";

            }
            Log.d(TAG, "getWifiApInfo: name:" + wifiName);
            Log.d(TAG, "getWifiApInfo: pass:" + wifiPassWord);
            Log.d(TAG, "getWifiApInfo: type" + encodeType);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private String getQrCodeContent() {
        StringBuilder builder = new StringBuilder("WIFI:S:");
        builder.append(wifiName);
        builder.append(";P:");
        builder.append(wifiPassWord);
        builder.append(";T:");
        builder.append(encodeType);
        builder.append(";");
        return builder.toString();
    }

    private void showQrCode() {
        ViewGroup rootView = (ViewGroup) getWindow().getDecorView();
        if (background == null) {
            background = new FrameLayout(this);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(rootView.getWidth(), rootView.getHeight());
            background.setLayoutParams(layoutParams);
            background.setBackgroundColor(getResources().getColor(R.color.fab_menu_color));
            background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideQrCode();
                }
            });
            if (qrImage == null) {
                qrImage = new ImageView(this);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(qrWidth, qrHeight);
                params.gravity = Gravity.CENTER;
                qrImage.setLayoutParams(params);
                if (qrCode == null) {
                    getWifiApInfo();
                    qrCode = createQrCode(getQrCodeContent(), qrWidth, qrHeight);
                }
                qrImage.setImageBitmap(qrCode);
            }
            background.addView(qrImage);
        }
        rootView.addView(background);
    }

    private void hideQrCode() {
        ViewGroup rootView = (ViewGroup) getWindow().getDecorView();
        rootView.removeView(background);
    }

    /**
     * @param content
     * @param w
     * @param h
     * @return
     */
    private Bitmap createQrCode(String content, int w, int h) {
        int width = DimenUtil.getRealWidth(LinkActivity.this, 768, w);
        int height = DimenUtil.getRealHeight(LinkActivity.this, 1280, h);
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, String> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
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
        Log.d(TAG, "onResume: ");
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
                if (isCreate) {
                    showQrCode();
                } else {
                    Intent intent = new Intent(LinkActivity.this, CaptureActivity.class);
                    startActivityForResult(intent, SACN_QR_CODE_REQUSET);
                }
                break;
        }
    }

    private void changeBgAlpha(float f) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = f;
        getWindow().setAttributes(lp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SACN_QR_CODE_REQUSET && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                String result = bundle.getString("result");
                Log.d(TAG, "onActivityResult: " + result);
                if (!TextUtils.isEmpty(result) && result.contains("WIFI") && result.contains("S:")
                        && result.contains("P")) {
                    getWifiInfoFromQrCode(result);
                    linkWifi(wifiName, wifiPassWord, encodeType);
                }
            }
        }
    }

    private void getWifiInfoFromQrCode(String str) {
        String pTemp = str.substring(str.indexOf("P:"));
        wifiPassWord = pTemp.substring(2, pTemp.indexOf(";"));
        String sTemp = str.substring(str.indexOf("S:"));
        wifiName = sTemp.substring(2, sTemp.indexOf(";"));
        String eTemp = str.substring(str.indexOf("T:"));
        encodeType = eTemp.substring(2, eTemp.indexOf(";"));
    }

    private void linkWifi(String ssid, String password, String type) {
        if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(password)) {
            return;
        }

        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";
        WifiConfiguration temp = isWifiExist(ssid);
        if (temp != null) {
            Log.d(TAG, "linkWifi: exit");
            mWifiManager.removeNetwork(temp.networkId);
        }
        switch (type) {
            case "NONE":
                Log.d(TAG, "linkWifi: none");
                //  config.hiddenSSID = true;
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;
            case "IEEE8021XEAP":
                break;
            case "WEP":
                break;
            case "WPA":
                Log.d(TAG, "linkWifi: wpa");
                config.preSharedKey = "\"" + password + "\"";
                config.hiddenSSID = true;
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                config.allowedProtocols.set(WPA);
                config.status = WifiConfiguration.Status.ENABLED;
                break;
            case "WPA2":
                Log.d(TAG, "linkWifi: wpa2");
                config.preSharedKey = "\"" + password + "\"";
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                config.status = WifiConfiguration.Status.ENABLED;
                break;
            default:
                break;
        }
        final int id = mWifiManager.addNetwork(config);
        if (id != -1) {
            boolean f = mWifiManager.enableNetwork(id, true);
            Log.d(TAG, "linkWifi: id" + id + "state:" + f + config.SSID + ":" + config.preSharedKey);
        
        }
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    Log.d(TAG, "run: " + mWifiManager.getConfiguredNetworks().get(id-1).status);
//                }
//            }
//        }).start();
    }

    private WifiConfiguration isWifiExist(String SSID) {
        List<WifiConfiguration> cList = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration configuration : cList) {
            if (configuration.SSID.equals("\"" + SSID + "\""))
                return configuration;
        }
        return null;
    }
}
