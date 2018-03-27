package com.linuxpara.agles20tutorials;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.linuxpara.agles20tutorials.camera.CameraActivity;
import com.linuxpara.agles20tutorials.cube.CubeActivity;
import com.linuxpara.agles20tutorials.earth.EarthActivity;
import com.linuxpara.agles20tutorials.triangle.TriangleActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.OnClick;
/**
 * Date: 2018/3/6
 * *************************************************************
 * Auther: 陈占洋
 * *************************************************************
 * Email: zhanyang.chen@gmail.com
 * *************************************************************
 * Description: 主界面Activity
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_triangle,R.id.btn_cube,
            R.id.btn_earth,R.id.btn_pyramid,
            R.id.btn_camera_effect})
    public void onBtnClick(View view){
        switch (view.getId()){
            //三角形
            case R.id.btn_triangle:
                startActivity(new Intent(this, TriangleActivity.class));
                break;
            //立方体
            case R.id.btn_cube:
                startActivity(new Intent(this, CubeActivity.class));
                break;
            //地球
            case R.id.btn_earth:
                startActivity(new Intent(this, EarthActivity.class));
                break;
            //金字塔
            case R.id.btn_pyramid:
                Toast.makeText(this, "金字塔",Toast.LENGTH_SHORT).show();
                break;
            //图片效果
            case R.id.btn_camera_effect:
                startActivity(new Intent(this,CameraActivity.class));
                break;
        }
    }

}
