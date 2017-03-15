package com.example.asus88.finaldesgin.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;

import com.example.asus88.finaldesgin.R;

import java.io.ByteArrayOutputStream;

/**
 * Created by asus88 on 2017/3/15.
 */

public class BitmapUtil {
    /**
     * 返回视频缩略图
     *
     * @param filePath
     * @param resources
     * @return
     */
    public static Bitmap getVideoThumbnail(String filePath, Resources resources) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        if (bitmap == null)
            bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher);
        bitmap = resizeBitmap(bitmap);
        return bitmap;
    }

    public static byte[] bitmapToByteArray(Bitmap bitmap){
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bao);
        return bao.toByteArray();
    }

    private static Bitmap resizeBitmap(Bitmap bitmap) {
        if (bitmap.getWidth() < bitmap.getHeight()) {
            float x = (float) bitmap.getHeight() / (float) bitmap.getWidth();
            Matrix matrix = new Matrix();
            matrix.postScale(x, 1);
            Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return resizeBmp;
        }
        return bitmap;
    }
}
