package com.smartism.znzk.activity.SmartMedicine;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.user.CropImageActivity;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.MedicHabitInfo;
import com.smartism.znzk.domain.SmartMedicineBean;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.webviewimage.ImageUtil;
import com.smartism.znzk.util.webviewimage.PermissionUtil;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.view.weightPickerview.picker.DateTimePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * Created by win7 on 2017/4/15.
 */

public class SmartMedicineAddActivity extends ActivityParentActivity implements View.OnClickListener {


    private static final int P_CODE_PERMISSIONS = 101;
    private static final String TAG = "smartadd";
    private static final int REQUEST_CODE_IMAGE_CROP = 2;
    private Intent mSourceIntent;
    private static final int REQUEST_CODE_PICK_IMAGE = 0;
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 1;

    //    private Button yx_back, yx_save;
    private Context mContext;
    private CheckBox iv_before, iv_after;
//    private CheckBox time_iv_two, time_iv_three, time_iv_one;

    private FrameLayout fl_bg;
    private EditText et_name, et_num;
    private TextView number_tv, photo_tv, add_time_et, menu_tv, dos_unit;
    private ImageView iv_minus, iv_plus, iv_photo;
    private int num = 0;
    private DeviceInfo deviceInfo;
    private String time = "";
    private String sourcePath = "";
    private String format;
    //    private String str_morn = "0";
//    private String str_nonn = "0";
//    private String str_even = "0";
    private String str_before = "0";
    private String str_after = "0";

    private SmartMedicineBean bean;
    private String med_time;

    private Spinner spinner;
    private AlertView mAlertView;
    private ImageView yx_add;
    private String img_url;

    private LinearLayout ll_define;

    private List<MedicHabitInfo> habitInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_smart_add);
        mContext = this;
        habitInfos = (List<MedicHabitInfo>) getIntent().getSerializableExtra("infos");
        initView();
        initData();
    }

    private void initData() {
        bean = (SmartMedicineBean) getIntent().getSerializableExtra("MedicineInfo");
//        bean.setLogo("http://dev.smart-ism.com:9999/devicelogo/upfile/2017/07/13/lP76FY.jpg");
        spinner = (Spinner) findViewById(R.id.field_item_spinner_content);
        //资源转[]
        String unit[] = getResources().getStringArray(R.array.med_unit);

        //构造ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item, unit);
        //设置下拉样式以后显示的样式
        adapter.setDropDownViewResource(R.layout.my_drop_down_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    dos_unit.setText(getString(R.string.unit_pian));
                } else {
                    dos_unit.setText(getString(R.string.unit_li));

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (bean != null) {
            menu_tv.setText(getString(R.string.smart_medc_update));
            img_url = bean.getLogo();
            if (!TextUtils.isEmpty(img_url)) {
                photo_tv.setVisibility(View.GONE);
                ImageLoader.getInstance().displayImage(img_url, iv_photo,
                        options_userlogo);
            }
            if (!TextUtils.isEmpty(bean.getAddTime())) {
                time = bean.getAddTime();
                add_time_et.setText(timeToStamp(Long.parseLong(bean.getAddTime()), "yyyy-MM-dd HH:mm:ss"));
            }
            et_name.setText(bean.getLname());
            et_num.setText(String.valueOf(bean.getTotal()));
            med_time = bean.getTakeMedicineCycle();

//            time_iv_one.setChecked((med_time.charAt(0) == '1') ? true : false);
//            time_iv_two.setChecked((med_time.charAt(1) == '1') ? true : false);
//            time_iv_three.setChecked((med_time.charAt(2) == '1') ? true : false);
//
//            if (time_iv_one.isChecked()) {
//                str_morn = "1";
//            }
//            if (time_iv_two.isChecked()) {
//                str_nonn = "1";
//            }
//            if (time_iv_three.isChecked()) {
//                str_even = "1";
//            }


            if (getString(R.string.unit_pian).equals(bean.getUnit())) {
                spinner.setSelection(0, true);
                dos_unit.setText(getString(R.string.unit_pian));
            } else {
                dos_unit.setText(getString(R.string.unit_li));
                spinner.setSelection(1, true);
            }
            number_tv.setText(String.valueOf(bean.getDosage()));
            num = bean.getDosage();
            if (bean.getAfterOrBeforeEat().equals("01")) {
                iv_after.setChecked(true);
                str_after = "1";
//                iv_after.setImageResource(R.drawable.yx_sel_check);
            } else {
                str_before = "1";
                iv_before.setChecked(true);
//                iv_before.setImageResource(R.drawable.yx_sel_check);
            }
        } else {
            num = 1;
            number_tv.setText("1");
        }
        Collections.sort(habitInfos, new SortType());
        for (int i = 0; i < habitInfos.size(); i++) {
            addViewItem(i);
        }

    }

    public class SortType implements Comparator {

        @Override
        public int compare(Object lhs, Object rhs) {

            return ((MedicHabitInfo) lhs).getType() - ((MedicHabitInfo) rhs).getType();
        }
    }

    private void addViewItem(int i) {
        MedicHabitInfo info = null;
        View view1 = View.inflate(this, R.layout.activity_medicine_show_time_view, null);


        final TextView tv_name = (TextView) view1.findViewById(R.id.tv_name);
        final CheckBox checkBox = (CheckBox) view1.findViewById(R.id.checkBox);


        info = habitInfos.get(i);
        if (info.getType() == 1) {
            info.setName(getString(R.string.smart_medc_add_time_morn));
        } else if (info.getType() == 2) {
            info.setName(getString(R.string.smart_medc_add_time_mid));
        } else if (info.getType() == 3) {
            info.setName(getString(R.string.smart_medc_add_time_noon));
        }
        tv_name.setText(info.getName());

        if (!TextUtils.isEmpty(med_time)) {
            try {
                checkBox.setChecked((med_time.charAt(i) == '1') ? true : false);
            } catch (StringIndexOutOfBoundsException e) {
                checkBox.setChecked(false);
                e.printStackTrace();
            }
            Log.e("med_time", med_time + "");
        } else {
            checkBox.setChecked(false);
        }

        ll_define.addView(view1);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fl_bg:
                showOptions();
                break;
            case R.id.add_time_et:
                Calendar calendar = Calendar.getInstance();
                DateTimePicker picker = new DateTimePicker(this, DateTimePicker.HOUR_OF_DAY);
                picker.setLabel(getString(R.string.smart_medc_add_time_n), getString(R.string.smart_medc_add_time_y),
                        getString(R.string.smart_medc_add_time_r), getString(R.string.smart_medc_add_time_s), getString(R.string.smart_medc_add_time_f));
                picker.setRange(2000, 2030);
                picker.setSelectedItem(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                picker.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
                    @Override
                    public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
                        if (hour.length() == 1) {
                            hour = "0" + hour;
                        }
                        if (minute.length() == 1) {
                            minute = "0" + minute;
                        }
                        format = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00";
                        time = date2TimeStamp(format, "yyyy-MM-dd HH:mm:ss");
                        Log.e(TAG, "format :" + time);
                        add_time_et.setText(format);
                    }
                });
                picker.show();
                break;
            case R.id.et_name:
                break;
            case R.id.et_num:
                break;
            case R.id.iv_before:
                if (iv_before.isChecked()) {
                    iv_after.setChecked(false);
                    str_before = "1";
                } else {
                    str_before = "0";
                    str_after = "1";
                    iv_after.setChecked(true);
                }
                break;
            case R.id.iv_after:
                if (iv_after.isChecked()) {
                    str_after = "1";
                    iv_before.setChecked(false);
                } else {
                    str_after = "0";
                    str_before = "1";
                    iv_before.setChecked(true);
                }

                break;
            case R.id.iv_photo:
                showOptions();
                break;
            case R.id.iv_minus:
                if (num == 0) {
                    number_tv.setText("0");
                } else {
                    num--;
                    number_tv.setText(String.valueOf(num));
                }
                break;
            case R.id.iv_plus:
                num++;
                number_tv.setText(String.valueOf(num));
                break;
            case R.id.yx_add:
//                sourcePath = "http://dev.smart-ism.com:9999/devicelogo/upfile/2017/07/13/lP76FY.jpg";
                //sourcepath图片本地路径
//                if (TextUtils.isEmpty(img_url)) {
//                    Toast.makeText(mContext, getString(R.string.smart_medc_add_photo), Toast.LENGTH_SHORT).show();
//                    return;
//                }
                if (TextUtils.isEmpty(add_time_et.getText().toString().trim())) {
                    Toast.makeText(mContext, getString(R.string.smart_medc_add_pl_time), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(et_name.getText().toString().trim())) {
                    Toast.makeText(mContext, getString(R.string.smart_medc_add_pl_name), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(et_num.getText().toString().trim()) || et_num.getText().toString().trim().equals("0")) {
                    Toast.makeText(mContext, getString(R.string.smart_medc_add_pl_total), Toast.LENGTH_SHORT).show();
                    return;
                }
//                String a = et_num.getText().toString().trim();
//                if (TextUtils.isEmpty(a) && a.charAt(0) == 0) {
//                    Toast.makeText(mContext, getString(R.string.smart_medc_add_pl_total), Toast.LENGTH_SHORT).show();
//                }

                StringBuilder builder = new StringBuilder("");
                for (int i = 0; i < ll_define.getChildCount(); i++) {
                    View view = ll_define.getChildAt(i);
                    CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
                    builder.append(checkBox.isChecked() ? "1" : "0");
                }
                med_time = builder.toString();
                if (!med_time.contains("1")) {
                    Toast.makeText(mContext, getString(R.string.smart_medc_add_pl_eat_time), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (num == 0) {
                    Toast.makeText(mContext, getString(R.string.smart_medc_add_pl_eat_num), Toast.LENGTH_SHORT).show();
                    return;
                }
                if ((str_before + str_after).equals("00")) {
                    Toast.makeText(mContext, getString(R.string.smart_medc_add_pl_beoraf), Toast.LENGTH_SHORT).show();
                    return;
                }

                showInProgress(getString(R.string.loading), false, true);
                JavaThreadPool.getInstance().excute(new AddMedcline());
                break;
        }
    }

    class AddMedcline implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();

            object.put("did", deviceInfo.getId());

            object.put("logo", TextUtils.isEmpty(img_url) ? "" : img_url);

            object.put("lname", et_name.getText().toString());
            object.put("unit", spinner.getSelectedItem().toString());
            //每次服药剂量
            object.put("dosage", number_tv.getText());


            object.put("takeMedicineCycle", med_time);
            //10 饭前
            object.put("afterOrBeforeEat", iv_before.isChecked() ? "10" : "01");
            object.put("total", et_num.getText().toString().trim());
            object.put("addTime", time);


            Log.e(TAG, object.toJSONString());

            String result;
            if (bean == null) {
                object.put("isStock", false);
                result = HttpRequestUtils
                        .requestoOkHttpPost(
                                 server + "/jdm/s3/dms/add", object, SmartMedicineAddActivity.this);
            } else {
                object.put("isStock", bean.isStock());
                object.put("vid", bean.getId());
                result = HttpRequestUtils
                        .requestoOkHttpPost(
                                 server + "/jdm/s3/dms/update", object, SmartMedicineAddActivity.this);
            }

            if ("0".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        //device_set_tip_success
                        cancelInProgress();
                        if (bean == null) {
                            Toast.makeText(mContext, getString(R.string.add_success),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(mContext, getString(R.string.device_set_tip_success),
                                    Toast.LENGTH_LONG).show();
                        }
                        sendBroadcast(new Intent(Actions.ACCETP_REFRESH_MEDICINE_INFO));
                        finish();

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
//            Message m = mHandler.obtainMessage(1);
//            m.obj = timerInfos;
//            mHandler.sendMessage(m);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public void showOptions() {

        mAlertView =
                new AlertView(getString(R.string.smart_medc_upload_photo), null, getString(R.string.cancel), null,
                        new String[]{getString(R.string.activity_beijingmy_makingpictures), getString(R.string.userinfo_alert_photo)},
                        this, AlertView.Style.ActionSheet, new OnItemClickListener() {
                    public void onItemClick(Object o, int position) {
//                        closeKeyboard();
                        if (position == 0) {
                            if (PermissionUtil.isOverMarshmallow()) {
                                if (!PermissionUtil.isPermissionValid(SmartMedicineAddActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                                    Toast.makeText(SmartMedicineAddActivity.this,
//                                            "请去\"设置\"中开启本应用的图片媒体访问权限",
//                                            Toast.LENGTH_SHORT).show();

                                    requestPermissionsAndroidM();
                                    return;
                                }

                                if (!PermissionUtil.isPermissionValid(SmartMedicineAddActivity.this, Manifest.permission.CAMERA)) {
//                                    Toast.makeText(SmartMedicineAddActivity.this,
//                                            "请去\"设置\"中开启本应用的相机权限",
//                                            Toast.LENGTH_SHORT).show();

                                    requestPermissionsAndroidM();
                                    return;
                                }
                            }

                            try {
                                mSourceIntent = ImageUtil.takeBigPicture();
                                startActivityForResult(mSourceIntent, REQUEST_CODE_IMAGE_CAPTURE);

                            } catch (Exception e) {
                                e.printStackTrace();
//                                Toast.makeText(SmartMedicineAddActivity.this,
//                                        "请去\"设置\"中开启本应用的相机和图片媒体访问权限",
//                                        Toast.LENGTH_SHORT).show();
                            }
                        } else if (position == 1) {
                            if (PermissionUtil.isOverMarshmallow()) {
                                if (!PermissionUtil.isPermissionValid(SmartMedicineAddActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                                    Toast.makeText(SmartMedicineAddActivity.this,
//                                            "请去\"设置\"中开启本应用的图片媒体访问权限",
//                                            Toast.LENGTH_SHORT).show();

                                    requestPermissionsAndroidM();
                                    return;
                                }

                            }
                            try {
                                mSourceIntent = ImageUtil.choosePicture();
                                startActivityForResult(mSourceIntent, REQUEST_CODE_PICK_IMAGE);
                            } catch (Exception e) {
                                e.printStackTrace();
//                                Toast.makeText(SmartMedicineAddActivity.this,
//                                        "请去\"设置\"中开启本应用的图片媒体访问权限",
//                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            mAlertView.dismiss();
                        }

                    }
                });

        mAlertView.show();
    }

    private void initView() {
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        menu_tv = (TextView) findViewById(R.id.menu_tv);
        dos_unit = (TextView) findViewById(R.id.dos_unit);
        fl_bg = (FrameLayout) findViewById(R.id.fl_bg);
        add_time_et = (TextView) findViewById(R.id.add_time_et);
        et_name = (EditText) findViewById(R.id.et_name);
        et_num = (EditText) findViewById(R.id.et_num);
//        time_iv_two = (CheckBox) findViewById(time_iv_two);
//        time_iv_three = (CheckBox) findViewById(time_iv_three);
//        time_iv_one = (CheckBox) findViewById(time_iv_one);
        iv_before = (CheckBox) findViewById(R.id.iv_before);
        iv_after = (CheckBox) findViewById(R.id.iv_after);

        iv_minus = (ImageView) findViewById(R.id.iv_minus);
        iv_plus = (ImageView) findViewById(R.id.iv_plus);

        yx_add = (ImageView) findViewById(R.id.yx_add);
        yx_add.setOnClickListener(this);

        iv_photo = (ImageView) findViewById(R.id.iv_photo);
        iv_photo.setOnClickListener(this);


        ll_define = (LinearLayout) findViewById(R.id.ll_define);

        photo_tv = (TextView) findViewById(R.id.tv);
        fl_bg.setOnClickListener(this);
        add_time_et.setOnClickListener(this);
        et_name.setOnClickListener(this);
        et_num.setOnClickListener(this);
        iv_before.setOnClickListener(this);
        iv_after.setOnClickListener(this);
        iv_minus.setOnClickListener(this);
        iv_plus.setOnClickListener(this);


        number_tv = (TextView) findViewById(R.id.number_tv);

        et_num.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 4){
                    Toast.makeText(SmartMedicineAddActivity.this,R.string.smart_medc_add_pl_total_toobig,Toast.LENGTH_SHORT).show();
                    s.delete(3,s.length());
                }
            }
        });

    }

    public void back(View v) {
        finish();
    }


    public static String timeToStamp(long date_str, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(new Date(date_str));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 日期格式字符串转换成时间戳
     *
     * @param date_str 字符串日期
     * @param format   如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String date2TimeStamp(String date_str, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return String.valueOf(sdf.parse(date_str).getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void requestPermissionsAndroidM() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> needPermissionList = new ArrayList<>();
            needPermissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            needPermissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            needPermissionList.add(Manifest.permission.CAMERA);

            PermissionUtil.requestPermissions(SmartMedicineAddActivity.this, P_CODE_PERMISSIONS, needPermissionList);

        } else {
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case P_CODE_PERMISSIONS:
                requestResult(permissions, grantResults);
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void requestResult(String[] permissions, int[] grantResults) {
        ArrayList<String> needPermissions = new ArrayList<String>();

        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (PermissionUtil.isOverMarshmallow()) {

                    needPermissions.add(permissions[i]);
                }
            }
        }

        if (needPermissions.size() > 0) {
            StringBuilder permissionsMsg = new StringBuilder();

            for (int i = 0; i < needPermissions.size(); i++) {
                String strPermissons = needPermissions.get(i);

                if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(strPermissons)) {
//                    permissionsMsg.append("," + getString(R.string.permission_storage));

                } else if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(strPermissons)) {
//                    permissionsMsg.append("," + getString(R.string.permission_storage));

                } else if (Manifest.permission.CAMERA.equals(strPermissons)) {
//                    permissionsMsg.append("," + getString(R.string.permission_camera));

                }
            }

            String strMessage = "请允许使用\"" + permissionsMsg.substring(1).toString() + "\"权限, 以正常使用APP的所有功能.";

            Toast.makeText(SmartMedicineAddActivity.this, strMessage, Toast.LENGTH_SHORT).show();

        } else {
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_IMAGE_CAPTURE:

                if (resultCode == RESULT_CANCELED)
                    return;
                sourcePath = ImageUtil.retrievePath(this, mSourceIntent, data);
                sourcePath = ImageUtil.getFileProviderPath();
//                    userinfo_logo.setImageBitmap(getSmallBitmap(sourcePath, 480, 800));
                Intent intent1 = new Intent(this, CropImageActivity.class);
                if (data != null) {

                    intent1.setData(data.getData());
                } else if (mSourceIntent != null) {
                    Uri uri = mSourceIntent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
                    intent1.setData(uri);
                }
                intent1.putExtra("path", sourcePath);
                intent1.putExtra("type", 2);
                intent1.putExtra("logo",false);
                startActivityForResult(intent1, REQUEST_CODE_IMAGE_CROP);

//                if (resultCode == RESULT_OK) {
//                    sourcePath = ImageUtil.retrievePath(this, mSourceIntent, data);
//                    photo_tv.setVisibility(View.GONE);
//                    iv_photo.setImageBitmap(getSmallBitmap(sourcePath, 480, 800));
//                }
                break;
            case REQUEST_CODE_PICK_IMAGE:

                try {
                    sourcePath = ImageUtil.retrievePath(this, mSourceIntent, data);
                    if (sourcePath == null) {
                        return;
                    }

//                    Bitmap bitmap = getSmallBitmap(sourcePath, 480, 800);
                    Intent intent = new Intent(this, CropImageActivity.class);
                    intent.setData(data.getData());
                    intent.putExtra("path", sourcePath);
                    intent.putExtra("type", 2);
                    intent.putExtra("logo",false);
                    startActivityForResult(intent, REQUEST_CODE_IMAGE_CROP);
//                    userinfo_logo.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                try {
//                    sourcePath = ImageUtil.retrievePath(this, mSourceIntent, data);
//                    if (sourcePath == null) {
//                        return;
//                    }
//                    photo_tv.setVisibility(View.GONE);
//                    Bitmap bitmap = getSmallBitmap(sourcePath, 480, 800);
//                    iv_photo.setImageBitmap(bitmap);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                break;

            case REQUEST_CODE_IMAGE_CROP:
                //http://dev.smart-ism.com:9999/devicelogo/upfile/2017/07/13/lP76FY.jpg
                if (resultCode == RESULT_CANCELED)
                    return;
                img_url = data.getStringExtra("path");
                if (!TextUtils.isEmpty(img_url)) {
                    photo_tv.setVisibility(View.GONE);
                    ImageLoader.getInstance().displayImage(img_url, iv_photo,
                            options_userlogo);
                }

                break;
        }
    }

    // logo图片的配置
    DisplayImageOptions options_userlogo = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
//            .displayer(new RoundedBitmapDisplayer(40))// 是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();


    /**
     * 计算图片的缩放值
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * 根据路径获得突破并压缩返回bitmap用于显示
     *
     * @param filePath
     * @return
     */
    public static Bitmap getSmallBitmap(String filePath, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;  //只返回图片的大小信息
        BitmapFactory.decodeFile(filePath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }
}
