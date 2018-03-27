package com.linuxpara.agles20tutorials.camera.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.linuxpara.agles20tutorials.R;
import com.linuxpara.agles20tutorials.camera.widget.camera.ICamera;

import java.io.File;

/**
 * Date: 2018/3/16
 * *************************************************************
 * Auther: 陈占洋
 * *************************************************************
 * Email: zhanyang.chen@gmail.com
 * *************************************************************
 * Description: 照相机控件。
 */
public class CameraView extends GLSurfaceView {

    private static final String TAG = "CameraView";

    private int mCameraId = ICamera.FRONT_CAMERA_ID;
    private CameraDrawer mCameraDrawer;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CameraView);
        mCameraId = ta.getInt(R.styleable.CameraView_camera_id, ICamera.FRONT_CAMERA_ID);
        ta.recycle();

        setEGLContextClientVersion(2);
        mCameraDrawer = new CameraDrawer(this, mCameraId);
        setRenderer(mCameraDrawer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        mCameraDrawer.destory();
    }

    public void switchCamera() {
        mCameraDrawer.switchCamera();
    }

    public void setEffect(CameraDrawer.Effect effect) {
        mCameraDrawer.setEffect(effect);
    }

    /**
     * 设置冷暖色强度
     *
     * @param strength 冷色负数，暖色正数。范围-1至+1.
     */
    public void setWarmCoolStrength(float strength) {
        mCameraDrawer.setWarmCoolStrength(strength);
    }

    /**
     * 卷积核大小，卷积核的大小一般为奇数3，5，7，9，......
     *
     * @param kernelSize
     */
    public void setKernelSize(int kernelSize) {
        mCameraDrawer.setKernelSize(kernelSize);
    }

    /**
     * 开始捕获视屏
     *
     * @param file
     */
    public void startCaptureVideo(File file) {
        mCameraDrawer.startCaptureVideo(file);
    }

    /**
     * 结束视频录制
     */
    public void stopCaptureVideo() {
        mCameraDrawer.stopCaptureVideo();
    }
}
