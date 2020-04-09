package com.smartism.znzk.activity.user;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.webviewimage.ImageUtil;
import com.smartism.znzk.util.webviewimage.PermissionUtil;
import com.smartism.znzk.view.CircleImageView;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class UserInfoActivity extends ActivityParentActivity implements View.OnClickListener, OnItemClickListener {
    private static final int ADD_UPDATE_BINDPHONE = 5;
    private static final int UNBIND_PHONE = 4;
    private static final int BIND_OR_UNBIND = 1000;
    private LinearLayout ll_accountinfo, ll_charge;
    private TextView recharge, record, tv_userinfo_name, userinfo_account;
    private CircleImageView userinfo_logo;
    private RelativeLayout rl_change_password, rl_update_nickname, rl_user;
    private static final int IMAGE_REQUEST_CODE = 0;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int RESIZE_REQUEST_CODE = 2;
    private static final int time_out = 100;
    private static final String IMAGE_FILE_NAME = "default_head_image.png";

    private static final int P_CODE_PERMISSIONS = 101;
    private static final int REQUEST_CODE_IMAGE_CROP = 10;
    private static final String TAG = "smartadd";
    private Intent mSourceIntent;
    private static final int REQUEST_CODE_PICK_IMAGE = 5;
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 6;
    private String sourcePath = "";
    private File mTakePictureFile ;
    private AlertView mAlertView;//避免创建重复View，先创建View，然后需要的时候show出来，推荐这个做法
    // logo图片的配置
    DisplayImageOptions options_userlogo = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
            .displayer(new RoundedBitmapDisplayer(40))// 是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();
    String number, mEmail;
    JSONArray bindArray;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case time_out:
                    cancelInProgress();
                    Toast.makeText(mContext, getString(R.string.time_out), Toast.LENGTH_SHORT).show();
                    break;
                case 1: // 初始化数据加载完成
                    cancelInProgress();
                    JSONObject userJsonObject = (JSONObject) msg.obj;
                    String account = String.valueOf(userJsonObject.get("a"));
                    dcsp.putString(Constant.ACCOUNT, account).commit();
                    if (userJsonObject.containsKey("l")) {
                        String url_logo = String.valueOf(userJsonObject.get("l"));
//                        dcsp.putString(Constant.LOGIN_LOGO, url_logo).commit();
                        ImageLoader.getInstance().displayImage(dcsp.getString(Constant.LOGIN_LOGO, ""), userinfo_logo,
                                options_userlogo);
                    } else {
                        userinfo_logo.setImageResource(R.drawable.h0);
                    }


                    if (account.startsWith("$qq$_")) {
                        account = getString(R.string.activity_user_info_account_qq);
                        rl_user.setEnabled(false);
                        rl_update_nickname.setEnabled(false);
                        rl_change_password.setEnabled(false);
                        ll_qq.setEnabled(false);
//                        ll_tel.setVisibility(View.GONE);
                        ll_email.setVisibility(View.GONE);
                        ll_password.setVisibility(View.GONE);//密保资料
                    } else if (account.startsWith("$weixin$_")) {
                        account = getString(R.string.activity_user_info_account_wechat);
                        rl_user.setEnabled(false);
                        rl_update_nickname.setEnabled(false);
                        rl_change_password.setEnabled(false);
                        ll_wechat.setEnabled(false);
//                        ll_tel.setVisibility(View.GONE);
                        ll_email.setVisibility(View.GONE);
                        ll_password.setVisibility(View.GONE);//密保资料
                    } else if (account.startsWith("$facebook$_")) {
                        account = getString(R.string.activity_user_info_account_facebook);
                        rl_user.setEnabled(false);
                        rl_update_nickname.setEnabled(false);
                        rl_change_password.setEnabled(false);
                        ll_facebook.setEnabled(false);
//                        ll_tel.setVisibility(View.GONE);
                        ll_email.setVisibility(View.GONE);
                        ll_password.setVisibility(View.GONE);//密保资料
                    } else if (account.startsWith("$twitter$_")) {
                        account = getString(R.string.activity_user_info_account_twitter);
                        rl_user.setEnabled(false);
                        rl_update_nickname.setEnabled(false);
                        rl_change_password.setEnabled(false);
                        ll_twitter.setEnabled(false);
//                        ll_tel.setVisibility(View.GONE);
                        ll_email.setVisibility(View.GONE);
                        ll_password.setVisibility(View.GONE);//密保资料
                    } else if (account.startsWith("$google$_")) {
                        account = getString(R.string.activity_user_info_account_google);
                        rl_user.setEnabled(false);
                        rl_update_nickname.setEnabled(false);
                        rl_change_password.setEnabled(false);
                        ll_google.setEnabled(false);
//                        ll_tel.setVisibility(View.GONE);
                        ll_email.setVisibility(View.GONE);
                        ll_password.setVisibility(View.GONE);//密保资料
                    }

                    userinfo_account.setText(account);
                    number = "";
                    number = userJsonObject.getString("m");
                    if (!TextUtils.isEmpty(number) && number.length() == 15) {
                        if (Util.checkPhoneNumber(number.substring(4))) {
                            number = number.substring(4);
                        }
                    }
                    iv_tel.setImageResource(TextUtils.isEmpty(number) ? R.drawable.zhzj_phone_m : R.drawable.zhzj_phone);
                    tv_tel.setText(TextUtils.isEmpty(number) ? getString(R.string.userinfo_activity_account_not_bind) : number.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
                    mEmail = userJsonObject.getString("e") == null ? "" : userJsonObject.getString("e");
                    iv_email.setImageResource(TextUtils.isEmpty(mEmail) ? R.drawable.zhzj_email_m : R.drawable.zhzj_email);
                    tv_email.setText(TextUtils.isEmpty(mEmail) ? getString(R.string.userinfo_activity_account_not_bind) : mEmail);
                    tv_userinfo_name.setText(String.valueOf(userJsonObject.get("n") == null ? "" : userJsonObject.get("n")));
                    break;
                case 10:
                    cancelInProgress();
                    dcsp.putString(Constant.LOGIN_APPNAME, etName.getText().toString()).commit();
                    tv_userinfo_name.setText(dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_APPNAME, ""));
                    Toast.makeText(UserInfoActivity.this, getString(R.string.userinfo_activity_updatesuccess),
                            Toast.LENGTH_LONG).show();
                    break;

                case MSG_AUTH_CANCEL:
                    //取消授权
                    cancelInProgress();
                    Toast.makeText(mContext, R.string.auth_cancel, Toast.LENGTH_SHORT).show();

                    break;
                case MSG_AUTH_ERROR:
                    //授权失败
                    cancelInProgress();
                    Toast.makeText(mContext, R.string.auth_error, Toast.LENGTH_SHORT).show();

                    break;
                case MSG_AUTH_COMPLETE:
                    //授权成功
                    cancelInProgress();
                    Toast.makeText(mContext, R.string.auth_complete, Toast.LENGTH_SHORT).show();
                    Object[] objs = (Object[]) msg.obj;
                    final String platform = (String) objs[0];
//                    final PlatformDb platformDb = (PlatformDb) objs[1];
//                    if (platformDb != null) {
////                        setHttps(platformDb);
//                        JavaThreadPool.getInstance().excute(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences
//                                        .getInstance(UserInfoActivity.this, Constant.CONFIG);
//                                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
//                                final JSONObject object = new JSONObject();
//                                if (platform.equals(getPlatform(QQ.NAME).getName())) {
//                                    object.put("p", 1);
//                                } else if (platform.equals(getPlatform(Wechat.NAME).getName())) {
//                                    object.put("p", 3);
//                                } else if (platform.equals(getPlatform(Facebook.NAME).getName())) {
//                                    object.put("p", 4);
//                                } else if (platform.equals(getPlatform(Twitter.NAME).getName())) {
//                                    object.put("p", 5);
//                                } else if (platform.equals(getPlatform(GooglePlus.NAME).getName())){
//                                    object.put("p", 6);
//                                }
//                                object.put("openid", platformDb.getUserId());
//                                object.put("unionid", platformDb.get("unionid"));
//                                object.put("name", platformDb.getUserName());
//                                object.put("logo", platformDb.getUserIcon());
////                                object.put("type", "android");
////                                object.put("lang",
////                                        Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry());
////                                object.put("tz", DateUtil.getCurrentTimeZone());
//
//                                String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/u/bft", object, UserInfoActivity.this);
//                                // -1参数为空,-5服务器错误
//                                if ("0".equals(result)) {
//                                    defHandler.post(new Runnable() {
//
//                                        @Override
//                                        public void run() {
//                                            cancelInProgress();
//                                            Toast.makeText(mContext, R.string.activity_beijingmy_bindsuccess, Toast.LENGTH_SHORT).show();
//                                            loadBindAccount();
//                                        }
//                                    });
//                                    return;
//                                } else if ("-5".equals(result)) {
//                                    defHandler.post(new Runnable() {
//
//                                        @Override
//                                        public void run() {
//                                            cancelInProgress();
//                                            Toast.makeText(UserInfoActivity.this, getString(R.string.net_error_servererror),
//                                                    Toast.LENGTH_LONG).show();
//                                        }
//                                    });
//                                    return;
//                                } else if ("-3".equals(result)) {
//                                    defHandler.post(new Runnable() {
//
//                                        @Override
//                                        public void run() {
//                                            cancelInProgress();
//                                            Toast.makeText(UserInfoActivity.this, getString(R.string.userinfo_activity_account_have_bind),
//                                                    Toast.LENGTH_LONG).show();
//                                            return;
//                                        }
//                                    });
//                                } else {
//                                    defHandler.post(new Runnable() {
//
//                                        @Override
//                                        public void run() {
//                                            cancelInProgress();
//                                            Toast.makeText(UserInfoActivity.this, getString(R.string.net_error),
//                                                    Toast.LENGTH_LONG).show();
//                                            return;
//                                        }
//                                    });
//                                }
//                            }
//                        });
//                    } else {
//                        Toast.makeText(UserInfoActivity.this, getString(R.string.login_thrid_error_failed), Toast.LENGTH_SHORT)
//                                .show();
//                        Log.d("TestData", "发生错误：");
//                    }
                    break;
                case bind_accounts:
                    cancelInProgress();
                    defHandler.removeMessages(time_out);
                    bindArray.clear();
                    bindArray.addAll((Collection<? extends Object>) msg.obj);
                    setBindAccountGone();
                    for (int i = 0; i < bindArray.size(); i++) {
                        JSONObject o = bindArray.getJSONObject(i);
                        if (o.getIntValue("p") == 1) {
                            iv_qq.setImageResource(R.drawable.zhzj_qq);
                            tv_qq.setText(TextUtils.isEmpty(o.getString("name")) ? "" : o.getString("name"));
                        } else if (o.getIntValue("p") == 3) {
                            iv_wechat.setImageResource(R.drawable.zhzj_wechat);
                            tv_wechat.setText(TextUtils.isEmpty(o.getString("name")) ? "" : o.getString("name"));
                        } else if (o.getIntValue("p") == 4) {
                            iv_facebook.setImageResource(R.drawable.zhzj_facebook);
                            tv_facebook.setText(TextUtils.isEmpty(o.getString("name")) ? "" : o.getString("name"));
                        } else if (o.getIntValue("p") == 5) {
                            iv_twitter.setImageResource(R.drawable.zhzj_twitter);
                            tv_twitter.setText(TextUtils.isEmpty(o.getString("name")) ? "" : o.getString("name"));
                        } else if (o.getIntValue("p") == 6) {
                            iv_google.setImageResource(R.drawable.zhzj_google);
                            tv_google.setText(TextUtils.isEmpty(o.getString("name")) ? "" : o.getString("name"));
                        }
                    }
                    break;
            }
            return false;
        }
    };
    private Handler defHandler = new WeakRefHandler(mCallback);

    private void setBindAccountGone() {
        iv_qq.setImageResource(R.drawable.zhzj_qq_m);
        tv_qq.setText(getString(R.string.userinfo_activity_account_not_bind));
        iv_wechat.setImageResource(R.drawable.zhzj_wechat_m);
        tv_wechat.setText(getString(R.string.userinfo_activity_account_not_bind));
        iv_facebook.setImageResource(R.drawable.zhzj_facebook_m);
        tv_facebook.setText(getString(R.string.userinfo_activity_account_not_bind));
        iv_twitter.setImageResource(R.drawable.zhzj_twitter_m);
        tv_twitter.setText(getString(R.string.userinfo_activity_account_not_bind));
        iv_google.setImageResource(R.drawable.zhzj_google_m);
        tv_google.setText(getString(R.string.userinfo_activity_account_not_bind));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhzj_user_info);
        // 启动activity时不自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        initView();
        initUserInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case BIND_OR_UNBIND:
                if (resultCode == RESULT_OK) {
                    String numbers = data.getStringExtra("number");
                    number = numbers;
                    String showNumber = numbers.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
                    iv_tel.setImageResource(TextUtils.isEmpty(numbers) ? R.drawable.zhzj_phone_m : R.drawable.zhzj_phone);
                    tv_tel.setText(TextUtils.isEmpty(numbers) ? getString(R.string.userinfo_activity_account_not_bind) : showNumber);
                }
                break;
            case IMAGE_REQUEST_CODE:
                resizeImage(data.getData());
                break;
            case CAMERA_REQUEST_CODE:
                if (isSdcardExisting()) {
                    resizeImage(getImageUri());
                } else {
                }
                break;

            case RESIZE_REQUEST_CODE:
                if (data != null) {
                    showResizeImage(data);
                }
                break;
            case REQUEST_CODE_IMAGE_CAPTURE:
                if (resultCode == RESULT_CANCELED)
                    return;
                Intent intent1 = new Intent(getApplicationContext(),CropImageActivity.class);
                intent1.setData(Uri.fromFile(mTakePictureFile));
                intent1.putExtra("logo", true);
                intent1.putExtra("path", mTakePictureFile.toString());
                startActivityForResult(intent1, REQUEST_CODE_IMAGE_CROP);
                break;
            case REQUEST_CODE_PICK_IMAGE: {
                try {
                    sourcePath = ImageUtil.retrievePath(this, mSourceIntent, data);
                    if (sourcePath == null) {
                        return;
                    }

//                    Bitmap bitmap = getSmallBitmap(sourcePath, 480, 800);
                    Intent intent = new Intent(this, CropImageActivity.class);
                    intent.setData(data.getData());
                    intent.putExtra("path", sourcePath);
                    intent.putExtra("logo", true);
                    startActivityForResult(intent, REQUEST_CODE_IMAGE_CROP);
//                    userinfo_logo.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case REQUEST_CODE_IMAGE_CROP:
                if (!"".equals(dcsp.getString(Constant.LOGIN_LOGO, ""))) {
                    ImageLoader.getInstance().displayImage(dcsp.getString(Constant.LOGIN_LOGO, ""), userinfo_logo,
                            options_userlogo);
                    Intent intent = new Intent(Actions.UPDATE_USER_LOGO);
                    sendBroadcast(intent);
                }
//                byte[] source = data.getByteArrayExtra(CropImageActivity.CROPBYTE);
//                if (source == null)
//                    return;
//                Bitmap bitmap = BitmapFactory.decodeByteArray(source, 0, source.length);
//                userinfo_logo.setImageBitmap(bitmap);
                break;
        }


        super.onActivityResult(requestCode, resultCode, data);
    }


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

    private boolean isSdcardExisting() {
        final String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public void resizeImage(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, RESIZE_REQUEST_CODE);
    }

    private void showResizeImage(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(photo);
            userinfo_logo.setImageDrawable(drawable);
        }
    }

    private Uri getImageUri() {
        return Uri.fromFile(new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME));
    }

    public void back(View v) {
        finish();
    }

    protected void onDestroy() {
        defHandler.removeCallbacksAndMessages(null);
        defHandler = null;
        super.onDestroy();
    }


    public void logout(View v) {
        new AlertView(getString(R.string.activity_user_info_logout_tip),
                getString(R.string.activity_user_info_logout_mes), getString(R.string.cancel),
                new String[]{getString(R.string.sure)}, null, UserInfoActivity.this, AlertView.Style.Alert,
                new com.smartism.znzk.view.alertview.OnItemClickListener() {

                    @Override
                    public void onItemClick(Object o, int position) {
                        if (position != -1) {
                            logout();
                        }
                    }
                }).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
//        JavaThreadPool.getInstance().excute(new Runnable() {
//            @Override
//            public void run() {
////                String url = "http://charge.smart-ism.com/index.php/home/charge";
//                long uid;
//                String n, s;
//                uid = dcsp.getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
//                String code = dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");
//                n = Util.randomString(12);
//                s = SecurityUtil.createSign(null, uid, code, n);
//                OkHttpClient client = new OkHttpClient();
//                String url = "http://charge.smart-ism.com/index.php/home/charge/predeposit/uid/" + uid + "/n/" + n + "/s/" + s;
//                Log.e("UserINfo:", url);
//                Request request = new Request.Builder().url(url).build();
//                client.newCall(request).enqueue(new Callback() {
//                    @Override
//                    public void onFailure(Call call, IOException e) {
//                        runOnUiThread(new Runnable() {
//                            public void run() {
//                                cancelInProgress();
//                                Toast.makeText(UserInfoActivity.this, "no", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onResponse(Call call, Response response) throws IOException {
//                        final String result = response.body().string();
//                        String banlanceResult = "0";
//                        if (response.isSuccessful()) {
//                            JSONObject object = JSONObject.parseObject(result);
//                            if (!object.isEmpty() && object.containsKey("datas")) {
//                                JSONObject object1 = object.getJSONObject("datas");
//                                banlanceResult = object1.getString("predeposit");
//                            }
//                            final String finalBanlanceResult = banlanceResult;
//                            defHandler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    cancelInProgress();
//                                    banlace.setText(finalBanlanceResult);
//                                }
//                            });
//                        }
//                    }
//
//                });
//
//
//            }
//        });
    }


    private LinearLayout ll_tel, ll_email, ll_qq, ll_wechat, ll_facebook, ll_twitter,ll_google, ll_password;
    private ImageView iv_tel, iv_email, iv_qq, iv_wechat, iv_facebook, iv_twitter,iv_google;
    private TextView tv_tel, tv_email, tv_qq, tv_wechat, tv_facebook, tv_twitter,tv_google;


    private void initView() {
//        ShareSDK.initSDK(this);
        ll_tel = (LinearLayout) findViewById(R.id.ll_tel);
        ll_email = (LinearLayout) findViewById(R.id.ll_email);
        ll_qq = (LinearLayout) findViewById(R.id.ll_qq);
        ll_wechat = (LinearLayout) findViewById(R.id.ll_wechat);
        ll_facebook = (LinearLayout) findViewById(R.id.ll_facebook);
        ll_twitter = (LinearLayout) findViewById(R.id.ll_twitter);
        ll_google = (LinearLayout) findViewById(R.id.ll_google);
        ll_password = (LinearLayout) findViewById(R.id.ll_password);

        if (!MainApplication.app.getAppGlobalConfig().isShowQQ()) {
            ll_qq.setVisibility(View.GONE);
        }
        if (!MainApplication.app.getAppGlobalConfig().isShowWeiXin()) {
            ll_wechat.setVisibility(View.GONE);
        }
        if (!MainApplication.app.getAppGlobalConfig().isShowFaceBook()) {
            ll_facebook.setVisibility(View.GONE);
        }
        if (!MainApplication.app.getAppGlobalConfig().isShowTwitter()) {
            ll_twitter.setVisibility(View.GONE);
        }
        if (!MainApplication.app.getAppGlobalConfig().isShowGoogle()) {
            ll_google.setVisibility(View.GONE);
        }

        iv_tel = (ImageView) findViewById(R.id.iv_tel);
        iv_email = (ImageView) findViewById(R.id.iv_email);
        iv_qq = (ImageView) findViewById(R.id.iv_qq);
        iv_wechat = (ImageView) findViewById(R.id.iv_wechat);
        iv_facebook = (ImageView) findViewById(R.id.iv_facebook);
        iv_twitter = (ImageView) findViewById(R.id.iv_twitter);
        iv_google = (ImageView) findViewById(R.id.iv_google);


        tv_tel = (TextView) findViewById(R.id.tv_tel);
        tv_email = (TextView) findViewById(R.id.tv_email);
        tv_qq = (TextView) findViewById(R.id.tv_qq);
        tv_wechat = (TextView) findViewById(R.id.tv_wechat);
        tv_facebook = (TextView) findViewById(R.id.tv_facebook);
        tv_twitter = (TextView) findViewById(R.id.tv_twitter);
        tv_google = (TextView) findViewById(R.id.tv_google);

        ll_tel.setOnClickListener(this);
        ll_email.setOnClickListener(this);
        ll_qq.setOnClickListener(this);
        ll_wechat.setOnClickListener(this);
        ll_facebook.setOnClickListener(this);
        ll_twitter.setOnClickListener(this);
        ll_google.setOnClickListener(this);
        if (!MainApplication.app.getAppGlobalConfig().isPhone()) {
            ll_tel.setEnabled(false);
            ll_tel.setVisibility(View.GONE);
        }
        if(Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            //uhome设置为不可用
            ll_tel.setEnabled(false);
        }


        tv_userinfo_name = (TextView) findViewById(R.id.tv_userinfo_name);
        rl_change_password = (RelativeLayout) findViewById(R.id.rl_change_password);
        rl_update_nickname = (RelativeLayout) findViewById(R.id.rl_update_nickname);
        rl_user = (RelativeLayout) findViewById(R.id.rl_user);
        ll_charge = (LinearLayout) findViewById(R.id.ll_charge);
        rl_update_nickname.setOnClickListener(this);
        ll_charge.setOnClickListener(this);
        rl_user.setOnClickListener(this);
        userinfo_account = (TextView) findViewById(R.id.userinfo_account_edit);//账号
        rl_change_password.setOnClickListener(this);
        ll_accountinfo = (LinearLayout) findViewById(R.id.ll_accountinfo);


        userinfo_logo = (CircleImageView) findViewById(R.id.userinfo_logo);

        if (Actions.VersionType.CHANNEL_JUJIANG.equals(MainApplication.app.getAppGlobalConfig().getVersion()) && Util.isChinaSimCard(mContext)) {
            //显示支付功能
            ll_accountinfo.setVisibility(View.VISIBLE);
        }

        recharge = (TextView) findViewById(R.id.tv_charge);

    }

    private void initUserInfo() {
        bindArray = new JSONArray();
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new UserInfoLoad());
        loadBindAccount();
    }

    private void loadBindAccount() {
        defHandler.sendEmptyMessageDelayed(time_out, 12 * 1000);
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new LoadBindAccount());
    }

    private EditText etName;
    private InputMethodManager imm;
    private static final int MSG_AUTH_CANCEL = 3;
    private static final int MSG_AUTH_ERROR = 4;
    private static final int MSG_AUTH_COMPLETE = 5;
    private static final int bind_accounts = 11;

    class LoadBindAccount implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            String result = HttpRequestUtils
                    .requestoOkHttpPost( server + "/jdm/s3/u/bftlist", null, UserInfoActivity.this);
            if ("-3".equals(result)) {
                defHandler.removeMessages(time_out);
                defHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(UserInfoActivity.this, getString(R.string.userinfo_activity_usernotexists),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if (!StringUtils.isEmpty(result) && result.startsWith("[")) {
                JSONArray array = null;
                try {
                    array = JSON.parseArray(result);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
                }
                if (array == null) {
                    defHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                        }
                    });
                    return;
                }
                Message m = Message.obtain();
                m.obj = array;
                m.what = bind_accounts;
                defHandler.sendMessage(m);
            } else {
                defHandler.removeMessages(time_out);
                defHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(UserInfoActivity.this, getString(R.string.net_error_requestfailed),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }


    private void removeBind(final long id, final int type) {

        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject o = new JSONObject();
                o.put("id", id);
                String result = HttpRequestUtils
                        .requestoOkHttpPost( server + "/jdm/s3/u/unbft", o, UserInfoActivity.this);
                if ("-3".equals(result)) {
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(UserInfoActivity.this, getString(R.string.userinfo_activity_usernotexists),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                    });
                } else if ("0".equals(result)) {
                    defHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.userinfo_activity_account_remove_bind), Toast.LENGTH_SHORT).show();

//                            if (type == 1) {
//                                iv_qq.setImageResource(R.drawable.zhzj_qq_m);
//                                tv_qq.setText(getString(R.string.userinfo_activity_account_not_bind));
//                            } else if (type == 3) {
//                                iv_wechat.setImageResource(R.drawable.zhzj_wechat_m);
//                                tv_wechat.setText(getString(R.string.userinfo_activity_account_not_bind));
//                            } else if (type == 4) {
//                                iv_facebook.setImageResource(R.drawable.zhzj_facebook_m);
//                                tv_facebook.setText(getString(R.string.userinfo_activity_account_not_bind));
//                            } else if (type == 5) {
//                                iv_twitter.setImageResource(R.drawable.zhzj_twitter_m);
//                                tv_twitter.setText(getString(R.string.userinfo_activity_account_not_bind));
//                            }
                            loadBindAccount();
                        }
                    });
                    return;
                }
            }
        });
    }


    private void bindTel() {

        if (TextUtils.isEmpty(number)) {
            Intent intent = new Intent();
            intent.setClass(this, BindPhoneActivity.class);
            intent.putExtra("bindType", ADD_UPDATE_BINDPHONE);
            intent.putExtra("number", number);
            intent.putExtra("isShowPassword", TextUtils.isEmpty(mEmail) ? true : false);
            startActivityForResult(intent, BIND_OR_UNBIND);
        } else {
            if(!Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                    && !Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.operator))
                        .setItems(new String[]{getString(R.string.userinfo_activity_update_phone), getString(R.string.userinfo_activity_remove_phone)}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.e("bind_account", which + "");
                                Intent intent = new Intent();
                                intent.setClass(UserInfoActivity.this, BindPhoneActivity.class);
                                intent.putExtra("number", number);
                                if (which == 0) {
                                    intent.putExtra("bindType", ADD_UPDATE_BINDPHONE);
                                } else {
                                    intent.putExtra("bindType", UNBIND_PHONE);
                                }
                                startActivityForResult(intent, BIND_OR_UNBIND);
                            }
                        }).show();
            }
        }

    }

    private void isSureUnbind(final int type) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.prompt))
                .setMessage(getString(R.string.userinfo_activity_remove_bind))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (Object o : bindArray) {
                            JSONObject o1 = (JSONObject) o;
                            if (o1.getIntValue("p") == type) {
                                removeBind(o1.getLongValue("id"), type);
                            }
                        }
                    }
                })
                .show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_tel:
                bindTel();
                break;
            case R.id.ll_email:
                break;
            case R.id.ll_qq:
                if (tv_qq.getText().toString().equals(getString(R.string.userinfo_activity_account_not_bind))) {
//                    Platform qq = getPlatform(QQ.NAME);
//                    authorize(qq);
                } else {
                    isSureUnbind(1);
                }
                break;
            case R.id.ll_wechat:
                if (tv_wechat.getText().toString().equals(getString(R.string.userinfo_activity_account_not_bind))) {
//                    Platform wechat = getPlatform(Wechat.NAME);
//                    authorize(wechat);
                } else {
                    isSureUnbind(3);
                }
                break;
            case R.id.ll_facebook:
                if (tv_facebook.getText().toString().equals(getString(R.string.userinfo_activity_account_not_bind))) {
//                    Platform facebook = getPlatform(Facebook.NAME);
//                    authorize(facebook);
                } else {
                    isSureUnbind(4);
                }
                break;
            case R.id.ll_twitter:
                if (tv_twitter.getText().toString().equals(getString(R.string.userinfo_activity_account_not_bind))) {
//                    Platform twitter = getPlatform(Twitter.NAME);
//                    authorize(twitter);
                } else {
                    isSureUnbind(5);
                }
                break;
            case R.id.ll_google:
                if (tv_google.getText().toString().equals(getString(R.string.userinfo_activity_account_not_bind))) {
//                    Platform google = getPlatform(GooglePlus.NAME);
//                    authorize(google);
                } else {
                    isSureUnbind(6);
                }
                break;
            case R.id.rl_change_password:
                startActivity(new Intent(UserInfoActivity.this, ChangePasswordActivity.class));
                break;
            case R.id.ll_charge:
                break;
            case R.id.rl_user:
                showOptions();
                break;
            case R.id.tv_expenses_record:
                break;
            case R.id.rl_update_nickname:
                imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //拓展窗口
                mAlertView = new AlertView(getString(R.string.userinfo_activity_nicheng), null, getString(R.string.cancel), null, new String[]{getString(R.string.compele)}, this, AlertView.Style.Alert, this);
//                mAlertViewExt = new AlertView(getString(R.string.activity_add_zhuji_idzhu_msg), null, getString(R.string.cancel), null, new String[]{getString(R.string.compele)}, this, Style.Alert, this);
                ViewGroup extView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_addzhuji_alertext_form, null);
                etName = (EditText) extView.findViewById(R.id.etName);
                etName.setHint(getString(R.string.userinfo_activity_nicheng));
                etName.setText(dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_APPNAME, ""));
                etName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean focus) {
                        //输入框出来则往上移动
                        boolean isOpen = imm.isActive();
                        mAlertView.setMarginBottom(isOpen && focus ? 120 : 0);
                    }
                });
                mAlertView.addExtView(extView);
                mAlertView.show();
                break;

        }
    }

    @Override
    public void onItemClick(Object o, int position) {
        //关闭软键盘
        imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
        //恢复位置
        mAlertView.setMarginBottom(0);

        if (o == mAlertView && position != AlertView.CANCELPOSITION) {
            String name = etName.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(this, getString(R.string.register_tip_nicheng_empty), Toast.LENGTH_SHORT).show();
            } else {
                showInProgress(getString(R.string.operationing), false, true);
                JavaThreadPool.getInstance().excute(new UpdateNickName(name));
            }
            if (mAlertView != null && mAlertView.isShowing()) {
                mAlertView.dismissImmediately();
            }
            return;
        } else {
            if (mAlertView != null && mAlertView.isShowing()) {
                mAlertView.dismissImmediately();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mAlertView != null && mAlertView.isShowing()) {
                mAlertView.dismiss();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showOptions() {

        mAlertView =
                new AlertView(getString(R.string.userinfo_alert_upload), null, getString(R.string.cancel), null,
                        new String[]{getString(R.string.activity_beijingmy_makingpictures), getString(R.string.userinfo_alert_photo)},
                        this, AlertView.Style.ActionSheet, new OnItemClickListener() {
                    public void onItemClick(Object o, int position) {
//                        closeKeyboard();
                        if (position == 0) {
                            if (PermissionUtil.isOverMarshmallow()) {
                                if (!PermissionUtil.isPermissionValid(UserInfoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                                    Toast.makeText(UserInfoActivity.this,
//                                            "请去\"设置\"中开启本应用的图片媒体访问权限",
//                                            Toast.LENGTH_SHORT).show();
                                    requestPermissionsAndroidM();
                                    return;
                                }
                                if (!PermissionUtil.isPermissionValid(UserInfoActivity.this, Manifest.permission.CAMERA)) {
//                                    Toast.makeText(UserInfoActivity.this,
//                                            "请去\"设置\"中开启本应用的相机权限",
//                                            Toast.LENGTH_SHORT).show();
                                    requestPermissionsAndroidM();
                                    return;
                                }
                            }
                            try {
                                mSourceIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//调用系统拍摄照片
                                //先清除以前保存过的拍照数据
                                File frontFile = new File(ImageUtil.getDirPath());
                                clearTakePictueFile(frontFile);//清空里面的文件
                                mTakePictureFile = new File(ImageUtil.getNewPhotoPath()); //保存图片的路径
                                Uri uri = null ;
                                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
                                    //大于7.0
                                    uri  = FileProvider.getUriForFile(getApplicationContext(),getPackageName()+".FileProvider",mTakePictureFile);
                                    mSourceIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                }else{
                                    uri = Uri.fromFile(mTakePictureFile);
                                }
                                mSourceIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                                startActivityForResult(mSourceIntent, REQUEST_CODE_IMAGE_CAPTURE);
                            } catch (Exception e) {
                                e.printStackTrace();
//                                Toast.makeText(UserInfoActivity.this,
//                                        "请去\"设置\"中开启本应用的相机和图片媒体访问权限",
//                                        Toast.LENGTH_SHORT).show();
                            }
                        } else if (position == 1) {
                            if (PermissionUtil.isOverMarshmallow()) {
                                if (!PermissionUtil.isPermissionValid(UserInfoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                                    Toast.makeText(UserInfoActivity.this,
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
//                                Toast.makeText(UserInfoActivity.this,
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

    //清空拍照目录下保存的文件
    private void clearTakePictueFile(File file){
        File[] files = file.listFiles() ;
        for(File temp : files){
            if(temp.isDirectory()){
                clearTakePictueFile(temp);
            }else{
                temp.delete() ;
            }
        }
    }


    private class DialogOnCancelListener implements DialogInterface.OnCancelListener {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
//            restoreUploadMsg();
        }
    }


    private void requestPermissionsAndroidM() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> needPermissionList = new ArrayList<>();
            needPermissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            needPermissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            needPermissionList.add(Manifest.permission.CAMERA);
            PermissionUtil.requestPermissions(UserInfoActivity.this, P_CODE_PERMISSIONS, needPermissionList);
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
        } else {
            return;
        }
    }

    public class UpdateNickName implements Runnable {
        private String name;

        public UpdateNickName(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            showInProgress(getString(R.string.userinfo_activity_updating), false, true);
            final String p = "";
            final String n = name;
            final String m = number;
            String email = mEmail;
            if (email.contains("@")) {
                //账号为邮箱
                String acc = email.substring(0, email.lastIndexOf("@"));
                String yu = email.substring(email.lastIndexOf("@"));
                if (yu != null) {
                    yu = yu.toLowerCase();
                    yu = yu.replaceAll("。", ".");
                }
                email = acc + yu;
            }
            final String e = email;

//        final long id = dcsp.getLong(Constant.LOGIN_APPID, 0);
            JavaThreadPool.getInstance().excute(new Runnable() {

                @Override
                public void run() {
                    String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                    JSONObject o = new JSONObject();
                    o.put("name", n);
                    o.put("logo", "");
                    o.put("mobile", m);
                    o.put("email", e);
                    String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/u/u", o, UserInfoActivity.this);
//                String
//                    result = HttpRequestUtils.requestHttpServer(
//                             server + "/jdm/service/updateU?name=" + URLEncoder.encode(n, "UTF-8")
//                                    + "&appid=" + id + "&password="
//                                    + ("".equals(p) ? "" : URLEncoder.encode(p, "UTF-8")) + "&mobile="
//                                    + URLEncoder.encode(m, "UTF-8") + "&email=" + URLEncoder.encode(e, "UTF-8")
//                                    + "&code=" + URLEncoder.encode(dcsp.getString(Constant.LOGIN_CODE, ""), "UTF-8"),
//                            UserInfoActivity.this, defHandler);
                    // -1账号不能为空，-2昵称不能为空，-3密码不能为空，-4邮箱不能为空，-5手机不能为空，-6账号不符合规则，-7账号不能输入手机号
                    if ("0".equals(result)) {
                        defHandler.sendEmptyMessage(10);
                    } else if ("-3".equals(result)) {
                        defHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                cancelInProgress();
                                Toast.makeText(UserInfoActivity.this, getString(R.string.register_tip_nicheng_empty),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if ("-4".equals(result)) {
                        defHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                cancelInProgress();
                                Toast.makeText(UserInfoActivity.this, getString(R.string.register_tip_email_empty),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if ("-5".equals(result)) {
                        defHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                cancelInProgress();
                                Toast.makeText(UserInfoActivity.this, getString(R.string.register_tip_phone_empty),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if ("-8".equals(result)) {
                        defHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                cancelInProgress();
                                Toast.makeText(UserInfoActivity.this, getString(R.string.register_tip_account_notemail),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if ("-9".equals(result)) {
                        defHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                cancelInProgress();
                                Toast.makeText(UserInfoActivity.this, getString(R.string.register_tip_phone_error),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if ("-10".equals(result)) {
                        defHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                cancelInProgress();
                                Toast.makeText(UserInfoActivity.this, getString(R.string.register_tip_email_isin),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if ("-11".equals(result)) {
                        defHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                cancelInProgress();
                                Toast.makeText(UserInfoActivity.this, getString(R.string.register_tip_phone_isin),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
        }
    }


    class UserInfoLoad implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
//            JSONObject pJsonObject = new JSONObject();
//            pJsonObject.put("uid", dcsp.getLong(Constant.LOGIN_APPID, 0));
//            pJsonObject.put("code", dcsp.getString(Constant.LOGIN_CODE, ""));
            String result = HttpRequestUtils
                    .requestoOkHttpPost( server + "/jdm/s3/u/i", null, UserInfoActivity.this);
//            String result = HttpRequestUtils
//                    .requestHttpServer(
//                             server + "/jdm/service/users?v="
//                                    + URLEncoder
//                                    .encode(SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP)),
//                            UserInfoActivity.this, defHandler);
            if ("-3".equals(result)) {
                defHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(UserInfoActivity.this, getString(R.string.userinfo_activity_usernotexists),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if (!StringUtils.isEmpty(result) && result.length() > 4) {
                JSONObject os = null;
                try {
                    os = JSON.parseObject(result);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
                }
                if (os == null) {
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(UserInfoActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                if (defHandler == null)
                    defHandler = new Handler(getMainLooper());
                Message m = defHandler.obtainMessage(1);
                m.obj = os;
                defHandler.sendMessage(m);
            } else {
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(UserInfoActivity.this, getString(R.string.net_error_requestfailed), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    //点击空白区域隐藏软键盘
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = getCurrentFocus();
        if ((v != null && (v instanceof EditText))) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        return super.onTouchEvent(event);
    }


}
