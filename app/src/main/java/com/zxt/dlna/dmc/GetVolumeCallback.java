package com.zxt.dlna.dmc;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GetVolumeCallback extends GetVolume {

	private Activity activity;

	private Handler handler;

	private int isSetVolumeFlag = 0;

	private int type;

	public GetVolumeCallback(Activity paramActivity, Handler paramHandler,
			int paramInt1, Service paramService, int paramInt2) {
		super(paramService);
		this.activity = paramActivity;
		this.handler = paramHandler;
		this.isSetVolumeFlag = paramInt1;
		this.type = paramInt2;
	}

	public void failure(ActionInvocation paramActionInvocation,
			UpnpResponse paramUpnpResponse, String paramString) {
		if (this.type == 1) {
			this.handler.sendEmptyMessage(DMCControlMessage.PLAYIMAGEFAILED);
		} else if (this.type == 2) {
			this.handler.sendEmptyMessage(DMCControlMessage.PLAYAUDIOFAILED);
		} else if (this.type == 3) {
			this.handler.sendEmptyMessage(DMCControlMessage.PLAYVIDEOFAILED);
		}
	}

	public void received(ActionInvocation paramActionInvocation, int paramInt) {
		Log.e("getcurrentvolume", "" + paramInt);
		Message localMessage = new Message();
		localMessage.what = DMCControlMessage.SETVOLUME;
		Bundle localBundle = new Bundle();
		localBundle.putLong("getVolume", paramInt);
		localBundle.putInt("isSetVolume", isSetVolumeFlag);
		localMessage.setData(localBundle);
		handler.sendMessage(localMessage);
	}

}
