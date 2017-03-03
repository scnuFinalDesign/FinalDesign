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
import com.example.asus88.finaldesgin.bean.MusicBean;
import com.example.asus88.finaldesgin.util.ListUtil;
import com.example.asus88.finaldesgin.util.LogUtil;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.List;

/**
 * Created by asus88 on 2016/12/29.
 */

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {
    private static final String TAG = "MusicAdapter";
    private Context mContext;
    private List<MusicBean> list;
    private onItemClickListener mOnItemClickListener;

    public MusicAdapter(Context context, List<MusicBean> list) {
        mContext = context;
        this.list = list;
    }

    public void setOnItemClickListener(onItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final MyViewHolder holder = new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.adapter_music, parent, false));

        if (mOnItemClickListener != null) {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(holder.getLayoutPosition());
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onItemLongClick(holder.getLayoutPosition());
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
        final MusicBean bean = list.get(position);
        holder.bean = bean;
        holder.name.setText(bean.getName());
        holder.time.setText(bean.getTimeAndSize());
        holder.singer.setText(bean.getSinger());
        holder.selected.setChecked(bean.isSelected());

    }

    @Override
    public int getItemCount() {
        return ListUtil.getSize(list);
    }

    public interface onItemClickListener {
        void onItemClick(int position);

        void onItemLongClick(int position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView time;
        TextView singer;
        ImageView icon;
        CheckBox selected;
        MusicBean bean;

        public MyViewHolder(View view) {
            super(view);
            AutoUtils.autoSize(view);
            name = (TextView) view.findViewById(R.id.music_adapter_name);
            time = (TextView) view.findViewById(R.id.music_adapter_time);
            singer = (TextView) view.findViewById(R.id.music_adapter_author);
            icon = (ImageView) view.findViewById(R.id.music_adapter_image);
            selected = (CheckBox) view.findViewById(R.id.music_adapter_checkBox);
            selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    bean.setSelected(isChecked);
                }
            });
        }
    }
}
