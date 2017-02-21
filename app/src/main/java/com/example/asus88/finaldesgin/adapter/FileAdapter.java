package com.example.asus88.finaldesgin.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.bean.FileBean;
import com.example.asus88.finaldesgin.myViews.MarqueeTextView;
import com.example.asus88.finaldesgin.util.FileUtil;
import com.example.asus88.finaldesgin.util.ListUtil;
import com.example.asus88.finaldesgin.util.LogUtil;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.List;

/**
 * Created by asus88 on 2017/1/14.
 */

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.MyViewHolder> {

    private static final String TAG = "FileAdapter";
    private List<FileBean> list;
    private Context mContext;
    private onItemClickListener mOnItemClickListener;

    public FileAdapter(Context context, List<FileBean> list) {
        mContext = context;
        this.list = list;
    }


    public void setOnItemClickListener(onItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final MyViewHolder holder = new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.adapter_file, parent, false));
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(holder.itemView, holder.getLayoutPosition());
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onItemLongClick(holder.itemView, holder.getLayoutPosition());
                    return true;
                }
            });
        } else {
            LogUtil.logd(TAG, "please implements the onItemClickListener");
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final FileBean bean = list.get(position);
        holder.bean = bean;
        holder.name.setText(bean.getName());
        if (bean.isDirectory()) {
            holder.size.setText("目录");
        } else {
            holder.size.setText(bean.getSize());
        }
        holder.icon.setImageResource(FileUtil.getImageId(bean.getType()));
        holder.modify.setText(bean.getModify());
        holder.selected.setChecked(bean.isSelected());
    }

    @Override
    public int getItemCount() {
        return ListUtil.getSize(list);
    }

    public interface onItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        MarqueeTextView name;
        TextView size;
        TextView modify;
        CheckBox selected;
        FileBean bean;


        public MyViewHolder(View itemView) {
            super(itemView);
            AutoUtils.autoSize(itemView);
            icon = (ImageView) itemView.findViewById(R.id.file_adapter_image);
            name = (MarqueeTextView) itemView.findViewById(R.id.file_adapter_name);
            size = (TextView) itemView.findViewById(R.id.file_adapter_size);
            modify = (TextView) itemView.findViewById(R.id.file_adapter_modify);
            selected = (CheckBox) itemView.findViewById(R.id.file_adapter_checkBox);
            selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    bean.setSelected(isChecked);
                }
            });
        }
    }
}
