package com.smartism.znzk.global;

import android.os.Environment;

import java.io.File;

/**
 * Created by dxs on 2015/8/28.
 */
public class AppConfig {
	 /**
     * 分辨率
     */
    public static int VideoMode=2;//0:流畅 1:高清 2标清
    /**
     * 调试相关参数，正式版记得改为合理值
     */
    public static class DeBug{
        public static final boolean isWrightAllLog=false;//是否写所有日志到SD卡
        public static final boolean isWrightErroLog=false;//是否记录错误日志到SD卡
    }

	/**
	 * 正式版参数
	 */
	public static class Relese {
//		public static final String VERSION = Config.AppConfig.VERSION;
		public static final String APTAG = "GW_IPC_";
		public static final String PREPOINTPATH = Environment
				.getExternalStorageDirectory().getPath()
				+ File.separator
				+ "prepoint" + File.separator + NpcCommon.mThreeNum;
		public static final String SCREENSHORT = Environment
				.getExternalStorageDirectory().getPath()
				+ File.separator
				+ "screenshot";
	}

}
