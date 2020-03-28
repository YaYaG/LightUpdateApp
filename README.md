更新效果：
<br/>
<img src="https://github.com/YaYaG/LightUpdateApp/blob/master/img/hello.gif" width="180" height="320" align="middle" />

####集成方式：
1 引入 jitpack.

```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

```
2 添加依赖
```
dependencies {
	        implementation 'com.github.YaYaG:LightUpdateApp:1.0.0'
}

```

使用方式：


1.在res中创建xml文件夹，并创建file_paths.xml:

```
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools"
    tools:ignore="MissingDefaultResource">
    <paths>
        <external-cache-path name="download" path=""/>
        <external-path name="download" path="" />
    </paths>
</resources>

```
2.在AndroidManifest.xml中配置：
```
<provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.provider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
```

3.代码实现：

```
                    //这里随便下载一个应用 QQ极速版 作为实例
                    private String apkUrl = "https://qd.myapp.com/myapp/qqteam/QQ_JS/qqlite_4.0.0.1025_537062065.apk";
                    ....
                    
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

```



