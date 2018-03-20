package com.linuxpara.agles20tutorials.camera.widget.camera;

/**
 * Date: 2018/3/16
 * *************************************************************
 * Auther: 陈占洋
 * *************************************************************
 * Email: zhanyang.chen@gmail.com
 * *************************************************************
 * Description:
 */

public class Size {

    private int mHeight;
    private int mWidth;

    public Size(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setHeight(int height) {
        this.mHeight = height;
    }

    public int getHeight() {
        return mHeight;
    }

    @Override
    public String toString() {
        return "width × height =  " + mWidth + " × " + mHeight;
    }
}
