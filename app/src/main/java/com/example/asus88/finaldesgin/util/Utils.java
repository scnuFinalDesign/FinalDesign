package com.example.asus88.finaldesgin.util;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;

public class Utils {
	static public String toHexString(byte b) {
		String s = "0" + Integer.toHexString(b);
		return s.substring(s.length() - 2);
	}

	private static final String STORE_PATH = "quickpass_store\\";

	/**
	 * 文件大小转换
	 *
	 * @param size
	 * @return
	 */
	public static String changeSize(long size) {
		DecimalFormat df = new DecimalFormat("#.00");
		String sizeStr = "";
		if (size < 1024) {
			sizeStr = size + "B";
		} else if (size < 1048576) {
			sizeStr = df.format(size / (double) 1024) + "KB";
		} else if (size < 1073741824) {
			sizeStr = df.format(size / (double) 1048576) + "MB";
		} else {
			sizeStr = df.format(size / (double) 1073741824) + "GB";
		}
		return sizeStr;
	}

	/**
	 * 获取本机ip地址
	 * 
	 * @return
	 */
	public static String getLocalHostIp() {
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			String ip = addr.getHostAddress();
			if (ip != null) {
				return ip;
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取默认存储位置
	 * 
	 * @return
	 */
	public static String getDefaultStorePath() {
		String path[] = new String[]{"D:\\","E:\\","F:\\","G:\\"};
		String filePath = "";
		File[] file = File.listRoots();
		for (File f : file) {
			for(String p:path){
				if(p.equals(f.getAbsolutePath())){
					filePath = p;
					break;
				}
				if(!"".equals(filePath))
					break;
			}
		}
		filePath += STORE_PATH;
		File folder= new File(filePath);
		if (!folder.exists())
			folder.mkdirs();
		return filePath;
	}
	
	public static String getFileNameWithoutSuffix(String name) {
        return name.substring(0, name.lastIndexOf("."));
    }
	
	/**
     * 返回文件类型
     *
     * @param filename
     * @return
     */
    public static String getFileType(String filename) {
        if (filename != null && filename.length() > 3) {
            int dot = filename.lastIndexOf(".");
            if (dot > 0)
                return filename.substring(dot + 1);
            else return "weizhi";
        }
        return "weizhi";
    }
}
