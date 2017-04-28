package com.example.asus88.finaldesgin.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.SharedElementCallback;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.adapter.PhotoAdapter;
import com.example.asus88.finaldesgin.bean.DevBean;
import com.example.asus88.finaldesgin.bean.PhotoBean;
import com.example.asus88.finaldesgin.connection.Transfer;
import com.example.asus88.finaldesgin.util.FileUtil;
import com.example.asus88.finaldesgin.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoActivity extends EBaseActivity implements PhotoAdapter.onItemClickListener, View.OnClickListener {

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

        mPhotoPath.setText(path);

        mAdapter = new PhotoAdapter(this, mPhotoBeanList);
        mPhotoRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        ((SimpleItemAnimator) mPhotoRecycler.getItemAnimator()).setSupportsChangeAnimations(false);
        mPhotoRecycler.setAdapter(mAdapter);

        setFabButtonSize(3);
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
        if (!isBackgroundShow()) {
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
            ActivityOptions option = ActivityOptions.makeSceneTransitionAnimation(
                    PhotoActivity.this, view, view.getTransitionName());
            startActivity(intent, option.toBundle());
        } else {
            startActivity(intent);
        }
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
        for (int i = 0; i < mPhotoBeanList.size(); i++) {
            PhotoBean bean = mPhotoBeanList.get(i);
            if (bean.getSelected() == 1) {
                bean.setSelected(0);
                mAdapter.notifyItemChanged(i);
            }
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            final int position = data.getIntExtra("position", 0);
            LinearLayoutManager layoutManager = (LinearLayoutManager) mPhotoRecycler.getLayoutManager();
            if (position > layoutManager.findLastCompletelyVisibleItemPosition()) {
                mPhotoRecycler.scrollToPosition(position);
                //// TODO: 2017/4/18 mPhotoRecycler.getChildAt(position) null
            }
            setExitSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    PhotoAdapter.MyViewHolder holder = (PhotoAdapter.MyViewHolder) mPhotoRecycler.
                            getChildViewHolder(mPhotoRecycler.getChildAt(position));
                    sharedElements.put(getString(R.string.transition_scan), holder.itemView.findViewById(R.id.photo_adapter_image));
                    Log.d(TAG, "onMapSharedElements: change");
                    super.onMapSharedElements(names, sharedElements);
                }
            });
        }
        super.onActivityReenter(resultCode, data);
    }

    @Override
    public void deleteFile() {
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
    }

    @Override
    public int getSelectedSize() {
        getSelectList();
        return selectedList.size();
    }

}
