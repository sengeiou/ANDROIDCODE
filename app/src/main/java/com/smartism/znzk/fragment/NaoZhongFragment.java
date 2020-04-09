package com.smartism.znzk.fragment;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.common.HongCaiSettingActivity;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DecimalUtils;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.view.SwitchButton.SwitchButton;
import com.smartism.znzk.xiongmai.utils.XMProgressDialog;
import com.smartism.znzk.xiongmai.widget.PickerDialog;
import com.smartism.znzk.xiongmai.widget.PickerParentLayout;

import java.util.ArrayList;

public  class NaoZhongFragment extends  Fragment implements View.OnClickListener,PickerParentLayout.OnClickListener{
    public NaoZhongFragment(){}


    //实现普抓back事件
    public  interface PuZhuoBackEvent{
        public boolean puZhuoBackEvent();
    }

    PickerDialog mDialog ;
    final int NAO_ONE = 0x99 ;
    final int NAO_TWO = 0x98;
    int currentNao = -1;
    XMProgressDialog xmProgressDialog ;
    long zhuji_id  = -1 ;
    //继电器
    SwitchButton btn_switch_naozhong_jidianqi_guangan_one ,btn_switch_naozhong_jidianqi_guangan_two;
    //十二V输出
    SwitchButton btn_switch_naozhong_shierfu_guangan_one ,btn_switch_naozhong_shierfu_guangan_two;
    SwitchButton btn_switch_naozhong_zhendong_guangan_one,btn_switch_naozhong_zhendong_guangan_two ; //震动
    SwitchButton btn_switch_naozhong_flash_deng_one ,btn_switch_naozhong_flash_deng_two; //闪灯

    TextView naozhong_time_conent_one ,naozhong_time_conent_two; //闹钟时间
    TextView music_select_content_one,music_select_content_two ; //音乐显示内容

    RelativeLayout naozhong_time_parent_two,naozhong_time_parent_one ;
    RelativeLayout naozhong_two_music_parent , naozhong_one_music_parent;
    ListView music_choice_list_view ;
    public MediaPlayer mediaPlayer ;
    int currentPosition =0;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        xmProgressDialog = new XMProgressDialog(getActivity());
        mDialog = new PickerDialog(getActivity());
        ArrayList<String[]> lists = new ArrayList<>();
        String[] hour = new String[24];
        String[] minute = new String[60];

        for(int i=0;i<hour.length;i++){
            hour[i] = i+getActivity().getResources().getString(R.string.smart_medc_add_time_s);
        }
        for(int i=0;i<minute.length;i++){
            minute[i] = i+getActivity().getResources().getString(R.string.smart_medc_add_time_f);
        }
        lists.add(hour);
        lists.add(minute);
        mDialog.setDisplayValues(lists,this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.hongcai_naozhong_setting,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        music_choice_list_view = view.findViewById(R.id.music_choice_list_view);
        naozhong_one_music_parent = view.findViewById(R.id.naozhong_one_music_parent);
        naozhong_two_music_parent = view.findViewById(R.id.naozhong_two_music_parent);
        naozhong_time_parent_two = view.findViewById(R.id.naozhong_time_parent_two);
        naozhong_time_parent_one = view.findViewById(R.id.naozhong_time_parent_one);
        btn_switch_naozhong_jidianqi_guangan_one = view.findViewById(R.id.btn_switch_naozhong_jidianqi_guangan_one);
        btn_switch_naozhong_jidianqi_guangan_two= view.findViewById(R.id.btn_switch_naozhong_jidianqi_guangan_two);
        btn_switch_naozhong_flash_deng_one= view.findViewById(R.id.btn_switch_naozhong_flash_deng_one);
        btn_switch_naozhong_flash_deng_two= view.findViewById(R.id.btn_switch_naozhong_flash_deng_two);
        btn_switch_naozhong_shierfu_guangan_one= view.findViewById(R.id.btn_switch_naozhong_shierfu_guangan_one);
        btn_switch_naozhong_shierfu_guangan_two= view.findViewById(R.id.btn_switch_naozhong_shierfu_guangan_two);
        btn_switch_naozhong_zhendong_guangan_one= view.findViewById(R.id.btn_switch_naozhong_zhendong_guangan_one);
        btn_switch_naozhong_zhendong_guangan_two= view.findViewById(R.id.btn_switch_naozhong_zhendong_guangan_two);

        naozhong_time_conent_one= view.findViewById(R.id.naozhong_time_conent_one);
        naozhong_time_conent_two= view.findViewById(R.id.naozhong_time_conent_two);

        music_select_content_two = view.findViewById(R.id.music_select_content_two);
        music_select_content_one = view.findViewById(R.id.music_select_content_one);

        naozhong_time_parent_one.setOnClickListener(this);
        naozhong_time_parent_two.setOnClickListener(this);
        naozhong_one_music_parent.setOnClickListener(this);
        naozhong_two_music_parent.setOnClickListener(this);

        String[] media = new String[33];
        for(int i=0;i<media.length;i++){
            int k = i +1;
            if(k<10){
                media[i] = "0"+k;
            }else{
                media[i] = k+"";
            }
        }
        //适配器
        ArrayAdapter adapter = new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_single_choice,media);
        music_choice_list_view.setAdapter(adapter);
        music_choice_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int i = position+1;
                String temp  = "";
                if(i<10){
                    temp = "0"+i;
                }else{
                    temp = i+"";
                }
                if(currentYinyue==1){
                    //修改闹钟一音乐文字
                    music_select_content_one.setText(temp);
                }else if(currentYinyue==2){
                    //修改闹钟二音乐文字
                    music_select_content_two.setText(temp);
                }
                currentPosition = position ;
                if(mediaPlayer!=null){
                    mediaPlayer.release();//释放之前资源
                }
                mediaPlayer = MediaPlayer.create(getActivity(),HongCaiSettingActivity.handleChoiceMusic(position));
                mediaPlayer.start();
                mediaPlayer.setLooping(false);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments() ;
        //闹钟一
        boolean jidianqi_one= false ;
        boolean shierfu_one= false ;
        boolean zhendong_one= false ;
        boolean flashdeng_one= false ;
        int hour_one = 0 ;
        int minute_one = 0 ;
        String time_one = "00:00";
        String music_one  = "01";

        //闹钟二
        boolean jidianqi_two= false ;
        boolean shierfu_two= false ;
        boolean zhendong_two= false ;
        boolean flashdeng_two= false ;
        int hour_two =0;
        int minute_two = 0;
        String time_two = "00:00";
        String music_two = "01";
        if(bundle!=null){
            CommandInfo commandInfo = (CommandInfo) bundle.get("zhuji_setting");
            zhuji_id = (long) bundle.get("zhuji_id");
            if(commandInfo!=null&&commandInfo.getCommand().length()>=28){
                String command = commandInfo.getCommand() ;
                String two_command = command.substring(14);
                String one_command = command.substring(0,14);
                //闹钟一
                for(int i=0;i<one_command.length();i+=2) {
                    int result = (int) DecimalUtils.getBytesInString(one_command, i, i + 2);
                    switch (i) {
                        case 0:
                            music_one = handleNaoZhongTime(result + 1);
                            break;
                        case 2:
                            //继电器
                            if (result == 1) {
                                jidianqi_one = true;
                            } else {
                                jidianqi_one = false;
                            }
                            break;
                        case 4:
                            //十二V输出
                            if (result == 1) {
                                shierfu_one = true;
                            } else {
                                shierfu_one = false;
                            }
                            break;
                        case 6:
                            //震动
                            if (result == 1) {
                                zhendong_one = true;
                            } else {
                                zhendong_one = false;
                            }
                            break;
                        case 8:
                            //闪光灯
                            if (result == 1) {
                                flashdeng_one = true;
                            } else {
                                flashdeng_one = false;
                            }
                            break;
                        case 10:
                            //小时
                            hour_one = result;
                            break;
                        case 12:
                            //分钟
                            minute_one = result;
                            break;
                    }
                }

                //闹钟二
                for(int i=0;i<two_command.length();i+=2){
                    int result = (int) DecimalUtils.getBytesInString(two_command,i,i+2);
                    switch (i){
                        case 0:
                            music_two = handleNaoZhongTime(result+1);
                            break;
                        case 2:
                            //继电器
                            if(result==1){
                                jidianqi_two = true;
                            }else{
                                jidianqi_two =false;
                            }
                            break;
                        case 4:
                            //十二V输出
                            if(result==1){
                                shierfu_two = true ;
                            }else{
                                shierfu_two = false ;
                            }
                            break;
                        case 6:
                            //震动
                            if(result==1){
                                zhendong_two = true ;
                            }else{
                                zhendong_two = false ;
                            }
                            break;
                        case 8:
                            //闪光灯
                            if(result==1){
                                flashdeng_two = true;
                            }else{
                                flashdeng_two = false;
                            }
                            break;
                        case 10:
                            //小时
                            hour_two = result ;
                            break;
                        case 12:
                            //分钟
                            minute_two =result;
                            break;
                    }
                }
            }
        }

        //时间处理
        time_one = handleNaoZhongTime(hour_one)+":"+handleNaoZhongTime(minute_one);
        time_two = handleNaoZhongTime(hour_two)+":"+handleNaoZhongTime(minute_two);
        //View初始化
        btn_switch_naozhong_jidianqi_guangan_one.setChecked(jidianqi_one);
        btn_switch_naozhong_jidianqi_guangan_two.setChecked(jidianqi_two);
        btn_switch_naozhong_flash_deng_one.setChecked(flashdeng_one);
        btn_switch_naozhong_flash_deng_two.setChecked(flashdeng_two);
        btn_switch_naozhong_shierfu_guangan_one.setChecked(shierfu_one);
        btn_switch_naozhong_shierfu_guangan_two.setChecked(shierfu_two);
        btn_switch_naozhong_zhendong_guangan_one.setChecked(zhendong_one);
        btn_switch_naozhong_zhendong_guangan_two.setChecked(zhendong_two);

        naozhong_time_conent_one.setText(time_one);
        naozhong_time_conent_two.setText(time_two);
        music_select_content_one.setText(music_one);
        music_select_content_two.setText(music_two);

    }

    private String handleNaoZhongTime(int time){
        if(time<10){
            return "0"+time;
        }
        return String.valueOf(time);
    }

    public static Fragment getNaoZhongFragment(Bundle bundle){
        NaoZhongFragment naozhong = new NaoZhongFragment();
        naozhong.setArguments(bundle);
        return naozhong ;
    }

    int currentYinyue = -1 ;
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.save:
                //保存
                if(music_choice_list_view.getVisibility()==View.VISIBLE){
                    if(mediaPlayer!=null){
                        mediaPlayer.release();//资源释放
                    }
                    music_choice_list_view.setVisibility(View.GONE);
                    Toast toast = Toast.makeText(getActivity(),"",Toast.LENGTH_LONG);
                    int i = currentPosition+1 ;
                    toast.setText(""+i);
                    toast.show();
                    return ;
                }
                //准备发出设置闹钟请求
                //闹钟一
                String result_one = "";

                //音乐处理
                int index_one = Integer.parseInt(music_select_content_one.getText().toString());
                if(index_one-1<16){
                    result_one = result_one +"0"+Integer.toHexString(index_one-1);
                }else{
                    result_one = result_one + Integer.toHexString(index_one-1);
                }


                //继电器处理
                if(btn_switch_naozhong_jidianqi_guangan_one.isChecked()){
                    result_one = result_one + "01";
                }else{
                    result_one = result_one + "00";
                }

                //十二V输出处理
                if(btn_switch_naozhong_shierfu_guangan_one.isChecked()){
                    result_one = result_one + "01";
                }else{
                    result_one = result_one +"00";
                }

                //震动处理
                if(btn_switch_naozhong_zhendong_guangan_one.isChecked()){
                    result_one = result_one + "01";
                }else{
                    result_one = result_one + "00";
                }


                //闪光灯处理
                if(btn_switch_naozhong_flash_deng_one.isChecked()){
                    result_one = result_one + "01";
                }else{
                    result_one = result_one +"00";
                }

                //时分处理
                String time_one = naozhong_time_conent_one.getText().toString();
                String[] time_temp = time_one.split(":");
                int hour_one = Integer.parseInt(time_temp[0]);
                int minute_one = Integer.parseInt(time_temp[1]);
                if(String.valueOf(Integer.toHexString(hour_one)).length()<2){
                    result_one = result_one + "0"+Integer.toHexString(hour_one);
                }else{
                    result_one = result_one+Integer.toHexString(hour_one);
                }

                if(String.valueOf(Integer.toHexString(minute_one)).length()<2){
                    result_one = result_one + "0"+Integer.toHexString(minute_one);
                }else{
                    result_one = result_one+Integer.toHexString(minute_one);
                }

                System.out.println("闹钟一读取结果:"+result_one);

                //闹钟二
                String result_two = "";
                //音乐处理
                int index_two = Integer.parseInt(music_select_content_two.getText().toString());
                if(index_two-1<16){
                    result_two = result_two +"0"+Integer.toHexString(index_two-1);
                }else{
                    result_two = result_two + Integer.toHexString(index_two-1);
                }

                //继电器处理
                if(btn_switch_naozhong_jidianqi_guangan_two.isChecked()){
                    result_two = result_two + "01";
                }else{
                    result_two = result_two + "00";
                }

                //十二V输出处理
                if(btn_switch_naozhong_shierfu_guangan_two.isChecked()){
                    result_two = result_two + "01";
                }else{
                    result_two = result_two +"00";
                }

                //震动处理
                if(btn_switch_naozhong_zhendong_guangan_two.isChecked()){
                    result_two = result_two + "01";
                }else{
                    result_two = result_two + "00";
                }

                //闪光灯处理
                if(btn_switch_naozhong_flash_deng_two.isChecked()){
                    result_two = result_two + "01";
                }else{
                    result_two = result_two +"00";
                }
                //时分处理
                String time_two = naozhong_time_conent_two.getText().toString();
                String[] time_temp_two = time_two.split(":");
                int hour_two = Integer.parseInt(time_temp_two[0]);
                int minute_two = Integer.parseInt(time_temp_two[1]);
                if(String.valueOf(Integer.toHexString(hour_two)).length()<2){
                    result_two = result_two + "0"+Integer.toHexString(hour_two);
                }else{
                    result_two = result_two+Integer.toHexString(hour_two);
                }

                if(String.valueOf(Integer.toHexString(minute_two)).length()<2){
                    result_two = result_two + "0"+Integer.toHexString(minute_two);
                }else{
                    result_two = result_two+Integer.toHexString(minute_two);
                }
                System.out.println("闹钟二读取结果:"+result_two);

                xmProgressDialog.showDialog();
                JavaThreadPool.getInstance().excute(new SettingAlarmRunnable(result_one+result_two));

                break ;
            case R.id.naozhong_time_parent_two:
                //闹钟二时间设置
                currentNao = NAO_TWO;
                mDialog.setTile(getActivity().getResources().getString(R.string.jjsuo_sp_please_choice_time));
                mDialog.show();
                break;
            case R.id.naozhong_time_parent_one:
                //闹钟一时间设置
                currentNao = NAO_ONE ;
                mDialog.setTile(getActivity().getResources().getString(R.string.jjsuo_sp_please_choice_time));
                mDialog.show();
                break;
            case R.id.naozhong_two_music_parent:
                handleMusicData(2);
                break;
            case R.id.naozhong_one_music_parent:
                handleMusicData(1);
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mediaPlayer!=null){
            mediaPlayer.release();
        }
    }

    private void handleMusicData(int currentYinyue){
        int i = 0;
        if(currentYinyue==1){
            //点击闹钟一的音乐
            this.currentYinyue = 1;
            i = Integer.parseInt(music_select_content_one.getText().toString());
        }else if(currentYinyue==2){
            //点击闹钟二的音乐
            this.currentYinyue = 2 ;
            i = Integer.parseInt(music_select_content_two.getText().toString());
        }
        currentPosition = i-1;//默认选中的位置
        music_choice_list_view.setItemChecked(currentPosition,true);//选中默认选项
        music_choice_list_view.setSelection(currentPosition);//滑动到选中的位置
        mediaPlayer = MediaPlayer.create(getActivity(),HongCaiSettingActivity.handleChoiceMusic(currentPosition));
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
        music_choice_list_view.setVisibility(View.VISIBLE);
    }

    @Override
    public void btnOnClick(String[] pickerValues, View v, int position) {
        mDialog.dismiss();
        if(position==0){
            return ;
        }
        String hour,minute;
        //去掉时分
        hour = pickerValues[0].substring(0,pickerValues[0].length()-1);
        minute = pickerValues[1].substring(0,pickerValues[1].length()-1);
        if(Integer.parseInt(hour)<10) {
            hour = "0" + hour;
        }

        if(Integer.parseInt(minute)<10){
            minute = "0"+minute;
        }
        switch (currentNao){
            case NAO_ONE:
                naozhong_time_conent_one.setText(hour+":"+minute);
                break ;
            case NAO_TWO:
                naozhong_time_conent_two.setText(hour+":"+minute);
                break;
        }
    }



    class SettingAlarmRunnable implements  Runnable{

        String result ;
        public SettingAlarmRunnable(String result){
            this.result = result ;
        }
        @Override
        public void run() {
            DataCenterSharedPreferences dscp  = DataCenterSharedPreferences.getInstance(getActivity(),
                    DataCenterSharedPreferences.Constant.CONFIG);
            String server  = dscp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            server = server+"/jdm/s3/sphctz/update";
            JSONObject object = new JSONObject();
            object.put("did",zhuji_id);
            object.put("setInfo",result);
            object.put("setKey","111");
            String result = HttpRequestUtils.requestoOkHttpPost(server,object, (ActivityParentActivity) getActivity());
            if(result.equals("0")){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        xmProgressDialog.dismissDialog();
                        Toast toast = Toast.makeText(getActivity(),"",Toast.LENGTH_LONG);
                        toast.setText(getActivity().getResources().getString(R.string.deviceinfo_activity_success));
                        toast.show();
                    }
                });

            }else{
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        xmProgressDialog.dismissDialog();
                        Toast toast = Toast.makeText(getActivity(),"",Toast.LENGTH_LONG);
                        toast.setText(getActivity().getResources().getString(R.string.activity_editscene_set_falid));
                        toast.show();
                    }
                });
            }
        }
    }
}

