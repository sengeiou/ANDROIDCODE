package com.smartism.znzk.util;

import android.content.Context;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;
/**
 * 日志输出工具，需要设置是否为debug
 * @author Administrator
 *
 */
public class LogUtil {
	public static String TAG_ZNZK = "smart-ism.com";
	public static boolean isDebug = true;
	public static void i(String tag,String msg){
		if (isDebug) {
			Log.i(tag, msg);
		}
	}
	public static void i(String tag,String msg,Throwable tr){
		if (isDebug) {
			Log.i(tag, msg,tr);
		}
	}
	/**
	 * 本地日志输出并上传到服务器
	 * @param context
	 * @param tag
	 * @param msg
	 */
	public static void e(Context context,String tag,String msg){
		Log.e(tag, msg);
		MobclickAgent.reportError(context, msg);
	}
	/**
	 * 本地错误日志输出并上传到服务器
	 * @param context
	 * @param tag
	 * @param msg
	 * @param tr
	 */
	public static void e(Context context,String tag,String msg,Throwable tr){
		Log.e(tag, msg,tr);
		MobclickAgent.reportError(context, tr);
	}


	public static void i(String msg){
		i(TAG_ZNZK,msg);
	}
	public static void i(String msg,Throwable tr){
		i(TAG_ZNZK,msg,tr);
	}
	/**
	 * 本地日志输出并上传到服务器
	 * @param context
	 * @param msg
	 */
	public static void e(Context context,String msg){
		e(context,TAG_ZNZK,msg);
	}
	/**
	 * 本地错误日志输出并上传到服务器
	 * @param context
	 * @param msg
	 * @param tr
	 */
	public static void e(Context context,String msg,Throwable tr){
		e(context,TAG_ZNZK,msg,tr);
	}
}
