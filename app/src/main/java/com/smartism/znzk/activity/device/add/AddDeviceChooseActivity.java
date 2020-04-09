package com.smartism.znzk.activity.device.add;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.DeviceCategoryActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CategoryInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.StringUtils;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.zbarscan.ScanCaptureActivity;
import com.smartism.znzk.zhicheng.tasks.LoadZhujiAndDeviceTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 添加设备选择设备类型
 */
public class AddDeviceChooseActivity extends ActivityParentActivity implements OnClickListener, AdapterView.OnItemClickListener {
    //	private RelativeLayout qr_layout,sd_layout,hi_layout;
//    private GridView chooseGridView;
    public static final String NET_TYPE = "netType";
    private TypesItemAdapter deviceItemAdapter;
//    private List<DeviceTypesInfo> items;

    private List<CategoryInfo> items;

    private ListView listView;

    private ImageView qrcode_add_iv, his_add_iv, iv_back, probe_add_iv,iv_menu_qrcode;

    private LinearLayout layout_scan_history, ll_prode,ll_devicetype,ll_history;
    private int netType;
    private ZhujiInfo zhuji;

    private int filter = 1;//这里只会为1 没地方修改它

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            cancelInProgress();
            deviceItemAdapter = new TypesItemAdapter(AddDeviceChooseActivity.this);
            listView.setAdapter(deviceItemAdapter);
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_device_choose);
        setContentView(R.layout.activity_device_add);
        initView();
    }

    private void initView() {
        layout_scan_history = (LinearLayout) findViewById(R.id.layout_scan_history);
        ll_devicetype = (LinearLayout) findViewById(R.id.ll_devicetype);
        ll_prode = (LinearLayout) findViewById(R.id.ll_prode);
        ll_history = (LinearLayout) findViewById(R.id.ll_history);

        if (!MainApplication.app.getAppGlobalConfig().isShowStudyProbe()) {
            ll_prode.setVisibility(View.GONE);
        }
        netType = getIntent().getIntExtra(NET_TYPE, 1);
        if (netType == 0 || Actions.VersionType.CHANNEL_AIERFUDE.equals(((MainApplication) mContext.getApplication()).getAppGlobalConfig().getVersion())
                || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                ||Actions.VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
            layout_scan_history.setVisibility(View.GONE);
        }
        zhuji = (ZhujiInfo) getIntent().getSerializableExtra("zhuji");
        qrcode_add_iv = (ImageView) findViewById(R.id.qrcode_add_iv);
        his_add_iv = (ImageView) findViewById(R.id.his_add_iv);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        probe_add_iv = (ImageView) findViewById(R.id.probe_add_iv);
        iv_menu_qrcode = (ImageView) findViewById(R.id.iv_menu_qrcode);

        if (Actions.VersionType.CHANNEL_AIERFUDE.equals(((MainApplication) mContext.getApplication()).getAppGlobalConfig().getVersion())
                || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
            iv_menu_qrcode.setVisibility(View.VISIBLE);
        }

        if(Actions.VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            qrcode_add_iv.setVisibility(View.GONE);
        }
        iv_back.setOnClickListener(this);
        probe_add_iv.setOnClickListener(this);
        qrcode_add_iv.setOnClickListener(this);
        his_add_iv.setOnClickListener(this);
        iv_menu_qrcode.setOnClickListener(this);

        listView = (ListView) findViewById(R.id.list_item);

        showInProgress(getString(R.string.loading), false, true);
        items = new ArrayList<CategoryInfo>();
        listView.setOnItemClickListener(this);

        if (zhuji!=null) {
            Map<String, String> setInfos = DatabaseOperator.getInstance().queryZhujiSets(zhuji.getId());
            if ("1".equalsIgnoreCase(setInfos.get(ZhujiInfo.GNSetNameMenu.supportAddDeviceByType.value()))) {
                ll_devicetype.setVisibility(View.GONE);
            }
            if ("1".equalsIgnoreCase(setInfos.get(ZhujiInfo.GNSetNameMenu.supportAddDeviceByAny.value()))) {
                ll_prode.setVisibility(View.GONE);
            }
            if ("1".equalsIgnoreCase(setInfos.get(ZhujiInfo.GNSetNameMenu.supportAddDeviceByHistory.value()))) {
                ll_history.setVisibility(View.GONE);
            }
            if (ll_history.getVisibility() == View.GONE && ll_prode.getVisibility() == View.GONE){
                iv_menu_qrcode.setVisibility(View.VISIBLE);
                layout_scan_history.setVisibility(View.GONE);
            }
            //有线防区添加是否显示
//            if ("1".equalsIgnoreCase(setInfos.get(ZhujiInfo.GNSetNameMenu.supportCable.value()))) {
//                JavaThreadPool.getInstance().excute(new LoadCategory(CategoryInfo.NetTypeEnum.cable.value()));
//            }
        }

        if(netType==3){
            layout_scan_history.setVisibility(View.GONE);
        }else if(Actions.VersionType.CHANNEL_DITAIXING.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            if(ZhujiListFragment.getMasterId().contains("FF41")){
                layout_scan_history.setVisibility(View.GONE);
            }
        }
        JavaThreadPool.getInstance().excute(new LoadCategory(netType));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(this, AddDeviceTypeActivity.class);
        intent.putExtra("id", items.get(position).getId());
        intent.putExtra("name", items.get(position).getName());
        intent.putExtra("categoryInfo", items.get(position));
        intent.putExtra("zhuji", zhuji);
        intent.putExtra(NET_TYPE, items.get(position).getNetType());
        if (items.get(position).getNetType() == CategoryInfo.NetTypeEnum.wifiOrLan.value() || items.get(position).getChakey().equals("security")) {
            startActivityForResult(intent, 5);
        } else if(items.get(position).getNetType() == CategoryInfo.NetTypeEnum.cable.value()){
            checkWiredZoneCount(zhuji,intent);
        }else {
            startActivity(intent);
        }
    }

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

    //安霸，检查有线防区设备是否满了
    private void checkWiredZoneCount(final ZhujiInfo zhuji,final Intent intent){
        new LoadZhujiAndDeviceTask().queryDeviceInfosByZhuji(zhuji.getId(), new LoadZhujiAndDeviceTask.ILoadResult<List<DeviceInfo>>() {
            @Override
            public void loadResult(List<DeviceInfo> result) {
                int total = 4;

                if (null == zhuji.getSetInfos().get(ZhujiInfo.GNSetNameMenu.cableCount.value())
                        || "4".equals(zhuji.getSetInfos().get(ZhujiInfo.GNSetNameMenu.cableCount.value()))) {
                    total = 4;
                }else{
                    try {
                        total = Integer.parseInt(zhuji.getSetInfos().get(ZhujiInfo.GNSetNameMenu.cableCount.value()));
                    }catch (Exception ex){
                        Log.i(TAG, "获取total个数错误：",ex);
                    }
                }

                if(result!=null&&result.size()>0){
                    for(int i=0;i< result.size();i++){
                        if(result.get(i).getNt() == 1){
                            total --;
                        }
                    }
                }

                if(total <= 0){
                    //没有可添加的有线防区设备
                    ToastTools.short_Toast(getApplicationContext(),getString(R.string.adwa_wired_zone_flow));
                }else{
                    //跳转至添加有线页面
                    startActivity(intent);
                }
            }
        });
    }

    class LoadCategory implements Runnable {

        //s3/c/list 这个接口，此参数已失效
        private int netType = 1;

        LoadCategory(int netType){
            this.netType = netType;
        }

        @Override
        public void run() {
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("net", netType);
            if (zhuji != null){
                pJsonObject.put("did", zhuji.getId());
            }
            pJsonObject.put("type","detector_type");
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/c/list", pJsonObject, AddDeviceChooseActivity.this);
            if (!StringUtils.isEmpty(result)) {
                try {
                    boolean add = false;
                    JSONArray r = JSON.parseArray(result);
                    if (r != null && !r.isEmpty()) {
                        List<CategoryInfo> ykq_items = new ArrayList<CategoryInfo>();
                        for (int i = 0; i < r.size(); i++) {
                            JSONObject o = r.getJSONObject(i);
                            CategoryInfo cInfo = new CategoryInfo();
                            cInfo.setId(o.getLongValue("id"));
                            cInfo.setP_id(o.getLongValue("parent"));
                            cInfo.setName(o.getString("name"));
                            cInfo.setNetType(o.getIntValue("netType"));
                            cInfo.setIoc(o.getString("ioc"));
                            cInfo.setCkey(o.getString("ckey"));
                            cInfo.setChakey(o.getString("chakey"));
                            cInfo.setRemark(o.getString("remark"));
                            if (Actions.VersionType.CHANNEL_UCTECH.equals(getJdmVersionType())) {
                                if (o.getString("ckey").equals("wsd") || o.getString("ckey").equals("wd")) {
                                    add = true;
                                } else {
                                    add = false;
                                }
                            } else if (o.getString("chakey") != null && filter == 1 && (o.getString("chakey").contains("security") | o.getString("chakey").contains("detection"))) {
                                if (Actions.VersionType.CHANNEL_JUJIANG.equals(getJdmVersionType()) && (o.getString("ckey").equals("wsd") || o.getString("ckey").equals("wd"))) {
                                    add = false;
                                } else {
                                    add = true;
                                }
                            } else if (o.getString("ckey") != null && filter == 2 && o.getString("ckey").startsWith("ykq_")) {
                                if (Actions.VersionType.CHANNEL_ZNZK.equals(getJdmVersionType())) {
                                    if (Integer.parseInt(o.getString("ckey").substring(o.getString("ckey").lastIndexOf("_") + 1)) <= 4) {
                                        ykq_items.add(cInfo);
                                    }
                                } else {
                                    ykq_items.add(cInfo);
                                }
                            } else if (!o.getString("ckey").equals("zhuji")) {
                                add = true;
                            }
                            if (add) {
                                items.add(cInfo);
                            }
                            add = false;
                        }
                        Collections.sort(ykq_items, new ComparatorCategoryInfo());
                        items.addAll(ykq_items);
//                        if (netType != 3) {
//                            //艾立恩 只支持控制类？
//                            if (zhuji != null && zhuji.getMasterid().contains("FF3B")) {
//                                ArrayList<CategoryInfo> temp = new ArrayList<>();
//                                for (int i = 0; i < items.size(); i++) {
//                                    if (items.get(i).getChakey().equals("control")) {
//                                        temp.add(items.get(i));
//                                    }
//                                }
//                                items.clear();
//                                items.addAll(temp);
//                            }
//                        }
                    }
                    defaultHandler.sendEmptyMessage(1);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "获取category分类返回值解析失败,异常为：" + e.toString());
                    defaultHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddDeviceChooseActivity.this, getString(R.string.net_error_response), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }
    }

    public class ComparatorCategoryInfo implements Comparator<CategoryInfo> {

        @Override
        public int compare(CategoryInfo o1,
                           CategoryInfo o2) {
            return Integer.parseInt(o1.getCkey().substring(o1.getCkey().indexOf("_") + 1, o1.getCkey().length())) - Integer.parseInt(o2.getCkey().substring(o2.getCkey().indexOf("_") + 1, o2.getCkey().length()));
        }
    }


    class DeviceTypesInfo {
        private int icon;
        private String name;

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }


    class TypesItemAdapter extends BaseAdapter {
        /**
         * 视图内部类
         *
         * @author Administrator
         */
        class DeviceInfoView {
            ImageView ioc;
            TextView name;
        }

        LayoutInflater layoutInflater;

        public TypesItemAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            DeviceInfoView viewCache = new DeviceInfoView();
            if (view == null) {
//                view = layoutInflater.inflate(
//                        R.layout.activity_scene_item, null);
                view = layoutInflater.inflate(
                        R.layout.activity_device_add_item, null);
//                viewCache.ioc = (ImageView) view.findViewById(R.id.scene_item_img);
                viewCache.name = (TextView) view.findViewById(R.id.name);
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }
            viewCache.name.setText(items.get(position).getName());
//            viewCache.ioc.setImageResource(items.get(position).getIcon());
            //宏才
            if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                viewCache.name.setText("("+(position+1)+")"+items.get(position).getName());
            }
            return view;
        }
    }

    public void back(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.iv_menu_qrcode:
            case R.id.qrcode_add_iv:
                intent.putExtra(Constant.CAPUTRE_REQUESTCOE, Constant.CAPUTRE_ADDDEVICE);
                intent.setClass(this, ScanCaptureActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.probe_add_iv://任意探头
                intent.setClass(getApplicationContext(), DeviceCategoryActivity.class);
                intent.putExtra("zhuji",zhuji);
                intent.putExtra("filter", 1); // 1为探头
                startActivity(intent);
                break;
            case R.id.iv_back:
                finish();
                break;
//            case sd_layout:
//                intent = new Intent();
//                intent.setClass(this, AddDeviceActivity.class);
//                startActivity(intent);
//                finish();
//                break;
            case R.id.his_add_iv:
                intent = new Intent();
                intent.setClass(this, AddDeviceFromHistoryActivity.class);
                startActivity(intent);
//                finish();
                break;
            default:
                break;
        }
    }
}
