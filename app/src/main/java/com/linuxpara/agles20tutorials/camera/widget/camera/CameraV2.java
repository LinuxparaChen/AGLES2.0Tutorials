package com.linuxpara.agles20tutorials.camera.widget.camera;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import com.linuxpara.agles20tutorials.util.ShaderUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Date: 2018/3/16
 * *************************************************************
 * Auther: 陈占洋
 * *************************************************************
 * Email: zhanyang.chen@gmail.com
 * *************************************************************
 * Description: 封装5.0后摄像头API
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CameraV2 implements ICamera {
    private static final String TAG = "CameraV2";

    private WeakReference<GLSurfaceView> mWeakGLViewRef;

    private CameraManager mCameraManager;
    private HandlerThread mCameraThead;
    private Handler mCameraHandler;

    private CameraDevice mCameraDevice;
    private SurfaceTexture mSurfaceTexture;

    private CameraCaptureSession mCaptureSession;
    private CaptureRequest mCaptureRequest;
    private boolean mIsOpened = false;
    private List<Size> mSupportSizes;
    private Size mOptimalSize;
    private int mOesTextureId;


    public CameraV2(GLSurfaceView glView) {
        mWeakGLViewRef = new WeakReference<>(glView);
        cameraPrepare();
    }

    /**
     * 获取上下文
     *
     * @return
     */
    public Context getContext() {
        return getGLView().getContext();
    }

    /**
     * 获取GLSurfaceView
     *
     * @return
     */
    private GLSurfaceView getGLView() {
        if (mWeakGLViewRef != null && mWeakGLViewRef.get() != null) {
            return mWeakGLViewRef.get();
        }
        throw new RuntimeException("CameraV2: GLView被释放！");
    }

    /**
     * 摄像头准备工作
     */
    private void cameraPrepare() {
        mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        mCameraThead = new HandlerThread("camera_thread");
        mCameraThead.start();
        mCameraHandler = new Handler(mCameraThead.getLooper());

        mOesTextureId = ShaderUtils.genOESTextureId();
        mSurfaceTexture = new SurfaceTexture(mOesTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                getGLView().requestRender();
            }
        });
    }

    /**
     * 开启摄像头
     */
    @Override
    public void openCamera(int cameraId) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "摄像头权限未开启！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cameraId == INVALID_CAMERA_ID) {
            Log.i(TAG, "openCamera: 摄像头ID无效");
            return;
        }
        if (!isClosed()) {
            closeCamera();
        }
        mSupportSizes = getSupportSize(cameraId);
        try {
            //开启摄像头
            mCameraManager.openCamera(cameraId + "", new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    mCameraDevice = camera;
//                    openCaptureSession();
                    mIsOpened = true;
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.i(TAG, "onError: 开启摄像头发生错误，错误码：" + error);
                    camera.close();
                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setDisplaySize(Size displaySize) {
        mOptimalSize = getOptimalSize(displaySize);
        mSurfaceTexture.setDefaultBufferSize(mOptimalSize.getWidth(), mOptimalSize.getHeight());
    }

    /**
     * 获取最优尺寸
     *
     * @param displaySize
     * @return
     */
    private Size getOptimalSize(Size displaySize) {
        if (mSupportSizes == null || mSupportSizes.size() == 0) {
            throw new RuntimeException("未开启摄像头，请先调用openCamera()方法开启摄像头");
        }
        List<Size> optimalSizes = new ArrayList<>();
        float d_r;
        if (displaySize.getHeight() > displaySize.getWidth()) {
            d_r = (float) displaySize.getHeight() / displaySize.getWidth();
        } else {
            d_r = (float) displaySize.getWidth() / displaySize.getHeight();
        }
        for (int i = 0; i < mSupportSizes.size(); i++) {
            Size supportSize = mSupportSizes.get(i);
            float s_r = (float) supportSize.getWidth() / supportSize.getHeight();
            if (Math.abs(d_r - s_r) <= 0.05 &&
                    supportSize.getWidth() * supportSize.getHeight() >= NORMAL_SIZE.getWidth() * NORMAL_SIZE.getHeight()) {
                optimalSizes.add(supportSize);
            }
        }
        if (optimalSizes.size() > 0) {
            return optimalSizes.get(0);
        }
        return NORMAL_SIZE;
    }


    private List<Size> getSupportSize(int cameraId) {
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId + "");
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            android.util.Size[] outputSizes = map.getOutputSizes(mSurfaceTexture.getClass());
            ArrayList<Size> supportSizes = new ArrayList<>();
            for (int i = 0; i < outputSizes.length; i++) {
                android.util.Size outputSize = outputSizes[i];
                Size size = new Size(outputSize.getWidth(), outputSize.getHeight());
                supportSizes.add(size);
            }
            printList(supportSizes);
            return supportSizes;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void printList(List<Size> sizes) {
        for (int i = 0; i < sizes.size(); i++) {
            Log.i(TAG, "printList: " + sizes.get(i).toString());
        }
    }

    /**
     * 开启摄像头捕捉画面会话
     */
    private void openCaptureSession() {
        try {
            Surface surface = new Surface(mSurfaceTexture);
            CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            mCaptureRequest = captureRequestBuilder.build();

            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCaptureSession = session;
                    try {
                        mCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.i(TAG, "onConfigureFailed: captureSession 配置失败！");
                    session.getDevice().close();
                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始预览
     */
    @Override
    public void startPreview() {
        //调用的时候有可能摄像头还没开启完成，需要等待开启完成。
        while (!mIsOpened) {
            SystemClock.sleep(100);
        }
        openCaptureSession();
    }

    /**
     * 关闭摄像头
     */
    @Override
    public void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
            mIsOpened = false;
            mCameraThead.quitSafely();
            mCameraHandler = null;
        }
    }

    /**
     * 判断摄像头是否关闭
     *
     * @return
     */
    @Override
    public boolean isClosed() {
        return mCameraDevice == null;
    }

    @Override
    public int getOesTextureId() {
        return mOesTextureId;
    }

    @Override
    public Size getPreviewSize() {
        if (mOptimalSize == null) {
            throw new RuntimeException("请先调用setDisplaySize()");
        }
        return new Size(mOptimalSize.getHeight(), mOptimalSize.getWidth());
    }

    @Override
    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }


}
