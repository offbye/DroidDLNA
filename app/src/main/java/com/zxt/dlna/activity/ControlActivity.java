package com.zxt.dlna.activity;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.fourthline.cling.android.AndroidUpnpService;

import com.zxt.dlna.application.BaseApplication;
import com.zxt.dlna.dmc.DMCControl;
import com.zxt.dlna.dmp.ContentItem;
import com.zxt.dlna.dmp.DeviceItem;
import com.zxt.dlna.util.Action;
import com.zxt.dlna.util.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.zxt.dlna.R;

public class ControlActivity extends Activity implements OnClickListener {

	public static boolean isplay = false;

	private final String TAG = "VideoControl";

	private SeekBar mSeekBar = null;

	private TextView mTotalTime;

	private TextView mCurrentTime;

	private TextView mNameTitle;

	private TextView mAuthorName;

	private ImageView mPlayBtn;

	private ImageView mVoicePlus;

	private ImageView mVoiceCut;

	private ImageView mVoiceMute;

	private String currentContentFormatMimeType = "";

	private DMCControl dmcControl = null;

	private DeviceItem dmrDeviceItem = null;

	private boolean isToMute = true;

	private boolean isUpdatePlaySeek = true;

	public ArrayList<ContentItem> listcontent;

	private String metaData;

	public String name;

	private String path;

	private int position;

	private Timer timer;

	private AndroidUpnpService upnpService = null;

	private ProgressDialog progDialog = null;

	private BroadcastReceiver updatePlayTime = new BroadcastReceiver() {
		public void onReceive(Context paramContext, Intent paramIntent) {
			if (paramIntent.getAction().equals(Action.PLAY_UPDATE)) {
				if (isUpdatePlaySeek) {
					Bundle localBundle = paramIntent.getExtras();
					String str1 = localBundle.getString("TrackDuration");
					String str2 = localBundle.getString("RelTime");
					int i = Utils.getRealTime(str1);
					int j = Utils.getRealTime(str2);
					mSeekBar.setMax(i);
					mSeekBar.setProgress(j);
					mTotalTime.setText(str1);
					mCurrentTime.setText(str2);
				}
				stopProgressDialog();
			}
			if (paramIntent.getAction().equals("com.transport.info")) {
				initData(paramIntent);
			}
			if (paramIntent.getAction().equals(Action.PLAY_ERR_VIDEO)
					|| paramIntent.getAction().equals(Action.PLAY_ERR_AUDIO)) {
				Toast.makeText(ControlActivity.this, R.string.media_play_err,
						Toast.LENGTH_SHORT).show();
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.control_lay);
		initView();

		IntentFilter localIntentFilter = new IntentFilter();
		localIntentFilter.addAction(Action.PLAY_UPDATE);
		localIntentFilter.addAction("com.video.play.error");
		localIntentFilter.addAction("com.connection.failed");
		localIntentFilter.addAction("com.transport.info");
		localIntentFilter.addAction(Action.PLAY_ERR_VIDEO);
		localIntentFilter.addAction(Action.PLAY_ERR_AUDIO);
		registerReceiver(this.updatePlayTime, localIntentFilter);

	}

	@Override
	protected void onResume() {
		DMCControl.isExit = false;
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		ControlActivity.this.unregisterReceiver(updatePlayTime);
		DMCControl.isExit = true;
		super.onDestroy();
	}

	private void initView() {
		mNameTitle = (TextView) findViewById(R.id.media_tv_title);
		mAuthorName = (TextView) findViewById(R.id.media_tv_author);

		mPlayBtn = (ImageView) findViewById(R.id.media_iv_play);
		mVoicePlus = (ImageView) findViewById(R.id.media_iv_voc_plus);
		mVoiceCut = (ImageView) findViewById(R.id.media_iv_voc_cut);
		mVoiceMute = (ImageView) findViewById(R.id.media_iv_voc_mute);
		mPlayBtn.setOnClickListener(this);
		mVoicePlus.setOnClickListener(this);
		mVoiceCut.setOnClickListener(this);
		mVoiceMute.setOnClickListener(this);
		mPlayBtn.setBackgroundResource(R.drawable.icon_media_pause);
		mVoiceMute.setBackgroundResource(R.drawable.icon_voc_mute);

		mCurrentTime = (TextView) findViewById(R.id.media_tv_time);
		mTotalTime = (TextView) findViewById(R.id.media_tv_total_time);

		mSeekBar = (SeekBar) findViewById(R.id.media_seekBar);
		mSeekBar.setOnSeekBarChangeListener(new PlaySeekBarListener());
	}

	private void initData(Intent localIntent) {
		if (null == localIntent) {
			Toast.makeText(this, getString(R.string.not_select_dev),
					Toast.LENGTH_SHORT).show();
			return;
		}
		path = localIntent.getStringExtra("playURI");
		name = localIntent.getStringExtra("name");
		currentContentFormatMimeType = localIntent
				.getStringExtra("currentContentFormatMimeType");
		metaData = localIntent.getStringExtra("metaData");

		mNameTitle.setText(name);
		mAuthorName.setText(name);

		if (null != path && null != currentContentFormatMimeType
				&& null != metaData) {

			isplay = true;
			startProgressDialog();
			// TODO get
			// mVideoThumb.setImageBitmap(Utils.getThumbnailForVideo(path));

			dmrDeviceItem = BaseApplication.dmrDeviceItem;
			upnpService = BaseApplication.upnpService;
			dmcControl = new DMCControl(this, 3, dmrDeviceItem,
					this.upnpService, this.path, this.metaData);
			dmcControl.getProtocolInfos(currentContentFormatMimeType);
		} else {
			Toast.makeText(this, getString(R.string.get_data_err),
					Toast.LENGTH_SHORT).show();
		}

	}

	private void startProgressDialog() {
		progDialog = new ProgressDialog(this);
		progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDialog.setIndeterminate(false);
		progDialog.setCancelable(true);
		progDialog.setMessage(getString(R.string.dialog_wait_msg));
	}

	private void stopProgressDialog() {
		if (null != progDialog) {
			progDialog.dismiss();
			progDialog = null;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.media_iv_play: {
			playPause();
			break;
		}
		case R.id.media_iv_voc_plus: {
			soundUp();
			break;
		}
		case R.id.media_iv_voc_cut: {
			soundDown();
			break;
		}
		case R.id.media_iv_voc_mute: {
			soundMute();
			break;
		}
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void playPause() {
		if (null != dmcControl) {
			if (isplay) {
				isplay = false;
				dmcControl.pause();
				mPlayBtn.setBackgroundResource(R.drawable.icon_media_play);
			} else {
				isplay = true;
				mPlayBtn.setBackgroundResource(R.drawable.icon_media_pause);
				dmcControl.play();
			}
		}
	}

	private void soundDown() {
		if (null != dmcControl) {
			dmcControl.getVolume(DMCControl.CUT_VOC);
		}
	}

	private void soundUp() {
		if (null != dmcControl) {
			dmcControl.getVolume(DMCControl.ADD_VOC);
		}
	}

	// if paramBoolean is True MUTE
	private void soundMute() {
		if (null != dmcControl) {
			dmcControl.getMute();
		}
	}

	public void setVideoRemoteMuteState(boolean paramBoolean) {
		Log.i("mute state", Boolean.toString(paramBoolean));
		isToMute = paramBoolean;
		if (!paramBoolean) {
			mVoiceMute.setBackgroundResource(R.drawable.icon_voc_mute);
		} else {
			mVoiceMute.setBackgroundResource(R.drawable.icon_voc_mute_click);
		}

	}

	class PlaySeekBarListener implements SeekBar.OnSeekBarChangeListener {
		PlaySeekBarListener() {
		}

		public void onProgressChanged(SeekBar paramSeekBar, int paramInt,
				boolean paramBoolean) {
		}

		public void onStartTrackingTouch(SeekBar paramSeekBar) {
			// isUpdatePlaySeek = false;
		}

		public void onStopTrackingTouch(SeekBar paramSeekBar) {
			if (null != dmcControl) {
				String str = Utils.secToTime(paramSeekBar.getProgress());
				Log.i("DMC", "SeekBar time:" + str);
				dmcControl.seekBarPosition(str);
			}
		}
	}

	// private void getCurrentPosition() {
	// final long beginTime = System.currentTimeMillis();
	// timer = new Timer();
	// timer.schedule(new TimerTask() {
	// @Override
	// public void run() {
	// dmcControl.getPositionInfo();
	// }
	// }, 100, 500);
	// }

}
