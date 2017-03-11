package com.example.asus88.finaldesgin.activity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.SharedElementCallback;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.TextViewFactory;
import com.example.asus88.finaldesgin.adapter.PhotoAdapter;
import com.example.asus88.finaldesgin.bean.DevBean;
import com.example.asus88.finaldesgin.bean.FabMenuButtonBean;
import com.example.asus88.finaldesgin.bean.PhotoBean;
import com.example.asus88.finaldesgin.connection.Manager;
import com.example.asus88.finaldesgin.connection.Transfer;
import com.example.asus88.finaldesgin.util.DimenUtil;
import com.example.asus88.finaldesgin.util.FileUtil;
import com.example.asus88.finaldesgin.util.Utils;
import com.example.asus88.finaldesgin.util.WifiUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLING;

public class PhotoActivity extends BaseActivity implements PhotoAdapter.onItemClickListener, View.OnClickListener {

    private static final String TAG = "PhotoActivity";
    @BindView(R.id.photo_back)
    ImageView mPhotoBack;
    @BindView(R.id.photo_path)
    TextView mPhotoPath;
    @BindView(R.id.photo_recycler)
    RecyclerView mPhotoRecycler;
    @BindView(R.id.photo_fab)
    FloatingActionButton mFab;

    private List<PhotoBean> mPhotoBeanList;
    private List<PhotoBean> selectedList;
    private PhotoAdapter mAdapter;

    private String path;

    private FrameLayout background;
    private TextView[] button;
    private List<FabMenuButtonBean> fabBtnList;
    private boolean isShowBtn;
    private Manager conManager;
    private WifiManager mWifiManager;
    private List<DevBean> devList;
    private popOnDismissListener mOnDismissListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ButterKnife.bind(this);
        initData();
        initEvents();
    }

    private void initData() {
        selectedList = new ArrayList<>();
        mPhotoBeanList = getIntent().getParcelableArrayListExtra("photo");
        path = getIntent().getStringExtra("path");

        button = new TextView[3];
        fabBtnList = new ArrayList<>();
        conManager = Manager.getManager();
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        devList = new ArrayList<>();
        mOnDismissListener = new popOnDismissListener();

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
        mFab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.photo_back:
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra("photo", (ArrayList) mPhotoBeanList);
                setResult(1, intent);
                finish();
                break;
            case R.id.photo_fab:
                showBackground();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (!isShowBtn) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra("photo", (ArrayList) mPhotoBeanList);
            setResult(1, intent);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPhotoBeanList != null) {
            mPhotoBeanList.clear();
            mPhotoBeanList = null;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(this, ScanPhotoActivity.class);
        intent.putParcelableArrayListExtra("photo", (ArrayList) mPhotoBeanList);
        intent.putExtra("position", position);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions option = ActivityOptions.makeSceneTransitionAnimation(PhotoActivity.this,
                    view, view.getTransitionName());
            startActivity(intent, option.toBundle());
        } else {
            startActivity(intent);
        }
    }

    private void showBackground() {
        if (background == null) {
            ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();
            background = new FrameLayout(this);
            background.setBackgroundColor(getResources().getColor(R.color.fab_menu_color));
            background.setLayoutParams(new FrameLayout.LayoutParams(rootView.getWidth(), rootView.getHeight()));
            background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeBtnFromBg();
                    hideBackground();
                }
            });
            initFabButtonData();
            rootView.addView(background);
        }
        addBtnToBg();
        background.setVisibility(View.VISIBLE);
    }

    private void hideBackground() {
        background.setVisibility(View.GONE);
    }

    /**
     * 初始化fab button
     */
    private void initFabButtonData() {
        FabMenuButtonBean delete = new FabMenuButtonBean("delete", R.drawable.bg_fab_delete_btn);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSelectList();
                if (selectedList.size() > 0) {
                    for (PhotoBean bean : selectedList) {
                        FileUtil.deleteFile(new File(bean.getPath()));
                    }
                    updateDataBase(selectedList);
                    mPhotoBeanList.removeAll(selectedList);
                    mAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(PhotoActivity.this, getString(R.string.no_selected), Toast.LENGTH_SHORT).show();
                }
                hideBackground();
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
                    showIsOpenWifiWindow(PhotoActivity.this, getWindow().getDecorView().getRootView(),
                            mWifiManager, mOnDismissListener);
                } else {
                    Intent intent = new Intent(PhotoActivity.this, LinkActivity.class);
                    startActivity(intent);
                    hideBackground();
                }
            }
        });
        FabMenuButtonBean send = new FabMenuButtonBean("send", R.drawable.bg_fab_send_btn);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBackground();
                getSelectList();
                if (selectedList.size() > 0) {
                    devList.clear();
                    devList.addAll(conManager.getLinkingDev());
                    if (devList.size() > 0) {
                        showSelectDevWindow(PhotoActivity.this, getWindow().getDecorView().getRootView(),
                                devList, mOnDismissListener);
                    } else {
                        Toast.makeText(PhotoActivity.this, getString(R.string.no_link), Toast.LENGTH_SHORT).show();
                        hideBackground();
                    }
                } else {
                    Toast.makeText(PhotoActivity.this, getString(R.string.no_selected), Toast.LENGTH_SHORT).show();
                    hideBackground();
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

    private void addBtnToBg() {
        for (int i = 0; i < 3; i++) {
            background.addView(button[i]);
        }
        isShowBtn = true;
    }

    private void removeBtnFromBg() {
        for (int i = 0; i < 3; i++) {
            background.removeView(button[i]);
        }
        isShowBtn = false;
    }

    private void updateDataBase(List<PhotoBean> list) {
        List<String> strList = new ArrayList<>();
        for (PhotoBean bean : list) {
            String path = new File(bean.getPath()).getParent();
            if (!strList.contains(path)) {
                strList.add(path);
            }
        }
        Utils.scanFileToUpdate(getApplicationContext(),
                strList.toArray(new String[strList.size()]));
    }

    private void getSelectList() {
        selectedList.clear();
        for (PhotoBean bean : mPhotoBeanList) {
            if (bean.getSelected() == 1) {
                selectedList.add(bean);
            }
        }
    }

    @Override

    public void sendFile(List<DevBean> list) {
        super.sendFile(list);
        for (DevBean bean : list) {
            if (bean.isSelected()) {
                Transfer transfer = bean.getTransfer();
                for (PhotoBean photoBean : selectedList) {
                    try {
                        transfer.addTask(photoBean.getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        setAllUnselected();
    }

    private void setAllUnselected() {
        for (PhotoBean bean : mPhotoBeanList) {
            if (bean.getSelected() == 1) {
                bean.setSelected(0);
            }
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        if (resultCode == RESULT_OK) {
            final int position = data.getIntExtra("position", 0);
            LinearLayoutManager layoutManager = (LinearLayoutManager) mPhotoRecycler.getLayoutManager();
            if (position > layoutManager.findLastCompletelyVisibleItemPosition()) {
                mPhotoRecycler.scrollToPosition(position);
            }
            setExitSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    PhotoAdapter.MyViewHolder holder = (PhotoAdapter.MyViewHolder) mPhotoRecycler.getChildViewHolder(mPhotoRecycler.getChildAt(position));
                    sharedElements.put(getString(R.string.transition_scan), holder.itemView.findViewById(R.id.photo_adapter_image));
                    super.onMapSharedElements(names, sharedElements);
                }
            });
        }
    }

    /**
     * 监听popWindow dismiss
     */
    private class popOnDismissListener implements PopupWindow.OnDismissListener {

        @Override
        public void onDismiss() {
            hideBackground();
        }
    }
}
