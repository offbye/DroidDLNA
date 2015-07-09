
package com.zxt.dlna.dmc;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.connectionmanager.callback.GetCurrentConnectionInfo;
import org.fourthline.cling.support.model.ConnectionInfo;

import android.util.Log;

public class CurrentConnectionInfoCallback extends GetCurrentConnectionInfo {
    private String TAG = "CurrentConnectionInfoCallback";

    public CurrentConnectionInfoCallback(Service paramService, ControlPoint paramControlPoint,
            int paramInt) {
        super(paramService, paramControlPoint, paramInt);
    }

    public void failure(ActionInvocation paramActionInvocation, UpnpResponse paramUpnpResponse,
            String paramString) {
        Log.e(this.TAG, "failed");
    }

    public void received(ActionInvocation paramActionInvocation, ConnectionInfo paramConnectionInfo) {
        Log.e(this.TAG, "" + paramConnectionInfo.getConnectionID());
        Log.e(this.TAG, "" + paramConnectionInfo.getConnectionStatus());
    }

}
