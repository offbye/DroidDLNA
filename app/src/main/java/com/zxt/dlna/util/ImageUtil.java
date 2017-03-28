package com.zxt.dlna.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Video.Thumbnails;
import android.text.TextUtils;
import android.util.Log;

public class ImageUtil {

	/**
	 * png后缀
	 */
	public static final String PNG_SUFFIX = ".png";

	/**
	 * jpg后缀
	 */
	public static final String JPG_SUFFIX = ".jpg";

	/**
	 * jpeg后缀
	 */
	public static final String JPEG_SUFFIX = ".jpeg";

	public static final String VIDEO_THUMBNAIL_PREFIX = "video_thumb_";

	/**
	 * 拼装保存的videoThumb的路径
	 * 
	 * @return
	 */
	public static String getSaveVideoFilePath(String path, String id) {
		return FileUtil.getSDPath() + FileUtil.VIDEO_THUMB_PATH
				+ File.separator + VIDEO_THUMBNAIL_PREFIX + id + PNG_SUFFIX;
	}

	public static String getServeVideoFilePath(String path) {
		return FileUtil.getSDPath() + FileUtil.VIDEO_THUMB_PATH
				+ File.separator + VIDEO_THUMBNAIL_PREFIX
				+ URLEncoder.encode(FileUtil.getFileName(path)) + PNG_SUFFIX;
	}

	/**
	 * 将Bitmap转化成图片存在本地，图片格式由文件路径的后缀名获得，支持png和jpg
	 * 
	 * @param bitmap
	 * @param filePath
	 * @throws Exception
	 */
	public static void saveBitmapWithFilePathSuffix(Bitmap bitmap,
			String filePath) throws Exception {
		if (null == bitmap || TextUtils.isEmpty(filePath)) {
			return;
		}

		String suffix = FileUtil.getFileSuffix(filePath);

		Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;

		if (PNG_SUFFIX.equalsIgnoreCase(suffix)) {
			format = Bitmap.CompressFormat.PNG;
		} else if (JPEG_SUFFIX.equalsIgnoreCase(suffix)
				|| JPG_SUFFIX.equalsIgnoreCase(suffix)) {
			format = Bitmap.CompressFormat.JPEG;
		} else {
			return;
		}

		File file = new File(filePath);
		FileOutputStream out;
		out = new FileOutputStream(file);
		if (bitmap.compress(format, 70, out)) {
			out.flush();
			out.close();
		}
	}

	/**
	 * 对于视频缩略图，均以video_thumbnail_作为前缀，每次存储时扫描文件夹，将旧缩略图删除，以减少冗余数据
	 * 
	 * @param filePath
	 */
	public static void deleteOldVideoThumbnail(String filePath) {
		if (!TextUtils.isEmpty(filePath)) {
			File fileFolder = new File(filePath);
			File[] files = fileFolder.listFiles();
			if (null != files && files.length > 0) {
				for (File file : files) {
					if (!file.isDirectory()
							&& file.getName().indexOf(VIDEO_THUMBNAIL_PREFIX) == 0) {
						file.delete();
					}
				}
			}
		}
	}

	@SuppressLint("NewApi")
	public static Bitmap getThumbnailForVideo(String videoAbsPath) {
		Bitmap bitmap = null;
		try {
			bitmap = ThumbnailUtils.createVideoThumbnail(videoAbsPath, Thumbnails.MINI_KIND);
		} catch (Exception e) {
			// It will not get here.
		}
		return bitmap;
	}

	// 用于生成缩略图。
	/**
	 * Creates a centered bitmap of the desired size. Recycles the input.
	 * 
	 * @param source
	 */
	public static Bitmap extractMiniThumb(Bitmap source, int width, int height) {
		return extractMiniThumb(source, width, height, true);
	}

	public static Bitmap extractMiniThumb(Bitmap source, int width, int height,
			boolean recycle) {
		if (source == null) {
			return null;
		}

		float scale;
		if (source.getWidth() < source.getHeight()) {
			scale = width / (float) source.getWidth();
		} else {
			scale = height / (float) source.getHeight();
		}
		Matrix matrix = new Matrix();
		matrix.setScale(scale, scale);
		Bitmap miniThumbnail = transform(matrix, source, width, height, false);

		if (recycle && miniThumbnail != source) {
			source.recycle();
		}
		return miniThumbnail;
	}

	public static Bitmap transform(Matrix scaler, Bitmap source,
			int targetWidth, int targetHeight, boolean scaleUp) {
		int deltaX = source.getWidth() - targetWidth;
		int deltaY = source.getHeight() - targetHeight;
		if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
			/*
			 * In this case the bitmap is smaller, at least in one dimension,
			 * than the target. Transform it by placing as much of the image as
			 * possible into the target and leaving the top/bottom or left/right
			 * (or both) black.
			 */
			Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
					Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b2);

			int deltaXHalf = Math.max(0, deltaX / 2);
			int deltaYHalf = Math.max(0, deltaY / 2);
			Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf
					+ Math.min(targetWidth, source.getWidth()), deltaYHalf
					+ Math.min(targetHeight, source.getHeight()));
			int dstX = (targetWidth - src.width()) / 2;
			int dstY = (targetHeight - src.height()) / 2;
			Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight
					- dstY);
			c.drawBitmap(source, src, dst, null);
			return b2;
		}
		float bitmapWidthF = source.getWidth();
		float bitmapHeightF = source.getHeight();

		float bitmapAspect = bitmapWidthF / bitmapHeightF;
		float viewAspect = (float) targetWidth / targetHeight;

		if (bitmapAspect > viewAspect) {
			float scale = targetHeight / bitmapHeightF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		} else {
			float scale = targetWidth / bitmapWidthF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		}

		Bitmap b1;
		if (scaler != null) {
			// this is used for minithumb and crop, so we want to filter here.
			b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
					source.getHeight(), scaler, true);
		} else {
			b1 = source;
		}

		int dx1 = Math.max(0, b1.getWidth() - targetWidth);
		int dy1 = Math.max(0, b1.getHeight() - targetHeight);

		Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth,
				targetHeight);

		if (b1 != source) {
			b1.recycle();
		}

		return b2;
	}

	/**
	 * 旋转位图
	 * 
	 * @param source
	 *            原位图
	 * @param degrees
	 *            旋转角度
	 * @return 旋转过得位图。
	 */
	public static Bitmap rotateBitmap(Bitmap source, float degrees) {
		if (source == null) {
			return null;
		}
		Matrix m = new Matrix();
		m.postRotate(degrees);
		Bitmap newBitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
				source.getHeight(), m, false);
		if (!source.isRecycled()) {
			source.recycle();
		}

		return newBitmap;
	}

	public static Bitmap getBitmapByIo(InputStream is, int maxWidth) {

		Bitmap bitmap = null;
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			int srcWidth = opts.outWidth;
			int srcHeight = opts.outHeight;
			int destWidth = 0;
			int destHeight = 0;

			// 缩放的比例
			double ratio = 0.0;

			// 按比例计算缩放后的图片大小，maxLength是长或宽允许的最大长度
			ratio = (double) srcWidth / maxWidth;
			destWidth = maxWidth;
			destHeight = (int) (srcHeight / ratio);

			// 对图片进行压缩，是在读取的过程中进行压缩，而不是把图片读进了内存再进行压缩
			BitmapFactory.Options newOpts = new BitmapFactory.Options();

			// 缩放的比例，缩放是很难按准备的比例进行缩放的，目前我只发现只能通过inSampleSize来进行缩放，其值表明缩放的倍数，SDK中建议其值是2的指数值
			if (ratio > 1.0) {
				newOpts.inSampleSize = (int) ratio;
			}

			// inJustDecodeBounds设为false表示把图片读进内存中
			newOpts.inJustDecodeBounds = false;

			// 设置大小，这个一般是不准确的，是以inSampleSize的为准，但是如果不设置却不能缩放
			newOpts.outHeight = destHeight;
			newOpts.outWidth = destWidth;

			bitmap = BitmapFactory.decodeStream(is, null, newOpts);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return bitmap;

	}

	/**
	 * 获得合适大小的位图
	 * 
	 * @param bytes
	 *            位图的二进制流
	 * @param maxHeight
	 *            最大高度
	 * @param path
	 *            图片路径
	 * @return Bitmap 位图
	 * @see [类、类#方法、类#成员]
	 */
	public static Bitmap getBitmapByWidth(byte[] bytes, int maxWidth,
			String path) {
		if (bytes == null && path == null) {
			return null;
		}

		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			if (path == null) {
				BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
			} else {
				BitmapFactory.decodeFile(path, opts);
			}
			int srcWidth = opts.outWidth;
			int srcHeight = opts.outHeight;
			int destWidth = 0;
			int destHeight = 0;

			// 缩放的比例
			double ratio = 0.0;

			// 按比例计算缩放后的图片大小，maxLength是长或宽允许的最大长度
			ratio = (double) srcWidth / maxWidth;
			destWidth = maxWidth;
			destHeight = (int) (srcHeight / ratio);

			// 对图片进行压缩，是在读取的过程中进行压缩，而不是把图片读进了内存再进行压缩
			BitmapFactory.Options newOpts = new BitmapFactory.Options();

			// 缩放的比例，缩放是很难按准备的比例进行缩放的，目前我只发现只能通过inSampleSize来进行缩放，其值表明缩放的倍数，SDK中建议其值是2的指数值
			if (ratio > 1.0) {
				newOpts.inSampleSize = (int) ratio;
			}

			// inJustDecodeBounds设为false表示把图片读进内存中
			newOpts.inJustDecodeBounds = false;

			// 设置大小，这个一般是不准确的，是以inSampleSize的为准，但是如果不设置却不能缩放
			newOpts.outHeight = destHeight;
			newOpts.outWidth = destWidth;
			Bitmap destBm1 = null;

			if (path == null) {
				// 获取缩放后图片
				destBm1 = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
						newOpts);
			} else {
				destBm1 = BitmapFactory.decodeFile(path, newOpts);
			}

			Bitmap bmp = null;
			if (ratio <= 1.0) {
				bmp = destBm1;
			} else if (destBm1.getWidth() != destWidth) {
				Bitmap destBm = Bitmap.createScaledBitmap(destBm1, destWidth,
						destHeight, true);
				destBm1.recycle();
				if (destBm != null) {
					bmp = destBm;
				}
			} else {
				bmp = destBm1;
			}

			// if (path != null)
			// {
			// int degree = getExifOrientation(path);
			// if (degree != 0)
			// {
			// bmp = rotateBitmap(bmp, degree);
			// }
			// }
			return bmp;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int getExifOrientation(String filepath) {
		int degree = 0;
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(filepath);
			Log.e("getExif", "get Exif");
		} catch (IOException ex) {
			Log.e("omg", "cannot read exif", ex);
		}
		if (exif != null) {
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION, -1);
			if (orientation != -1) {
				// We only recognize a subset of orientation tag values.
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
				default:
					break;
				}
			}
		}
		Log.e("degrees", "" + degree);
		return degree;
	}

}
