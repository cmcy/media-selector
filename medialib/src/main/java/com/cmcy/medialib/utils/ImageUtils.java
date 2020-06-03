package com.cmcy.medialib.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * FileName    : ImageUtils.java
 * Description : 
 * @Copyright  : hysdpower. All Rights Reserved
 * Create Date : 2014-4-10 下午2:14:37
 **/
public class ImageUtils {

	private static final String TAG = "ImageUtils";

	/** 尝试打开图片次数 **/
	private static final int MAX_TRY_OPEN_IMAGE = 5;

	public static Bitmap getBitmap(String pathFile) {
		return getBitmap(pathFile, /*-1*/1280 * 720);//1280 * 720
	}

	/**
	 * 通过路径获取图片，屏蔽掉oom，并且获取图片进行oom重试机制
	 * 
	 * @param pathFile
	 *            pathFile
	 * @param maxLength
	 *            期望大小，（长X宽）
	 * @return Bitmap [返回类型说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static Bitmap getBitmap(String pathFile, int maxLength) {
		if (TextUtils.isEmpty(pathFile) || !Utils.exists(pathFile)) {
			Log.e(TAG, "不能获取到bitmap,pathFile=" + pathFile);
			return null;
		}

		Bitmap bitmap = null;
		try {
			BitmapFactory.Options option = new BitmapFactory.Options();

			option.inJustDecodeBounds = true;

			BitmapFactory.decodeFile(pathFile, option);

			option.inJustDecodeBounds = false;

			// 获取压缩值
			option.inSampleSize = computeSampleSize(option, -1, maxLength);

			// 重试次数
			int tryCount = 1;

			Log.d(TAG, "获取bitmap，pathFile=" + pathFile);

			do {
				if (tryCount > 1) {
					if (option.inSampleSize < 1) {
						option.inSampleSize = 1;
					}

					option.inSampleSize *= tryCount;
				}

				bitmap = getBitmap(pathFile, option);
				// 纠正选择的图片
				int degree = ImageUtils.readPictureDegree(pathFile);
				if (degree % 360 != 0) {
					bitmap = ImageUtils.rotaingImageView(degree, bitmap);
				}
				tryCount++;

				Log.d(TAG, "尝试打开图片次数，tryCount=" + tryCount + ",压缩大小=" + option.inSampleSize);
			} while (bitmap == null && tryCount < MAX_TRY_OPEN_IMAGE);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	private static Bitmap getBitmap(String pathFile, BitmapFactory.Options option) {
		Bitmap bitmap = null;
		if (!TextUtils.isEmpty(pathFile)) {
			InputStream stream = null;
			try {
				stream = new FileInputStream(pathFile);

				bitmap = BitmapFactory.decodeStream(stream, null, option);
			} catch (FileNotFoundException e) {
				Log.e(TAG, "没有文件，pathFile=" + pathFile, e);
			} catch (OutOfMemoryError oom) {
				long length = -1;
				try {
					length = stream != null ? stream.available() : -1;
				} catch (IOException e) {
					Log.e(TAG, e.toString(), e);
				}

				Log.e(TAG, "获取图片内存溢出，option=" + option.inSampleSize + ",length=" + length);
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						Log.e(TAG, "close InputStream is Error", e);
					}
				}
			}
		}

		return bitmap;
	}

	/**
	 * 读取图片属性：旋转的角度
	 * 
	 * @param path
	 *            图片绝对路径
	 * @return degree旋转的角度
	 */
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * 旋转图片
	 * 
	 * @param angle
	 *            angle
	 * @param bitmap
	 *            bitmap
	 * @return Bitmap
	 */
	public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
		// 旋转图片 动作
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}

	/**
	 * android源码提供的计算inSampleSize方法
	 * 
	 * @param options
	 *            options
	 * @param minSideLength
	 *            minSideLength
	 * @param maxNumOfPixels
	 *            maxNumOfPixels
	 * @return int [返回类型说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
		int roundedSize;

		if (initialSize <= 8) {
			roundedSize = 1;

			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}

		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;

		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));

		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {

			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}

		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 获取Uri对应的缩略图
	 * @param uri
	 * @return
	 */
	public static Bitmap getThumbnailImage(Context context, Uri uri) {
		Bitmap bitmap = null;
		String[] proj = new String[] { MediaColumns.DATA, BaseColumns._ID };
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(uri, proj, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				long thumbnailId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
				Log.e(TAG, "ThumbnailID = " + thumbnailId);
				// MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
				bitmap = Images.Thumbnails.getThumbnail(context.getContentResolver(), thumbnailId, Images.Thumbnails.MICRO_KIND, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return bitmap;
	}

	/**
	 * 获取Uri对应的原图路径
	 * @param uri
	 * @return
	 */
	public static String getImagePath(Context context, Uri uri) {
		String path = null;
		String[] proj = { MediaColumns.DATA };
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(uri, proj, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				path = cursor.getString(cursor.getColumnIndexOrThrow(MediaColumns.DATA));
				Log.e(TAG, "OrialImagePath = " + path);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return path;
	}

	/**
	 * Resize the bitmap
	 * 
	 * @param bitmap
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) width / w);
		float scaleHeight = ((float) height / h);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		return newbmp;
	}
	
	/**
	 * Check the SD card 
	 * @return
	 */
	public static boolean checkSDCardAvailable(){
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
	
	/**
	 * Save image to the SD card 
	 * @param photoBitmap
	 * @param photoPath
	 */
	public static void savePhotoToSDCard(Bitmap photoBitmap,String photoPath){
		if (checkSDCardAvailable()) {
			File photoFile = new File(photoPath);
			File parentDir = new File(photoFile.getParent());
			if (!parentDir.exists()){
				parentDir.mkdirs();
			}

			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(photoFile);
				if (photoBitmap != null) {
					if (photoBitmap.compress(CompressFormat.PNG, 100, fileOutputStream)) {
						fileOutputStream.flush();
					}
				}
			} catch (FileNotFoundException e) {
				photoFile.delete();
				e.printStackTrace();
			} catch (IOException e) {
				photoFile.delete();
				e.printStackTrace();
			} finally{
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public static void progressAfterSeletPhoto(String imagePath,
			String tempFilePath) {

		Bitmap resizeBitmap = null;

		try {
			File file = new File(imagePath);
			resizeBitmap = fitSizePic(file, 0, 0);

			File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/my_avatar");
			if (directory.exists() == false) {
				directory.mkdirs();
			}

			File tempFile = new File(tempFilePath);
			if (tempFile.exists() == false) {
				tempFile.createNewFile();
			}

			FileOutputStream fos = null;
			fos = new FileOutputStream(tempFile);
			resizeBitmap.compress(CompressFormat.JPEG, 80, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (resizeBitmap != null && resizeBitmap.isRecycled() == false) {
				resizeBitmap.recycle();
				resizeBitmap = null;
			}
		}
	}
	
	
	public static Bitmap fitSizePic(File file, int width, int height) {

		Bitmap resizeBmp = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		// Log.v("---length==----", String.valueOf(file.length()));
		if (file.length() < 254800) {
			opts.inSampleSize = 1;
		} else if (file.length() >= 254800 && file.length() < 409600) {
			opts.inSampleSize = 2;
			// Log.v("---length1==----", String.valueOf(file.length()));
		} else if (file.length() >= 409600 && file.length() < 614400) { // 400-600k
			// Log.v("---length2==----", String.valueOf(file.length()));
			opts.inSampleSize = 2;
		} else if (file.length() >= 614400 && file.length() < 819200) { // 600-800k
			// Log.v("---length3==----", String.valueOf(file.length()));
			opts.inSampleSize = 2;
		} else if (file.length() >= 819200 && file.length() < 1024000) {// 800-1000K
			// Log.v("---length4==----", String.valueOf(file.length()));
			opts.inSampleSize = 2;
			// Log.v("---length15==----", String.valueOf(file.length()));
		} else if (file.length() >= 1024000 && file.length() < 2048000) {// 800-1000K
			// Log.v("---length4==----", String.valueOf(file.length()));
			opts.inSampleSize = 3;
			// Log.v("---length15==----", String.valueOf(file.length()));
		} else if (file.length() > 2048000 && file.length() < 4096000) {// 800-1000K
			// Log.v("---length4==----", String.valueOf(file.length()));
			opts.inSampleSize = 3;
			// Log.v("---length15==----", String.valueOf(file.length()));
		} else if (file.length() >= 4096000 && file.length() < 6144000) {// 800-1000K
			// Log.v("---length4==----", String.valueOf(file.length()));
			opts.inSampleSize = 4;
			// Log.v("---length15==----", String.valueOf(file.length()));
		} else if (file.length() >= 6144000 && file.length() < 8192000) {// 800-1000K
			// Log.v("---length4==----", String.valueOf(file.length()));
			opts.inSampleSize = 4;
			// Log.v("---length15==----", String.valueOf(file.length()));
		} else if (file.length() >= 8192000 && file.length() < 10240000) {// 800-1000K
			// Log.v("---length4==----", String.valueOf(file.length()));
			opts.inSampleSize = 5;
			// Log.v("---length15==----", String.valueOf(file.length()));
		} else {
			opts.inSampleSize = 6;
		}

		int rotateDegree = 0;

		try {
			rotateDegree = readPictureDegree(file.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (rotateDegree == 0) {
			resizeBmp = BitmapFactory.decodeFile(file.getPath(), opts);
		} else {
			Matrix matrix = new Matrix();
			matrix.postRotate(rotateDegree);

			Bitmap source = BitmapFactory.decodeFile(file.getPath(), opts);
			resizeBmp = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
					source.getHeight(), matrix, false);
		}

		return resizeBmp;
	}


	public static Bitmap decodeScaleImage(String var0, int var1, int var2) {
		BitmapFactory.Options var3 = getBitmapOptions(var0);
		int var4 = calculateInSampleSize(var3, var1, var2);
		Log.d("img", "original wid" + var3.outWidth + " original height:" + var3.outHeight + " sample:" + var4);
		var3.inSampleSize = var4;
		var3.inJustDecodeBounds = false;
		Bitmap var5 = BitmapFactory.decodeFile(var0, var3);
		int var6 = readPictureDegree(var0);
		Bitmap var7 = null;
		if(var5 != null && var6 != 0) {
			var7 = rotaingImageView(var6, var5);
			var5.recycle();
			var5 = null;
			return var7;
		} else {
			return var5;
		}
	}

	public static int calculateInSampleSize(BitmapFactory.Options var0, int var1, int var2) {
		int var3 = var0.outHeight;
		int var4 = var0.outWidth;
		int var5 = 1;
		if(var3 > var2 || var4 > var1) {
			int var6 = Math.round((float)var3 / (float)var2);
			int var7 = Math.round((float)var4 / (float)var1);
			var5 = var6 > var7?var6:var7;
		}

		return var5;
	}

	public static BitmapFactory.Options getBitmapOptions(String var0) {
		BitmapFactory.Options var1 = new BitmapFactory.Options();
		var1.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(var0, var1);
		return var1;
	}


	/**
	 * 对图片文件进行压缩
	 *
	 * @param filePath
	 * @return
	 */
	public static File compress(String filePath)
	{
		try
		{
			FileInputStream fis = new FileInputStream(filePath);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			Bitmap bitmap = BitmapFactory.decodeFile(filePath);
			if (bitmap != null)
				baos = compress(bitmap, 1024);// 对图片进行压缩处理

			File photoFile = new File(filePath);
			File parentDir = new File(photoFile.getParent());
			if (!parentDir.exists()){
				parentDir.mkdirs();
			}
			if (!photoFile.exists()) {
				photoFile.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(photoFile);
			fos.write(baos.toByteArray());
			fos.close();
			fis.close();
			return photoFile;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * 图片压缩方法
	 * 
	 * @param bitmap
	 *            图片文件
	 * @param maxBit
	 *            文件大小最大值
	 * @return 压缩后的字节流
	 * @throws Exception
	 */
	public static ByteArrayOutputStream compress(Bitmap bitmap, int maxBit)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
			int options = 100;
			while ((baos.toByteArray().length / 1024) > maxBit) // 循环判断如果压缩后图片是否大于maxBit,大于继续压缩
			{
				options -= 10;// 每次都减少10
				// 压缩比小于0，不再压缩
				if (options < 0)
				{
					break;
				}
				Log.i(TAG,baos.toByteArray().length / 1024+"");
				baos.reset();// 重置baos即清空baos
				bitmap.compress(CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			}
			return baos;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.e(TAG, "压缩图片异常......", e);
			System.gc();
			return null;
		}

	}
	
	
	
	public static void progressAfterTakenPicture(String tempFilePath) {

		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 4;

		Bitmap bitmap = null;
		FileOutputStream fos = null;

		try {
			int rotateDegree = readPictureDegree(tempFilePath);

			Matrix matrix = new Matrix();
			matrix.postRotate(rotateDegree);

			bitmap = BitmapFactory.decodeFile(tempFilePath, opts);
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
					bitmap.getHeight(), matrix, true);

			fos = new FileOutputStream(tempFilePath);
			bitmap.compress(CompressFormat.JPEG, 80, fos);
			fos.flush();
			fos.close();

			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
