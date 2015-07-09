package com.zxt.dlna.dmc;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.renderingcontrol.callback.GetMute;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GetMuteCallback extends GetMute {
	private Handler handler;

	public GetMuteCallback(Service paramService, Handler paramHandler) {
		super(paramService);
		this.handler = paramHandler;
	}

	public void failure(ActionInvocation paramActionInvocation,
			UpnpResponse paramUpnpResponse, String paramString) {
		Log.i("DMC", "get mute failed");
	}

	public void received(ActionInvocation paramActionInvocation,
			boolean paramBoolean) {
		Log.i("DMC", "get mute status:" + Boolean.toString(paramBoolean));
		Message localMessage = new Message();
		localMessage.what = DMCControlMessage.SETMUTE;
		Bundle localBundle = new Bundle();
		localBundle.putBoolean("mute", paramBoolean);
		localMessage.setData(localBundle);
		handler.sendMessage(localMessage);
	}

}
