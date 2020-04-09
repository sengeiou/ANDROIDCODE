package com.smartism.znzk.activity.device;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.share.ShareDevicesActivity;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.OwenerInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;
import com.smartism.znzk.zhicheng.tasks.LoadZhujiAndDeviceTask;


public class SingleProductDetailActivity extends MZBaseActivity implements View.OnClickListener, HttpAsyncTask.IHttpResultView {

    private LinearLayout mLinearZhujiInfo ,mLinearZhujiOwner ,mLinearZhujiUser;
    private ImageView mLogoImg ;
    private OwenerInfo mOwenerInfo;
    private DeviceInfo mDeviceInfo ;
    private ZhujiInfo mZhujiInfo ;
    private TextView mZhujiInfoTv ,mZhujiOwner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLinearZhujiInfo = findViewById(R.id.ll_zhuji_info);
        mLinearZhujiOwner = findViewById(R.id.layout_zhuji_owner);
        mLinearZhujiUser = findViewById(R.id.ll_users);
        mLogoImg = findViewById(R.id.logo_img);
        mZhujiInfoTv = findViewById(R.id.tv_zhuji_info);
        mZhujiOwner = findViewById(R.id.tv_zhuji_owenr);


        mLinearZhujiOwner.setOnClickListener(this);
        mLinearZhujiInfo.setOnClickListener(this);
        mLinearZhujiUser.setOnClickListener(this);

        if(savedInstanceState==null){
            mDeviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        }else{
            mDeviceInfo = (DeviceInfo) savedInstanceState.getSerializable("device");
        }

        requestOwner();
        initViewStatus();

        ImageLoader.getInstance().displayImage(dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "")
                + "/devicelogo/" + mDeviceInfo.getLogo(), mLogoImg, options, new MImageLoadingBar());
    }

    private  void initViewStatus(){
        //设置标题
        if(mDeviceInfo.getCa().equals(DeviceInfo.CaMenu.nbyg.value())){
            setTitle(getString(R.string.deviceinfo_activity_singleproduct_title,getString(R.string.deviceinfo_activity_singleproduct_nbyg)));
            mZhujiInfoTv.setText(getString(R.string.deviceinfo_activity_singleproduct_info,getString(R.string.deviceinfo_activity_singleproduct_nbyg)));
            mZhujiOwner.setText(getString(R.string.deviceinfo_activity_singleproduct_ower,getString(R.string.deviceinfo_activity_singleproduct_nbyg)));
        }else{
            setTitle(getString(R.string.deviceinfo_activity_zhujimanager_title));
        }

        //获取一下主机
        new LoadZhujiAndDeviceTask().queryZhujiInfoByZhuji(mDeviceInfo.getZj_id(), new LoadZhujiAndDeviceTask.ILoadResult<ZhujiInfo>() {
            @Override
            public void loadResult(ZhujiInfo result) {
                if(result!=null){
                    mZhujiInfo = result ;
                    if(mZhujiInfo.isAdmin()){
                        mLinearZhujiOwner.setVisibility(View.VISIBLE);
                    }else{
                        mLinearZhujiOwner.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("device",mDeviceInfo);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mZhujiInfo!=null&&mZhujiInfo.isAdmin()){
            getMenuInflater().inflate(R.menu.shared_menu,menu);
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.share_item:
                Intent intent =   new Intent();
                intent.putExtra("pattern", "status_forver");
                intent.setClass(getApplicationContext(), ShareDevicesActivity.class);
                intent.putExtra("shareid",mDeviceInfo!=null?mDeviceInfo.getId():0);
                startActivity(intent);
            return true ;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private  void requestOwner(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",mDeviceInfo.getId());
        new HttpAsyncTask(this,HttpAsyncTask.Zhuji_OWERN_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,jsonObject);
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_single_product_detail_layout;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.ll_zhuji_info:
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(),ZhujiInfoActivity.class);
                intent.putExtra("device", mDeviceInfo);
                intent.putExtra("owenerInf", mOwenerInfo);
                startActivity(intent);
                break  ;
            case R.id.layout_zhuji_owner:
                if(mOwenerInfo==null){
                    return ;
                }
                new LoadZhujiAndDeviceTask().queryZhujiInfoByZhuji(mDeviceInfo.getZj_id(), new LoadZhujiAndDeviceTask.ILoadResult<ZhujiInfo>() {
                    @Override
                    public void loadResult(ZhujiInfo result) {
                        if(result!=null){
                            Intent intent = new Intent();
                            intent.setClass(getApplicationContext(),ZhujiOwnerActivity.class);
                            intent.putExtra("zhuji",result);
                            intent.putExtra("owenerInf", mOwenerInfo);
                            startActivity(intent);
                        }
                    }
                });
                break ;
            case R.id.ll_users:
                Intent tempIntent = new Intent();
                tempIntent.putExtra("device", mDeviceInfo);
                tempIntent.setClass(this, PerminssonTransActivity.class);
                startActivity(tempIntent);
                break ;
        }
    }

    @Override
    public void setResult(int flag, String result) {
        if(flag==HttpAsyncTask.Zhuji_OWERN_URL_FLAG){
            if (result != null && result.length() > 4) {
                JSONObject JSONobj = JSONObject.parseObject(result);
                mOwenerInfo = new OwenerInfo();
                mOwenerInfo.setId(Long.parseLong(JSONobj.getString("dId")));
                mOwenerInfo.setMasterId(JSONobj.getString("masterId"));
                if (JSONobj.getString("serviceTime") != null) {
                    mOwenerInfo.setServiceTime(Long.parseLong(JSONobj.getString("serviceTime")));
                }
                mOwenerInfo.setUserAddress(JSONobj.getString("userAddress"));
                mOwenerInfo.setUserAreaInfo(JSONobj.getString("userAreaInfo"));
                mOwenerInfo.setUserAreaId(Long.parseLong(JSONobj.getString("userAreaId")));
                mOwenerInfo.setUserCityId(Long.parseLong(JSONobj.getString("userCityId")));
                mOwenerInfo.setUserCountyId(Long.parseLong(JSONobj.getString("userCountyId")));
                mOwenerInfo.setUserName(JSONobj.getString("userName"));
                mOwenerInfo.setUserPhone(JSONobj.getString("userPhone"));
                mOwenerInfo.setUserTel(JSONobj.getString("userTel"));
            }
        }
    }

    // 显示图片的配置
    DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
            // .displayer(new RoundedBitmapDisplayer(20))//是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();

    public class MImageLoadingBar implements ImageLoadingListener {

        @Override
        public void onLoadingCancelled(String arg0, View arg1) {
            if (arg1 != null)
                arg1.clearAnimation();
        }

        @Override
        public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
            if (arg1 != null)
                arg1.clearAnimation();
        }

        @Override
        public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
            if (arg1 != null)
                arg1.clearAnimation();
        }

        @Override
        public void onLoadingStarted(String arg0, View arg1) {
            if (arg1 != null)
                arg1.startAnimation(imgloading_animation);
        }
    }
}
