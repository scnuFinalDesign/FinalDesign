package com.example.asus88.finaldesgin.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
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
import com.example.asus88.finaldesgin.bean.VideoBean;
import com.example.asus88.finaldesgin.util.BitmapUtil;
import com.example.asus88.finaldesgin.util.ListUtil;
import com.example.asus88.finaldesgin.util.LogUtil;
import com.zhy.autolayout.utils.AutoUtils;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by asus88 on 2017/1/4.
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyViewHolder> {
    private static final String TAG = "VideoAdapter";
    private Context mContext;
    private List<VideoBean> list;
    private onItemClickListener mOnItemClickListener;

    public VideoAdapter(Context context, List<VideoBean> list) {
        mContext = context;
        this.list = list;
    }

    public void setOnItemClickListener(onItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final MyViewHolder holder = new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.adapter_video, parent, false));
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
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final VideoBean bean = list.get(position);
        holder.bean = bean;
        holder.name.setText(bean.getName());
        holder.size.setText(bean.getSize());
        holder.duration.setText(bean.getDuration());
        if (bean.getBitmap() == null) {
            AsyncByteTask task = new AsyncByteTask(holder, mContext);
            task.execute(bean.getPath());
        } else {
            Glide.with(mContext).load(bean.getBitmap()).thumbnail(0.1f).into(holder.image);
        }
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

        private TextView name;
        private TextView duration;
        private TextView size;
        private ImageView image;
        private CheckBox selected;
        VideoBean bean;

        public MyViewHolder(View view) {
            super(view);
            AutoUtils.autoSize(view);
            name = (TextView) view.findViewById(R.id.video_adapter_name);
            duration = (TextView) view.findViewById(R.id.video_adapter_duration);
            size = (TextView) view.findViewById(R.id.video_adapter_size);
            image = (ImageView) view.findViewById(R.id.video_adapter_image);
            selected = (CheckBox) view.findViewById(R.id.video_adapter_checkbox);
            selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    bean.setSelected(isChecked);
                }
            });
        }
    }

    class AsyncByteTask extends AsyncTask<String, Void, byte[]> {
        private MyViewHolder mHolder;
        private WeakReference<Context> context;
        private String path;

        public AsyncByteTask(MyViewHolder holder, Context context) {
            mHolder = holder;
            this.context = new WeakReference<Context>(context);
        }

        @Override
        protected byte[] doInBackground(String... params) {
            path = params[0];
            Bitmap bitmap = BitmapUtil.getVideoThumbnail(path, context.get().getResources());
            return BitmapUtil.bitmapToByteArray(bitmap);
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            if (mHolder != null && bytes != null) {
                mHolder.bean.setBitmap(bytes);
                Glide.with(context.get()).load(bytes).thumbnail(0.1f).into(mHolder.image);
            }
        }
    }
}
