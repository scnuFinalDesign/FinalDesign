package com.example.asus88.finaldesgin.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.TextViewFactory;
import com.example.asus88.finaldesgin.bean.DevBean;
import com.example.asus88.finaldesgin.bean.FabMenuButtonBean;
import com.example.asus88.finaldesgin.bean.FileBean;
import com.example.asus88.finaldesgin.connection.Manager;
import com.example.asus88.finaldesgin.fragment.FileFragment;
import com.example.asus88.finaldesgin.util.DimenUtil;
import com.example.asus88.finaldesgin.util.FileUtil;
import com.example.asus88.finaldesgin.util.WifiUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLING;

public class SearchActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "SearchActivity";

    @BindView(R.id.search_act_back)
    ImageView mBack;
    @BindView(R.id.search_act_search_content)
    EditText mSearchContent;
    @BindView(R.id.search_act_fab)
    FloatingActionButton mFab;
    @BindView(R.id.search_act_bar)
    LinearLayout mBar;
    @BindView(R.id.search_act_file_content)
    FrameLayout mFileContent;


    private List<FileBean> fileList;
    private String content;
    private FrameLayout background;
    private TextView[] button;
    private List<FabMenuButtonBean> fabBtnList;
    private Manager conManager;
    private WifiManager mWifiManager;
    private FileFragment mFileFragment;

    private List<DevBean> devList;
    private popOnDismissListener mOnDismissListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initViews();
        initData();
        initEvents();
    }

    private void initViews() {
        ButterKnife.bind(this);
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

        button = new TextView[3];
        fabBtnList = new ArrayList<>();
        conManager = Manager.getManager();
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (mFileFragment == null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isSearch", true);
            mFileFragment = new FileFragment();
            mFileFragment.setArguments(bundle);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.search_act_file_content, mFileFragment);
        transaction.commit();

        devList = new ArrayList<>();
        mOnDismissListener = new popOnDismissListener();
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

    /**
     * 初始化fab button
     */
    private void initFabButtonData() {
        FabMenuButtonBean delete = new FabMenuButtonBean("delete", R.drawable.bg_fab_delete_btn);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFileFragment.deleteFile();
                setBackGroundState(false);
            }
        });
        FabMenuButtonBean link = new FabMenuButtonBean("link", R.drawable.bg_fab_link_btn);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeBtnFromBg();
                int state = WifiUtil.getWifiApState(mWifiManager);
                if ((mWifiManager.getWifiState() == WIFI_STATE_DISABLING ||
                        mWifiManager.getWifiState() == WIFI_STATE_DISABLED) &&
                        (state == 10 || state == 11)) {
                    showIsOpenWifiWindow(SearchActivity.this, getWindow().getDecorView().getRootView(),
                            mWifiManager, mOnDismissListener);
                } else {
                    Intent intent = new Intent(SearchActivity.this, LinkActivity.class);
                    startActivity(intent);
                    setBackGroundState(false);
                }
            }
        });
        FabMenuButtonBean send = new FabMenuButtonBean("send", R.drawable.bg_fab_send_btn);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBackGroundState(false);
                if (mFileFragment.getSelectedNum() > 0) {
                    devList.clear();
                    devList.addAll(conManager.getLinkingDev());
                    if (devList.size() > 0) {
                        showSelectDevWindow(SearchActivity.this, getWindow().getDecorView().getRootView(),
                                devList, mOnDismissListener);
                    } else {
                        Toast.makeText(SearchActivity.this, getString(R.string.no_link), Toast.LENGTH_SHORT).show();
                        setBackGroundState(false);
                    }
                } else {
                    Toast.makeText(SearchActivity.this, getString(R.string.no_selected), Toast.LENGTH_SHORT).show();
                    setBackGroundState(false);
                }
            }
        });

        fabBtnList.add(delete);
        fabBtnList.add(link);
        fabBtnList.add(send);
        int marLeft = DimenUtil.getRealWidth(this, 1280, 140);
        int firMargin = (768 - 70 - (3 - 1) * (70 + 20)) / 2;
        for (int i = 0; i < 3; i++) {
            if (button[i] == null) {
                button[i] = TextViewFactory.createTextView(this, fabBtnList.get(i));
            }
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) button[i].getLayoutParams();
            params.setMargins(marLeft, DimenUtil.getRealHeight(this, 768, (firMargin + i * 90)), 0, 0);
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Rect out = new Rect();
        getWindow().findViewById(Window.ID_ANDROID_CONTENT).getDrawingRect(out);
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        mBar.measure(w, h);
        int height = mBar.getMeasuredHeight();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                out.height() - height - DimenUtil.getRealHeight(this, 1280, 20));
        params.gravity = Gravity.BOTTOM;
        mFileContent.setLayoutParams(params);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fileList != null) {
            fileList.clear();
            fileList = null;
        }
    }


    private void setBackGroundState(boolean isShow) {
        if (isShow) {
            if (background == null) {
                ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();
                background = new FrameLayout(this);
                background.setBackgroundColor(getResources().getColor(R.color.fab_menu_color));
                background.setLayoutParams(new FrameLayout.LayoutParams(rootView.getWidth(), rootView.getHeight()));
                background.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setBackGroundState(false);
                    }
                });
                initFabButtonData();
                rootView.addView(background);
            }
            addBtnToBg();
            background.setVisibility(View.VISIBLE);
        } else {
            background.setVisibility(View.GONE);
        }
    }

    private void removeBtnFromBg() {
        for (int i = 0; i < 3; i++) {
            background.removeView(button[i]);
        }
    }

    private void addBtnToBg() {
        for (int i = 0; i < 3; i++) {
            background.addView(button[i]);
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
                setBackGroundState(true);
                break;
        }
    }

    @Override
    public void sendFile(List<DevBean> list) {
        super.sendFile(list);
        mFileFragment.sendFile(list);
    }

    /**
     * 监听popWindow dismiss
     */
    private class popOnDismissListener implements PopupWindow.OnDismissListener {

        @Override
        public void onDismiss() {
            setBackGroundState(false);
        }
    }
}
