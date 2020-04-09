package com.smartism.znzk.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

/**
 * Created by Administrator on 2016/7/13 0013.
 */

public class IntentUtils {
    private static IntentUtils instance = null;

    /**
     * 返回该类的一个实例
     */
    public static IntentUtils getInstance() {
        if (instance == null) {
            synchronized (IntentUtils.class) {
                if (instance == null)
                    instance = new IntentUtils();
            }
        }
        return instance;
    }


    /**
     * 启动某个Activity
     *
     * @param ctx
     * @param cls
     */
    public void startActivity(Context ctx, Class cls) {
        /* new一个Intent对象，并指定class */
        Intent intent = new Intent();
        intent.setClass(ctx, cls);
		/* 调用Activity */
        ctx.startActivity(intent);
    }

    /**
     * 判断是否已经安装某个应用
     *
     * @param packageName 应用程序的包名
     * @return
     */
    public boolean existPackage(Context ctx, String packageName) {
        try {
            PackageManager pm = ctx.getPackageManager();
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 启动某个应用程序
     *
     * @param ctx
     * @param packageName 应用程序的包名
     */
    public void startPackage(Context ctx, String packageName) {
        Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            ctx.startActivity(intent);
        }
    }

    /**
     * 调用系统的分享程序
     *
     * @param ctx
     * @param tip 分享内容
     */
    public void share(Context ctx, String tip) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
//		intent.putExtra( Intent.EXTRA_SUBJECT, "分享" ); //将以彩信形式
        intent.putExtra(Intent.EXTRA_TEXT, tip);
        ctx.startActivity(Intent.createChooser(intent, "分享"));
    }



    /**
     * 打开浏览器
     *
     * @param url
     * @param ctx
     */
    public void startBrowser(String url, Context ctx) {
        Uri uri = Uri.parse(url);
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        ctx.startActivity(it);
    }

    /**
     * 回收该类的数据资源
     */
    public static void recycle()
    {
        instance = null;
    }
}
