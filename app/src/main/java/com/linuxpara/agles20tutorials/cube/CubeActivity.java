package com.linuxpara.agles20tutorials.cube;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.linuxpara.agles20tutorials.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

public class CubeActivity extends AppCompatActivity {

    @BindView(R.id.cube_gl_view)
    GLSurfaceView mGLView;
    private float mPreX;
    private float mPreY;
    private CubeRender mCubeRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cube);
        ButterKnife.bind(this);

        mCubeRender = new CubeRender(mGLView);
        mGLView.setEGLContextClientVersion(2);
        mGLView.setRenderer(mCubeRender);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    @OnClick({R.id.cube_x_rotate, R.id.cube_y_rotate})
    public void onBtnClick(View view) {
        switch (view.getId()) {
            case R.id.cube_x_rotate:
                mCubeRender.setRAxis(CubeRender.RAxis.X);
                break;
            case R.id.cube_y_rotate:
                mCubeRender.setRAxis(CubeRender.RAxis.Y);
                break;
        }
    }

    @OnTouch(R.id.cube_gl_view)
    public boolean onGLViewTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPreX = event.getX();
                mPreY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                mCubeRender.rotate(event.getX() - mPreX, event.getY() - mPreY);
                mPreX = event.getX();
                mPreY = event.getY();
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        return true;
    }
}
