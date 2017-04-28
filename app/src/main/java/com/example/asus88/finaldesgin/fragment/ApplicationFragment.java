package com.example.asus88.finaldesgin.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.activity.MainActivity;
import com.example.asus88.finaldesgin.adapter.ApplicationAdapter;
import com.example.asus88.finaldesgin.bean.ApplicationBean;
import com.example.asus88.finaldesgin.bean.Bean;
import com.example.asus88.finaldesgin.bean.DevBean;
import com.example.asus88.finaldesgin.connection.Transfer;
import com.example.asus88.finaldesgin.myViews.LVBlock;
import com.example.asus88.finaldesgin.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus88 on 2016/12/21.
 */

public class ApplicationFragment extends BaseFragment implements ApplicationAdapter.onItemClickListener {
    private static final String TAG = "ApplicationFragment";
    private static final int REQUEST_UNINSTALL = 101;
    private View mView;
    private RecyclerView mRecyclerView;
    private ApplicationAdapter mAdapter;
    private List<ApplicationBean> mApplicationBeanList;
    private PackageManager mManager;
    private ApplicationBean bean;
    private String path;
    private int uninstallPosition;
    private RelativeLayout loadingLayout;
    private LVBlock loadingView;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    loadingView.stopAnim();
                    loadingLayout.setVisibility(View.GONE);
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
        loadingLayout = (RelativeLayout) mView.findViewById(R.id.loading_layout);
        loadingLayout.setVisibility(View.VISIBLE);
        loadingView = (LVBlock) mView.findViewById(R.id.loading_view);
        loadingView.startAnim();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mApplicationBeanList != null) {
            mApplicationBeanList.clear();
            mApplicationBeanList = null;
        }
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
    public void onDeleteClick(int position) {
        uninstallPosition = position;
        Uri uri = Uri.parse("package:" + mApplicationBeanList.get(position).getPackName());
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        startActivityForResult(intent, REQUEST_UNINSTALL);
    }

    @Override
    public void onLongClick() {
        ((MainActivity) getActivity()).changeFabBtnImage(false);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UNINSTALL && resultCode == Activity.RESULT_OK) {
            mApplicationBeanList.remove(uninstallPosition);
            mAdapter.notifyItemRemoved(uninstallPosition);
        }
    }

    @Override
    public List getDataList() {
        return mApplicationBeanList;
    }

    public int getFabButtonNum() {
        return 2;
    }

    public boolean getFabBtnMode() {
        if (mAdapter == null) {
            return false;
        }
        return mAdapter.getDeleteMode();

    }

    /**
     * 删除模式完成
     */
    public void finishDelMode() {
        mAdapter.setDeleteMode(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyRecyclerView(List<Bean> list) {
        mApplicationBeanList.removeAll(list);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateMediaDataBase(List<Bean> list) {
    }

    @Override
    public void setAllUnSelected() {
        int size = mApplicationBeanList.size();
        for (int i = 0; i < size; i++) {
            if (mApplicationBeanList.get(i).isSelected()) {
                mApplicationBeanList.get(i).setSelected(false);
                mAdapter.notifyItemChanged(i);
            }
        }
    }


    @Override
    public void sendFile(List<DevBean> devList) {
        for (DevBean bean : devList) {
            if (bean.isSelected()) {
                Transfer transfer = bean.getTransfer();
                for (ApplicationBean fileBean : mApplicationBeanList) {
                    if (fileBean.isSelected()) {
                        try {
                            transfer.addTask(fileBean.getPath(), fileBean.getName() + ".apk");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        setAllUnSelected();
    }
}
