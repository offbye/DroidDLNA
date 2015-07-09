package com.zxt.dlna.dmc;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;

import android.util.Log;

public class SetVolumeCallback extends SetVolume{
	public SetVolumeCallback(Service paramService, long paramLong)
    {
      super(paramService, paramLong);
    }

    public void failure(ActionInvocation paramActionInvocation, UpnpResponse paramUpnpResponse, String paramString)
    {
      Log.e("set volume failed", "set volume failed");
    }

    public void success(ActionInvocation paramActionInvocation)
    {
      super.success(paramActionInvocation);
      Log.e("set volume success", "set volume success");
    }

}
