package com.example.asus88.finaldesgin;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.asus88.finaldesgin.util.BitmapUtil;

import java.lang.ref.WeakReference;

/**
 * Created by asus88 on 2017/3/21.
 */


public class BitmapTask extends AsyncTask<String, Void, byte[]> {
    private static final String TAG = "BitmapTask";
    ImageView mImageView;
    WeakReference<Context> context;
    String path;
    ArrayMap<String, Bitmap> arrayMap;

    public BitmapTask(Context context, ImageView imageView, ArrayMap<String, Bitmap> arrayMap) {
        this.context = new WeakReference<Context>(context);
        mImageView = imageView;
        this.arrayMap = arrayMap;
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
            Glide.with(context.get()).load(bytes).asBitmap().thumbnail(0.1f).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    mImageView.setImageBitmap(resource);
                    Log.d(TAG, "onResourceReady: ");
                    if (arrayMap != null) {
                        arrayMap.put(path, resource);
                    }
                }
            });
        }
    }
}

