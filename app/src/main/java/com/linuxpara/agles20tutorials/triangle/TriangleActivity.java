package com.linuxpara.agles20tutorials.triangle;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


import com.linuxpara.agles20tutorials.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Date: 2018/1/15
 * *************************************************************
 * Auther: 陈占洋
 * *************************************************************
 * Email: zhanyang.chen@gmail.com
 * *************************************************************
 * Description: 展示三角形界面
 */
public class TriangleActivity extends AppCompatActivity {

    @BindView(R.id.triangle_gl_view)
    GLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triangle);
        ButterKnife.bind(this);

        mGLView.setEGLContextClientVersion(2);
        mGLView.setRenderer(new TriangleRender(mGLView));
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
