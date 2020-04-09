package com.smartism.znzk.activity.common;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DecimalUtils;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.view.SwitchButton.SwitchButton;
import com.smartism.znzk.xiongmai.utils.XMProgressDialog;
import com.smartism.znzk.xiongmai.widget.PickerDialog;
import com.smartism.znzk.xiongmai.widget.PickerParentLayout;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HongCaiTantouSettingActivity extends ActivityParentActivity implements View.OnClickListener{


    MediaPlayer mMediaPlayer ;
    XMProgressDialog mProgressDialg ;
    PickerDialog mPicker ;
    CommandInfo mInfo  ;
    long device_id  ;
    ImageView iv_back ;
    ListView showMusicList ;
    TextView save,tantou_name_select_content,tantou_quhao_select_content,music_select_content_one;
    RelativeLayout tantou_setting_name,tantou_quhao_setting,tantou_yinyue_setting,shierfushuchu_parent_rl,jidianqi_parent_rl,inputNameParent;
    SwitchButton btn_switch_tantou_zhendong_guangan,btn_switch_tantou_flash_deng
            ,btn_switch_tantou_shierfu_guangan_one,btn_switch_tantou_jidianqi_guangan_one,btn_switch_liandong_guangan_one;

    //默认值
    boolean relay,tvo,vibration,flash=true;
    String music = "01";
    int quhao = 7;
    String name ="";
    List<String> musicList = new ArrayList<>();
    int currentPosition = 0 ; //记录当前选择的音乐
    EditText name_edit ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hongcai_tantou_setting);
        if(savedInstanceState==null){
            device_id = getIntent().getLongExtra("device_id",-1);
        }else{
            device_id = savedInstanceState.getLong("device_id");
        }

        initView();
        initViewData();

    }

    private void initViewData(){
        mPicker = new PickerDialog(this);
        List<CommandInfo> infos = DatabaseOperator.getInstance().queryAllCommands(device_id);
        if(infos!=null&&infos.size()>0){
            for(CommandInfo info:infos){
                if(info.getCtype().equals("110")){
                    mInfo = info;
                }
            }
        }
        if(mInfo!=null){
            String command = mInfo.getCommand();
             //这个是名字,占7个字节，每一个字节代表一个字符
            //顺序继电器、十二v输出、震动开关、闪灯开关、音乐选择、探头区号,记得反正来，低位在右，高位在左
            for(int i=0;i<command.substring(0,12).length();i+=2){
                int result = (int) DecimalUtils.getBytesInString(command.substring(0,12),i,i+2);
                switch (i){
                    case  0:
                        if(result==1){
                            relay = true;
                        }else{
                            relay=false;
                        }
                        break;
                    case 2:
                        //十二V
                        if(result==1){
                            tvo =true;
                        }else{
                            tvo = false;
                        }
                        break;
                    case 4:
                        //震动开关
                        if(result==1){
                            vibration = true;
                        }else{
                            vibration =false;
                        }
                        break;
                    case 6:
                        //闪灯
                        if(result==1){
                            flash = true;
                        }else{
                            flash = false;
                        }
                        break;
                    case 8:
                        //音乐选择
                        if(result+1<10){
                            music = "0"+(result+1);
                        }else{
                            music = (result+1)+"";
                        }
                        currentPosition = result ;
                        break;
                    case 10:
                        //探头区号
                        quhao = result;
                        break;
                }
            }

            //名字处理
            for(int i=0;i<command.substring(12,26).length();i+=2){
                char ch = (char) DecimalUtils.getBytesInString(command.substring(12,26),i,i+2);
                name+=ch;
            }
        }



        //给View设置值
        if(quhao==7){
            tantou_quhao_select_content.setText(getResources().getString(R.string.hongcai_quwai));
        }else{
            tantou_quhao_select_content.setText(quhao+"");
        }
        music_select_content_one.setText(music);
        btn_switch_tantou_flash_deng.setChecked(flash);
        btn_switch_tantou_jidianqi_guangan_one.setChecked(relay);
        btn_switch_tantou_zhendong_guangan.setChecked(vibration);
        btn_switch_tantou_shierfu_guangan_one.setChecked(tvo);
        tantou_name_select_content.setText(name);

        if(name.equals("")){
            //表明是默认值,连动输出为关闭
            btn_switch_liandong_guangan_one.setChecked(false);
            jidianqi_parent_rl.setVisibility(View.GONE);
            shierfushuchu_parent_rl.setVisibility(View.GONE);
        }else{
            if(btn_switch_tantou_jidianqi_guangan_one.isChecked()||btn_switch_tantou_shierfu_guangan_one.isChecked()){
                btn_switch_liandong_guangan_one.setChecked(true);
            }else{
                jidianqi_parent_rl.setVisibility(View.GONE);
                shierfushuchu_parent_rl.setVisibility(View.GONE);
            }
        }

    }

    private void initView(){
        iv_back = findViewById(R.id.iv_back);
        save = findViewById(R.id.save);
        showMusicList = findViewById(R.id.showMusicList);
        tantou_setting_name = findViewById(R.id.tantou_setting_name);
        tantou_quhao_setting = findViewById(R.id.tantou_quhao_setting);
        tantou_yinyue_setting = findViewById(R.id.tantou_yinyue_setting);
        tantou_name_select_content = findViewById(R.id.tantou_name_select_content);
        tantou_quhao_select_content = findViewById(R.id.tantou_quhao_select_content);
        music_select_content_one = findViewById(R.id.music_select_content_one);
        shierfushuchu_parent_rl = findViewById(R.id.shierfushuchu_parent_rl);
        jidianqi_parent_rl = findViewById(R.id.jidianqi_parent_rl);
        inputNameParent = findViewById(R.id.inputNameParent);
        name_edit = findViewById(R.id.name_edit);


        btn_switch_tantou_zhendong_guangan = findViewById(R.id.btn_switch_tantou_zhendong_guangan);
        btn_switch_tantou_flash_deng = findViewById(R.id.btn_switch_tantou_flash_deng);
        btn_switch_tantou_shierfu_guangan_one = findViewById(R.id.btn_switch_tantou_shierfu_guangan_one);
        btn_switch_tantou_jidianqi_guangan_one = findViewById(R.id.btn_switch_tantou_jidianqi_guangan_one);
        btn_switch_liandong_guangan_one = findViewById(R.id.btn_switch_liandong_guangan_one);

        //输入框初始化
        name_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String result = s.toString();
                result = DecimalUtils.checkUserInput(result,"[a-zA-Z\\d_\\.\\s\\\\\\(\\)]");
                //不一样了，说明有字符不符合正则表达式
                if(!result.equals(s.toString())){
                    name_edit.setText(result);
                    name_edit.setSelection(name_edit.getText().toString().length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        //事件初始化
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showMusicList.getVisibility()==View.VISIBLE){
                    stopMusic();
                    showMusicList.setVisibility(View.GONE);
                    showToast(musicList.get(currentPosition));
                    music_select_content_one.setText(musicList.get(currentPosition));
                    return ;
                }else if(inputNameParent.getVisibility()==View.VISIBLE){
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm.isActive()&&name_edit.getWindowToken()!=null){
                        imm.hideSoftInputFromWindow(name_edit.getWindowToken(),0);
                    }
                    inputNameParent.setVisibility(View.GONE);
                    return ;
                }
                finish();
            }
        });

        btn_switch_liandong_guangan_one.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    btn_switch_tantou_shierfu_guangan_one.setChecked(false);
                    btn_switch_tantou_jidianqi_guangan_one.setChecked(false);
                    jidianqi_parent_rl.setVisibility(View.GONE);
                    shierfushuchu_parent_rl.setVisibility(View.GONE);
                }else{
                    jidianqi_parent_rl.setVisibility(View.VISIBLE);
                    shierfushuchu_parent_rl.setVisibility(View.VISIBLE);
                }
            }
        });
        tantou_setting_name.setOnClickListener(this);
        tantou_quhao_setting.setOnClickListener(this);
        tantou_yinyue_setting.setOnClickListener(this);
        save.setOnClickListener(this);

        //初始化音乐列表
        int count = 33 ;
        for(int i=1;i<=count;i++){
            if(i<10){
               musicList.add("0"+i);
            }else{
                musicList.add(""+i);
            }
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_single_choice,musicList);
        showMusicList.setAdapter(arrayAdapter);
        showMusicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentPosition = position ;
                musicPlay(currentPosition);
            }
        });
    }



    public void clearEditText(View v){
        //清空文本
        name_edit.setText("");
    }

    @Override
    public void onBackPressed() {
        if(showMusicList.getVisibility()==View.VISIBLE){
            stopMusic();
            showMusicList.setVisibility(View.GONE);
            showToast(musicList.get(currentPosition));
            music_select_content_one.setText(musicList.get(currentPosition));
        }else if(inputNameParent.getVisibility()==View.VISIBLE){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm.isActive()&&name_edit.getWindowToken()!=null){
                imm.hideSoftInputFromWindow(name_edit.getWindowToken(),0);
            }
            inputNameParent.setVisibility(View.GONE);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopMusic();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("device_id",device_id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.save:
                //保存
                if(showMusicList.getVisibility()==View.VISIBLE){
                    stopMusic();
                    showMusicList.setVisibility(View.GONE);
                    showToast(musicList.get(currentPosition));
                    music_select_content_one.setText(musicList.get(currentPosition));
                    return ;
                }else if(inputNameParent.getVisibility()==View.VISIBLE){
                    setName();
                    return ;
                }
                updateStatus();
                break;
            case R.id.tantou_setting_name:
                //输入名字
                name_edit.setText(tantou_name_select_content.getText());
                name_edit.setSelection(tantou_name_select_content.getText().length());
                inputNameParent.setVisibility(View.VISIBLE);
                name_edit.setFocusable(true);
                name_edit.setFocusableInTouchMode(true);
                name_edit.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);

                break;
            case R.id.tantou_quhao_setting:
                //区号
                String[] temp = new String[7];
                for(int i=0;i<temp.length;i++){
                    if(i==6){
                        temp[i]=getResources().getString(R.string.hongcai_quwai);
                    }else{
                        temp[i] = (i+1)+"";
                    }
                }
                ArrayList<String[]> lists = new ArrayList<>();
                lists.add(temp);
                mPicker.setTile(getResources().getString(R.string.hongcai_tantou_quhao));
                mPicker.setDisplayValues(lists, new PickerParentLayout.OnClickListener() {
                    @Override
                    public void btnOnClick(String[] pickerValues, View v, int position) {
                        //区号设置
                        if(position==1) {
                            tantou_quhao_select_content.setText(pickerValues[0]);
                        }
                        mPicker.dismiss();
                    }
                });
                mPicker.show();
                break;
            case R.id.tantou_yinyue_setting:
                //说明音乐列表不可见
                showMusicList.setVisibility(View.VISIBLE);
                //初始化音乐选择的默认选项
                showMusicList.setSelection(currentPosition);
                showMusicList.performItemClick(null,currentPosition,currentPosition);
                break;
        }
    }
     Toast mToast ;
    private void showToast(String text){
        if(text==null){
            text = "";
        }
        if(mToast ==null){
            mToast  = Toast.makeText(this,"",Toast.LENGTH_SHORT);
        }

        mToast.setText(text);
        mToast.show();
    }

    private void musicPlay(int position){
        stopMusic();
        mMediaPlayer = MediaPlayer.create(this,HongCaiSettingActivity.handleChoiceMusic(position));
        mMediaPlayer.setLooping(false);
        mMediaPlayer.start(); //如果加载的是res中的资源，这个时候已经完成了初始化，不用调用prepare方法进行加载，可以直接播放，否则会抛出状态异常
    }

    private void stopMusic(){
        if(mMediaPlayer!=null){
            mMediaPlayer.release();
            mMediaPlayer = null ;
        }
    }
    /*
    * 顺序:继电器、十二v输出、震动开关、闪灯开关、音乐选择、探头区号,名字记得反正来，低位在右，高位在左
     * */
    private void updateStatus(){
        String result = "";
        //继电器
        if(btn_switch_tantou_jidianqi_guangan_one.isChecked()){
            result+="01";
        }else{
            result+="00";
        }
        //十二V输出
        if(btn_switch_tantou_shierfu_guangan_one.isChecked()){
            result+="01";
        }else{
            result+="00";
        }

        //震动开关
        if(btn_switch_tantou_zhendong_guangan.isChecked()){
            result+="01";
        }else{
            result+="00";
        }
        //闪灯开关
        if(btn_switch_tantou_flash_deng.isChecked()){
            result+="01";
        }else{
            result+="00";
        }
        //音乐选择
        Integer temp = Integer.parseInt(musicList.get(currentPosition))-1;
        if(temp<16){
            result = result+"0"+Integer.toHexString(temp);
        }else{
            result+=Integer.toHexString(temp);
        }
        //区号
        String quhao = tantou_quhao_select_content.getText().toString();
        if(quhao.equals(getResources().getString(R.string.hongcai_quwai))){
            result+="07";
        }else{
            result+="0"+quhao;
        }
        //名字
        String name = tantou_name_select_content.getText().toString();
        if(TextUtils.isEmpty(name)||name.equals("       ")){
           //为空字符，即用户没有进行输入，不能保存
            Toast toast  = Toast.makeText(this,"",Toast.LENGTH_SHORT);
            toast.setText(getResources().getString(R.string.hongcai_request_name_format_tishi));
            toast.show();
           return ;
        }
        for(int i=0;i<name.length();i++){
            int ch = name.charAt(i);
            //用户输入的是可见字符，一定大于F，不用补0了
            result+=Integer.toHexString(ch);
        }
        showInProgress("");
        JavaThreadPool.getInstance().excute(new SettingAlarmRunnable(result));
    }

    private void setName(){
        String result = name_edit.getText().toString();
        if(!TextUtils.isEmpty(result)){
            //不为空，就是设置了值后才补空格
            while(result.length()<7){
                result = result+" ";//补空格
            }
        }

        tantou_name_select_content.setText(result);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm.isActive()&&name_edit.getWindowToken()!=null){
            imm.hideSoftInputFromWindow(name_edit.getWindowToken(),0);
        }
        inputNameParent.setVisibility(View.GONE);
    }

    class SettingAlarmRunnable implements  Runnable{

        String result ;
        public SettingAlarmRunnable(String result){
            this.result = result ;
        }
        @Override
        public void run() {
            DataCenterSharedPreferences dscp  = DataCenterSharedPreferences.getInstance(HongCaiTantouSettingActivity.this,
                    DataCenterSharedPreferences.Constant.CONFIG);
            String server  = dscp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            server = server+"/jdm/s3/sphctz/update";
            JSONObject object = new JSONObject();
            object.put("did",device_id);
            object.put("setInfo",result);
            object.put("setKey","110");
            String result = HttpRequestUtils.requestoOkHttpPost(server,object,HongCaiTantouSettingActivity.this);
            if(result.equals("0")){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast toast = Toast.makeText(HongCaiTantouSettingActivity.this,"",Toast.LENGTH_LONG);
                        toast.setText(getResources().getString(R.string.deviceinfo_activity_success));
                        toast.show();
                    }
                });

            }else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast toast = Toast.makeText(HongCaiTantouSettingActivity.this,"",Toast.LENGTH_LONG);
                        toast.setText(getResources().getString(R.string.activity_editscene_set_falid));
                        toast.show();
                    }
                });
            }
        }
    }
}
