package com.example.asus88.finaldesgin.fragment;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.adapter.ApplicationAdapter;
import com.example.asus88.finaldesgin.bean.ApplicationBean;
import com.example.asus88.finaldesgin.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus88 on 2016/12/21.
 */

public class ApplicationFragment extends BaseFragment implements ApplicationAdapter.onItemClickListener {
    private static final String TAG = "ApplicationFragment";
    private View mView;
    private RecyclerView mRecyclerView;
    private ApplicationAdapter mAdapter;
    private List<ApplicationBean> mApplicationBeanList;
    private PackageManager mManager;
    private ApplicationBean bean;
    private String path;
    private boolean isSelected;
    private int mBgColor;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mAdapter.notifyDataSetChanged();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_content, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
        mApplicationBeanList = new ArrayList<>();
        mAdapter = new ApplicationAdapter(mView.getContext(), mApplicationBeanList);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mView.getContext(), 3));
        mRecyclerView.setAdapter(mAdapter);
        mBgColor = getResources().getColor(R.color.fab_menu_color);
        new Thread(new Runnable() {
            @Override
            public void run() {
                getApplicationData();
                Message message = Message.obtain();
                message.what = 1;
                mHandler.sendMessage(message);
            }
        }).start();
        return mView;
    }

    private void getApplicationData() {
        mManager = mView.getContext().getPackageManager();
        List<PackageInfo> mAllPackages = mManager.getInstalledPackages(0);
        for (int i = 0; i < mAllPackages.size(); i++) {
            PackageInfo packageInfo = mAllPackages.get(i);
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                bean = new ApplicationBean();
                path = packageInfo.applicationInfo.sourceDir;
                bean.setPath(path);
                bean.setPackName(packageInfo.packageName);
                bean.setName(packageInfo.applicationInfo.loadLabel(mManager).toString());
                bean.setNumber(packageInfo.versionName);
                bean.setSize(FileUtil.getFileSize(path));
                bean.setIcon(packageInfo.applicationInfo.loadIcon(mView.getContext().getPackageManager()));
                mApplicationBeanList.add(bean);
            }
        }
    }


    @Override
    public void onDeleteClick(View view, int position) {

    }

    @Override
    public void onItemLongClick() {

    }


    @Override
    public List getDataList() {
        return mApplicationBeanList;
    }

    public int getFabButtonNum() {
        return 2;
    }
    @Override
    public void notifyRecyclerView() {
        mAdapter.notifyDataSetChanged();
    }

}
