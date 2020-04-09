package com.smartism.znzk.zhicheng.tasks;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.FragmentParentActivity;
import com.smartism.znzk.activity.device.RqzjDeviceInfoActivity;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.HistoryCommandInfo;
import com.smartism.znzk.util.*;
import com.smartism.znzk.zhicheng.iviews.IBaseView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.smartism.znzk.activity.device.DeviceInfoActivity.getWeek;

public class LoadHistoryAsyncTask extends AsyncTask<JSONObject,Void, List<HistoryCommandInfo>>{

    WeakReference<ILoadHistoryIterface> mHistoryView ;
    long id   ;
    int totalCount = 0 ;
    public LoadHistoryAsyncTask(ILoadHistoryIterface views,long id){
        mHistoryView = new WeakReference<>(views);
        this.id = id ;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onPostExecute(List<HistoryCommandInfo> historyCommandInfos) {
        if(!isActive()){
            return ;
        }
        Context context = (Context) mHistoryView.get();
        if(historyCommandInfos!=null){
            mHistoryView.get().success("");
            Log.d("LoadHistoryAsyncTask","刷新"+historyCommandInfos.size()+"条记录");
        }else{
            mHistoryView.get().error(context.getResources().getString(R.string.load_failed));
            Log.d("LoadHistoryAsyncTask","加载失败");
        }
        mHistoryView.get().showHistory(historyCommandInfos,totalCount);
    }

    @Override
    protected List<HistoryCommandInfo> doInBackground(JSONObject... jsonObjects) {
        if(!isActive()){
            return null;
        }
        List<HistoryCommandInfo> histories = null ;
        String result = HttpRequestUtils.requestoOkHttpPost(Util.getUrl((Context) mHistoryView.get(),"/jdm/s3/d/hm"),jsonObjects[0],(FragmentParentActivity)mHistoryView.get());
        if(result.length()>4){
            //成功了，请求
            List<JSONObject> commands = new ArrayList<JSONObject>();
            JSONObject resultJson = null;
            try {
                resultJson = JSON.parseObject(result);
            } catch (Exception e) {
                return histories;
            }
            JSONArray array = resultJson.getJSONArray("result");
            if (array != null && !array.isEmpty()) {
                for (int j = 0; j < array.size(); j++) {
                    commands.add(array.getJSONObject(j));
                }
            }
            totalCount = resultJson.getIntValue("allCount");
            histories = new ArrayList<>();
            for (int i = 0; i < commands.size(); i++) {
                HistoryCommandInfo info = new HistoryCommandInfo();
                JSONObject object1 = commands.get(i);
                info.setCommand(object1.getString("deviceCommand"));
                info.setOpreator(object1.getString("send"));
                String parms1 = "yyyy:MM:dd:HH:mm:ss";
                Date date = object1.getDate("deviceCommandTime");
                String hour = new SimpleDateFormat(parms1).format(date.getTime());

                info.setDate(hour);
                info.setDayOfWeek(getWeek(date));
                histories.add(info);

                //清空未读消息数
                ContentValues values = new ContentValues();
                values.put("nr", 0); // 未读消息数
                DatabaseOperator.getInstance().getWritableDatabase().update(
                        "DEVICE_STATUSINFO", values, "id = ?", new String[]{String.valueOf(id)});
            }
        }
        return histories;
    }


    //判断View是否被销毁
    boolean isActive(){
        Activity activity = (Activity) mHistoryView.get();
        if(activity==null||activity.isFinishing()){
            return false ;
        }

        return true ;
    }

    public static interface ILoadHistoryIterface extends IBaseView {

        void showHistory(List<HistoryCommandInfo> data,int totalCount);
    }
}
