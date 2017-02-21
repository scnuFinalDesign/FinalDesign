package com.example.asus88.finaldesgin.util;

import com.example.asus88.finaldesgin.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by asus88 on 2017/1/9.
 */

public class FileUtil {
    private static final String TAG = "FileUtil";

    public static String getFileType(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();
        int dot = fileName.lastIndexOf(".");
        if (dot > 0) {
            String type = fileName.substring(dot + 1);
            type = type.toLowerCase();
            switch (type) {
                case "mp4":
                case "3gp":
                case "avi":
                case "mov":
                case "wmv":
                case "mkv":
                case "flv":
                case "rmvb":
                    return "视频";
                case "mp3":
                case "wav":
                case "ogg":
                case "ape":
                case "aac":
                case "cda":
                case "wma":
                case "ra":
                case "rma":
                case "midi":
                case "flac":
                    return "音频";
                case "jpg":
                case "gif":
                case "jpeg":
                case "bmp":
                case "png":
                    return "图片";
                default:
                    return "文件";
            }
        }
        return "文件夹";
    }

    public static int getImageId(String type) {
        switch (type) {
            case "directory":
                return R.mipmap.ic_folder_white;
            case "jpg":
            case "gif":
            case "jpeg":
            case "bmp":
            case "png":
                return R.mipmap.ic_photo_white;
            case "mp3":
            case "wav":
            case "ogg":
            case "ape":
            case "aac":
            case "cda":
            case "wma":
            case "ra":
            case "rma":
            case "midi":
            case "flac":
                return R.mipmap.ic_music_white;
            case "mp4":
            case "3gp":
            case "avi":
            case "mov":
            case "wmv":
            case "mkv":
            case "flv":
            case "rmvb":
                return R.mipmap.ic_movie_white;
            case "txt":
            case "word":
            case "pdf":
            case "ppt":
            case "excel":
                return R.mipmap.ic_file_white;
            case "zip":
            case "rar":
                return R.mipmap.ic_zip_white;
            default:
                return R.mipmap.ic_unknown_white;
        }
    }

    public static String getFileSuffix(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();
        int dot = fileName.lastIndexOf(".");
        if (dot > 0) {
            String type = fileName.substring(dot + 1);
            type = type.toLowerCase();
            return type;
        }
        return "unknown";
    }

    public static String getFileSize(String path) {
        File file = new File(path);
        if (!file.exists())
            return "0 B";
        DecimalFormat df = new DecimalFormat("#.00");
        String sizeStr = "";
        long size = 0;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
            size = fis.available();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
     * @param fileName
     * @param filePath
     * @return
     */
    public static boolean newDirectory(String fileName, String filePath) {
        File file = new File(filePath, fileName);
        if (file.exists()) {
            return false;
        }
        return file.mkdirs();
    }

    /**
     * @param fileName
     * @param filePath
     * @param suffix   后缀
     * @return
     */
    public static boolean newFile(String fileName, String filePath, String suffix) {
        Boolean flag = false;
        File file = new File(filePath, fileName + "." + suffix);
        if (file.exists()) {
            return false;
        }
        try {
            flag = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * @param file
     * @return
     */
    public static boolean deleteFile(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] fileList = file.listFiles();
                if (fileList == null || fileList.length == 0) {
                    return file.delete();
                }
                for (int i = 0; i < fileList.length; i++) {
                    deleteFile(fileList[i]);
                }
                return file.delete();
            } else {
                return file.delete();
            }
        }
        return false;
    }
}
