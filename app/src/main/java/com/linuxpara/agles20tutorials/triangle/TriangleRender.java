package com.linuxpara.agles20tutorials.triangle;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;


import com.linuxpara.agles20tutorials.GraphicalRender;
import com.linuxpara.agles20tutorials.util.ShaderUtils;

import java.nio.FloatBuffer;

/**
 * Date: 2018/1/16
 * *************************************************************
 * Auther: 陈占洋
 * *************************************************************
 * Email: zhanyang.chen@gmail.com
 * *************************************************************
 * Description: 三角形渲染器
 */

public class TriangleRender extends GraphicalRender {

    private static final int TRIANGLE_SHADER_TAG = 0;

    private float mWidth;
    private float mHeight;

    private int a_position;
    private int a_color;
    private int u_mMatrix;
    private int u_vMatrix;
    private int u_projMatrix;

    private FloatBuffer mVertBuf;
    private FloatBuffer mVertColorBuf;
    private int mVertSize;

    private int mTriangleShaderProgram;

    public TriangleRender(GLSurfaceView view) {
        super(view);
    }

    @Override
    public void onCreate() {
        //设置背景色，普通的setBackground()对GLSurfaceView无效
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
    }

    @Override
    public void onChange(int width, int height) {
        //设置视口
        GLES20.glViewport(0, 0, width, height);
        float radio = (float) width / height;
        //初始化变换矩阵为单位矩阵
        Matrix.setIdentityM(sMMatrix, 0);
        //设置观察点(相机)矩阵，在坐标转换中详细介绍了每个参数的含义、及系统是怎么确定摄像机位置的。
        Matrix.setLookAtM(sVMatrix, 0,
                0, 0, 3,//摄像机位置，
                0, 0, 0,//目标位置（摄像机和目标位置可以决定屏幕的坐标体系）
                0, 1, 0);//up向量(上向量)
        //设置正交投影矩阵
        Matrix.orthoM(sProjMatrix, 0,
                0, radio, 1, 0,//平截头体的四个坐标（可以确定坐标原点的位置，此时坐标原点为屏幕左上角）
                3, 10);//近平面距离摄像机的距离、远平面距离摄像机的距离
        mWidth = radio;
        mHeight = 1;
        //初始化顶点数据（顶点在局部空间中的位置）
        initVert();
        //初始化顶点颜色
        initVertColor();
        //加载着色器程序
        initShaderFromAsset(TRIANGLE_SHADER_TAG, "triangle/triangle.vert", "triangle/triangle.frag");
    }

    @Override
    public void onDraw() {
        //清除颜色缓存
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //使用指定着色器程序
        GLES20.glUseProgram(mTriangleShaderProgram);
        //给着色器中attribute修饰的变量赋值
        GLES20.glVertexAttribPointer(a_position, 3, GLES20.GL_FLOAT, false, 0, mVertBuf);
        //给着色器中的a_color变量赋值
        GLES20.glVertexAttribPointer(a_color, 4, GLES20.GL_FLOAT, false, 0, mVertColorBuf);
        //给着色器中uniform修饰的变量赋值
        GLES20.glUniformMatrix4fv(u_mMatrix, 1, false, sMMatrix, 0);
        GLES20.glUniformMatrix4fv(u_vMatrix, 1, false, sVMatrix, 0);
        GLES20.glUniformMatrix4fv(u_projMatrix, 1, false, sProjMatrix, 0);
        //启用顶点位置数据
        GLES20.glEnableVertexAttribArray(a_position);
        //启用顶点颜色数据
        GLES20.glEnableVertexAttribArray(a_color);
        //绘制图形,数组法绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertSize);
        //绘制图形，索引法绘制
        //GLES20.glDrawElements(GLES20.GL_TRIANGLES,mVertSize,GLES20.GL_INT,mVertIdxBuf);
        //关闭顶点属性
        GLES20.glDisableVertexAttribArray(a_position);
        //关闭顶点颜色属性
        GLES20.glDisableVertexAttribArray(a_color);

    }

    @Override
    protected void findShaderAttr(int shaderTag, int shaderProgram) {
        if (TRIANGLE_SHADER_TAG == shaderTag) {
            mTriangleShaderProgram = shaderProgram;
            //获取顶点着色器中a_position指针(attribute类型)
            a_position = GLES20.glGetAttribLocation(mTriangleShaderProgram, "a_position");
            //获取顶点着色器中a_color指针(attribute类型)
            a_color = GLES20.glGetAttribLocation(mTriangleShaderProgram, "a_color");
            //获取顶点着色器中u_MMatrix指针(uniform类型)
            u_mMatrix = GLES20.glGetUniformLocation(mTriangleShaderProgram, "u_MMatrix");
            //获取顶点着色器中u_VMatrix指针(uniform类型)
            u_vMatrix = GLES20.glGetUniformLocation(mTriangleShaderProgram, "u_VMatrix");
            //获取顶点着色器中u_ProjMatrix指针(uniform类型)
            u_projMatrix = GLES20.glGetUniformLocation(mTriangleShaderProgram, "u_ProjMatrix");
        }
    }

    @Override
    protected void initVert() {
        float[] verts = {
                mWidth / 2, 0.0f, 0.0f,//第一个顶点的xyz坐标
                0.0f, mHeight, 0.0f,//第二个顶点的xyz坐标
                mWidth, mHeight, 0.0f//第三个顶点的xyz坐标
        };
        //顶点个数
        mVertSize = verts.length / 3;
        mVertBuf = ShaderUtils.getFloatBuffer(verts);
    }

    @Override
    protected void initVertColor() {
        float[] vertColor = {
                1.0f, 0.0f, 0.0f, 0.0f,//第一个顶点的rgba颜色值
                0.0f, 1.0f, 0.0f, 0.0f,//第二个顶点的rgba颜色值
                0.0f, 0.0f, 1.0f, 0.0f//第三个顶点的rgba颜色值
        };
        mVertColorBuf = ShaderUtils.getFloatBuffer(vertColor);
    }
}
