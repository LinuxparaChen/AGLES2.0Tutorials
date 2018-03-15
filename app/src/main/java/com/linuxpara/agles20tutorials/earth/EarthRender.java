package com.linuxpara.agles20tutorials.earth;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.linuxpara.agles20tutorials.GraphicalRender;
import com.linuxpara.agles20tutorials.R;
import com.linuxpara.agles20tutorials.util.ShaderUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Date: 2018/3/11
 * *************************************************************
 * Auther: 陈占洋
 * *************************************************************
 * Email: zhanyang.chen@gmail.com
 * *************************************************************
 * Description:
 */

class EarthRender extends GraphicalRender {

    private static final int SPHERE_SHADER_TAG = 0;

    private int a_position;
    private int a_texCoord;
    private int u_lightPos;
    private int u_viewPos;
    private int u_lightColor;
    private int u_mMatrix;
    private int u_vMatrix;
    private int u_projMatrix;

    private int mSphereShaderProgram;
    private FloatBuffer mVertBuf;
    private FloatBuffer mTexCoordBuf;
    private int mVertSize;
    private int mTextureId;

    private int mVStep = 5;//步长需要能被90整除
    private int mHStep = 2 * mVStep;
    private float mR = 1.5f;

    private float mAngle;
    private final Handler mHandler;
    private float mRotateX = (float) Math.sin(Math.toRadians(23.5));
    private float mRotateY = (float) Math.cos(Math.toRadians(23.5));


    public EarthRender(GLSurfaceView view) {
        super(view);
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                mAngle++;
                mHandler.sendEmptyMessageDelayed(0, 40);
            }
        };
        mHandler.sendEmptyMessageDelayed(0, 100);
    }

    @Override
    public void onCreate() {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    @Override
    public void onChange(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float r = (float) width / height;

        Matrix.setIdentityM(sMMatrix, 0);
        //setRotateEulerM：欧拉角转成旋转矩阵
        //rotateM：基于上次旋转，累加
        //setRotateM：与rotateM相反不会累加，基于0
        Matrix.setRotateM(sMMatrix, 0, -23.5f, 0, 0, 1);

        Matrix.setLookAtM(sVMatrix, 0,
                0, 0, 8,
                0, 0, 0,
                0, 1, 0);

        Matrix.frustumM(sProjMatrix, 0,
                -r, r, -1, 1,
                3, 13);

        initVert();
        initTextureCoord();

        Bitmap bitmap = BitmapFactory.decodeResource(getView().getResources(), R.mipmap.earth);
        mTextureId = genBitmapTextureId(bitmap);
        bitmap.recycle();

        initShaderFromAsset(SPHERE_SHADER_TAG, "earth/earth.vert", "earth/earth.frag");
    }

    @Override
    public void onDraw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setRotateM(sMMatrix, 0, mAngle, mRotateX, mRotateY, 0);
        GLES20.glUseProgram(mSphereShaderProgram);

        GLES20.glVertexAttribPointer(a_position, 3, GLES20.GL_FLOAT, false, 0, mVertBuf);
        GLES20.glVertexAttribPointer(a_texCoord, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuf);
        //给光源位置赋值
        GLES20.glUniform3fv(u_lightPos, 1, new float[]{40, 0, 0}, 0);
        //给观察点位置赋值(setLookAtM中eye的xyz)
        GLES20.glUniform3fv(u_viewPos, 1, new float[]{0.0f, 0.0f, 8.0f}, 0);
        //给光源颜色赋值
        GLES20.glUniform4fv(u_lightColor, 1, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        GLES20.glUniformMatrix4fv(u_mMatrix, 1, false, sMMatrix, 0);
        GLES20.glUniformMatrix4fv(u_vMatrix, 1, false, sVMatrix, 0);
        GLES20.glUniformMatrix4fv(u_projMatrix, 1, false, sProjMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

        GLES20.glEnableVertexAttribArray(a_position);
        GLES20.glEnableVertexAttribArray(a_texCoord);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertSize);

        GLES20.glDisableVertexAttribArray(a_position);
        GLES20.glDisableVertexAttribArray(a_texCoord);

    }

    @Override
    protected void findShaderAttr(int shaderTag, int shaderProgram) {
        if (shaderTag == SPHERE_SHADER_TAG) {
            mSphereShaderProgram = shaderProgram;
            a_position = GLES20.glGetAttribLocation(mSphereShaderProgram, "a_position");
            a_texCoord = GLES20.glGetAttribLocation(mSphereShaderProgram, "a_texCoord");

            u_lightPos = GLES20.glGetUniformLocation(mSphereShaderProgram, "u_lightPos");
            u_viewPos = GLES20.glGetUniformLocation(mSphereShaderProgram, "u_viewPos");
            u_lightColor = GLES20.glGetUniformLocation(mSphereShaderProgram, "u_lightColor");

            u_mMatrix = GLES20.glGetUniformLocation(mSphereShaderProgram, "u_MMatrix");
            u_vMatrix = GLES20.glGetUniformLocation(mSphereShaderProgram, "u_VMatrix");
            u_projMatrix = GLES20.glGetUniformLocation(mSphereShaderProgram, "u_ProjMatrix");

        }
    }

    @Override
    protected void initVert() {
        ArrayList<Float> vertList = new ArrayList<>();

        for (int vAngle = -90; vAngle < 90; vAngle += mVStep) {

            double hR1 = mR * Math.cos(Math.toRadians(vAngle));
            double y1 = mR * Math.sin(Math.toRadians(vAngle));

            double hR2 = mR * Math.cos(Math.toRadians(vAngle + mVStep));
            double y2 = mR * Math.sin(Math.toRadians(vAngle + mVStep));

            for (int hAngle = 0; hAngle <= 360; hAngle += mHStep) {
                double x1 = hR1 * Math.cos(Math.toRadians(hAngle));
                double z1 = hR1 * Math.sin(Math.toRadians(hAngle));

                double x2 = hR2 * Math.cos(Math.toRadians(hAngle));
                double z2 = hR2 * Math.sin(Math.toRadians(hAngle));

                vertList.add((float) x1);
                vertList.add((float) y1);
                vertList.add((float) z1);

                vertList.add((float) x2);
                vertList.add((float) y2);
                vertList.add((float) z2);
            }
        }
        float[] verts = new float[vertList.size()];
        for (int i = 0; i < vertList.size(); i++) {
            verts[i] = vertList.get(i);
        }
        mVertSize = verts.length / 3;
        mVertBuf = ShaderUtils.getFloatBuffer(verts);
    }

    @Override
    protected void initTextureCoord() {
        ArrayList<Float> texCoordList = new ArrayList<>();
        for (int i = 0; i < 180; i += mVStep) {
            float t1 = 1 - i / 180.0f;
            float t2 = 1 - (i + mVStep) / 180.0f;
            for (int j = 0; j <= 360; j += mHStep) {
                float s = 1 - j / 360.0f;

                texCoordList.add(s);
                texCoordList.add(t1);

                texCoordList.add(s);
                texCoordList.add(t2);
            }
        }
        float[] texCoords = new float[texCoordList.size()];
        for (int i = 0; i < texCoordList.size(); i++) {
            texCoords[i] = texCoordList.get(i);
        }
        mTexCoordBuf = ShaderUtils.getFloatBuffer(texCoords);
    }

}
