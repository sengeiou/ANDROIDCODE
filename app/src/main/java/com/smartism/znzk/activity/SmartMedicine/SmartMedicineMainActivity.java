package com.smartism.znzk.activity.SmartMedicine;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.MedicHabitInfo;
import com.smartism.znzk.domain.SmartMedicineBean;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Created by win7 on 2017/4/15.
 */

public class SmartMedicineMainActivity extends ActivityParentActivity implements View.OnClickListener {
    private RecyclerView recyclerView;
    private Context mContext;
    private List<SmartMedicineBean> beanList;
    private MedicineAdapter myAdapter;
    private TextView d_name, d_type, d_elelctricity, tv_history, tv_title, tv_habit;
    private ImageView yx_add, device_logo;
    private DeviceInfo deviceInfo;
    private int size = 50;
    private MedMenuPopupWindow popupWindow;
    private LinearLayout linearLayout;


    // logo图片的配置
    DisplayImageOptions options_userlogo = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
//            .displayer(new RoundedBitmapDisplayer(40))// 是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();
    private int totalSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_smart_main);
        mContext = this;
        initView();
        initData();
    }

    private void initView() {
        linearLayout = (LinearLayout) findViewById(R.id.ll_layout);
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");

        d_name = (TextView) findViewById(R.id.d_name);
        d_type = (TextView) findViewById(R.id.d_type);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText((deviceInfo.getName() != null ? deviceInfo.getName() : getString(R.string.smart_medc_main_title)));
        d_elelctricity = (TextView) findViewById(R.id.d_elelctricity);
        tv_history = (TextView) findViewById(R.id.tv_history);
        tv_habit = (TextView) findViewById(R.id.tv_habit);
        yx_add = (ImageView) findViewById(R.id.yx_add);
        device_logo = (ImageView) findViewById(R.id.device_logo);
        yx_add.setOnClickListener(this);
        tv_history.setOnClickListener(this);
        tv_habit.setOnClickListener(this);
        d_name.setText(getString(R.string.smart_medc_main_title));
        d_type.setText(deviceInfo.getType());
        d_elelctricity.setText(deviceInfo.isLowb() ? getString(R.string.qwq_battry_low) : getString(R.string.normal));
        recyclerView = (RecyclerView) findViewById(R.id.recyleview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // 设置图片
        ImageLoader.getInstance().displayImage( dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "")
                + "/devicelogo/" + deviceInfo.getLogo(), device_logo, options_userlogo);
    }

    private void initData() {
        habitInfos = new ArrayList<>();
        popupWindow = new MedMenuPopupWindow(this, this);
        beanList = new ArrayList<>();
        myAdapter = new MedicineAdapter(beanList, mContext);
        recyclerView.setAdapter(myAdapter);
        array = new JSONArray();
        showInProgress(getString(R.string.ongoing), false, true);
        mHandler.sendEmptyMessageDelayed(10, 8 * 1000);
        JavaThreadPool.getInstance().excute(new CommandLoad(0, size));
        JavaThreadPool.getInstance().excute(new Medcline());
        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.ACCETP_REFRESH_MEDICINE_INFO);
        filter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        registerReceiver(receiver, filter);
    }


    class Medcline implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();

            object.put("id", deviceInfo.getId());


            Log.e(TAG, object.toJSONString());

            String result;
            result = HttpRequestUtils
                    .requestoOkHttpPost(
                             server + "/jdm/s3/duth/list", object, SmartMedicineMainActivity.this);

            List<MedicHabitInfo> infos = new ArrayList<>();

            if (result.length() > 4) {

                JSONArray array = JSONObject.parseObject(result).getJSONArray("result");

                for (int i = 0; i < array.size(); i++) {
                    MedicHabitInfo info = new MedicHabitInfo();
                    JSONObject o = array.getJSONObject(i);
                    info.setId(o.getLongValue("id"));
                    info.setEndTime(o.getIntValue("endTime"));
                    info.setStartTime(o.getIntValue("startTime"));
                    info.setType(o.getIntValue("type"));
                    info.setValid(o.getBooleanValue("valid"));
                    info.setName(o.getString("name"));
                    infos.add(info);
                }
                Message m = mHandler.obtainMessage(2);
                m.obj = infos;
                mHandler.sendMessage(m);
            } else if (result.equals("{}")) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.smart_medc_main_habit_set), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.register_tip_empty),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Actions.ACCETP_REFRESH_MEDICINE_INFO.equals(intent.getAction())) {
                showInProgress(getString(R.string.ongoing), false, true);
                if (array != null) {
                    array.clear();
                }
                JavaThreadPool.getInstance().excute(new CommandLoad(0, size));
            } else if (Actions.ACCETP_ONEDEVICE_MESSAGE.equals(intent.getAction())) {
                if (String.valueOf(deviceInfo.getId()).equals(intent.getStringExtra("device_id"))) {
                    if (array != null) {
                        array.clear();
                    }
                    Log.e(TAG, "medic_push");
                    JavaThreadPool.getInstance().excute(new CommandLoad(0, size));
                }
            }
        }
    };

    private int itemPosition = -1;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    public void back(View v) {
        finish();
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            mHandler.sendEmptyMessageDelayed(10, 8 * 1000);
            JavaThreadPool.getInstance().excute(new Medcline());
            JavaThreadPool.getInstance().excute(new CommandLoad(0, size));
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = getIntent();
        switch (v.getId()) {
            case R.id.yx_add:
                if (habitInfos.size() == 0) {
                    Toast.makeText(mContext, getString(R.string.smart_medc_main_habit_set), Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.setClass(mContext, SmartMedicineAddActivity.class);
                intent.putExtra("device", deviceInfo);
                intent.putExtra("infos", (Serializable) habitInfos);
                startActivity(intent);
                break;
            case R.id.tv_history:
                intent.setClass(mContext, SMartMedicineHistoryActivity.class);
                intent.putExtra("device", deviceInfo);
                startActivity(intent);
                break;
            case R.id.tv_habit:
                intent.setClass(mContext, MedicineSetTimeActivity.class);
                intent.putExtra("infos", (Serializable) habitInfos);
                intent.putExtra("device", deviceInfo);
                startActivityForResult(intent, 1);
                break;
            case R.id.btn_setdevice:
                popupWindow.dismiss();
//                Intent intent2 = new Intent();
//                intent2.setClass(SmartMedicineMainActivity.this, SmartMedicineAddActivity.class);
//                intent2.putExtra("MedicineInfo", beanList.get(itemPosition));
//                intent2.putExtra("device", deviceInfo);
//                startActivity(intent2);

                break;
            case R.id.btn_deldevice:
                popupWindow.dismiss();
                new AlertView(getString(R.string.deviceslist_server_leftmenu_deltitle),
                        getString(R.string.med_del),
                        getString(R.string.deviceslist_server_leftmenu_delcancel),
                        new String[]{getString(R.string.deviceslist_server_leftmenu_delbutton)}, null,
                        mContext, AlertView.Style.Alert,
                        new OnItemClickListener() {

                            @Override
                            public void onItemClick(Object o, final int position) {
                                if (position != -1) {
                                    showInProgress(getString(R.string.deviceslist_server_leftmenu_deltips), false, true);
                                    JavaThreadPool.getInstance().excute(new Runnable() {

                                        @Override
                                        public void run() {

                                            String server = dcsp.getString(
                                                    DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                                            JSONObject object = new JSONObject();
                                            object.put("id", deviceInfo.getId());
                                            JSONObject object1 = new JSONObject();
                                            object1.put("vid", beanList.get(itemPosition).getId());
                                            JSONArray array = new JSONArray();
                                            array.add(object1);
                                            object.put("vids", array);
                                            server = server + "/jdm/s3/dms/del";
                                            String result = HttpRequestUtils.requestoOkHttpPost( server, object, SmartMedicineMainActivity.this);
                                            // -1参数为空，0删除成功
                                            if (result != null && result.equals("0")) {
                                                mHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        cancelInProgress();
                                                        Toast.makeText(SmartMedicineMainActivity.this, getString(R.string.device_del_success), Toast.LENGTH_SHORT).show();
                                                        beanList.remove(itemPosition);
                                                        myAdapter.notifyDataSetChanged();
                                                    }
                                                });

                                            }
                                        }
                                    });
                                }
                            }
                        }).show();
                break;
        }
    }

    private JSONArray array;
    private List<MedicHabitInfo> habitInfos;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
//                    beanList.clear();
                    List<SmartMedicineBean> list;
                    if (array == null || totalSize == 0 || msg.obj == null) {
                        cancelInProgress();
                        return true;
                    }
                    array.clear();
                    array.addAll((JSONArray) msg.obj);

                    if (array.size() != totalSize) {
                        JavaThreadPool.getInstance().excute(new CommandLoad(array.size(), size));
                        return true;
                    }
                    list = JSON.parseArray(array.toJSONString(), SmartMedicineBean.class);
                    beanList.clear();
                    beanList.addAll(list);
                    cancelInProgress();
                    Log.e("smart_medic_data", beanList.toString());
//                    myAdapter = new MedicineAdapter(beanList,mContext);
                    myAdapter.notifyDataSetChanged();

                    break;
                case 2:
                    cancelInProgress();
                    if (msg.obj == null)
                        return true;
                    if (habitInfos==null)
                        habitInfos = new ArrayList<>();
                    habitInfos.clear();
                    habitInfos.addAll((Collection<? extends MedicHabitInfo>) msg.obj);
                    break;
                case 10:
                    cancelInProgress();
                    Toast.makeText(mContext, getString(R.string.time_out), Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    class CommandLoad implements Runnable {
        private int start, size;

        public CommandLoad(int start, int size) {
            this.size = size;
            this.start = start;
        }

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", deviceInfo.getId());
            object.put("start", this.start);
            object.put("size", this.size);
            String result = HttpRequestUtils.requestoOkHttpPost(
                     server + "/jdm/s3/dms/list", object,
                    SmartMedicineMainActivity.this);
            if ("-3".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(SmartMedicineMainActivity.this, getString(R.string.history_response_nodevice),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if (result.length() > 4) {
                JSONObject object1 = JSONObject.parseObject(result);
                JSONArray array = object1.getJSONArray("result");
                totalSize = object1.getIntValue("total");
                Message m = mHandler.obtainMessage(1);
                m.obj = array;
                mHandler.sendMessage(m);
/*                // 请求成功了，需要刷新数据到页面，也需要清除此设备的历史未读记录
                ContentValues values = new ContentValues();
                values.put("nr", 0); // 未读消息数
                DatabaseOperator.getInstance(SmartMedicineMainActivity.this).getWritableDatabase().update(
                        "DEVICE_STATUSINFO", values, "id = ?", new String[]{String.valueOf(deviceInfo.getId())});*/
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    class UpdateMedcline implements Runnable {
        public SmartMedicineBean bean;

        public UpdateMedcline(SmartMedicineBean bean) {
            this.bean = bean;
        }

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();

            object.put("did", deviceInfo.getId());

            object.put("logo", bean.getLogo());

            object.put("lname", bean.getLname());
            object.put("unit", bean.getUnit());
            //每次服药剂量
            object.put("dosage", bean.getDosage());


            object.put("takeMedicineCycle", bean.getTakeMedicineCycle());
            //10 饭前
            object.put("afterOrBeforeEat", bean.getAfterOrBeforeEat());
            object.put("total", bean.getTotal());
            object.put("addTime", bean.getAddTime());
            object.put("isStock", bean.isStock());

            Log.e(TAG, object.toJSONString());

            String result;

            object.put("vid", bean.getId());
            result = HttpRequestUtils
                    .requestoOkHttpPost(
                             server + "/jdm/s3/dms/update", object, SmartMedicineMainActivity.this);


            if ("0".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        //device_set_tip_success
//                        cancelInProgress();

                        Toast.makeText(mContext, getString(R.string.device_set_tip_success),
                                Toast.LENGTH_LONG).show();
                        sendBroadcast(new Intent(Actions.ACCETP_REFRESH_MEDICINE_INFO));
                    }
                });
            } else {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.register_tip_empty),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }


    class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineHolder> {
        private List<SmartMedicineBean> mDatas;
        private LayoutInflater inflater;

        public MedicineAdapter(List<SmartMedicineBean> datas, Context context) {
            this.mDatas = datas;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public MedicineHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_medicine, null);
            MedicineHolder holder = new MedicineHolder(view);

            return holder;
        }

        @Override
        public void onBindViewHolder(final MedicineHolder holder, final int position) {

            final SmartMedicineBean bean = mDatas.get(position);
            holder.logo.setImageResource(R.drawable.icon_yaopin);
            holder.name.setText(bean.getLname());
            holder.number.setText((bean.getTotal() <= 0 ? 0 : bean.getTotal()) + " " + bean.getUnit());
            holder.yes_iv.setChecked(bean.isStock() ? true : false);
            holder.yes_iv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!buttonView.isPressed())
                        return;
                    if (isChecked) {
                        bean.setStock(true);
                    } else {
                        bean.setStock(false);
                    }
                    showInProgress(getString(R.string.ongoing), false, true);
                    mHandler.sendEmptyMessageDelayed(10, 8 * 1000);
                    JavaThreadPool.getInstance().excute(new UpdateMedcline(bean));
                }
            });
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (habitInfos.size() == 0) {
                        Toast.makeText(mContext, getString(R.string.smart_medc_main_habit_set), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent2 = new Intent();
                    intent2.setClass(SmartMedicineMainActivity.this, SmartMedicineAddActivity.class);
                    intent2.putExtra("MedicineInfo", beanList.get(position));
                    intent2.putExtra("infos", (Serializable) habitInfos);
                    intent2.putExtra("device", deviceInfo);
                    startActivity(intent2);
                }
            });
            holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    itemPosition = position;
                    popupWindow.updateDeviceMenu(mContext);
                    popupWindow.showAtLocation(linearLayout, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置

                    WindowManager.LayoutParams manager = getWindow().getAttributes();
                    manager.alpha = 0.7f;
                    getWindow().setAttributes(manager);
                    return false;
                }
            });
        }


        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        class MedicineHolder extends RecyclerView.ViewHolder {
            private TextView name, number;
            private ImageView logo;
            private CheckBox yes_iv;
            private View view;

            public MedicineHolder(View itemView) {
                super(itemView);
                view = itemView;
                name = (TextView) itemView.findViewById(R.id.item_tv_name);
                number = (TextView) itemView.findViewById(R.id.item_tv_number);
                logo = (ImageView) itemView.findViewById(R.id.item_iv_img);
                yes_iv = (CheckBox) itemView.findViewById(R.id.yes_iv);
            }
        }
    }

    public class MedMenuPopupWindow extends PopupWindow {

        private View mMenuView;
        private Button btn_deldevice, btn_setdevice;

        public MedMenuPopupWindow(Context context, View.OnClickListener itemsOnClick) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mMenuView = inflater.inflate(R.layout.zss_item_menu, null);
            btn_deldevice = (Button) mMenuView.findViewById(R.id.btn_deldevice);
            btn_setdevice = (Button) mMenuView.findViewById(R.id.btn_setdevice);

            btn_deldevice.setOnClickListener(itemsOnClick);
            btn_setdevice.setOnClickListener(itemsOnClick);
            //设置SelectPicPopupWindow的View
            this.setContentView(mMenuView);
            //设置SelectPicPopupWindow弹出窗体的宽
            this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            //设置SelectPicPopupWindow弹出窗体的高
            this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            //设置SelectPicPopupWindow弹出窗体可点击
            this.setFocusable(true);
            //设置SelectPicPopupWindow弹出窗体动画效果
            this.setAnimationStyle(R.style.Devices_list_menu_Animation);
            //实例化一个ColorDrawable颜色为半透明
            ColorDrawable dw = new ColorDrawable(0x00000000);
            //设置SelectPicPopupWindow弹出窗体的背景
            this.setBackgroundDrawable(dw);
            //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
            mMenuView.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {

                    int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                    int y = (int) event.getY();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (y < height) {
                            dismiss();
                        }
                    }
                    return true;
                }
            });

            this.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss() {
                    WindowManager.LayoutParams mana = getWindow().getAttributes();
                    mana.alpha = 1.0f;
                    getWindow().setAttributes(mana);
                }
            });

        }


        public void updateDeviceMenu(Context context) {
            btn_setdevice.setText(context.getResources().getString(R.string.cancel));
//            btn_setdevice.setVisibility(View.GONE);
            btn_deldevice.setText(context.getResources().getString(R.string.zss_item_del));
        }

    }

}
