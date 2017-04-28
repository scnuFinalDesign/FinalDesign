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

import com.bumptech.glide.Glide;
import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.bean.PhotoGroupBean;
import com.example.asus88.finaldesgin.util.ListUtil;
import com.example.asus88.finaldesgin.util.LogUtil;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.List;

/**
 * Created by asus88 on 2017/1/4.
 */

public class PhotoGroupAdapter extends RecyclerView.Adapter<PhotoGroupAdapter.MyViewHolder> {
    private static final String TAG = "PhotoGroupAdapter";
    private Context mContext;
    private List<PhotoGroupBean> list;
    private onItemClickListener mOnItemClickListener;

    public PhotoGroupAdapter(Context context, List<PhotoGroupBean> list) {
        mContext = context;
        this.list = list;
    }

    public void setOnItemClickListener(onItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final MyViewHolder holder = new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.adapter_photo_group, parent, false));
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(holder.getLayoutPosition());
                }
            });
        } else {
            LogUtil.logd(TAG, "please implements onItemClickListener");
        }
        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                holder.bean.setSelected(isChecked);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        PhotoGroupBean bean = list.get(position);
        holder.bean=bean;
        holder.path.setText(bean.getName());
        Glide.with(mContext).load(bean.getPhotoPath().get(0).getPath()).
                error(R.drawable.ic_menu_camera).thumbnail(0.1f).into(holder.image);
        holder.number.setText(String.valueOf(bean.getPhotoPath().size()));
        holder.mCheckBox.setChecked(bean.isSelected());
    }

    @Override
    public int getItemCount() {
        return ListUtil.getSize(list);
    }

    public interface onItemClickListener {
        void onItemClick(int position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView number;
        TextView path;
        CheckBox mCheckBox;
        PhotoGroupBean bean;

        public MyViewHolder(View itemView) {
            super(itemView);
            AutoUtils.autoSize(itemView);
            image = (ImageView) itemView.findViewById(R.id.photo_group_adapter_image);
            number = (TextView) itemView.findViewById(R.id.photo_group_adapter_number);
            path = (TextView) itemView.findViewById(R.id.photo_group_adapter_path);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.photo_group_adapter_checkbox);
        }
    }
}
