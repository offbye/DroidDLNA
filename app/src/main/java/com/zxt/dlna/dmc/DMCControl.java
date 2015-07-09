package com.zxt.dlna.dmc;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;

import com.zxt.dlna.activity.ControlActivity;
import com.zxt.dlna.dmp.DeviceItem;
import com.zxt.dlna.util.Action;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.zxt.dlna.R;

public class DMCControl {

	public static final int TYPE_IMAGE = 1;

	public static final int TYPE_AUDIO = 2;

	public static final int TYPE_VIDEO = 3;

	public static final int CUT_VOC = 0;

	public static final int ADD_VOC = 1;

	public static boolean isExit = false;

	private Activity activity;

	private int controlType = 1;

	int currentPlayPosition;

	private long currentVolume = 0L;

	private DeviceItem executeDeviceItem;

	public boolean isGetNoMediaPlay = false;

	public boolean isMute = false;

	private String metaData;

	String relTime;

	private boolean threadGetState = false;

	int totalPlayTime;

	String trackTime;

	private AndroidUpnpService upnpService;

	private String uriString;

	private Handler mHandle = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {

			case DMCControlMessage.ADDVOLUME: {

				break;
			}

			case DMCControlMessage.CONNECTIONFAILED: {

				break;
			}

			case DMCControlMessage.CONNECTIONSUCESSED: {
				getTransportInfo(false);
				break;
			}

			case DMCControlMessage.GETMUTE: {
				DMCControl.this.isMute = msg.getData().getBoolean("mute");
				DMCControl.this.setMuteToActivity(isMute);
				// getMute();
				break;
			}

			case DMCControlMessage.GETMEDIA: {

				break;
			}

			case DMCControlMessage.GETPOTITION: {
				getPositionInfo();

				// TODO
				if (!isExit && controlType != TYPE_IMAGE) {
					mHandle.sendEmptyMessageDelayed(
							DMCControlMessage.GETPOTITION, 500);
				}
				break;
			}

			case DMCControlMessage.GETTRANSPORTINFO: {

				break;
			}

			case DMCControlMessage.GET_CURRENT_VOLUME: {

				break;
			}

			case DMCControlMessage.PAUSE: {

				break;
			}

			case DMCControlMessage.PLAY: {
				mHandle.sendEmptyMessageDelayed(DMCControlMessage.GETPOTITION,
						500);
				play();
				break;
			}

			case DMCControlMessage.PLAYAUDIOFAILED: {

				break;
			}

			case DMCControlMessage.PLAYIMAGEFAILED: {

				break;
			}

			case DMCControlMessage.PLAYVIDEOFAILED: {

				break;
			}

			case DMCControlMessage.PLAYMEDIAFAILED: {
				setPlayErrorMessage();
				stopGetPosition();
				break;
			}
			case DMCControlMessage.REDUCEVOLUME: {

				break;
			}

			case DMCControlMessage.REMOTE_NOMEDIA: {

				break;
			}

			case DMCControlMessage.SETMUTE: {
				isMute = msg.getData().getBoolean("mute");
				setMute(!isMute);
				break;
			}

			case DMCControlMessage.SETMUTESUC: {
				isMute = msg.getData().getBoolean("mute");
				setMuteToActivity(isMute);
				break;
			}

			case DMCControlMessage.SETURL: {
				setAvURL();
				break;
			}

			case DMCControlMessage.SETVOLUME: {
				if (msg.getData().getInt("isSetVolume") == CUT_VOC) {
					setVolume(msg.getData().getLong("getVolume"), CUT_VOC);
				} else {
					setVolume(msg.getData().getLong("getVolume"), ADD_VOC);
				}
				break;
			}

			case DMCControlMessage.STOP: {

				break;
			}

			case DMCControlMessage.UPDATE_PLAY_TRACK: {

				break;
			}

			}
		}
	};

	public DMCControl(Activity paramActivity, int paramInt,
			DeviceItem paramDeviceItem,
			AndroidUpnpService paramAndroidUpnpService, String paramString1,
			String paramString2) {
		this.activity = paramActivity;
		this.controlType = paramInt;
		this.executeDeviceItem = paramDeviceItem;
		this.upnpService = paramAndroidUpnpService;
		this.uriString = paramString1;
		this.metaData = paramString2;
	}

	private void setPlayErrorMessage() {
		Intent localIntent = new Intent();
		if (this.controlType == TYPE_VIDEO) {
			localIntent.setAction(Action.PLAY_ERR_VIDEO);
		} else if (this.controlType == TYPE_AUDIO) {
			localIntent.setAction(Action.PLAY_ERR_AUDIO);
		} else {
			localIntent.setAction(Action.PLAY_ERR_IMAGE);
		}
		activity.sendBroadcast(localIntent);
	}

	private void stopGetPosition() {
		Message msg = new Message();
		msg.what = DMCControlMessage.GETPOTITION;
		msg.arg1 = 1;
		mHandle.sendMessage(msg);
	}

	public void getCurrentConnectionInfo(int paramInt) {
		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("ConnectionManager"));
			if (localService != null) {
				this.upnpService.getControlPoint().execute(
						new CurrentConnectionInfoCallback(localService,
								this.upnpService.getControlPoint(), paramInt));
			} else {
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void getDeviceCapability() {
		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("AVTransport"));
			if (localService != null) {
				this.upnpService.getControlPoint().execute(
						new GetDeviceCapabilitiesCallback(localService));
			} else {
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void getMediaInfo() {
		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("AVTransport"));
			if (localService != null) {
				this.upnpService.getControlPoint().execute(
						new GetMediaInfoCallback(localService));
			} else {
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void getMute() {
		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("RenderingControl"));
			if (localService != null) {
				this.upnpService.getControlPoint().execute(
						new GetMuteCallback(localService, mHandle));
			} else {
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void getPositionInfo() {
		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("AVTransport"));
			if (localService != null) {
				this.upnpService.getControlPoint().execute(
						new GetPositionInfoCallback(localService, mHandle,
								this.activity));
			} else {
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void getProtocolInfos(String paramString) {
		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("ConnectionManager"));
			if (localService != null) {
				this.upnpService.getControlPoint().execute(
						new GetProtocolInfoCallback(localService,
								this.upnpService.getControlPoint(),
								paramString, mHandle));
			} else {
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void getTransportInfo(boolean paramBoolean) {
		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("AVTransport"));
			if (localService != null) {
				this.upnpService.getControlPoint().execute(
						new GetTransportInfoCallback(localService, mHandle,
								paramBoolean, this.controlType));
			} else {
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void getVolume(int paramInt) {
		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("RenderingControl"));
			if (localService != null) {
				Log.e("get volume", "get volume");
				this.upnpService.getControlPoint().execute(
						new GetVolumeCallback(this.activity, mHandle, paramInt,
								localService, this.controlType));
			} else {
				Log.e("null", "null");
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void pause() {
		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("AVTransport"));
			if (localService != null) {
				Log.e("pause", "pause");
				this.upnpService.getControlPoint().execute(
						new PauseCallback(localService));
			} else {
				Log.e("null", "null");
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void play() {
		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("AVTransport"));
			if (localService != null) {
				Log.e("start play", "start play");
				this.upnpService.getControlPoint().execute(
						new PlayerCallback(localService, mHandle));
			} else {
				Log.e("null", "null");
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void rePlayControl() {
		if (this.isGetNoMediaPlay)
			return;
		this.isGetNoMediaPlay = true;
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(2000L);
					DMCControl.this.setAvURL();
					DMCControl.this.isGetNoMediaPlay = false;
					return;
				} catch (Exception localException) {
					localException.printStackTrace();
				}
			}
		}).start();
	}

	public void seekBarPosition(String paramString) {
		try {
			Device localDevice = this.executeDeviceItem.getDevice();
			Log.e("control action", "seekBarPosition");
			Service localService = localDevice.findService(new UDAServiceType(
					"AVTransport"));
			if (localService != null) {
				Log.e("get seekBarPosition info", "get seekBarPosition info");
				this.upnpService.getControlPoint().execute(
						new SeekCallback(activity, localService, paramString,
								mHandle));
			} else {
				Log.e("null", "null");
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void setAvURL() {
		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("AVTransport"));
			if (localService != null) {
				Log.e("set url", "set url" + this.uriString);
				this.upnpService.getControlPoint().execute(
						new SetAVTransportURIActionCallback(localService,
								this.uriString, this.metaData, mHandle,
								this.controlType));
			} else {
				Log.e("null", "null");
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void setCurrentPlayPath(String paramString) {
		uriString = paramString;
	}

	public void setCurrentPlayPath(String paramString1, String paramString2) {
		uriString = paramString1;
		metaData = paramString2;
	}

	public void setMute(boolean paramBoolean) {
		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("RenderingControl"));
			if (localService != null) {
				ControlPoint localControlPoint = this.upnpService
						.getControlPoint();
				localControlPoint.execute(new SetMuteCalllback(localService,
						paramBoolean, mHandle));
			} else {
				Log.e("null", "null");
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void setMuteToActivity(boolean paramBoolean) {
		if (activity instanceof ControlActivity) {
			((ControlActivity) activity).setVideoRemoteMuteState(paramBoolean);
		}
	}

	public void setVolume(long paramLong, int paramInt) {
		if (paramInt == 0) {
		}
		Service localService = null;
		try {
			localService = this.executeDeviceItem.getDevice().findService(
					new UDAServiceType("RenderingControl"));
			if (localService != null) {
				if (paramInt == CUT_VOC) {
					if (paramLong >= 0L) {
						paramLong -= 1L;
					} else {
						Toast.makeText(activity, R.string.min_voc,
								Toast.LENGTH_SHORT).show();
					}
				} else {
					paramLong += 1L;
				}
				this.upnpService.getControlPoint().execute(
						new SetVolumeCallback(localService, paramLong));
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void startThreadGetMessage() {
		Log.e("startThreadGetMessage", "startThreadGetMessage"
				+ this.threadGetState);
		DMCControlMessage.runing = true;
		if (!threadGetState)
			return;
		threadGetState = false;
		new Thread(new Runnable() {
			public void run() {
				try {
					boolean bool = DMCControlMessage.runing;
					if (!bool) {
						DMCControl.this.threadGetState = true;
						return;
					}
					Thread.sleep(1000L);
					DMCControl.this.getPositionInfo();
					// DMCControl.this.getTransportInfo(true);
				} catch (Exception localException) {
					localException.printStackTrace();
				}
			}
		}).start();
	}

	public void stop(Boolean paramBoolean) {
		try {
			Service localService = this.executeDeviceItem.getDevice()
					.findService(new UDAServiceType("AVTransport"));
			if (localService != null) {
				this.upnpService.getControlPoint().execute(
						new StopCallback(localService, mHandle, paramBoolean,
								this.controlType));
			} else {
				Log.e("null", "null");
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

}
