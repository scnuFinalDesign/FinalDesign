package com.example.asus88.finaldesgin.fragment;

import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.bean.Bean;
import com.example.asus88.finaldesgin.bean.DevBean;
import com.example.asus88.finaldesgin.connection.Transfer;
import com.example.asus88.finaldesgin.util.FileUtil;
import com.example.asus88.finaldesgin.util.ListUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus88 on 2016/12/21.
 */

public abstract class BaseFragment extends Fragment {
    private static final String TAG = "BaseFragment";

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
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(1f);
            }
        });
        setBackgroundAlpha(0.5f);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                setBackgroundAlpha(1f);
            }
        });
    }

    private void setBackgroundAlpha(float alpha) {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = alpha;
        getActivity().getWindow().setAttributes(lp);
    }

    public void deleteFile() {
        List<Bean> sList = getSelectedList(getDataList());
        if (sList == null || sList.size() <= 0) {
            Toast.makeText(getActivity(), getString(R.string.no_selected), Toast.LENGTH_SHORT).show();
        } else {
            for (Bean bean : sList) {
                FileUtil.deleteFile(new File(bean.getPath()));
            }
            updateMediaDataBase(sList);
            notifyRecyclerView(sList);
        }
    }

    public void sendFile(List<DevBean> devList) {
        List<Bean> sList = getSelectedList(getDataList());
        for (DevBean bean : devList) {
            if (bean.isSelected()) {
                Transfer transfer = bean.getTransfer();
                for (Bean fileBean : sList) {
                    try {
                        transfer.addTask(fileBean.getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        setAllUnSelected();
    }

    private List getSelectedList(List<Bean> list) {
        List<Bean> mList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isSelected()) {
                mList.add(list.get(i));
            }
        }
        return mList;
    }
    public  void notifyRecyclerView(List<Bean> list){}

    public  void updateMediaDataBase(List<Bean> list){}

    public int getSelectedNum() {
        return ListUtil.getSize(getSelectedList(getDataList()));
    }

    public abstract List getDataList();

    public abstract int getFabButtonNum();

    public abstract void setAllUnSelected();
}
