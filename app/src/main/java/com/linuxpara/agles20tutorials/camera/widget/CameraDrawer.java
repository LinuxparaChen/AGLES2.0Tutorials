package com.linuxpara.agles20tutorials.camera.widget;

import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.annotation.NonNull;

import com.linuxpara.agles20tutorials.GraphicalRender;
import com.linuxpara.agles20tutorials.camera.widget.camera.ICamera;
import com.linuxpara.agles20tutorials.camera.widget.camera.Size;
import com.linuxpara.agles20tutorials.camera.widget.video.VideoEncoder;
import com.linuxpara.agles20tutorials.util.ShaderUtils;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: 2018/3/16
 * *************************************************************
 * Auther: 陈占洋
 * *************************************************************
 * Email: zhanyang.chen@gmail.com
 * *************************************************************
 * Description: 获取Camera数据，并绘制。根据展示窗口大小自动裁剪camera图片数据。
 */

public class CameraDrawer extends GraphicalRender {

    private static final String TAG = "CameraDrawer";
    private CaptureVideoStatus mCaptureVideoStatus = CaptureVideoStatus.NONE;
    private VideoEncoder mVideoEncoder;
    private File mCaptureVideoOutFile;

    public enum CaptureVideoStatus {
        NONE, START_CAPTURE, CAPTURING, STOP_CAPTURE
    }

    public enum Effect {
        //默认(原始)、灰度、  底片、        冷暖色、        浮雕       雕刻
        NONE(0), GRAY(1), NEGATIVE(2), WARM_COOL(3), CAMEO(4), CARVING(5);

        private int mValue;

        Effect(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }
    }

    private static final int CAMERA_SHADER_TAG = 0;

    private static final float[] sCoordMatrix = new float[16];

    private int mCameraId = ICamera.INVALID_CAMERA_ID;
    private CameraBridge mCameraBridge;
    private int mCameraShaderProgram;

    private int a_position;
    private int a_texCoord;
    private int u_mMatrix;
    private int u_vMatrix;
    private int u_projMatrix;
    private int u_coordMatrix;
    private int u_effect;
    private int u_warmCoolStrength;
    private int u_kernelSize;
    private int u_imgWH;

    private int mVertSize;
    private FloatBuffer mVertBuf;
    private FloatBuffer mTexCoordBuf;

    private float w;
    private float h;
    private Size mPreviewSize;
    private Size mViewSize;

    private Effect mEffect = Effect.NONE;
    //冷暖色强度
    private float mWarmCoolStrength;
    private int mWidth;
    private int mHeight;
    private int mKernelSize = 3;//默认卷积核的大小为3

    public CameraDrawer(GLSurfaceView view) {
        super(view);
    }

    public CameraDrawer(GLSurfaceView view, int cameraId) {
        super(view);
        mCameraId = cameraId;
    }


    @Override
    public void onCreate() {
        GLES20.glClearColor(0, 0, 0, 0);
        mCameraBridge = new CameraBridge(getView());
        mCameraBridge.openCamera(mCameraId);

    }

    @Override
    public void onChange(int width, int height) {
        mWidth = width;
        mHeight = height;
        GLES20.glViewport(0, 0, width, height);

        mCameraBridge.setDisplaySize(new Size(width, height));
        mCameraBridge.startPreview();

        mPreviewSize = mCameraBridge.getPreviewSize();
        mViewSize = new Size(width, height);

        float v_r = (float) width / height;
        Matrix.setIdentityM(sMMatrix, 0);
        Matrix.setLookAtM(sVMatrix, 0,
                0, 0, 3,
                0, 0, 0,
                0, 1, 0);
        Matrix.orthoM(sProjMatrix, 0,
                -v_r, v_r, -1, 1,//这样设置，(0,0)点为屏幕中心点
                2, 4);
        w = 2 * v_r;
        h = 2 * 1;
        initVert();
        initTextureCoord();
        initShaderFromAsset(CAMERA_SHADER_TAG, "camera/camera.vert", "camera/camera.frag");
        //视频编码
        mVideoEncoder = new VideoEncoder("video_encoder");
    }

    @Override
    public void onDraw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (mCaptureVideoStatus == CaptureVideoStatus.START_CAPTURE) {
            VideoEncoder.EncoderConfig encoderConfig = new VideoEncoder.EncoderConfig(mWidth, mHeight, 1000000, EGL14.eglGetCurrentContext());
            mVideoEncoder.setEncoderConfig(encoderConfig);
            mVideoEncoder.setCameraDrawer(this);
            mVideoEncoder.startCaptureVideo(mCaptureVideoOutFile);
            mCaptureVideoStatus = CaptureVideoStatus.CAPTURING;
        }

        SurfaceTexture surfaceTexture = mCameraBridge.getSurfaceTexture();
        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(sCoordMatrix);

        drawPreview();

        if (mCaptureVideoStatus == CaptureVideoStatus.CAPTURING) {
            mVideoEncoder.CaptureFrame(surfaceTexture.getTimestamp());
        }
        if (mCaptureVideoStatus == CaptureVideoStatus.STOP_CAPTURE) {
            mVideoEncoder.CaptureEOSFrame(surfaceTexture.getTimestamp());
            mCaptureVideoStatus = CaptureVideoStatus.NONE;
        }
    }

    /**
     * 绘制摄像头预览界面
     */
    public void drawPreview() {

        GLES20.glUseProgram(mCameraShaderProgram);

        GLES20.glVertexAttribPointer(a_position, 3, GLES20.GL_FLOAT, false, 0, mVertBuf);
        GLES20.glVertexAttribPointer(a_texCoord, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuf);

        GLES20.glUniformMatrix4fv(u_mMatrix, 1, false, sMMatrix, 0);
        GLES20.glUniformMatrix4fv(u_vMatrix, 1, false, sVMatrix, 0);
        GLES20.glUniformMatrix4fv(u_projMatrix, 1, false, sProjMatrix, 0);

        GLES20.glUniformMatrix4fv(u_coordMatrix, 1, false, sCoordMatrix, 0);

        GLES20.glUniform1i(u_effect, mEffect.getValue());
        GLES20.glUniform1f(u_warmCoolStrength, mWarmCoolStrength);
        GLES20.glUniform1i(u_kernelSize, mKernelSize);
        GLES20.glUniform2iv(u_imgWH, 1, new int[]{mWidth, mHeight}, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mCameraBridge.getOesTextureId());

        GLES20.glEnableVertexAttribArray(a_position);//设置效果
        GLES20.glEnableVertexAttribArray(a_texCoord);//设置冷暖色调强度

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertSize);

        GLES20.glDisableVertexAttribArray(a_position);
        GLES20.glDisableVertexAttribArray(a_texCoord);
    }

    public void destory() {
        if (mCaptureVideoStatus == CaptureVideoStatus.CAPTURING) {
            mCaptureVideoStatus = CaptureVideoStatus.STOP_CAPTURE;
        }
        mVideoEncoder.quitSafely();
    }

    @Override
    protected void findShaderAttr(int shaderTag, int shaderProgram) {
        if (shaderTag == CAMERA_SHADER_TAG) {
            mCameraShaderProgram = shaderProgram;
            a_position = GLES20.glGetAttribLocation(mCameraShaderProgram, "a_position");
            a_texCoord = GLES20.glGetAttribLocation(mCameraShaderProgram, "a_texCoord");

            u_mMatrix = GLES20.glGetUniformLocation(mCameraShaderProgram, "u_MMatrix");
            u_vMatrix = GLES20.glGetUniformLocation(mCameraShaderProgram, "u_VMatrix");
            u_projMatrix = GLES20.glGetUniformLocation(mCameraShaderProgram, "u_ProjMatrix");

            u_coordMatrix = GLES20.glGetUniformLocation(mCameraShaderProgram, "u_coordMatrix");

            u_effect = GLES20.glGetUniformLocation(mCameraShaderProgram, "u_effect");
            u_warmCoolStrength = GLES20.glGetUniformLocation(mCameraShaderProgram, "u_warmCoolStrength");
            u_kernelSize = GLES20.glGetUniformLocation(mCameraShaderProgram, "u_kernelSize");
            u_imgWH = GLES20.glGetUniformLocation(mCameraShaderProgram, "u_imgWH");
        }
    }

    @Override
    protected void initVert() {
        float[] verts = {
                -w / 2, h / 2, 0,//左上
                w / 2, h / 2, 0,//右上
                -w / 2, -h / 2, 0,//左下
                w / 2, -h / 2, 0//右下
        };
        mVertSize = verts.length / 3;
        mVertBuf = ShaderUtils.getFloatBuffer(verts);
    }

    @Override
    protected void initTextureCoord() {
        RectF rectF = new RectF();
        float w_r = (float) mPreviewSize.getWidth() / mViewSize.getWidth();
        float h_r = (float) mPreviewSize.getHeight() / mViewSize.getHeight();
        if (w_r < h_r) {
            //以宽为纹理单位长度
            //先把预览图片缩放为展示图片的宽度，计算出缩放后的高度
            float h = mPreviewSize.getHeight() / w_r;
            //纹理的起始点在左下角（top、bottom是反着的），计算顶点对应纹理的坐标
            float end = (h - mViewSize.getHeight()) / 2 / h;
            float start = end + mViewSize.getHeight() / h;
            //截取纹理区域
            rectF.left = 0;
            rectF.top = start;
            rectF.right = 1;
            rectF.bottom = end;
        } else {
            //以高为纹理单位长度
            //先把预览图片缩放为展示图片的高度，计算出缩放后的宽度
            float w = mPreviewSize.getWidth() / h_r;
            //计算顶点对应纹理的坐标
            float start = (w - mViewSize.getWidth()) / 2 / w;
            float end = start + mViewSize.getWidth() / w;
            rectF.left = start;
            rectF.top = 1;
            rectF.right = end;
            rectF.bottom = 0;
        }
        float[] coords = {
                rectF.left, rectF.top,
                rectF.right, rectF.top,
                rectF.left, rectF.bottom,
                rectF.right, rectF.bottom
        };
        mTexCoordBuf = ShaderUtils.getFloatBuffer(coords);
    }

    public void switchCamera() {
        mCameraId = mCameraId == ICamera.FRONT_CAMERA_ID ? ICamera.BACK_CAMERA_ID : ICamera.FRONT_CAMERA_ID;
        mCameraBridge.closeCamera();
        getView().onPause();
        getView().onResume();
    }

    public void setEffect(Effect effect) {
        mEffect = effect;
    }

    /**
     * 设置冷暖色强度
     *
     * @param strength 冷色负数，暖色正数。范围-1至+1.
     */
    public void setWarmCoolStrength(float strength) {
        mWarmCoolStrength = strength > 1 ? 1 : strength;
        mWarmCoolStrength = strength < -1 ? -1 : strength;
    }

    /**
     * 设置卷积核大小
     *
     * @param kernelSize
     */
    public void setKernelSize(int kernelSize) {
        this.mKernelSize = kernelSize;
    }

    /**
     * 开始录制视频，如果文件已经存在会在文件名后增加(num)
     */
    public void startCaptureVideo(File file) {
//        File final_file;
//        if (!file.exists()) {
//            file.getParentFile().mkdirs();//创建父目录
//            try {
//                file.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            final_file = file;
//        } else {
//            String fileName = file.getName();
//            Pattern pattern = Pattern.compile("\\(\\d+\\)");
//            Matcher m = pattern.matcher(fileName);
//            if (m.matches()) {
//                int num = 0;
//                while (m.find()) {
//                    String content = m.group(0);
//                    num = Integer.parseInt(content.substring(1, content.length() - 1));
//                }
//                String new_file_name = fileName.replace(num + "", ++num + "");
//                final_file = new File(file.getParentFile(), new_file_name);
//            } else {
//                String filePath = file.getAbsolutePath();
//                //后缀
//                String suffix = filePath.substring(filePath.indexOf("."), filePath.length() - 1);
//                String new_file_path = filePath.replace(suffix, "(1)" + suffix);
//                final_file = new File(new_file_path);
//            }
//            if (!final_file.exists()) {
//                try {
//                    final_file.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCaptureVideoOutFile = file;
        mCaptureVideoStatus = CaptureVideoStatus.START_CAPTURE;
    }

    /**
     * 结束视频录制
     */

    public void stopCaptureVideo() {
        mCaptureVideoStatus = CaptureVideoStatus.STOP_CAPTURE;
    }
}
