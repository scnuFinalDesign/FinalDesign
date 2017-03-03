package com.example.asus88.finaldesgin.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.bean.DevBean;
import com.example.asus88.finaldesgin.util.ListUtil;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.List;

/**
 * Created by asus88 on 2017/2/25.
 */

public class SelectDevAdapter extends RecyclerView.Adapter<SelectDevAdapter.MyViewHolder> {
    private static final String TAG = "SelectDevAdapter";

    private Context mContext;
    private List<DevBean> list;

    public SelectDevAdapter(Context context, List<DevBean> list) {
        mContext = context;
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final MyViewHolder viewHolder = new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.adapter_select_dev, parent, false));
        viewHolder.isSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged: " + viewHolder.bean.getName());
                viewHolder.bean.setSelected(isChecked);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        DevBean dev = list.get(position);
        holder.bean = dev;
        holder.name.setText(dev.getName());
        holder.isSelected.setChecked(dev.isSelected());
        Log.d(TAG, "onBindViewHolder: "+dev.isSelected());
    }

    @Override
    public int getItemCount() {
        return ListUtil.getSize(list);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private CheckBox isSelected;
        private DevBean bean;

        public MyViewHolder(View itemView) {
            super(itemView);
            AutoUtils.autoSize(itemView);
            name = (TextView) itemView.findViewById(R.id.select_dev_adapter_name);
            isSelected = (CheckBox) itemView.findViewById(R.id.select_dev_adapter_selected);
        }
    }
}
