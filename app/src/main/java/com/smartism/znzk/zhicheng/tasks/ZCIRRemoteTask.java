package com.smartism.znzk.zhicheng.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
import com.smartism.znzk.zhicheng.iviews.IBaseView;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.smartism.znzk.zhicheng.tasks.GeneralHttpTask.GET_IR_MAC_VALUE_URL;

/*
* AsyncTask不适合长时间、高并发后台任务,高版本串行执行
* */
public class ZCIRRemoteTask extends AsyncTask<Map<String,String>,Void,String>{

    WeakReference<IzcIrRemoteImpl> weakReference ;
    long zhujiID;
    public ZCIRRemoteTask(IzcIrRemoteImpl impl,long id){
        weakReference = new WeakReference<>(impl);
        zhujiID = id;
    }

    @Override
    protected void onPostExecute(String s) {
        if(!MZBaseActivity.isActive((Activity) weakReference.get())){
            return ;
        }
        weakReference.get().hideProgress();
        weakReference.get().handleResult(s);
    }

    @Override
    protected void onPreExecute() {
        if(!MZBaseActivity.isActive((Activity) weakReference.get())){
            return ;
        }
        weakReference.get().showProgress("");
    }

    @Override
    protected String doInBackground(Map<String,String>... maps) {
        List<CommandInfo> commandInfos  = null;
        String deviceMac = "";
        commandInfos = DatabaseOperator.getInstance().queryAllCommands(zhujiID);
        if(commandInfos==null||commandInfos.size()==0){
            return null;
        }

        for(int i=0;i<commandInfos.size();i++){
           CommandInfo temp = commandInfos.get(i);
           if(temp.getCtype().equals("149")){
               deviceMac = temp.getCommand() ;
               break;
           }
        }
        if(deviceMac==null||deviceMac.equals("")){
            return null;
        }
        String result = null ;
        //先从本地读取保存的MAC值
        String realMac = DataCenterSharedPreferences.getInstance((Context) weakReference.get(),DataCenterSharedPreferences.Constant.CONFIG).getString(deviceMac,"");
        if(!realMac.equals("")){
            result =  realMac ;
        }else{
            //做请求
            try {
                OkHttpClient httpClient = new OkHttpClient();
                FormBody.Builder formBody = new FormBody.Builder();
                formBody.add("mac",deviceMac);
                formBody.add("username","深圳市志诚科技有限公司");
                Request request = new Request.Builder()
                        .url(GET_IR_MAC_VALUE_URL)
                        .post(formBody.build())
                        .build();
                Response response = httpClient.newCall(request).execute();
                if (response != null && response.isSuccessful()) {
                    String temp  = response.body().string();//网络请求的一部分，获取服务器的数据
                    JSONObject jsonObject = JSONObject.parseObject(temp);
                    result = jsonObject.getString("mac");
                    //保存一下
                    DataCenterSharedPreferences.getInstance((Context) weakReference.get(),DataCenterSharedPreferences.Constant.CONFIG).putString(deviceMac,result).commit();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static interface IzcIrRemoteImpl extends IBaseView {
        void handleResult(String result);
    }
}
