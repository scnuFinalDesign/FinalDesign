package com.example.asus88.finaldesgin.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.bean.ApplicationBean;
import com.example.asus88.finaldesgin.myViews.AutoCardView;
import com.example.asus88.finaldesgin.myViews.MarqueeTextView;
import com.example.asus88.finaldesgin.util.ListUtil;
import com.example.asus88.finaldesgin.util.LogUtil;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.List;

/**
 * Created by asus88 on 2017/1/9.
 */

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.MyViewHolder> {
    private static final String TAG = "ApplicationAdapter";
    private Context mContext;
    private List<ApplicationBean> list;
    private onItemClickListener mOnItemClickListener;
    private boolean deleteMode;

    public ApplicationAdapter(Context context, List<ApplicationBean> list) {
        mContext = context;
        this.list = list;
        deleteMode = false;
    }

    public void setDeleteMode(boolean deleteMode) {
        this.deleteMode = deleteMode;
        notifyDataSetChanged();
    }

    public boolean getDeleteMode() {
        return deleteMode;
    }

    public void setOnItemClickListener(onItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final MyViewHolder holder = new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.adapter_application, parent, false));
        if (mOnItemClickListener != null) {
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!deleteMode) {
                        mOnItemClickListener.onDeleteClick(holder.getLayoutPosition());
                    }
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!deleteMode) {
                        deleteMode = true;
                        notifyDataSetChanged();
                        mOnItemClickListener.onLongClick();
                    }
                    return true;
                }
            });

        } else {
            LogUtil.logd(TAG, "please implements onItemClickListener");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.bean.setSelected(!holder.bean.isSelected());
                notifyItemChanged(holder.getLayoutPosition());
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final ApplicationBean bean = list.get(position);
        holder.bean = bean;
        holder.number.setText("版本:" + bean.getNumber());
        holder.name.setText(bean.getName());
        holder.size.setText(bean.getSize());
        holder.icon.setImageDrawable(bean.getIcon());
        final Boolean isSelected = bean.isSelected();
        if (isSelected) {
            holder.selected.setVisibility(View.VISIBLE);
            holder.cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.gray));
        } else {
            holder.cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.white));
            holder.selected.setVisibility(View.GONE);
        }

        if (deleteMode) {
            holder.delete.setVisibility(View.VISIBLE);
        } else {
            holder.delete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return ListUtil.getSize(list);
    }

    public interface onItemClickListener {

        void onDeleteClick(int position);

        void onLongClick();

    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        ImageView selected;
        ImageView delete;
        MarqueeTextView name;
        TextView size;
        TextView number;
        AutoCardView cardView;
        ApplicationBean bean;

        public MyViewHolder(View view) {
            super(view);
            AutoUtils.autoSize(view);
            icon = (ImageView) view.findViewById(R.id.application_adapter_image);
            selected = (ImageView) view.findViewById(R.id.application_adapter_selected);
            delete = (ImageView) view.findViewById(R.id.application_adapter_delete);
            name = (MarqueeTextView) view.findViewById(R.id.application_adapter_name);
            size = (TextView) view.findViewById(R.id.application_adapter_size);
            number = (TextView) view.findViewById(R.id.application_adapter_number);
            cardView = (AutoCardView) view.findViewById(R.id.application_adapter_cardView);
        }
    }
}
