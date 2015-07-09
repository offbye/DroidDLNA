
package com.zxt.dlna.dmc;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.model.TransportStatus;

import android.os.Handler;
import android.util.Log;

public class GetTransportInfoCallback extends GetTransportInfo {

    private Handler handler;

    private boolean isOnlyGetState;

    private int type;

    public GetTransportInfoCallback(Service paramService, Handler paramHandler,
            boolean paramBoolean, int paramInt) {
        super(paramService);
        this.handler = paramHandler;
        this.isOnlyGetState = paramBoolean;
        this.type = paramInt;
    }

    @Override
    public void failure(ActionInvocation paramActionInvocation, UpnpResponse paramUpnpResponse,
            String paramString) {
        if (this.type == 1) {
            this.handler.sendEmptyMessage(DMCControlMessage.PLAYIMAGEFAILED);
        } else if (this.type == 2) {
            this.handler.sendEmptyMessage(DMCControlMessage.PLAYAUDIOFAILED);
        } else if (this.type == 3) {
            this.handler.sendEmptyMessage(DMCControlMessage.PLAYVIDEOFAILED);
        }
    }

    @Override
    public void received(ActionInvocation paramActionInvocation, TransportInfo paramTransportInfo) {
        Log.e("GetTransportInfoCallback", "" + paramTransportInfo.getCurrentTransportState());
        Log.e("GetTransportInfoCallback", "" + paramTransportInfo.getCurrentTransportStatus());
        Log.e("isOnlyGetState", Boolean.toString(this.isOnlyGetState));
        handler.sendEmptyMessage(DMCControlMessage.SETURL);
        
        //TODO
        //XGF
        
        // if (!this.isOnlyGetState)
        // if (paramTransportInfo.getCurrentTransportState() ==
        // TransportState.NO_MEDIA_PRESENT
        // || paramTransportInfo.getCurrentTransportState() ==
        // TransportState.STOPPED
        // || paramTransportInfo.getCurrentTransportState() ==
        // TransportState.PAUSED_PLAYBACK) {
        // this.handler.sendEmptyMessage(DMCControlMessage.SETURL);
        // } else if (paramTransportInfo.getCurrentTransportState() ==
        // TransportState.PLAYING) {
        // this.handler.sendEmptyMessage(DMCControlMessage.STOP);
        // } else if (paramTransportInfo.getCurrentTransportStatus() ==
        // TransportStatus.CUSTOM) {
        // this.handler.sendEmptyMessage(DMCControlMessage.CONNECTIONFAILED);
        // } else if (paramTransportInfo.getCurrentTransportState() ==
        // TransportState.NO_MEDIA_PRESENT) {
        // this.handler.sendEmptyMessage(DMCControlMessage.REMOTE_NOMEDIA);
        // }
    }
}
