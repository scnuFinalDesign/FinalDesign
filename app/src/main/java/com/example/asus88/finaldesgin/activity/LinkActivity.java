package com.example.asus88.finaldesgin.activity;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.adapter.LinkAdapter;
import com.example.asus88.finaldesgin.connection.Dev;
import com.example.asus88.finaldesgin.connection.Manager;
import com.example.asus88.finaldesgin.connection.Transfer;
import com.example.asus88.finaldesgin.itemDecoration.LineItemDecoration;
import com.example.asus88.finaldesgin.util.WifiUitl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
    @BindView(R.id.link_act_refresh)
    ImageView mRefresh;
    @BindView(R.id.link_act_recycler)
    RecyclerView mRecycler;

    private List<Dev> mDevList;
    private LinkAdapter mAdapter;

    private Manager conManager;
    private WifiManager mWifiManager;

    private String wifiName;
    private String wifiPassWord;

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
        mDevList = new ArrayList<>();
        getDevDataFromMap();
        mAdapter = new LinkAdapter(this, mDevList);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.addItemDecoration(new LineItemDecoration(this, 0, 0, R.drawable.line_item_decoration));
        mRecycler.setAdapter(mAdapter);
        conManager.searchDevice();
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        int state = WifiUitl.getWifiApState(mWifiManager);
        if (state == 12 || state == 13) {
            //state create hotspot
            //todo show hotspot message
            getWifiApNameAndPassWord();
        } else {
            // state link wifi
            // todo scan qrCode to link wifi
        }
    }

    private void initEvents() {
        mAdapter.setOnItemClickListener(this);
        mBack.setOnClickListener(this);
    }

    private void getDevDataFromMap() {
        Map<Dev, Transfer> map = conManager.getDevMap();
        Iterator<Map.Entry<Dev, Transfer>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Dev, Transfer> entry = iterator.next();
            Dev dev = entry.getKey();
            Transfer transfer=entry.getValue();
            if(transfer==null){
                dev.setTransferState(0);
            }else if(transfer.isEnable()){
                dev.setTransferState(1);
            }else{
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

    @Override
    public void onItemClick(int position) {
        Dev dev = mDevList.get(position);
        int state=dev.getTransferState();
        if (state==1) {
            //todo break link
            conManager.getTransferFromMap(dev).close();
        } else {
            conManager.createTransfer(dev);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        conManager.stopSearchDevice();
    }

    private void showBreakLinkWindow() {
        View window = LayoutInflater.from(this).inflate(R.layout.popup_window_ask, null);
        TextView title = (TextView) window.findViewById(R.id.pop_ask_title);
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
        } else {
            mDevList.get(position).setTransferState(2);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyItemChanged(position);
            }
        });
    }

    @Override
    public void onNetWorkStateChange() {
// todo toast check network
    }

    @Override
    public void onCreateTransferFail(Dev dev) {
//todo create transfer fail
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.link_act_back:
                finish();
                break;
        }
    }
}
