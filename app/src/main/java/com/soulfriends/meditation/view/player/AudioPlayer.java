package com.soulfriends.meditation.view.player;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.soulfriends.meditation.util.PreferenceManager;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;


public class AudioPlayer {

    private static AudioPlayer instance = null;

    private Context context;

    public static AudioPlayer instance() {
        return instance;
    }


    private SimpleExoPlayer simpleExoPlayer;

    private String audio_state;

    private AudioPlayer(Context context) {
        this.context = context;
    }

    public static AudioPlayer with(Context context) {

        if (instance == null)
            instance = new AudioPlayer(context);

        return instance;
    }

    public void update() {
        boolean bSound_off = PreferenceManager.getBoolean(context, "sound_off");

        if (bSound_off) {
            // off 상태임.
        } else {
            if(instance != null) {
                if (isPlayingAndPause()) {
                    if (isPlaying()) {
                    } else {
                        play();
                    }
                }
            }
        }
    }

    public void restart() {

        simpleExoPlayer.setPlayWhenReady(true);

        simpleExoPlayer.getPlaybackState();
    }


    public void pause() {
        if (simpleExoPlayer == null) {
            return;
        }
        simpleExoPlayer.setPlayWhenReady(false);

        simpleExoPlayer.getPlaybackState();
    }

    public void release() {
        if (simpleExoPlayer == null) {
            return;
        }

        stop();

        simpleExoPlayer.release();

        simpleExoPlayer = null;
    }

    public void play() {
        if (simpleExoPlayer == null) {
            return;
        }


        simpleExoPlayer.setPlayWhenReady(true);

        simpleExoPlayer.getPlaybackState();

        //setAudioFocus();
    }

    public void stop() {
        if (simpleExoPlayer == null) {
            return;
        }

        simpleExoPlayer.stop(true);
    }

    public boolean isPlaying() {

        if (simpleExoPlayer == null) {
            return false;
        }
        return this.audio_state.equals(PlaybackStatus.PLAYING);
    }

    public boolean isPlayingAndPause() {

        if (simpleExoPlayer == null) {
            return false;
        }

        boolean res = false;

        if (this.audio_state.equals(PlaybackStatus.PLAYING)) {
            res = true;
        }
        if (this.audio_state.equals(PlaybackStatus.PAUSED)) {
            res = true;
        }
        return res;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void playSound(int sounRes, float volume) {

        if (simpleExoPlayer == null) {

            simpleExoPlayer = new SimpleExoPlayer.Builder(context).build();
        }

        DataSpec dataSpec = new DataSpec(RawResourceDataSource.buildRawResourceUri(sounRes));
        final RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(context);
        try {
            rawResourceDataSource.open(dataSpec);
        } catch (RawResourceDataSource.RawResourceDataSourceException e) {
            e.printStackTrace();
        }

        DataSource.Factory factory = () -> rawResourceDataSource;
        ProgressiveMediaSource mediaSource = new ProgressiveMediaSource
                .Factory(factory)
                .createMediaSource(rawResourceDataSource.getUri());

        simpleExoPlayer.prepare(mediaSource);
        simpleExoPlayer.setPlayWhenReady(true);
        simpleExoPlayer.setVolume(volume);
        simpleExoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build();
        simpleExoPlayer.setAudioAttributes(audioAttributes, true);

        simpleExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        audio_state = PlaybackStatus.LOADING;
                        break;
                    case Player.STATE_ENDED:
                        audio_state = PlaybackStatus.STOPPED;
                        break;
                    case Player.STATE_IDLE:
                        audio_state = PlaybackStatus.IDLE;
                        break;
                    case Player.STATE_READY:
                        audio_state = playWhenReady ? PlaybackStatus.PLAYING : PlaybackStatus.PAUSED;
                        break;
                    default:
                        audio_state = PlaybackStatus.IDLE;
                        break;
                }

            }
        });

        //setAudioFocus();
    }

//    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
//        @Override
//        public void onAudioFocusChange(int focusChange) {
//            switch (focusChange) {
//                case AudioManager.AUDIOFOCUS_LOSS:            //알수없는 기간동안 오디오 포커스 잃은 경우
//                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:  //일시적으로 오디오 포커스 빼앗긴 경우
//                    pause();
//
//                    break;
//                case AudioManager.AUDIOFOCUS_GAIN:           //오디오 포커스를 얻은 경우
//                {
//                    int xx = 0;
//                }
//                break;
//            }
//        }
//    };
//
//    //오디오 포커스 관련
//    private void setAudioFocus() {
//        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//
//        int result = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);  //0이면 실패, 1이면 성공
//
//        if (result == AudioManager.AUDIOFOCUS_GAIN) {  //성공시
//            //play();
//
//            int xx = 0;
//        } else {  //실패시
//
//            pause();
//        }
//    }
}


