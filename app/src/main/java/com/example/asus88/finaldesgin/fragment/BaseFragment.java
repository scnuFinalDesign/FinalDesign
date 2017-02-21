package com.example.asus88.finaldesgin.fragment;

import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.bean.Bean;
import com.example.asus88.finaldesgin.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus88 on 2016/12/21.
 */

public abstract class BaseFragment extends Fragment {

    private int selectedNum = 0;

    public int getSelectedNum() {
        return selectedNum;
    }

    public void selectedNumChange(boolean flag, int max) {
        if (flag) {
            selectedNum++;
            if (selectedNum > max) {
                selectedNum = max;
            }
        } else {
            selectedNum--;
            if (selectedNum < 0) {
                selectedNum = 0;
            }
        }
    }


    public void showFileInfo(Bean bean, View view) {
        View window = LayoutInflater.from(view.getContext()).inflate(R.layout.popup_window_show_file_info, null);
        TextView name = (TextView) window.findViewById(R.id.popup_name);
        TextView type = (TextView) window.findViewById(R.id.popup_type);
        TextView location = (TextView) window.findViewById(R.id.popup_location);
        TextView modify = (TextView) window.findViewById(R.id.popup_modify);
        TextView sure = (TextView) window.findViewById(R.id.popup_sure);

        name.setText("标题:   " + bean.getName());
        type.setText("类别:   " + FileUtil.getFileType(bean.getPath()));
        location.setText("位置:   " + bean.getPath());
        modify.setText("修改:   " + bean.getModify());

        final PopupWindow popupWindow = new PopupWindow(window, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x000000));
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    public List getSelectedList(List<Bean> list) {
        List<Bean> mList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isSelected()) {
                mList.add(list.get(i));
            }
        }
        return mList;
    }

    public abstract List getDataList();
    public abstract int getFabButtonNum();
    public abstract void notifyRecyclerView();
}
