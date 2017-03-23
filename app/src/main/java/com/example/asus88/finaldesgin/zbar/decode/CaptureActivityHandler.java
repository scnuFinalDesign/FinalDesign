package com.example.asus88.finaldesgin.zbar.decode;

import android.os.Handler;
import android.os.Message;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.activity.CaptureActivity;
import com.example.asus88.finaldesgin.zbar.camera.CameraManager;


/**
 * 作者: 陈涛(1076559197@qq.com)
 * <p>
 * 时间: 2014年5月9日 下午12:23:32
 * <p>
 * 版本: V_1.0.0
 * <p>
 * 描述: 扫描消息转发
 */
public final class CaptureActivityHandler extends Handler {

    DecodeThread decodeThread = null;
    CaptureActivity activity = null;
    private State state;

    private enum State {
        PREVIEW, SUCCESS, DONE
    }

    public CaptureActivityHandler(CaptureActivity activity) {
        this.activity = activity;
        decodeThread = new DecodeThread(activity);
        decodeThread.start();
        state = State.SUCCESS;
        CameraManager.get().startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        int id = message.what;
        if (id == R.id.auto_focus){
            if (state == State.PREVIEW) {
                CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
            }
        }else if (id==R.id.restart_preview){
            restartPreviewAndDecode();
        }else if (id==R.id.decode_succeeded){
            state = State.SUCCESS;
            activity.handleDecode((String) message.obj);// 解析成功，回调
        }else if (id==R.id.decode_failed){
            state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
                    R.id.decode);
        }

    }

    public void quitSynchronously() {
        state = State.DONE;
        CameraManager.get().stopPreview();
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
        removeMessages(R.id.decode);
        removeMessages(R.id.auto_focus);
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
                    R.id.decode);
            CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
        }
    }

}