package com.jackwang.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jackwang.updateapk.UpdateApp;
import com.jackwang.updateapk.UpdateListener;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn1;
    private TextView mTextView;
    //这里随便下载一个应用 QQ极速版 作为实例
    private String apkUrl = "https://qd.myapp.com/myapp/qqteam/QQ_JS/qqlite_4.0.0.1025_537062065.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = findViewById(R.id.btn1);
        mTextView = findViewById(R.id.tv_progress);

        btn1.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                btn1.setEnabled(false);
                UpdateApp.getInstance()
                        .setAutoInatall(true)//是否弹起安装apk
                        .setUpdateListener(new UpdateListener() {
                            @Override
                            public void progress(int progress) {
                                mTextView.setText("进度：" + progress);
                            }

                            @Override
                            public void downFinish() {
                                mTextView.setText("进度：完成" );

                                //不自动安装 可以调用 该方法 安装
//                                UpdateApp.getInstance().startInstall(MainActivity.this);
                            }

                            @Override
                            public void downFail(Throwable throwable) {
                                mTextView.setText("失败" + throwable.getMessage() );
                            }
                        }).downloadApp(this, apkUrl, R.mipmap.ic_launcher);
                break;
        }
    }
}
