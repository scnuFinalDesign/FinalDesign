package com.example.asus88.finaldesgin.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.connection.Dev;
import com.example.asus88.finaldesgin.util.ListUtil;
import com.example.asus88.finaldesgin.util.LogUtil;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.List;

/**
 * Created by asus88 on 2017/2/11.
 */

public class LinkAdapter extends RecyclerView.Adapter<LinkAdapter.MyViewHolder> {

    private static final String TAG = "LinkAdapter";
    private Context mContext;
    private List<Dev> list;
    private onItemClickListener mOnItemClickListener;

    public LinkAdapter(Context context, List<Dev> list) {
        mContext = context;
        this.list = list;
    }

    public void setOnItemClickListener(onItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final MyViewHolder holder = new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.adapter_link, parent, false));
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(holder.getLayoutPosition());
                }
            });
        } else {
            LogUtil.logd(TAG, "please implements the onItemClickListener");
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Dev dev = list.get(position);
        holder.name.setText(dev.getName());
        int status = dev.getTransferState();
        if (status == 1) {
            holder.isLink.setVisibility(View.VISIBLE);
            holder.isLink.setText("已连接");
        } else {
            holder.isLink.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return ListUtil.getSize(list);
    }

    public interface onItemClickListener {
        void onItemClick(int position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView isLink;

        public MyViewHolder(View itemView) {
            super(itemView);
            AutoUtils.autoSize(itemView);
            name = (TextView) itemView.findViewById(R.id.link_adapter_name);
            isLink = (TextView) itemView.findViewById(R.id.link_adapter_isLink);
        }
    }
}
