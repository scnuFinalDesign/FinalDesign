package com.example.asus88.finaldesgin.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.example.asus88.finaldesgin.util.ListUtil;

import java.util.List;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by asus88 on 2017/2/7.
 */

public class ScanPhotoAdapter extends PagerAdapter {

    private static final String TAG = "ScanPhotoAdapter";
    private Context mContext;
    private List<PhotoView> list;

    public ScanPhotoAdapter(Context context, List<PhotoView> list) {
        mContext = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return ListUtil.getSize(list);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(list.get(position));
        return list.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(list.get(position));
    }
}
