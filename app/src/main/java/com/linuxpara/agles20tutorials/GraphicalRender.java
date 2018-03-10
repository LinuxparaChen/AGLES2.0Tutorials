package com.linuxpara.agles20tutorials;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;


import com.linuxpara.agles20tutorials.util.ShaderUtils;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Date: 2018/1/15
 * *************************************************************
 * Auther: 陈占洋
 * *************************************************************
 * Email: zhanyang.chen@gmail.com
 * *************************************************************
 * Description: 图形渲染器，提取了GL绘制图形的一些公共方法
 */

public abstract class GraphicalRender implements GLSurfaceView.Renderer{

    //4*4的投影矩阵
    public static float[] sProjMatrix = new float[16];
    //4*4摄像头矩阵（观察矩阵）
    public static float[] sVMatrix = new float[16];
    //4*4模型矩阵（变化矩阵）
    public static float[] sMMatrix = new float[16];

    protected WeakReference<GLSurfaceView> mWeakRefView;

    public GraphicalRender(GLSurfaceView view) {
        mWeakRefView = new WeakReference<>(view);
    }

    /**
     * 界面被创建时被调用
     * @param gl
     * @param config
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        onCreate();
    }

    /**
     * 界面改变时被调用
     * @param gl
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        onChange(width,height);
    }

    /**
     * 绘制一帧内容时被调用
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        onDraw();
    }

    public abstract void onCreate();

    public abstract void onChange(int width, int height);

    public abstract void onDraw();

    /**
     * 初始化着色器
     *
     * @param shaderTag
     * @param verFileName
     * @param fragFileName
     */
    protected void initShaderFromAsset(int shaderTag,String verFileName, String fragFileName) {
        String verCode = ShaderUtils.getCodeFromAsset(verFileName, getView().getResources());
        String fragCode = ShaderUtils.getCodeFromAsset(fragFileName, getView().getResources());
        int shaderProgram = ShaderUtils.createProgram(verCode, fragCode);
        findShaderAttr(shaderTag,shaderProgram);
    }

    /**
     * 查找着色器属性。
     * @param shaderTag
     * @param shaderProgram
     */
    protected abstract void findShaderAttr(int shaderTag, int shaderProgram);

    /**
     * 初始化顶点缓存
     */
    protected void initVert() {

    }

    /**
     * 初始化顶点索引
     */
    protected void initVertIdx(){

    }

    /**
     * 初始化顶点颜色缓存
     */
    protected void initVertColor() {

    }

    /**
     * 初始化纹理坐标
     */
    protected void initTextureCoord() {

    }

    /**
     * 初始化3D模型
     */
    protected void initObjMtl() {

    }

    /**
     * 生成bitmap贴图纹理Id
     *
     * @param bitmap
     */
    protected int genBitmapTextureId(Bitmap bitmap) {
        int textureId = ShaderUtils.genTextureId();
        if (bitmap == null || bitmap.isRecycled()) {
            throw new RuntimeException("传入的图片为空或者已经呗释放掉了！");
        }
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        return textureId;
    }

    /**
     * 获取总变换矩阵
     *
     * @param matrix
     * @return
     */
    protected float[] getMVPMatrix(float[] matrix) {

        float[] mvpMatrix = new float[16];
        //mvpMatrix = sVMatrix × matrix
        Matrix.multiplyMM(mvpMatrix, 0,
                sVMatrix, 0,
                matrix, 0);
        //mvpMatrix = sProjMatrix × mvpMatrix
        Matrix.multiplyMM(mvpMatrix, 0,
                sProjMatrix, 0,
                mvpMatrix, 0);

        return mvpMatrix;
    }

    protected GLSurfaceView getView(){
        if (mWeakRefView == null || mWeakRefView.get() == null) {
            throw new RuntimeException("初始化着色器，需要在界面销毁前执行！！！");
        }
        return mWeakRefView.get();
    }
}
