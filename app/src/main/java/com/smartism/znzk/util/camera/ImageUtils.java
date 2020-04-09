package com.smartism.znzk.util.camera;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Color.WHITE;
import static android.graphics.PorterDuff.Mode.DST_IN;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import com.smartism.znzk.global.Constants;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;



/**
 * Image utilities
 */
public class ImageUtils {

	private static final String TAG = "ImageUtils";

	/**
	 * Get a bitmap from the image path
	 * 
	 * @param imagePath
	 * @return bitmap or null if read fails
	 */
	public static Bitmap getBitmap(final String imagePath) {
		return getBitmap(imagePath, 1);
	}

	/**
	 * Get a bitmap from the image path
	 * 
	 * @param imagePath
	 * @param sampleSize
	 * @return bitmap or null if read fails
	 */
	public static Bitmap getBitmap(final String imagePath, int sampleSize) {
		final Options options = new Options();
		options.inDither = false;
		options.inSampleSize = sampleSize;

		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile(imagePath, "r");
			return BitmapFactory.decodeFileDescriptor(file.getFD(), null,
					options);
		} catch (IOException e) {
			// Log.d(TAG, e.getMessage(), e);
			return null;
		} finally {
			if (file != null)
				try {
					file.close();
				} catch (IOException e) {
					// Log.d(TAG, e.getMessage(), e);
				}
		}
	}

	/**
	 * Get a bitmap from the image
	 * 
	 * @param image
	 * @param sampleSize
	 * @return bitmap or null if read fails
	 */
	public static Bitmap getBitmap(final byte[] image, int sampleSize) {
		final Options options = new Options();
		options.inDither = false;
		options.inSampleSize = sampleSize;
		return BitmapFactory.decodeByteArray(image, 0, image.length, options);
	}

	/**
	 * Get scale for image of size and max height/width
	 * 
	 * @param size
	 * @param width
	 * @param height
	 * @return scale
	 */
	public static int getScale(Point size, int width, int height) {
		if (size.x > width || size.y > height)
			return Math.max(Math.round((float) size.y / (float) height),
					Math.round((float) size.x / (float) width));
		else
			return 1;
	}

	/**
	 * Get size of image
	 * 
	 * @param imagePath
	 * @return size
	 */
	public static Point getSize(final String imagePath) {
		final Options options = new Options();
		options.inJustDecodeBounds = true;

		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile(imagePath, "r");
			BitmapFactory.decodeFileDescriptor(file.getFD(), null, options);
			return new Point(options.outWidth, options.outHeight);
		} catch (IOException e) {
			Log.d(TAG, e.getMessage(), e);
			return null;
		} finally {
			if (file != null)
				try {
					file.close();
				} catch (IOException e) {
					Log.d(TAG, e.getMessage(), e);
				}
		}
	}

	/**
	 * Get size of image
	 * 
	 * @param image
	 * @return size
	 */
	public static Point getSize(final byte[] image) {
		final Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(image, 0, image.length, options);
		return new Point(options.outWidth, options.outHeight);
	}

	/**
	 * Get bitmap with maximum height or width
	 * 
	 * @param imagePath
	 * @param width
	 * @param height
	 * @return image
	 */
	public static Bitmap getBitmap(final String imagePath, int width, int height) {
		Point size = getSize(imagePath);
		return getBitmap(imagePath, getScale(size, width, height));
	}

	/**
	 * Get bitmap with maximum height or width
	 * 
	 * @param image
	 * @param width
	 * @param height
	 * @return image
	 */
	public static Bitmap getBitmap(final byte[] image, int width, int height) {
		Point size = getSize(image);
		return getBitmap(image, getScale(size, width, height));
	}

	/**
	 * Get bitmap with maximum height or width
	 * 
	 * @param image
	 * @param width
	 * @param height
	 * @return image
	 */
	public static Bitmap getBitmap(final File image, int width, int height) {
		return getBitmap(image.getAbsolutePath(), width, height);
	}

	/**
	 * Get a bitmap from the image file
	 * 
	 * @param image
	 * @return bitmap or null if read fails
	 */
	public static Bitmap getBitmap(final File image) {
		return getBitmap(image.getAbsolutePath());
	}

	/**
	 * Load a {@link Bitmap} from the given path and set it on the given
	 * {@link ImageView}
	 * 
	 * @param imagePath
	 * @param view
	 */
	public static void setImage(final String imagePath, final ImageView view) {
		setImage(new File(imagePath), view);
	}

	/**
	 * Load a {@link Bitmap} from the given {@link File} and set it on the given
	 * {@link ImageView}
	 * 
	 * @param image
	 * @param view
	 */
	public static void setImage(final File image, final ImageView view) {
		Bitmap bitmap = getBitmap(image);
		if (bitmap != null)
			view.setImageBitmap(bitmap);
	}

	/**
	 * Round the corners of a {@link Bitmap}
	 * 
	 * @param source
	 * @param radius
	 * @return rounded corner bitmap
	 */
	public static Bitmap roundCorners(final Bitmap source, final float radius) {
		int width = source.getWidth();
		int height = source.getHeight();

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(WHITE);

		Bitmap clipped = Bitmap.createBitmap(width, height, ARGB_8888);
		Canvas canvas = new Canvas(clipped);

		canvas.drawRoundRect(new RectF(0, 0, width, height), radius, radius, paint);
		paint.setXfermode(new PorterDuffXfermode(DST_IN));

		Bitmap rounded = Bitmap.createBitmap(width, height, ARGB_8888);
		canvas = new Canvas(rounded);
		canvas.drawBitmap(source, 0, 0, null);
		canvas.drawBitmap(clipped, 0, 0, paint);

		source.recycle();
		clipped.recycle();

		return rounded;
	}

	public static Bitmap roundHalfCorners(Context context, final Bitmap source,
			final float radius, int oratation) {
		int width = source.getWidth();
		int height = source.getHeight();

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(WHITE);

		Bitmap clippeds = Bitmap.createBitmap(width, height, ARGB_8888);
		Canvas canvas = new Canvas(clippeds);
		clipTop(canvas, paint, (int) radius, width, height);

		paint.setXfermode(new PorterDuffXfermode(DST_IN));

		Bitmap rounded = Bitmap.createBitmap(width, height, ARGB_8888);
		canvas = new Canvas(rounded);
		canvas.drawBitmap(source, 0, 0, null);
		canvas.drawBitmap(clippeds, 0, 0, paint);

		source.recycle();
		clippeds.recycle();

		return rounded;
	}

	private static void clipTop(final Canvas canvas, final Paint paint,
			int offset, int width, int height) {
		final Rect block = new Rect(0, offset, width, height);
		canvas.drawRect(block, paint);
		final RectF rectF = new RectF(0, 0, width, offset * 2);
		canvas.drawRoundRect(rectF, offset, offset, paint);
	}

	private static void clipBottom(final Canvas canvas, final Paint paint,
			int offset, int width, int height) {
		final Rect block = new Rect(0, 0, width, height - offset);
		canvas.drawRect(block, paint);
		final RectF rectF = new RectF(0, height - offset * 2, width, height);
		canvas.drawRoundRect(rectF, offset, offset, paint);
	}

	public static String getAbsPath(Context context, Uri uri) {
		if (uri == null) {
			return null;
		}

		String path = uri.getPath();
		if (path != null && uri.toString().toLowerCase().startsWith("file://")) {
			return path;
		}

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(uri, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();

			int index = cursor.getColumnIndex("_data");
			if (index != -1) {
				String filePath = cursor.getString(index);
				return filePath;
			}
		}
		return path;
	}

	public static void saveImg(Bitmap bitmap, String path, String name) {
		// bitmap = roundCorners(bitmap,20);
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		File img = new File(file, name);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(img);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static Bitmap grayBitmap(Bitmap bitmap) {
		int width, height;
		height = bitmap.getHeight();
		width = bitmap.getWidth();

		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
				Config.RGB_565);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bitmap, 0, 0, paint);
		bitmap.recycle();
		return bmpGrayscale;
	}

	public static int getScaleRounded(int width) {
		return (int) (((float) width) * Constants.Image.USER_HEADER_ROUND_SCALE);
	}

	public static byte[] getImageFromNetByUrl(String strUrl) {
		try {
			URL url = new URL(strUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5 * 1000);
			InputStream inStream = conn.getInputStream();// 通过输入流获取图片数据
			byte[] btImg = readInputStream(inStream);// 得到图片的二进制数据
			return btImg;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void writeImageToDisk(byte[] img, String path, String name) {
		try {
			File file = new File(path);
			if (!file.exists()) {
				file.mkdirs();
			}
			File destFile = new File(file, name);
			FileOutputStream fops = new FileOutputStream(destFile);
			fops.write(img);
			fops.flush();
			fops.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static byte[] readInputStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		inStream.close();
		return outStream.toByteArray();
	}
}