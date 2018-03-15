package com.linuxpara.agles20tutorials.earth;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.linuxpara.agles20tutorials.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EarthActivity extends AppCompatActivity {

    @BindView(R.id.earth_gl_view)
    GLSurfaceView mGLView;
    private EarthRender mEarthRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earth);
        ButterKnife.bind(this);

        mEarthRender = new EarthRender(mGLView);
        mGLView.setEGLContextClientVersion(2);
        mGLView.setRenderer(mEarthRender);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

}
