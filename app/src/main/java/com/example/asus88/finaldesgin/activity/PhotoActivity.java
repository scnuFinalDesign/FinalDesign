package com.example.asus88.finaldesgin.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.adapter.PhotoAdapter;
import com.example.asus88.finaldesgin.bean.PhotoBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoActivity extends BaseActivity implements PhotoAdapter.onItemClickListener, View.OnClickListener {

    private static final String TAG = "PhotoActivity";
    @BindView(R.id.photo_back)
    ImageView mPhotoBack;
    @BindView(R.id.photo_path)
    TextView mPhotoPath;
    @BindView(R.id.photo_recycler)
    RecyclerView mPhotoRecycler;

    private List<PhotoBean> mPhotoBeanList;
    private PhotoAdapter mAdapter;

    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ButterKnife.bind(this);
        initData();
        initEvents();
        setStatusBarColor(this, Color.BLACK);
    }

    private void initData() {
        mPhotoBeanList = getIntent().getParcelableArrayListExtra("photo");
        path = getIntent().getStringExtra("path");

        mPhotoPath.setText(path);

        mAdapter = new PhotoAdapter(this, mPhotoBeanList);
        mPhotoRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        mPhotoRecycler.setAdapter(mAdapter);
        mPhotoRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {//停止滑动 开始加载
                    Glide.with(PhotoActivity.this).resumeRequests();
                } else {
                    Glide.with(PhotoActivity.this).pauseRequests();
                }
            }
        });
    }

    private void initEvents() {
        mAdapter.setOnItemClickListener(this);
        mPhotoBack.setOnClickListener(this);
    }

    @Override
    public void onItemClick(int position) {
        //todo to scan the big photo
        Intent intent = new Intent(this, ScanPhotoActivity.class);
        intent.putParcelableArrayListExtra("photo", (ArrayList) mPhotoBeanList);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.photo_back:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mPhotoBeanList!=null){
            mPhotoBeanList.clear();
            mPhotoBeanList=null;
        }
    }
}
