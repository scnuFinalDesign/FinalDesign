package com.example.asus88.finaldesgin.connection;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

public class SendTask extends Task {
    /**
     * 用于发送的列表
     */
    private File[] files;
    private int focus;

    private SendTask(String name, String path, File[] files, long totalCount) {
        super(SEND_TASK_TYPE, name, path, totalCount);
        this.files = files;
        this.focus = 0;
    }

    public static SendTask createSendTask(String path) throws Exception {
        File file = new File(path);
        if (!file.exists()) throw new Exception("路径不存在");
        long totalCount = 0;
        Queue<File> fileQueue = new LinkedList<>();
        LinkedList<File> children = new LinkedList<>();
        fileQueue.offer(file);
        while (!fileQueue.isEmpty()) {
            File temp = fileQueue.poll();

            if (temp.isFile()) totalCount += temp.length();
            children.add(temp);
            File[] files = temp.listFiles();
            if (files != null) {
                for (File f : files) {
                    fileQueue.offer(f);
                }
            }
        }
        File[] arr = new File[children.size()];
        children.toArray(arr);
        return new SendTask(file.getName(), file.getPath(), arr, totalCount);
    }

    public File getFocusFile() {
        if (focus >= files.length) return null;
        return files[focus];
    }

    public void nextFile() {
        transferedCount += offset;
        offset = 0;
        focus++;
        if (focus >= files.length) state = OVER;
    }
}
