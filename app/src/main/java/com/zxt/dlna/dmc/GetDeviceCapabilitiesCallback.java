
package com.zxt.dlna.dmc;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.GetDeviceCapabilities;
import org.fourthline.cling.support.model.DeviceCapabilities;

import android.util.Log;

public class GetDeviceCapabilitiesCallback extends GetDeviceCapabilities {
    public GetDeviceCapabilitiesCallback(Service paramService) {
        super(paramService);
    }

    public void failure(ActionInvocation paramActionInvocation, UpnpResponse paramUpnpResponse,
            String paramString) {
    }

    public void received(ActionInvocation paramActionInvocation,
            DeviceCapabilities paramDeviceCapabilities) {
        Log.e("cap receive", "cap receive" + paramDeviceCapabilities.getPlayMediaString());
    }

}
