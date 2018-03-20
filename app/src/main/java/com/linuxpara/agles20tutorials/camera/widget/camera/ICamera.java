package com.linuxpara.agles20tutorials.camera.widget.camera;

import android.graphics.SurfaceTexture;

/**
 * Date: 2018/3/16
 * *************************************************************
 * Auther: 陈占洋
 * *************************************************************
 * Email: zhanyang.chen@gmail.com
 * *************************************************************
 * Description: 自定义摄像头接口，用于适配5.0前后的API
 */

public interface ICamera {

    int INVALID_CAMERA_ID = -1;
    int BACK_CAMERA_ID = 0;
    int FRONT_CAMERA_ID = 1;

    Size NORMAL_SIZE = new Size(1280, 720);

    /**
     * 打开摄像头
     *
     * @param cameraId
     */
    void openCamera(int cameraId);

    /**
     * 设置显示的大小
     *
     * @param displaySize
     */
    void setDisplaySize(Size displaySize);

    /**
     * 摄像头开始预览
     */
    void startPreview();

    /**
     * 获取预览图片大小
     *
     * @return
     */
    Size getPreviewSize();

    /**
     * 获取SurfaceTexture
     */
    SurfaceTexture getSurfaceTexture();

    /**
     * 关闭摄像头
     */
    void closeCamera();

    /**
     * 判断摄像头是否关闭
     *
     * @return
     */
    boolean isClosed();

    /**
     * 获取外部纹理Id
     *
     * @return
     */
    int getOesTextureId();

}
