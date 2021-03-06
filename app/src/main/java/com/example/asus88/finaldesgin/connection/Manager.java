package com.example.asus88.finaldesgin.connection;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.example.asus88.finaldesgin.bean.DevBean;
import com.example.asus88.finaldesgin.bean.ReceiverBean;
import com.example.asus88.finaldesgin.bean.SendTakBean;
import com.example.asus88.finaldesgin.util.LogUtil;
import com.example.asus88.finaldesgin.util.SharePUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Manager {
    private static final String TAG = "Manager";

    public void onTransferClosed(Transfer t) {
        if (t.isDisconnected()) {
            mainPool.execute(new Runnable() {
                public void run() {
                    disconnectedTransferCount--;
                    if (disconnectedTransferCount == 0) {
                        // 不需要重连探测
                        stopReconnectDetect();
                    }
                }
            });
        }

        devMap.remove(t.getRemoteDev());//删除设备列表中对应的项
        //刷新设备列表
        onUpdateDevTransfer(t.getRemoteDev(), false);
    }

    private volatile int disconnectedTransferCount = 0;

    public void onTransferDisconnected(Transfer t) {
        Log.i("TAG","链接中断");
        mainPool.execute(new Runnable() {
            public void run() {
                disconnectedTransferCount++;
                NetworkInterface i = Dev.getLocalInterface();
                if (i == null) {
                    // 断网
                    detectNetwork();
                } else {
                    startReconnectDetect();
                }
            }
        });

        //有连接断开，刷新设备列表
        onUpdateDevTransfer(t.getRemoteDev(), false);
    }

    public void onTransferReconnectSucceed(Transfer t) {
        mainPool.execute(new Runnable() {
            public void run() {
                disconnectedTransferCount--;
                if (disconnectedTransferCount == 0) {
                    // 不需要重连探测
                    stopReconnectDetect();
                }
            }
        });
        onUpdateDevTransfer(t.getRemoteDev(), true);
    }

    public void onTransferReconnectFailed(Transfer t) {
        //提醒用户检查网络
        if (mOnDevMapChangeListener != null) {
            mOnDevMapChangeListener.onNetWorkStateChange();
        } else {
            LogUtil.logd(TAG, "please implements onDevMapChangeListener");
        }
    }

    public void onTransferReceiveListChanged(Transfer t, Task task, int action) {
        //刷新接收列表
        Log.d(TAG, "onTransferReceiveListChanged: ");
        if (mOnReceiveTaskListChangeListener != null) {
            mOnReceiveTaskListChangeListener.onReceiveTaskChange(t, task, action);
        }
    }

    public void onTransferSendListChanged(Transfer t, Task task, int action) {
        //刷新发送列表
        Log.d(TAG, "onTransferSendListChanged: ");
        if (mOnSendTaskListChangeListener != null) {
            mOnSendTaskListChangeListener.onSendTaskChane(t, task, action);
        }
    }

    private void onUpdateDevTransfer(Dev dev, boolean isEnabled) {
        Log.d(TAG, "onUpdateDevTransfer: "+isEnabled);
        if (mOnDevMapChangeListener != null) {
            mOnDevMapChangeListener.onTransferStateChange(dev, isEnabled);
        } else {
            LogUtil.logd(TAG, "please implements the onDevMapChangeListener");
        }
    }

    private void onUpdateDeviceMap(Dev dev, boolean isAdd) {
        //如果正在显示设备列表则刷新设备列表
        Log.d(TAG, "onUpdateDeviceMap: " + dev.mac + isAdd);
        if (mOnDevMapChangeListener != null) {
            mOnDevMapChangeListener.onDevNumChange(dev, isAdd);
        } else {
            LogUtil.logd(TAG, "please implements the onDevMapChangeListener");
        }
    }

    private void onCreatedTransfer(Dev t) {
        //提示用户成功创建与其他设备的连接
        onUpdateDevTransfer(t, true);
    }

    private void onCreateTransferFailed(Dev d) {
        //提示用户，与dev的连接失败了
        if (mOnDevMapChangeListener != null) {
            mOnDevMapChangeListener.onCreateTransferFail(d);
        }
    }

    private void onAcceptedTransfer(Dev t) {
        //提示用户收到其他设备的连接
        onUpdateDevTransfer(t, true);
    }

    private static Manager ref;
    private String storePath;
    private volatile ExecutorService mainPool;
    private volatile Map<Dev, Transfer> devMap;
    private volatile SocketListener socketListener;
    private volatile DeviceDetector deviceDetector;
    private volatile ReconnectDetector reconnectDetector;
    private volatile Timer networkDetector;
    private onDevMapChangeListener mOnDevMapChangeListener;
    private onSendTaskListChangeListener mOnSendTaskListChangeListener;
    private onReceiveTaskListChangeListener mOnReceiveTaskListChangeListener;


    public static Manager getManager() {
        if (ref != null) {
            return ref;
        }
        synchronized (Manager.class) {
            if (ref == null) {
                ref = new Manager();
                return ref;
            } else {
                return ref;
            }
        }
    }

    private Manager() {
        mainPool = Executors.newSingleThreadExecutor();
        devMap = new LinkedHashMap<Dev, Transfer>();
        storePath = SharePUtil.read("savePath", "path");
        if (TextUtils.isEmpty(storePath)) {
            String absPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File file = new File(absPath + "/receiveFile");
            if (!file.exists()) {
                boolean flag = file.mkdirs();
                if (!flag) {
                    try {
                        throw new Exception("创建失败");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            storePath = file.getAbsolutePath() + "/";
        }
        Log.d(TAG, "Manager: " + storePath);
        // 用于监听套接字的链接
        socketListener = new SocketListener();
        socketListener.start();
    }

    // 若返回false，应提示用户检查是否已经连上wifi或者开启了热点
    public boolean searchDevice() {
        Log.d(TAG, "searchDevice: ");
        // 用与搜索可链接设备的探测器
        if (deviceDetector == null) {
            deviceDetector = new DeviceDetector();
        }
        return deviceDetector.start();
    }

    // 调用此方法可停止搜索设备
    public void stopSearchDevice() {
        if (deviceDetector != null) {
            deviceDetector.stop();
        }
    }

    // dev参数应从设备列表获取
    public boolean createTransfer(final Dev dev) {
        if (devMap.get(dev) != null)
            return false;// 对应设备的链接已经存在，不可重复建立

        mainPool.execute(new Runnable() {
            public void run() {
                try {
                    Transfer t = Transfer.createTransfer(dev);
                    devMap.put(dev, t);
                    onCreatedTransfer(dev);// 提示链接成功
                    //onUpdateDeviceMap();// 刷新列表
                    onUpdateDevTransfer(dev, true);
                } catch (IOException e) {
                    e.printStackTrace();
                    onCreateTransferFailed(dev);// 提示链接失败
                }
            }
        });
        // 允许创建链接，不过需要等待事件的回调才能知道创建结果
        return true;
    }

    public String getStorePath() {
        return storePath;
    }

    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }

    private void detectNetwork() {
        if (networkDetector != null)
            return;

        networkDetector = new Timer();
        networkDetector.schedule(new TimerTask() {
            public void run() {
                NetworkInterface i = Dev.getLocalInterface();
                if (i != null) {
                    // 接入网络
                    if (disconnectedTransferCount > 0) {
                        startReconnectDetect();
                    }
                    this.cancel();
                    networkDetector.cancel();
                    networkDetector = null;
                } else {
                    // 提示用户检查是否接入wifi
                }
            }
        }, 10000, 10000);
    }

    private boolean startReconnectDetect() {
        if (reconnectDetector == null) {
            reconnectDetector = new ReconnectDetector();
        }
        return reconnectDetector.start();
    }

    private void stopReconnectDetect() {
        if (reconnectDetector != null) {
            reconnectDetector.stop();
        }
    }

    private class SocketListener extends SocketAccepter {
        public SocketListener() {
            super(LISTEN_PORT);
        }

        protected void onAcceptedSocket(SocketChannel msgChannel, SocketChannel dataChannel) {
            InetAddress remoteAddr = msgChannel.socket().getInetAddress();// 获取对方ip

            for (Map.Entry<Dev, Transfer> entry : devMap.entrySet()) {
                if (entry.getKey().ip.equals(remoteAddr)) {// 对方ip与map中某项key对应
                    if (entry.getValue() == null) {// 新链接
                        Transfer t = Transfer.createTransfer(entry.getKey(), msgChannel, dataChannel);
                        entry.setValue(t);
                        onAcceptedTransfer(entry.getKey());//收到其他设备的链接
                    } else {// 重连
                        entry.getValue().setSocket(msgChannel, dataChannel);
                    }
                    return;
                }
            }

            try {
                msgChannel.close();
                dataChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class DeviceDetector extends MulticastHelper {
        public static final int DETECTOR_PORT = 12450;
        private volatile Map<String, Dev> map;

        public DeviceDetector() {
            super(DETECTOR_PORT);
            map = new HashMap<String, Dev>();
        }

        private void onUpdate(Dev dev, boolean quit) {
            Log.d(TAG, "onUpdate: ");
            if (!devMap.containsKey(dev)) {// map中不存在对应设备
                Log.d(TAG, "onUpdate: llllll");
                if (!quit) {
                    Log.d(TAG, "onUpdate: " + dev.mac);
                    Log.d(TAG, "onUpdate: add dev");
                    devMap.put(dev, null);
                    onUpdateDeviceMap(dev, true);// 搜索到新设备，刷新列表
                }
            } else {
                if (quit && devMap.get(dev) == null) {
                    devMap.remove(dev);
                    onUpdateDeviceMap(dev, false);// 有设备退出搜索，刷新列表
                }
            }
        }

        protected void onClosed() {
            map.clear();
            // 退出搜索设备并清空未链接设备
            for (Map.Entry<Dev, Transfer> entry : devMap.entrySet()) {
                if (entry.getValue() == null) {
                    devMap.remove(entry.getKey());
                }
            }
        }

        protected void onReceivePacket(DatagramPacket packet) {
            String str = new String(packet.getData(), 0, packet.getLength());
            Dev local = Dev.getLocalDev();
            System.out.println("收到ip:" + packet.getAddress() + "本地ip:" + local.ip + "数据包" + str);

//            try {
//                Enumeration<NetworkInterface> aa = NetworkInterface.getNetworkInterfaces();
//                while (aa.hasMoreElements()){
//                    NetworkInterface it = aa.nextElement();
//                    System.out.println(it.getDisplayName()+"____"+it.isUp());
//                    Enumeration<InetAddress> bb = it.getInetAddresses();
//                    while(bb.hasMoreElements()){
//                        InetAddress cc = bb.nextElement();
//                        System.out.println(cc);
//                    }
//                }
//            } catch (SocketException e) {
//                e.printStackTrace();
//            }
            //非ipv6 本地地址？
            if (!packet.getAddress().isAnyLocalAddress() && packet.getAddress().isSiteLocalAddress()
                    && !packet.getAddress().equals(local.ip)) {
                try {
                    JSONObject obj = new JSONObject(str);
                    String mac = obj.getString("mac");
                    Dev dev = map.get(mac);
                    if (dev == null) {
                        dev = new Dev(obj.getString("name"), packet.getAddress(), mac, obj.getInt("devType"));
                        map.put(mac, dev);
                    }
                    onUpdate(dev, obj.getBoolean("quit"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        protected DatagramPacket getBroadcastPacket() throws Exception {
            Dev localDev = Dev.getLocalDev();
            DatagramPacket packet = new DatagramPacket(new byte[PACKET_LEN], PACKET_LEN, InetAddress.getByName(GROUP_IP),
                    DETECTOR_PORT);

            JSONObject obj = new JSONObject();
            obj.put("name", localDev.name);
            obj.put("mac", localDev.mac);
            obj.put("devType", localDev.type);
            obj.put("quit", false);

            String str = obj.toString();
            packet.setData(str.getBytes());
            packet.setLength(str.getBytes().length);

            return packet;
        }

        protected DatagramPacket getQuitPacket() throws Exception {
            Dev localDev = Dev.getLocalDev();
            DatagramPacket packet = new DatagramPacket(new byte[PACKET_LEN], PACKET_LEN, InetAddress.getByName(GROUP_IP),
                    DETECTOR_PORT);

            JSONObject obj = new JSONObject();
            obj.put("name", localDev.name);
            obj.put("mac", localDev.mac);
            obj.put("devType", localDev.type);
            obj.put("quit", true);

            String str = obj.toString();
            packet.setData(str.getBytes());
            packet.setLength(str.getBytes().length);

            return packet;
        }
    }

    private class ReconnectDetector extends MulticastHelper {
        public static final int RECONNECT_PORT = 12449;
        private volatile Map<String, Dev> map;

        public ReconnectDetector() {
            super(RECONNECT_PORT);
            map = new HashMap<String, Dev>();
        }

        private void onReceive(Dev dev) {
            // 更新ip
            Transfer t = devMap.get(dev);
            if (t != null) {
                t.getRemoteDev().ip = dev.ip;
                if (t.isDisconnected() && t.getState() == Transfer.CREATED) {
                    t.reconnect();
                }
            }
        }

        protected void onClosed() {
            map.clear();
        }

        protected void onReceivePacket(DatagramPacket packet) {
            String str = new String(packet.getData(), 0, packet.getLength());
            System.out.println(str);

            Dev local = Dev.getLocalDev();
            if (!packet.getAddress().isAnyLocalAddress() && packet.getAddress().isSiteLocalAddress()
                    && !packet.getAddress().equals(local.ip)) {

                // 其他设备发来的消息
                Dev dev = null;
                try {
                    JSONObject obj = new JSONObject(str);
                    String mac = obj.getString("mac");
                    dev = map.get(mac);
                    if (dev == null) {
                        dev = new Dev(obj.getString("name"), packet.getAddress(), mac, obj.getInt("devType"));
                        map.put(mac, dev);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                onReceive(dev);
            }
        }

        protected DatagramPacket getBroadcastPacket() throws Exception {
            Dev localDev = Dev.getLocalDev();
            DatagramPacket packet = new DatagramPacket(new byte[PACKET_LEN], PACKET_LEN, InetAddress.getByName(GROUP_IP),
                    RECONNECT_PORT);

            JSONObject obj = new JSONObject();
            obj.put("name", localDev.name);
            obj.put("mac", localDev.mac);
            obj.put("devType", localDev.type);
            String str = obj.toString();
            packet.setData(str.getBytes());
            packet.setLength(str.getBytes().length);

            return packet;
        }

        protected DatagramPacket getQuitPacket() throws Exception {
            return getBroadcastPacket();
        }
    }

    public void setOnDevMapChangeListener(onDevMapChangeListener onDevMapChangeListener) {
        mOnDevMapChangeListener = onDevMapChangeListener;
    }

    public void setOnSendTaskListChangeListener(onSendTaskListChangeListener onSendTaskListChangeListener) {
        mOnSendTaskListChangeListener = onSendTaskListChangeListener;
    }

    public void setOnReceiveTaskListChangeListener(onReceiveTaskListChangeListener onReceiveTaskListChangeListener) {
        mOnReceiveTaskListChangeListener = onReceiveTaskListChangeListener;
    }

    public Map<Dev, Transfer> getDevMap() {
        return devMap;
    }

    public List<DevBean> getLinkingDev() {
        List<DevBean> list = new ArrayList<>();
        Iterator<Map.Entry<Dev, Transfer>> iterator = devMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Dev, Transfer> entry = iterator.next();
            Transfer t = entry.getValue();
            if (t.isEnable()) {
                DevBean bean = new DevBean();
                bean.setName(entry.getKey().getName());
                bean.setTransfer(t);
                bean.setSelected(false);
                list.add(bean);
            }
        }
        return list;
    }

    public Transfer getTransferFromMap(Dev dev) {
        Iterator<Map.Entry<Dev, Transfer>> iterator = devMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Dev, Transfer> entry = iterator.next();
            if (dev.equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 返回当前设备接收列表
     *
     * @return
     */
    public List<Task> getReceiveTaskList() {
        List<Task> taskList = new ArrayList<>();
        Iterator<Map.Entry<Dev, Transfer>> iterator = devMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Dev, Transfer> entry = iterator.next();
            Transfer t = entry.getValue();
            if (t != null && t.isEnable()) {
                taskList.addAll(t.getReceiveTaskList());
            }
        }
        return taskList;
    }

    /**
     * 返回当前设备发送列表
     *
     * @return
     */
    public List<SendTakBean> getSendTaskList() {
        List<SendTakBean> list = new ArrayList<>();
        Iterator<Map.Entry<Dev, Transfer>> iterator = devMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Dev, Transfer> entry = iterator.next();
            Transfer t = entry.getValue();
            if (t != null & t.isEnable() && t.getSendTaskList().size() > 0) {
                ReceiverBean rBean = new ReceiverBean();
                rBean.setDev(t.getRemoteDev());
                rBean.setSendList(new ArrayList<Task>());
                //   rBean.setSendList(t.getSendTaskList());
                rBean.getSendList().addAll(t.getSendTaskList());
                list.add(rBean);
            }
        }
        return list;
    }


    public interface onDevMapChangeListener {
        void onDevNumChange(Dev dev, boolean isAdd);

        void onTransferStateChange(Dev dev, boolean isEnabled);

        void onNetWorkStateChange();

        void onCreateTransferFail(Dev dev);
    }

    public interface onSendTaskListChangeListener {
        void onSendTaskChane(Transfer transfer, Task task, int action);

    }

    public interface onReceiveTaskListChangeListener {
        void onReceiveTaskChange(Transfer transfer, Task task, int action);

    }
}
