package com.qingguoguo.androidplugindemo;

import com.morgoo.droidplugin.PluginApplication;

public class BaseApplication extends PluginApplication {

    @Override
    public void onCreate() {
        super.onCreate();
//        HookStartActivityUtils hookStartActivityUtil =
//                new HookStartActivityUtils(this,ProxyActivity.class);
//        try {
//            hookStartActivityUtil.hookStartActivity();
//            hookStartActivityUtil.hookLaunchActivity();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
