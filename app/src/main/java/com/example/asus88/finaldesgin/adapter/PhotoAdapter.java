package com.example.asus88.finaldesgin.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.bean.PhotoBean;
import com.example.asus88.finaldesgin.util.ListUtil;
import com.example.asus88.finaldesgin.util.LogUtil;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.List;

/**
 * Created by asus88 on 2017/2/6.
 */

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyViewHolder> {

    private static final String TAG = "PhotoAdapter";

    private Context mContext;
    private List<PhotoBean> list;
    private onItemClickListener mOnItemClickListener;

    public PhotoAdapter(Context context, List<PhotoBean> list) {
        mContext = context;
        this.list = list;
    }

    public void setOnItemClickListener(onItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final MyViewHolder holder=new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.adapter_photo, parent, false));
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(holder.image,holder.getLayoutPosition());
                }
            });
        } else {
            LogUtil.logd(TAG, "please implements onItemClickListener");
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final PhotoBean bean = list.get(position);
        holder.bean=bean;
        Glide.with(mContext).load(bean.getPath()).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return ListUtil.getSize(list);
    }

    public interface onItemClickListener {
        void onItemClick(View view,int position);
    }

  public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        CheckBox isSelected;
        PhotoBean bean;

        public MyViewHolder(View itemView) {
            super(itemView);
            AutoUtils.autoSize(itemView);
            image = (ImageView) itemView.findViewById(R.id.photo_adapter_image);
            isSelected = (CheckBox) itemView.findViewById(R.id.photo_adapter_checkBox);
            isSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        bean.setSelected(1);
                    } else {
                        bean.setSelected(0);
                    }

                }
            });
        }
    }
}
