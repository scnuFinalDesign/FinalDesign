package com.example.asus88.finaldesgin.connection;

import android.os.Build;

import com.example.asus88.finaldesgin.util.Utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Dev {

    public static final int PC_TYPE = 0;
    public static final int MOBILE_TYPE = 1;
    public String name;
    public InetAddress ip;
    public String mac;
    public int type;
    private int transferState;  // 0 transfer null 1 transfer able 2 transfer enabled

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTransferState() {
        return transferState;
    }

    public void setTransferState(int transferState) {
        this.transferState = transferState;
    }

    public Dev() {

    }

    public Dev(String name, InetAddress ip, String mac, int type) {
        this.name = name;
        this.ip = ip;
        this.mac = mac;
        this.type = type;
    }

    public Dev(Dev dev) {
        this.name = dev.name;
        this.ip = dev.ip;
        this.mac = dev.mac;
        this.type = dev.type;
    }

    public static NetworkInterface getWLAN(){
        try {
            Enumeration<NetworkInterface> a = NetworkInterface.getNetworkInterfaces();
            while (a.hasMoreElements()) {
                NetworkInterface b = a.nextElement();
                if (b.getName().contains("wlan") && b.isUp()) {
                    return b;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getMac(NetworkInterface i) {
        try {
            if(i.getHardwareAddress() == null) {
                System.out.println("网卡"+i.getName()+"无法获取mac");
                i = getWLAN();
            }

            if(i != null)
            return i.getHardwareAddress();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InetAddress getIp(NetworkInterface i){
        Enumeration<InetAddress> c = i.getInetAddresses();
        while (c.hasMoreElements()) {
            InetAddress d = c.nextElement();
            if (d.isSiteLocalAddress()) {
                System.out.println("getIp使用的网卡"+i.getName()+"，"+d);
                return d;
            }
        }
        return null;
    }

    public static Dev getLocalDev() {
        NetworkInterface i = getLocalInterface();
        if (i == null)
            return null;

        Dev localDev = new Dev();
        localDev.type = MOBILE_TYPE;
        localDev.name = Build.MODEL;
        localDev.ip = getIp(i);
        byte[] macByte = getMac(i);
        localDev.mac = Utils.toHexString(macByte[0]) + ":" + Utils.toHexString(macByte[1]) + ":"
                + Utils.toHexString(macByte[2]) + ":" + Utils.toHexString(macByte[3]) + ":"
                + Utils.toHexString(macByte[4]) + ":" + Utils.toHexString(macByte[5]);
        return localDev;
    }

    public static NetworkInterface getLocalInterface() {
        try {
            Enumeration<NetworkInterface> a = NetworkInterface.getNetworkInterfaces();
            while (a.hasMoreElements()) {
                NetworkInterface b = a.nextElement();
                if ((b.getName().contains("rmnet") || b.getName().contains("ap") || b.getName().contains("wlan") || b.getName().contains("lo")) && b.isUp()) {
                    Enumeration<InetAddress> e = b.getInetAddresses();
                    while (e.hasMoreElements()) {
                        InetAddress ip = e.nextElement();
                        if(ip.isSiteLocalAddress()){
                            System.out.println("getLocalInterface获取的网卡"+b.getName());
                            return b;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o) {
            return false;
        }
        if (!(o instanceof Dev)) {
            return false;
        }

        if (mac.equals(((Dev) o).mac))
            return true;
        else
            return false;
    }

    @Override
    public int hashCode() {
        return mac.hashCode();
    }

}
