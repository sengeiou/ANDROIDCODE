package com.smartism.znzk.util.yaokan;

import android.content.Context;
import android.os.Handler;

import com.smartism.znzk.domain.yankan.MatchRemoteControlResult;

public interface YkanIRInterface {
	
	public void getDeviceType(Handler handler);
	
	public void getBrandsByType(int type, Handler handler);
	
	public MatchRemoteControlResult getRemoteMatched(int type, int bid, Handler handler);
	
	public void getRemoteDetailsHashMap(Context context, String rcID, String name, Handler handler) ;

	public void registerDevice(Handler handler);
	
}
