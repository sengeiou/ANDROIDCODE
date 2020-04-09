package com.smartism.znzk.zhicheng.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.zhicheng.iviews.IBaseView;

import java.lang.ref.WeakReference;
import java.util.List;

/*
* 专门用于从数据库加载CommandsInfo,通用型的Task，需要实现ILoadCommands接口
*
* */
public class LoadCommandsInfo extends AsyncTask<Long,Void,List<CommandInfo>>{

    WeakReference<ILoadCommands> weakReference  ;

    public LoadCommandsInfo(ILoadCommands loadCommands){
        weakReference = new WeakReference<>(loadCommands);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //由于从数据加载，就不显示进度条了
        if(!isActity()){
            return ;
        }
        weakReference.get().showProgress("");
    }

    @Override
    protected void onPostExecute(List<CommandInfo> commandInfos) {
        super.onPostExecute(commandInfos);
        if(!isActity()){
            return ;
        }
        weakReference.get().hideProgress();
        if(commandInfos==null){
            weakReference.get().error("");
            Log.d("LoadCommandsInfo","error");
        }else{
            weakReference.get().success("");
            Log.d("LoadCommandsInfo","success");
        }
        weakReference.get().loadCommands(commandInfos);
    }

    boolean isActity(){
        Activity activity = (Activity) weakReference.get();
        if(activity==null||activity.isFinishing()){
            return false;
        }
        return true;
    }

    @Override
    protected List<CommandInfo> doInBackground(Long... integers) {
        List<CommandInfo> result  = null;
        try{
            result = DatabaseOperator.getInstance().queryAllCommands(integers[0]);
        }catch (Exception e){
            return  null ;
        }
        return result;
    }


    public  interface ILoadCommands extends IBaseView {

        void loadCommands(List<CommandInfo> lists);
    }
}
