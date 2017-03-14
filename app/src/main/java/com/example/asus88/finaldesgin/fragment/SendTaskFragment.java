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
import com.example.asus88.finaldesgin.adapter.SendTaskAdapter;
import com.example.asus88.finaldesgin.bean.ReceiverBean;
import com.example.asus88.finaldesgin.bean.SendTakBean;
import com.example.asus88.finaldesgin.connection.Manager;
import com.example.asus88.finaldesgin.connection.Task;
import com.example.asus88.finaldesgin.connection.Transfer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus88 on 2017/2/27.
 */

public class SendTaskFragment extends Fragment implements Manager.onSendTaskListChangeListener, SendTaskAdapter.onReceiverItemClickListener {
    private static final String TAG = "SendTaskFragment";

    private RecyclerView mRecyclerView;
    private List<SendTakBean> taskList;
    private SendTaskAdapter mAdapter;
    private View mView;

    private Manager conManager;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mAdapter.notifyDataSetChanged();
                    Log.d(TAG, "handleMessage: " + taskList.size());
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
        conManager.setOnSendTaskListChangeListener(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                taskList = conManager.getSendTaskList();
                Message message = Message.obtain();
                message.what = 1;
                mHandler.sendMessage(message);
            }
        }).start();
        mAdapter = new SendTaskAdapter(mView.getContext(), taskList);
        mAdapter.setOnReceiverItemClickListener(this);
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
    public void onSendTaskChane(Transfer transfer, Task task, int action) {
        Log.d(TAG, "onSendTaskChane: " + action);
        String mac = transfer.getRemoteDev().mac;
        int pos = 0;
        ReceiverBean rBean = null;
        boolean flag = true;
        if (task != null) {
            for (int i = 0; i < taskList.size(); i++) {
                SendTakBean bean = taskList.get(i);
                if (bean instanceof ReceiverBean) {
                    if (((ReceiverBean) bean).getMac().equals(mac)) {
                        rBean = (ReceiverBean) taskList.get(i);
                        pos = i;
                        flag = false;
                        break;
                    }
                }
            }
            if (flag) {
                rBean = new ReceiverBean();
                rBean.setDev(transfer.getRemoteDev());
                rBean.setSendList(new ArrayList<Task>());
                taskList.add(rBean);
                pos = taskList.size();
            }
            Log.d(TAG, "onSendTaskChane: " + rBean.getName());
            switch (action) {
                case 0:
                    rBean.setSendList(null);
                    break;
                case 1:
                    rBean.getSendList().add(task);
                    break;
                default:
                    break;
            }
            if (flag) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            } else if (rBean.isExpand()) {
                if (action == 1) {
                    Log.d(TAG, "onSendTaskChane: pos:" + pos);
                    pos = +rBean.getSendList().size();
                    Log.d(TAG, "onSendTaskChane: calsize" + pos);
                    taskList.add(pos, task);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }


        }
    }


    @Override
    public void onReceiverItemClick(int position) {
        if (taskList.get(position) instanceof ReceiverBean) {
            ReceiverBean rBean = ((ReceiverBean) (taskList.get(position)));
            Log.d(TAG, "onReceiverItemClick: " + rBean.getName());
            List<Task> tList = rBean.getSendList();
            if (!rBean.isExpand()) {
                rBean.setExpand(true);
                taskList.addAll(position + 1, tList);
            } else {
                rBean.setExpand(false);
                taskList.removeAll(tList);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 改变任务状态
     *
     * @param position
     */
    @Override
    public void onTaskStateChange(int position) {
        if (taskList.get(position) instanceof Task) {
            if (conManager == null) {
                conManager = Manager.getManager();
            }
            Task task = (Task) taskList.get(position);
            Transfer t = conManager.getTransferFromMap(task.getDev());
            if (t != null) {
                t.clickSendTaskListItem(task);
            }
        }
    }
}
