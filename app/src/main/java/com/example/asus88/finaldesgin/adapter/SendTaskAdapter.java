package com.example.asus88.finaldesgin.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.asus88.finaldesgin.BitmapTask;
import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.bean.ReceiverBean;
import com.example.asus88.finaldesgin.bean.SendTakBean;
import com.example.asus88.finaldesgin.connection.Task;
import com.example.asus88.finaldesgin.myViews.DigitalProgressBar;
import com.example.asus88.finaldesgin.util.FileUtil;
import com.example.asus88.finaldesgin.util.ListUtil;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.List;

/**
 * Created by asus88 on 2017/2/27.
 */

public class SendTaskAdapter<T extends SendTakBean> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "SendTaskAdapter";
    private Context mContext;
    private List<T> mList;

    private onReceiverItemClickListener mOnReceiverItemClickListener;

    public SendTaskAdapter(Context context, List<T> list) {
        mContext = context;
        mList = list;
    }

    public void setOnReceiverItemClickListener(onReceiverItemClickListener onReceiverItemClickListener) {
        mOnReceiverItemClickListener = onReceiverItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == R.layout.adapter_receiver) {
            final ReceiverViewHolder holder = new ReceiverViewHolder(LayoutInflater.from(mContext).inflate(viewType,
                    parent, false));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnReceiverItemClickListener != null) {
                        mOnReceiverItemClickListener.onReceiverItemClick(holder.getLayoutPosition());
                    }
                }
            });
            return holder;
        } else {
            final TaskViewHolder holder = new TaskViewHolder(LayoutInflater.from(mContext).inflate(viewType,
                    parent, false));
            holder.status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnReceiverItemClickListener != null) {
                        mOnReceiverItemClickListener.onTaskStateChange(holder.getLayoutPosition());
                    }
                }
            });
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ReceiverViewHolder) {
            ((ReceiverViewHolder) holder).bean = (ReceiverBean) mList.get(position);
            ((ReceiverViewHolder) holder).name.setText(((ReceiverBean) mList.get(position)).getName());
            if (((ReceiverViewHolder) holder).bean.isExpand()) {
                ((ReceiverViewHolder) holder).isExpand.setImageResource(R.mipmap.icon_up);
            } else {
                ((ReceiverViewHolder) holder).isExpand.setImageResource(R.mipmap.icon_down);
            }
        } else if (holder instanceof TaskViewHolder) {
            Task bean = (Task) mList.get(position);
            ((TaskViewHolder) holder).bean = bean;
            ((TaskViewHolder) holder).fileName.setText(bean.name);
            ((TaskViewHolder) holder).progressBar.setProgress((int) bean.getRate());
            ((TaskViewHolder) holder).status.setImageResource(bean.getStateIconId());
            String t = FileUtil.getFileType(bean.path);
            if (t.equals("视频")) {
                ((TaskViewHolder) holder).icon.setImageResource(R.mipmap.ic_movie_white);
                BitmapTask bTask = new BitmapTask(mContext, ((TaskViewHolder) holder).icon);
                bTask.execute(bean.path);
            } else if (t.equals("图片")) {
                ((TaskViewHolder) holder).icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(mContext).load(bean.path).placeholder(R.mipmap.ic_photo_white).thumbnail(0.1f).into(((TaskViewHolder) holder).icon);
            } else {
                ((TaskViewHolder) holder).icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ((TaskViewHolder) holder).icon.setImageResource(FileUtil.getImageId(FileUtil.getFileSuffix(bean.path)));

            }
        }
    }

    @Override
    public int getItemCount() {
        return ListUtil.getSize(mList);
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).getLayoutId();
    }

    public interface onReceiverItemClickListener {
        void onReceiverItemClick(int position);

        void onTaskStateChange(int position);
    }

    private static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView isExpand;
        ReceiverBean bean;

        public ReceiverViewHolder(View itemView) {
            super(itemView);
            AutoUtils.autoSize(itemView);
            name = (TextView) itemView.findViewById(R.id.receiver_adapter_name);
            isExpand = (ImageView) itemView.findViewById(R.id.receiver_adapter_image);
        }
    }

    private static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        ImageView icon;
        ImageView status;
        DigitalProgressBar progressBar;
        Task bean;

        public TaskViewHolder(View itemView) {
            super(itemView);
            AutoUtils.autoSize(itemView);
            fileName = (TextView) itemView.findViewById(R.id.task_adapter_name);
            icon = (ImageView) itemView.findViewById(R.id.task_adapter_icon);
            status = (ImageView) itemView.findViewById(R.id.task_adapter_status);
            progressBar = (DigitalProgressBar) itemView.findViewById(R.id.task_adapter_progressBar);
        }
    }
}
