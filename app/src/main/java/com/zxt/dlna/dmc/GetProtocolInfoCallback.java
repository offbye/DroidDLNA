package com.zxt.dlna.dmc;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.connectionmanager.callback.GetProtocolInfo;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.ProtocolInfos;

import com.zxt.dlna.util.Utils;


import android.os.Handler;
import android.util.Log;

public class GetProtocolInfoCallback extends GetProtocolInfo {

	private String TAG = "GetProtocolInfoCallback";

	private Handler handler;

	private boolean hasType = false;

	private String requestPlayMimeType = "";

	public GetProtocolInfoCallback(Service paramService,
			ControlPoint paramControlPoint, String paramString,
			Handler paramHandler) {
		super(paramService, paramControlPoint);
		this.requestPlayMimeType = paramString;
		this.handler = paramHandler;
	}

	public void failure(ActionInvocation paramActionInvocation,
			UpnpResponse paramUpnpResponse, String paramString) {
		Log.e("DMC", "GetProtocolInfo  failure");
		this.handler.sendEmptyMessage(DMCControlMessage.CONNECTIONFAILED);
	}

	public void received(ActionInvocation paramActionInvocation,
			ProtocolInfos paramProtocolInfos1, ProtocolInfos paramProtocolInfos2) {
		this.handler.sendEmptyMessage(DMCControlMessage.CONNECTIONSUCESSED);
		// TODO

	}
}
