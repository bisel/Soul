package com.soulfriends.meditation.view.player;

import android.content.Context;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.soulfriends.meditation.util.PreferenceManager;

public class AudioPlayer {

    private static AudioPlayer instance = null;

    private Context context;

    public static AudioPlayer instance(){
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

    public void update()
    {
        boolean bSound_off = PreferenceManager.getBoolean(context, "sound_off");

        if (bSound_off) {
            // off 상태임.
        } else {
            if (AudioPlayer.instance().isPlayingAndPause()) {
                if (AudioPlayer.instance().isPlaying()) {

                } else {
                    AudioPlayer.instance().play();
                }
            }
        }
    }

    public void restart() {

        simpleExoPlayer.setPlayWhenReady(true);

        simpleExoPlayer.getPlaybackState();
    }

    public void pause()
    {
        if (simpleExoPlayer == null) {
            return;
        }
        simpleExoPlayer.setPlayWhenReady(false);

        simpleExoPlayer.getPlaybackState();
    }

    public void release()
    {
        if (simpleExoPlayer == null) {
            return;
        }

        simpleExoPlayer.release();
    }

    public void play()
    {
        if (simpleExoPlayer == null) {
            return;
        }


        simpleExoPlayer.setPlayWhenReady(true);

        simpleExoPlayer.getPlaybackState();
    }

    public void stop()
    {
        if (simpleExoPlayer == null) {
            return;
        }

        simpleExoPlayer.stop(true);
    }

    public boolean isPlaying() {

        return this.audio_state.equals(PlaybackStatus.PLAYING);
    }

    public boolean isPlayingAndPause()
    {
        boolean res = false;

        if(this.audio_state.equals(PlaybackStatus.PLAYING))
        {
            res = true;
        }
        if(this.audio_state.equals(PlaybackStatus.PAUSED))
        {
            res = true;
        }
        return res;
    }

    public void playSound(int sounRes, float volume)
    {

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
    }
}


