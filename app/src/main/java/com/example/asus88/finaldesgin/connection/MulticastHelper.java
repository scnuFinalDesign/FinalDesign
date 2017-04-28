package com.example.asus88.finaldesgin.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public abstract class MulticastHelper {
    private static final String TAG = "MulticastHelper";
    public static final String GROUP_IP = "230.0.0.1";
    protected static final int PACKET_LEN = 1024;
    protected static final int INTERVAL = 1000;

    protected volatile int port;
    private volatile MulticastSocket socket;

    private volatile SendThread sendThread;
    private volatile ReceiveThread receiveThread;

    private volatile boolean isStop = false;

    public MulticastHelper(int port) {
        this.port = port;
    }

    // 初始化套接字
    private synchronized void initSocket() throws IOException {
        if (socket != null && !socket.isClosed())
            return;

        socket = new MulticastSocket(port);
        NetworkInterface i = Dev.getLocalInterface();
        socket.setNetworkInterface(i);
        socket.joinGroup(new InetSocketAddress(GROUP_IP,port),i);
//        socket.joinGroup(InetAddress.getByName("230.0.0.1"));

    }

    // 关闭套接字
    private void closeSocket() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public boolean isRunning() {
        return (sendThread != null || receiveThread != null);
    }

    public boolean start() {
        if (isRunning())
            return false;

        sendThread = new SendThread();
        receiveThread = new ReceiveThread();
        sendThread.start();
        receiveThread.start();
        isStop = false;
        return true;
    }

    public void stop() {
        if (sendThread != null){
            sendThread.quit();
        }

        isStop = true;
    }

    private class SendThread extends Thread {
        private volatile boolean isQuit;

        public void quit() {
            isQuit = true;
        }

        public void run() {
            try {
                initSocket();
                // 循环发送部分
                do {
                    socket.send(getBroadcastPacket());
                    try {
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (!isQuit);

                // 单次发送部分
                socket.send(getQuitPacket());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sendThread = null;
                closeSocket();
            }
        }
    }

    private class ReceiveThread extends Thread {
        private volatile DatagramPacket packet = new DatagramPacket(new byte[PACKET_LEN], PACKET_LEN);

        public void run() {
            try {
                initSocket();
                do {
                    socket.receive(packet);
                    onReceivePacket(packet);
                } while (true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                receiveThread = null;
                onClosed();
            }
        }
    }

    protected abstract void onClosed();

    protected abstract void onReceivePacket(DatagramPacket packet);

    protected abstract DatagramPacket getBroadcastPacket() throws Exception;

    protected abstract DatagramPacket getQuitPacket() throws Exception;
}