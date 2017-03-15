package com.example.asus88.finaldesgin.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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

    private static final String TAG = "ScanPhotoActivity";
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //延迟共享元素动画
            postponeEnterTransition();
        }
        ButterKnife.bind(this);
        setStatusBarColor(this, 0xFF797878);
        initData();
        initEvents();
    }

    private void initData() {
        photoList = new ArrayList<>();
        position = getIntent().getIntExtra("position", 0);
        list = getIntent().getParcelableArrayListExtra("photo");
        for (int i = 0; i < list.size(); i++) {
            PhotoView photoView = new PhotoView(this);
            photoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            Glide.with(this).load(list.get(i).getPath()).thumbnail(0.1f).into(photoView);
            photoList.add(photoView);
        }
        mAdapter = new ScanPhotoAdapter(this, photoList);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(position);
        mViewPager.setBackgroundColor(0xFF797878);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final PhotoView photo = photoList.get(position);
            photo.setTransitionName(getString(R.string.transition_scan));
            photo.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            //启动动画
                            photo.getViewTreeObserver().removeOnPreDrawListener(this);
                            startPostponedEnterTransition();
                            return true;
                        }
                    });
        }
    }

    private void initEvents() {
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                } else {
                    finish();
                }
            }
        });
    }

    /**
     * 通知activity 元素对应关系的改变
     */
    public void finishAfterTransition() {
        int pos = mViewPager.getCurrentItem();
        if (pos != position && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent data = new Intent();
            photoList.get(pos).setTransitionName(getString(R.string.transition_scan));
            data.putExtra("position", pos);
            setResult(RESULT_OK, data);
        }
        super.finishAfterTransition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (photoList != null) {
            photoList.clear();
            photoList = null;
        }
        if (list != null) {
            list.clear();
            list = null;
        }
    }
}
