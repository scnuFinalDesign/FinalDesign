package com.example.asus88.finaldesgin.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.bean.DevBean;
import com.example.asus88.finaldesgin.bean.FileBean;
import com.example.asus88.finaldesgin.fragment.FileFragment;
import com.example.asus88.finaldesgin.myViews.LVBlock;
import com.example.asus88.finaldesgin.util.FileUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.asus88.finaldesgin.R.string.loading;

public class SearchActivity extends EBaseActivity implements View.OnClickListener, FileFragment.onSearchingListener {
    private static final String TAG = "SearchActivity";

    @BindView(R.id.search_act_back)
    ImageView mBack;
    @BindView(R.id.search_act_search_content)
    EditText mSearchContent;
    @BindView(R.id.search_act_fab)
    FloatingActionButton mFab;
    @BindView(R.id.search_act_file_content)
    FrameLayout mFileContent;
    @BindView(R.id.search_act_icon)
    ImageView mIcon;


    private List<FileBean> fileList;
    private String content;
    private FileFragment mFileFragment;

    private RelativeLayout loadingLayout;
    private LVBlock loadingView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //延迟共享元素动画
            postponeEnterTransition();
        }
        ButterKnife.bind(this);
        initData();
        initEvents();
    }


    private void initData() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition transition = getWindow().getSharedElementEnterTransition();
            transition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {

                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    mBack.setVisibility(View.VISIBLE);
                    mSearchContent.setVisibility(View.VISIBLE);
                    mFab.setVisibility(View.VISIBLE);
                    mFileContent.setVisibility(View.VISIBLE);
                }

                @Override
                public void onTransitionCancel(Transition transition) {

                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            });
        }

        if (mFileFragment == null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isSearch", true);
            mFileFragment = new FileFragment();
            mFileFragment.setArguments(bundle);
            mFileFragment.setOnSearchingListener(this);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.search_act_file_content, mFileFragment);
        transaction.commit();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mIcon.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            //启动动画
                            mIcon.getViewTreeObserver().removeOnPreDrawListener(this);
                            startPostponedEnterTransition();
                            return true;
                        }
                    });
        }
        setFabButtonSize(3);
    }

    private void initEvents() {
        mBack.setOnClickListener(this);
        mFab.setOnClickListener(this);
        mSearchContent.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    content = mSearchContent.getText().toString();
                    if (!TextUtils.isEmpty(content)) {
                        showLoadingLayout();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mFileFragment.setFileList(FileUtil.searchFile(SearchActivity.this, content));
                            }
                        }).start();
                    } else {
                        Toast.makeText(SearchActivity.this, getString(R.string.search_content_not_null),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fileList != null) {
            fileList.clear();
            fileList = null;
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.search_act_back:
                finish();
                break;
            case R.id.search_act_fab:
                showBackground();
                break;
        }
    }

    @Override
    public void sendFile(List<DevBean> list) {
        super.sendFile(list);
        mFileFragment.sendFile(list);
    }

    @Override
    public void deleteFile() {
        mFileFragment.deleteFile();
    }

    @Override
    public int getSelectedSize() {
        return mFileFragment.getSelectedNum();
    }

    private void showLoadingLayout() {
        if (loadingLayout == null) {
            loadingLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.view_loading, null);
            loadingView = (LVBlock) loadingLayout.findViewById(R.id.loading_view);
            TextView textView = (TextView) loadingLayout.findViewById(R.id.loading_text);
            textView.setText(getString(R.string.searching));
        }
        mFileContent.addView(loadingLayout);
        loadingView.startAnim();
        mFab.setVisibility(View.GONE);
    }

    private void hideLoadingLayout() {
        loadingView.stopAnim();
        mFileContent.removeView(loadingLayout);
        mFab.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSearchedFinish() {
        hideLoadingLayout();
    }
}
