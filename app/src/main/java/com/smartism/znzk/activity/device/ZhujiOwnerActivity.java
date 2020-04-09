package com.smartism.znzk.activity.device;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.domain.AreaInfo;
import com.smartism.znzk.domain.OwenerInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.cityweel.SelectAddreActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZhujiOwnerActivity extends SelectAddreActivity implements View.OnClickListener {

    private EditText etname, etphone, etaddress, ettel;
    private TextView zhujiname, rightMenu;
    private Button city;
//    private ImageView zhujilogo;
    private ZhujiInfo zhujiInfo;
    private ImageView back;
    private LinearLayout parent;
    private List<AreaInfo> sList;
    private List<AreaInfo> cList;
    private List<AreaInfo> qList;
    private OwenerInfo owenerInf;
    private boolean flag;
    private final int TIME_OUT = 1, SUCESS_REQUREST = 0;


    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == SUCESS_REQUREST) {
                handler.removeMessages(TIME_OUT);
                cancelInProgress();
                String textStr = rightMenu.getText().toString().trim();
                if (getResources().getString(R.string.activity_zhujiowner_edit).equals(textStr)) {
                    rightMenu.setText(getResources().getString(R.string.activity_zhujiowner_sure));
                    setEditEnable(true);
                    //获取编辑框焦点
                    etname.requestFocus();
                } else if (getResources().getString(R.string.activity_zhujiowner_sure).equals(textStr)) {
                    rightMenu.setText(getResources().getString(R.string.activity_zhujiowner_edit));
                    setEditEnable(false);
                }
            }
            if (msg.what == TIME_OUT) {
                initViewDate();
                Toast.makeText(ZhujiOwnerActivity.this, getResources().getString(R.string.time_out), Toast.LENGTH_SHORT).show();
                cancelInProgress();
            }
            return false;
        }
    };
    private Handler handler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhuji_owner);
        initView();
        initDate();
        initEvent();

        //易迅格更换主机显示图片
//        if(Actions.VersionType.CHANNEL_YIXUNGE.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
//            zhujilogo.setImageResource(R.drawable.zhzj_addhost_host);
//        }
    }

    private void initEvent() {
        back.setOnClickListener(this);
        rightMenu.setOnClickListener(this);
        city.setOnClickListener(this);
    }

    public void initViewDate() {
        zhujiInfo = (ZhujiInfo) getIntent().getSerializableExtra("zhuji");
        owenerInf = (OwenerInfo) getIntent().getSerializableExtra("owenerInf");
//        zhujiname.setText(zhujiInfo.getName());
        setEditEnable(false);
//        ImageLoader.getInstance().displayImage(
//                 dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/" + zhujiInfo.getLogo(),
//                zhujilogo, new ImageLoadingBar());
        rightMenu.setText(getResources().getString(R.string.activity_zhujiowner_edit));
        if (owenerInf == null) {
            owenerInf = new OwenerInfo();
        } else {
            etname.setText(owenerInf.getUserName());
            etphone.setText(owenerInf.getUserPhone());
            etaddress.setText(owenerInf.getUserAddress());
            ettel.setText(owenerInf.getUserTel());
            city.setText(owenerInf.getUserAreaInfo());
        }
    }

    private void initDate() {
        sList = new ArrayList<>();
        cList = new ArrayList<>();
        qList = new ArrayList<>();
        initViewDate();
    }

    private void initView() {
        parent = (LinearLayout) findViewById(R.id.activity_zhuji_owner);
        etname = (EditText) findViewById(R.id.activity_zhuji_owner_name);
        etphone = (EditText) findViewById(R.id.activity_zhuji_owner_phone);
        etaddress = (EditText) findViewById(R.id.activity_zhuji_owner_address);
        ettel = (EditText) findViewById(R.id.activity_zhuji_owner_tel);
//        zhujiname = (TextView) findViewById(R.id.activity_zhuji_owner_zhujiname);
        city = (Button) findViewById(R.id.activity_zhuji_owner_city);
//        zhujilogo = (ImageView) findViewById(R.id.activity_zhuji_owner_logo);
        back = (ImageView) findViewById(R.id.back);
        rightMenu = (TextView) findViewById(R.id.activity_zhuji_owner_save);

    }

    public void setEditEnable(boolean isEnable) {
        setEditEnable(etname, isEnable);
        setEditEnable(etphone, isEnable);
        setEditEnable(etaddress, isEnable);
        setEditEnable(ettel, isEnable);

    }

    public void setEditEnable(View view, boolean isEnable) {
        flag = isEnable;
        if (isEnable) {
            view.setFocusableInTouchMode(true);
            view.setFocusable(true);
            view.setClickable(true);
        } else {
            view.setFocusable(false);
            view.setFocusableInTouchMode(false);
            view.setClickable(false);
        }
    }

    /**
     * 地址返回
     *
     * @param areaInfos
     * @param id        标识从那个级别开始 1、洲 2、国家 3、省/州 4、市 5、区
     */
    @Override
    public void result(List<AreaInfo> areaInfos, int id) {
        super.result(areaInfos, id);
        if(MainApplication.app.getAppGlobalConfig().isDebug()
                ||Actions.VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            StringBuffer sb = new StringBuffer();
            List<Integer> useAreaLevel = new ArrayList<>();//保存已经设置过的areaLevel
            if (id == 1) {
                for (int i = 1; i < areaInfos.size(); i++) {
                    sb.append(areaInfos.get(i).getAreaName());
                    sb.append(" ");
                }
            } else {
                for (int i = 0; i < areaInfos.size(); i++) {
                    sb.append(areaInfos.get(i).getAreaName());
                    sb.append(" ");
                }
            }
            owenerInf.setUserAreaInfo(sb.toString());//设置地区信息
            for(int i=0;i<areaInfos.size();i++){
                AreaInfo tmp = areaInfos.get(i);
                if(!useAreaLevel.contains(tmp.getAreaLevel())){
                    switch (tmp.getAreaLevel()){
                        case 1:
                            //洲
                            break ;
                        case 2:
                            //国家
                            owenerInf.setUserCountryId(tmp.getId());
                            break ;
                        case 3:
                            //省
                            owenerInf.setUserAreaId(tmp.getId());
                            break ;
                        case 4:
                            //市
                            owenerInf.setUserCityId(tmp.getId());
                            break ;
                        case 5:
                            //区/县
                            owenerInf.setUserCountyId(tmp.getId());
                            break ;
                        case 6:
                            //街道
                            owenerInf.setUserStreetId(tmp.getId());
                            break ;
                        case 7:
                            //小区
                            owenerInf.setUserCommunityId(tmp.getId());
                            break ;
                    }
                }
                useAreaLevel.add(tmp.getAreaLevel());
            }
            city.setText(owenerInf.getUserAreaInfo().trim());
        }else{
            StringBuffer sb = new StringBuffer();
            if (id == 1) {
                for (int i = 1; i < areaInfos.size(); i++) {
                    sb.append(areaInfos.get(i).getAreaName());
                    sb.append(" ");
                }
            } else {
                for (int i = 0; i < areaInfos.size(); i++) {
                    sb.append(areaInfos.get(i).getAreaName());
                    sb.append(" ");
                }
            }
            int sub = 2 - id;
            if (sub > -1 && areaInfos.size() > sub) {
                owenerInf.setUserCountryId(areaInfos.get(sub).getId());
            }
            sub = sub + 1;
            if (sub > -1 && areaInfos.size() > sub) {
                owenerInf.setUserAreaId(areaInfos.get(sub).getId());
            }
            sub = sub + 1;
            if (sub > -1 && areaInfos.size() > sub) {
                owenerInf.setUserCityId(areaInfos.get(sub).getId());
            }
            sub = sub + 1;
            if (sub > -1 && areaInfos.size() > sub) {
                owenerInf.setUserCountyId(areaInfos.get(sub).getId());
            }
            if (Actions.VersionType.CHANNEL_WOAIJIA.equals(MainApplication.app.getAppGlobalConfig().getVersion()))
                owenerInf.setUserCountryId(100000);
            String str = sb.toString();
            owenerInf.setUserAreaInfo(str.trim());
            city.setText(str.trim());
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.activity_zhuji_owner_save:
                String textStr = rightMenu.getText().toString().trim();
                if (getString(R.string.activity_zhujiowner_edit).equals(textStr)) {
                    handler.sendEmptyMessage(SUCESS_REQUREST);
                } else if (getString(R.string.activity_zhujiowner_sure).equals(textStr)) {
                    String name = etname.getText().toString().trim();
                    String phone = etphone.getText().toString().trim();
                    String tel = ettel.getText().toString().trim();
                    String address = etaddress.getText().toString().trim();
                    owenerInf.setId(zhujiInfo.getId());
                    owenerInf.setMasterId(zhujiInfo.getMasterid());
                    if (name == null || "".equals(name)) {
                        Toast.makeText(mContext,getString(R.string.zhuji_owner_msg_name),Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (phone == null || "".equals(phone)) {
                        Toast.makeText(mContext,getString(R.string.zhuji_owner_msg_phone),Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (tel == null || "".equals(tel)) {
                        Toast.makeText(mContext,getString(R.string.zhuji_owner_msg_tell),Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (address == null || "".equals(address)) {
                        Toast.makeText(mContext,getString(R.string.zhuji_owner_msg_addr),Toast.LENGTH_SHORT).show();
                        return;
                    }
                    owenerInf.setUserName(name);
                    owenerInf.setUserPhone(phone);
                    owenerInf.setUserTel(tel);
                    owenerInf.setUserAddress(address);
                    handler.sendEmptyMessageDelayed(TIME_OUT, 10 * 1000);
                    showInProgress(getString(R.string.loading), false, true);
                    JavaThreadPool.getInstance().excute(new SaveOwener());
                }
                break;
            case R.id.activity_zhuji_owner_city:
                if (flag) {
                    showSelectWin(parent);
                }
                break;
        }
    }

    class SaveOwener implements Runnable {
        @Override
        public void run() {
            JSONObject o = new JSONObject();
            o.put("id", owenerInf.getId());
            o.put("masterId", owenerInf.getMasterId());

            o.put("userAreaId", owenerInf.getUserAreaId());
            o.put("userCityId", owenerInf.getUserCityId());
            o.put("userCountyId", owenerInf.getUserCountyId());
            o.put("userCountryId", owenerInf.getUserCountryId());
            o.put("userStreetId",owenerInf.getUserStreetId());
            o.put("userCommunityId",owenerInf.getUserCommunityId());

            o.put("userAddress", owenerInf.getUserAddress());
            o.put("userAreaInfo", owenerInf.getUserAreaInfo());

            o.put("userName", owenerInf.getUserName());
            o.put("userPhone", owenerInf.getUserPhone());
            o.put("userTel", owenerInf.getUserTel());
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            String result = HttpRequestUtils
                    .requestoOkHttpPost(
                             server + "/jdm/s3/dzj/update", o, ZhujiOwnerActivity.this);
            // -1参数为空 -2校验失败 -10服务器不存在
            if (result != null && "0".equals(result)) {
                Log.e("1参数为空-result", "result-" + result);
                handler.sendEmptyMessage(SUCESS_REQUREST);
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        initViewDate();
                    }
                });
            }

        }
    }

    // 点击空白区域隐藏软键盘
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            // 隐藏软键盘
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
        return super.onTouchEvent(event);
    }
}
