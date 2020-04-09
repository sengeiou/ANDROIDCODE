package com.smartism.znzk.activity.user;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.util.CropImageUtil.CropImageView;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.SecurityUtil;
import com.smartism.znzk.util.Util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by dandy on 2016/6/22.
 */
public class CropImageActivity extends ActivityParentActivity {

    private static final String TAG = "CropImageActivity";
    private CropImageView cropImageView;
    public static final String CROPBYTE = "crop_bytes";

    private boolean isCompress = false;
    private String logoUrl;
    private String sourcePath = "";
    private File file;
    public static final int MEDICLINE_TYPE = 2;//药箱类型
    private int type;
    private boolean isUserLogo;

    private ImageView iv_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropimage_layout);
        type = getIntent().getIntExtra("type", 0);
        isUserLogo = getIntent().getBooleanExtra("logo", false);
        sourcePath = getIntent().getStringExtra("path");
        cropImageView = (CropImageView) findViewById(R.id.cropImageView);
        iv_save = (ImageView) findViewById(R.id.iv_save);
        cropImageView.setImageUriAsync(getIntent().getData());

        iv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showInProgress(getString(R.string.ongoing), false, true);
                if (sourcePath == null) {
                    return;
                }
//        file = new File(sourcePath);
//            byte[] bitmaps = compressBitmap(cropImageView.getCroppedImage());
                byte[] bitmaps = compressBitmap(cropImageView.getCroppedImage());

//            Bitmap bm = getSmallBitmap(sourcePath, 100, 100);
//            Log.e("TAG", "size: " + bm.getByteCount() + " width: " + bm.getWidth() + " heigth:" + bm.getHeight()); // 输出图像数据
//            if (bm == null) {
//                return;
//            }
//            ByteBuffer buffer = ByteBuffer.allocate(bm.getByteCount());
//            bm.copyPixelsToBuffer(buffer);
                if (bitmaps == null) {
                    Toast.makeText(mContext, getString(R.string.get_image_fail), Toast.LENGTH_SHORT).show();
                    cancelInProgress();
                    return;
                }
                getFile(bitmaps, sourcePath);
                if (file == null) {
                    return;
                }
                uploadMultiFile();
            }
        });

    }


    public void back(View v) {
        finish();
    }


    private void uploadMultiFile() {
        showInProgress(getString(R.string.ongoing), false, false);
        String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");

        String url = "";
        long uid = dcsp.getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
        String code = dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");
        if (type == MEDICLINE_TYPE) {
            url =  server + "/jdm/s3/upimg/img";
        } else {

            url =  server + "/jdm/s3/u/up/logo";
        }
        if (url.contains("s3/u/login") || url.contains("s3/u/gzhlogin")) {//登录请求不需要uid验证，登出和踢下线dcsp里面的数据偶尔会不清除
            uid = 0;
        }
        String v = "";
        String n = Util.randomString(12);
//        String s = SecurityUtil.createSign(v, uid, code, n);
        String s = SecurityUtil.createSign(v, MainApplication.app.getAppGlobalConfig().getAppid(), MainApplication.app.getAppGlobalConfig().getAppSecret(), code, n);


        final RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("img", file.getName(), RequestBody.create(MediaType.parse("image/png"), file))
                .addFormDataPart("uid", String.valueOf(uid))
                .addFormDataPart("appid", MainApplication.app.getAppGlobalConfig().getAppid())
                .addFormDataPart("v", v)
                .addFormDataPart("n", n)
                .addFormDataPart("s", s)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        final okhttp3.OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        OkHttpClient okHttpClient = httpBuilder
                //设置超时
                .connectTimeout(15000L, TimeUnit.MILLISECONDS)
                .readTimeout(40000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, IOException e) {
                Log.e(TAG, "uploadMultiFile() e=" + e);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(CropImageActivity.this, getString(R.string.time_out), Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    cancelInProgress();
                    logoUrl = response.body().string();
                    if (logoUrl.length() > 4) {
                        if (isUserLogo) {
                            dcsp.putString(Constant.LOGIN_LOGO, logoUrl).commit();
                        } else if (type == MEDICLINE_TYPE || type == 0) {
                            Intent intent = new Intent();
                            intent.putExtra("path", logoUrl);
                            setResult(RESULT_OK, intent);
                        }
//                        Intent intent = new Intent(Actions.USER_LOGO);
//                        sendBroadcast(intent);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, getString(R.string.error_ap), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            }
        });
    }


    public void saveBitmapFile(Bitmap bitmap) {
        File file = new File("");//将要保存图片的路径
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据byte数组，生成文件
     */
    public void getFile(byte[] bfile, String filePath) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory()) {//判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(dir.getParent() + "/temp" + filePath.substring(filePath.lastIndexOf("."), filePath.length()));
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


    private byte[] compressBitmap(Bitmap source) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 100;
        byte[] bytes = null;
        do {
            baos.reset();
            options -= 10;
            source.compress(Bitmap.CompressFormat.JPEG, options <= 0 ? 0 : options, baos);
            if (options == 0 && baos.toByteArray().length / 1024 >= 150) {
                Toast.makeText(this, getString(R.string.crop_image_big), Toast.LENGTH_SHORT).show();
                bytes = null;
                return null;
            }
        } while ((bytes = baos.toByteArray()).length / 1024 >= 150);
        return bytes;
    }

    private byte[] compressBitmap1(Bitmap source) {
        byte[] bytes = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            source.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            bytes = baos.toByteArray();
            baos.flush();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
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

    public static Bitmap getSmallBitmap1(String filePath) {
        // 设置参数
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 只获取图片的大小信息，而不是将整张图片载入在内存中，避免内存溢出
        BitmapFactory.decodeFile(filePath, options);
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 2; // 默认像素压缩比例，压缩为原图的1/2
        int minLen = Math.min(height, width); // 原图的最小边长
        if (minLen > 100) { // 如果原始图像的最小边长大于100dp（此处单位我认为是dp，而非px）
            float ratio = (float) minLen / 100.0f; // 计算像素压缩比例
            inSampleSize = (int) ratio;
        }
        options.inJustDecodeBounds = false; // 计算好压缩比例后，这次可以去加载原图了
        options.inSampleSize = inSampleSize; // 设置为刚才计算的压缩比例
//        Bitmap bm = BitmapFactory.decodeFile(imagePath, options); // 解码文件
        return BitmapFactory.decodeFile(filePath, options);
    }
}
