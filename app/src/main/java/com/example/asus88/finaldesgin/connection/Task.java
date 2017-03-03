package com.example.asus88.finaldesgin.connection;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.bean.SendTakBean;

public class Task extends SendTakBean {

    public static int ID = 0;
    public int mID;
    public int remoteID;

    public static final int SEND_TASK_TYPE = 1;
    public static final int RECEIVE_TASK_TYPE = 2;
    public int type;

    public static final int WAIT = 3;
    public static final int RUN = 4;
    public static final int PAUSE = 5;
    public static final int OVER = 6;
    public static final int FAILED = 7;
    public int state;

    public String name;//用于显示的名称
    public String path;//根目录路径

    public long totalCount;
    public long transferedCount;
    public long offset;

    public boolean isBackup;//是否需要获取备份，用于网络中断的情况

    protected Task(int type, String name, String path, long totalCount, Dev dev) {
        this.mID = ID++;
        this.type = type;
        this.name = name;
        this.path = path;
        this.totalCount = totalCount;
        this.state = WAIT;
        this.setDev(dev);
    }

    public static Task createReceiveTask(String name, String path, long totalCount, Dev dev) {
        return new Task(RECEIVE_TASK_TYPE, name, path, totalCount, dev);
    }

    public double getRate() {
        if (totalCount == 0) return 100d;
        else return ((double) (transferedCount + offset) * 100d / (double) totalCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o) {
            return false;
        }
        if (!(o instanceof Task)) {
            return false;
        }

        if (path.equals(((Task) o).path) && type == ((Task) o).type) return true;
        else return false;
    }

    public int getStateIconId() {
        switch (state) {
            case WAIT:
                return R.mipmap.state_icon_wait;
            case RUN:
                return R.mipmap.state_icon_stop;
            case PAUSE:
                return R.mipmap.state_icon_start;
            case FAILED:
                return R.mipmap.state_icon_restart;
            case OVER:
                return R.mipmap.state_icon_finish;
            default:
                return R.mipmap.state_icon_restart;
        }
    }

    public String getStateString() {
        switch (state) {
            case WAIT: {
                if (type == SEND_TASK_TYPE)
                    return "等待发送";
                else
                    return "等待中";
            }
            case RUN: {
                if (type == SEND_TASK_TYPE)
                    return "正在发送";
                else
                    return "接收中";
            }
            case PAUSE: {
                return "暂停";
            }
            case OVER: {
                if (type == SEND_TASK_TYPE)
                    return "发送完成";
                else
                    return "接收完毕";
            }
            case FAILED: {
                if (type == SEND_TASK_TYPE)
                    return "发送失败";
                else
                    return "接收失败";
            }
        }
        return null;
    }


    @Override
    protected int initLayoutId() {
        return R.layout.adapter_send_receive_task;
    }
}
