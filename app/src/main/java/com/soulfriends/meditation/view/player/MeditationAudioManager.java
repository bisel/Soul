package com.soulfriends.meditation.view.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.soulfriends.meditation.netservice.NetServiceManager;
import com.soulfriends.meditation.util.RecvEventListener;

import org.greenrobot.eventbus.EventBus;

public class MeditationAudioManager {

    private RecvEventListener recvEventListener;

    private static MeditationAudioManager instance = null;

    private static MeditationService service;

    private Context context;

    private boolean serviceBound;

    private boolean serviceBind = false;

    public boolean getServiceBound() {
        return serviceBound;
    }

    public void setReceivedEvent(RecvEventListener listener) {
        recvEventListener = listener;
    }

    private MeditationAudioManager(Context context) {
        this.context = context;
        serviceBound = false;
    }

    public static MeditationAudioManager getinstance(){
        // 반드시 값이 할당되어있다는 가정에서 사용해야 함.
        return instance;
    }

    public static MeditationAudioManager with(Context context) {

        if (instance == null)
            instance = new MeditationAudioManager(context);

        return instance;
    }

    public void release_service()
    {
        service = null;
    }

    public void SetThumbnail_Url(String url)
    {
        if (service == null) return;
        service.SetThumbnail_Url(url);
    }

    public void SetTitle_Url(String title)
    {
        if (service == null) return;
        service.SetTitle_Url(title);
    }


    public static MeditationService getService() {
        return service;
    }

    public static SimpleExoPlayer getplayerInstance() {
        return service.getplayerInstance();
    }

    public static void playOrPause(String streamUrl) {

        if (service == null) return;
        service.playOrPause(streamUrl);
    }

//    public void rew(){
//
//        service.playOrPause(streamUrl);
//    }
//
//    public void ffwd(){
//
//        service.playOrPause(streamUrl);
//    }

    public static boolean isPlaying() {

        if (service == null) return false;
        return service.isPlaying();
    }

    public static boolean isPlayingAndPause()
    {
        if (service == null) return false;
        return service.isPlayingAndPause();
    }

    public static void pause() {
        if (service == null) return;
        service.pause();
    }

    public static long getDuration() {
        if (service == null) return 0;
        return service.getDuration();
    }

    public static long getContentPosition() {
        if (service == null) return 0;
        return service.getContentPosition();
    }

    public static long getCurrentPosition() {
        if (service == null) return 0;
        return service.getCurrentPosition();
    }


    public static void resume() {

        if (service == null) return;
        service.resume();
    }

    public static void play() {
        if (service == null) return;
        service.play();
    }

    public static void stop() {
        if (service == null) return;
        service.stop();
    }

    public static void idle_start() {
        if (service == null) return;
        service.idle_start();
    }

    public void bind() {

        Intent intent = new Intent(context, MeditationService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        if (service != null)
            EventBus.getDefault().post(service.getStatus());
    }

    public void unbind() {

        if(serviceBind)
        {
            context.unbindService(serviceConnection);
            serviceBind = false;

            serviceBound = false;
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder binder) {

            service = ((MeditationService.LocalBinder) binder).getService();
            serviceBound = true;

            serviceBind = true;
            recvEventListener.onReceivedEvent();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

            serviceBound = false;
        }
    };

    public static void StartTimer(long second_time) {
        if (service == null) return;
        service.StartTimer(second_time);
    }

    public static void StopTimer() {
        if (service == null) return;
        service.StopTimer();
    }

}
