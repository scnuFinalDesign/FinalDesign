package com.example.asus88.finaldesgin.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.adapter.ScanPhotoAdapter;
import com.example.asus88.finaldesgin.bean.PhotoBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoView;


public class ScanPhotoActivity extends BaseActivity {


    @BindView(R.id.scan_photo_viewPager)
    ViewPager mViewPager;
    @BindView(R.id.scan_photo_back)
    ImageView mBack;

    private List<PhotoView> photoList;
    private List<PhotoBean> list;
    private ScanPhotoAdapter mAdapter;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_photo);
        ButterKnife.bind(this);
        setStatusBarColor(this,0xFF797878);
        initData();
        initEvents();
    }

    private void initData() {
        photoList = new ArrayList<>();
        position = getIntent().getIntExtra("position", 0);
        list = getIntent().getParcelableArrayListExtra("photo");
        for (int i = 0; i < list.size(); i++) {
            PhotoView photoView = new PhotoView(this);
            Glide.with(this).load(list.get(i).getPath()).into(photoView);
            photoList.add(photoView);
        }
        mAdapter = new ScanPhotoAdapter(this, photoList);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(position);
        mViewPager.setBackgroundColor(0xFF797878);
    }

    private void initEvents() {
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(photoList!=null){
            photoList.clear();
            photoList=null;
        }
        if(list!=null){
            list.clear();
            list=null;
        }
    }
}
