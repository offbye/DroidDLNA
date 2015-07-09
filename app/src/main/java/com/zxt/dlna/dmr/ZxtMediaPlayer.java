
package com.zxt.dlna.dmr;

import java.net.URI;
import java.util.logging.Logger;

import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelMute;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable;

import com.zxt.dlna.dmp.GPlayer;
import com.zxt.dlna.dmp.GPlayer.MediaListener;
import com.zxt.dlna.util.Action;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

/**
 * @author offbye
 */
public class ZxtMediaPlayer {

    final private static Logger log = Logger.getLogger(ZxtMediaPlayer.class.getName());

    private static final String TAG = "GstMediaPlayer";

    final private UnsignedIntegerFourBytes instanceId;
    final private LastChange avTransportLastChange;
    final private LastChange renderingControlLastChange;

//    final private VideoComponent videoComponent = new VideoComponent();

    // We'll synchronize read/writes to these fields
    private volatile TransportInfo currentTransportInfo = new TransportInfo();
    private PositionInfo currentPositionInfo = new PositionInfo();
    private MediaInfo currentMediaInfo = new MediaInfo();
    private double storedVolume;
    
    private Context mContext;

    public ZxtMediaPlayer(UnsignedIntegerFourBytes instanceId,Context context,
                          LastChange avTransportLastChange,
                          LastChange renderingControlLastChange) {
        super();
        this.instanceId = instanceId;
        this.mContext = context;
        this.avTransportLastChange = avTransportLastChange;
        this.renderingControlLastChange = renderingControlLastChange;

        try {
            // Disconnect the old bus listener
            /* TODO: That doesn't work for some reason...
            getPipeline().getBus().disconnect(
                    (Bus.STATE_CHANGED) Reflections.getField(getClass(), "stateChanged").get(this)
            );
            */

            // Connect a fixed bus state listener
//            getPipeline().getBus().connect(busStateChanged);

            // Connect a bus tag listener
//            getPipeline().getBus().connect(busTag);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

//        addMediaListener(new GstMediaListener());

//        setVideoSink(videoComponent.getElement());
    }

    public UnsignedIntegerFourBytes getInstanceId() {
        return instanceId;
    }

    public LastChange getAvTransportLastChange() {
        return avTransportLastChange;
    }

    public LastChange getRenderingControlLastChange() {
        return renderingControlLastChange;
    }

//    public VideoComponent getVideoComponent() {
//        return videoComponent;
//    }

    // TODO: gstreamer-java has a broken implementation of getStreamInfo(), so we need to
    // do our best fishing for the stream type inside the playbin pipeline

    /*
    synchronized public boolean isDecodingStreamType(String prefix) {
        for (Element element : getPipeline().getElements()) {
            if (element.getName().matches("decodebin[0-9]+")) {
                for (Pad pad : element.getPads()) {
                    if (pad.getName().matches("src[0-9]+")) {
                        Caps caps = pad.getNegotiatedCaps();
                        Structure struct = caps.getStructure(0);
                        if (struct.getName().startsWith(prefix + "/"))
                            return true;
                    }
                }
            }
        }
        return false;
    } */

    synchronized public TransportInfo getCurrentTransportInfo() {
        return currentTransportInfo;
    }

    synchronized public PositionInfo getCurrentPositionInfo() {
        return currentPositionInfo;
    }

    synchronized public MediaInfo getCurrentMediaInfo() {
        return currentMediaInfo;
    }

   // @Override
    synchronized public void setURI(URI uri, String type, String name, String currentURIMetaData) {
        Log.i(TAG, "setURI " + uri);

        currentMediaInfo = new MediaInfo(uri.toString(),currentURIMetaData);
        currentPositionInfo = new PositionInfo(1, "", uri.toString());

        getAvTransportLastChange().setEventedValue(getInstanceId(),
                new AVTransportVariable.AVTransportURI(uri),
                new AVTransportVariable.CurrentTrackURI(uri));

        transportStateChanged(TransportState.STOPPED);
        
        GPlayer.setMediaListener(new GstMediaListener());
        
        Intent intent = new Intent();
        intent.setClass(mContext, RenderPlayerService.class);
        intent.putExtra("type", type);
        intent.putExtra("name", name);
        intent.putExtra("playURI", uri.toString());
        mContext.startService(intent);
    }

//    @Override
    synchronized public void setVolume(double volume) {
        Log.i(TAG,"setVolume " + volume);
        storedVolume = getVolume();
        
        Intent intent = new Intent();
        intent.setAction(Action.DMR);
        intent.putExtra("helpAction", Action.SET_VOLUME);
        intent.putExtra("volume", volume);

        mContext.sendBroadcast(intent);        
        
        ChannelMute switchedMute =
                (storedVolume == 0 && volume > 0) || (storedVolume > 0 && volume == 0)
                        ? new ChannelMute(Channel.Master, storedVolume > 0 && volume == 0)
                        : null;

        getRenderingControlLastChange().setEventedValue(
                getInstanceId(),
                new RenderingControlVariable.Volume(
                        new ChannelVolume(Channel.Master, (int) (volume * 100))
                ),
                switchedMute != null
                        ? new RenderingControlVariable.Mute(switchedMute)
                        : null
        );
    }

    synchronized public void setMute(boolean desiredMute) {
        if (desiredMute && getVolume() > 0) {
            log.fine("Switching mute ON");
            setVolume(0);
        } else if (!desiredMute && getVolume() == 0) {
            log.fine("Switching mute OFF, restoring: " + storedVolume);
            setVolume(storedVolume);
        }
    }

    // Because we don't have an automated state machine, we need to calculate the possible transitions here

    synchronized public TransportAction[] getCurrentTransportActions() {
        TransportState state = currentTransportInfo.getCurrentTransportState();
        TransportAction[] actions;

        switch (state) {
            case STOPPED:
                actions = new TransportAction[]{
                        TransportAction.Play
                };
                break;
            case PLAYING:
                actions = new TransportAction[]{
                        TransportAction.Stop,
                        TransportAction.Pause,
                        TransportAction.Seek
                };
                break;
            case PAUSED_PLAYBACK:
                actions = new TransportAction[]{
                        TransportAction.Stop,
                        TransportAction.Pause,
                        TransportAction.Seek,
                        TransportAction.Play
                };
                break;
            default:
                actions = null;
        }
        return actions;
    }

    // Can't disconnect the broken bus listener, these funny methods disable it
/*
    protected void fireStartEvent(StartEvent ev) {
    }

    protected void fireStartEventFix(StartEvent ev) {
        for (MediaListener l : getMediaListeners()) {
            l.start(ev);
        }
    }

    protected void fireStopEvent(StopEvent ev) {

    }

    protected void fireStopEventFix(StopEvent ev) {
        for (MediaListener l : getMediaListeners()) {
            l.stop(ev);
        }
    }

    protected void firePauseEvent(PauseEvent ev) {

    }

    protected void firePauseEventFix(PauseEvent ev) {
        for (MediaListener l : getMediaListeners()) {
            l.pause(ev);
        }
    }

    // TODO: The gstreamer-java folks don't seem to understand their code very well, nobody knew what
    // I was talking about when I mentioned "transitioning" as a new callback for the listener... so yes,
    // this hack is still necessary.
    private final Bus.STATE_CHANGED busStateChanged = new Bus.STATE_CHANGED() {
        public void stateChanged(GstObject source, State old, State newState, State pending) {
            if (!source.equals(getPipeline())) return;
            log.fine("GST pipeline changed state from " + old + " to " + newState + ", pending: " + pending);
            final ClockTime position = getPipeline().queryPosition();
            if (newState.equals(State.PLAYING) && old.equals(State.PAUSED)) {
                fireStartEventFix(new StartEvent(GstMediaPlayer.this, old, newState, State.VOID_PENDING, position));
            } else if (newState.equals(State.PAUSED) && pending.equals(State.VOID_PENDING)) {
                firePauseEventFix(new PauseEvent(GstMediaPlayer.this, old, newState, State.VOID_PENDING, position));
            } else if (newState.equals(State.READY) && pending.equals(State.NULL)) {
                fireStopEventFix(new StopEvent(GstMediaPlayer.this, old, newState, pending, position));
            }

            // Anything else means we are transitioning!
            if (!pending.equals(State.VOID_PENDING) && !pending.equals(State.NULL))
                transportStateChanged(TransportState.TRANSITIONING);
        }
    };
*/
    synchronized protected void transportStateChanged(TransportState newState) {
        TransportState currentTransportState = currentTransportInfo.getCurrentTransportState();
        log.fine("Current state is: " + currentTransportState + ", changing to new state: " + newState);
        currentTransportInfo = new TransportInfo(newState);

        getAvTransportLastChange().setEventedValue(
                getInstanceId(),
                new AVTransportVariable.TransportState(newState),
                new AVTransportVariable.CurrentTransportActions(getCurrentTransportActions())
        );
    }

    protected class GstMediaListener implements MediaListener {
        public void pause() {
            transportStateChanged(TransportState.PAUSED_PLAYBACK);
        }

        public void start() {
            transportStateChanged(TransportState.PLAYING);
        }

        public void stop() {
            transportStateChanged(TransportState.STOPPED);
        }

        public void endOfMedia() {
            log.fine("End Of Media event received, stopping media player backend");
            transportStateChanged(TransportState.NO_MEDIA_PRESENT);
            //GstMediaPlayer.this.stop();
        }

        public void positionChanged(int position) {
            log.fine("Position Changed event received: " + position);
            synchronized (ZxtMediaPlayer.this) {
                currentPositionInfo = new PositionInfo(1, currentMediaInfo.getMediaDuration(),
                        currentMediaInfo.getCurrentURI(), ModelUtil.toTimeString(position/1000),
                        ModelUtil.toTimeString(position/1000));
            }
        }

        public void durationChanged(int duration) {
            log.fine("Duration Changed event received: " + duration);
            synchronized (ZxtMediaPlayer.this) {
                String newValue = ModelUtil.toTimeString(duration/1000);
                currentMediaInfo = new MediaInfo(currentMediaInfo.getCurrentURI(), "",
                        new UnsignedIntegerFourBytes(1), newValue, StorageMedium.NETWORK);

                getAvTransportLastChange().setEventedValue(getInstanceId(),
                        new AVTransportVariable.CurrentTrackDuration(newValue),
                        new AVTransportVariable.CurrentMediaDuration(newValue));
            }
        }
    } 
    
    public double getVolume() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Service.AUDIO_SERVICE);
        double v =  (double)audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.i(TAG, "getVolume " + v);
        return v;
    }
    
    public void play() {
        Log.i(TAG,"play");
        sendBroadcastAction(Action.PLAY);
    }

    public void pause() {
        Log.i(TAG,"pause");
        sendBroadcastAction(Action.PAUSE);
    }

    public void stop() {
        Log.i(TAG,"stop");
        sendBroadcastAction(Action.STOP);
    }
    
    public void seek(int position) {
        Log.i(TAG,"seek " +  position);
        Intent intent = new Intent();
        intent.setAction(Action.DMR);
        intent.putExtra("helpAction", Action.SEEK);
        intent.putExtra("position", position);
        mContext.sendBroadcast(intent);
    }
    
    public void sendBroadcastAction(String action) {
        Intent intent = new Intent();
        intent.setAction(Action.DMR);
        intent.putExtra("helpAction", action);
        mContext.sendBroadcast(intent);
    }
}

