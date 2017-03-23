package com.example.asus88.finaldesgin.connection;

import android.util.Log;

import com.example.asus88.finaldesgin.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Transfer {
    private static final String TAG = "Transfer";

    private class ReceiveDataRunnable implements Runnable {
        private String destPath;
        private long srcOffset;
        private long deservedCount;
        private boolean isStop;

        public ReceiveDataRunnable(String destPath, long srcOffset, long deservedCount) {
            this.destPath = destPath;
            this.srcOffset = srcOffset;
            this.deservedCount = deservedCount;
        }

        public void stopReceive() {
            isStop = true;
            rSelector.wakeup();
        }

        public void run() {
            /**
             * 接收前清空缓冲区，然后才开启线程接收
             */
            clearBuffer();
            /**
             * 表明从新的文件头发送数据，则之前的接收到的文件长度累加进已接收字节数中
             */
            if (srcOffset == 0) {
                receiveTaskRef.transferedCount += receiveTaskRef.offset;
            }
            receiveTaskRef.offset = srcOffset;
            try {
                FileOutputStream fileOut = new FileOutputStream(destPath, (srcOffset != 0));
                int receivedCount = 0;
                int size = 0;
                if (deservedCount != 0) {
                    FileChannel fileChannel = fileOut.getChannel();
                    fileChannel.position(srcOffset);
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(RECEIVE_BUFFER_SIZE);
                    ackReceiveData();
                    while (!isStop) {
                        rSelector.select();
                        size = dataSocket.read(byteBuffer);
                        if (size == -1)
                            break;
                        byteBuffer.flip();
                        fileChannel.write(byteBuffer);
                        byteBuffer.clear();
                        receivedCount += size;
                        onReceiveProgress(size);
                        if (receivedCount == deservedCount)
                            break;
                    }
                }
                fileOut.close();
                synDataResult(receiveTaskRef.offset, false);
            } catch (Exception e) {
                e.printStackTrace();
                synDataResult(-1, true);
            } finally {
                receiveDataRunnable = null;
            }
        }
    }

    private class SendDataRunnable implements Runnable {
        private String srcPath;
        private long srcOffset;
        private long deservedCount;
        private boolean isStop;

        public SendDataRunnable(String srcPath, long srcOffset, long deservedCount) {
            this.srcPath = srcPath;
            this.srcOffset = srcOffset;
            this.deservedCount = deservedCount;
        }

        public void stopSend() {
            isStop = true;
            sSelector.wakeup();
        }

        public void run() {
            try {
                FileInputStream fileIn = new FileInputStream(srcPath);
                int size = 0;
                long writtenCount = 0;
                if (deservedCount != 0) {
                    FileChannel fileChannel = fileIn.getChannel();
                    fileChannel.position(srcOffset);
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(SEND_BUFFER_SIZE);
                    while (!isStop) {
                        if (fileChannel.read(byteBuffer) == -1)
                            break;
                        byteBuffer.flip();
                        sSelector.select();
                        do {
                            size = dataSocket.write(byteBuffer);
                            writtenCount += size;
                            onSendProgress(size);
                        } while (byteBuffer.hasRemaining());
                        byteBuffer.clear();
                        if (writtenCount == deservedCount)
                            break;
                    }
                }
                fileIn.close();
            } catch (Exception e) {
                sendTaskRef.state = Task.FAILED;
            } finally {
                sendDataRunnable = null;
            }
        }
    }

    private abstract class KeepAliveTimerTask extends TimerTask {
        private int log = 5;

        public void reset() {
            log = 5;
        }

        public void run() {
            sendMsg("heart beat");
            log--;
            if (log == 0) {
                this.cancel();
                onTimeOut();
            }
        }

        protected abstract void onTimeOut();
    }

    public int getState() {
        return state;
    }

    public Dev getRemoteDev() {
        return remoteDev;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public boolean isDisconnected() {
        return isDisconnected;
    }

    public List<Task> getSendTaskList() {
        return sendTaskList;
    }

    public List<Task> getReceiveTaskList() {
        return receiveTaskList;
    }

    private volatile int state;// 连接发起方或接收方的状态
    private volatile Dev remoteDev;// 包含对方设备的信息
    private volatile boolean isEnable;// 链接是否可用
    private volatile boolean isDisconnected;// 链接是否意外断开
    private volatile boolean isClosed;// 链接是否被关闭
    private volatile SocketChannel msgSocket;// 用于通讯的套接字
    private volatile SocketChannel dataSocket;// 用于传输数据的套接字
    private volatile BufferedReader msgIn;
    private volatile BufferedWriter msgOut;
    private volatile InputStream dataIn;
    private volatile Selector rSelector;
    private volatile Selector sSelector;
    private volatile ExecutorService mainPool;
    private volatile ExecutorService receiveThreadPool;
    private volatile ExecutorService sendThreadPool;
    private volatile ReceiveDataRunnable receiveDataRunnable;
    private volatile SendDataRunnable sendDataRunnable;
    private volatile Timer keepAliveTimer;// 心跳计时器
    private volatile KeepAliveTimerTask keepAliveTimerTask;// 心跳任务
    private volatile Queue<Task> initTaskQueue;// 初始化任务队列
    private volatile List<Task> sendTaskList;// 发送任务列表
    private volatile List<Task> receiveTaskList;// 接收任务列表
    private volatile Task initTaskRef;// 当前正在初始化的任务
    private volatile Task sendTaskRef;// 当前正在发送的任务
    private volatile Task receiveTaskRef;// 当前正在接收的任务

    private Transfer(int state, Dev remoteDev, SocketChannel msgChannel, SocketChannel dataChannel) {
        this.state = state;// 连接状态
        this.remoteDev = remoteDev;// 对方设备信息
        mainPool = Executors.newSingleThreadExecutor();
        receiveThreadPool = Executors.newSingleThreadExecutor();
        sendThreadPool = Executors.newSingleThreadExecutor();
        keepAliveTimer = new Timer();
        initTaskQueue = new LinkedList<>();// 初始化任务队列
        sendTaskList = new LinkedList<>();// 发送列表
        receiveTaskList = new LinkedList<>();// 接收列表
        setSocket(msgChannel, dataChannel);
    }

    public static Transfer createTransfer(Dev remoteDev) throws IOException {// 耗时操作
        return new Transfer(CREATED, remoteDev,
                SocketChannel.open(new InetSocketAddress(remoteDev.ip, SocketAccepter.LISTEN_PORT)),
                SocketChannel.open(new InetSocketAddress(remoteDev.ip, SocketAccepter.LISTEN_PORT)));
    }

    public static Transfer createTransfer(Dev remoteDev, SocketChannel msgChannel, SocketChannel dataChannel) {
        return new Transfer(ACCEPTED, remoteDev, msgChannel, dataChannel);
    }

    public void reconnect() {
        mainPool.execute(new Runnable() {
            public void run() {
                if (!isDisconnected) {
                    try {
                        setSocket(SocketChannel.open(new InetSocketAddress(remoteDev.ip, SocketAccepter.LISTEN_PORT)),
                                SocketChannel.open(new InetSocketAddress(remoteDev.ip, SocketAccepter.LISTEN_PORT)));
                    } catch (IOException e) {
                        e.printStackTrace();
                        // 通知管理类重连失败
                        Manager.getManager().onTransferReconnectFailed(Transfer.this);
                    }
                }
            }
        });
    }

    public void clearBuffer() {
        try {
            dataIn.skip(dataIn.available());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSocket(SocketChannel msgSocket, SocketChannel dataSocket) {
        if (isEnable)
            return;

        isEnable = true;

        try {
            this.msgSocket = msgSocket;
            msgSocket.socket().setTcpNoDelay(true);
            msgIn = new BufferedReader(new InputStreamReader(msgSocket.socket().getInputStream()));
            msgOut = new BufferedWriter(new OutputStreamWriter(msgSocket.socket().getOutputStream()));

            this.dataSocket = dataSocket;
            dataSocket.socket().setTcpNoDelay(true);
            dataIn = dataSocket.socket().getInputStream();
            dataSocket.configureBlocking(false);  //设置非阻塞
            rSelector = Selector.open();
            dataSocket.register(rSelector, SelectionKey.OP_READ);
            sSelector = Selector.open();
            dataSocket.register(sSelector, SelectionKey.OP_WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 开启线程监听消息
        new Thread() {
            public void run() {
                try {
                    do {
                        String msg = msgIn.readLine();
                        if (msg != null) {
                            msgResolve(msg);
                        }
                    } while (true);
                } catch (IOException e) {
                    shutdown();
                }
            }
        }.start();// 创建一个线程用于监听消息
        /**
         * 开始心跳检测
         */
        keepAliveTimerTask = new KeepAliveTimerTask() {
            protected void onTimeOut() {
                // 链接超时
                shutdown();
            }
        };
        keepAliveTimer.schedule(keepAliveTimerTask, 1000, 1000);// 每隔1秒发一次心跳包

        initTask();
        dispatch();

        if (isDisconnected) {
            isDisconnected = false;
            Manager.getManager().onTransferReconnectSucceed(this);
        }
    }

    public void close() {
        if (isClosed)
            return;

        isClosed = true;

        if (isDisconnected) {
            // 用户关闭
            mainPool.shutdown();
            sendThreadPool.shutdown();
            receiveThreadPool.shutdown();
            keepAliveTimer.cancel();
            clearAllTasks();
            // 通知管理类链接已经关闭
            Manager.getManager().onTransferClosed(Transfer.this);
        } else {
            synClose();
        }
    }

    private void shutdown() {
        if(mainPool.isShutdown()) return;

        mainPool.execute(new Runnable() {
            public void run() {
                if (isEnable == false)
                    return;

                try {
                    msgSocket.close();
                    dataSocket.close();
                    rSelector.close();
                    sSelector.close();
                    msgIn.close();
                    msgOut.close();
                    dataIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                /**
                 * 取消心跳任务
                 */
                if (keepAliveTimerTask != null) {
                    keepAliveTimerTask.cancel();
                    keepAliveTimerTask = null;
                }
                if (sendDataRunnable != null)
                    sendDataRunnable.stopSend();
                if (receiveDataRunnable != null)
                    receiveDataRunnable.stopReceive();

                if (isClosed) {
                    // 用户关闭
                    mainPool.shutdown();
                    sendThreadPool.shutdown();
                    receiveThreadPool.shutdown();
                    keepAliveTimer.cancel();
                    clearAllTasks();
                    // 通知管理类链接已经关闭
                    Manager.getManager().onTransferClosed(Transfer.this);
                } else {
                    // 意外关闭
                    isDisconnected = true;
                    initTaskRef = null;
                    if (sendTaskRef != null && ((SendTask) sendTaskRef).getFocusFile().isFile()) {
                        sendTaskRef.isBackup = true;
                    }
                    sendTaskRef = null;
                    receiveTaskRef = null;
                    pauseAllTasks();
                    // 通知管理类链接已经断开
                    Manager.getManager().onTransferDisconnected(Transfer.this);
                }

                isEnable = false;
            }
        });
    }

    private void clearAllTasks() {
        // 清空所有任务
        initTaskQueue.clear();
        receiveTaskList.clear();
        sendTaskList.clear();

        Manager.getManager().onTransferSendListChanged(Transfer.this, null, ACTION_CLEAR);
        Manager.getManager().onTransferReceiveListChanged(Transfer.this, null, ACTION_CLEAR);
    }

    private void pauseAllTasks() {
        // 暂停所有任务
        for (Task t : sendTaskList) {
            if (t.state == Task.RUN || t.state == Task.WAIT) {
                t.state = Task.PAUSE;
            }
        }
        for (Task t : receiveTaskList) {
            if (t.state == Task.RUN || t.state == Task.WAIT) {
                t.state = Task.PAUSE;
            }
        }

        Manager.getManager().onTransferSendListChanged(Transfer.this, null, ACTION_CHANGE);
        Manager.getManager().onTransferReceiveListChanged(Transfer.this, null, ACTION_CHANGE);
    }

    /**
     * 添加任务
     *
     * @param path
     * @throws Exception
     */
    public void addTask(String path) throws Exception {
        Log.d(TAG, "addTask: " + path);
        if (isClosed)
            throw new Exception("链接已关闭");
        if (!isEnable)
            throw new Exception("链接不可用");

        initTaskQueue.offer(SendTask.createSendTask(path,remoteDev));
        initTask();
    }

    public void addPhotoGroupTask(String path) throws Exception {
        Log.d(TAG, "addTask: " + path);
        if (isClosed)
            throw new Exception("链接已关闭");
        if (!isEnable)
            throw new Exception("链接不可用");

        initTaskQueue.offer(SendTask.createSendTask(path,remoteDev,new String[]{"jpg","jpeg","png","bmp","gif"}));
        initTask();
    }

    private void stopSendTask() {
        if (sendTaskRef != null && sendTaskRef.state == Task.RUN) {
            sendTaskRef.state = Task.PAUSE;
            requestStopReceiveData();
            if (sendDataRunnable != null) {
                sendDataRunnable.stopSend();
            }
        }
    }

    private void stopReceiveTask() {
        if (receiveTaskRef != null) {
            requestStopSendTask();
        }
    }

    public void clickSendTaskListItem(Task t) {
        switch (t.state) {
            case Task.WAIT: {
                t.state = Task.PAUSE;

                Manager.getManager().onTransferSendListChanged(Transfer.this, t, ACTION_CHANGE);
            }
            break;
            case Task.RUN: {
                stopSendTask();
            }
            break;
            case Task.PAUSE: {
                t.state = Task.WAIT;
                Manager.getManager().onTransferSendListChanged(Transfer.this, t, ACTION_CHANGE);
                dispatch();
            }
            break;
        }
    }

    public void clickReceiveTaskListItem(Task t) {
        switch (t.state) {
            case Task.WAIT: {
                t.state = Task.PAUSE;
                Manager.getManager().onTransferReceiveListChanged(Transfer.this, t, ACTION_CHANGE);
            }
            break;
            case Task.RUN: {
                stopReceiveTask();
            }
            break;
            case Task.PAUSE: {
                t.state = Task.WAIT;
                Manager.getManager().onTransferReceiveListChanged(Transfer.this, t, ACTION_CHANGE);
                dispatch();
            }
            break;
        }
    }

    /**
     * 初始化任务
     */
    private void initTask() {
        mainPool.execute(new Runnable() {
            public void run() {
                if (initTaskRef != null)// 当前正在初始化
                    return;
                if (initTaskQueue.isEmpty())// 没有等待初始化的任务
                    return;
                if (!isEnable)
                    return;// 链接不可用
                if (isClosed)
                    return;

                initTaskRef = initTaskQueue.element();
                synNewReceiveTask(initTaskRef.mID, initTaskRef.name, initTaskRef.totalCount);
            }
        });
    }

    private void dispatch() {
        mainPool.execute(new Runnable() {
            public void run() {
                if (sendTaskRef != null)// 当前正在发送
                    return;
                if (sendTaskList.isEmpty())// 没有可发送的任务
                    return;
                if (!isEnable)
                    return;// 链接不可用
                if (isClosed)
                    return;

                // 选择处于等待发送状态的任务
                for (Task t : sendTaskList) {
                    if (t.type == Task.SEND_TASK_TYPE && t.state == Task.WAIT) {
                        sendTaskRef = t;
                        synStartReceiveTask(sendTaskRef.remoteID);
                        break;
                    }
                }
            }
        });
    }

    private void handle() {
        mainPool.execute(new Runnable() {
            public void run() {
                if (sendTaskRef == null)
                    return;
                if (!isEnable)
                    return;// 链接不可用
                if (isClosed)
                    return;

                int state = sendTaskRef.state;
                Manager.getManager().onTransferSendListChanged(Transfer.this, sendTaskRef, ACTION_CHANGE);
                switch (state) {
                    case Task.RUN: {
                        File f = ((SendTask) sendTaskRef).getFocusFile();
                        String srcPath = f.getPath().substring(sendTaskRef.path.length());
                        if (f.isFile()) {
                            synReceiveData(srcPath, sendTaskRef.offset, f.length() - sendTaskRef.offset);
                        } else {
                            synCreateDirectory(srcPath);
                        }
                    }
                    break;
                    case Task.PAUSE:
                    case Task.OVER:
                    case Task.FAILED: {
                        synTaskResult(state);
                    }
                    break;
                }
            }
        });
    }

    //syn 要求对方做  ack 确认信息
    private void msgResolve(final String msg) {// 在mainPool的线程中执行，保证消息处理的顺序正确
        mainPool.execute(new Runnable() {
            public void run() {
                if (msg.equals("heart beat")) {
                    keepAliveTimerTask.reset();
                    return;
                }

                try {
                    JSONObject obj = new JSONObject(msg);
                    switch (obj.getInt("type")) {
                        case SYN_NEW_RECEIVE_TASK_TYPE: {
                            onSynNewReceiveTask(obj.getInt("mID"), obj.getString("name"), obj.getLong("totalCount"));
                        }
                        break;
                        case ACK_NEW_RECEIVE_TASK_TYPE: {
                            onAckNewReceiveTask(obj.getInt("mID"), obj.getBoolean("isCreated"));
                        }
                        break;
                        case SYN_START_RECEIVE_TASK_TYPE: {
                            onSynStartReceiveTask(obj.getInt("remoteID"));
                        }
                        break;
                        case ACK_START_RECEIVE_TASK_TYPE: {
                            onAckStartReceiveTask(obj.getLong("offset"), obj.getBoolean("isStarted"));
                        }
                        break;
                        case SYN_CREATE_DIRECTORY_TYPE: {
                            onSynCreateDirectory(obj.getString("srcPath"));
                        }
                        break;
                        case ACK_CREATE_DIRECTORY_TYPE: {
                            onAckCreateDirectory(obj.getBoolean("isCreated"));
                        }
                        break;
                        case SYN_RECEIVE_DATA_TYPE: {
                            onSynReceiveData(obj.getString("srcPath"), obj.getLong("srcOffset"), obj.getLong("deservedCount"));
                        }
                        break;
                        case ACK_RECEIVE_DATA_TYPE: {
                            sendDataRunnable = new SendDataRunnable(((SendTask) sendTaskRef).getFocusFile().getPath(),
                                    sendTaskRef.offset, ((SendTask) sendTaskRef).getFocusFile().length() - sendTaskRef.offset);
                            sendThreadPool.execute(sendDataRunnable);
                        }
                        break;
                        case SYN_DATA_RESULT_TYPE: {
                            onSynDataResult(obj.getLong("offset"), obj.getBoolean("isFailed"));
                        }
                        break;
                        case SYN_TASK_RESULT_TYPE: {
                            onSynTaskResult(obj.getInt("state"));
                        }
                        break;
                        case ACK_TASK_RESULT_TYPE: {
                            sendTaskRef = null;
                            dispatch();
                        }
                        break;
                        case SYN_CLOSE_TYPE: {
                            isClosed = true;
                            ackClose();
                        }
                        break;
                        case ACK_CLOSE_TYPE: {
                            shutdown();
                        }
                        break;
                        case REQUEST_STOP_RECEIVE_DATA_TYPE: {
                            if (receiveDataRunnable != null) {
                                receiveDataRunnable.stopReceive();
                            }
                        }
                        break;
                        case REQUEST_STOP_SEND_TASK_TYPE: {
                            stopSendTask();
                        }
                        break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendMsg(final String msg) {// 在mainPool线程池中执行保证消息发送顺序
        mainPool.execute(new Runnable() {
            public void run() {
                Log.i("tag", "发送:" + msg);
                try {
                    msgOut.write(msg);
                    msgOut.newLine();
                    msgOut.flush();
                } catch (IOException e) {
                    shutdown();
                }
            }
        });
    }

    private void onSynNewReceiveTask(int mID, String name, long totalCount) {
        /**
         * 删除重复任务
         */
        Iterator<Task> it = receiveTaskList.iterator();
        while (it.hasNext()) {
            Task t = it.next();
            if (t.type == Task.RECEIVE_TASK_TYPE && t.remoteID == mID) {
                it.remove();
            }
        }
        /**
         * 创建新任务，避免重名
         */
        try {
            int n = 1;
            String tempName = name;
            File f;
            f = new File(Manager.getManager().getStorePath() + tempName);
            while (f.exists()) {
                if (f.isDirectory()) {
                    tempName = name + "-" + String.valueOf(n++);
                } else {
                    tempName = Utils.getFileNameWithoutSuffix(name) + "-" + String.valueOf(n++) + "."
                            + Utils.getFileType(name);
                }
                f = new File(Manager.getManager().getStorePath() + tempName);
            }
            Task tempTask = Task.createReceiveTask(tempName, Manager.getManager().getStorePath() + tempName,
                    totalCount, remoteDev);
            tempTask.remoteID = mID;
            receiveTaskList.add(tempTask);
            ackNewReceiveTask(tempTask.mID, true);
            //通知Manager新增了任务
            Manager.getManager().onTransferReceiveListChanged(Transfer.this, tempTask, ACTION_ADD);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("tag", "异常");
            ackNewReceiveTask(-1, false);
        }
    }

    private void onAckNewReceiveTask(int mID, boolean isCreated) {
        initTaskQueue.poll();
        if (isCreated) {
            initTaskRef.remoteID = mID;
            sendTaskList.add(initTaskRef);
            //通知Manager新增了任务
            Manager.getManager().onTransferSendListChanged(Transfer.this, initTaskRef, ACTION_ADD);
        } else {
            // 对方创建任务失败，应进行提示
        }
        initTaskRef = null;
        initTask();
        dispatch();
    }

    private void onSynStartReceiveTask(int remoteID) {
        for (Task t : receiveTaskList) {
            if (t.type == Task.RECEIVE_TASK_TYPE && t.mID == remoteID) {
                t.state = Task.RUN;
                receiveTaskRef = t;
                //通知任务已开始
                Manager.getManager().onTransferReceiveListChanged(Transfer.this, receiveTaskRef, ACTION_CHANGE);
                break;
            }
        }
        if (receiveTaskRef != null) {
            ackStartReceiveTask(receiveTaskRef.offset, true);
        } else {
            ackStartReceiveTask(-1, false);
        }
    }

    private void onAckStartReceiveTask(long offset, boolean isStarted) {
        if (isStarted) {
            sendTaskRef.state = Task.RUN;
            if (sendTaskRef.isBackup) {
                sendTaskRef.offset = offset;
                if (sendTaskRef.offset == ((SendTask) sendTaskRef).getFocusFile().length()) {
                    ((SendTask) sendTaskRef).nextFile();
                }
            }
        } else {
            // 开始任务失败
            sendTaskRef.state = Task.FAILED;
        }
        handle();
    }

    private void onSynCreateDirectory(String srcPath) {
        File f = new File(receiveTaskRef.path + srcPath);
        boolean isCreated = f.exists() || f.mkdirs();
        ackCreateDirectory(isCreated);
    }

    private void onAckCreateDirectory(boolean isCreated) {
        if (isCreated) {
            /**
             * 文件夹创建成功
             */
            ((SendTask) sendTaskRef).nextFile();

        } else {
            /**
             * 创建失败
             */
            sendTaskRef.state = Task.FAILED;
        }
        handle();
    }

    private void onSynReceiveData(String srcPath, long srcOffset, long deservedCount) {
        receiveDataRunnable = new ReceiveDataRunnable(receiveTaskRef.path + srcPath, srcOffset, deservedCount);
        receiveThreadPool.execute(receiveDataRunnable);
    }

    /**
     * 在接收线程中调用
     *
     * @param size
     */
    private void onReceiveProgress(int size) {
        double preRate = receiveTaskRef.getRate();
        receiveTaskRef.offset += size;
        if (((int) preRate != (int) receiveTaskRef.getRate() || receiveTaskRef.getRate() == 100d)) {
            Manager.getManager().onTransferReceiveListChanged(Transfer.this, receiveTaskRef, ACTION_CHANGE);
        }
    }

    /**
     * 在发送线程中调用
     *
     * @param size
     */
    private void onSendProgress(int size) {
        double preRate = sendTaskRef.getRate();
        sendTaskRef.offset += size;
        if (((int) preRate != (int) sendTaskRef.getRate() || sendTaskRef.getRate() == 100d)) {
            Manager.getManager().onTransferSendListChanged(Transfer.this, sendTaskRef, ACTION_CHANGE);
        }
    }

    private void onSynDataResult(long offset, boolean isFailed) {
        if (isFailed) {
            sendTaskRef.state = Task.FAILED;
        } else {
            sendTaskRef.offset = offset;
            if (sendTaskRef.offset == ((SendTask) sendTaskRef).getFocusFile().length()) {
                ((SendTask) sendTaskRef).nextFile();
            }
        }
        handle();
    }

    private void onSynTaskResult(int state) {
        receiveTaskRef.state = state;
        Manager.getManager().onTransferReceiveListChanged(Transfer.this, receiveTaskRef, ACTION_CHANGE);
        receiveTaskRef = null;
        ackTaskResult();
    }

    private void synNewReceiveTask(int mID, String name, long totalCount) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", SYN_NEW_RECEIVE_TASK_TYPE);
            obj.put("mID", mID);
            obj.put("name", name);
            obj.put("totalCount", totalCount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMsg(obj.toString());
    }

    private void ackNewReceiveTask(int mID, boolean isCreated) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", ACK_NEW_RECEIVE_TASK_TYPE);
            obj.put("mID", mID);
            obj.put("isCreated", isCreated);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMsg(obj.toString());
    }

    private void synStartReceiveTask(int remoteID) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", SYN_START_RECEIVE_TASK_TYPE);
            obj.put("remoteID", remoteID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMsg(obj.toString());
    }

    private void ackStartReceiveTask(long offset, boolean isStarted) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", ACK_START_RECEIVE_TASK_TYPE);
            obj.put("offset", offset);
            obj.put("isStarted", isStarted);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMsg(obj.toString());
    }

    private void synCreateDirectory(String srcPath) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", SYN_CREATE_DIRECTORY_TYPE);
            obj.put("srcPath", srcPath);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMsg(obj.toString());
    }

    private void ackCreateDirectory(boolean isCreated) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", ACK_CREATE_DIRECTORY_TYPE);
            obj.put("isCreated", isCreated);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMsg(obj.toString());
    }

    private void synReceiveData(String srcPath, long srcOffset, long deservedCount) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", SYN_RECEIVE_DATA_TYPE);
            obj.put("srcPath", srcPath);
            obj.put("srcOffset", srcOffset);
            obj.put("deservedCount", deservedCount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMsg(obj.toString());
    }

    private void ackReceiveData() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", ACK_RECEIVE_DATA_TYPE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMsg(obj.toString());
    }

    private void synDataResult(long offset, boolean isFailed) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", SYN_DATA_RESULT_TYPE);
            obj.put("offset", offset);
            obj.put("isFailed", isFailed);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMsg(obj.toString());
    }

    private void synTaskResult(int state) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", SYN_TASK_RESULT_TYPE);
            obj.put("state", state);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMsg(obj.toString());
    }

    private void ackTaskResult() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", ACK_TASK_RESULT_TYPE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMsg(obj.toString());
    }

    private void synClose() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", SYN_CLOSE_TYPE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMsg(obj.toString());
    }

    private void ackClose() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", ACK_CLOSE_TYPE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMsg(obj.toString());
    }

    private void requestStopReceiveData() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", REQUEST_STOP_RECEIVE_DATA_TYPE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMsg(obj.toString());
    }

    private void requestStopSendTask() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", REQUEST_STOP_SEND_TASK_TYPE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMsg(obj.toString());
    }

    public static final int CREATED = 0;
    public static final int ACCEPTED = 1;
    private static final int SYN_NEW_RECEIVE_TASK_TYPE = 4;
    private static final int ACK_NEW_RECEIVE_TASK_TYPE = 5;
    private static final int SYN_START_RECEIVE_TASK_TYPE = 6;
    private static final int ACK_START_RECEIVE_TASK_TYPE = 7;
    private static final int SYN_CREATE_DIRECTORY_TYPE = 8;
    private static final int ACK_CREATE_DIRECTORY_TYPE = 9;
    private static final int SYN_RECEIVE_DATA_TYPE = 10;
    private static final int ACK_RECEIVE_DATA_TYPE = 11;
    private static final int SYN_DATA_RESULT_TYPE = 12;
    private static final int SYN_TASK_RESULT_TYPE = 13;
    private static final int ACK_TASK_RESULT_TYPE = 14;
    private static final int SYN_CLOSE_TYPE = 15;
    private static final int ACK_CLOSE_TYPE = 16;
    private static final int REQUEST_STOP_RECEIVE_DATA_TYPE = 17;
    private static final int REQUEST_STOP_SEND_TASK_TYPE = 18;
    private static int RECEIVE_BUFFER_SIZE = 8688;
    private static int SEND_BUFFER_SIZE = 7240;

    public static int ACTION_CLEAR = 0;
    public static int ACTION_ADD = 1;
    public static int ACTION_CHANGE = 2;
}
