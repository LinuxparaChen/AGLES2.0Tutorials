package com.linuxpara.agles20tutorials;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_triangle,R.id.btn_cube,
            R.id.btn_globe,R.id.btn_pyramid,
            R.id.btn_picture_effect})
    public void onBtnClick(View view){
        switch (view.getId()){
            //三角形
            case R.id.btn_triangle:
                Toast.makeText(this, "三角形",Toast.LENGTH_SHORT).show();
                break;
            //立方体
            case R.id.btn_cube:
                Toast.makeText(this, "立方体",Toast.LENGTH_SHORT).show();
                break;
            //地球仪
            case R.id.btn_globe:
                Toast.makeText(this, "地球仪",Toast.LENGTH_SHORT).show();
                break;
            //金字塔
            case R.id.btn_pyramid:
                Toast.makeText(this, "金字塔",Toast.LENGTH_SHORT).show();
                break;
            //图片效果
            case R.id.btn_picture_effect:
                Toast.makeText(this, "图片效果",Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
