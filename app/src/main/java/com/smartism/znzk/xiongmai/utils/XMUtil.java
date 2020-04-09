package com.smartism.znzk.xiongmai.utils;

import android.os.Environment;
import android.util.Log;

import com.lib.FunSDK;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunSupport;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.JsonConfig;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevice;
import com.smartism.znzk.xiongmai.lib.sdk.bean.DefaultConfigBean;
import com.smartism.znzk.xiongmai.lib.sdk.bean.HandleConfigData;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class XMUtil {
    final static int LOCAL_MEDIA = 0X89 ; //本地录制视频标识
    final static int LOCAL_PICTURE = 0X88; //本地截图标识
    final static String TAG = XMUtil.class.getSimpleName();

    //创建图片录像文件路径
    public static  String getFilePath(final String houZhuiMing, final int type, FunDevice device,String packageName){
        //截图和录像是属于某一个摄像头的，显示匹配当前设备的截图和录像文件
        String filePath = Environment.getExternalStorageDirectory().toString()+
                File.separator+packageName+File.separator+"xiongmaitempimg"+File.separator+device.getDevSn();
        File picFile = null ;
        if(type==LOCAL_MEDIA){
            picFile = new File(filePath+File.separator+"local_media");
        }else if(type==LOCAL_PICTURE){
            picFile = new File(filePath+File.separator+"local_picture");
        }

        if(!picFile.exists()){
            boolean bool = picFile.mkdirs();
            if(bool){
                Log.v(TAG,"截图目录创建成功");
            }else{
                Log.v(TAG,"截图目录创建失败");
            }
        }
        return picFile.toString()+File.separator+System.currentTimeMillis()+houZhuiMing;
    }

    //获取截图目录下的文件名
    private List<String> getImageFiles(FunDevice device,String packageName){
        //需要读写权限申请，否则会失败
        List<String> list = new ArrayList<>();
        File picFile = new File(Environment.getExternalStorageDirectory().toString()+File.separator+packageName
                +File.separator+"xiongmaitempimg"+File.separator+device.getDevSn()+File.separator+"local_picture");
        if(!picFile.exists()){
            return  list;
        }
        //获取该目录下的文件名，注意仅仅是文件名
        String[] temp = picFile.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                //s表示当前目录下的文件名
                if(s.endsWith(".jpg")){
                    return true;
                }
                return false ;
            }
        });
        if(temp!=null){
            //加上父目录路径
            for(int i=0;i<temp.length;i++){
                temp[i]=picFile.toString()+File.separator+temp[i];
                list.add(temp[i]);
            }
        }

        return list;
    }


    //对摄像头进行复位
    public static void restoreDeviceDefaultConfig(FunDevice mFunDevice){
        if(mFunDevice==null){
            throw new IllegalArgumentException("不能为null");
        }
        DefaultConfigBean mdefault = new DefaultConfigBean();
        mdefault.setAllConfig(1);
        FunSDK.DevSetConfigByJson(FunSupport.getInstance().getHandler(),
                mFunDevice.devSn, JsonConfig.OPERATION_DEFAULT_CONFIG,
                HandleConfigData.getSendData(JsonConfig.OPERATION_DEFAULT_CONFIG, "0x1", mdefault), -1 ,
                20000, mFunDevice.getId());
    }
}
