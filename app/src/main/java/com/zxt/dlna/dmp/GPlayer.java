
package com.zxt.dlna.dmp;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.zxt.dlna.R;
import com.zxt.dlna.util.Action;
import com.zxt.dlna.util.Utils;

import java.io.IOException;

public class GPlayer extends Activity implements OnCompletionListener, OnErrorListener,
        OnInfoListener, OnPreparedListener, OnSeekCompleteListener, OnVideoSizeChangedListener,
        SurfaceHolder.Callback, MediaController.MediaPlayerControl, OnClickListener {
    private static final int MEDIA_PLAYER_BUFFERING_UPDATE = 4001;

    private static final int MEDIA_PLAYER_COMPLETION = 4002;

    private static final int MEDIA_PLAYER_ERROR = 4003;

    private static final int MEDIA_PLAYER_INFO = 4004;

    private static final int MEDIA_PLAYER_PREPARED = 4005;

    private static final int MEDIA_PLAYER_PROGRESS_UPDATE = 4006;

    private static final int MEDIA_PLAYER_VIDEO_SIZE_CHANGED = 4007;

    private static final int MEDIA_PLAYER_VOLUME_CHANGED = 4008;

    private static final int MEDIA_PLAYER_HIDDEN_CONTROL = 4009;

    Display currentDisplay;

    SurfaceView surfaceView;

    SurfaceHolder surfaceHolder;

    MediaPlayer mMediaPlayer;

    MediaController mediaController;

    public static MediaListener mMediaListener;

    int videoWidth = 0;

    int videoHeight = 0;

    boolean readyToPlay = false;

    String playURI;

    private AudioManager mAudioManager;

    public final static String LOGTAG = "GPlayer";

    private TextView mTextViewTime;

    private SeekBar mSeekBarProgress;

    private TextView mTextViewLength;

    private ImageButton mPauseButton;

    private ProgressBar mProgressBarPreparing;

    private TextView mTextProgress;

    private TextView mTextInfo;

    private RelativeLayout mBufferLayout;

    private LinearLayout mLayoutBottom;

    private RelativeLayout mLayoutTop;

    private TextView mVideoTitle;

    private Button mLeftButton;

    private Button mRightButton;

    private ImageView mSound;

    private SeekBar mSeekBarSound;

    private volatile boolean mCanSeek = true;

    private boolean isMute;

    private int mBackCount;

    public static void setMediaListener(MediaListener mediaListener) {
        mMediaListener = mediaListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gplayer);
        mAudioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);

        surfaceView = (SurfaceView) findViewById(R.id.gplayer_surfaceview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);

        initControl();

        Intent intent = getIntent();
        playURI = intent.getStringExtra("playURI");
        if (!TextUtils.isEmpty(playURI)) {
            setUri(playURI);
        }

        setTitle(intent);
        currentDisplay = getWindowManager().getDefaultDisplay();

        registerBrocast();
    }

    private void setTitle(Intent intent) {
        String name = intent.getStringExtra("name");
        if (!TextUtils.isEmpty(name)) {
            mVideoTitle.setText(name);
        }
    }

    private void initControl() {
        mediaController = new MediaController(this);

        mBufferLayout = (RelativeLayout) findViewById(R.id.buffer_info);
        mProgressBarPreparing = (ProgressBar) findViewById(R.id.player_prepairing);
        mTextProgress = (TextView) findViewById(R.id.prepare_progress);
        mTextInfo = (TextView) findViewById(R.id.info);

        mLayoutTop = (RelativeLayout) findViewById(R.id.layout_top);
        mVideoTitle = (TextView) findViewById(R.id.video_title);
        mLeftButton = (Button) findViewById(R.id.topBar_back);
        mRightButton = (Button) findViewById(R.id.topBar_list_switch);
        mLeftButton.setOnClickListener(this);
        mRightButton.setOnClickListener(this);

        mTextViewTime = (TextView) findViewById(R.id.current_time);
        mTextViewLength = (TextView) findViewById(R.id.totle_time);
        mPauseButton = (ImageButton) findViewById(R.id.play);
        mPauseButton.setOnClickListener(this);
        mLayoutBottom = (LinearLayout) findViewById(R.id.layout_control);
        mTextProgress = (TextView) findViewById(R.id.prepare_progress);
        mTextInfo = (TextView) findViewById(R.id.info);

        mSeekBarProgress = (SeekBar) findViewById(R.id.seekBar_progress);
        mSeekBarProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int id = seekBar.getId();
                switch (id) {
                    case R.id.seekBar_progress:
                        if (mCanSeek) {
                            int position = seekBar.getProgress();
                            if (mMediaPlayer != null) {
                                mMediaPlayer.seekTo(position);
                            }
                        }
                        break;
                    default:
                        break;
                }

            }

        });

        mSound = (ImageView) findViewById(R.id.sound);
        mSound.setOnClickListener(this);
        mSeekBarSound = (SeekBar) findViewById(R.id.seekBar_sound);
        mSeekBarSound.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        mSeekBarSound.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        mSeekBarSound.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        playURI = intent.getStringExtra("playURI");
        if (!TextUtils.isEmpty(playURI)) {
            setUri(playURI);
        }
        setTitle(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        exit();
        unregisterBrocast();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mBackCount > 0) {
                exit();
            } else {
                mBackCount++;
                Toast.makeText(this, R.string.player_exit, Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_VOLUME_CHANGED, 100);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_VOLUME_CHANGED, 100);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mMediaListener = null;
        finish();
    }

    @Override
    public void onClick(View v) {
        // if (!mMediaPlayerLoaded)
        // return;
        int id = v.getId();
        switch (id) {
            case R.id.topBar_back:
                exit();

                break;
            case R.id.sound:
                isMute = !isMute;
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, isMute);
                if (isMute) {
                    mSound.setImageResource(R.drawable.phone_480_sound_mute);
                } else {
                    mSound.setImageResource(R.drawable.phone_480_sound_on);
                }
                break;
            case R.id.play: {
                doPauseResume();
                break;
            }
            default:
                break;
        }
    }

    private void updatePausePlay() {
        if (mMediaPlayer == null || mPauseButton == null) {
            return;
        }

        int resource = mMediaPlayer.isPlaying() ? R.drawable.button_pause
                : R.drawable.button_play;
        mPauseButton.setBackgroundResource(resource);
    }

    private void doPauseResume() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            if (null != mMediaListener) {
                mMediaListener.pause();
            }
        } else {
            mMediaPlayer.start();
            mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_PROGRESS_UPDATE, 200);

            if (null != mMediaListener) {
                mMediaListener.start();
            }
        }
        updatePausePlay();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            int visibility = mLayoutTop.getVisibility();
            if (visibility != View.VISIBLE) {
                mLayoutTop.setVisibility(View.VISIBLE);
                mLayoutBottom.setVisibility(View.VISIBLE);
            } else {
                mLayoutTop.setVisibility(View.GONE);
                mLayoutBottom.setVisibility(View.GONE);
            }

        }

        // if (mediaController.isShowing()) {
        // mediaController.hide();
        // } else {
        // mediaController.show(10000);
        // }
        return false;
    }

    public  int getAudioSessionId() {
        return 1;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v(LOGTAG, "surfaceChanged Called");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v(LOGTAG, "surfaceCreated Called");
        mMediaPlayer.setDisplay(holder);
        try {
            mMediaPlayer.prepare();
        } catch (IllegalStateException e) {
            Log.v(LOGTAG, "IllegalStateException", e);
        } catch (IOException e) {
            Log.v(LOGTAG, "IOException", e);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v(LOGTAG, "surfaceDestroyed Called");
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.v(LOGTAG, "onVideoSizeChanged Called");
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        Log.v(LOGTAG, "onSeekComplete Called");
        if (null != mMediaListener) {
            mMediaListener.endOfMedia();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.v(LOGTAG, "onPrepared Called");
        videoWidth = mp.getVideoWidth();
        videoHeight = mp.getVideoHeight();
        if (videoWidth > currentDisplay.getWidth() || videoHeight > currentDisplay.getHeight()) {
            float heightRatio = (float) videoHeight / (float) currentDisplay.getHeight();
            float widthRatio = (float) videoWidth / (float) currentDisplay.getWidth();
            if (heightRatio > 1 || widthRatio > 1) {
                if (heightRatio > widthRatio) {
                    videoHeight = (int) FloatMath.ceil((float) videoHeight / (float) heightRatio);
                    videoWidth = (int) FloatMath.ceil((float) videoWidth / (float) heightRatio);
                } else {
                    videoHeight = (int) FloatMath.ceil((float) videoHeight / (float) widthRatio);
                    videoWidth = (int) FloatMath.ceil((float) videoWidth / (float) widthRatio);
                }
            }
        }
        // surfaceView.setLayoutParams(new FrameLayout.LayoutParams(videoWidth,
        // videoHeight));
        mp.start();
        if (null != mMediaListener) {
            mMediaListener.start();
        }

        // mediaController.setMediaPlayer(this);
        // mediaController.setAnchorView(this.findViewById(R.id.gplayer_surfaceview));
        // mediaController.setEnabled(true);
        // mediaController.show(5000);
        mHandler.sendEmptyMessage(MEDIA_PLAYER_PREPARED);

        mHandler.sendEmptyMessage(MEDIA_PLAYER_PROGRESS_UPDATE);
        mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_HIDDEN_CONTROL, 10000);
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int whatInfo, int extra) {
        if (whatInfo == MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING) {
            Log.v(LOGTAG, "Media Info, Media Info Bad Interleaving " + extra);
        } else if (whatInfo == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
            Log.v(LOGTAG, "Media Info, Media Info Not Seekable " + extra);
        } else if (whatInfo == MediaPlayer.MEDIA_INFO_UNKNOWN) {
            Log.v(LOGTAG, "Media Info, Media Info Unknown " + extra);
        } else if (whatInfo == MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING) {
            Log.v(LOGTAG, "MediaInfo, Media Info Video Track Lagging " + extra);
        } else if (whatInfo == MediaPlayer.MEDIA_INFO_METADATA_UPDATE) {
            Log.v(LOGTAG, "MediaInfo, Media Info Metadata Update " + extra);
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.v(LOGTAG, "onCompletion Called");
        if (null != mMediaListener) {
            mMediaListener.endOfMedia();
        }

        exit();
    }

    @Override
    public boolean onError(MediaPlayer mp, int whatError, int extra) {
        Log.d(LOGTAG, "onError Called" + whatError + "  " + extra);
        if (whatError == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            Log.v(LOGTAG, "Media Error, Server Died " + extra);
        } else if (whatError == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            Log.v(LOGTAG, "Media Error, Error Unknown " + extra);
        }

        return false;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void setUri(String uri) {
        try {
            mMediaPlayer.reset();
            playURI = uri;
            mMediaPlayer.setDataSource(playURI);
        } catch (IllegalArgumentException e) {
            Log.v(LOGTAG, e.getMessage());
        } catch (IllegalStateException e) {
            Log.v(LOGTAG, e.getMessage());
        } catch (IOException e) {
            Log.v(LOGTAG, e.getMessage());
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            if (null != mMediaListener) {
                mMediaListener.pause();
            }
        }
    }

    @Override
    public void seekTo(int pos) {
        mMediaPlayer.seekTo(pos);
        if (null != mMediaListener) {
            mMediaListener.positionChanged(pos);
        }
    }

    @Override
    public void start() {

        try {
            mMediaPlayer.start();
            mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_PROGRESS_UPDATE, 200);

            if (null != mMediaListener) {
                mMediaListener.start();
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "start()", e);
        }
    }

    public void stop() {

        try {
            mMediaPlayer.stop();
            if (null != mMediaListener) {
                mMediaListener.stop();
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "stop()", e);
        }

    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d(LOGTAG, "msg=" + msg.what);
            switch (msg.what) {
                case MEDIA_PLAYER_BUFFERING_UPDATE: {

                    break;
                }
                case MEDIA_PLAYER_COMPLETION: {

                    break;
                }
                case MEDIA_PLAYER_ERROR: {

                    break;
                }
                case MEDIA_PLAYER_INFO: {

                    break;
                }
                case MEDIA_PLAYER_PREPARED: {
                    mBufferLayout.setVisibility(View.GONE);
                    break;
                }
                case MEDIA_PLAYER_PROGRESS_UPDATE: {
                    if (null == mMediaPlayer || !mMediaPlayer.isPlaying()) {
                        break;
                    }

                    int position = mMediaPlayer.getCurrentPosition();
                    int duration = mMediaPlayer.getDuration();
                    if (null != mMediaListener) {
                        mMediaListener.positionChanged(position);
                        mMediaListener.durationChanged(duration);
                    }

                    mTextViewLength.setText(Utils.secToTime(duration / 1000));
                    mSeekBarProgress.setMax(duration);
                    mTextViewTime.setText(Utils.secToTime(position / 1000));
                    mSeekBarProgress.setProgress(position);
                    mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_PROGRESS_UPDATE, 500);

                    break;
                }
                case MEDIA_PLAYER_VIDEO_SIZE_CHANGED: {

                    break;
                }
                case MEDIA_PLAYER_VOLUME_CHANGED: {
                    mSeekBarSound.setProgress(mAudioManager
                            .getStreamVolume(AudioManager.STREAM_MUSIC));
                    break;
                }
                case MEDIA_PLAYER_HIDDEN_CONTROL: {
                    mLayoutTop.setVisibility(View.GONE);
                    mLayoutBottom.setVisibility(View.GONE);
                    break;
                }
                default:
                    break;
            }
        }
    };

    private PlayBrocastReceiver playRecevieBrocast = new PlayBrocastReceiver();

    public void registerBrocast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Action.DMR);
        intentFilter.addAction(Action.VIDEO_PLAY);
        registerReceiver(this.playRecevieBrocast, intentFilter);
    }

    public void unregisterBrocast() {
        unregisterReceiver(this.playRecevieBrocast);
    }

    class PlayBrocastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String str1 = intent.getStringExtra("helpAction");

            if (str1.equals(Action.PLAY)) {
                start();
                updatePausePlay();
            } else if (str1.equals(Action.PAUSE)) {
                pause();
                updatePausePlay();
            } else if (str1.equals(Action.SEEK)) {
                boolean isPaused = false;
                if (!mMediaPlayer.isPlaying()) {
                    isPaused = true;
                }
                int position = intent.getIntExtra("position", 0);
                mMediaPlayer.seekTo(position);
                if (isPaused) {
                    pause();
                } else {
                    start();
                }

            } else if (str1.equals(Action.SET_VOLUME)) {
                int volume = (int) (intent.getDoubleExtra("volume", 0) * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) ;
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
                mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_VOLUME_CHANGED, 100);
            } else if (str1.equals(Action.STOP)) {
                stop();
            }

        }
    }

    public interface MediaListener {
        void pause();

        void start();

        void stop();

        void endOfMedia();

        void positionChanged(int position);

        void durationChanged(int duration);
    }

}
