package com.example.asus88.finaldesgin.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.adapter.FileAdapter;
import com.example.asus88.finaldesgin.adapter.LocationAdapter;
import com.example.asus88.finaldesgin.bean.Bean;
import com.example.asus88.finaldesgin.bean.FileBean;
import com.example.asus88.finaldesgin.itemDecoration.FileItemDirection;
import com.example.asus88.finaldesgin.itemDecoration.LineItemDecoration;
import com.example.asus88.finaldesgin.util.FileUtil;
import com.example.asus88.finaldesgin.util.TimeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by asus88 on 2017/1/14.
 */

public class FileFragment extends BaseFragment implements FileAdapter.onItemClickListener, LocationAdapter.onItemClickListener {
    private static final String TAG = "FileFragment";
    private List<FileBean> mFileList;
    private RecyclerView mRecyclerView;
    private FileAdapter mAdapter;
    private View mView;
    private String fileName;
    private String filePath;
    private String fileType;

    //location recycler
    private List<String> locationList;
    private LocationAdapter mLocationAdapter;
    private String rootPath;
    private RecyclerView locationRecycler;

    private Comparator mComparator = new Comparator() {
        @Override
        public int compare(Object lhs, Object rhs) {
            String f1 = ((FileBean) lhs).getType();
            String f2 = ((FileBean) rhs).getType();
            if (TextUtils.isEmpty(f1))
                return -1;
            else if (TextUtils.isEmpty(f2)) return 1;
            else return f1.compareToIgnoreCase(f2);
        }
    };
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_file, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
        mFileList = new ArrayList<>();
        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mAdapter = new FileAdapter(mView.getContext(), mFileList);
        new Thread(new Runnable() {
            @Override
            public void run() {
                getFileData(rootPath);
                Message msg = Message.obtain();
                msg.what = 1;
                mHandler.sendMessage(msg);
            }
        }).start();
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mView.getContext()));
        mRecyclerView.addItemDecoration(new LineItemDecoration(getContext(), 250, 20, R.drawable.line_item_decoration));
        mRecyclerView.setAdapter(mAdapter);


        locationRecycler = (RecyclerView) mView.findViewById(R.id.location_recycler);
        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        locationList = new ArrayList<>();
        locationList.add(rootPath);
        mLocationAdapter = new LocationAdapter(getContext(), locationList);
        mLocationAdapter.setOnItemClickListener(this);
        locationRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        locationRecycler.setAdapter(mLocationAdapter);
        locationRecycler.addItemDecoration(new FileItemDirection(getContext(), 10));

        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mFileList!=null){
            mFileList.clear();
            mFileList=null;
        }
        if(locationList!=null){
            locationList.clear();
            locationList=null;
        }
    }

    private void getFileData(String path) {
        mFileList.clear();
        File f = new File(path);
        File[] files = f.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                fileName = file.getName();
                if (!fileName.startsWith(".")) {
                    FileBean bean = newFileBean(file, 0);
                    mFileList.add(bean);
                }
            }
        }
        Collections.sort(mFileList);
        Collections.sort(mFileList, mComparator);
    }

    public FileBean newFileBean(File file, long modify) {
        FileBean bean = new FileBean();
        bean.setName(file.getName());
        filePath = file.getPath();
        bean.setPath(filePath);
        if (modify == 0) {
            bean.setModify(TimeUtil.ms2Modify(String.valueOf(file.lastModified())));
        } else {
            bean.setModify(TimeUtil.ms2Modify(String.valueOf(modify)));
        }
        if (file.isDirectory()) {
            bean.setType("directory");
        } else {
            bean.setSize(FileUtil.getFileSize(filePath));
            bean.setType(FileUtil.getFileSuffix(filePath));
        }
        bean.setSelected(false);
        return bean;
    }

    /**
     * 新建后刷新页面
     *
     * @param bean
     */
    public void addFileBean(FileBean bean) {
        mFileList.add(bean);
        Collections.sort(mFileList);
        Collections.sort(mFileList, mComparator);
        mAdapter.notifyItemInserted(mFileList.indexOf(bean));
    }

    /**
     * 返回当前页面的路径
     *
     * @return
     */
    public String getCurrentPath() {
        return locationList.get(locationList.size() - 1);
    }

    /**
     * file recyclerView item click
     *
     * @param view
     * @param position
     */
    @Override
    public void onItemClick(View view, int position) {
        FileBean bean = mFileList.get(position);
        fileType = bean.getType();
        if (fileType.equals("directory")) {
            getFileData(bean.getPath());
            mAdapter.notifyDataSetChanged();
            locationList.add(bean.getPath());
            mLocationAdapter.notifyItemInserted(locationList.size() - 1);
        } else {
            FileUtil.showOpenTypeWindow(bean.getPath(),getContext());
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        FileBean bean = mFileList.get(position);
        showFileInfo(bean, mView);
    }


    /**
     * location recyclerView item click
     *
     * @param position
     */
    @Override
    public void onLocationItemClick(int position) {
        for (int i = locationList.size() - 1; i > position; i--) {
            locationList.remove(i);
        }
        mLocationAdapter.notifyDataSetChanged();
        getFileData(locationList.get(position));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public List getDataList() {
        return mFileList;
    }

    public int getFabButtonNum() {
        return 5;
    }

    @Override
    public void notifyRecyclerView(List<Bean> list) {
        mFileList.removeAll(list);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateMediaDataBase(List<Bean> list) {

    }

    @Override
    public void setAllUnSelected() {
        for(int i=0;i<mFileList.size();i++){
            FileBean bean=mFileList.get(i);
            if(bean.isSelected()) {
                bean.setSelected(false);
                mAdapter.notifyItemChanged(i);
            }
        }
    }

}
