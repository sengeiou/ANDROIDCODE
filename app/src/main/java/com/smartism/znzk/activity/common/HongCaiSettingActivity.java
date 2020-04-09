package com.smartism.znzk.activity.common;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.fragment.AlarmFragment;
import com.smartism.znzk.fragment.NaoZhongFragment;
import com.smartism.znzk.xiongmai.widget.PickerParentLayout;

import java.util.List;

public class HongCaiSettingActivity extends ActivityParentActivity implements NaoZhongFragment.PuZhuoBackEvent {
    public final static String FLASH_AND_ALARM = "com.smartism.znzk.activity.AlarmFragment_FLASH_ALARM";
    Fragment mShowFragment ;
    TextView tv_title,save;
    ImageView iv_back ;
    final int ALARM_SETTING=1;
    final int NAOZHONG_SETTING=2;
    int currentShowSetting = -1;
    private long zhuji_id = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hong_cai_setting);
        save = findViewById(R.id.save);
        tv_title = findViewById(R.id.tv_title);
        iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!puZhuoBackEvent()){
                    finish();
                }
            }
        });
        mShowFragment = getFragmentManager().findFragmentById(R.id.settingContent);
        if(mShowFragment==null) {
            //表示活动正常启动,通过setArguments方法已经给Fragment传递进了参数，不用处理非正常启动时的情况了
            currentShowSetting = getIntent().getIntExtra("whatsetting", -1);
            zhuji_id = getIntent().getLongExtra("zhuji_id", -1l);
            showFragment(currentShowSetting, zhuji_id);
            save.setOnClickListener((View.OnClickListener) mShowFragment);
        }

    }

    private void showFragment(int flag, long zhuji_id){
        Bundle bundle = new Bundle();
        List<CommandInfo> temp = DatabaseOperator.getInstance().queryAllCommands(zhuji_id);
        bundle.putLong("zhuji_id",zhuji_id);
        switch (flag){
            case ALARM_SETTING:
                if(temp!=null&&temp.size()>0){
                    for(CommandInfo commandInfo :temp){
                        if(commandInfo.getCtype().equals("109")){
                            bundle.putSerializable("zhuji_setting",commandInfo);
                        }
                    }
                }
                tv_title.setText(R.string.hongcai_alarm_setting_title);
                mShowFragment = AlarmFragment.getAlarmFragment(bundle);
                mShowFragment.setArguments(bundle);
                break;
            case NAOZHONG_SETTING:
                if(temp!=null&&temp.size()>0){
                    for(CommandInfo commandInfo :temp){
                        if(commandInfo.getCtype().equals("111")){
                            bundle.putSerializable("zhuji_setting",commandInfo);
                        }
                    }
                }
                tv_title.setText(getString(R.string.hongcai_naozhong_title));
                mShowFragment = NaoZhongFragment.getNaoZhongFragment(bundle);
                mShowFragment.setArguments(bundle);
                break;
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.settingContent,mShowFragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if(!puZhuoBackEvent()){
            super.onBackPressed();
        }
    }

    @Override
    public boolean puZhuoBackEvent() {
        if(mShowFragment instanceof NaoZhongFragment){
            //是闹钟设置
            NaoZhongFragment fragment = (NaoZhongFragment) mShowFragment;
            if(fragment.mediaPlayer!=null){
                fragment.mediaPlayer.release();//释放资源
            }
            View v = fragment.getView().findViewById(R.id.music_choice_list_view);
            if(v.getVisibility()==View.VISIBLE){
                //把他隐藏掉
                v.setVisibility(View.GONE);
                return true;
            }
        }
        return false ;
    }

    //由于音频资源放置在hongcai版本中，使用非宏才版本时，需要将下面的资源代码进行注释掉,减少智慧APK体积
    public static int handleChoiceMusic(int position){
        switch (position){
//            case 0 :
//                return R.raw.one;
//            case 1 :
//                return R.raw.two;
//            case 2 :
//                return R.raw.three;
//            case 3 :
//                return R.raw.four;
//            case 4 :
//                return R.raw.five;
//            case 5 :
//                return R.raw.six;
//            case 6 :
//                return R.raw.seven;
//            case 7 :
//                return R.raw.eight;
//            case 8 :
//                return R.raw.nine;
//            case 9 :
//                return R.raw.ten;
//            case 10 :
//                return R.raw.shiyi;
//            case 11:
//                return R.raw.shier;
//            case 12:
//                return R.raw.shisan;
//            case 13:
//                return R.raw.shisi;
//            case 14:
//                return R.raw.shiwu;
//            case 15:
//                return R.raw.shiliu;
//            case 16:
//                return R.raw.shiqi;
//            case 17:
//                return R.raw.shiba;
//            case 18:
//                return R.raw.shijiu;
//            case 19:
//                return R.raw.ershi;
//            case 20:
//                return R.raw.eryi;
//            case 21:
//                return R.raw.ershier;
//            case 22:
//                return R.raw.ershisan;
//            case 23:
//                return R.raw.ershisi;
//            case 24:
//                return R.raw.ershiwu;
//            case 25:
//                return R.raw.ershiliu;
//            case 26:
//                return R.raw.ershiqi;
//            case 27:
//                return R.raw.ershiba;
//            case 28:
//                return R.raw.ershijiu;
//            case 29:
//                return R.raw.sanshi;
//            case 30:
//                return R.raw.sanshiyi;
//            case 31:
//                return R.raw.sanshier;
//            case 32:
//                return R.raw.sanshisan;
        }
        return -1;
    }
}
