package com.example.asus88.finaldesgin.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.asus88.finaldesgin.BitmapTask;
import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.connection.Manager;
import com.example.asus88.finaldesgin.connection.Task;
import com.example.asus88.finaldesgin.myViews.DigitalProgressBar;
import com.example.asus88.finaldesgin.util.FileUtil;
import com.example.asus88.finaldesgin.util.ListUtil;
import com.example.asus88.finaldesgin.util.Utils;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.List;

/**
 * Created by asus88 on 2017/2/28.
 */

public class ReceiveTaskAdapter extends RecyclerView.Adapter<ReceiveTaskAdapter.TaskViewHolder> {
    private static final String TAG = "ReceiveTaskAdapter";

    private Context mContext;
    private List<Task> list;
    private onItemStateClickListener mOnItemStateClickListener;
    private String savePath;
    private ArrayMap<String, Bitmap> arrayMap;

    public ReceiveTaskAdapter(Context context, List<Task> list) {
        mContext = context;
        this.list = list;
       arrayMap = new ArrayMap<>();
        try {
            savePath = Manager.getManager().getStorePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final TaskViewHolder holder = new TaskViewHolder(LayoutInflater.from(mContext).inflate(R.layout.adapter_send_receive_task
                , parent, false));
        holder.status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemStateClickListener != null) {
                    mOnItemStateClickListener.onItemStateChangeListener(holder.getLayoutPosition());
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        Task task = list.get(position);
        holder.bean = task;
        holder.fileName.setText(task.name);
        holder.progressBar.setProgress((int) task.getRate());

        String t = FileUtil.getFileType(task.path);
        if (t.equals("视频")) {
            if (task.getRate() > 20) {
                if (arrayMap.get(task.path) == null) {
                    BitmapTask bTask = new BitmapTask(mContext, holder.icon, null);
                    bTask.execute(task.path);
                }
            } else {
                holder.icon.setImageResource(R.mipmap.ic_movie_white);
            }
        } else  {
            holder.icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            holder.icon.setImageResource(FileUtil.getImageId(FileUtil.getFileSuffix(task.path)));
        }

        holder.status.setImageResource(task.getStateIconId());
        if (task.getRate() == 100) {
            Utils.scanFileToUpdate(mContext, new String[]{savePath});
        }
    }

    @Override
    public int getItemCount() {
        return ListUtil.getSize(list);
    }

    public void setOnItemStateClickListener(onItemStateClickListener onItemStateClickListener) {
        mOnItemStateClickListener = onItemStateClickListener;
    }

    public interface onItemStateClickListener {
        void onItemStateChangeListener(int position);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
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
