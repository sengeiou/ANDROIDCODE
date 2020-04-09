package com.smartism.znzk.communication.service;


import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import me.tatarka.support.job.JobParameters;
import me.tatarka.support.job.JobService;

public class KeepLiveService extends JobService {
    private final static String TAG = KeepLiveService.class.getCanonicalName();

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG,"KeepliveService onStartJob ..................................");
        try {
            //调用一遍start即可
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), CoreService.class);
            startService(intent);
        }catch(Exception ex){
            //oppo 手机有些会调用异常
//            Toast.makeText(mContext,"service start failed",Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
