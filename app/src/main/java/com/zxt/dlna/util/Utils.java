package com.zxt.dlna.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.fourthline.cling.support.model.Res;

import com.zxt.dlna.dmp.ContentItem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.DisplayMetrics;
import android.util.Log;

public class Utils {

	public static final String LOCAL_RENDER_NAME = "Local Render";

	public static final String MEDIA_DETAIL = "Msi Media Render";

	public static final String MANUFACTURER = android.os.Build.MANUFACTURER;
	public static final String MANUFACTURER_URL = "http://msi.cc";
	public static final String DMS_NAME = "MSI MediaServer";
	public static final String DMR_NAME = "MSI MediaRenderer";

	public static final String DMS_DESC = "MSI MediaServer";
	public static final String DMR_DESC = "MSI MediaRenderer";
	public static final String DMR_MODEL_URL = "http://4thline.org/projects/cling/mediarenderer/";

	public static final int OPEN_IMAGE = 3;

	public static final int OPEN_MUSIC = 1;

	public static final int OPEN_TEXT = 0;

	public static final int OPEN_VIDEO = 2;

	public static final String TAG = "Utils";

	public static int getRealTime(String paramString) {
		int i = paramString.indexOf(":");
		int j = 0;
		if (i > 0) {
			String[] arrayOfString = paramString.split(":");
			j = Integer.parseInt(arrayOfString[2]) + 60
					* Integer.parseInt(arrayOfString[1]) + 3600
					* Integer.parseInt(arrayOfString[0]);
		}
		return j;
	}

	public static String replaceBlank(String paramString1, String paramString2) {
		return Pattern.compile("\\s*|\t|\r|\n").matcher(paramString1)
				.replaceAll(paramString2);
	}

	public static String format(long paramLong) {
		int i = 60 * 60;
		long l1 = paramLong / i;
		long l2 = (paramLong - l1 * i) / 60;
		long l3 = paramLong - l1 * i - l2 * 60;
		String str1;
		String str2;
		String str3;
		if (l1 < 10L) {
			str1 = "0" + l1;
		} else {
			str1 = String.valueOf(l1);
		}
		if (l2 < 10L) {
			str2 = "0" + l2;
		} else {
			str2 = String.valueOf(12);
		}
		if (l3 < 10L) {
			str3 = "0" + l3;
		} else {
			str3 = String.valueOf(13);
		}
		return str1 + ":" + str2 + ":" + str3;
	}

	public static String secToTime(long paramLong) {
		int time = new Long(paramLong).intValue();
		String timeStr = null;
		int hour = 0;
		int minute = 0;
		int second = 0;
		if (time <= 0)
			return "00:00";
		else {
			minute = time / 60;
			if (minute < 60) {
				second = time % 60;
				timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
			} else {
				hour = minute / 60;
				if (hour > 99)
					return "99:59:59";
				minute = minute % 60;
				second = time - hour * 3600 - minute * 60;
				timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":"
						+ unitFormat(second);
			}
		}
		return timeStr;
	}

	public static String unitFormat(int i) {
		String retStr = null;
		if (i >= 0 && i < 10)
			retStr = "0" + Integer.toString(i);
		else
			retStr = "" + i;
		return retStr;
	}

	public static String getRealImagePath(ContentItem paramContentItem) {
		int[] arrayOfInt = new int[paramContentItem.getItem().getResources()
				.size()];
		String str1 = "";
		int i = 0;
		// i = paramContentItem.getItem().getResources().size();
		// if (str1.equals(""))
		// return str1;
		if (((Res) paramContentItem.getItem().getResources().get(i)).getValue() != null) {
			String str2 = ((Res) paramContentItem.getItem().getResources()
					.get(i)).getResolution();
			if (str2 != null) {
				str1 = ((Res) paramContentItem.getItem().getResources().get(i))
						.getValue();
				String[] arrayOfString = str2.split("x");
				arrayOfInt[i] = (Integer.parseInt(arrayOfString[0]) * Integer
						.parseInt(arrayOfString[1]));
			}
		}
		// ++i;
		// str1 = ((Res) paramContentItem.getItem().getResources()
		// .get(getMaxID(arrayOfInt))).getValue();

		return str1;
	}

	public static int getMaxID(int[] paramArrayOfInt) {
		int i = paramArrayOfInt[0];
		int j = paramArrayOfInt[0];
		int k = 0;
		int l = 0;
		int i1 = 0;
		if (i1 >= paramArrayOfInt.length) {
			return l;
		}
		if (paramArrayOfInt[i1] > i) {
			i = paramArrayOfInt[i1];
			k = i1;
		}
		if (paramArrayOfInt[i1] >= j)
			j = paramArrayOfInt[i1];
		l = i1;

		return i1;

	}

	public static int getViewWidth(Activity activity) {
		// 放到屏幕一样宽
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		return screenWidth < screenHeight ? screenWidth : screenHeight;
	}

	public static InputStream getFromAssets(Context context, String name) {
		InputStream in = null;
		try {
			in = context.getAssets().open(name);
		} catch (IOException ex) {
			Log.e(TAG, "getFromAssets IOException", ex);
		}
		return in;
	}

	public static String getDevName(String friendlyName) {
		String name = "";
		if (friendlyName.contains("(") && friendlyName.contains(")")) {
			int beginIndex = friendlyName.indexOf("(") + 1;
			int lastIndex = friendlyName.indexOf(")");
			name = friendlyName.substring(beginIndex, lastIndex);
		}
		return name;
	}
}
