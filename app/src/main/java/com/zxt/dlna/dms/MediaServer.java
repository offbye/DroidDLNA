
package com.zxt.dlna.dms;

import java.io.IOException;
import java.net.URI;

import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;

import com.zxt.dlna.activity.SettingActivity;
import com.zxt.dlna.application.BaseApplication;
import com.zxt.dlna.util.FileUtil;
import com.zxt.dlna.util.UpnpUtil;
import com.zxt.dlna.util.Utils;

import android.content.Context;
import android.util.Log;

public class MediaServer {

    private UDN udn ;

    private LocalDevice localDevice;

    private final static String deviceType = "MediaServer";

    private final static int version = 1;

    private final static String LOGTAG = "MediaServer";

    public final static int PORT = 8192;
    private Context mContext;

    public MediaServer(Context context ) throws ValidationException {
        mContext = context;
        DeviceType type = new UDADeviceType(deviceType, version);

        DeviceDetails details = new DeviceDetails(SettingActivity.getDeviceName(context) + " ("
                + android.os.Build.MODEL + ")", new ManufacturerDetails(
                android.os.Build.MANUFACTURER), new ModelDetails(android.os.Build.MODEL,
                Utils.DMS_DESC, "v1"));

        LocalService service = new AnnotationLocalServiceBinder()
                .read(ContentDirectoryService.class);

        service.setManager(new DefaultServiceManager<ContentDirectoryService>(service,
                ContentDirectoryService.class));

        udn = UpnpUtil.uniqueSystemIdentifier("msidms");

        localDevice = new LocalDevice(new DeviceIdentity(udn), type, details, createDefaultDeviceIcon(), service);

        Log.v(LOGTAG, "MediaServer device created: ");
        Log.v(LOGTAG, "friendly name: " + details.getFriendlyName());
        Log.v(LOGTAG, "manufacturer: " + details.getManufacturerDetails().getManufacturer());
        Log.v(LOGTAG, "model: " + details.getModelDetails().getModelName());

        // start http server
        try {
            new HttpServer(PORT);
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
            System.exit(-1);
        }

        Log.v(LOGTAG, "Started Http Server on port " + PORT);
    }

    public LocalDevice getDevice() {
        return localDevice;
    }

    public String getAddress() {
        return BaseApplication.getHostAddress() + ":" + PORT;
    }

    protected Icon createDefaultDeviceIcon() {
        try {
            return new Icon("image/png", 48, 48, 32, "msi.png", mContext.getResources().getAssets()
                    .open(FileUtil.LOGO));
        } catch (IOException e) {
            Log.w(LOGTAG, "createDefaultDeviceIcon IOException");
            return null;
        }
    }
   
}
