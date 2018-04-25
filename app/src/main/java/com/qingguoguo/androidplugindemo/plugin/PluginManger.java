package com.qingguoguo.androidplugindemo.plugin;

import android.content.Context;

/**
 * @author :qingguoguo
 * @datetime ：2018/4/25
 * @describe :
 */

public class PluginManger {

    public static final void install(Context context, String apkPath) {
        try {
            FixDexManager fixDexManager = new FixDexManager(context);
            fixDexManager.fixDex(apkPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
