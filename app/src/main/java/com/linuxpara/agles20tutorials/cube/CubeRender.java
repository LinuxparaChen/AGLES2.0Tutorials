package com.linuxpara.agles20tutorials.cube;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.linuxpara.agles20tutorials.GraphicalRender;
import com.linuxpara.agles20tutorials.R;
import com.linuxpara.agles20tutorials.util.ShaderUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Date: 2018/3/9
 * *************************************************************
 * Auther: 陈占洋
 * *************************************************************
 * Email: zhanyang.chen@gmail.com
 * *************************************************************
 * Description:
 */

class CubeRender extends GraphicalRender {

    private static final int CUBE_SHADER_TAG = 0;

    private int mCubeShaderProgram;

    private int a_position;
    private int a_texCoord;
    private int u_mMatrix;
    private int u_vMatrix;
    private int u_projMatrix;

    private int mTextureId;
    private int mVertIdxSize;
    private FloatBuffer mVertBuf;
    private ByteBuffer mVertIdxBuf;
    private FloatBuffer mTexCoordBuf;

    private float mXAxis = 1;
    private float mYAxis = 0;
    private float mAngle = 0;
    private RAxis mRAxis = RAxis.X;

    //旋转轴
    enum RAxis {
        X, Y
    }

    public CubeRender(GLSurfaceView glview) {
        super(glview);
    }

    @Override
    public void onCreate() {
        //设置背景
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
        //开启深度测试，在绘制3D效果时，需要开启深度测试，
        // 否则绘制出来的3D顶点在平面上，效果跟想要的结果不一样。
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    @Override
    public void onChange(int width, int height) {
        //宽高比
        float r = (float) width / height;
        //设置视口
        GLES20.glViewport(0, 0, width, height);
        //设置模型矩阵为单位矩阵
        Matrix.setIdentityM(sMMatrix, 0);
        //设置摄像机位置、观察矩阵
        Matrix.setLookAtM(sVMatrix, 0,
                0, 0, 6,
                0, 0, 0,
                0, 1, 0);
        //设置平截头体、投影矩阵(透视投影)
        Matrix.frustumM(sProjMatrix, 0,
                -r, r, 1, -1,//此时屏幕坐标原点为屏幕中心点
                3, 9);
        //初始化顶点数据
        initVert();
        //初始化顶点索引
        initVertIdx();
        //根据将资源图片转成纹理
        Bitmap bitmap = BitmapFactory.decodeResource(getView().getResources(), R.mipmap.box);
        mTextureId = genBitmapTextureId(bitmap);
        bitmap.recycle();
        //初始化纹理坐标
        initTextureCoord();
        //初始化着色器程序
        initShaderFromAsset(CUBE_SHADER_TAG, "cube/cube.vert", "cube/cube.frag");

    }

    @Override
    public void onDraw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setRotateM(sMMatrix, 0,
                mAngle, mXAxis, mYAxis, 0);

        GLES20.glUseProgram(mCubeShaderProgram);

        GLES20.glVertexAttribPointer(a_position, 3, GLES20.GL_FLOAT, false, 0, mVertBuf);
        GLES20.glVertexAttribPointer(a_texCoord, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuf);

        GLES20.glUniformMatrix4fv(u_mMatrix, 1, false, sMMatrix, 0);
        GLES20.glUniformMatrix4fv(u_vMatrix, 1, false, sVMatrix, 0);
        GLES20.glUniformMatrix4fv(u_projMatrix, 1, false, sProjMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

        GLES20.glEnableVertexAttribArray(a_position);
        GLES20.glEnableVertexAttribArray(a_texCoord);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mVertIdxSize, GLES20.GL_UNSIGNED_BYTE, mVertIdxBuf);

        GLES20.glDisableVertexAttribArray(a_position);
        GLES20.glDisableVertexAttribArray(a_texCoord);
    }

    @Override
    protected void findShaderAttr(int shaderTag, int shaderProgram) {
        if (shaderTag == CUBE_SHADER_TAG) {
            mCubeShaderProgram = shaderProgram;

            a_position = GLES20.glGetAttribLocation(mCubeShaderProgram, "a_position");
            a_texCoord = GLES20.glGetAttribLocation(mCubeShaderProgram, "a_texCoord");

            u_mMatrix = GLES20.glGetUniformLocation(mCubeShaderProgram, "u_MMatrix");
            u_vMatrix = GLES20.glGetUniformLocation(mCubeShaderProgram, "u_VMatrix");
            u_projMatrix = GLES20.glGetUniformLocation(mCubeShaderProgram, "u_ProjMatrix");
        }
    }

    @Override
    protected void initVert() {
        float[] verts = {
                -0.5f, 0.5f, 0.5f,//0
                -0.5f, -0.5f, 0.5f,//1
                0.5f, 0.5f, 0.5f,//2
                0.5f, -0.5f, 0.5f,//3

                0.5f, 0.5f, -0.5f,//4
                0.5f, -0.5f, -0.5f,//5
                -0.5f, 0.5f, -0.5f,//6
                -0.5f, -0.5f, -0.5f,//7
        };
        mVertBuf = ShaderUtils.getFloatBuffer(verts);
    }

    @Override
    protected void initVertIdx() {
        byte[] vertIdxs = {
                0, 1, 2, 1, 2, 3,//正面
                2, 3, 4, 3, 4, 5,//右侧面
                4, 5, 6, 5, 6, 7,//背面
                6, 7, 0, 7, 0, 1,//左侧面
                0, 2, 6, 2, 6, 4,//上面
                1, 3, 7, 3, 7, 5//下面
        };
        mVertIdxSize = vertIdxs.length;
        mVertIdxBuf = ShaderUtils.getByteBuffer(vertIdxs);
    }

    @Override
    protected void initTextureCoord() {
        float[] texCoords = {
                0, 1,
                0, 0,
                1, 1,
                1, 0,

                0, 1,
                0, 0,
                1, 1,
                1, 0
        };
        mTexCoordBuf = ShaderUtils.getFloatBuffer(texCoords);
    }

    /**
     * 设置旋转轴
     *
     * @param r_axis
     */
    public void setRAxis(RAxis r_axis) {
        mRAxis = r_axis;
        mAngle = 0;
        if (r_axis == RAxis.X) {
            mXAxis = 1;
            mYAxis = 0;
        } else {
            mXAxis = 0;
            mYAxis = 1;
        }
    }

    public void rotate(float distanceX, float distanceY) {
        if (mRAxis == RAxis.X) {
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                return;
            }
            mAngle += -distanceY / 5;
            return;
        }
        if (mRAxis == RAxis.Y) {
            if (Math.abs(distanceY) > Math.abs(distanceX)) {
                return;
            }
            mAngle += distanceX / 5;
            return;
        }
    }
}
