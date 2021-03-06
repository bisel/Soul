package com.soulfriends.meditation.view.player;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.soulfriends.meditation.R;
import com.soulfriends.meditation.util.UtilAPI;

import org.greenrobot.eventbus.EventBus;

public class MeditationService extends Service implements Player.EventListener, AudioManager.OnAudioFocusChangeListener {

    public static final String ACTION_PLAY = "com.soulfriends.meditation.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.soulfriends.meditation.ACTION_PAUSE";
    public static final String ACTION_STOP = "com.soulfriends.meditation.ACTION_STOP";
    public static final String ACTION_REW = "com.soulfriends.meditation.ACTION_REW";
    public static final String ACTION_FFWD = "com.soulfriends.meditation.ACTION_FFWD";

    private final IBinder iBinder = new LocalBinder();

    private Context context;
    public Context getContext()
    {
        return context;
    }

    private Handler handler;
    private final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private SimpleExoPlayer exoPlayer;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    private boolean onGoingCall = false;
    private TelephonyManager telephonyManager;

    private WifiManager.WifiLock wifiLock;

    private AudioManager audioManager;

    private MeditationNotificationMgr notificationManager;

    private String status;

    private String strAppName;
    private String strLiveBroadcast;
    private String streamUrl;

    private String thumbnail_url;
    private String title;

    public void SetThumbnail_Url(String url)
    {
        this.thumbnail_url = url;
    }

    public void SetTitle_Url(String title)
    {
        this.title = title;
    }

    public class LocalBinder extends Binder {
        public MeditationService getService() {
            return MeditationService.this;
        }
    }

    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            pause();
        }
    };

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            if (state == TelephonyManager.CALL_STATE_OFFHOOK
                    || state == TelephonyManager.CALL_STATE_RINGING) {

                if (!isPlaying()) return;

                onGoingCall = true;
                stop();

            } else if (state == TelephonyManager.CALL_STATE_IDLE) {

                if (!onGoingCall) return;

                onGoingCall = false;
                resume();
            }
        }
    };

    private MediaSessionCompat.Callback mediasSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPause() {
            super.onPause();

            pause();
        }

        @Override
        public void onStop() {
            super.onStop();

            stop();

            EventBus.getDefault().post(PlaybackStatus.STOP_NOTI);

            notificationManager.cancelNotify();
        }

        @Override
        public void onPlay() {
            super.onPlay();

            resume();
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        strAppName = getResources().getString(R.string.app_name);
        strLiveBroadcast = getResources().getString(R.string.live_broadcast);

        context = getApplicationContext();

        onGoingCall = false;

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        notificationManager = new MeditationNotificationMgr(MeditationService.this);

        wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mcScPAmpLock");

        mediaSession = new MediaSessionCompat(this, getClass().getSimpleName());
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "...")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, strAppName)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, strLiveBroadcast)
                .build());
        mediaSession.setCallback(mediasSessionCallback);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        handler = new Handler();
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        AdaptiveTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        exoPlayer = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector);
        exoPlayer.addListener(this);


//        AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                .setUsage(C.USAGE_MEDIA)
//                .setContentType(C.CONTENT_TYPE_MUSIC)
//                .build();
//        exoPlayer.setAudioAttributes(audioAttributes, true);

        //setAudioFocus();

        registerReceiver(becomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

        status = PlaybackStatus.IDLE;
    }

    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:            //알수없는 기간동안 오디오 포커스 잃은 경우
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:  //일시적으로 오디오 포커스 빼앗긴 경우
                    pause();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN :           //오디오 포커스를 얻은 경우
                    //play();
                   // setAudioFocus();
                    break;
            }
        }
    };

    //오디오 포커스 관련
    private void setAudioFocus() {
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        int result = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);  //0이면 실패, 1이면 성공

        if (result == AudioManager.AUDIOFOCUS_GAIN) {  //성공시
            //play();
            //setAudioFocus();
        }else{  //실패시

            pause();
        }
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getAction();

        if (TextUtils.isEmpty(action))
            return START_NOT_STICKY;

        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

            stop();

            return START_NOT_STICKY;
        }

        if (action.equalsIgnoreCase(ACTION_PLAY)) {

            transportControls.play();
            EventBus.getDefault().post(PlaybackStatus.PLAYING_NOTI);

        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {


            transportControls.pause();
            EventBus.getDefault().post(PlaybackStatus.PAUSED_NOTI);

        } else if (action.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();

            if(UtilAPI.s_bEvent_service_main == false && UtilAPI.s_bEvent_service_player == false) {
                UtilAPI.s_bEvent_service_player_stop = true;
            }

            StopTimer();

        } else if (action.equalsIgnoreCase(ACTION_FFWD)) {
            transportControls.fastForward();
        } else if (action.equalsIgnoreCase(ACTION_REW)) {
            transportControls.rewind();
        }


        return START_NOT_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        if (status.equals(PlaybackStatus.IDLE))
            stopSelf();

        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(final Intent intent) {

    }

    @Override
    public void onDestroy() {

        pause();

        exoPlayer.release();
        exoPlayer.removeListener(this);

        if (telephonyManager != null)
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);

        notificationManager.cancelNotify();

        mediaSession.release();

        unregisterReceiver(becomingNoisyReceiver);

        super.onDestroy();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:

                exoPlayer.setVolume(0.8f);

                resume();

                break;

            case AudioManager.AUDIOFOCUS_LOSS:

                stop();

                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:

                if (isPlaying()) pause();

                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

                if (isPlaying())
                    exoPlayer.setVolume(0.1f);

                break;
        }

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        switch (playbackState) {
            case Player.STATE_BUFFERING:
                status = PlaybackStatus.LOADING;
                break;
            case Player.STATE_ENDED: {
                status = PlaybackStatus.STOPPED;


                }
                break;
            case Player.STATE_IDLE:
                status = PlaybackStatus.IDLE;
                break;
            case Player.STATE_READY:
                status = playWhenReady ? PlaybackStatus.PLAYING : PlaybackStatus.PAUSED;
                break;
            default:
                status = PlaybackStatus.IDLE;
                break;
        }

        if (!status.equals(PlaybackStatus.IDLE))
            notificationManager.startNotify(status, thumbnail_url, title);

        if(playWhenReady == true && playbackState == Player.STATE_ENDED)
        {
            status = PlaybackStatus.STOPPED_END;
            
            // main activity / player activity / 감정 activity  등 고려 해야함.
            // EventBus.getDefault().post(status); 를 보낼수 없을 경우에 해당 된다.
            if(UtilAPI.s_bEvent_service_main == false && UtilAPI.s_bEvent_service_player == false)
            {
                UtilAPI.s_bEvent_service = true;

                MeditationAudioManager.stop();
                MeditationAudioManager.getinstance().unbind();
            }
        }

        EventBus.getDefault().post(status);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        // 반복
        if(exoPlayer.getCurrentPosition() > 10) {
            EventBus.getDefault().post(PlaybackStatus.TRACK_CHANGE);

            if(UtilAPI.s_bEvent_service_main == false && UtilAPI.s_bEvent_service_player == false) {
                UtilAPI.s_player_timer_count++;
            }
        }

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

        EventBus.getDefault().post(PlaybackStatus.ERROR);
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    private MediaSource buildMediaSource(Uri uri) {
        String userAgent = Util.getUserAgent(this, "exoplayerfirebase");

        //new ExtractorMediaSource
        return new ProgressiveMediaSource.Factory(new DefaultHttpDataSourceFactory(userAgent))
                .createMediaSource(uri);
    }


    public void play(String streamUrl) {

        this.streamUrl = streamUrl;

        if (wifiLock != null && !wifiLock.isHeld()) {

            wifiLock.acquire();

        }

        MediaSource mediaSource = buildMediaSource(Uri.parse(streamUrl));

        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);

        //setAudioFocus();
    }

    public SimpleExoPlayer getplayerInstance() {
        return exoPlayer;
    }

    public void resume() {

        if (streamUrl != null)
            play(streamUrl);
    }

    public void play() {
        exoPlayer.setPlayWhenReady(true);

    }



    public void pause() {

        exoPlayer.setPlayWhenReady(false);

        audioManager.abandonAudioFocus(this);
        wifiLockRelease();
    }

    public void stop() {

        exoPlayer.stop();

        audioManager.abandonAudioFocus(this);
        wifiLockRelease();

        StopTimer();
    }

    public void idle_start() {
        exoPlayer.seekTo(0);
        exoPlayer.setPlayWhenReady(false);

        audioManager.abandonAudioFocus(this);
        wifiLockRelease();
    }

//    public void rew()
//    {
//        exoPlayer.();
//
//        audioManager.abandonAudioFocus(this);
//        wifiLockRelease();
//    }
//
//    public void ffwd()
//    {
//
//        exoPlayer.stop();
//
//        audioManager.abandonAudioFocus(this);
//        wifiLockRelease();
//    }


    public void playOrPause(String url) {

        if (streamUrl != null && streamUrl.equals(url)) {

            if (!isPlaying()) {

                play(streamUrl);

            } else {

                pause();
            }

        } else {

            if (isPlaying()) {

                pause();

            }

            play(url);
        }
    }

    public String getStatus() {

        return status;
    }

    public long getDuration() {
        return exoPlayer.getDuration();
    }

    public long getContentPosition() {
        return exoPlayer.getContentPosition();
    }

    public long getCurrentPosition() {
        return exoPlayer.getCurrentPosition();
    }


    public MediaSessionCompat getMediaSession() {

        return mediaSession;
    }

    public boolean isPlaying() {

        return this.status.equals(PlaybackStatus.PLAYING);
    }

    public boolean isPlayingAndPause()
    {
        boolean res = false;

        if(this.status.equals(PlaybackStatus.PLAYING))
        {
            res = true;
        }
        if(this.status.equals(PlaybackStatus.PAUSED))
        {
            res = true;
        }
        return res;
    }

    private void wifiLockRelease() {

        if (wifiLock != null && wifiLock.isHeld()) {

            wifiLock.release();
        }
    }

    private String getUserAgent() {

        return Util.getUserAgent(this, getClass().getSimpleName());
    }

    private Handler handlerUpdateLocation;
    private long second_time = 0;
    private boolean bOneEntryTimer = false;

    public void StartTimer(long second_time)
    {
        this.second_time = second_time * 1000;

        //https://jae-young.tistory.com/25
        if (handlerUpdateLocation == null) {
            handlerUpdateLocation = new Handler(Looper.getMainLooper());
            handlerUpdateLocation.post(runnableUpdateLocation);
            bOneEntryTimer = false;

            UtilAPI.s_player_timer_count = 0;
        }
    }

    public void StopTimer()
    {
        if (handlerUpdateLocation != null) {
            //https://pjsproject.tistory.com/191
            handlerUpdateLocation.removeMessages(0);
            handlerUpdateLocation = null;

            UtilAPI.s_player_timer_count = 0;
        }
    }

    private Runnable runnableUpdateLocation = new Runnable() {
        @Override
        public void run() {
            // TODO: You can send location here

            if(bOneEntryTimer) {

                bOneEntryTimer = false;

                if(UtilAPI.s_bEvent_service_main == false && UtilAPI.s_bEvent_service_player == false) {
                    UtilAPI.s_bEvent_service_player_timer_stop = true;
                }
                else
                {
                    status = PlaybackStatus.STOP_TIMER;
                    EventBus.getDefault().post(status);
                }

                MeditationAudioManager.stop();
                MeditationAudioManager.getinstance().unbind();

                StopTimer();
            }
            else {
                bOneEntryTimer = true;
                handlerUpdateLocation.postDelayed(this, second_time);
            }
        }
    };
}
