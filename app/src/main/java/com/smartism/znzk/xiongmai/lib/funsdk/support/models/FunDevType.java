package com.smartism.znzk.xiongmai.lib.funsdk.support.models;

import com.smartism.znzk.R;

public enum FunDevType {

	// 0, 监控设备
	EE_DEV_NORMAL_MONITOR(0,
			R.string.dev_type_monitor,
			R.drawable.ic_launcher),
	// 1, 智能插座
	EE_DEV_INTELLIGENTSOCKET(1,
			R.string.dev_type_intelligentsocket,
			R.drawable.ic_launcher),
	// 2, 情景灯泡
	EE_DEV_SCENELAMP(2,
			R.string.dev_type_scenelamp,
			R.drawable.ic_launcher),
	// 3, 智能灯座
	EE_DEV_LAMPHOLDER(3,
			R.string.dev_type_lampholder,
			R.drawable.ic_launcher),
	// 4, 汽车伴侣
	EE_DEV_CARMATE(4,
			R.string.dev_type_carmate,
			R.drawable.ic_launcher),
	// 5, 大眼睛行车记录仪
	EE_DEV_BIGEYE(5,
			R.string.dev_type_bigeye,
			R.drawable.ic_launcher),
	// 6, 小雨点
	EE_DEV_SMALLEYE(6,
			R.string.dev_type_smalleye,
			R.drawable.ic_launcher),
	// 7, 雄迈摇头机
	EE_DEV_BOUTIQUEROTOT(7, 
			R.string.dev_type_boutiquerotot,
			R.drawable.ic_launcher),
	// 8, 运动摄像机
	EE_DEV_SPORTCAMERA(8, 
			R.string.dev_type_sportcamera,
			R.drawable.ic_launcher),
	// 9, 鱼眼小雨点
	EE_DEV_SMALLRAINDROPS_FISHEYE(9, 
			R.string.dev_type_smallraindrops_fisheye,
			R.drawable.ic_launcher),

    // 21, 智能门铃
    EE_DEV_IDR(21,
            R.string.dev_type_idr,
            R.drawable.ic_launcher),

    // 24, 摄像机
    EE_DEV_CAMERA(24,
            R.string.dev_type_camera,
            R.drawable.ic_launcher),
	
	// 未知设备
	EE_DEV_UNKNOWN(-1, 
			R.string.dev_type_unknown,
			R.drawable.ic_launcher);

	private int devIndex;
	private int devResId;
	private int drawResId;

	FunDevType(int id, int resid, int iconid) {
		this.devIndex = id;
		this.devResId = resid;
		this.drawResId = iconid;
	}

	/**
	 * 获取设备类型的字符串ID
	 * 
	 * @return 设备类型字符串ID
	 */
	public int getTypeStrId() {
		return this.devResId;
	}

	/**
	 * 获取设备图标的资源ID
	 * @return
	 */
	public int getDrawableResId() {
		return this.drawResId;
	}

	
	/**
	 * 获取设备类型的索引号
	 * 
	 * @return
	 */
	public int getDevIndex() {
		return this.devIndex;
	}
	
	public static FunDevType getType(int index) {
		for (FunDevType devType : FunDevType.values()) {
			if (devType.getDevIndex() == index) {
				return devType;
			}
		}
		return EE_DEV_NORMAL_MONITOR;
	}
}
