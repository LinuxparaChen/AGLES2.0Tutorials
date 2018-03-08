package com.linuxpara.agles20tutorials.util;

import android.content.res.Resources;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Date: 2018/1/15
 * *************************************************************
 * Auther: 陈占洋
 * *************************************************************
 * Email: zhanyang.chen@gmail.com
 * *************************************************************
 * Description: 着色器相关的工具类
 */

public class ShaderUtils {
    /**
     * 获取浮点类型缓存
     *
     * @param buffer
     * @return
     */
    public static FloatBuffer getFloatBuffer(float[] buffer) {

        FloatBuffer floatBuffer = ByteBuffer.allocateDirect(buffer.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(buffer);
        floatBuffer.position(0);

        return floatBuffer;
    }

    /**
     * 获取int类型缓存
     *
     * @param buffer
     * @return
     */
    public static IntBuffer getIntBuffer(int[] buffer) {

        IntBuffer intBuffer = ByteBuffer.allocateDirect(buffer.length * 4)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer()
                .put(buffer);
        intBuffer.position(0);

        return intBuffer;
    }

    /**
     * 获取字节类型缓存
     *
     * @param buffer
     * @return
     */
    public static ByteBuffer getByteBuffer(byte[] buffer) {

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffer.length)
                .order(ByteOrder.nativeOrder())
                .put(buffer);
        byteBuffer.position(0);

        return byteBuffer;
    }

    /**
     * 生成纹理Id
     * @return
     */
    public static int genTextureId(){
        int[] textures = new int[1];
        GLES20.glGenTextures(1,textures,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[0]);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);

        return textures[0];
    }

    /**
     * 生成OES扩展纹理Id
     * @return
     */
    public static int genOESTextureId(){
        int[] textures = new int[1];
        GLES20.glGenTextures(1,textures,0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textures[0]);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);

        return textures[0];
    }

    /**
     * 从资产目录中获取shader代码
     *
     * @param fileName
     * @return
     */
    public static String getCodeFromAsset(String fileName, Resources resources) {

        InputStream in = null;
        ByteArrayOutputStream baos = null;
        String result = "";

        try {
            in = resources.getAssets().open(fileName);

            baos = new ByteArrayOutputStream();

            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = in.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            String source = baos.toString("UTF-8");
            result = source.replaceAll("\\r\\n", "\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }
    }

    /**
     * 创建着色器程序。
     *
     * @param verCode
     * @param fragCode
     * @return
     */
    public static int createProgram(String verCode, String fragCode) {
        //加载顶点着色器代码，获取顶点着色器。
        int verShader = loadShader(GLES20.GL_VERTEX_SHADER, verCode);
        if (verShader == 0) {
            return 0;
        }
        //加载片元着色器代码，获取片元着色器。
        int fragShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragCode);
        if (fragShader == 0) {
            return 0;
        }
        //创建着色器程序
        int program = GLES20.glCreateProgram();
        if (program != 0) {
            //将顶点着色器依赖到创建好的着色器程序上。
            GLES20.glAttachShader(program, verShader);
            //检查错误
            checkGLError("glAttachShader");
            //将片元着色器依赖到创建好的着色器程序上。
            GLES20.glAttachShader(program, fragShader);
            //检查错误
            checkGLError("glAttachShader");
            //链接着色器程序。
            GLES20.glLinkProgram(program);

            int[] linked = new int[1];
            //获取着色器程序信息检查是否出错。
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linked, 0);
            if (linked[0] == 0) {
                Log.e("ES20_ERROR", "Could not link program: ");
                Log.e("ES20_ERROR", GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        //删除着色器
        GLES20.glDeleteShader(verShader);
        GLES20.glDeleteShader(fragShader);
        return program;
    }

    /**
     * 加载着色器代码
     *
     * @param shaderType 着色器类型，GLES20.GL_VERTEX_SHADER(顶点着色器)
     *                   GLES20.GL_FRAGMENT_SHADER(片元着色器）
     * @param shaderCode 着色器代码
     * @return
     */
    private static int loadShader(int shaderType, String shaderCode) {
        //创建shader索引
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            //加载shader代码
            GLES20.glShaderSource(shader, shaderCode);
            //编译shader代码
            GLES20.glCompileShader(shader);
            int[] complied = new int[1];
            //获取shader编译信息
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, complied, 0);
            if (complied[0] == 0) {
                Log.e("ES20_ERROR", "Could not compile shader " + shaderType + ":");
                Log.e("ES20_ERROR", GLES20.glGetShaderInfoLog(shader));
                //出错后，删除着色器。
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;

    }

    /**
     * 检查错误
     *
     * @param op
     */
    public static void checkGLError(String op) {

        int error = 0;
        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("ES20_ERROR", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }

    }

}
