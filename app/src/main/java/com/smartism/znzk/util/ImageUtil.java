package com.smartism.znzk.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

/**
 * android图片工具类 对图片进行压缩处理
 *
 * @author 王建
 */
public class ImageUtil {

    public static final int IMAGE_WIDTH_LIMIT = 640;
    public static final int IMAGE_HEIGHT_LIMIT = 480;
    public static final int MESSAGE_SIZE_LIMIT = 300 * 1024;
    private static final String TAG = "UriImage";

    private Context mContext;
    private Uri mUri;
    private int mWidth;
    private int mHeight;
    private String mContentType;
    @SuppressWarnings("unused")
    private String mPath;
    private String mSrc;
    /**
     * The quality parameter which is used to compress JPEG images.
     */
    public static final int IMAGE_COMPRESSION_QUALITY = 80;

    public ImageUtil() {

    }

    public ImageUtil(Context context, Uri uri) {
        if ((null == context) || (null == uri)) {
            throw new IllegalArgumentException();
        }

        String scheme = uri.getScheme();
        if (scheme.equals("content")) {
        } else if (uri.getScheme().equals("file")) {
            initFromFile(context, uri);
        }

        // mSrc = mPath.substring(mPath.lastIndexOf('/') + 1);
        //
        // // Some MMSCs appear to have problems with filenames
        // // containing a space. So just replace them with
        // // underscores in the name, which is typically not
        // // visible to the user anyway.
        // mSrc = mSrc.replace(' ', '_');

        mContext = context;
        mUri = uri;

        decodeBoundsInfo();
    }

	/**
	 * 以最省内存的方式读取本地资源的图片
	 * 
	 * @param context
	 * @param resId
	 * @return
	 */
	public static Bitmap readBitMap(Context context, int resId) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		// 获取资源图片
		InputStream is = context.getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, opt); //重点是这个
	}
    /**
     * 将bitmap设置为灰图
     *
     * @param img1
     * @return
     */
    public static Bitmap ConvertGrayImg(Bitmap img1) {
        int w = img1.getWidth(), h = img1.getHeight();
        int[] pix = new int[w * h];
        img1.getPixels(pix, 0, w, 0, 0, w, h);
        int alpha = 0xFF << 24;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                // 获得像素的颜色 
                int color = pix[w * i + j];
                int red = ((color & 0x00FF0000) >> 16);
                int green = ((color & 0x0000FF00) >> 8);
                int blue = color & 0x000000FF;
                color = (red + green + blue) / 3;
                color = alpha | (color << 16) | (color << 8) | color;
                pix[w * i + j] = color;
            }
        }
        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        result.setPixels(pix, 0, w, 0, 0, w, h);
        return result;
    }

    // 图片压缩
    private void decodeBoundsInfo() {
        InputStream input = null;
        try {
            input = mContext.getContentResolver().openInputStream(mUri);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, opt);
            mWidth = opt.outWidth;
            mHeight = opt.outHeight;
        } catch (FileNotFoundException e) {
            // Ignore
            Log.e(TAG, "IOException caught while opening stream", e);
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    // Ignore
                    Log.e(TAG, "IOException caught while closing stream", e);
                }
            }
        }
    }

    private void initFromFile(Context context, Uri uri) {
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        mContentType = mimeTypeMap.getMimeTypeFromExtension(extension);
        if (mContentType == null) {
            throw new IllegalArgumentException(
                    "Unable to determine extension for " + uri.toString());
        }
        mPath = uri.getPath();
    }

    public String getContentType() {
        return mContentType;
    }

    public String getSrc() {
        return mSrc;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    /**
     * @param file
     * @param widthLimit
     * @param heightLimit
     * @return
     * @throws FileNotFoundException
     */
    public static Bitmap getResizedImageData(File file, int widthLimit,
                                             int heightLimit) throws FileNotFoundException {
        try {
            //重新编辑By wj
            Bitmap bitmap1 = BitmapFactory.decodeFile(file.getAbsolutePath());
            Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap1, widthLimit, heightLimit, true);
            bitmap1.recycle();
            return bitmap2;
        } catch (OutOfMemoryError error) {
            Log.w(TAG, "getResizedImageData 内存溢出了！");
            return null;
        }
        // Log.i(TAG, " getResizedImageData====>");
        /*FileInputStream is = new FileInputStream(file);
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(is, null, opt);
		int outWidth = opt.outWidth;
		int outHeight = opt.outHeight;
		int s = 1;
		while ((outWidth / s > widthLimit) || (outHeight / s > heightLimit)) {
			s *= 2;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = s;
		try {
			FileInputStream inputStream = new FileInputStream(file);
			Bitmap b = BitmapFactory.decodeStream(inputStream, null, options);
			return b;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
		 */
    }

    public byte[] getResizedImageData(int widthLimit, int heightLimit) {

        int outWidth = mWidth;
        int outHeight = mHeight;

        int s = 1;
        while ((outWidth / s > widthLimit) || (outHeight / s > heightLimit)) {
            s *= 2;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = s;

        InputStream input = null;
        try {
            input = mContext.getContentResolver().openInputStream(mUri);
            Bitmap b = BitmapFactory.decodeStream(input, null, options);
            if (b == null) {
                return null;
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            b.compress(CompressFormat.JPEG, IMAGE_COMPRESSION_QUALITY, os);
            return os.toByteArray();
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }

    /**
     * bitmap -> byte
     *
     * @param bm
     * @return
     */
    public static byte[] Bitmap2Bytes(Bitmap bm, CompressFormat format) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 100;
        bm.compress(format, options, baos);//第二个参数表示压缩0压缩100% 100不压缩
        while (baos.toByteArray().length > 60000) {  //循环判断如果压缩后图片是否大于60000字节,大于继续压缩
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            bm.compress(format, options, baos);//这里压缩options%，把压缩后的数据存放到baos中 
            if (options == 0) {
                break;
            }
        }
        bm.recycle();
        return baos.toByteArray();
    }

    /**
     * bitmap -> byte
     *
     * @param bm
     * @return
     */
    public static byte[] Bitmap2BytesYasuo(Bitmap bm, CompressFormat format, int yasuolv) {
        if (yasuolv > 100) {
            yasuolv = 100;
        }
        if (yasuolv < 0) {
            yasuolv = 0;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(format, 100 - yasuolv, baos);//第二个参数表示压缩0压缩100% 100不压缩
        return baos.toByteArray();
    }

    /**
     * drawable -> bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable drawable2 = (BitmapDrawable) drawable;
            return drawable2.getBitmap();
        } else {
            Bitmap bitmap = Bitmap
                    .createBitmap(70, 70,
                            /**drawable.getIntrinsicWidth(),
                             drawable.getIntrinsicHeight(),*/
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565
                    );
            Canvas canvas = new Canvas(bitmap);
            // canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);

            return bitmap;
        }

    }

    /**
     * btye -> bitmap
     *
     * @param b
     * @return
     */
    public static Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public static void startPhotoZoom(Activity activity, Uri uri, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 800);
        intent.putExtra("outputY", 800);
        intent.putExtra("return-data", true);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 使用SD卡上的图片 用来发送消息
     *
     * @throws FileNotFoundException
     */
    public static byte[] loaddSDCardImage(String imageSDCardPath)
            throws FileNotFoundException {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了   
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(imageSDCardPath, newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //照片最长的一边不能超过1000像素
        int hh = 1000;
        int ww = 1000;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可   
        int be = 1;//be=1表示不缩放   
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放   
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放   
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例   
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了   
        bitmap = BitmapFactory.decodeFile(imageSDCardPath, newOpts);
        String imageName = imageSDCardPath.substring(imageSDCardPath.lastIndexOf("/") + 1);//图片名
        String fileSuffixes = imageName.substring(imageName.lastIndexOf("."));//图片后缀
        if (fileSuffixes != null && !"".equals(fileSuffixes) && ".png".equalsIgnoreCase(fileSuffixes)) {
            return Bitmap2Bytes(bitmap, CompressFormat.PNG);
        }
        return Bitmap2Bytes(bitmap, CompressFormat.JPEG);//压缩好比例大小后再进行质量压缩
    }

    /**
     * 图片圆角  剪裁方式
     *
     * @param bitmap
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 12;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();
        return output;
    }

    /**
     * 图片圆角 纹理填充方式
     *
     * @param bitmap
     * @return
     */
    public static Bitmap getRoundedCornerBitmap2(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);

        RectF rect = new RectF(0.0f, 0.0f, bitmap.getWidth(), bitmap.getHeight());

        // rect contains the bounds of the shape
        // radius is the radius in pixels of the rounded corners
        // paint contains the shader that will texture the shape
        canvas.drawRoundRect(rect, 12, 12, paint);
        bitmap.recycle();
        return output;
    }
    
  //使用Bitmap加Matrix来缩放
    /**
     * 缩放图片 固定大小
     * @param bitmap
     * @param w
     * @param h
     * @return
     */
    public static Bitmap resizeBitmap(Bitmap bitmap, int w, int h) 
    {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleL = 0;
        if(width > height){
        	scaleL = ((float) w) / width;
        }else{
        	scaleL = ((float) h) / height;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scaleL, scaleL);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);
        return Bitmap.createBitmap(bitmap, 0, 0, width,  
                        height, matrix, true);
    }

    /**
     * 给图片带上阴影
     */
    public static Bitmap drawImageDropShadow(Bitmap source) {
        if (source == null) {
            return null;
        }
        BlurMaskFilter blurFilter = new BlurMaskFilter(2, BlurMaskFilter.Blur.NORMAL);
        Paint shadowPaint = new Paint();
        shadowPaint.setMaskFilter(blurFilter);
        int[] offsetXY = new int[2];
        Bitmap shadowBitmap = source.extractAlpha(shadowPaint, offsetXY);

        Bitmap shadowImage32 = shadowBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas c = new Canvas(shadowImage32);
        c.drawBitmap(source, 0, 0, null);
        return shadowImage32;
    }
}
