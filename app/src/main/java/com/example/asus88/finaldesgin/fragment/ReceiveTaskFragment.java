package com.example.asus88.finaldesgin.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.adapter.ReceiveTaskAdapter;
import com.example.asus88.finaldesgin.connection.Manager;
import com.example.asus88.finaldesgin.connection.Task;
import com.example.asus88.finaldesgin.connection.Transfer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus88 on 2017/2/27.
 */

public class ReceiveTaskFragment extends Fragment implements Manager.onReceiveTaskListChangeListener, ReceiveTaskAdapter.onItemStateClickListener {
    private static final String TAG = "ReceiveTaskFragment";

    private List<Task> taskList;
    private RecyclerView mRecyclerView;
    private View mView;
    private ReceiveTaskAdapter mAdapter;

    private Manager conManager;

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
        mView = inflater.inflate(R.layout.fragment_content, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
        taskList = new ArrayList<>();
        conManager = Manager.getManager();
        conManager.setOnReceiveTaskListChangeListener(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                taskList = conManager.getReceiveTaskList();
                Message message = Message.obtain();
                message.what = 1;
                mHandler.sendMessage(message);
            }
        }).start();
        taskList = conManager.getReceiveTaskList();
        mAdapter = new ReceiveTaskAdapter(mView.getContext(), taskList);
        mAdapter.setOnItemStateClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mView.getContext()));
        mRecyclerView.setAdapter(mAdapter);
        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (taskList != null) {
            taskList.clear();
            taskList = null;
        }
    }

    @Override
    public void onReceiveTaskChange(Transfer transfer, Task task, int action) {
        switch (action) {
            case 0:
                taskList.clear();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
                break;
            case 1:
                taskList.add(task);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyItemInserted(taskList.size() - 1);
                    }
                });
                break;
            case 2:
                final int pos = taskList.indexOf(task);
                taskList.set(pos, task);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyItemChanged(pos);
                    }
                });
                break;
        }
    }

    @Override
    public void onItemStateChangeListener(int position) {
        Task task = taskList.get(position);
        if (conManager == null) {
            conManager = Manager.getManager();
        }
        Log.d(TAG, "onItemStateChangeListener: "+task.getDev());
        Transfer t = conManager.getTransferFromMap(task.getDev());
        if (t != null) {
            t.clickReceiveTaskListItem(task);
        }
    }
}
