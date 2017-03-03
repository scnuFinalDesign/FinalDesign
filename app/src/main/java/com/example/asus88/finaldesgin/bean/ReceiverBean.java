package com.example.asus88.finaldesgin.bean;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.connection.Task;

import java.util.List;

/**
 * Created by asus88 on 2017/2/27.
 */

public class ReceiverBean extends SendTakBean {
    private List<Task> sendList;
    private boolean isExpand;  //是否展开

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }

    public List<Task> getSendList() {
        return sendList;
    }

    public void setSendList(List<Task> sendList) {
        this.sendList = sendList;
    }

    public String getMac() {
        return getDev().mac;
    }


    public String getName() {
        return getDev().getName();
    }


    @Override
    protected int initLayoutId() {
        return R.layout.adapter_receiver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o) {
            return false;
        }
        if (!(o instanceof ReceiverBean)) {
            return false;
        }
        return this.getMac() == ((ReceiverBean) o).getMac();
    }
}
