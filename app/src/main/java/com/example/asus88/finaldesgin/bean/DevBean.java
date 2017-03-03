package com.example.asus88.finaldesgin.bean;

import com.example.asus88.finaldesgin.connection.Transfer;

/**
 * Created by asus88 on 2017/2/25.
 */

public class DevBean {
    private String name;
    private Transfer transfer;
    private boolean isSelected;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Transfer getTransfer() {
        return transfer;
    }

    public void setTransfer(Transfer transfer) {
        this.transfer = transfer;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
