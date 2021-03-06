package com.example.asus88.finaldesgin.fragment;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.adapter.VideoAdapter;
import com.example.asus88.finaldesgin.bean.Bean;
import com.example.asus88.finaldesgin.bean.VideoBean;
import com.example.asus88.finaldesgin.itemDecoration.LineItemDecoration;
import com.example.asus88.finaldesgin.myViews.LVBlock;
import com.example.asus88.finaldesgin.util.FileUtil;
import com.example.asus88.finaldesgin.util.LogUtil;
import com.example.asus88.finaldesgin.util.TimeUtil;
import com.example.asus88.finaldesgin.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus88 on 2016/12/21.
 */

public class VideoFragment extends BaseFragment implements VideoAdapter.onItemClickListener {
    private static final String TAG = "VideoFragment";

    private View mView;
    private RecyclerView mRecyclerView;
    private List<VideoBean> mVideoBeanList;
    private VideoAdapter mAdapter;
    private ContentResolver mResolver;
    private VideoBean bean;
    private String size;
    private RelativeLayout loadingLayout;
    private LVBlock loadingView;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    loadingView.stopAnim();
                    loadingLayout.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtil.logd(TAG, "create");
        mView = inflater.inflate(R.layout.fragment_content, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
        mVideoBeanList = new ArrayList<>();
        mResolver = mView.getContext().getContentResolver();
        mAdapter = new VideoAdapter(mView.getContext(), mVideoBeanList);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mView.getContext()));
        mRecyclerView.addItemDecoration(new LineItemDecoration(getContext(), 280, 20, R.drawable.line_item_decoration));
        mRecyclerView.setAdapter(mAdapter);
        loadingLayout = (RelativeLayout) mView.findViewById(R.id.loading_layout);
        loadingLayout.setVisibility(View.VISIBLE);
        loadingView = (LVBlock) mView.findViewById(R.id.loading_view);
        loadingView.startAnim();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getVideoData();
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
        if (mVideoBeanList != null) {
            mVideoBeanList.clear();
            mVideoBeanList = null;
        }
    }

    private void getVideoData() {
        mVideoBeanList.clear();
        Cursor c = mResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
                null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        if (c == null) {

        } else {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                size = c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                if (Long.parseLong(size) > 1024) {
                    bean = new VideoBean();
                    bean.setModify(TimeUtil.ms2Modify(c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED))));
                    bean.setName(c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)));
                    bean.setPath(c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                    bean.setSize(Utils.changeSize(Long.parseLong(size)));
                    bean.setDuration(TimeUtil.ms2Time(c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))));
                    mVideoBeanList.add(bean);
                }
            }
        }
        c.close();
    }


    @Override
    public void onItemClick(int position) {
        FileUtil.showOpenTypeWindow(mVideoBeanList.get(position).getPath(), getContext());
    }

    @Override
    public void onItemLongClick(int position) {
        showFileInfo(mVideoBeanList.get(position), mView);
    }

    @Override
    public List getDataList() {
        return mVideoBeanList;
    }

    public int getFabButtonNum() {
        return 3;
    }

    @Override
    public void notifyRecyclerView(List<Bean> list) {
        mVideoBeanList.removeAll(list);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateMediaDataBase(List<Bean> list) {
        List<String> strList = new ArrayList<>();
        for (Bean bean : list) {
            String path = new File(bean.getPath()).getParent();
            if (!strList.contains(path)) {
                strList.add(path);
            }
        }
        Utils.scanFileToUpdate((getActivity()).getApplicationContext(),
                strList.toArray(new String[strList.size()]));
    }

    @Override
    public void setAllUnSelected() {

        int size = mVideoBeanList.size();
        for (int i = 0; i < size; i++) {
            if (mVideoBeanList.get(i).isSelected()) {
                mVideoBeanList.get(i).setSelected(false);
                mAdapter.notifyItemChanged(i);
            }
        }
    }
}
