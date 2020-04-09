package com.smartism.znzk.activity.device;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.add.AddDeviceChooseActivity;
import com.smartism.znzk.activity.device.add.AddDeviceOfDIYActivity;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CategoryInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions.VersionType;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.WeakRefHandler;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * 对应 任意探头添加页面
 * 展示设备分类，点击设备分类将返回点击的设备分类id
 * 请使用startActivityForResult方法打开并获取返回值
 * 返回值格式为 resultCode为 22  “category” 为categoryInfo对象
 *
 * @author wangjian
 *         参数 “filter”表示需要过滤的类型，1 只需要探头  2只需要遥控器
 */
public class DeviceCategoryActivity extends ActivityParentActivity implements OnItemClickListener {
    private GridView categoryGridView;
    private TypesItemAdapter deviceItemAdapter;
    private List<CategoryInfo> items;
    private int filter;
    private ZhujiInfo zhuji;

    //显示图片的配置
    DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow)
            .cacheInMemory(false)
            .cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_category);
        initView();
        initCategory();
    }

    private void initView() {
        categoryGridView = (GridView) findViewById(R.id.device_category);
        categoryGridView.setOnItemClickListener(this);
    }

    private void initCategory() {
        filter = getIntent().getIntExtra("filter", 0);
        zhuji = (ZhujiInfo) getIntent().getSerializableExtra("zhuji");
        showInProgress(getString(R.string.loading), false, true);
        items = new ArrayList<CategoryInfo>();
        JavaThreadPool.getInstance().excute(new LoadCategory());
    }

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            cancelInProgress();
            switch (msg.what) {
                case 1:
                    deviceItemAdapter = new TypesItemAdapter(DeviceCategoryActivity.this);
                    categoryGridView.setAdapter(deviceItemAdapter);
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    @Override
    public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(DeviceCategoryActivity.this, AddDeviceOfDIYActivity.class);
        intent.putExtra("category", items.get(position));
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {
            finish();
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
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            DeviceInfoView viewCache = new DeviceInfoView();
            if (view == null) {
                view = layoutInflater.inflate(
                        R.layout.activity_device_category_item, null);
                viewCache.ioc = (ImageView) view.findViewById(R.id.item_img);
                viewCache.name = (TextView) view.findViewById(R.id.item_txt);
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }
            viewCache.name.setText(items.get(position).getName());
            ImageLoader.getInstance().displayImage( dcsp.getString(Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/" + items.get(position).getIoc(), viewCache.ioc, options, new ImageLoadingBar());
            return view;
        }

    }

    public void back(View v) {
        finish();
    }

    @Override
    protected void onDestroy() {
        defaultHandler.removeCallbacksAndMessages(null);
        defaultHandler = null;
        super.onDestroy();
    }

    class LoadCategory implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
//			String result = HttpRequestUtils.requestHttpServer(server+"/jdm/service/categorys/all?prefix="+MainApplication.app.getJdmVersionPrefix()+"&lang="+Locale.getDefault().getLanguage()+"-"+Locale.getDefault().getCountry(),DeviceCategoryActivity.this,defaultHandler);
            JSONObject pJsonObject = new JSONObject();
            if (zhuji==null) {
//                zhuji = DatabaseOperator.getInstance(DeviceCategoryActivity.this).queryDeviceZhuJiInfo(dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, ""));
                //替换
                zhuji = DatabaseOperator.getInstance(DeviceCategoryActivity.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
            }
            if (zhuji!=null) {
                pJsonObject.put("did", zhuji.getId());
            }
            pJsonObject.put("type", "detector_any");
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/c/list", pJsonObject, DeviceCategoryActivity.this);
            if (!StringUtils.isEmpty(result)) {
                try {
                    Log.e("detection", "result:" + result);
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
                            cInfo.setIoc(o.getString("ioc"));
                            cInfo.setCkey(o.getString("ckey"));
                            cInfo.setChakey(o.getString("chakey"));
                            cInfo.setRemark(o.getString("remark"));
//                            if (o.getString("chakey") != null && filter == 1 && (o.getString("chakey").contains("security") | o.getString("chakey").contains("detection"))) {
                            if (o.getString("chakey") != null && filter == 1 && (o.getString("chakey").contains("security") ||o.getString("chakey").contains("doorbell") || (DeviceInfo.CaMenu.ybq.value().equals(o.getString("ckey")) && DeviceInfo.CakMenu.detection.value().equals(o.getString("chakey"))))) {
                                if ((o.getString("ckey").equals("wsd") || o.getString("ckey").equals("wd"))) {
                                    add = false;
                                } else {
                                    add = true;
                                }
                            } else if (o.getString("ckey") != null && filter == 2 && o.getString("ckey").startsWith("ykq_")) {
                                if (VersionType.CHANNEL_ZNZK.equals(getJdmVersionType())) {
                                    if (Integer.parseInt(o.getString("ckey").substring(o.getString("ckey").lastIndexOf("_") + 1)) <= 4) {
                                        ykq_items.add(cInfo);
                                    }
                                } else {
                                    ykq_items.add(cInfo);
                                }
                            } else if (filter == 0 && !o.getString("ckey").equals("zhuji")) {
                                add = true;
                            }
                            if (add) {
                                items.add(cInfo);
                            }
                            add = false;
                        }
                        Collections.sort(ykq_items, new ComparatorCategoryInfo());
                        items.addAll(ykq_items);
                    }
                    defaultHandler.sendEmptyMessage(1);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "获取category分类返回值解析失败,异常为：" + e.toString());
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(DeviceCategoryActivity.this, getString(R.string.net_error_response), Toast.LENGTH_LONG).show();
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
}
