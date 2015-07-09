
package com.zxt.dlna.dmr;

import java.net.URI;
import java.util.Map;
import java.util.logging.Logger;

import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportErrorCode;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.AbstractAVTransportService;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PlayMode;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.SeekMode;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;
import org.seamless.http.HttpFetch;
import org.seamless.util.URIUtil;

import android.util.Log;

import com.zxt.dlna.util.Utils;

/**
 * @author offbye
 */
public class AVTransportService extends AbstractAVTransportService {

    final private static Logger log = Logger.getLogger(AVTransportService.class.getName());

    private static final String TAG = "GstAVTransportService";

    final private Map<UnsignedIntegerFourBytes, ZxtMediaPlayer> players;

    protected AVTransportService(LastChange lastChange, Map<UnsignedIntegerFourBytes, ZxtMediaPlayer> players) {
        super(lastChange);
        this.players = players;
    }

    protected Map<UnsignedIntegerFourBytes, ZxtMediaPlayer> getPlayers() {
        return players;
    }

    protected ZxtMediaPlayer getInstance(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        ZxtMediaPlayer player = getPlayers().get(instanceId);
        if (player == null) {
            throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID);
        }
        return player;
    }

    @Override
    public void setAVTransportURI(UnsignedIntegerFourBytes instanceId,
                                  String currentURI,
                                  String currentURIMetaData) throws AVTransportException {
        Log.d(TAG, currentURI + "---" +currentURIMetaData );
        URI uri;
        try {
            uri = new URI(currentURI);
        } catch (Exception ex) {
            throw new AVTransportException(
                    ErrorCode.INVALID_ARGS, "CurrentURI can not be null or malformed"
            );
        }

        if (currentURI.startsWith("http:")) {
            try {
                HttpFetch.validate(URIUtil.toURL(uri));
            } catch (Exception ex) {
                throw new AVTransportException(
                        AVTransportErrorCode.RESOURCE_NOT_FOUND, ex.getMessage()
                );
            }
        } else if (!currentURI.startsWith("file:")) {
            throw new AVTransportException(
                    ErrorCode.INVALID_ARGS, "Only HTTP and file: resource identifiers are supported"
            );
        }

        // TODO: Check mime type of resource against supported types
        // TODO: DIDL fragment parsing and handling of currentURIMetaData
        String type = "image";
        if (currentURIMetaData.contains("object.item.videoItem")) {
            type = "video";
        } else if (currentURIMetaData.contains("object.item.imageItem")) {
            type = "image";
        } else if (currentURIMetaData.contains("object.item.audioItem")) {
            type = "audio";
        }
        String name = currentURIMetaData.substring(currentURIMetaData.indexOf("<dc:title>") + 10,
                currentURIMetaData.indexOf("</dc:title>"));
        Log.d(TAG, name);

        getInstance(instanceId).setURI(uri,type,name,currentURIMetaData);
    }

    @Override
    public MediaInfo getMediaInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return getInstance(instanceId).getCurrentMediaInfo();
    }

    @Override
    public TransportInfo getTransportInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return getInstance(instanceId).getCurrentTransportInfo();
    }

    @Override
    public PositionInfo getPositionInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return getInstance(instanceId).getCurrentPositionInfo();
    }

    @Override
    public DeviceCapabilities getDeviceCapabilities(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId);
        return new DeviceCapabilities(new StorageMedium[]{StorageMedium.NETWORK});
    }

    @Override
    public TransportSettings getTransportSettings(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId);
        return new TransportSettings(PlayMode.NORMAL);
    }

    @Override
    public void stop(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId).stop();
    }

    @Override
    public void play(UnsignedIntegerFourBytes instanceId, String speed) throws AVTransportException {
        getInstance(instanceId).play();
    }

    @Override
    public void pause(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId).pause();
    }

    @Override
    public void record(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        // Not implemented
        log.info("### TODO: Not implemented: Record");
    }

    @Override
    public void seek(UnsignedIntegerFourBytes instanceId, String unit, String target) throws AVTransportException {
        final ZxtMediaPlayer player = getInstance(instanceId);
        SeekMode seekMode;
        try {
            seekMode = SeekMode.valueOrExceptionOf(unit);

            if (!seekMode.equals(SeekMode.REL_TIME)) {
                throw new IllegalArgumentException();
            }

//            final ClockTime ct = ClockTime.fromSeconds(ModelUtil.fromTimeString(target));
            int pos = (int) (Utils.getRealTime(target) * 1000);
            Log.i(TAG,"### " + unit + " target: "+ target +"  pos: " + pos);

//            if (getInstance(instanceId).getCurrentTransportInfo().getCurrentTransportState()
//                    .equals(TransportState.PLAYING)) {
//                getInstance(instanceId).pause();
//                getInstance(instanceId).seek(pos);
//                getInstance(instanceId).play();
//            } else if (getInstance(instanceId).getCurrentTransportInfo().getCurrentTransportState()
//                    .equals(TransportState.PAUSED_PLAYBACK)) {
                getInstance(instanceId).seek(pos);
//            }

        } catch (IllegalArgumentException ex) {
            throw new AVTransportException(
                    AVTransportErrorCode.SEEKMODE_NOT_SUPPORTED, "Unsupported seek mode: " + unit
            );
        }
    }

    @Override
    public void next(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        // Not implemented
        log.info("### TODO: Not implemented: Next");
    }

    @Override
    public void previous(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        // Not implemented
        log.info("### TODO: Not implemented: Previous");
    }

    @Override
    public void setNextAVTransportURI(UnsignedIntegerFourBytes instanceId,
                                      String nextURI,
                                      String nextURIMetaData) throws AVTransportException {
        log.info("### TODO: Not implemented: SetNextAVTransportURI");
        // Not implemented
    }

    @Override
    public void setPlayMode(UnsignedIntegerFourBytes instanceId, String newPlayMode) throws AVTransportException {
        // Not implemented
        log.info("### TODO: Not implemented: SetPlayMode");
    }

    @Override
    public void setRecordQualityMode(UnsignedIntegerFourBytes instanceId, String newRecordQualityMode) throws AVTransportException {
        // Not implemented
        log.info("### TODO: Not implemented: SetRecordQualityMode");
    }

    @Override
    protected TransportAction[] getCurrentTransportActions(UnsignedIntegerFourBytes instanceId) throws Exception {
        return getInstance(instanceId).getCurrentTransportActions();
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        UnsignedIntegerFourBytes[] ids = new UnsignedIntegerFourBytes[getPlayers().size()];
        int i = 0;
        for (UnsignedIntegerFourBytes id : getPlayers().keySet()) {
            ids[i] = id;
            i++;
        }
        return ids;
    }
}
