package com.qingguoguo.androidplugindemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         *  Caused by: java.lang.IllegalArgumentException:
         *  android.content.pm.PackageManager$NameNotFoundException:
         *  ComponentInfo {com.darren.androidplugindemo/com.qinguoguo.androidplugindemo.TestActivity}
         */

        setContentView(R.layout.activity_test);
    }
}

