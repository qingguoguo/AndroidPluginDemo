package com.qingguoguo.androidplugindemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.morgoo.droidplugin.pm.PluginManager;
import com.morgoo.helper.Log;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    String apkPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "yaoyiyao.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void click1(View view) {
        //PluginManger.install(this, apkPath);
        try {
            int result = PluginManager.getInstance().installPackage(apkPath, 0);
            Log.e("MainActivity","result:"+result);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void click2(View view) {
//        Intent intent = new Intent(this, TestActivity.class);
//        intent.setClassName(getPackageName(), "com.qingguoguo.maraquetext.MainActivity");
//        intent.putExtra("qingguoguo", "哈哈，成功啦");
//        startActivity(intent);

        PackageManager pm = getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        String packageName = info.packageName;
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPermission();
    }

    int REQUEST_EXTERNAL_STORAGE = 1;

    private void initPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
