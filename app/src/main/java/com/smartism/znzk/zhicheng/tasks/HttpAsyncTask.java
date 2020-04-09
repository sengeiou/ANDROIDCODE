package com.smartism.znzk.zhicheng.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.FragmentParentActivity;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.zhicheng.iviews.IBaseView;

import java.lang.ref.WeakReference;

/*
* 默认情况下AsyncTask采用的SerialExecutor进行串行执行任务，当然可以自定义线程池来实现并行执行.
* 采用AsyncTask请求我们自己的Http接口，
* */
public class HttpAsyncTask extends AsyncTask<JSONObject,Void,String>{

    //需要请求的接口
    private static final String Zhuji_SET_URL ="/jdm/s3/d/p/set"; //设置url
    private  static final String Device_History_Command = "/jdm/s3/d/hm";//get history
    private static  final String IR_REMOTE_GET_URL = "/jdm/s3/infr/list";
    private static final String IR_REMOTE_DELETE_URL = "/jdm/s3/infr/del";
    private static final String IR_REMOTE_ADD_URL=  "/jdm/s3/infr/add";//将红外设备与空调进行绑定
    private static final String Zhuji_FIND_URL = "/jdm/s3/d/find";
    private static final String Zhuji_GSM_INIT_STATUS = "/jdm/s3/gsmtel/status" ; //获取gsm设置状态
    private static final String Zhuji_GSM_SETTTING_STATUS = "/jdm/s3/gsmtel/ustatus";//设置gsm启用状态
    private static final String Zhuji_GSM_PHONE_LIST = "/jdm/s3/gsmtel/list"; //获取主机gsm号码
    private static final String Zhuji_GSM_PHONE_ADD = "/jdm/s3/gsmtel/add";//给主机添加gsm号码
    private  static  final String Zhuji_GSM_PHONE_DELETE = "/jdm/s3/gsmtel/del";//删除主机的gsm号码
    private static final String Zhuji_WIRED_ADD_URL = "/jdm/s3/d/cabledadd" ; //主机添加有线防区
    private static final String Zhuji_OWERN_URL = "/jdm/s3/dzj/get";//主机所有者
    private static final String Zhuji_IPC_ADD = "/jdm/s3/ipcs/add";//添加摄像头

    //标识上述接口
    public static final int Zhuji_SET_URL_FLAG = 1 ;
    public static final int IR_REMOTE_GET_URL_FLAG = 3 ;
    public static final int Device_History_Command_FLAG =2 ;
    public static final int IR_REMOTE_DELETE_URL_FLAG=4 ;
    public static final int IR_REMOTE_ADD_URL_FLAG=  5  ;
    public static final int Zhuji_FIND_URL_FLAG= 6 ;
    public static final int Zhuji_GSM_INIT_STATUS_FALG = 7 ;
    public static final int Zhuji_GSM_SETTING_STATUE_FLAG = 8 ;
    public static final int Zhuji_GSM_PHONE_LIST_FLAG = 9 ;
    public  static final int Zhuji_GSM_PHONE_ADD_FLAG = 10 ;
    public static final int Zhuji_GSM_PHONE_DELETE_FLAG = 11 ;
    public  static final int Zhuji_WIRED_ADD_URL_FLAG = 12;
    public static final int Zhuji_OWERN_URL_FLAG = 13 ;
    public static final int Zhuji_IPC_ADD_FLAG = 14;



    //防止内存泄漏
    WeakReference<IHttpResultView> weakReference ;
    int flag = -1 ;
    String server ;
    public HttpAsyncTask(IHttpResultView iHttpResultView,int flag){
        weakReference =new WeakReference<>(iHttpResultView);
        this.flag = flag ;
    }

    //判断View是否被销毁
    boolean isActive(){
        Activity activity = (Activity) weakReference.get();
        if(activity==null||activity.isFinishing()){
            return false ;
        }

        return true ;
    }


    @Override
    protected void onPostExecute(String s) {
        if(!isActive()){
            return ;
        }
        weakReference.get().hideProgress();
        Activity activity = (Activity) weakReference.get();
        if(s.equals("0")){
            //操作成功,不想弹出提示，可以重写success方法
            weakReference.get().success(activity.getResources().getString(R.string.success));
        }else if(s.equals("-1")){
            //参数为空
            weakReference.get().error("");
            Log.d("HttpAsyncTask","操作失败，参数为空");

        }else if(s.equals("-100")){
            //服务器错误
            weakReference.get().error(activity.getResources().getString(R.string.net_error_servererror));
        }
        weakReference.get().setResult(flag,s);
    }

    @Override
    protected String doInBackground(JSONObject... jsonObjects) {
        if(!isActive()){
            return "";
        }
        server = getHttpAddress(flag);
        String result = null;
        if(flag==IR_REMOTE_ADD_URL_FLAG){
            result = HttpRequestUtils.requestoOkHttpPost(server,jsonObjects[0],false, (FragmentParentActivity) weakReference.get());
        }else{
            if(weakReference.get() instanceof ActivityParentActivity){
                result = HttpRequestUtils.requestoOkHttpPost(server,jsonObjects[0],(ActivityParentActivity) weakReference.get());
            }else if(weakReference.get() instanceof FragmentParentActivity){
                result = HttpRequestUtils.requestoOkHttpPost(server,jsonObjects[0],(FragmentParentActivity) weakReference.get());
            }
        }
        return result;
    }

    //将ip地址与http后缀进行连接整合
    String getHttpAddress(int flag){
        String temp   = DataCenterSharedPreferences
                .getInstance((Context) weakReference.get(),DataCenterSharedPreferences.Constant.CONFIG)
                .getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS,"");
        switch (flag){
            case Zhuji_SET_URL_FLAG:
                return temp+Zhuji_SET_URL ;
            case IR_REMOTE_GET_URL_FLAG:
                return temp+IR_REMOTE_GET_URL;
            case IR_REMOTE_DELETE_URL_FLAG:
                return temp+IR_REMOTE_DELETE_URL;
            case IR_REMOTE_ADD_URL_FLAG:
                return temp+IR_REMOTE_ADD_URL;
            case Zhuji_FIND_URL_FLAG:
                return temp+Zhuji_FIND_URL;
            case Zhuji_GSM_INIT_STATUS_FALG:
                return temp+Zhuji_GSM_INIT_STATUS;
            case Zhuji_GSM_SETTING_STATUE_FLAG:
                return temp+Zhuji_GSM_SETTTING_STATUS;
            case Zhuji_GSM_PHONE_LIST_FLAG:
                return temp+Zhuji_GSM_PHONE_LIST;
            case Zhuji_GSM_PHONE_ADD_FLAG:
                return temp+Zhuji_GSM_PHONE_ADD;
            case Zhuji_GSM_PHONE_DELETE_FLAG:
                return temp+Zhuji_GSM_PHONE_DELETE;
            case Zhuji_WIRED_ADD_URL_FLAG:
                return  temp+Zhuji_WIRED_ADD_URL;
            case Zhuji_OWERN_URL_FLAG:
                return temp+Zhuji_OWERN_URL;
            case Zhuji_IPC_ADD_FLAG:
                return temp + Zhuji_IPC_ADD;
        }

        return "";
    }

    @Override
    protected void onPreExecute() {
        if(!isActive()){
            return ;
        }
        //显示进度条
        weakReference.get().showProgress(((Activity) weakReference.get()).getString(R.string.operationing));
    }



    public  interface IHttpResultView extends IBaseView {
        //请求后的结果
        void setResult(int flag,String result);
    }

}
