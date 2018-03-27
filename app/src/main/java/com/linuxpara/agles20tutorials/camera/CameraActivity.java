package com.linuxpara.agles20tutorials.camera;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import com.linuxpara.agles20tutorials.R;
import com.linuxpara.agles20tutorials.camera.widget.CameraDrawer;
import com.linuxpara.agles20tutorials.camera.widget.CameraView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraActivity extends AppCompatActivity {

    @BindView(R.id.camera_view)
    CameraView mCameraView;
    @BindView(R.id.camera_effect_warm_cool_sb)
    SeekBar mWarmCoolSB;
    private boolean mCapturingVideoStatus;//false停止录制状态，ture开始录制状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

        mWarmCoolSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float strength = (progress - 50) / 50.0f;
                mCameraView.setWarmCoolStrength(strength);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @OnClick({R.id.camera_switch, R.id.camera_capture, R.id.camera_video_capture})
    public void onBtnCtlClick(View view) {
        switch (view.getId()) {
            case R.id.camera_switch:
                mCameraView.switchCamera();
                break;
            case R.id.camera_capture:

                break;
            case R.id.camera_video_capture:
                if (!mCapturingVideoStatus) {
                    Toast.makeText(this, "开始录制视屏", Toast.LENGTH_SHORT).show();
                    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                    String file = dir.getAbsolutePath() + File.separator + "gles20Tutorials" + File.separator + System.currentTimeMillis() + ".mp4";
                    mCameraView.startCaptureVideo(new File(file));
                    mCapturingVideoStatus = !mCapturingVideoStatus;
                } else {
                    Toast.makeText(this, "结束录制视屏", Toast.LENGTH_SHORT).show();
                    mCameraView.stopCaptureVideo();
                    mCapturingVideoStatus = !mCapturingVideoStatus;
                }
                break;
        }
    }

    @OnClick({R.id.camera_effect_original, R.id.camera_effect_gray,
            R.id.camera_effect_negative, R.id.camera_effect_warm_cool,
            R.id.camera_effect_cameo, R.id.camera_effect_carving})
    public void onBtnEffectClick(View view) {
        switch (view.getId()) {
            case R.id.camera_effect_original:
                mCameraView.setEffect(CameraDrawer.Effect.NONE);
                break;
            case R.id.camera_effect_gray:
                mCameraView.setEffect(CameraDrawer.Effect.GRAY);
                break;
            case R.id.camera_effect_negative:
                mCameraView.setEffect(CameraDrawer.Effect.NEGATIVE);
                break;
            case R.id.camera_effect_warm_cool:
                if (mWarmCoolSB.getVisibility() != View.VISIBLE) {
                    mWarmCoolSB.setVisibility(View.VISIBLE);
                }
                mCameraView.setEffect(CameraDrawer.Effect.WARM_COOL);
                break;
            case R.id.camera_effect_cameo:
                mCameraView.setEffect(CameraDrawer.Effect.CAMEO);
                mCameraView.setKernelSize(5);
                break;
            case R.id.camera_effect_carving:
                mCameraView.setEffect(CameraDrawer.Effect.CARVING);
                mCameraView.setKernelSize(5);
                break;
        }
        if (view.getId() != R.id.camera_effect_warm_cool) {
            mWarmCoolSB.setVisibility(View.GONE);
        }
    }

}
