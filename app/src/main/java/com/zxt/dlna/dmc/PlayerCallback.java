package com.zxt.dlna.dmc;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.Play;

import android.os.Handler;
import android.util.Log;

public class PlayerCallback extends Play {

	private Handler handler;

	public PlayerCallback(Service paramService, Handler paramHandler) {
		super(paramService);
		this.handler = paramHandler;
	}

	public void failure(ActionInvocation paramActionInvocation,
			UpnpResponse paramUpnpResponse, String paramString) {
		handler.sendEmptyMessage(DMCControlMessage.PLAYVIDEOFAILED);
		Log.e("play failed", "play failed");
	}

	public void run() {
		super.run();
	}

	public void success(ActionInvocation paramActionInvocation) {
		super.success(paramActionInvocation);
		Log.e("play success", "play success");
		handler.sendEmptyMessage(DMCControlMessage.GETMEDIA);
	}

}
