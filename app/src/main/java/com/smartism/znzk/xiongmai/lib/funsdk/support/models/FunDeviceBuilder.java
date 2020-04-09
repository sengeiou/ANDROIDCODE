package com.smartism.znzk.xiongmai.lib.funsdk.support.models;


import com.smartism.znzk.xiongmai.lib.funsdk.support.FunLog;

public class FunDeviceBuilder {

	public static FunDevice buildWith(FunDevType type) {
		if ( FunDevType.EE_DEV_INTELLIGENTSOCKET == type ) {
			// 智能插座
			FunLog.i("build", "a FunDeviceSocket()");
			return new FunDeviceSocket();
		}
		
		FunLog.i("build", "a FunDevice()");
		return new FunDevice();
	}
}
