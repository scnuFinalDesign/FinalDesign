package com.example.asus88.finaldesgin;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.asus88.finaldesgin.util.BitmapUtil;

import java.lang.ref.WeakReference;

/**
 * Created by asus88 on 2017/3/21.
 */


public class BitmapTask extends AsyncTask<String, Void, byte[]> {
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

