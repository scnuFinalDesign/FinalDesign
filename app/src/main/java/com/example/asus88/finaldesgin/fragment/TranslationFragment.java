package com.example.asus88.finaldesgin.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.adapter.TranslationAdapter;
import com.example.asus88.finaldesgin.bean.FragnemtBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus88 on 2017/2/28.
 */

public class TranslationFragment extends Fragment {
    private static final String TAG = "TranslationFragment";

    private View mView;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private TranslationAdapter mAdapter;
    private List<FragnemtBean> fragmentList;
    private SendTaskFragment sendFragment;
    private ReceiveTaskFragment receiveFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_translation, container, false);
        mTabLayout = (TabLayout) mView.findViewById(R.id.translation_fragment_tabLayout);
        mViewPager = (ViewPager) mView.findViewById(R.id.translation_fragment_viewPager);
        fragmentList = new ArrayList<>();
        sendFragment = new SendTaskFragment();
        receiveFragment = new ReceiveTaskFragment();
        fragmentList.add(new FragnemtBean(sendFragment, "send"));
        fragmentList.add(new FragnemtBean(receiveFragment, "receive"));
        mAdapter = new TranslationAdapter(getFragmentManager(), mView.getContext(), fragmentList);
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        return mView;
    }
}
