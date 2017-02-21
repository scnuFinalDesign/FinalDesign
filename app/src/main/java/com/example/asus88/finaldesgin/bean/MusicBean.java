package com.example.asus88.finaldesgin.bean;

/**
 * Created by asus88 on 2016/12/22.
 */

public class MusicBean extends Bean {
    private String duration;
    private String singer;

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTimeAndSize(){
        StringBuilder builder = new StringBuilder(this.duration);
        builder.append("\n");
        builder.append(getSize());
        return builder.toString();
    }
}
