package com.smartism.znzk.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
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

public class AlarmFragment extends Fragment implements View.OnClickListener{
    //二十四小时
    SwitchButton btn_switch_ershisi_setting ;
    //闪光灯开关
    SwitchButton btn_switch_flash_deng ;
    //警报
    SwitchButton btn_switch_jingbao ;
    //自动光暗
    SwitchButton btn_switch_zidong_guangan ;

    TextView zhendong_select_second ; //震动内容显示
    TextView shierfu_jiantou_second ;//十二V输出显示内容
    TextView jidianqi_second ; //继电器显示内容
    //时间选择
    RelativeLayout zhendong_parent_rl,shierfu_parent_rl,jidianqi_parent_rl;
    PickerDialog mDialog ;
    XMProgressDialog xmProgressDialog ;
    long zhuji_id = -1 ;
    public AlarmFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDialog = new PickerDialog(getActivity());
        xmProgressDialog= new XMProgressDialog(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.hongcai_alarm_setting,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btn_switch_ershisi_setting = view.findViewById(R.id.btn_switch_ershisi_setting);
        btn_switch_flash_deng = view.findViewById(R.id.btn_switch_flash_deng);
        btn_switch_jingbao = view.findViewById(R.id.btn_switch_jingbao);
        btn_switch_zidong_guangan = view.findViewById(R.id.btn_switch_zidong_guangan);

        zhendong_select_second = view.findViewById(R.id.zhendong_select_second);
        shierfu_jiantou_second = view.findViewById(R.id.shierfu_jiantou_second);
        jidianqi_second = view.findViewById(R.id.jidianqi_second);

        zhendong_parent_rl = view.findViewById(R.id.zhendong_parent_rl);
        shierfu_parent_rl = view.findViewById(R.id.shierfu_parent_rl);
        jidianqi_parent_rl = view.findViewById(R.id.jidianqi_parent_rl);

        //事件
        zhendong_parent_rl.setOnClickListener(this);
        shierfu_parent_rl.setOnClickListener(this);
        jidianqi_parent_rl.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments() ;
        //默认配置
        boolean isFlash = true;
        boolean isAlarm = true ;
        int dengdong = 15;
        int shierfuchushu = 2;
        int jidianqi = 3 ;
        boolean zidongguanan = true;
        int ershisixiaoshizhi = 24;
        if(bundle!=null){
            CommandInfo commandInfo = (CommandInfo) bundle.get("zhuji_setting");
            zhuji_id = (long) bundle.get("zhuji_id");
            if(commandInfo!=null&&commandInfo.getCommand().length()>=14){
                //109有值
                String command = commandInfo.getCommand();
                for(int i=0;i<command.length();i+=2){
                    int result = (int) DecimalUtils.getBytesInString(command,i,i+2);
                    switch (i){
                        case 0:
                            //闪光灯设置状态、
                            if(result==1){
                                isFlash = true;
                            }else{
                                isFlash =false;
                            }
                            break;
                        case 2:
                            //报警开关
                            if(result==1){
                                isAlarm = true;
                            }else{
                                isAlarm =false;
                            }
                            break;
                        case 4:
                            //震动
                            dengdong = result ;
                            break;
                        case 6:
                            shierfuchushu =result ;
                            break;
                        case 8:
                            jidianqi = result;
                            break;
                        case 10:
                            //自动光暗
                            if(result==1){
                                zidongguanan = true;
                            }else{
                                zidongguanan =false;
                            }
                            break;
                        case 12:
                            //24小时制结果
                            ershisixiaoshizhi =result;
                            break;

                    }
                }
            }
            //进行View的初始化
            if(ershisixiaoshizhi==24){
                btn_switch_ershisi_setting.setChecked(true);
            }else{
                btn_switch_ershisi_setting.setChecked(false);
            }
            btn_switch_flash_deng.setChecked(isFlash);
            btn_switch_jingbao.setChecked(isAlarm);
            btn_switch_zidong_guangan.setChecked(zidongguanan);

            //显示关闭
            if(dengdong==0){
                zhendong_select_second.setText(getActivity().getResources().getString(R.string.security_device_close));
            }else{
                zhendong_select_second.setText("+"+dengdong+getActivity().getResources().getString(R.string.pickerview_seconds));
            }

            if(shierfuchushu==0){
                shierfu_jiantou_second.setText(getActivity().getResources().getString(R.string.security_device_close));
            }else{
                shierfu_jiantou_second.setText(shierfuchushu+getActivity().getResources().getString(R.string.pickerview_seconds));
            }

            if(jidianqi==0){
                jidianqi_second.setText(getActivity().getResources().getString(R.string.security_device_close));
            }else{
                jidianqi_second.setText(jidianqi+getActivity().getResources().getString(R.string.pickerview_seconds));
            }

        }
    }

    public static Fragment getAlarmFragment(Bundle bundle){
        AlarmFragment alarm = new AlarmFragment();
        alarm.setArguments(bundle);
        return alarm ;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.save:
                //保存
                //先从高位开始，从闪光灯
                String result = "" ;
                //闪光灯设置
                if(btn_switch_flash_deng.isChecked()){
                    result = result + "01";
                }else{
                    result  = result +"00";
                }

                //报警设置
                if(btn_switch_jingbao.isChecked()){
                    result = result + "01";
                }else{
                    result  = result + "00";
                }

                //震动
                String zhendong_save = zhendong_select_second.getText().toString();
                if(zhendong_save.equals(getActivity().getResources().getString(R.string.security_device_close))){
                    result = result+"00";
                }else{
                    //把秒和+去掉
                    zhendong_save = zhendong_save.replace("+","");
                    zhendong_save = zhendong_save.replace(getActivity().getResources().getString(R.string.pickerview_seconds),"");
                    String temp = Integer.toHexString(Integer.parseInt(zhendong_save));
                    if(temp.length()<2){
                        temp = "0"+temp;
                    }
                    result = result + temp;
                }
                //十二V输出
                String shierfu_save = shierfu_jiantou_second.getText().toString();
                if(shierfu_save.equals(getActivity().getResources().getString(R.string.security_device_close))){
                    result = result + "00";
                }else{
                    //把秒去掉
                    shierfu_save = shierfu_save.replace(getActivity().getResources().getString(R.string.pickerview_seconds),"");
                    String temp = Integer.toHexString(Integer.parseInt(shierfu_save)) ;
                    if(temp.length()<2){
                        temp = "0"+temp;
                    }
                    result = result + temp;
                }
                //继电器
                String jidianqi_save = jidianqi_second.getText().toString();
                if(jidianqi_save.equals(getActivity().getResources().getString(R.string.security_device_close))){
                    result = result + "00";
                }else{
                    //把秒去掉
                    jidianqi_save = jidianqi_save.replace(getActivity().getResources().getString(R.string.pickerview_seconds),"");
                    String temp = Integer.toHexString(Integer.parseInt(jidianqi_save));
                    if(temp.length()<2){
                        temp="0"+temp;
                    }
                    result = result+temp;
                }

                //自动光暗
                if(btn_switch_zidong_guangan.isChecked()){
                    result=result+"01";
                }else{
                    result=result+"00";
                }

                //24小时制
                if(btn_switch_ershisi_setting.isChecked()){
                    result = result + "18";
                }else{
                    result = result + "0C";
                }
                System.out.println("将要提交的结果:"+result);
                xmProgressDialog.showDialog();
                JavaThreadPool.getInstance().excute(new SettingAlarmRunnable(result));
                break;
            case R.id.zhendong_parent_rl:
                //震动时间选择
                mDialog.setTile(getActivity().getResources().getString(R.string.shock));
                String[] temp = new String[31];
                for(int i=0;i<temp.length;i++){
                    if(i==0){
                        temp[i] = getActivity().getResources().getString(R.string.security_device_close);
                    }else{
                        temp[i]="+"+i+getActivity().getResources().getString(R.string.pickerview_seconds);
                    }
                }
                ArrayList<String[]> list = new ArrayList<>();
                list.add(temp);
                mDialog.setDisplayValues(list,new PickerParentLayout.OnClickListener() {
                    @Override
                    public void btnOnClick(String[] pickerValues, View v,int position) {
                        if(position==1) {
                            if(pickerValues[0].equals("+0")){
                                zhendong_select_second.setText(getActivity().getResources().getString(R.string.gensture_off));
                            }else{
                                zhendong_select_second.setText(pickerValues[0]);
                            }
                        }
                        mDialog.dismiss();
                    }
                });
                mDialog.show();
                break;
            case R.id.shierfu_parent_rl:
                //十二V输出时间选择
                mDialog.setTile(getActivity().getResources().getString(R.string.hongcai_shierfushuchu));
                String[] shierfu = new String[31];
                for(int i=0;i<shierfu.length;i++){
                    if(i==0){
                        shierfu[i] = getActivity().getResources().getString(R.string.security_device_close);
                    }else{
                        shierfu[i]=i+getActivity().getResources().getString(R.string.pickerview_seconds);
                    }
                }
                ArrayList<String[]> shierfulist = new ArrayList<>();
                shierfulist.add(shierfu);
                mDialog.setDisplayValues(shierfulist,new PickerParentLayout.OnClickListener() {
                    @Override
                    public void btnOnClick(String[] pickerValues, View v,int position) {
                        if(position==1) {
                            if(pickerValues[0].equals("+0")){
                                shierfu_jiantou_second.setText(getActivity().getResources().getString(R.string.gensture_off));
                            }else{
                                shierfu_jiantou_second.setText(pickerValues[0]);
                            }
                        }
                        mDialog.dismiss();
                    }
                });
                mDialog.show();
                break;
            case R.id.jidianqi_parent_rl:
                //继电器时间选择
                mDialog.setTile(getActivity().getResources().getString(R.string.hongcai_jidianqi_title));
                String[] jidianqi = new String[31];
                for(int i=0;i<jidianqi.length;i++){
                    if(i==0){
                        jidianqi[i] = getActivity().getResources().getString(R.string.security_device_close);
                    }else{
                        jidianqi[i]=i+getActivity().getResources().getString(R.string.pickerview_seconds);
                    }
                }
                ArrayList<String[]> jidianqilist = new ArrayList<>();
                jidianqilist.add(jidianqi);
                mDialog.setDisplayValues(jidianqilist,new PickerParentLayout.OnClickListener() {
                    @Override
                    public void btnOnClick(String[] pickerValues, View v,int position) {
                        if(position==1) {
                            if(pickerValues[0].equals("+0")){
                                jidianqi_second.setText(getActivity().getResources().getString(R.string.gensture_off));
                            }else{
                                jidianqi_second.setText(pickerValues[0]);
                            }
                        }
                        mDialog.dismiss();
                    }
                });
                mDialog.show();
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
            object.put("setKey","109");
            String result = HttpRequestUtils.requestoOkHttpPost(server,object, (ActivityParentActivity) getActivity());
            if(result.equals("0")){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().sendBroadcast(new Intent(HongCaiSettingActivity.FLASH_AND_ALARM));//更新DeviceMainFragment图标
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

