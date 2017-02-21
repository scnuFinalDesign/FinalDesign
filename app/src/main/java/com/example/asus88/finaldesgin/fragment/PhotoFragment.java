package com.example.asus88.finaldesgin.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.activity.PhotoActivity;
import com.example.asus88.finaldesgin.adapter.PhotoGroupAdapter;
import com.example.asus88.finaldesgin.bean.PhotoBean;
import com.example.asus88.finaldesgin.bean.PhotoGroupBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus88 on 2016/12/21.
 */

public class PhotoFragment extends BaseFragment implements PhotoGroupAdapter.onItemClickListener {
    private static final String TAG = "PhotoFragment";

    private View mView;

    private RecyclerView mRecyclerView;
    private List<PhotoGroupBean> mPhotoBeanList;
    private PhotoGroupAdapter mAdapter;

    private ContentResolver mResolver;
    private String path;
    private String parentName;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_content, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
        mPhotoBeanList = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getPhotoData();
                Message message = Message.obtain();
                message.what = 1;
                mHandler.sendMessage(message);
            }
        }).start();
        mAdapter = new PhotoGroupAdapter(getContext(), mPhotoBeanList);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mRecyclerView.setAdapter(mAdapter);
        return mView;
    }

    private void getPhotoData() {
        mResolver = mView.getContext().getContentResolver();
        Cursor cursor = mResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Images.Media.DATE_MODIFIED);
        while (cursor.moveToNext()) {
            path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            parentName = new File(path).getParentFile().getName();
            PhotoGroupBean bean = new PhotoGroupBean();
            bean.setPath(parentName);

            PhotoBean photoBean = new PhotoBean();
            photoBean.setPath(path);

            if (!mPhotoBeanList.contains(bean)) {
                List<PhotoBean> list = new ArrayList<>();
                list.add(photoBean);

                bean.setPhotoPath(list);
                mPhotoBeanList.add(bean);
            } else {
                mPhotoBeanList.get(mPhotoBeanList.indexOf(bean)).getPhotoPath().add(photoBean);
            }
        }
        cursor.close();
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(getActivity(), PhotoActivity.class);
        intent.putExtra("path",mPhotoBeanList.get(position).getPath());
        intent.putParcelableArrayListExtra("photo",(ArrayList) mPhotoBeanList.get(position).getPhotoPath());
        startActivity(intent);
    }

    @Override
    public List getDataList() {
        return mPhotoBeanList;
    }
    public int getFabButtonNum(){return 3;}
    @Override
    public void notifyRecyclerView() {
        mAdapter.notifyDataSetChanged();
    }

}
