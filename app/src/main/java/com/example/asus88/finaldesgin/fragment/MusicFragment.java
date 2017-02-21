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

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.adapter.MusicAdapter;
import com.example.asus88.finaldesgin.bean.MusicBean;
import com.example.asus88.finaldesgin.itemDecoration.LineItemDecoration;
import com.example.asus88.finaldesgin.util.LogUtil;
import com.example.asus88.finaldesgin.util.TimeUtil;
import com.example.asus88.finaldesgin.util.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by asus88 on 2016/12/21.
 */

public class MusicFragment extends BaseFragment implements MusicAdapter.onItemClickListener {
    private static final String TAG = "MusicFragment";

    private View mView;
    private RecyclerView mRecyclerView;
    private List<MusicBean> mMusicBeanList;
    private MusicAdapter mAdapter;
    private ContentResolver mResolver;
    private String size;
    private MusicBean bean;
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
       LogUtil.logd(TAG,"create");
        mView = inflater.inflate(R.layout.fragment_content, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
        mMusicBeanList = new ArrayList<>();
        mAdapter = new MusicAdapter(mView.getContext(), mMusicBeanList);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mView.getContext()));
        mRecyclerView.addItemDecoration(new LineItemDecoration(getContext(),250,20,R.drawable.line_item_decoration));
        mRecyclerView.setAdapter(mAdapter);
        new Thread(new Runnable() {
            @Override
            public void run() {
                getMusicData();
                Message msg = Message.obtain();
                msg.what = 1;
                mHandler.sendMessage(msg);
            }
        }).start();
        return mView;
    }

    private void getMusicData() {
        mResolver = mView.getContext().getContentResolver();
        Cursor c = mResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (c == null) {

        } else {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                size = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                if (Long.parseLong(size) > 1048576) {
                    bean = new MusicBean();
                    bean.setSize(Utils.changeSize(Long.parseLong(size)));
                    bean.setSelected(false);
                    bean.setName(c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
                    bean.setPath(c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                    bean.setSinger(c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
                    bean.setDuration(TimeUtil.ms2Time(c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))));
                    mMusicBeanList.add(bean);
                }
            }
        }
        c.close();
    }

    @Override
    public void onItemClick(View view, int position) {
        //play the music
    }

    @Override
    public void onItemLongClick(View view, int position) {
        showFileInfo(mMusicBeanList.get(position), mView);
    }

    @Override
    public List getDataList() {
        return mMusicBeanList;
    }
    public int getFabButtonNum(){return 3;}

    @Override
    public void notifyRecyclerView() {
        mAdapter.notifyDataSetChanged();
    }

}
