
package com.zxt.dlna.dmc;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo;
import org.fourthline.cling.support.model.MediaInfo;

public class GetMediaInfoCallback extends GetMediaInfo {

    public GetMediaInfoCallback(Service service) {
        super(service);
    }

    @Override
    public void received(ActionInvocation paramActionInvocation, MediaInfo paramMediaInfo) {
    }

    @Override
    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
        // TODO Auto-generated method stub

    }

    public void success(ActionInvocation paramActionInvocation) {
        super.success(paramActionInvocation);
    }
}
