package com.smartism.znzk.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.ListCtrUserDeviceRelationsQuery;
import com.amazonaws.amplify.generated.graphql.ListCtrUserGroupsQuery;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.global.AccountPersist;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import me.tatarka.support.job.JobScheduler;

public class Util {

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static String getUrl(Context context,String houzhui){
        String temp   = DataCenterSharedPreferences
                .getInstance(context,DataCenterSharedPreferences.Constant.CONFIG)
                .getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS,"")+houzhui;
        return temp;
    }

    public static void initImageLoader(Context context) {
        // DisplayImageOptions defaultOptions = new
        // DisplayImageOptions.Builder()
        // .showImageOnLoading(R.drawable.default_face)
        // .showImageForEmptyUri(R.drawable.default_face)
        // .showImageOnFail(R.drawable.default_face).cacheInMemory(true)
        // .considerExifParams(true)
        // .displayer(new FadeInBitmapDisplayer(300, true, true, true))
        // .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
        // .bitmapConfig(Bitmap.Config.RGB_565).build();
        // ImageLoaderConfiguration.Builder builder = new
        // ImageLoaderConfiguration.Builder(
        // context).defaultDisplayImageOptions(defaultOptions)
        // .memoryCache(new WeakMemoryCache());
        // ImageLoaderConfiguration config = builder.build();
        // ImageLoader.getInstance().init(config);
    }

    @SuppressWarnings("deprecation")
    public static ArrayList<String> getGalleryPhotos(Activity act) {
        ArrayList<String> galleryList = new ArrayList<String>();
        try {
            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
            final String orderBy = MediaStore.Images.Media._ID;
            Cursor imagecursor = act.managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null,
                    orderBy);
            if (imagecursor != null && imagecursor.getCount() > 0) {
                while (imagecursor.moveToNext()) {
                    String item = new String();
                    int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    item = imagecursor.getString(dataColumnIndex);
                    galleryList.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collections.reverse(galleryList);
        return galleryList;
    }

    public static Bitmap convertViewToBitmap(View view) {
        Bitmap bitmap = null;
        try {
            int width = view.getWidth();
            int height = view.getHeight();
            if (width != 0 && height != 0) {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                view.layout(0, 0, width, height);
                view.setBackgroundColor(Color.WHITE);
                view.draw(canvas);
            }
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }
        return bitmap;

    }



    public static String convertBitmapToBase64String(CompressFormat format, Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(format, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    public static String convertBitmapToBase64String(CompressFormat format, InputStream stream) throws Exception {
        return Base64.encodeToString(IOUtils.toByteArray(stream), Base64.DEFAULT);
    }

    public static Bitmap convertDrawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static boolean saveImageToGallery(Context context, Bitmap bmp, boolean isPng) {
        if (bmp == null) {
            return false;
        }
        File appDir = new File(Environment.getExternalStorageDirectory(), context.getString(R.string.app_name));
        if (!appDir.exists()) {
            if (!appDir.mkdir()) {
                return false;
            }
        }
        String fileName;
        if (isPng) {
            fileName = System.currentTimeMillis() + ".png";
        } else {
            fileName = System.currentTimeMillis() + ".jpg";
        }
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            if (isPng) {
                bmp.compress(CompressFormat.PNG, 100, fos);
            } else {
                bmp.compress(CompressFormat.JPEG, 100, fos);
            }
            bmp.recycle();
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(appDir)));
        return true;
    }

    public static void startPhotoZoom(ActivityParentActivity context, Uri input, Uri output, int w, int h) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(input, "image/*");
        intent.putExtra("crop", true); // 显示view可剪裁
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1); // X Y的宽高比
        intent.putExtra("outputX", 150); // X 的宽度
        intent.putExtra("outputY", 150); // Y的宽高度
        intent.putExtra("outputFormat", CompressFormat.JPEG.toString());
        intent.putExtra("scale", true);// 黑边
        intent.putExtra("scaleUpIfNeeded", true);// 黑边
        // intent.putExtra("return-data", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, output); // 这种方式用于存放大图在uri中，而不需要返回bitmap对象
        intent.putExtra("noFaceDetection", true);
        context.startActivityForResult(intent, 400);
    }

    public static void t(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 判断某一个app是否安装
     *
     * @param context
     * @param activitypath 需要打开的app的首页activity全路径
     * @return
     */
    public static boolean appIsInstalled(Context context, String activitypath) {
        final PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(activitypath.substring(0, activitypath.lastIndexOf("/")), 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * dp转px
     *
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, r.getDisplayMetrics());
        return (int) px;
    }

    /**
     * 获取通知栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Activity context) {
        Rect rect = new Rect();
        context.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    //设置状态栏的颜色
    public static void setStatusBarColor(@NonNull Activity activity , int color){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            //大于Android5.0
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().setStatusBarColor(color);
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                //系统的状态栏文字与图标的颜色默认是白色，放置我们要设置的颜色与白色接近
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            //大于Android4.4
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            decorView.addView(createStatusBarView(activity,color),0);
            FrameLayout contentView = activity.findViewById(android.R.id.content);
            View firstView  = contentView.getChildAt(0);
            if(firstView==null){
                throw new IllegalStateException("必须在setContentView方法之后调用");
            }
            firstView.setFitsSystemWindows(true);
        }
    }

    //获取状态栏高度
    public static int getStatusBarHeight(@NonNull Context context){
        int id = context.getResources().getIdentifier("status_bar_height","dimen","android");
        return context.getResources().getDimensionPixelSize(id);
    }
    //创建一个自定义颜色的状态栏
    public static View createStatusBarView(@NonNull Context context,int color){
        View view = new View(context);
        view.setBackgroundDrawable(new ColorDrawable(color));
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getStatusBarHeight(context));
        view.setLayoutParams(lp);
        return view ;
    }

    /**
     * 安装一个app
     *
     * @param context
     * @return
     */
    public static void install(Context context, Uri uri) {
        Uri tempUri = null ;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            tempUri = FileProvider.getUriForFile(context,MainApplication.app.getPackageName()+".FileProvider",new File(uri.getPath()));
        }else{
            tempUri = uri ;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(tempUri, "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    /**
     * 判断是否有主机
     *
     * @return
     */
    public static boolean isHaveZhuji(Context context) {
        Cursor cursor = DatabaseOperator.getInstance(context).getWritableDatabase()
                .rawQuery("select count(1) as coun from ZHUJI_STATUSINFO", new String[]{});
        boolean isHave = false;
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(cursor.getColumnIndex("coun")) > 0) {
                    isHave = true;
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return isHave;
    }

    /**
     * 判断是否有非摄像头以外的设备
     *
     * @return
     */
    public static boolean isHaveDevices(Context context) {
        Cursor cursor = DatabaseOperator.getInstance(context).getWritableDatabase()
                .rawQuery("select count(1) as coun from DEVICE_STATUSINFO where device_controltype <> ?", new String[]{"shexiangtou"});
        boolean isHave = false;
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(cursor.getColumnIndex("coun")) > 0) {
                    isHave = true;
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return isHave;
    }

    // 生成QR图
    public static Bitmap createImage(String text, int QR_WIDTH, int QR_HEIGHT) {
        try {
            // 需要引入core包
            QRCodeWriter writer = new QRCodeWriter();

            if (text == null || "".equals(text) || text.length() < 1) {
                return null;
            }

            // 把输入的文本转为二维码
            BitMatrix martix = writer.encode(text, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);

            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix bitMatrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            for (int y = 0; y < QR_HEIGHT; y++) {
                for (int x = 0; x < QR_WIDTH; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    } else {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                    }

                }
            }

            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);

            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            return bitmap;
        } catch (WriterException e) {
            Log.e("jdm", "生成二维码异常", e);
        }
        return null;
    }

    /**
     * 登出调用
     */
    public static void clearLoginInfo(Context context, DataCenterSharedPreferences dcsp) {
        dcsp.remove(Constant.IS_LOGIN).remove(Constant.LOGIN_ACCOUNT).remove(Constant.LOGIN_PWD).remove(Constant.LOGIN_CODE)
                .remove(Constant.LOGIN_APPID).remove(Constant.LOGIN_APPNAME).remove(Constant.LOGIN_LOGO).commit();
        AccountPersist.getInstance().delActiveAccount(context); //清空摄像头账户信息
        //清除本地缓存数据
        DatabaseOperator.getInstance(context).clearAllDbData();
        JobScheduler.getInstance(context).cancelAll();//关掉定时执行任务
    }

    /**
     * HUAWEIPUSH 注销Token
     * @param context
     * @return
     */
//    private void deleteToken() {
//
//        if(!client.isConnected()) {
//
//            Log.i("HuaweiPush", "注销token失败，原因：HuaweiApiClient未连接");
//
//            client.connect();
//
//            return;
//
//        }
//
//        //需要在子线程中执行删除token操作
//
//        new Thread() {
//
//            @Override
//
//            public void run() {
//
//                //调用删除token需要传入通过getToken接口获取到token，并且需要对token进行非空判断
//
//                Log.i("HuaweiPush", "删除Token：" + token);
//
//                if (!TextUtils.isEmpty(token)){
//
//                    try {
//
//                        HuaweiPush.HuaweiPushApi.deleteToken(client, token);
//
//                    } catch (PushException e) {
//
//                        Log.i("HuaweiPush", "删除Token失败:" + e.getMessage());
//
//                    }
//
//                }
//
//            }
//
//
//        }.start();
//
//    }

    public static ShapeDrawable createReadBgShapeDrawable(Context context) {

        int r = dip2px(context, 5);
        float[] outerR = new float[]{r, r, r, r, r, r, r, r};

        RoundRectShape rr = new RoundRectShape(outerR, null, null);
        ShapeDrawable drawable = new ShapeDrawable(rr);
        drawable.getPaint().setColor(Color.parseColor("#CCFF0000"));

        return drawable;

    }

    /**
     * 获取wifiIP地址
     *
     * @param mContext
     * @return
     */
    public static String getWIFILocalIpAdress(Context mContext) {

        // 获取wifi服务
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        // 判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = formatIpAddress(ipAddress);
        return ip;
    }

    private static String formatIpAddress(int ipAdress) {

        return (ipAdress & 0xFF) + "." + ((ipAdress >> 8) & 0xFF) + "." + ((ipAdress >> 16) & 0xFF) + "."
                + (ipAdress >> 24 & 0xFF);
    }

    public static long lastClickTime;

    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    private static long judgeFastClickLastTime ;
    public synchronized static boolean judgeFastClick(long gapTime){
        long time = System.currentTimeMillis();
        if (time - judgeFastClickLastTime < gapTime) {
            return true;
        }
        judgeFastClickLastTime = time;
        return false;
    }

    /**
     * 获取单个文件的MD5值！
     *
     * @param file
     * @return
     */

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16).toUpperCase();
    }

    /**
     * 返回指定长度的随机字符串 只包含数字和字母
     *
     * @param length
     * @return
     */
    public static final String randomString(int length) {
        if (length < 1) {
            return null;
        }
        Random randGen = new Random();
        char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz"
                + "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
        char[] randBuffer = new char[length];
        for (int i = 0; i < randBuffer.length; i++) {
            randBuffer[i] = numbersAndLetters[randGen.nextInt(71)];
        }
        return new String(randBuffer);
    }

    public static boolean checkPhoneNumber(String phoneNumber) {
        Pattern pattern = Pattern.compile("^1[0-9]{10}$");
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

//
//    /**
//     * 验证手机格式
//     */
//    public static boolean isMobileNO(String mobiles) {
//    /*
//    移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
//    联通：130、131、132、152、155、156、185、186
//    电信：133、153、180、189、（1349卫通）
//    总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
//
//    ------------------------------------------------
//    13(老)号段：130、131、132、133、134、135、136、137、138、139
//    14(新)号段：145、147
//    15(新)号段：150、151、152、153、154、155、156、157、158、159
//    17(新)号段：170、171、173、175、176、177、178
//    18(3G)号段：180、181、182、183、184、185、186、187、188、189
//    */
//        String telRegex = "[1][34578]\\d{9}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、4、5、7、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
//        if (TextUtils.isEmpty(mobiles)) return false;
//        else return mobiles.matches(telRegex);
//    }

    public static boolean isMobileNO(String str)
            throws PatternSyntaxException {
        String regExp = "^((13[0-9])|(15[^4])|(18[0-9])|(17[0-8])|147|145|199)\\d{8}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 摄氏度转华氏度
     *
     * @param sheshidu 摄氏度
     * @return 华氏度
     */
    public static double CToF(double sheshidu) {
        double huashidu = 0.0;
        huashidu = 1.8 * sheshidu + 32;
        return huashidu;

    }


    /**
     * @param kg
     * @return 公斤转磅
     */
    public static double kgTolb(double kg) {
        DecimalFormat format = new DecimalFormat("0.0");
        format.setRoundingMode(RoundingMode.HALF_UP);
        double lb = 0.0;
        lb = Double.parseDouble(format.format(Math.round(2.2046226 * kg)));
        return lb;

    }

    /**
     * 公斤转磅
     *
     * @param kg
     * @return
     */
    public static String kgTolbs(double kg) {
        DecimalFormat format = new DecimalFormat("0.0");
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(2.2046226 * kg);

    }

    /**
     * 磅转公斤
     *
     * @param lb
     * @return
     */
    public static String lbToKg(double lb) {
        DecimalFormat format = new DecimalFormat("0.0");
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(0.4535924 * lb);

    }

    /**
     * 磅转公斤
     *
     * @param lb
     * @return
     */
    public static double lbToKgs(double lb) {
        DecimalFormat format = new DecimalFormat("0.0");
        format.setRoundingMode(RoundingMode.HALF_UP);
        double kg = 0.0;
        kg = Double.parseDouble(format.format(Math.round(0.4535924 * lb)));
        return kg;

    }

    private static String getSimOperator(Context c) {
        TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            return tm.getSimOperator();
        } catch (Exception e) {

        }
        return null;
    }


    /**
     * 因为发现像华为Y300，联想双卡的手机，会返回 "null" "null,null" 的字符串
     */
    private static boolean isOperatorEmpty(String operator) {
        if (operator == null) {
            return true;
        }


        if (operator.equals("") || operator.toLowerCase(Locale.US).contains("null")) {
            return true;
        }


        return false;
    }


    /**
     * 判断是否是国内的 SIM 卡，优先判断注册时的mcc
     */
    public static boolean isChinaSimCard(Context c) {
        String mcc = getSimOperator(c);
        if (isOperatorEmpty(mcc)) {
            return false;
        } else {
            return mcc.startsWith("460");
        }
    }

    public static void saveYKCodeToFile(String code, String id) {
        try {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                String path = Environment.getExternalStorageDirectory()
                        .getPath() + "/" + MainApplication.app.getPackageName() + "/infrared/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                IOUtils.write(code, new FileOutputStream(path + SecurityUtil.MD5(id) + ".txt"), "UTF-8");
            }
        } catch (Exception ex) {
            Log.e("UTIL", "saveYKCodeToFile: ", ex);
        }
    }

    public static String readYKCodeFromFile(String id) {
        try {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                String path = Environment.getExternalStorageDirectory()
                        .getPath() + "/" + MainApplication.app.getPackageName() + "/infrared/";
                File codeFle = new File(path + SecurityUtil.MD5(id) + ".txt");
                if (codeFle.exists()) {
                    return FileUtils.readFileToString(codeFle, "UTF-8");
                }
            }
        } catch (Exception ex) {
            Log.e("UTIL", "readYKCodeFromFile: error", ex);
        }
        return null;
    }

    public static File readYKCodeFile(String id) {
        try {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                String path = Environment.getExternalStorageDirectory()
                        .getPath() + "/" + MainApplication.app.getPackageName() + "/infrared/";
                File codeFile = new File(path + SecurityUtil.MD5(id) + ".txt");
                if (codeFile.exists()) {
                    return codeFile;
                }
            }
        } catch (Exception ex) {
            Log.e("UTIL", "readYKCodeFile: error", ex);
        }
        return null;
    }


    //获得当天0点时间
    public static int getTimesmorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (int) (cal.getTimeInMillis() / 1000);
    }

    //获得当天24点时间
    public static int getTimesnight() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 24);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (int) (cal.getTimeInMillis() / 1000);
    }

    /**
     * 获取联系人
     */
    public void getContacts(Context context) {
        Uri uri = Uri.parse("content://com.android.contacts/contacts"); // 访问所有联系人
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{"_id"}, null, null, null);
        while (cursor.moveToNext()) {
            int contactsId = cursor.getInt(0);
            StringBuilder sb = new StringBuilder("contactsId=");
            sb.append(contactsId);
            uri = Uri.parse("content://com.android.contacts/contacts/" + contactsId + "/data"); //某个联系人下面的所有数据
            Cursor dataCursor = resolver.query(uri, new String[]{"mimetype", "data1", "data2"}, null, null, null);
            while (dataCursor.moveToNext()) {
                String data = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                String type = dataCursor.getString(dataCursor.getColumnIndex("mimetype"));
                if ("vnd.android.cursor.item/name".equals(type)) {    // 如果他的mimetype类型是name
                    sb.append(", name=" + data);
                } else if ("vnd.android.cursor.item/email_v2".equals(type)) { // 如果他的mimetype类型是email
                    sb.append(", email=" + data);
                } else if ("vnd.android.cursor.item/phone_v2".equals(type)) { // 如果他的mimetype类型是phone
                    sb.append(", phone=" + data);
                }
            }
        }
    }

    /**
     * 添加联系人 直接操作通讯录的方式
     * 数据一个表一个表的添加，每次都调用insert方法
     */
    public static void addContacts(Context context) {
        /* 往 raw_contacts 中添加数据，并获取添加的id号*/
//        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();

        // 向RawContacts.CONTENT_URI空值插入，
        // 先获取Android系统返回的rawContactId
        // 后面要基于此id插入值
        Uri rawContactUri = resolver.insert(ContactsContract.RawContacts.CONTENT_URI, values);
        if (rawContactUri == null)
            return;
        long contactId = ContentUris.parseId(rawContactUri);
//        long contactId = ContentUris.parseId(resolver.insert(uri, values));

        /* 往 data 中添加数据（要根据前面获取的id号） */
        // 添加姓名
//        uri = Uri.parse("content://com.android.contacts/data");
        values.put("raw_contact_id", contactId);
        values.put("mimetype", "vnd.android.cursor.item/name");
        if (Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
            values.put("data2", "安霸保全");
        }else if(Actions.VersionType.CHANNEL_DITAIXING.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            //迪泰兴
            values.put("data2",context.getResources().getString(R.string.ditaixing_alarm_center));
        }else {
            values.put("data2",context.getResources().getString(R.string.alarm_center));
        }
        resolver.insert(Data.CONTENT_URI, values);
        //添加电话
        for(int i=0;i<numbers.size();i++){
            values.clear();
            values.put("raw_contact_id", contactId);
            values.put("mimetype", "vnd.android.cursor.item/phone_v2");
            values.put("data2", "2");
            values.put("data1", numbers.get(i));
            resolver.insert(Data.CONTENT_URI, values);
        }
    }

    /**
     * 添加联系人  数据写入通讯录插入页面，由用户主动保存
     */
    public static void addContactsByContract(Context context) {
        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);

        if (Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion())
            ||Actions.VersionType.CHANNEL_DITAIXING.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
            intent.putExtra(ContactsContract.Intents.Insert.NAME, context.getString(R.string.app_name));
        }else {
            intent.putExtra(ContactsContract.Intents.Insert.NAME, context.getString(R.string.alarm_center));
        }

        ArrayList<ContentValues> contactData = new ArrayList<>();

        //添加电话
        for(int i=0;i<numbers.size();i++){
            ContentValues phoneRow = new ContentValues();
            phoneRow.put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
            );
            phoneRow.put(ContactsContract.CommonDataKinds.Phone.NUMBER, numbers.get(i));
            contactData.add(phoneRow);
        }
        intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, contactData);
        context.startActivity(intent);
    }

    //保存要写入的报警号码，需要增加新的号码，直接添加即可
    static ArrayList<String> numbers = new ArrayList<>();
    static {
        //不要有空格和“-”，因为判断逻辑会去掉空格和“-”符号
        numbers.add("+1(941)6666980");
        numbers.add("+601117226662");
        numbers.add("+447520632566");
        numbers.add("02131354074");
        numbers.add("051068584606");
    }

    //删除设备上的报警中心号码
    /*
     * author mz
     * */
   public static  void deletePreviousContacts(@NonNull Context context){
        ArrayList<String> contactName = new ArrayList<>();
        int rawCount = 0;//受影响的行数
       //保存联系人名字
        if(Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            contactName.add("安霸保全");
        }else if(Actions.VersionType.CHANNEL_DITAIXING.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            //迪泰兴
            contactName.add(context.getResources().getString(R.string.ditaixing_alarm_center));
        }else{
            contactName.add(context.getResources().getString(R.string.alarm_center));
        }

        ContentResolver resolver = context.getContentResolver();
        //查询出来的联系人是名为报警中心或者安霸保全的联系人
        Cursor raw_cursor = resolver.query(ContactsContract.RawContacts.CONTENT_URI,null,ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY+"=?",
                new String[]{contactName.get(0)},null);
        if(raw_cursor!=null){
            //说明先前存在报警中心或者安霸保全联系人
            while(raw_cursor.moveToNext()){
                if(raw_cursor.getInt(raw_cursor.getColumnIndex(ContactsContract.RawContacts.DELETED))==1){
                    //表示此条数据已被用户删除，跳过
                    continue;
                }
                //删除之前得判断下是不是我们自己的报警号码
                String id = String.valueOf(raw_cursor.getInt(raw_cursor.getColumnIndex(ContactsContract.RawContacts._ID)));
                Cursor numberCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,Data.RAW_CONTACT_ID+"=?",
                        new String[]{id},null);
                if(numberCursor!=null){
                    while(numberCursor.moveToNext()){
                        String number = numberCursor.getString(numberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1));
                        //将空格和-处理成空字符
                        number = DecimalUtils.handleMatcheString(number,"[\\s-]");//这里说明，电话号码不要有空格和“-”，否则一定会判断不存在
                        if(numbers.contains(number)){
                            //删除掉这个号码
                            int temp = resolver.delete(ContactsContract.RawContacts.CONTENT_URI,ContactsContract.RawContacts._ID+"=?",new String[]{id});
                            break;
                        }
                    }
                    numberCursor.close();
                }

            }
            raw_cursor.close();
        }

            //删除之后添加
            addContacts(context);
   }

   //如果号码不全就返回false，表示要更新，如果返回为true,表示报警号码以是最新
    /*
    * author mz
    * */
   public static boolean hasAlarmNumber(Context context){
       ArrayList<String> contactName = new ArrayList<>();
       ArrayList<String> tempNumber = new ArrayList<>();
       //保存联系人名字
       if(Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
           contactName.add("安霸保全");
       }else if(Actions.VersionType.CHANNEL_DITAIXING.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
           //迪泰兴
           contactName.add(context.getResources().getString(R.string.ditaixing_alarm_center));
       }else{
           contactName.add(context.getResources().getString(R.string.alarm_center));
       }

       ContentResolver resolver = context.getContentResolver();
       //查询出来的联系人是名为报警中心或者安霸保全的联系人
       Cursor raw_cursor = resolver.query(ContactsContract.RawContacts.CONTENT_URI,null,ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY+"=?",
               new String[]{contactName.get(0)},null);
       //说明先前存在报警中心或者安霸保全联系人
       if(raw_cursor==null){
           return false ;
       }
       while(raw_cursor.moveToNext()){
           if(raw_cursor.getInt(raw_cursor.getColumnIndex(ContactsContract.RawContacts.DELETED))==1){
               //表示此条数据已被用户删除，跳过
               continue;
           }
           //得判断下是不是我们自己的报警号码，万一用户自己也有一个叫报警号码的用户，删错了怎么办？
           String id = String.valueOf(raw_cursor.getInt(raw_cursor.getColumnIndex(ContactsContract.RawContacts._ID)));
           Cursor numberCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,Data.RAW_CONTACT_ID+"=?",
                   new String[]{id},null);
           if(numberCursor!=null){
               while(numberCursor.moveToNext()){
                   String number = numberCursor.getString(numberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1));
                   //将空格和-处理成空字符
                   number=DecimalUtils.handleMatcheString(number,"[\\s-]");
                  tempNumber.add(number);
               }
               numberCursor.close();
               if(tempNumber.containsAll(numbers)){
                   return true ;
               }
               //下一轮号码保存，记得清空
               tempNumber.clear();
           }
       }
       raw_cursor.close();
       //返回false表示设备当中的报警号码不全,应该进行删除更新
       return false ;
   }


    //zhujiinfo转成deviceinfo
    public static DeviceInfo getZhujiDevice(ZhujiInfo zhuji) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setId(zhuji.getId());
        deviceInfo.setRelationId(zhuji.getRelationId());
        deviceInfo.setMac(zhuji.getMac());
        deviceInfo.setZj_id(zhuji.getId());
        deviceInfo.setName(zhuji.getName());
        deviceInfo.setWhere(zhuji.getWhere());
        deviceInfo.setType(zhuji.getDt());
        deviceInfo.setStatus(zhuji.getUpdateStatus());
        deviceInfo.setControlType(DeviceInfo.ControlTypeMenu.zhuji.value());
        deviceInfo.setLogo(zhuji.getLogo());
        deviceInfo.setGsm(zhuji.getGsm());
        deviceInfo.setFlag(zhuji.isAdmin()); // 利用deviceInfo的flag存主机的是否admin信息
        deviceInfo.setPowerStatus(zhuji.getPowerStatus());
        deviceInfo.setLowb(zhuji.getBatteryStatus() == 1);//是否底电
        deviceInfo.setCa(zhuji.getCa());
        deviceInfo.setCak(zhuji.getCak());
        deviceInfo.setMasterId(zhuji.getMasterid());
        deviceInfo.setSlaveId(zhuji.getMasterid());
        deviceInfo.setBipc(String.valueOf(zhuji.getBipc()));
        return deviceInfo;
    }

    public static ZhujiInfo itemToZhujiInfo(ListCtrUserDeviceRelationsQuery.Item info) {
        ZhujiInfo zhujiInfo = new ZhujiInfo();
        zhujiInfo.setType(info.type());
        zhujiInfo.setRelationId(info.id());
        zhujiInfo.setName(info.name());
        zhujiInfo.setMac(info.mac());
        zhujiInfo.setCak(DeviceInfo.CakMenu.zhuji.value());
        return zhujiInfo;
    }

    public static ZhujiInfo itemToZhujiInfo(ListCtrUserGroupsQuery.Item info) {
        ZhujiInfo zhujiInfo = new ZhujiInfo();
        zhujiInfo.setType(info.type());
        zhujiInfo.setRelationId(info.id());
        zhujiInfo.setName(info.name());
        zhujiInfo.setCak(DeviceInfo.CakMenu.group.value());
        return zhujiInfo;
    }

    /**
     * 判断此activity是否被销毁
     * @param activity
     * @return
     */
    public boolean activityIsHave(Activity activity){
        if (activity == null || activity.isFinishing()) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed()) {
                return false;
            }
        }
        return true;
    }
    //关闭可关闭对象
    public static void closeQueity(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //防止解析格式错误
    public static double parseDouble(String value){
        double result = 0.0 ;
        try{
          result =  Double.parseDouble(value);
        }catch (NumberFormatException e){
            result = 0.0;
        }
        return result ;
    }

    /**
     * 判断正数的二进制某位是否为1
     * @return
     */
    public static boolean intBitIsTrue(int src,int position){
        return ((src>>(position-1))&1) == 1;
    }

    /**
     * pm10按等级转
     * @param pm10
     * @return
     */
    public static String pm10ToName(float pm10){
        if (0 <= pm10 && pm10 < 50){
            return "Very good and Excellent";
        }else if(51 <= pm10 && pm10 < 100){
            return "Good";
        }else if(101 <= pm10 && pm10 < 150){
            return "Bad";
        }
        return "Very Bad";
    }
}
