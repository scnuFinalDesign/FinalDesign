package com.example.asus88.finaldesgin.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.adapter.LinkAdapter;
import com.example.asus88.finaldesgin.connection.Dev;
import com.example.asus88.finaldesgin.connection.Manager;
import com.example.asus88.finaldesgin.connection.Transfer;
import com.example.asus88.finaldesgin.itemDecoration.LineItemDecoration;

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
        wifiName = getIntent().getStringExtra("account");
        wifiPassWord = getIntent().getStringExtra("password");
        conManager = Manager.getManager();
        conManager.setOnDevMapChangeListener(this);
        mDevList = new ArrayList<>();
        getDevDataFromMap();
        Log.d(TAG, "initData: "+mDevList.size());
        mAdapter = new LinkAdapter(this, mDevList);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.addItemDecoration(new LineItemDecoration(this, 0, 0, R.drawable.line_item_decoration));
        mRecycler.setAdapter(mAdapter);
        conManager.searchDevice();
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

    @Override
    public void onItemClick(int position) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        conManager.stopSearchDevice();
    }

    /**
     * 搜索列表改变是触发
     *
     * @param dev
     * @param isAdd 增加或减少设备
     */
    @Override
    public void onDevNumChange(Dev dev, boolean isAdd) {
        Log.d(TAG, "onDevNumChange: "+dev.getName());
        if (isAdd) {
            dev.setTransferState(0);
            mDevList.add(dev);
            Log.d(TAG, "onDevNumChange: "+mDevList.size());
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
        int position = mDevList.indexOf(dev);
        if (isEnabled) {
            mDevList.get(position).setTransferState(1);
        } else {
            mDevList.get(position).setTransferState(2);
        }
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
