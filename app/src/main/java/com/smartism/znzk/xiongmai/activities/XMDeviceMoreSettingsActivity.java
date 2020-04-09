package com.smartism.znzk.xiongmai.activities;


import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.lib.MsgContent;
import com.lib.SDKCONST;
import com.smartism.znzk.R;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.view.SwitchButton.SwitchButton;
import com.smartism.znzk.xiongmai.devices.tour.listener.TourContract;
import com.smartism.znzk.xiongmai.devices.tour.model.bean.PTZTourBean;
import com.smartism.znzk.xiongmai.devices.tour.presenter.TourPresenter;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunSupport;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevice;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 雄迈摄像头，高级配置 ，实现方式，拷贝demo中的代码实现。
 * @author 王建
 */
public class XMDeviceMoreSettingsActivity extends MZBaseActivity implements TourContract.ITourView, View.OnClickListener {

    private SwitchButton mDetectTrackSwitch = null;
    private FunDevice mFunDevice = null;
    private TextView mSensitivityTv ;
    BottomSheetDialog mBottomSheetDialog ;
    private View mSensitivityParent;
    private TourContract.ITourPresenter tourPresenter;  // P
    private String mDeviceSn  ;
    private View mBossGroup ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置标题
        setTitle(getResources().getString(R.string.xm_more_setting));
        if(savedInstanceState==null){
            mDeviceSn = getIntent().getStringExtra("sn");
        }else{
            mDeviceSn= savedInstanceState.getString("sn");
        }

        //初始化控件
        initView();

        //初始化灵敏度设置选项
        initBottomSheetDialog();

        //初始化信息
        initData();
    }

    private void initView(){
        mBossGroup  = findViewById(R.id.boss_group);
        mDetectTrackSwitch  = findViewById(R.id.detectTrackSwitchBtn);
        mDetectTrackSwitch.setOnCheckedChangeListener((buttonView,isChecked) -> {
            tourPresenter.setDetectTrackSwitch(isChecked?SDKCONST.Switch.Open:SDKCONST.Switch.Close);
        });
        mSensitivityParent = findViewById(R.id.sensitivity_parent);
        mSensitivityParent.setOnClickListener(this);
        mSensitivityTv = findViewById(R.id.sensitivityTv);
    }

    private void initData() {

        FunDevice funDevice = FunSupport.getInstance().findDeviceBySn(mDeviceSn);
        if ( null == funDevice ) {
            finish();
            return;
        }
        mFunDevice  = funDevice ;

        tourPresenter = new TourPresenter(this, this,mFunDevice);
//        tourPresenter.getTour(); //定时巡航功能  暂时不启用
//        tourPresenter.getTimimgPtzTour();//定时巡航功能  暂时不启用
        tourPresenter.getDetectTrack();

//        if (FunSDK.GetDevAbility(funDevice.getDevSn(),"OtherFunction/SupportTimingPtzTour") > 0) {
////            tvTimimgPtzTourSupport.setText("定时巡航功能是支持的，可以正常操作");
//        }else {
////            tvTimimgPtzTourSupport.setText("定时巡航功能是不支持，不可以操作");
//        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("sn",mDeviceSn);
        super.onSaveInstanceState(outState);
    }

    ListView listView ;
    List<String> valuesOff = new ArrayList<>();
    void initBottomSheetDialog(){
        //灵敏度 低  中 高
        String[] value = getResources().getStringArray(R.array.xmdss_sensitivity_values);
        for(int i=0;i<value.length;i++){
            valuesOff.add(value[i]);
        }
        mBottomSheetDialog = new BottomSheetDialog(this);
        View view = (View) getLayoutInflater().inflate(R.layout.zhuji_setting_offline,null);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getResources().getDisplayMetrics().heightPixels/4);
        view.setLayoutParams(lp);
        listView = view.findViewById(R.id.bottom_lv);
        listView.setAdapter(new ArrayAdapter(this,android.R.layout.simple_list_item_single_choice,valuesOff));
        mBottomSheetDialog.setContentView(view);
        listView.setOnItemClickListener((parent, v, position, id) -> {
                //发送指令
                mBottomSheetDialog.dismiss();
                //如果用户选中的清晰度和当前的一致，则不进行更改，直接返回
                if(valuesOff.get(position).equals(mSensitivityTv.getText().toString())){
                    return ;
                }else{
                    tourPresenter.setSensitivity(position);
                }
        });
        //把BottomSheetDialog的背景透明，以便显示出我们的背景
        ViewGroup viewGroup = (ViewGroup) view.getParent();
        viewGroup.setBackgroundColor(Color.parseColor("#00000000"));
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_xmdevice_more_settings_layout;
    }

    @Override
    protected void onDestroy() {
        tourPresenter.removeAllCallback();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sensitivity_parent:
                if(!mBottomSheetDialog.isShowing()){
                    int index = valuesOff.indexOf(mSensitivityTv.getText().toString());
                    listView.performItemClick(listView,index>=0?index:0,index>=0?index:0);//设置默认的选中项
                    mBottomSheetDialog.show();
                }
                break ;
        }
    }

    //以下V接口实现

    @Override
    public void onLoadTours(@Nullable PTZTourBean tourBean) {

    }

    @Override
    public void onTourAdded(int presetId) {

    }

    @Override
    public void onTourDeleted(int presetId) {

    }

    @Override
    public void onTourReseted(int presetId) {

    }

    @Override
    public void onTourStarted() {

    }

    @Override
    public void onTourStoped() {

    }

    @Override
    public void onTour360Started() {

    }

    @Override
    public void onTour360Stoped() {

    }

    @Override
    public void onTourCleared() {

    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void showLoading(boolean cancelable, String info) {
        showProgress(info);
    }

    @Override
    public void dismissLoading() {
        hideProgress();
    }

    @Override
    public void onFailed(Message msg, MsgContent ex, String extraStr) {
        hideProgress();
        if (msg != null && ex != null) {
            ToastUtil.longMessage(getString(R.string.error) + msg.arg1);
        } else if (extraStr != null) {
            ToastUtil.longMessage(extraStr);
        }
    }

    @Override
    public void onTmimgPtzTourResult(boolean isEnable, int timeInterval) {

    }

    @Override
    public void onSaveTimimgPtzTourResult(boolean isSuccess) {

    }

    @Override
    public void onUpdateDetectTrack(boolean enable, int sensitivity) {
        hideProgress();
        mSensitivityTv.setText(valuesOff.get(sensitivity));
        //判断一下当前选中状态是否和要设置的相同，防止控件有动画时会跳掉动画。
        if (enable != mDetectTrackSwitch.isChecked()){
            mDetectTrackSwitch.setCheckedImmediatelyNoEvent(enable);
        }
    }

    @Override
    public void onSaveDetectTrack(boolean isSuccess) {
        hideProgress();
        if (isSuccess) {
            ToastUtil.shortMessage(getString(R.string.activity_editscene_modify_success));
        }
    }
}
