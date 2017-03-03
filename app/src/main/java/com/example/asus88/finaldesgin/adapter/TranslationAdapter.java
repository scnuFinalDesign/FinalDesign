package com.example.asus88.finaldesgin.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.asus88.finaldesgin.bean.FragnemtBean;
import com.example.asus88.finaldesgin.util.ListUtil;

import java.util.List;

/**
 * Created by asus88 on 2017/2/28.
 */

public class TranslationAdapter extends FragmentPagerAdapter {

    private List<FragnemtBean> fragmentList;
    private Context mContext;

    public TranslationAdapter(FragmentManager fm, Context context, List<FragnemtBean> fragmentList) {
        super(fm);
        mContext = context;
        this.fragmentList = fragmentList;
    }

    @Override
    public int getCount() {
        return ListUtil.getSize(fragmentList);
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position).getFragment();
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentList.get(position).getName();
    }

}
