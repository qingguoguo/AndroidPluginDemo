package com.qingguoguo.androidplugindemo.plugin;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.BaseDexClassLoader;

/**
 * @author :qingguoguo
 * @datetime ：2018/3/27
 * @describe :
 */

public class FixDexManager {

    private Context mContext;
    private File mDexDir;
    public final static String TAG = FixDexManager.class.getSimpleName();

    public FixDexManager(Context context) {
        this.mContext = context;
        this.mDexDir = context.getDir("odex", Context.MODE_PRIVATE);
    }

    /**
     * 修复dex包
     *
     * @param srcPath
     */
    public void fixDex(String srcPath) throws Exception {
        //2.获取下载好的补丁的dexElements  Element[] applicationDexElements;
        //2.1 copy到系统能够访问的 dex目录下
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            throw new FileNotFoundException(srcPath);
        }

        File targetFile = new File(mDexDir, srcFile.getName());

        if (targetFile.exists()) {
//            Log.d(TAG, "patch [" + srcPath + "] has be loaded.");
//            return;
            targetFile.delete();
        }

        copyFile(srcFile, targetFile);

        //2.2 ClassLoader读取fixDex路径，为什么要加入到集合，可能一启动就需要加载fixDex文件修复 加载多个
        List<File> fixDexFiles = new ArrayList<>();
        fixDexFiles.add(targetFile);
        fixDexFiles(fixDexFiles);
    }

    /**
     * 合并数组
     *
     * @param arrayLhs 前面的数组
     * @param arrayRhs 后面的数组
     */
    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> componentType = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = i + Array.getLength(arrayRhs);
        Object newArray = Array.newInstance(componentType, j);
        for (int k = 0; k < j; k++) {
            if (k < i) {
                Array.set(newArray, k, Array.get(arrayLhs, k));
            } else {
                Array.set(newArray, k, Array.get(arrayRhs, k - i));
            }
        }
        return newArray;
    }

    /**
     * 从classloader中获取DexElement
     *
     * @param classLoader
     * @return
     */
    private Object getDexElementsByClassLoader(ClassLoader classLoader) throws NoSuchFieldException, IllegalAccessException {
        //1.先获取PathList
        Field pathListFd = BaseDexClassLoader.class.getDeclaredField("pathList");
        pathListFd.setAccessible(true);
        //返回指定对象上此 Field 表示的字段的值
        Object pathList = pathListFd.get(classLoader);

        //2.获取dexElements
        Field dexElements = pathList.getClass().getDeclaredField("dexElements");
        dexElements.setAccessible(true);

        return dexElements.get(pathList);
    }

    /**
     * 把dexElements注入到ClassLoader中
     *
     * @param classLoader
     * @param dexElements
     */
    private void injectElements(ClassLoader classLoader, Object dexElements) throws Exception {
        //1.先获取PathList
        Field pathListFd = BaseDexClassLoader.class.getDeclaredField("pathList");
        pathListFd.setAccessible(true);
        //返回指定对象上此 Field 表示的字段的值
        Object pathList = pathListFd.get(classLoader);

        //2.获取dexElements
        Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);
        dexElementsField.set(pathList, dexElements);
    }

    /**
     * copy file copy阿里热修复PatchManager copyFile方法
     *
     * @param src  source file
     * @param dest target file
     * @throws IOException
     */
    public static void copyFile(File src, File dest) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            if (!dest.exists()) {
                dest.createNewFile();
            }
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dest).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    /**
     * 加载全部的修复包
     */
    public void loadFixDex() throws Exception {
        File[] dexFiles = mDexDir.listFiles();
        List<File> fixDexFiles = new ArrayList<>();

        for (File dexFile : dexFiles) {
            if (dexFile.exists() && dexFile.getName().endsWith(".dex")) {
                fixDexFiles.add(dexFile);
            }
        }
        fixDexFiles(fixDexFiles);
    }

    /**
     * 修复fixDexFiles
     *
     * @param fixDexFiles
     */
    private void fixDexFiles(List<File> fixDexFiles) throws Exception {
        //1.获取App已经运行的dexElements
        ClassLoader applicationClassLoader = mContext.getClassLoader();
        Object applicationDexElements = getDexElementsByClassLoader(applicationClassLoader);

        File optimizedDirectory = new File(mDexDir, "odex");
        if (!optimizedDirectory.exists()) {
            optimizedDirectory.mkdirs();
        }
        //修复
        for (File fixDexFile : fixDexFiles) {
            // String dexPath,dex路径
            // File optimizedDirectory,解压路径
            // String librarySearchPath, .so文件的位置
            // ClassLoader parent
            ClassLoader fixDexClassLoader = new BaseDexClassLoader(
                    fixDexFile.getAbsolutePath(),
                    optimizedDirectory,
                    null,
                    applicationClassLoader);

            Object fixDexElements = getDexElementsByClassLoader(fixDexClassLoader);

            //3. 把补丁的dexElements插到在运行的dexElement前面
            //3.1 合并数组
            applicationDexElements = combineArray(fixDexElements, applicationDexElements);
        }
        //3.2 把合并的数组注入到原来的类中
        injectElements(applicationClassLoader, applicationDexElements);
    }
}
