package com.smartism.znzk.zhicheng.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;
import com.alibaba.fastjson.JSONObject;
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

//面向不是我们自己的http接口请求-AysncTask
public class GeneralHttpTask extends AsyncTask<Map<String,String>,Void, String>{

    public static final String GET_BRAND_KEY_EVENT_URL = "http://www.huilink.com.cn/dk2018/keyevent.asp?"; //获取特定遥控器按键
    public static final String GET_BRAND_KEY_LIST_URL = "http://www.huilink.com.cn/dk2018/getkeylist.asp?";//获取某一种空调牌子下的遥控器支持的按键
    public static final String GET_IR_MAC_VALUE_URL = "http://www.huilink.com.cn/dk2018/macreg.asp?";//获取MAC值
    public static final  String GET_BRAND_REMOTE_CONTROL = "http://www.huilink.com.cn/dk2018/getmodellist.asp?";  //请求某一种品牌下所有的遥控器
    public static final String GET_SMART_MATCH_URL = "http://www.huilink.com.cn/dk2018/getrid.asp?";//一键匹配

    WeakReference<ILoadARKeysImpl> mActivity ;
    String url ;
    private String mProgressText ="";
    public GeneralHttpTask(ILoadARKeysImpl activity, String url)
    {
        mActivity = new WeakReference<>(activity);
        this.url = url;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(!MZBaseActivity.isActive((Activity) mActivity.get())){
            cancel(true);//取消
            return ;
        }
        mActivity.get().showProgress(mProgressText);
    }

    public void setProgressText(String mProgressText) {
        this.mProgressText = mProgressText;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(!MZBaseActivity.isActive((Activity) mActivity.get())){
            return ;
        }
        mActivity.get().hideProgress();
        mActivity.get().getRequestResult(s);
    }

    @Override
    protected String doInBackground(Map<String, String>... maps) {
        if(!MZBaseActivity.isActive((Activity) mActivity.get())){
            return null;
        }
        String result = null ;
        try {
            List<String> keys = new ArrayList<>(maps[0].keySet());
            OkHttpClient httpClient = new OkHttpClient();
            FormBody.Builder formBody = new FormBody.Builder();
            for(int i=0;i<keys.size();i++){
                formBody.add(keys.get(i),maps[0].get(keys.get(i)));
            }
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody.build())
                    .build();
            //同步请求x
            Response response = httpClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                 result = response.body().string();//网络请求的一部分，获取服务器的数据
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public  interface ILoadARKeysImpl extends IBaseView {
        //请求结果回调
        void getRequestResult(String results);
    }
}