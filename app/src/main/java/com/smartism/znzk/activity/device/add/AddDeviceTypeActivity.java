package com.smartism.znzk.activity.device.add;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CategoryInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.smartism.znzk.R.id.tv_device_name;

/**
 * Created by win7 on 2017/5/26.
 * 添加设备的设备类型选择
 */

public class AddDeviceTypeActivity extends ActivityParentActivity {

    private ListView lv;
    private DeviceTypeAdapter adapter;
    private List<DeviceInfo> dInfos;

    private Map<String, Map<String, Object>> typs = new HashMap<String, Map<String, Object>>();

    private HashMap<String, Object> map = new HashMap<>();
    private CategoryInfo categoryInfo;
    // 显示图片的配置
    DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
            // .displayer(new RoundedBitmapDisplayer(20))//是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();


    private long id = -1;

    private ImageView iv_back;
    private TextView tv_title;
    private String name = "";
    private int netType = 1;
    private ZhujiInfo zhuji; //主机对象，可能为空，请注意空指针
    private Map<String,String> setInfos = new HashMap<>(); //初始化个对象 可以不用判空
    AdapterView.OnItemClickListener mItemClickListener ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_add_type);

        iv_back = (ImageView) findViewById(R.id.iv_back);
        tv_title = (TextView) findViewById(R.id.tv_title);
        lv = (ListView) findViewById(R.id.lv);
        dInfos = new ArrayList<>();

        id = getIntent().getLongExtra("id", -1);
        name = getIntent().getStringExtra("name");
        categoryInfo = (CategoryInfo) getIntent().getSerializableExtra("categoryInfo");
        zhuji = (ZhujiInfo) getIntent().getSerializableExtra("zhuji");
        netType = getIntent().getIntExtra(AddDeviceChooseActivity.NET_TYPE, 1);
        tv_title.setText(name);
        lv.addFooterView(new View(this));
        adapter = new DeviceTypeAdapter();
        lv.setAdapter(adapter);
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new TypeLoad());

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

       mItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (DeviceInfo.CaMenu.zhuji.value().equals(dInfos.get(position).getCa())) {
                    Intent intent = new Intent();
                    intent.setClass(AddDeviceTypeActivity.this, AddZhujiActivity.class);
                    startActivity(intent);
                } else if (dInfos.get(position).getId() == -1) {
                    Intent intent = new Intent();
                    intent.setClass(AddDeviceTypeActivity.this, AddDeviceOfDIYActivity.class);
                    intent.putExtra("category", categoryInfo);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent();
                    if(netType== CategoryInfo.NetTypeEnum.cable.value()){
                        String typeId = String.valueOf(typs.get(dInfos.get(position).getName()).get("id"));
                        intent.setClass(getApplicationContext(),AddWiredDeviceActivity.class);
                        intent.putExtra("zhuji",zhuji);
                        intent.putExtra("type_id",typeId);
                        intent.putExtra("name",dInfos.get(position).getName());
                        startActivity(intent);
                    }else {
                        intent.setClass(AddDeviceTypeActivity.this, AddDeviceActivity.class);
//                intent.putExtra("device", dInfos.get(position));
                        map = (HashMap<String, Object>) typs.get(dInfos.get(position).getName());
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("map", map);
                        bundle.putSerializable("device", dInfos.get(position));
                        bundle.putSerializable("zhuji", zhuji);
                        intent.putExtra("category", categoryInfo);
                        intent.putExtras(bundle);
                        if (netType == CategoryInfo.NetTypeEnum.wifiOrLan.value()) {
                            startActivityForResult(intent, 5);
                        } else {
                            startActivity(intent);
                        }
                    }
//                intent.putExtra("map", (Serializable) map);
                }
            }
        };
        lv.setOnItemClickListener(mItemClickListener);

    }


    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            cancelInProgress();
            if (msg.what == 1) {
                dInfos.clear();
//                if (!Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
//                        && !Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())
//                        &&!Actions.VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())
//                        &&!Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())
//                        &&!Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
//                    if (!"1".equalsIgnoreCase(setInfos.get(ZhujiInfo.GNSetNameMenu.supportAddDeviceByAny.value()))
//                            && categoryInfo != null && categoryInfo.getChakey().equals(DeviceInfo.CakMenu.security.value())) {
//                        DeviceInfo deviceInfo = new DeviceInfo();
//                        deviceInfo.setLogo(categoryInfo.getIoc());
//                        deviceInfo.setId(-1);
//                        deviceInfo.setName(getString(R.string.add_device_other_type));
//                        dInfos.add(deviceInfo);
//                    }
//                }
//
//                if(Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
//                        &&categoryInfo.getCkey().equals(DeviceInfo.CaMenu.menling.value())){
//                    //智利的版本,添加其它类型
//                    DeviceInfo deviceInfo = new DeviceInfo();
//                    deviceInfo.setLogo(categoryInfo.getIoc());
//                    deviceInfo.setId(-1);
//                    deviceInfo.setName(getString(R.string.add_device_other_type));
//                    dInfos.add(deviceInfo);
//                }
                dInfos.addAll((Collection<? extends DeviceInfo>) msg.obj);
               // adapter.notifyDataSetChanged();
                //宏才，凯普乐跳转，要跳转说明只有一个类型或者其它类型和相应设备类型
                if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())||(zhuji!=null && (zhuji.getMasterid().startsWith("FF30")||zhuji.getMasterid().startsWith("ff30")))){
                    if(dInfos.size()!=0){
                        //跳转
                        mItemClickListener.onItemClick(lv,null,0,0);
                        //主机下的设备添加，netType为默认值1,不需活动回传值
                        finish();
                    }
                }else{
                    adapter.notifyDataSetChanged();
                }
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 5 && resultCode == 11) {
            setResult(resultCode);
            finish();
        } else if (requestCode == 5 && resultCode == 8) {
            setResult(resultCode);
            finish();
        }
    }


    class TypeLoad implements Runnable {
        @Override
        public void run() {
            if (zhuji!=null) {
                setInfos = DatabaseOperator.getInstance().queryZhujiSets(zhuji.getId());
            }
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            String result;
            JSONObject o = new JSONObject();
            if (zhuji != null) {
                o.put("did", zhuji.getId());
            }
            if (id != -1) {
                o.put("cid", id);

                result = HttpRequestUtils
                        .requestoOkHttpPost( server + "/jdm/s3/t/all", o, AddDeviceTypeActivity.this);
            } else {

                result = HttpRequestUtils
                        .requestoOkHttpPost( server + "/jdm/s3/t/all", null, AddDeviceTypeActivity.this);
            }

            if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(AddDeviceTypeActivity.this, getString(R.string.net_error_nodata),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if (!StringUtils.isEmpty(result)) {
                typs.clear();
                List<String> ls = new ArrayList<String>();
                List<DeviceInfo> infos = new ArrayList<DeviceInfo>();
                JSONArray ll = null;
                try {
                    ll = JSON.parseArray(URLDecoder.decode(result, "UTF-8"));
                } catch (Exception e) {
                    Log.e(TAG, "解密错误：：", e);
                }
                if (ll != null) {
                    for (int j = 0; j < ll.size(); j++) {
                        DeviceInfo info = new DeviceInfo();
                        if (Actions.VersionType.CHANNEL_UCTECH.equals(getJdmApplication().getAppGlobalConfig().getVersion())) {
                            if (!((JSONObject) ll.get(j)).getString("device_type").startsWith("F007T")) {
                                continue;
                            }
                        }
                        String type = ((JSONObject) ll.get(j)).getString("device_name") + " "+ ((JSONObject) ll.get(j)).getString("device_type");
                        ls.add(type);
                        info.setName(type);
                        String logo = ((JSONObject) ll.get(j)).getString("device_logo");
                        info.setLogo(logo);
                        infos.add(info);
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("id",((JSONObject) ll.get(j)).getLongValue("id"));
                        map.put("details_url", ((JSONObject) ll.get(j)).getString("details_url") != null ?
                                ((JSONObject) ll.get(j)).getString("details_url") : "");
                        map.put("type", ((JSONObject) ll.get(j)).getString("device_type"));
                        map.put("onlyControl", ((JSONObject) ll.get(j)).getBoolean("device_control_only"));
                        map.put("autoPeidui", ((JSONObject) ll.get(j)).getBoolean("device_autopeidui"));
                        map.put("controlType", ((JSONObject) ll.get(j)).getString("device_control_type"));
                        map.put("code", ((JSONObject) ll.get(j)).get("code"));
                        map.put("pdMessage",
                                ((JSONObject) ll.get(j)).get("device_pd_message") != null
                                        ? ((JSONObject) ll.get(j)).getString("device_pd_message")
                                        : getString(R.string.add_shoudong_tips_notip));
                        map.put("apkDownload", ((JSONObject) ll.get(j)).get("device_android_download") != null
                                ? ((JSONObject) ll.get(j)).getString("device_android_download") : "");
                        map.put("apkPackage", ((JSONObject) ll.get(j)).get("device_android_package") != null
                                ? ((JSONObject) ll.get(j)).getString("device_android_package") : "");
                        if (Boolean.parseBoolean(String.valueOf(map.get("onlyControl")))) {
                            map.put("keys", ((JSONObject) ll.get(j)).getJSONArray("keys"));
                        }
                        map.put("netType", ((JSONObject) ll.get(j)).getIntValue("device_nettype"));
                        typs.put(type, map);
                    }
                }
                Message m = defaultHandler.obtainMessage(1);
                m.obj = infos;
                defaultHandler.sendMessage(m);
            }
        }
    }


    class DeviceTypeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return dInfos != null ? dInfos.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return dInfos != null ? dInfos.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DeviceInfoView spImg = new DeviceInfoView();

            if (convertView == null) {
                convertView = View.inflate(AddDeviceTypeActivity.this, R.layout.activity_device_add_type_item, null);
                spImg.ioc = (ImageView) convertView.findViewById(R.id.iv_device_logo);
                spImg.name = (TextView) convertView.findViewById(tv_device_name);

                convertView.setTag(spImg);
            } else {
                spImg = (DeviceInfoView) convertView.getTag();
            }
            setShowInfo(spImg, position);
            return convertView;
        }

        /**
         * 设置设备logo图片和名称
         *
         * @param i
         */
        private void setShowInfo(DeviceInfoView viewCache, int i) {
            if (categoryInfo != null && dInfos.get(i).getId() == -1) {
                viewCache.name.setTextColor(getResources().getColor(R.color.zhzj_default));
            }else {
                viewCache.name.setTextColor(getResources().getColor(R.color.black));
                viewCache.name.getCurrentTextColor();
            }
            //待替换
            if (DeviceInfo.ControlTypeMenu.wenduji.value().equals(dInfos.get(i).getControlType())) {
                // 设置图片
                if (Actions.VersionType.CHANNEL_UCTECH.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())) {
                    try {
                        viewCache.ioc.setImageBitmap(BitmapFactory.decodeStream(
                                getAssets().open("uctech/uctech_t_" + dInfos.get(i).getChValue() + ".png")));
                    } catch (IOException e) {
                        Log.e("uctech", "读取图片文件错误");
                    }
                } else {
                    ImageLoader.getInstance()
                            .displayImage(
                                     dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/"
                                            + dInfos.get(i).getLogo(),
                                    viewCache.ioc, options, new ImageLoadingBar());
                }
                viewCache.name.setText(dInfos.get(i).getName() + "CH" + dInfos.get(i).getChValue());
            } else if (DeviceInfo.ControlTypeMenu.wenshiduji.value().equals(dInfos.get(i).getControlType())) {
                if (Actions.VersionType.CHANNEL_UCTECH.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())) {
                    try {
                        viewCache.ioc.setImageBitmap(BitmapFactory.decodeStream(
                                getAssets().open("uctech/uctech_th_" + dInfos.get(i).getChValue() + ".png")));
                    } catch (IOException e) {
                        Log.e("uctech", "读取图片文件错误");
                    }
                } else {
                    ImageLoader.getInstance()
                            .displayImage(
                                     dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/"
                                            + dInfos.get(i).getLogo(),
                                    viewCache.ioc, options, new ImageLoadingBar());
                }
                viewCache.name.setText(dInfos.get(i).getName() + "CH" + dInfos.get(i).getChValue());
            } else {
                // 设置图片
                ImageLoader.getInstance().displayImage( dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "")
                        + "/devicelogo/" + dInfos.get(i).getLogo(), viewCache.ioc, options, new ImageLoadingBar());
                viewCache.name.setText(dInfos.get(i).getName());
            }
//            viewCache.name.setText(dInfos.get(i).getName());
//            viewCache.ioc.setImageResource(R.drawable.zhzj_menci);

        }

        class DeviceInfoView {
            ImageView ioc;
            TextView name;
        }
    }

//
//    class TypesItemAdapter extends BaseAdapter {
//        /**
//         * 视图内部类
//         *
//         * @author Administrator
//         */
//        class DeviceInfoView {
//            ImageView ioc;
//            TextView name;
//        }
//
//        LayoutInflater layoutInflater;
//
//        public TypesItemAdapter(Context context) {
//            layoutInflater = LayoutInflater.from(context);
//        }
//
//        @Override
//        public int getCount() {
//            // TODO Auto-generated method stub
//            return items.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            // TODO Auto-generated method stub
//            return items.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            // TODO Auto-generated method stub
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View view, ViewGroup parent) {
//            DeviceInfoView viewCache = new DeviceInfoView();
//            if (view == null) {
////                view = layoutInflater.inflate(
////                        R.layout.activity_scene_item, null);
//                view = layoutInflater.inflate(
//                        R.layout.activity_device_add_type_item, null);
//                viewCache.ioc = (ImageView) view.findViewById(R.id.iv_device_logo);
//                viewCache.name = (TextView) view.findViewById(R.id.tv_device_name);
//                view.setTag(viewCache);
//            } else {
//                viewCache = (DeviceInfoView) view.getTag();
//            }
//            viewCache.name.setText(items.get(position).getName());
////            viewCache.ioc.setImageResource(items.get(position).getIcon());
//            return view;
//        }
//    }

}
