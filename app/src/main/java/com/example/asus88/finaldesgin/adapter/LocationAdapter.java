package com.example.asus88.finaldesgin.adapter;

import android.content.Context;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.asus88.finaldesgin.R;

import java.util.List;

/**
 * Created by asus88 on 2017/1/19.
 */

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.MyViewHolder> {
    private static final String TAG = "LocationAdapter";
    private Context mContext;
    private List<String> list;
    private onItemClickListener mOnItemClickListener;


    public LocationAdapter(Context context, List<String> list) {
        mContext = context;
        this.list = list;
    }

    public void setOnItemClickListener(onItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final MyViewHolder holder = new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.adapter_location, parent, false));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onLocationItemClick(holder.getLayoutPosition());
                } else {
                    Log.d(TAG, "onClick: please implements the onItemClickListener");
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        String path = list.get(position);
        if (path.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            holder.mTextView.setText("手机存储");
        } else {
            int index = path.lastIndexOf("/");
            holder.mTextView.setText(path.substring(index + 1));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface onItemClickListener {
        void onLocationItemClick(int position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.location_adapter_text);
        }
    }
}
