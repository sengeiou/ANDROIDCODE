package com.smartism.znzk.activity.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.amplify.generated.graphql.DeleteCtrUserDeviceRelationsMutation;
import com.amazonaws.amplify.generated.graphql.DeleteCtrUserGroupMutation;
import com.amazonaws.amplify.generated.graphql.ListCtrDeviceFirmwaresQuery;
import com.amazonaws.amplify.generated.graphql.ListCtrUserDeviceRelationsQuery;
import com.amazonaws.amplify.generated.graphql.ListCtrUserGroupsQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.bumptech.glide.Glide;
import com.hjq.toast.ToastUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.CommonWebViewActivity;
import com.smartism.znzk.activity.HeaterActivity;
import com.smartism.znzk.activity.alert.ChooseAudioSettingMode;
import com.smartism.znzk.activity.camera.CameraListActivity;
import com.smartism.znzk.activity.common.HongCaiSettingActivity;
import com.smartism.znzk.activity.device.add.AddSelectActivity;
import com.smartism.znzk.activity.device.add.AddZhujiWayChooseActivity;
import com.smartism.znzk.activity.weather.WeatherInfoActivity;
import com.smartism.znzk.adapter.ZhujiListExpandableAdapter;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.awsClient.AWSClients;
import com.smartism.znzk.communication.connector.SyncClientAWSMQTTConnector;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.HeaterShadowInfo;
import com.smartism.znzk.domain.ImageBannerInfo;
import com.smartism.znzk.domain.ZhujiGroupInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.StringUtils;
import com.smartism.znzk.util.TemperatureUtil;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.DevicesMenuPopupWindow;
import com.smartism.znzk.view.SelectAddPopupWindow;
import com.smartism.znzk.view.TextViewAutoVerticalScroll;
import com.smartism.znzk.view.alertview.AlertView;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import type.DeleteCtrUserDeviceRelationsInput;
import type.DeleteCtrUserGroupInput;
import type.TableCtrDeviceFirmwareFilterInput;
import type.TableCtrUserDeviceRelationsFilterInput;
import type.TableCtrUserGroupFilterInput;
import type.TableStringFilterInput;

/**
 * Created by 王建 on 2017/4/22.
 * 主机列表
 */

public class ZhujiListFragment extends Fragment implements ZhujiListExpandableAdapter.ZhujiListAdapterOnclick,View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static String MASTER_ID = "";//采用静态字段代替写入数据库的master_id
    private final int
            dHandler_scenes = 1,
            dHandler_loadsuccess = 2,
            dHandler_loadsuccess_group = 200,
            dHandler_timeout = 3,
            dHandler_timeout_binupdate = 30,
            dHandlerWhat_serverupdatetimeout = 4,
            dHandler_notice = 5,
            dHandler_experzhujis = 6,
            dHandler_experzhuji = 7,
            dHandler_banner_top = 21,
            dHandler_banner_bottom = 22,
            dHandler_initContast = 12;

    private GridView airListGridView,pestListGridView;
    private MyGridAdapter airAdapter,pestAdapter;

    //天气
    private ImageView iconWeather;
    private TextView textWeatherMain,textWeatherTemp,textWeatherPm25,textWeatherAirQuality;
    private RelativeLayout layoutWeather;


    private List<ImageBannerInfo.ImageBannerBean> bannerBeanTopList,bannerBeanBottomList; //轮播广告
    private Banner banner_bottom,banner_top;
    private final int SEARCH_MASTER_HUBCOUNT = 20;//主机数量大于多少个时显示搜索框。
    private DeviceMainActivity mContext;
    private int sortType = 0;
    //分组的主机列表
    private ExpandableListView zj_ExpandListView;
    private List<ZhujiGroupInfo> groupInfoList;
    private List<List<ZhujiInfos>> zhujiInGroupList;
    private ZhujiListExpandableAdapter expandableAdapter;
    //搜索框
    private LinearLayout ll_search;
    private EditText iv_search;
    private ImageView iv_search_close;
    private boolean iv_search_havetxt;//表示搜索框是否有值，默认无，在从有值变为无值时，需要关闭所有的分组，打开最后一个 不然这个时候切换第一个分组也会打开

    private DevicesMenuPopupWindow itemMenu; //底部菜单
    private DeviceInfo operationDevice;
    private JSONObject operationReported;
    private List<ZhujiInfo> experInfos = new ArrayList<>();
    private SelectAddPopupWindow experWindow;
    private LinearLayout ll_device_main;
    private SwipeRefreshLayout mRefreshLayout;
    private TextViewAutoVerticalScroll textview_auto_roll;//公告
    private String[] adv;
    private int number = 0;
    private LinearLayout ll_notice;
    private ImageView iv_notice;
    private ImageView iv_close;
    private ZhujiInfo experZhuji; //暂时保存选中的体验主机信息请求开始体验后需要设置为null
    private View listViewHeadView;//断开连接提示信息
    public String newMasterId;
    private AlertView updateAlertView ; //更新固件Dialog

    private ZhujiInfo zjinfo;

    private int total = 0;
    private List<String> list = new ArrayList<>();

    // 显示图片的配置
    DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
            // .displayer(new RoundedBitmapDisplayer(20))//是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();

    //重置MASTER_ID
    public static void resetMasterId(){
        MASTER_ID="";
    }

    public static void setMasterId(String id){
        if(id==null||id.equals("")){
            return ;
        }
        MASTER_ID = id ;
    }

    public static String getMasterId(){
        return MASTER_ID;
    }

    public void refrushDate() {

        AWSClients.getInstance().getmAWSAppSyncClient().query(ListCtrUserDeviceRelationsQuery.builder()
                .filter(TableCtrUserDeviceRelationsFilterInput.builder()
                        .uid(TableStringFilterInput.builder().eq(AWSMobileClient.getInstance().getUsername()).build())
                        .build())
                .build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryRelatioinsCallback);

        AWSClients.getInstance().getmAWSAppSyncClient().query(ListCtrUserGroupsQuery.builder()
                .filter(TableCtrUserGroupFilterInput.builder()
                        .uid(TableStringFilterInput.builder().eq(AWSMobileClient.getInstance().getUsername()).build())
                        .build())
                .build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryGroupCallback);
    }

    private GraphQLCall.Callback<ListCtrUserDeviceRelationsQuery.Data> queryRelatioinsCallback = new GraphQLCall.Callback<ListCtrUserDeviceRelationsQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListCtrUserDeviceRelationsQuery.Data> response) {
            Log.i("Results", response.data().listCtrUserDeviceRelations().items().toString());

            List<ZhujiInfo> airAndGroupList = new ArrayList<>();
            List<ZhujiInfo> pestAndGroupList = new ArrayList<>();
            List<ListCtrUserDeviceRelationsQuery.Item> allZhujiInfos = response.data().listCtrUserDeviceRelations().items();
            if (!CollectionsUtils.isEmpty(allZhujiInfos)) {
                for (ListCtrUserDeviceRelationsQuery.Item info : allZhujiInfos) {
                    ZhujiInfo zhujiInfo = Util.itemToZhujiInfo(info);
                    if (ZhujiInfo.CtrDeviceType.INSECTICIDE_SINGLE_REFILL.equalsIgnoreCase(zhujiInfo.getType())){
                        pestAndGroupList.add(zhujiInfo);
                    }else{
                        airAndGroupList.add(zhujiInfo);
                    }
                }
            }
            Message message = defaultHandler.obtainMessage(dHandler_loadsuccess);
            List<Object> obj = new ArrayList<>();
            obj.add(airAndGroupList);
            obj.add(pestAndGroupList);
            message.obj = obj;
            defaultHandler.sendMessage(message);
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };
    private GraphQLCall.Callback<ListCtrUserGroupsQuery.Data> queryGroupCallback = new GraphQLCall.Callback<ListCtrUserGroupsQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListCtrUserGroupsQuery.Data> response) {
            Log.i("Results", response.data().listCtrUserGroups().items().toString());
            List<ZhujiInfo> airAndGroupList = new ArrayList<>();
            List<ZhujiInfo> pestAndGroupList = new ArrayList<>();
            List<ListCtrUserGroupsQuery.Item> allGroupInfos = response.data().listCtrUserGroups().items();
            if (!CollectionsUtils.isEmpty(allGroupInfos)) {
                for (ListCtrUserGroupsQuery.Item info : allGroupInfos) {
                    ZhujiInfo zhujiInfo = Util.itemToZhujiInfo(info);
                    if (ZhujiInfo.CtrDeviceType.INSECTICIDE_SINGLE_GROUP.equalsIgnoreCase(zhujiInfo.getType())){
                        pestAndGroupList.add(zhujiInfo);
                    }else{
                        airAndGroupList.add(zhujiInfo);
                    }
                }
            }
            Message message = defaultHandler.obtainMessage(dHandler_loadsuccess_group);
            List<Object> obj = new ArrayList<>();
            obj.add(airAndGroupList);
            obj.add(pestAndGroupList);
            message.obj = obj;
            defaultHandler.sendMessage(message);
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };

    class MyGridAdapter extends BaseAdapter {

        private List<ZhujiInfo> mGridList;

        public List<ZhujiInfo> getmGridList() {
            return mGridList;
        }

        MyGridAdapter(List<ZhujiInfo> zhujiInfoList){
            mGridList = zhujiInfoList;
        }

        public void clearZhuji(){
            if (!CollectionsUtils.isEmpty(mGridList)){
                for (int i = 0; i < mGridList.size(); i++) {
                    if (DeviceInfo.CakMenu.zhuji.value().equals(mGridList.get(i).getCak())){
                        mGridList.remove(i);
                        i--;
                    }
                }
            }
        }

        public void clearGroup(){
            if (!CollectionsUtils.isEmpty(mGridList)){
                for (int i = 0; i < mGridList.size(); i++) {
                    if (DeviceInfo.CakMenu.group.value().equals(mGridList.get(i).getCak())){
                        mGridList.remove(i);
                        i--;
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return mGridList.size();
        }

        @Override
        public Object getItem(int position) {
            return mGridList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(mContext, R.layout.fragment_zhujilist_grid_item, null);
                holder.bg = (LinearLayout) convertView.findViewById(R.id.zhujilist_grid_item_layout);
                holder.iv = (ImageView) convertView.findViewById(R.id.logo);
                holder.tv = (TextView) convertView.findViewById(R.id.name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (!StringUtils.isEmpty(mGridList.get(position).getRelationId())){
                holder.bg.setBackgroundResource(R.drawable.fragment_zhujilist_grid_item_bg);
                holder.iv.setImageResource(mGridList.get(position).getLogoResource());
                holder.tv.setText(mGridList.get(position).getName());
                holder.tv.setVisibility(View.VISIBLE);
            }else{
                holder.bg.setBackgroundResource(R.color.transparent);
                holder.iv.setImageResource(R.drawable.zhzj_cj_add);
                holder.tv.setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    class ViewHolder {
        public LinearLayout bg;
        public ImageView iv;
        public TextView tv;
    }

    /**
     * 注册广播
     */
    private void initRegisterReceiver() {
        if (isRegist) return;
        isRegist = true;
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        receiverFilter.addAction(Actions.REFRESH_DEVICES_LIST);
        receiverFilter.addAction(Actions.SHOW_SERVER_MESSAGE);
        receiverFilter.addAction(Actions.ZHUJI_CHECKUPDATE);
        receiverFilter.addAction(Actions.ZHUJI_UPDATE);
        receiverFilter.addAction(Actions.CONNECTION_FAILED);
        receiverFilter.addAction(Actions.ADD_NEW_ZHUJI);
        receiverFilter.addAction(Constants.Action.GET_FRIENDS_STATE);
        receiverFilter.addAction(Actions.WEATHER_GET_RESULT);
        receiverFilter.addAction(Actions.MQTT_GET_ACCEPTED);
        receiverFilter.addAction(Actions.MQTT_UPDATE_ACCEPTED);
        mContext.registerReceiver(receiver, receiverFilter);
    }

    private boolean initSuccess = false;

    @Override
    public void onResume() {
        super.onResume();
        mContext.device_main_scnce.setVisibility(View.GONE);
        if (!isRegist) {
            initRegisterReceiver();
        }
        if (initSuccess)
            refrushDate();
        initSuccess = true;
    }

    boolean isRegist = false;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null && isRegist) {
            isRegist = false;
            mContext.unregisterReceiver(receiver);
        }
    }

    boolean zhuji_success = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (DeviceMainActivity) getActivity();
        zhuji_success = mContext.getIntent().getBooleanExtra("zhuji_success", false);
        mContext.initLeftMenu();
        itemMenu = new DevicesMenuPopupWindow(mContext, this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {//显示时触发
//            resetMasterId();//将master_id置空
          //  mContext.getDcsp().remove(DataCenterSharedPreferences.Constant.APP_MASTERID).commit();
        }
    }


    public class GlideImageLoader extends com.youth.banner.loader.ImageLoader {

        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            Glide.with(context).load(path).into(imageView);
        }
    }

    private void initBanner(final List<ImageBannerInfo.ImageBannerBean> list, Banner banner) {
   //     banner.setVisibility(View.VISIBLE);
        List<String> images = new ArrayList<>();
        //设置banner样式
        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        int width = getActivity().getResources().getDisplayMetrics().widthPixels;
        if (banner.getId() == R.id.banner_bottom) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, width / 4);
            banner.setLayoutParams(lp);
        }else if(banner.getId() == R.id.banner_top){
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Integer.parseInt(String.valueOf((int)(width * 0.45))));
            banner.setLayoutParams(lp);
        }
        //设置图片集合
        for (ImageBannerInfo.ImageBannerBean bean : list) {
            images.add(bean.getContent());
        }
        banner.setImages(images);
        //设置banner动画效果
        banner.setBannerAnimation(Transformer.Default);
        //设置自动轮播
        banner.isAutoPlay(images.size() > 1);
        //设置轮播时间
        banner.setDelayTime(15000);

        banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                Intent intent = new Intent(getActivity(), CommonWebViewActivity.class);
                intent.putExtra("url", list.get(position).getUrl());
                startActivity(intent);
            }
        });

        //设置指示器位置（当banner模式中有指示器时）
        banner.setIndicatorGravity(BannerConfig.RIGHT);
        //banner设置方法全部调用完毕时最后调用
        banner.start();
    }


    public boolean serviceTimeOut(int group, int position) {
        if (zhujiInGroupList.get(group).get(position).getZhujiInfo().getUpdateStatus() == 3) {
            Toast.makeText(mContext, getString(R.string.sever_close), Toast.LENGTH_SHORT).show();
            return true;
        }
        if (zhujiInGroupList.get(group).get(position).getZhujiInfo().isOp()) {
            if (System.currentTimeMillis() > zhujiInGroupList.get(group).get(position).getZhujiInfo().getStime()) {
                Toast.makeText(mContext, getString(R.string.sever_time_out), Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_zhiji_list, null, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //广告控件
        banner_top = (Banner) view.findViewById(R.id.banner_top);

        //添加缓冲
        defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 8000);
        initView(view);
        initData();
        experWindow = new SelectAddPopupWindow(mContext, 0, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                experZhuji = experInfos.get(position);
                if (null == DatabaseOperator.getInstance(mContext.getApplicationContext()).queryDeviceZhuJiInfo(experZhuji.getMasterid())) {
                    if (experZhuji.getUsercount() < 50) {
                        mContext.showInProgress(getString(R.string.operationing), false, false);
                        JavaThreadPool.getInstance().excute(new ExperZhuji(experZhuji.getId()));
                        experWindow.dismiss();
                    } else {
                        mContext.cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.experience_zhuji_illegal), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mContext.cancelInProgress();
                    Toast.makeText(mContext, getString(R.string.experience_zhuji_exit), Toast.LENGTH_SHORT).show();
                }
            }
        });

        zj_ExpandListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return false;
            }
        });

        zj_ExpandListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (mContext.connLoadingVisible()) {
                    Toast.makeText(mContext, R.string.connecting_network, Toast.LENGTH_SHORT).show();
                    return true;
                }
                operationDevice = Util.getZhujiDevice(zjinfo);
                mContext.setZhuji(zhujiInGroupList.get(groupPosition).get(childPosition).getZhujiInfo());
                zjinfo = zhujiInGroupList.get(groupPosition).get(childPosition).getZhujiInfo();
                setMasterId(zhujiInGroupList.get(groupPosition).get(childPosition).getZhujiInfo().getMasterid());//设置master_id
//                mContext.dcsp.putString(DataCenterSharedPreferences.Constant.APP_MASTERID, zhujiInGroupList.get(groupPosition).get(childPosition).getZhujiInfo().getMasterid()).commit();
                if (DeviceInfo.CakMenu.zhuji.value().equals(zhujiInGroupList.get(groupPosition).get(childPosition).getZhujiInfo().getCak())) {
                    mContext.setZhuji(zhujiInGroupList.get(groupPosition).get(childPosition).getZhujiInfo());
                    Intent intent = new Intent();
                    intent.setClass(mContext, HeaterActivity.class);
                    intent.putExtra(HeaterShadowInfo.mac, zjinfo.getMac());
                    intent.putExtra(HeaterShadowInfo.name, zjinfo.getName());
                    intent.putExtra(HeaterShadowInfo.relationId, zjinfo.getRelationId());
                    intent.putExtra("isGroup", DeviceInfo.CakMenu.group.value().equalsIgnoreCase(zjinfo.getCak()));
                    mContext.startActivity(intent);
                }
                return true;
            }
        });
        zj_ExpandListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final long packedPosition = zj_ExpandListView.getExpandableListPosition(position);
                final int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
                final int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);
                //长按设置主机
                if (childPosition != -1) {
                    zjinfo = zhujiInGroupList.get(groupPosition).get(childPosition).getZhujiInfo();
                    operationDevice = Util.getZhujiDevice(zjinfo);
                    if (serviceTimeOut(groupPosition, childPosition)) {
                        if (DeviceInfo.CakMenu.zhuji.value().equals(zjinfo.getCak())) {
                            deleZhuji();
                        } else {
                            deleGroup();
                        }
                    } else {
                        itemMenu.updateDeviceMenu(mContext, operationDevice, mContext.dcsp, zjinfo, false);
                        itemMenu.showAtLocation(ll_device_main, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
                    }
                }
                return true;
            }
        });
    }

    public Contact buildContact(CameraInfo info) {
        Contact contact = null;
        if (info != null) {
            contact = new Contact();
            contact.contactId = info.getId();
            contact.contactName = info.getN();
            contact.contactPassword = info.getP();
            contact.userPassword = info.getOriginalP();
        }
        return contact;
    }

    @Override
    public void OnItemImgClickListener(int groupPosition, int position, View view, int viewId) {

    }

    @Override
    public void OnDlistImgClickListener(int groupPosition, int childPosition) {
        setMasterId(zhujiInGroupList.get(groupPosition).get(childPosition).getZhujiInfo().getMasterid());//设置master_id
//        mContext.dcsp.putString(DataCenterSharedPreferences.Constant.APP_MASTERID, zhujiInGroupList.get(groupPosition).get(childPosition).getZhujiInfo().getMasterid()).commit();
        mContext.setZhuji(zhujiInGroupList.get(groupPosition).get(childPosition).getZhujiInfo());
        Intent intent = new Intent();
        intent.setAction(DeviceMainActivity.ACTION_CHANGE_FRAGMENT);
        intent.putExtra("fragment", "zhuji");
        mContext.sendBroadcast(intent);
        //刷新数据
        intent.setAction(Actions.REFRESH_DEVICES_LIST);
        mContext.sendBroadcast(intent);
    }


    /**
     * 开始体验主机数据子线程
     */
    class ExperZhuji implements Runnable {
        long id;

        public ExperZhuji(long id) {
            this.id = id;
        }

        @Override
        public void run() {
            String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("did", id);
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/ex/add", jsonObject, mContext);
            List<ZhujiInfo> zhujiInfos = new ArrayList<>();
            if ("0".equals(result)) {
                SyncMessageContainer.getInstance().produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mContext.cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.experience_zhuji_success), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                mContext.cancelInProgress();
                experZhuji = null;
                if ("3".equals(result)) {
                    defaultHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mContext.cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.experience_zhuji_inexitence), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if ("4".equals(result)) {
                    defaultHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mContext.cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.experience_zhuji_illegal), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if ("5".equals(result)) {
                    defaultHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mContext.cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.experience_zhuji_maximize), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }

    }

    public void initView(View view) {
        iv_close = (ImageView) view.findViewById(R.id.iv_close);
        ll_search = (LinearLayout) view.findViewById(R.id.ll_search);
        ll_device_main = (LinearLayout) view.findViewById(R.id.layout_zhuji_main);
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_ly);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeResources(R.color.green, R.color.green, R.color.green, R.color.green);
        //公告view
        ll_notice = (LinearLayout) view.findViewById(R.id.ll_notice);
        iv_notice = (ImageView) view.findViewById(R.id.iv_notice);
        //搜索
        iv_search = (EditText) view.findViewById(R.id.iv_search_edit);
        iv_search_close = (ImageView) view.findViewById(R.id.iv_search_close);

        textview_auto_roll = (TextViewAutoVerticalScroll) view.findViewById(R.id.textview_auto_roll);
        //断开连接
        listViewHeadView = LayoutInflater.from(mContext).inflate(R.layout.activity_devices_list_item_nonet, null);

        zj_ExpandListView = (ExpandableListView) view.findViewById(R.id.zhujilist_listview);

//        footView = LayoutInflater.from(getActivity()).inflate(R.layout.add_zhuji_footerview, null);
//        zj_ExpandListView.addFooterView(footView);
        zj_ExpandListView.addHeaderView(listViewHeadView);
//        footView.findViewById(R.id.add_zhuji_foot).setOnClickListener(this);

        zhujiInGroupList = new ArrayList<>();
        groupInfoList = new ArrayList<>();
        expandableAdapter = new ZhujiListExpandableAdapter(groupInfoList, zhujiInGroupList, getActivity());
        expandableAdapter.setZhujiListAdapterOnclick(this);
        zjinfo = new ZhujiInfo();
        zj_ExpandListView.setAdapter(expandableAdapter);
        zj_ExpandListView.setGroupIndicator(null);


        iconWeather = (ImageView) view.findViewById(R.id.icon_weather);
        textWeatherMain = (TextView) view.findViewById(R.id.text_weather);
        textWeatherPm25 = (TextView) view.findViewById(R.id.outdoor_pm25);
        textWeatherTemp = (TextView) view.findViewById(R.id.outdoor_temp);
        textWeatherAirQuality = (TextView) view.findViewById(R.id.outdoor_quality);
        layoutWeather = (RelativeLayout) view.findViewById(R.id.weather_layout);
        layoutWeather.setOnClickListener(this);

        ZhujiInfo zhujiInfo = new ZhujiInfo();
        List<ZhujiInfo> airList = new ArrayList<>();
        airList.add(zhujiInfo);
        List<ZhujiInfo> pestList = new ArrayList<>();
        pestList.add(zhujiInfo);
        airAdapter = new MyGridAdapter(airList);
        pestAdapter = new MyGridAdapter(pestList);
        airListGridView = (GridView) view.findViewById(R.id.airlist_gridview);
        pestListGridView = (GridView) view.findViewById(R.id.pestlist_gridview);
        airListGridView.setAdapter(airAdapter);
        pestListGridView.setAdapter(pestAdapter);

        airListGridView.setOnItemClickListener((AdapterView<?> parent, View v, int position, long id) -> {
            if (!mContext.connLoadingVisible()) {
                ZhujiInfo zj = airAdapter.getmGridList().get(position);
                Intent intent = new Intent();
                if (StringUtils.isEmpty(zj.getRelationId())) {
                    intent.setClass(mContext, AddZhujiWayChooseActivity.class);
                    intent.putExtra(HeaterShadowInfo.type, ZhujiInfo.CtrDeviceType.AIRCARE_SINGLE_REFILL);
                } else {
                    intent.setClass(mContext, HeaterActivity.class);
                    intent.putExtra(HeaterShadowInfo.mac, zj.getMac());
                    intent.putExtra(HeaterShadowInfo.name, zj.getName());
                    intent.putExtra(HeaterShadowInfo.relationId, zj.getRelationId());
                    intent.putExtra("isGroup", DeviceInfo.CakMenu.group.value().equalsIgnoreCase(zj.getCak()));
                }
                startActivity(intent);
            }else{
                ToastUtils.show("APP Connecting");
            }
        });


        airListGridView.setOnItemLongClickListener((AdapterView<?> parent, View v, int position, long id) -> {
            ZhujiInfo zj = airAdapter.getmGridList().get(position);
            if (!StringUtils.isEmpty(zj.getRelationId())) {
                operationDevice = Util.getZhujiDevice(zj);
                itemMenu.updateDeviceMenu(mContext, operationDevice, mContext.dcsp, zj, false);
                itemMenu.showAtLocation(ll_device_main, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
            }
            return true;
        });

        pestListGridView.setOnItemClickListener((AdapterView<?> parent, View v, int position, long id) -> {
            if (!mContext.connLoadingVisible()) {
                ZhujiInfo zj = pestAdapter.getmGridList().get(position);
                Intent intent = new Intent();
                if (StringUtils.isEmpty(zj.getRelationId())) {
                    intent.setClass(mContext, AddZhujiWayChooseActivity.class);
                    intent.putExtra(HeaterShadowInfo.type, ZhujiInfo.CtrDeviceType.INSECTICIDE_SINGLE_REFILL);
                } else {
                    intent.setClass(mContext, HeaterActivity.class);
                    intent.putExtra(HeaterShadowInfo.mac, zj.getMac());
                    intent.putExtra(HeaterShadowInfo.name, zj.getName());
                    intent.putExtra(HeaterShadowInfo.relationId, zj.getRelationId());
                    intent.putExtra("isGroup", DeviceInfo.CakMenu.group.value().equalsIgnoreCase(zj.getCak()));
                }
                startActivity(intent);
            }else{
                ToastUtils.show("APP Connecting");
            }
        });


        pestListGridView.setOnItemLongClickListener((AdapterView<?> parent, View v, int position, long id) -> {
            ZhujiInfo zj = pestAdapter.getmGridList().get(position);
            if (!StringUtils.isEmpty(zj.getRelationId())) {
                operationDevice = Util.getZhujiDevice(zj);
                itemMenu.updateDeviceMenu(mContext, operationDevice, mContext.dcsp, zj, false);
                itemMenu.showAtLocation(ll_device_main, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
            }
            return true;
        });

        iv_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                refrushDate();
                if (!"".equals(s.toString())) {
                    iv_search_close.setVisibility(View.VISIBLE);
                } else {
                    iv_search_close.setVisibility(View.GONE);
                }
            }
        });
        iv_search_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_search.setText("");
                iv_search.clearFocus();
                mContext.getImm().hideSoftInputFromWindow(iv_search.getWindowToken(), 0);
            }
        });
        listViewHeadView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (v.findViewById(R.id.nonet_layout).getVisibility() == View.VISIBLE) {
                                                        startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), 0);
                                                    }
                                                }
                                            }

        );

    }


    public void initData() {
        refrushDate();//初始化
    }

    @Override
    public void onPause() {
        super.onPause();
        if (receiver != null && isRegist) {
            isRegist = false;
            mContext.unregisterReceiver(receiver);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            //天气点击
            case R.id.weather_layout:
                intent.setClass(mContext.getApplicationContext(), WeatherInfoActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_icon:
                mContext.menuWindow.showAtLocation(v, Gravity.TOP | Gravity.RIGHT, 0,
                        Util.dip2px(mContext.getApplicationContext(), 55) + Util.getStatusBarHeight(mContext));
//                P2PHandler.getInstance().getFriendStatus();
                break;
            case R.id.add_zhuji_foot:
                intent.setClass(mContext.getApplicationContext(), AddSelectActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_setdevice: // 设置设备
                itemMenu.dismiss();
                intent.setClass(mContext.getApplicationContext(), ChooseAudioSettingMode.class);
                intent.putExtra("device", operationDevice);
                startActivity(intent);
                break;
            case R.id.btn_setgsm: // 设置主机GSM号码
                itemMenu.dismiss();
                intent.setClass(mContext.getApplicationContext(), DeviceSetGSMPhoneActivity.class);
                intent.putExtra("device", operationDevice);
                intent.putExtra("type", 0);
                startActivity(intent);
                break;
            case R.id.btn_setscall: // 设置主机电话报警号码
                itemMenu.dismiss();
                intent.setClass(mContext.getApplicationContext(), DeviceSetGSMPhoneActivity.class);
                intent.putExtra("device", operationDevice);
                intent.putExtra("type", 1);
                startActivity(intent);
                break;
            case R.id.btn_checkversion: // 固件检查更新
                itemMenu.dismiss();
                SyncClientAWSMQTTConnector.getInstance().registerDevicesShadowTopic(operationDevice.getMac());
                SyncClientAWSMQTTConnector.getInstance().getDevicesStatus(operationDevice.getMac());
                mContext.showInProgress(getString(R.string.loading), false, false);
                defaultHandler.sendEmptyMessageDelayed(dHandler_timeout_binupdate, 8 * 1000);
                break;
            case R.id.btn_accept_auto_strongshow: // 强力提醒模式
                itemMenu.dismiss();
                SyncMessageContainer.getInstance().sendMessageToServer(SyncMessage.CommandMenu.rq_controlRemind, SyncMessage.CodeMenu.zero,
                        operationDevice.getId(), new byte[]{0x03});
                break;
            case R.id.btn_accept_autoshow: // 自动提醒
                itemMenu.dismiss();
                SyncMessageContainer.getInstance().sendMessageToServer(SyncMessage.CommandMenu.rq_controlRemind, SyncMessage.CodeMenu.zero,
                        operationDevice.getId(), new byte[]{0x02});
                break;
            case R.id.btn_acceptnotshow: // 接收消息不提醒
                itemMenu.dismiss();
                SyncMessageContainer.getInstance().sendMessageToServer(SyncMessage.CommandMenu.rq_controlRemind, SyncMessage.CodeMenu.zero,
                        operationDevice.getId(), new byte[]{0x01});
                break;
            case R.id.btn_notaccept: // 关操作
                itemMenu.dismiss();
                SyncMessageContainer.getInstance().sendMessageToServer(SyncMessage.CommandMenu.rq_controlRemind, SyncMessage.CodeMenu.zero,
                        operationDevice.getId(), new byte[]{0x00});
                break;
            case R.id.btn_deldevice:
                // 删除设备
                if (DeviceInfo.CakMenu.zhuji.value().equals(operationDevice.getCak())) {
                    deleZhuji();
                } else {
                    deleGroup();
                }
                break;
            case R.id.hongcai_alarm_setting:
                itemMenu.dismiss();
                //主机报警设定
                intent.setClass(mContext,HongCaiSettingActivity.class);
                intent.putExtra("whatsetting",1);
                intent.putExtra("zhuji_id",zjinfo.getId());
                startActivity(intent);
                break;
            case R.id.hongcai_naozhong_setting:
                itemMenu.dismiss();
                //主机闹钟设定
                intent.setClass(mContext,HongCaiSettingActivity.class);
                intent.putExtra("whatsetting",2);
                intent.putExtra("zhuji_id",zjinfo.getId());
                startActivity(intent);
                break;
            case R.id.btn_bind_camera:
                //绑定摄像头
                itemMenu.dismiss();
                intent.setClass(getActivity(),CameraListActivity.class);
                intent.putExtra("device",operationDevice);
                startActivity(intent);
                break;
            /*    System.out.println(operationDevice.getBipc());
                JavaThreadPool.getInstance().excute(new CommandLoad("696fdc93edf62bda",
                        DeviceInfo.ControlTypeMenu.group.value().equals(operationDevice.getControlType())));
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ToastTools.long_Toast(getContext(),operationDevice.getBipc());
                    }
                },6000);
                break;*/
        }
    }

    public void deleZhuji() {
        itemMenu.dismiss();
        new AlertView(getString(R.string.deviceslist_server_leftmenu_deltitle),
                DeviceInfo.ControlTypeMenu.zhuji.value().equals(operationDevice.getControlType())
                        ? getString(R.string.deviceslist_server_leftmenu_delmessage_zhuji)
                        : getString(R.string.deviceslist_server_leftmenu_delmessage),
                getString(R.string.deviceslist_server_leftmenu_delcancel),
                new String[]{getString(R.string.deviceslist_server_leftmenu_delbutton)}, null,
                mContext, AlertView.Style.Alert,
                new com.smartism.znzk.view.alertview.OnItemClickListener() {

                    @Override
                    public void onItemClick(final Object o, int position) {
                        if (position != -1) {
                            mContext.showInProgress(getString(R.string.deviceslist_server_leftmenu_deltips), false, true);

                            DeleteCtrUserDeviceRelationsInput deleteCtrUserDeviceRelationsInput = DeleteCtrUserDeviceRelationsInput.builder()
                                    .id(operationDevice.getRelationId())
                                    .build();

                            AWSClients.getInstance().getmAWSAppSyncClient().mutate(DeleteCtrUserDeviceRelationsMutation.builder().input(deleteCtrUserDeviceRelationsInput).build())
                                    .enqueue(mutationCallback);
                        }
                    }
                }).show();
    }

    private GraphQLCall.Callback<DeleteCtrUserDeviceRelationsMutation.Data> mutationCallback = new GraphQLCall.Callback<DeleteCtrUserDeviceRelationsMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<DeleteCtrUserDeviceRelationsMutation.Data> response) {
            Log.i(MainApplication.TAG,"Results GraphQL delete:" + JSON.toJSONString(response));
            mContext.runOnUiThread(()->{
                mContext.cancelInProgress();
                refrushDate();
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error","GraphQL delete Exception", e);
            mContext.runOnUiThread(()-> {
                ToastUtil.longMessage("Delete failed");
                mContext.cancelInProgress();
                refrushDate();
            });
        }
    };

    public void deleGroup() {
        itemMenu.dismiss();
        new AlertView(getString(R.string.deviceslist_server_leftmenu_deltitle),
                DeviceInfo.ControlTypeMenu.zhuji.value().equals(operationDevice.getControlType())
                        ? getString(R.string.deviceslist_server_leftmenu_delmessage_zhuji)
                        : getString(R.string.deviceslist_server_leftmenu_delmessage),
                getString(R.string.deviceslist_server_leftmenu_delcancel),
                new String[]{getString(R.string.deviceslist_server_leftmenu_delbutton)}, null,
                mContext, AlertView.Style.Alert,
                new com.smartism.znzk.view.alertview.OnItemClickListener() {

                    @Override
                    public void onItemClick(final Object o, int position) {
                        if (position != -1) {
                            mContext.showInProgress(getString(R.string.deviceslist_server_leftmenu_deltips), false, true);

                            DeleteCtrUserGroupInput deleteCtrUserGroupInput = DeleteCtrUserGroupInput.builder()
                                    .id(operationDevice.getRelationId())
                                    .build();

                            AWSClients.getInstance().getmAWSAppSyncClient().mutate(DeleteCtrUserGroupMutation.builder().input(deleteCtrUserGroupInput).build())
                                    .enqueue(deleteGroupCallback);
                        }
                    }
                }).show();
    }

    private GraphQLCall.Callback<DeleteCtrUserGroupMutation.Data> deleteGroupCallback = new GraphQLCall.Callback<DeleteCtrUserGroupMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<DeleteCtrUserGroupMutation.Data> response) {
            Log.i(MainApplication.TAG,"Results GraphQL delete:" + JSON.toJSONString(response));
            mContext.runOnUiThread(()->{
                mContext.cancelInProgress();
                refrushDate();
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error","GraphQL delete Exception", e);
            mContext.runOnUiThread(()-> {
                ToastUtil.longMessage("Delete failed");
                mContext.cancelInProgress();
                refrushDate();
            });
        }
    };

    @Override
    public void onRefresh() {
        //添加缓冲
        defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 8000);
        refrushDate();
    }

    public class ZhujiInfos {

        private ZhujiInfo zhujiInfo;
        private int devices;
        private boolean flag;

        public ZhujiInfos(ZhujiInfo zhujiInfo) {
            this.zhujiInfo = zhujiInfo;
            this.devices = zhujiInfo.getDevices();
        }

        public boolean isFlag() {
            return flag;
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }

        public ZhujiInfo getZhujiInfo() {
            return zhujiInfo;
        }

        public void setZhujiInfo(ZhujiInfo zhujiInfo) {
            this.zhujiInfo = zhujiInfo;
        }

        public int getDevices() {
            return devices;
        }

        public void setDevices(int devices) {
            this.devices = devices;
        }

        @Override
        public String toString() {
            return "ZhujiInfos{" +
                    "zhujiInfo=" + zhujiInfo +
                    ", devices=" + devices +
                    ", flag=" + flag +
                    '}';
        }
    }


    public void changeCameraStatus(String[] contactIDs, int[] status) {
        for (int i = 0; i < contactIDs.length; i++) {
            for (int j = 0; j < zhujiInGroupList.size(); j++) {
                for (int k = 0; k < zhujiInGroupList.get(j).size(); k++) {
                    if (contactIDs[i].equals(String.valueOf(zhujiInGroupList.get(j).get(k).getZhujiInfo().getCameraInfo().getId()))) {
                        zhujiInGroupList.get(j).get(k).getZhujiInfo().setOnline(status[i] == 1);//1 ：在线
//                      updateItem(j);
                    }
                }
            }
        }
        expandableAdapter.notifyDataSetChanged();
    }

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case dHandler_timeout:
                    defaultHandler.removeMessages(dHandler_timeout);
                    mContext.cancelInProgress();
                    mRefreshLayout.setRefreshing(false);
                    break;
                case dHandler_timeout_binupdate:
                    mContext.cancelInProgress();
                    ToastUtils.show(R.string.time_out);
                    break;
                case dHandler_loadsuccess: // 刷新主机列表
                    List<Object> obj = (List<Object>) msg.obj;
                    if (obj != null && obj.size() >= 2) {
                        List<ZhujiInfo> airList = (List<ZhujiInfo>) obj.get(0);
                        List<ZhujiInfo> pestList = (List<ZhujiInfo>) obj.get(1);

                        airAdapter.clearZhuji();
                        pestAdapter.clearZhuji();

                        if (!CollectionsUtils.isEmpty(airList)) {
                            airAdapter.getmGridList().addAll(airAdapter.getmGridList().size() - 1,airList);
                        }

                        if (!CollectionsUtils.isEmpty(pestList)) {
                            pestAdapter.getmGridList().addAll(pestAdapter.getmGridList().size() - 1,pestList);
                        }

                        airAdapter.notifyDataSetChanged();
                        pestAdapter.notifyDataSetChanged();
                    }
                    mContext.cancelInProgress();
                    break;
                case dHandler_loadsuccess_group: // 刷新分组列表
                    obj = (List<Object>) msg.obj;
                    if (obj != null && obj.size() >= 2) {
                        List<ZhujiInfo> airList = (List<ZhujiInfo>) obj.get(0);
                        List<ZhujiInfo> pestList = (List<ZhujiInfo>) obj.get(1);

                        airAdapter.clearGroup();
                        pestAdapter.clearGroup();

                        if (!CollectionsUtils.isEmpty(airList)) {
                            airAdapter.getmGridList().addAll(0,airList);
                        }

                        if (!CollectionsUtils.isEmpty(pestList)) {
                            pestAdapter.getmGridList().addAll(0,pestList);
                        }

                        airAdapter.notifyDataSetChanged();
                        pestAdapter.notifyDataSetChanged();
                    }
                    mContext.cancelInProgress();
                    break;
                case dHandler_notice:
                    Map<Long, String> map = new HashMap<>();
                    map = (Map<Long, String>) msg.obj;
                    if (map != null && map.size() > 0&&map.entrySet()!=null) {
                        Iterator i = map.entrySet().iterator();
                        List<String> list = new ArrayList<>();
                        final List<Long> idList = new ArrayList<>();
                        if(i!=null){
                            while (i.hasNext()) {
                                Map.Entry entry = (Map.Entry) i.next();
                                long id = (long) entry.getKey();
                                String title = (String) entry.getValue();
                                list.add(title);
                                idList.add(id);
                            }
                        }
                        adv = list.toArray(new String[list.size()]);
                        textview_auto_roll.setText(adv[0]);
                        ll_notice.setVisibility(View.VISIBLE);
                        iv_notice.setVisibility(View.VISIBLE);
                        textview_auto_roll.setVisibility(View.VISIBLE);
                        textview_auto_roll.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(mContext, CommonWebViewActivity.class);
                                intent.putExtra("title", adv[number % adv.length]);
                                intent.putExtra("url", mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "")
                                        + "/jdm/notice?nid=" + idList.get((number % adv.length)) + "&lang=" + Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry());
                                startActivity(intent);

                            }
                        });
                        if (adv.length > 1) {
                            defaultHandler.sendEmptyMessageDelayed(99, 3000);
                        }
                    }
                    break;
                case 99:
                    textview_auto_roll.next();
                    number++;
                    textview_auto_roll.setText(adv[number % adv.length]);
                    defaultHandler.sendEmptyMessageDelayed(99, 3000);
                    break;
                case dHandler_experzhujis:
                    //获取体验主机列表
                    mContext.cancelInProgress();
                    final List<ZhujiInfo> infos = (List<ZhujiInfo>) msg.obj;
                    if (infos == null || infos.isEmpty()) {
                        Toast.makeText(mContext, getString(R.string.experience_zhuji_nodate), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    experInfos.clear();
                    for (int j = 0; j < infos.size(); j++) {
                        experInfos.add(infos.get(j));
                    }
                    experWindow.showAtLocation(ll_device_main, Gravity.CENTER, 0, 0);
                    experWindow.updateMenu(infos);
                    changeWindowAlfa(0.7f);
                    break;
                case dHandler_banner_top:
                    bannerBeanTopList = new ArrayList<>();
                    JSONArray array = JSON.parseArray((String) msg.obj);
                    if (array != null && array.size() > 0) {
                        for (int i = 0; i < array.size(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            ImageBannerInfo.ImageBannerBean bean = new ImageBannerInfo.ImageBannerBean();
                            bean.setContent(object.getString("content"));
                            bean.setLang(object.getString("lang"));
                            bean.setName(object.getString("name"));
                            bean.setUrl(object.getString("url"));
                            bean.setUrlType(object.getString("urlType"));
                            bannerBeanTopList.add(bean);
                        }
                        initBanner(bannerBeanTopList,banner_top);
                    }

                    break;
                case dHandler_banner_bottom:
                    bannerBeanBottomList = new ArrayList<>();
                    array = JSON.parseArray((String) msg.obj);
                    if (array != null && array.size() > 0) {
                        for (int i = 0; i < array.size(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            ImageBannerInfo.ImageBannerBean bean = new ImageBannerInfo.ImageBannerBean();
                            bean.setContent(object.getString("content"));
                            bean.setLang(object.getString("lang"));
                            bean.setName(object.getString("name"));
                            bean.setUrl(object.getString("url"));
                            bean.setUrlType(object.getString("urlType"));
                            bannerBeanBottomList.add(bean);
                        }
                        initBanner(bannerBeanBottomList,banner_bottom);
                    }

                    break;
                case dHandler_initContast:
                    mContext.cancelInProgress();
                    Bundle bundle = (Bundle) msg.obj;
                    Intent deviceIntent = bundle.getParcelable("intent");
                    if (deviceIntent != null) {
                        Contact contact = (Contact) bundle.getSerializable("contact");
                        deviceIntent.putExtra("cameraPaiZi",bundle.getString("cameraPaiZi"));
                        deviceIntent.putExtra("contact", contact);
                    }
                    if(bundle.getString("cameraPaiZi").equals(CameraInfo.CEnum.xiongmai.value())){
                        startActivity(deviceIntent);
                    }else if(bundle.getString("cameraPaiZi").equals(CameraInfo.CEnum.jiwei.value())){
                        deviceIntent.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
                    }
                    break ;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);



    /*
     更改屏幕窗口透明度
     */
    public void changeWindowAlfa(float alfa) {
        WindowManager.LayoutParams params = mContext.getWindow().getAttributes();
        params.alpha = alfa;
        mContext.getWindow().setAttributes(params);
    }

    /**
     * 更新数据
     */

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Actions.WEATHER_GET_RESULT.equals(intent.getAction())){
                try{
                    JSONObject weather = JSON.parseObject(mContext.getDcsp().getString(DataCenterSharedPreferences.Constant.WEATHER_INFO,"{}"));
                    explainWeatherInfoAndRefreshPage(weather);
                }catch (Exception ex){
                    //防止json无数据崩溃
                }
            }else if (Constants.Action.GET_FRIENDS_STATE.equals(intent.getAction())) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    String[] contactIDs = bundle.getStringArray("contactIDs");
                    int[] status = bundle.getIntArray("status");
                    changeCameraStatus(contactIDs, status);
                }
            } else if (Actions.ACCETP_ONEDEVICE_MESSAGE.equals(intent.getAction()) && !isHidden()){
                refrushDate();//设备指令推送
            } else if (Actions.REFRESH_DEVICES_LIST.equals(intent.getAction())) {
                refrushDate();//设备列表刷新
                //只有一个主机情况下跳转到主机列表
                if (mContext.dcsp.getBoolean(DataCenterSharedPreferences.Constant.IS_TURN, false) && expandableAdapter.getAllChildrenCount() == 1 && DeviceInfo.CakMenu.zhuji.value().equals(zhujiInGroupList.get(0).get(0).getZhujiInfo().getCak()) && !isHidden()) {
                    if (DeviceInfo.CaMenu.zhujijzm.value().equals(zhujiInGroupList.get(0).get(0).getZhujiInfo().getCa())) {//卷闸门主机
                        intent.setClass(mContext, DeviceInfoActivity.class);
                        intent.putExtra("device", Util.getZhujiDevice(zhujiInGroupList.get(0).get(0).getZhujiInfo()));
                        startActivity(intent);
                    } else {
                        setMasterId(zhujiInGroupList.get(0).get(0).getZhujiInfo().getMasterid());//设置master_id
//                        mContext.dcsp.putString(DataCenterSharedPreferences.Constant.APP_MASTERID, zhujiInGroupList.get(0).get(0).getZhujiInfo().getMasterid()).commit();
                        mContext.setZhuji(zhujiInGroupList.get(0).get(0).getZhujiInfo());
                        Intent intent1 = new Intent();
                        intent1.setAction(DeviceMainActivity.ACTION_CHANGE_FRAGMENT);
                        intent1.putExtra("fragment", "zhuji");
                        mContext.sendBroadcast(intent1);
                    }
                }
                mContext.dcsp.putBoolean(DataCenterSharedPreferences.Constant.IS_TURN, false).commit();
            } else if (Actions.SHOW_SERVER_MESSAGE.equals(intent.getAction()) && !isHidden()) { // 显示服务器信息
                mContext.cancelInProgress();
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(intent.getStringExtra("message"));
                } catch (Exception e) {
                    Log.w("DevicesList", "获取服务器返回消息，转换为json对象失败，用原始值处理");
                }
                if (resultJson != null) {
                    switch (resultJson.getIntValue("Code")) {
                        case 4:
                            Toast.makeText(mContext, getString(R.string.tips_4), Toast.LENGTH_SHORT).show();
                            break;
                        case 5:
                            Toast.makeText(mContext, getString(R.string.tips_5), Toast.LENGTH_SHORT).show();
                            break;
                        case 6:
                            Toast.makeText(mContext, getString(R.string.tips_6), Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            Toast.makeText(mContext, getString(R.string.tips_7), Toast.LENGTH_SHORT).show();
                            break;
                        case 8:
                            Toast.makeText(mContext, getString(R.string.tips_8), Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            Toast.makeText(mContext, "Unknown Info", Toast.LENGTH_SHORT).show();
                            break;
                    }

                } else {
                    Toast.makeText(mContext, intent.getStringExtra("message"), Toast.LENGTH_SHORT)
                            .show();

                }
                refrushDate();//显示服务器返回信息
            } else if (Actions.ZHUJI_CHECKUPDATE.equals(intent.getAction()) && !isHidden()) { // 检查主机版本
                mContext.cancelInProgress();
                boolean to = defaultHandler.hasMessages(dHandler_timeout);
                defaultHandler.removeMessages(dHandler_timeout);
                if (SyncMessage.CodeMenu.rp_checkpudate_nonew.value() == intent.getIntExtra("data", 0)) {
                    if (to) { // 当页面数据初始化完成会检测主机的固件版本，这个时候是不需要显示固件是最新提示的
                        String data_info = intent.getStringExtra("data_info");
                        if (data_info != null && !"".equals(data_info)) {
                            JSONObject object = JSON.parseObject(data_info);
                            Toast.makeText(mContext, String.format(getString(R.string.deviceslist_server_noupdatev), object.getString("ov")),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, getString(R.string.deviceslist_server_noupdatev),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (SyncMessage.CodeMenu.rp_checkpudate_havenew.value() == intent.getIntExtra("data", 0)) {
                    String data_info = intent.getStringExtra("data_info");
                    if (data_info != null && !"".equals(data_info)) {
                        JSONObject object = JSON.parseObject(data_info);
                        data_info = String.format(getString(R.string.deviceslist_server_update_havenewv), object.getString("ov"), object.getString("nv"));
                    } else {
                        data_info = getString(R.string.deviceslist_server_update_havenew);
                    }
                    if(updateAlertView==null||!updateAlertView.isShowing()){
                        updateAlertView =  new AlertView(getString(R.string.deviceslist_server_update),
                                data_info,
                                getString(R.string.deviceslist_server_leftmenu_delcancel),
                                new String[]{getString(R.string.deviceslist_server_update_button)}, null,
                                mContext, AlertView.Style.Alert,
                                new com.smartism.znzk.view.alertview.OnItemClickListener() {

                                    @Override
                                    public void onItemClick(Object o, int position) {
                                        if (position != -1) {
                                            if (operationDevice == null)
                                                return;
                                            mContext.showInProgress(getString(R.string.ongoing), false, false);
                                            defaultHandler.sendEmptyMessageDelayed(dHandlerWhat_serverupdatetimeout, 20000);
                                            SyncMessage message1 = new SyncMessage();
                                            message1.setCommand(SyncMessage.CommandMenu.rq_pudate.value());
                                            message1.setDeviceid(operationDevice.getId());
                                            SyncMessageContainer.getInstance().produceSendMessage(message1);
                                        }
                                    }
                                });
                        updateAlertView.show();
                    }

                }
            } else if (Actions.ZHUJI_UPDATE.equals(intent.getAction()) && !isHidden()) { // 主机更新
                System.out.println(
                        "max:" + intent.getIntExtra("max", 0) + "progress:" + intent.getIntExtra("progress", 0));
                defaultHandler.removeMessages(dHandlerWhat_serverupdatetimeout);
                if (SyncMessage.CodeMenu.rp_pupdate_into.value() == intent.getIntExtra("data", 0)) {
                    mContext.cancelInProgress();
                    mContext.showOrUpdateProgressBar(getString(R.string.deviceslist_server_updating), false, 1, 100);
                } else if (SyncMessage.CodeMenu.rp_pupdate_success.value() == intent.getIntExtra("data", 0)) {
                    mContext.cancelInProgress();
                    mContext.cancelInProgressBar();
                    Toast.makeText(mContext, getString(R.string.deviceslist_server_update_success),
                            Toast.LENGTH_LONG).show();
                } else if (SyncMessage.CodeMenu.rp_pupdate_progress.value() == intent.getIntExtra("data", 0)) {
                    mContext.showOrUpdateProgressBar(getString(R.string.deviceslist_server_updating), false,
                            intent.getIntExtra("progress", 0) + 1, intent.getIntExtra("max", 0));
                    if (intent.getIntExtra("progress", 0) + 1 == intent.getIntExtra("max", 0)
                            && intent.getIntExtra("progress", 0) != 0) {
                        mContext.showInProgress(getString(R.string.deviceslist_server_update_reboot), false, false);
                    }
                }
            } else if (Actions.ADD_NEW_ZHUJI.equals(intent.getAction())) {
                newMasterId = intent.getStringExtra("masterId");
                if (newMasterId != null && (Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion()))) {
//                    showDeleteAlert(newMasterId);
                    refrushDate();
                }
            } else if (Actions.MQTT_GET_ACCEPTED.equals(intent.getAction()) && !isHidden()) { //获取到设备信息
                try{
                    JSONObject param = JSONObject.parseObject(intent.getStringExtra(Actions.MQTT_GET_ACCEPTED_DATA_JSON));
                    JSONObject state = param.getJSONObject("state");
                    if (state.containsKey("reported")) {
                        JSONObject reported = state.getJSONObject("reported");
                        String macInfo = intent.getStringExtra(Actions.MQTT_TOPIC_THINGNAME);
                        if (operationDevice!=null && operationDevice.getMac().equalsIgnoreCase(macInfo)){
                            operationReported = reported;
                            if ("connected".equalsIgnoreCase(operationReported.getString(HeaterShadowInfo.connected))){
                                queryFirmwareList();
                            }else{
                                Toast.makeText(mContext, getString(R.string.update_zhuji_gujian), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }catch (Exception ex){
                    //
                }
            } else if (Actions.MQTT_UPDATE_ACCEPTED.equals(intent.getAction()) && !isHidden()) { //收到更新信息
                try{
                    JSONObject param = JSONObject.parseObject(intent.getStringExtra(Actions.MQTT_UPDATE_ACCEPTED_DATA_JSON));
                    JSONObject state = param.getJSONObject("state");
                    if (state.containsKey("reported")) {
                        JSONObject reported =  state.getJSONObject("reported");
                        String macInfo = intent.getStringExtra(Actions.MQTT_TOPIC_THINGNAME);
                        if (operationDevice!=null && operationDevice.getMac().equalsIgnoreCase(macInfo)){
                            if(reported.containsKey("otaState")){
                                if ("otaing".equalsIgnoreCase(reported.getString("otaState"))){
                                    mContext.cancelInProgress();
                                    mContext.showOrUpdateProgressBar(getString(R.string.deviceslist_server_updating), false, 1, 100);
                                }else if("success".equalsIgnoreCase(reported.getString("otaState"))){
                                    mContext.cancelInProgress();
                                    mContext.cancelInProgressBar();
                                    Toast.makeText(mContext, getString(R.string.deviceslist_server_update_success),
                                            Toast.LENGTH_LONG).show();
                                }else if("fail".equalsIgnoreCase(reported.getString("otaState"))){
                                    mContext.cancelInProgress();
                                    mContext.cancelInProgressBar();
                                    Toast.makeText(mContext, "update failed",
                                            Toast.LENGTH_LONG).show();
                                }else if("normal".equalsIgnoreCase(reported.getString("otaState"))){
                                    //正常 不作处理
                                }else{
                                    //进度预留
                                    try {
                                        mContext.showOrUpdateProgressBar(getString(R.string.deviceslist_server_updating), false,
                                                reported.getIntValue("otaState"), 100);
                                    }catch (Exception ex){
                                        //防止 otaState 不是整型出错崩溃
                                    }
                                }
                            }
                        }
                    }
                }catch (Exception ex){
                    //
                }
            }
        }
    };

    private void queryFirmwareList(){
        AWSClients.getInstance().getmAWSAppSyncClient().query(ListCtrDeviceFirmwaresQuery.builder()
                .filter(TableCtrDeviceFirmwareFilterInput.builder()
                        .type(TableStringFilterInput.builder().eq(operationReported.getString(HeaterShadowInfo.type)).build())
                        .build())
                .build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryFirmwareCallback);
    }

    private GraphQLCall.Callback<ListCtrDeviceFirmwaresQuery.Data> queryFirmwareCallback = new GraphQLCall.Callback<ListCtrDeviceFirmwaresQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListCtrDeviceFirmwaresQuery.Data> response) {
            Log.i("Results", response.data().listCtrDeviceFirmwares().items().toString());
            mContext.cancelInProgress();
            boolean to = defaultHandler.hasMessages(dHandler_timeout_binupdate);
            defaultHandler.removeMessages(dHandler_timeout_binupdate);
            if (response.data() != null && response.data().listCtrDeviceFirmwares() != null && response.data().listCtrDeviceFirmwares().items() != null && response.data().listCtrDeviceFirmwares().items().size() > 0){
                ListCtrDeviceFirmwaresQuery.Item firmware = response.data().listCtrDeviceFirmwares().items().get(0);
                if (firmware.versionCode() > operationReported.getIntValue(HeaterShadowInfo.versionCode)){
                    mContext.runOnUiThread(()->{
                        if (to) {
                            String data_info = String.format(getString(R.string.deviceslist_server_update_havenewv), operationReported.getString(HeaterShadowInfo.versionName), firmware.versionName());
                            if(updateAlertView==null||!updateAlertView.isShowing()){
                                updateAlertView =  new AlertView(getString(R.string.deviceslist_server_update),
                                        data_info,
                                        getString(R.string.deviceslist_server_leftmenu_delcancel),
                                        new String[]{getString(R.string.deviceslist_server_update_button)}, null,
                                        mContext, AlertView.Style.Alert,
                                        new com.smartism.znzk.view.alertview.OnItemClickListener() {

                                            @Override
                                            public void onItemClick(Object o, int position) {
                                                if (position != -1) {
                                                    if (operationDevice == null)
                                                        return;
                                                    mContext.showInProgress(getString(R.string.ongoing), false, false);
                                                    defaultHandler.sendEmptyMessageDelayed(dHandlerWhat_serverupdatetimeout, 20000);
                                                    JSONObject update = new JSONObject();
                                                    JSONObject version = new JSONObject();
                                                    version.put("url",firmware.url());
                                                    version.put("domain",firmware.domain());
                                                    version.put("md5",firmware.md5());
                                                    update.put("newVersion",version);
                                                    SyncClientAWSMQTTConnector.getInstance().setDevicesStatus(operationDevice.getMac(),"",update);
                                                }
                                            }
                                        });
                                updateAlertView.show();
                            }
                        }
                    });
                }else{
                    mContext.runOnUiThread(()->{
                        if (to) {
                            Toast.makeText(mContext, String.format(getString(R.string.deviceslist_server_noupdatev), operationReported.getString(HeaterShadowInfo.versionName)),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }else{
                mContext.runOnUiThread(()->{
                    if (to) {
                        Toast.makeText(mContext, String.format(getString(R.string.deviceslist_server_noupdatev), operationReported.getString(HeaterShadowInfo.versionName)),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };

    private void explainWeatherInfoAndRefreshPage(JSONObject weatherInfo){
        /**
         *  地址：https://openweathermap.org/current
         * {
         *     "coord": {
         *         "lon": 112.56,
         *         "lat": 28.2
         *     },
         *     "weather": [
         *         {
         *             "id": 701,
         *             "main": "Mist",
         *             "description": "mist",
         *             "icon": "50d"
         *         }
         *     ],
         *     "base": "stations",
         *     "main": {
         *         "temp": 13,
         *         "feels_like": 13.23,
         *         "temp_min": 13,
         *         "temp_max": 13,
         *         "pressure": 1022,
         *         "humidity": 100
         *     },
         *     "visibility": 4000,
         *     "wind": {
         *         "speed": 1
         *         "deg":
         *     },
         *     "clouds": {
         *         "all": 20
         *     },
         *     "dt": 1584415151,
         *     "uvindex": 1.1 - 另外的接口获取，紫外线强度
         *     "pm25": 223 - 另外的接口获取
         *     "sys": {
         *         "type": 1,
         *         "id": 9622,
         *         "country": "CN",
         *         "sunrise": 1584398227,
         *         "sunset": 1584441548
         *     },
         *     "timezone": 28800,
         *     "id": 1799352,
         *     "name": "Yutan",
         *     "cod": 200
         * }
         */
        JSONObject weather = weatherInfo.getJSONArray("weather").getJSONObject(0);
        textWeatherMain.setText(weather.getString("main"));
        ImageLoader.getInstance().displayImage(String.format("https://openweathermap.org/img/wn/%s@2x.png",weather.getString("icon")), iconWeather, options, new MImageLoadingBar());
        JSONObject main = weatherInfo.getJSONObject("main");
        String tempUnit = mContext.getDcsp().getString(DataCenterSharedPreferences.Constant.SHOW_TEMPERATURE_UNIT, "ssd");
        if (tempUnit.equals("ssd")) {
            textWeatherTemp.setText(String.format(Locale.ENGLISH,"%.0f℃",main.getFloatValue("temp")));
        }else{
            textWeatherTemp.setText(String.format(Locale.ENGLISH,"%.0f℉",TemperatureUtil.ssdToFsd(main.getFloatValue("temp"))));
        }
        if (weatherInfo.containsKey("pm25")){
            textWeatherPm25.setText(String.valueOf(weatherInfo.getFloatValue("pm25")));
        }
        if (weatherInfo.containsKey("pm10")){
            textWeatherAirQuality.setText(Util.pm10ToName(weatherInfo.getFloatValue("pm10")));
        }
    }

    public class MImageLoadingBar implements ImageLoadingListener {

        @Override
        public void onLoadingCancelled(String arg0, View arg1) {
            if (arg1 != null)
                arg1.clearAnimation();
        }

        @Override
        public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
            arg1.clearAnimation();
        }

        @Override
        public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
            arg1.clearAnimation();
        }

        @Override
        public void onLoadingStarted(String arg0, View arg1) {
            arg1.startAnimation(mContext.imgloading_animation);
        }
    }
}