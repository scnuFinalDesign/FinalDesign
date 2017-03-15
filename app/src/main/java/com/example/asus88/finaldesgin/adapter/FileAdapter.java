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
import com.example.asus88.finaldesgin.bean.FileBean;
import com.example.asus88.finaldesgin.util.BitmapUtil;
import com.example.asus88.finaldesgin.util.FileUtil;
import com.example.asus88.finaldesgin.util.ListUtil;
import com.example.asus88.finaldesgin.util.LogUtil;
import com.zhy.autolayout.utils.AutoUtils;

import java.lang.ref.WeakReference;
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
        String t = FileUtil.getFileType(bean.getPath());
        holder.icon.setImageResource(FileUtil.getImageId(bean.getType()));
        if (t.equals("视频")) {
            BitmapTask task = new BitmapTask(mContext, holder.icon);
            task.execute(bean.getPath());
        } else if (t.equals("图片")) {
            holder.icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(mContext).load(bean.getPath()).thumbnail(0.1f).into(holder.icon);
        } else {
            holder.icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
        holder.modify.setText(bean.getModify());
        holder.selected.setChecked(bean.isSelected());
    }

    @Override
    public int getItemCount() {
        return ListUtil.getSize(list);
    }

    public void setList(List<FileBean> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public List<FileBean> getList() {
        return list;
    }

    public interface onItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        TextView size;
        TextView modify;
        CheckBox selected;
        FileBean bean;


        public MyViewHolder(View itemView) {
            super(itemView);
            AutoUtils.autoSize(itemView);
            icon = (ImageView) itemView.findViewById(R.id.file_adapter_image);
            name = (TextView) itemView.findViewById(R.id.file_adapter_name);
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

    class BitmapTask extends AsyncTask<String, Void, byte[]> {
        ImageView mImageView;
        WeakReference<Context> context;
        String path;

        public BitmapTask(Context context, ImageView imageView) {
            this.context = new WeakReference<Context>(context);
            mImageView = imageView;
        }

        @Override
        protected byte[] doInBackground(String... params) {
            path = params[0];
            Bitmap bitmap = BitmapUtil.getVideoThumbnail(path, context.get().getResources());
            return BitmapUtil.bitmapToByteArray(bitmap);
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            if (mImageView != null && bytes != null) {
                mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(context.get()).load(bytes).thumbnail(0.1f).into(mImageView);
            }
        }
    }
}
