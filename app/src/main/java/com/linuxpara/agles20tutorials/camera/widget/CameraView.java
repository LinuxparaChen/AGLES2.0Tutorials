package com.linuxpara.agles20tutorials.camera.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;

import com.linuxpara.agles20tutorials.R;
import com.linuxpara.agles20tutorials.camera.widget.camera.ICamera;

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

    public void switchCamera() {
        mCameraDrawer.switchCamera();
    }
}
