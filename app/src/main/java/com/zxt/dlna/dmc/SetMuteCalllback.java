package com.zxt.dlna.dmc;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.renderingcontrol.callback.SetMute;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SetMuteCalllback extends SetMute {

	private boolean desiredMute;

	private Handler handler;

	public SetMuteCalllback(Service paramService, boolean paramBoolean,
			Handler paramHandler) {
		super(paramService, paramBoolean);
		this.handler = paramHandler;
		this.desiredMute = paramBoolean;
	}

	public void failure(ActionInvocation paramActionInvocation,
			UpnpResponse paramUpnpResponse, String paramString) {
		Log.e("set mute failed", "set mute failed");
	}

	public void success(ActionInvocation paramActionInvocation) {
		Log.e("set mute success", "set mute success");
		if (desiredMute) {
			desiredMute = false;
		}
		Message localMessage = new Message();
		localMessage.what = DMCControlMessage.SETMUTESUC;
		Bundle localBundle = new Bundle();
		localBundle.putBoolean("mute", desiredMute);
		localMessage.setData(localBundle);
		this.handler.sendMessage(localMessage);
	}
}
