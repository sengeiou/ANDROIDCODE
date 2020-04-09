package com.smartism.znzk.util;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.smartism.znzk.application.MainApplication;
/*
* 主要想解决Toast对相同的文字重复显示问题
* author :mz
* */
public  class ToastUtil{
    private static final String DEBUG_LOG = "ToastUtil";
    private static Toast sToast ;
    static {
        sToast = Toast.makeText(MainApplication.app,"",Toast.LENGTH_SHORT);
    }
    private static String mCurrentText;

    public static synchronized void shortMessage(String message){

        if(judgeShow(sToast)){
            sToast.setDuration(Toast.LENGTH_SHORT);
            sToast.setText(message);
            //返回true表示当前Toast不可见
            sToast.show();
            Log.d(DEBUG_LOG,"Toast不可见,重新显示");
        }else{
            //表示当前Toast正在显示，如果需要显示的文字和Toast显示的文字不一致时，我们创建一个新的Toast
            if(!message.equals(mCurrentText)){
                sToast = Toast.makeText(MainApplication.app,"",Toast.LENGTH_SHORT);
                sToast.setText("");
                sToast.show();
                Log.d(DEBUG_LOG,"Toast可见,但是是新的文字，重新创建");
            }
        }
        mCurrentText =message ; //保存显示文字
    }

    public static synchronized void longMessage(String message){
        if(judgeShow(sToast)){
            sToast.setDuration(Toast.LENGTH_LONG);
            sToast.setText(message);
            sToast.show();
            Log.d(DEBUG_LOG,"Toast不可见,重新显示");
        }else{
            //表示当前Toast正在显示，如果需要显示的文字和Toast显示的文字不一致时，我们创建一个新的Toast
            if(!message.equals(mCurrentText)){
                sToast = Toast.makeText(MainApplication.app,"",Toast.LENGTH_LONG);
                sToast.setText(message);
                sToast.show();
                Log.d(DEBUG_LOG,"Toast可见,但是是新的文字，重新创建");
            }
        }
        mCurrentText =message ;
    }

    private static boolean judgeShow(Toast toast){
        boolean isShow = false ;
        if(toast.getView().getWindowVisibility()!= View.VISIBLE){
            isShow = true ;
        }
        return isShow ;
    }
}
