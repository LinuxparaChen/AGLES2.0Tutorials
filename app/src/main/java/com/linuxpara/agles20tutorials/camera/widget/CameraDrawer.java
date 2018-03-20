package com.linuxpara.agles20tutorials.camera.widget;

import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.linuxpara.agles20tutorials.GraphicalRender;
import com.linuxpara.agles20tutorials.camera.widget.camera.ICamera;
import com.linuxpara.agles20tutorials.camera.widget.camera.Size;
import com.linuxpara.agles20tutorials.util.ShaderUtils;

import java.nio.FloatBuffer;

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

    private int mVertSize;
    private FloatBuffer mVertBuf;
    private FloatBuffer mTexCoordBuf;

    private float w;
    private float h;
    private Size mPreviewSize;
    private Size mViewSize;

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
    }

    @Override
    public void onDraw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        SurfaceTexture surfaceTexture = mCameraBridge.getSurfaceTexture();
        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(sCoordMatrix);

        GLES20.glUseProgram(mCameraShaderProgram);

        GLES20.glVertexAttribPointer(a_position, 3, GLES20.GL_FLOAT, false, 0, mVertBuf);
        GLES20.glVertexAttribPointer(a_texCoord, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuf);

        GLES20.glUniformMatrix4fv(u_mMatrix, 1, false, sMMatrix, 0);
        GLES20.glUniformMatrix4fv(u_vMatrix, 1, false, sVMatrix, 0);
        GLES20.glUniformMatrix4fv(u_projMatrix, 1, false, sProjMatrix, 0);

        GLES20.glUniformMatrix4fv(u_coordMatrix, 1, false, sCoordMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mCameraBridge.getOesTextureId());

        GLES20.glEnableVertexAttribArray(a_position);
        GLES20.glEnableVertexAttribArray(a_texCoord);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertSize);

        GLES20.glDisableVertexAttribArray(a_position);
        GLES20.glDisableVertexAttribArray(a_texCoord);
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
        //以上的实现思路：缩放纹理图片，让纹理图片的宽或者高任意一项与展示宽高相等，
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
}
