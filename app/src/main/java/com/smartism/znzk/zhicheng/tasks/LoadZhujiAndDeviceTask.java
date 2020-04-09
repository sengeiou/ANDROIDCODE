package com.smartism.znzk.zhicheng.tasks;

import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.os.Handler;
import android.util.Log;

import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.zhicheng.iviews.ILoadZhujiAndDeviceOperator;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/*从数据库加载主机与设备信息-异步
    调用查询方法，提供实现ILoadResult对象，
    ILoadResult中的loadResult方法最终会在主线程调用。

    多线程中，调用同一个LoadZhujiAndDeviceTask对象访问数据库，可能导致线程安全问题，不要
    在多线程中调用同一个LoadZhujiAndDeviceTask对象查询数据。

*/
public class LoadZhujiAndDeviceTask {

    public  final static String LOG_TAG = "LoadZhujiAndDeviceTask";
    //从数据库加载
    private static   ILoadZhujiAndDeviceOperator  sOperator = new GetZhujiAndDeviceOperator();
    private static int SEND_LOAD_MESSAGE = 0X94;



    //在主线程进行结果回调
    private static Handler mHandler  = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            if(SEND_LOAD_MESSAGE==msg.what){
                LoadZhujiAndDeviceResult mResult = (LoadZhujiAndDeviceResult) msg.obj;
                ILoadResult loadResult = mResult.mTask.mLoadResult;
                if(loadResult!=null){
                    Log.d(LOG_TAG,"loadResult不为null");
                    loadResult.loadResult(mResult.result);
                }else{
                    Log.d(LOG_TAG,"loadResult为null");
                }
                mResult.mTask.mLoadResult=null ;
                msg.obj = null;
            }

        }
    };

    //加载结果封装
    private static class LoadZhujiAndDeviceResult<Result>{
        LoadZhujiAndDeviceTask mTask;
        Result result ;
    }

    //需要数据实现的接口
    public interface ILoadResult<T>{
        void loadResult(T result);
    }


    //防止内存泄漏,但是在内存小的设备
 //   private WeakReference<ILoadResult> mLoadResult ;
    private ILoadResult mLoadResult ;
    //通过主机设备id查询设备信息
    public void queryDeviceInfoByDevice(final long deviceId, ILoadResult<DeviceInfo> result){
        setILoadResult(result);
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                LoadZhujiAndDeviceResult<DeviceInfo> loadResult = new LoadZhujiAndDeviceResult<>();
                loadResult.result = sOperator.loadDeviceInfoByDeviceId(deviceId);
                loadResult.mTask = LoadZhujiAndDeviceTask.this ;
                Message message  =   new Message();
                message.what = SEND_LOAD_MESSAGE;
                message.obj = loadResult ;
                mHandler.sendMessage(message);
            }
        });

    }

    //查询主机的ID
    public void queryDeviceInfosByZhuji(final long zhujiId,ILoadResult<List<DeviceInfo>> result){
        setILoadResult(result);
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                LoadZhujiAndDeviceResult<List<DeviceInfo>> loadResult = new LoadZhujiAndDeviceResult<>();
                loadResult.result = sOperator.loadDeviceInfosByZhujiId(zhujiId);
                loadResult.mTask = LoadZhujiAndDeviceTask.this ;
                Message message  =   new Message();
                message.what = SEND_LOAD_MESSAGE;
                message.obj = loadResult ;
                mHandler.sendMessage(message);
            }
        });
    }

    //通过主机id查询主机
    public void queryZhujiInfoByZhuji(final long zhujiId,ILoadResult<ZhujiInfo> result){
        setILoadResult(result);
        Log.d(LOG_TAG,"queryZhujiInfoByZhuji:"+mLoadResult);
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                LoadZhujiAndDeviceResult<ZhujiInfo> loadResult = new LoadZhujiAndDeviceResult<>();
                loadResult.result = sOperator.loadZhujiByZhujiId(zhujiId);
                loadResult.mTask = LoadZhujiAndDeviceTask.this ;
                Message message  =   new Message();
                message.what = SEND_LOAD_MESSAGE;
                message.obj = loadResult ;
                mHandler.sendMessage(message);
            }
        });
    }

    //查询当前用户所有的主机
    public void queryZhujiInfos(ILoadResult<List<ZhujiInfo>> result){
        setILoadResult(result);
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                LoadZhujiAndDeviceResult<List<ZhujiInfo>> loadResult = new LoadZhujiAndDeviceResult<>();
                loadResult.result = sOperator.loadZhujis();
                loadResult.mTask = LoadZhujiAndDeviceTask.this ;
                Message message  =   new Message();
                message.what = SEND_LOAD_MESSAGE;
                message.obj = loadResult ;
                mHandler.sendMessage(message);
            }
        });
    }

    //通过masterId查询主机信息-异步
    public void queryZhujiInfoByMasterId(final String masterId, ILoadResult<ZhujiInfo> result){
        setILoadResult(result);
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                LoadZhujiAndDeviceResult<ZhujiInfo> loadResult = new LoadZhujiAndDeviceResult<>();
                loadResult.result = sOperator.loadZhujiByMasterId(masterId);
                loadResult.mTask = LoadZhujiAndDeviceTask.this ;
                Message message  =   new Message();
                message.what = SEND_LOAD_MESSAGE ;
                message.obj = loadResult ;
                mHandler.sendMessage(message);
            }
        });
    }

    public void queryAllCommandInfo(final long did, ILoadResult<List<CommandInfo>> result){
        setILoadResult(result);
        Log.d(LOG_TAG,"queryAllCommandInfo:"+mLoadResult);
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                LoadZhujiAndDeviceResult<List<CommandInfo>> loadResult = new LoadZhujiAndDeviceResult<>();
                loadResult.result = sOperator.loadCommandInfo(did);
                loadResult.mTask = LoadZhujiAndDeviceTask.this ;
                Message message  =  new Message();
                message.what = SEND_LOAD_MESSAGE ;
                message.obj = loadResult ;
                mHandler.sendMessage(message);
            }
        });
    }

    private void setILoadResult(ILoadResult result){
        mLoadResult = result;
    }
}
