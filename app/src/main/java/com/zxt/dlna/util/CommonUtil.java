//from Allshare
package com.zxt.dlna.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

public class CommonUtil {

    private static final String TAG = "CommonUtil";

    public static boolean checkNetState(Context context) {
        boolean netstate = false;
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        netstate = true;
                        break;
                    }
                }
            }
        }
        return netstate;
    }

    public static void showToask(Context context, String tip) {
        Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();
    }

    public static boolean hasSDCard() {
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            // log.e("No sdcard");
            return false;
        }
        return true;
    }

    public static String getRootFilePath() {
        if (hasSDCard()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/";// filePath:/sdcard/
        } else {
            return Environment.getDataDirectory().getAbsolutePath() + "/data/"; // filePath:
                                                                                // /data/data/
        }
    }

    public static int getScreenWidth(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getWidth();
    }

    public static int getScreenHeight(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getHeight();
    }

    public static ViewSize getFitSize(Context context, MediaPlayer mediaPlayer) {
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        double fit1 = videoWidth * 1.0 / videoHeight;

        int width2 = getScreenWidth(context);
        int height2 = getScreenHeight(context);
        double fit2 = width2 * 1.0 / height2;

        Log.e(TAG, "videoWidth = " + videoWidth + ", videoHeight = " + videoHeight + ",fit1 = "
                + fit1);
        Log.e(TAG, "width2 = " + width2 + ", height2 = " + height2 + ",fit2 = " + fit2);

        double fit = 1;
        if (fit1 > fit2) {
            fit = width2 * 1.0 / videoWidth;
        } else {
            fit = height2 * 1.0 / videoHeight;
        }

        Log.d(TAG, "fit = " + fit);

        ViewSize viewSize = new ViewSize();
        viewSize.width = (int) (fit * videoWidth);
        viewSize.height = (int) (fit * videoHeight);

        return viewSize;
    }

    public static class ViewSize {
        public int width = 0;

        public int height = 0;
    }

}
