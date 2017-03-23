package com.example.asus88.finaldesgin.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.activity.PhotoActivity;
import com.example.asus88.finaldesgin.adapter.PhotoGroupAdapter;
import com.example.asus88.finaldesgin.bean.DevBean;
import com.example.asus88.finaldesgin.bean.PhotoBean;
import com.example.asus88.finaldesgin.bean.PhotoGroupBean;
import com.example.asus88.finaldesgin.connection.Transfer;
import com.example.asus88.finaldesgin.myViews.LVBlock;
import com.example.asus88.finaldesgin.util.FileUtil;
import com.example.asus88.finaldesgin.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus88 on 2016/12/21.
 */

public class PhotoFragment extends BaseFragment implements PhotoGroupAdapter.onItemClickListener {
    private static final String TAG = "PhotoFragment";

    private static final int SCAN_PHOTO_REQUEST_CODE = 110;
    private int scanPos;
    private View mView;

    private RecyclerView mRecyclerView;
    private List<PhotoGroupBean> mPhotoBeanList;
    private PhotoGroupAdapter mAdapter;
    private RelativeLayout loadingLayout;
    private LVBlock loadingView;
    private ContentResolver mResolver;
    private String path;
    private String parentName;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    loadingView.stopAnim();
                    loadingLayout.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_content, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
        mPhotoBeanList = new ArrayList<>();
        loadingLayout = (RelativeLayout) mView.findViewById(R.id.loading_layout);
        loadingLayout.setVisibility(View.VISIBLE);
        loadingView = (LVBlock) mView.findViewById(R.id.loading_view);
        loadingView.startAnim();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getPhotoData();
                Message message = Message.obtain();
                message.what = 1;
                mHandler.sendMessage(message);
            }
        }).start();
        mAdapter = new PhotoGroupAdapter(getContext(), mPhotoBeanList);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mRecyclerView.setAdapter(mAdapter);
        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPhotoBeanList != null) {
            mPhotoBeanList.clear();
            mPhotoBeanList = null;
        }
    }

    private void getPhotoData() {
        mResolver = mView.getContext().getContentResolver();
        Cursor cursor = mResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Images.Media.DATE_MODIFIED);
        while (cursor.moveToNext()) {
            path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            File file = new File(path).getParentFile();
            PhotoGroupBean bean = new PhotoGroupBean();
            bean.setName(file.getName());
            bean.setPath(file.getAbsolutePath());
            PhotoBean photoBean = new PhotoBean();
            photoBean.setPath(path);
            photoBean.setSelected(0);
            if (!mPhotoBeanList.contains(bean)) {
                List<PhotoBean> list = new ArrayList<>();
                list.add(photoBean);
                bean.setPhotoPath(list);
                mPhotoBeanList.add(bean);
            } else {
                mPhotoBeanList.get(mPhotoBeanList.indexOf(bean)).getPhotoPath().add(photoBean);
            }
        }
        cursor.close();
    }

    @Override
    public void onItemClick(int position) {
        scanPos = position;
        Intent intent = new Intent(getActivity(), PhotoActivity.class);
        intent.putExtra("path", mPhotoBeanList.get(position).getName());
        intent.putParcelableArrayListExtra("photo", (ArrayList) mPhotoBeanList.get(position).getPhotoPath());
        startActivityForResult(intent, SCAN_PHOTO_REQUEST_CODE);
    }

    private List<PhotoGroupBean> getSelectedList() {
        List<PhotoGroupBean> sList = new ArrayList<>();
        for (PhotoGroupBean bean : mPhotoBeanList) {
            if (bean.isSelected()) {
                sList.add(bean);
            }
        }
        return sList;
    }

    @Override
    public List getDataList() {
        return mPhotoBeanList;
    }

    @Override
    public void deleteFile() {
        List<PhotoGroupBean> sList = getSelectedList();
        if (sList == null || sList.size() <= 0) {
            Toast.makeText(getActivity(), getString(R.string.no_selected), Toast.LENGTH_SHORT).show();
        } else {
            for (PhotoGroupBean bean : sList) {
                List<PhotoBean> photoList = bean.getPhotoPath();
                for (PhotoBean photo : photoList) {
                    FileUtil.deleteFile(new File(photo.getPath()));
                }
            }
            updateDataBase(sList);
            refreshRecyclerView(sList);
        }
    }

    @Override
    public void sendFile(List<DevBean> devList) {
        List<PhotoGroupBean> sList = getSelectedList();
        for (DevBean dev : devList) {
            if (dev.isSelected()) {
                Transfer transfer = dev.getTransfer();
                for (PhotoGroupBean bean : sList) {
                    try {
                        Log.d(TAG, "sendFile: " + bean.getPath());
                        transfer.addPhotoGroupTask(bean.getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        setAllUnSelected();
    }

    @Override
    public int getSelectedNum() {
        int num = 0;
        for (PhotoGroupBean bean : mPhotoBeanList) {
            if (bean.isSelected()) {
                num++;
            }
        }
        return num;
    }

    public int getFabButtonNum() {
        return 3;
    }


    private void refreshRecyclerView(List<PhotoGroupBean> list) {
        mPhotoBeanList.removeAll(list);
        mAdapter.notifyDataSetChanged();
    }


    private void updateDataBase(List<PhotoGroupBean> list) {
        List<String> strList = new ArrayList<>();
        for (PhotoGroupBean bean : list) {
            List<PhotoBean> photoList = bean.getPhotoPath();
            for (PhotoBean photo : photoList) {
                String path = new File(photo.getPath()).getParent();
                if (!strList.contains(path)) {
                    strList.add(path);
                }
            }
        }
        Utils.scanFileToUpdate((getActivity()).getApplicationContext(),
                strList.toArray(new String[strList.size()]));
    }

    @Override
    public void setAllUnSelected() {
        int size = mPhotoBeanList.size();
        for (int i = 0; i < size; i++) {
            if (mPhotoBeanList.get(i).isSelected()) {
                mPhotoBeanList.get(i).setSelected(false);
                mAdapter.notifyItemChanged(i);
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_PHOTO_REQUEST_CODE && resultCode == 1) {
            List<PhotoBean> list = data.getParcelableArrayListExtra("photo");
            if (list.size() > 0) {
                mPhotoBeanList.get(scanPos).setPhotoPath(list);
                mAdapter.notifyItemChanged(scanPos);
            } else {
                mAdapter.notifyItemRemoved(scanPos);
            }
        }
    }
}
