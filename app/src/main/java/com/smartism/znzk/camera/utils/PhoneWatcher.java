package com.smartism.znzk.camera.utils;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class PhoneWatcher {
	Context mContext;
	boolean isWatchering;
	OnCommingCallListener onCommingCallListener;
	public PhoneWatcher(Context context){
		this.mContext = context;
	}
	
	public void startWatcher(){
	    TelephonyManager telephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);  
	    telephonyManager.listen(new PhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);
	}
	
	public void stopWatcher(){
	    onCommingCallListener = null;
	}
	
	private class PhoneListener extends PhoneStateListener {  
        @Override  
        public void onCallStateChanged(int state, String incomingNumber) {  
            super.onCallStateChanged(state, incomingNumber);  
              
            switch (state) {  
            case TelephonyManager.CALL_STATE_RINGING://来电状态  
                if(null!=onCommingCallListener){
                	onCommingCallListener.onCommingCall();
                }
                break;  
            case TelephonyManager.CALL_STATE_OFFHOOK://接听状态  
                break;
            case TelephonyManager.CALL_STATE_IDLE://挂断后回到空闲状态  
                break;
                  
            default:  
                break;  
            }  
        }  
          
    } 
	
	
	
	public void setOnCommingCallListener(OnCommingCallListener onCommingCallListener) {
		this.onCommingCallListener = onCommingCallListener;
	}

	public interface OnCommingCallListener{
		public void onCommingCall();
	}
}
