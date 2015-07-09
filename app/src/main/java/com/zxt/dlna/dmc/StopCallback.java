
package com.zxt.dlna.dmc;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.Stop;

import android.os.Handler;

public class StopCallback extends Stop {

    private Handler handler;

    private Boolean isRePlay = false;

    private int type;

    public StopCallback(Service paramService, Handler paramHandler, Boolean paramBoolean,
            int paramInt) {
        super(paramService);
        this.handler = paramHandler;
        this.isRePlay = paramBoolean;
        this.type = paramInt;
    }

    public void failure(ActionInvocation paramActionInvocation, UpnpResponse paramUpnpResponse,
            String paramString) {
        if (this.type == 1)
            this.handler.sendEmptyMessage(DMCControlMessage.PLAYIMAGEFAILED);
        if (this.type == 2)
            this.handler.sendEmptyMessage(DMCControlMessage.PLAYAUDIOFAILED);
        if (this.type == 3)
            this.handler.sendEmptyMessage(DMCControlMessage.PLAYVIDEOFAILED);
    }

    public void success(ActionInvocation paramActionInvocation) {
        super.success(paramActionInvocation);
        if (!isRePlay.booleanValue()) {
            this.handler.sendEmptyMessage(DMCControlMessage.SETURL);
        } else {
            this.handler.sendEmptyMessage(DMCControlMessage.GETTRANSPORTINFO);
        }
    }

}
