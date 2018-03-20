package com.linuxpara.agles20tutorials.camera.widget;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.linuxpara.agles20tutorials.camera.widget.camera.CameraV2;
import com.linuxpara.agles20tutorials.camera.widget.camera.ICamera;
import com.linuxpara.agles20tutorials.camera.widget.camera.Size;

/**
 * Date: 2018/3/16
 * *************************************************************
 * Auther: 陈占洋
 * *************************************************************
 * Email: zhanyang.chen@gmail.com
 * *************************************************************
 * Description: CameraV2、CameraV1(暂时没有)的代理，根据系统api版本自动调用对应的API
 */

public class CameraBridge implements ICamera {
    private static final String TAG = "CameraBridge";
    private ICamera mCamera;

    public CameraBridge(GLSurfaceView glView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCamera = new CameraV2(glView);
        } else {
            Toast.makeText(glView.getContext(), "暂时不支持5.0以下的系统！", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "CameraBridge: 暂时不支持5.0以下的系统！");
        }
    }


    @Override
    public void openCamera(int cameraId) {
        mCamera.openCamera(cameraId);
    }

    @Override
    public void setDisplaySize(Size displaySize) {
        mCamera.setDisplaySize(displaySize);
    }

    @Override
    public void startPreview() {
        mCamera.startPreview();
    }

    @Override
    public Size getPreviewSize() {
        return mCamera.getPreviewSize();
    }

    @Override
    public SurfaceTexture getSurfaceTexture() {
        return mCamera.getSurfaceTexture();
    }

    @Override
    public void closeCamera() {
        mCamera.closeCamera();
    }

    @Override
    public boolean isClosed() {
        return mCamera.isClosed();
    }

    @Override
    public int getOesTextureId() {
        return mCamera.getOesTextureId();
    }
}
