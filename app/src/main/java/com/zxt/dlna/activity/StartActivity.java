
package com.zxt.dlna.activity;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.zxt.dlna.application.BaseApplication;
import com.zxt.dlna.dms.ContentTree;
import com.zxt.dlna.util.FileUtil;
import com.zxt.dlna.util.ImageUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.widget.Toast;
import com.zxt.dlna.R;

public class StartActivity extends Activity {

    public static final int GET_IP_FAIL = 0;

    public static final int GET_IP_SUC = 1;

    private Context mContext;

//    private ProgressDialog progDialog = null;

    private String hostName;

    private String hostAddress;

    private List<Map<String, String>> mVideoFilePaths;

    private Handler mHandle = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_IP_FAIL: {
                    Toast.makeText(mContext, R.string.ip_get_fail, Toast.LENGTH_SHORT);
                    break;
                }
                case GET_IP_SUC: {
                    if (null != msg.obj) {
                        InetAddress inetAddress = (InetAddress) msg.obj;
                        if (null != inetAddress) {
                            setIp(inetAddress);
                            setIpInfo();
                            jumpToMain();
                        }
                    } else {
                        Toast.makeText(mContext, R.string.ip_get_fail, Toast.LENGTH_SHORT);
                    }
                    break;
                }

            }

            super.handleMessage(msg);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_lay);
        mContext = this;
        createFolder();
        getVideoFilePaths();
        createVideoThumb();
        getIp();
    }


    private void createFolder() {
        FileUtil.createSDCardDir(true);
    }

    private void getIp() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                WifiManager wifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();

                InetAddress inetAddress;
                Message message = new Message();
                try {
                    inetAddress = InetAddress.getByName(String.format("%d.%d.%d.%d",
                            (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
                            (ipAddress >> 24 & 0xff)));

                    hostName = inetAddress.getHostName();
                    hostAddress = inetAddress.getHostAddress();
                    message.obj = inetAddress;
                    message.what = GET_IP_SUC;
                    mHandle.sendMessage(message);
                } catch (UnknownHostException e) {
                    mHandle.sendEmptyMessage(GET_IP_FAIL);
                }
            }
        }).start();

    }

    private void getVideoFilePaths() {
        mVideoFilePaths = new ArrayList<Map<String, String>>();
        Cursor cursor;
        String[] videoColumns = {
                MediaStore.Video.Media._ID, MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DATA, MediaStore.Video.Media.ARTIST,
                MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DURATION, MediaStore.Video.Media.RESOLUTION
        };
        cursor = mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoColumns, null, null, null);
        if (null != cursor && cursor.moveToFirst()) {
            do {
                String id = ContentTree.VIDEO_PREFIX
                        + cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                String filePath = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                Map<String, String> fileInfoMap = new HashMap<String, String>();
                fileInfoMap.put(id, filePath);
                mVideoFilePaths.add(fileInfoMap);
                // Log.v(LOGTAG, "added video item " + title + "from " +
                // filePath);
            } while (cursor.moveToNext());
        }
        if (null != cursor) {
            cursor.close();
        }

    }

    private void createVideoThumb() {
        if (null != mVideoFilePaths && mVideoFilePaths.size() > 0) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    for (int i = 0; i < mVideoFilePaths.size(); i++) {
                        Set entries = mVideoFilePaths.get(i).entrySet();
                        if (entries != null) {
                            Iterator iterator = entries.iterator();
                            while (iterator.hasNext()) {
                                Map.Entry entry = (Entry) iterator.next();
                                Object id = entry.getKey();
                                Object filePath = entry.getValue();

                                Bitmap videoThumb = ImageUtil.getThumbnailForVideo(filePath
                                        .toString());
                                String videoSavePath = ImageUtil.getSaveVideoFilePath(
                                        filePath.toString(), id.toString());
                                try {
                                    ImageUtil.saveBitmapWithFilePathSuffix(videoThumb,
                                            videoSavePath);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

            }).start();
        }
    }

    private void setIp(InetAddress inetAddress) {
        BaseApplication.setLocalIpAddress(inetAddress);
    }

    private void setIpInfo() {
        BaseApplication.setHostName(hostName);
        BaseApplication.setHostAddress(hostAddress);
    }

    private void jumpToMain() {
        Intent intent = new Intent(StartActivity.this, IndexActivity.class);
        startActivity(intent);
        this.finish();
    }
}
