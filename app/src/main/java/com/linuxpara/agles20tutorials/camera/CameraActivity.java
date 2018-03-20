package com.linuxpara.agles20tutorials.camera;

import android.Manifest;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.linuxpara.agles20tutorials.R;
import com.linuxpara.agles20tutorials.camera.widget.CameraView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraActivity extends AppCompatActivity {

    @BindView(R.id.camera_view)
    CameraView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.CAMERA)
                .subscribe(grant -> {
                    if (grant) {
                        init();
                    } else {
                        finish();
                    }
                });
    }

    private void init() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick({R.id.camera_switch,R.id.camera_capture,R.id.camera_video_capture})
    public void onBtnClick(View view){
        switch (view.getId()){
            case R.id.camera_switch:
                mCameraView.switchCamera();
                break;
            case R.id.camera_capture:

                break;
            case R.id.camera_video_capture:

                break;
        }
    }
}
