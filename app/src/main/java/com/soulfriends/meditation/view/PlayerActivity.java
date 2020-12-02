package com.soulfriends.meditation.view;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.soulfriends.meditation.R;
import com.soulfriends.meditation.databinding.PlayerBinding;
import com.soulfriends.meditation.model.MeditationContents;
import com.soulfriends.meditation.model.UserProfile;
import com.soulfriends.meditation.netservice.NetServiceManager;
import com.soulfriends.meditation.util.PreferenceManager;
import com.soulfriends.meditation.util.RecvEventListener;
import com.soulfriends.meditation.util.ResultListener;
import com.soulfriends.meditation.util.UtilAPI;
import com.soulfriends.meditation.view.player.AudioPlayer;
import com.soulfriends.meditation.view.player.MeditationAudioManager;
import com.soulfriends.meditation.view.player.PlaybackStatus;
import com.soulfriends.meditation.viewmodel.PlayerViewModel;
import com.soulfriends.meditation.viewmodel.PlayerViewModelFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class PlayerActivity extends AppCompatActivity implements RecvEventListener, ResultListener {

    private PlayerControlView playerView;
    //private ProgressBar progressBar;
    //private SimpleExoPlayer simpleExoPlayer;

    MeditationAudioManager meditationAudioManager;

    private PlayerBinding binding;
    private PlayerViewModel viewModel;

    private ViewModelStore viewModelStore = new ViewModelStore();
    private PlayerViewModelFactory playerViewModelFactory;

    private MeditationContents meditationContents;

    private boolean bBookmark_state;

    private boolean bOneEntry_Stopped;

    private long mLastClickTime = 0;

    private String strPlaybackStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_player);
        binding.setLifecycleOwner(this);

        UtilAPI.s_bBookmark_update = false;


        meditationContents = NetServiceManager.getinstance().getCur_contents();

        if (playerViewModelFactory == null) {
            playerViewModelFactory = new PlayerViewModelFactory(meditationContents, this, this);
        }
        viewModel = new ViewModelProvider(this.getViewModelStore(), playerViewModelFactory).get(PlayerViewModel.class);
        binding.setViewModel(viewModel);

        // bg
        UtilAPI.load_image(this, meditationContents.bgimg, binding.ivBg1);

        // 플레이 화면에서 "나레이터 저자"순으로 보이면 1, "아티스트"만 보이면 2, 아무것도 안보이면 ,0
        if (meditationContents.showtype == 1) {
            binding.layoutTextTwo.setVisibility(View.VISIBLE);
            binding.layoutArtist.setVisibility(View.GONE);
        } else if (meditationContents.showtype == 2) {
            binding.layoutTextTwo.setVisibility(View.GONE);
            binding.layoutArtist.setVisibility(View.VISIBLE);
        } else {
            binding.layoutTextTwo.setVisibility(View.GONE);
            binding.layoutArtist.setVisibility(View.GONE);
        }

        meditationAudioManager = MeditationAudioManager.with(this);

        meditationAudioManager.setReceivedEvent(this);

        playerView = findViewById(R.id.player_view);


        FrameLayout ivStopEx = playerView.findViewById(R.id.stop_ex);
        ivStopEx.setOnClickListener(view -> {

            if (SystemClock.elapsedRealtime() - mLastClickTime < UtilAPI.button_delay_time) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

//            // 음악이 플레이중이면 정지하고 나가기
//            UtilAPI.player_play_duration = (int)(meditationAudioManager.getDuration() / 1000);
//            UtilAPI.player_play_time = (int)(meditationAudioManager.getContentPosition() / 1000);
//            UtilAPI.Calc_PlayerCheck();

            meditationAudioManager.stop();
            meditationAudioManager.unbind();

            finish();
            if(UtilAPI.s_bBookmark_update)
            {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }


            //Toast myToast = Toast.makeText(this,"hhhhhhh stop success", Toast.LENGTH_SHORT);
            //myToast.show();
        });

        binding.playerView.setVisibility(View.GONE);

        if (meditationAudioManager.isPlayingAndPause()) {
            onReceivedEvent();

            //Toast.makeText(getApplicationContext(),"playing",Toast.LENGTH_SHORT).show();
        }
        else
        {
            StopState();
        }

        // 좋아요 표시
        String uid = PreferenceManager.getString(this,"uid");
        int reactiionCode = NetServiceManager.getinstance().reqContentsFavoriteEvent(uid, meditationContents.uid);
        if(reactiionCode == 1)
        {
            // 좋아요
            // good 활성화
            UtilAPI.setButtonBackground(this, binding.btLike, R.drawable.like_btn_com);
        }
        else if(reactiionCode == 2)
        {
            // 별로예요
            UtilAPI.setButtonBackground(this, binding.btLike, R.drawable.dislike_btn_com);
        }

//        if (meditationContents.favoritecnt > 0) {
//
//            int state = 2;
//            if (state == 2) {
//                // 좋아요
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    binding.btLike.setBackground(ContextCompat.getDrawable(this, R.drawable.like_btn_com));
//                } else {
//                    binding.btLike.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.like_btn_com));
//                }
//            } else {
//                // 나빠요
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    binding.btLike.setBackground(ContextCompat.getDrawable(this, R.drawable.dislike_btn_com));
//                } else {
//                    binding.btLike.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dislike_btn_com));
//                }
//            }
//        }

        playerView.setRepeatToggleModes(1);
        playerView.setVisibility(View.VISIBLE);

        //playerView.setUseController(true);
        //playerView.showController();
        //playerView.setRepeatToggleModes(1);
        //playerView.showController(true);
        //playerView.setVisibility(View.VISIBLE);
        //playerView.setControllerAutoShow(true);
        //playerView.setControllerHideOnTouch(false);
    }

    private void play(boolean bPlay)
    {

        meditationAudioManager.SetTitle_Url(meditationContents.title);
        meditationAudioManager.SetThumbnail_Url(meditationContents.thumbnail_uri);

//        Glide.with(this)
//                .asBitmap()
//                .load(meditationContents.thumbnail_uri)
//                .into(new SimpleTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
//                        int xx = 0;
//                    }
//                });

        FirebaseStorage storage = FirebaseStorage.getInstance();

        //StorageReference storageRef = storage.getReferenceFromUrl("gs://meditation-m.appspot.com/test/play_bgm5.mp3");
        StorageReference storageRef = storage.getReferenceFromUrl(meditationContents.audio);

        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Download url of file

                String url = uri.toString();
                // String url = "http://bbcmedia.ic.llnwd.net/stream/bbcmedia_asianet_mf_p";

                if (meditationAudioManager.getServiceBound()) {
                    SimpleExoPlayer player = meditationAudioManager.getplayerInstance();
                    playerView.setPlayer(player);

                    if(bPlay) {


                        meditationAudioManager.playOrPause(url);


                    }
                    else
                    {
                        meditationAudioManager.pause();
                    }

                    //Toast.makeText(getApplicationContext(),"start play 777",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //Toast.makeText(getApplicationContext(),"서비스 바인딩 실패",Toast.LENGTH_SHORT).show();

                    meditationAudioManager.unbind();

                    meditationAudioManager.bind();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Log.i("TAG", e.getMessage());

               // Toast.makeText(getApplicationContext(),"failed play",Toast.LENGTH_SHORT).show();

                //play();
            }
        });
    }

    public void StopState()
    {
        if (meditationAudioManager.getServiceBound()) {
            SimpleExoPlayer player = meditationAudioManager.getplayerInstance();
            playerView.setPlayer(player);

            //Toast.makeText(getApplicationContext(),"서비스 바인딩 성공",Toast.LENGTH_SHORT).show();

            play(false);
        }
    }

    @Override
    public void onReceivedEvent() {

        if (meditationAudioManager.getServiceBound()) {
            SimpleExoPlayer player = meditationAudioManager.getplayerInstance();
            playerView.setPlayer(player);

            //Toast.makeText(getApplicationContext(),"서비스 바인딩 성공",Toast.LENGTH_SHORT).show();

            if (meditationAudioManager.isPlaying()) {

            }
            else
            {
                play(true);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mLastClickTime = 0;

        strPlaybackStatus = PlaybackStatus.IDLE;

        //UtilAPI.player_track_count = 0;

        EventBus.getDefault().register(this);

        //즐겨찾기 여부 : false - 즐겨찾기 안되어 있음. true - 즐겨찾기 되어있음

        bBookmark_state = NetServiceManager.getinstance().reqFavoriteContents(meditationContents.uid);
        if (bBookmark_state) {
            UtilAPI.setButtonBackground(this, binding.btBookmark, R.drawable.bookmark_btn_com);
        } else {
            UtilAPI.setButtonBackground(this, binding.btBookmark, R.drawable.bookmark_btn);
        }

        bOneEntry_Stopped = false;

        // 배경음 정지
        AudioPlayer.instance().pause();
    }

    @Override
    protected void onStop() {

        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

        meditationAudioManager.bind();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Subscribe
    public void onEvent(String status) {

        if(strPlaybackStatus == status) return;

        strPlaybackStatus = status;

        switch (status) {

            case PlaybackStatus.IDLE:
                //Toast.makeText(this, "플레이 아이들", Toast.LENGTH_SHORT).show();
                break;

            case PlaybackStatus.LOADING:
                break;

            case PlaybackStatus.STOPPED: {

                int xx = 0;
                //Toast.makeText(this, R.string.no_stream, Toast.LENGTH_SHORT).show();
            }
            break;
            case PlaybackStatus.STOP_NOTI: {

                meditationAudioManager.stop();
                meditationAudioManager.unbind();
                finish();
                //Toast.makeText(this, R.string.no_stream, Toast.LENGTH_SHORT).show();
            }
            break;

            case PlaybackStatus.TRACK_CHANGE: {
               // Toast.makeText(getApplicationContext(), "[메인] 리플레이", Toast.LENGTH_SHORT).show();

               // if (player_track_count > 0) {
                    NetServiceManager.getinstance().Update_UserProfile_Play((int)(MeditationAudioManager.getDuration() / 1000));

                    Toast.makeText(this, "세션 반복 1 증가", Toast.LENGTH_SHORT).show();
                //}
               // player_track_count++;
            }
            break;


            case PlaybackStatus.STOPPED_END: {

               // player_track_count = 0;

                // 음악이 완료 정지 된 경우에 들어옴
                NetServiceManager.getinstance().Update_UserProfile_Play((int)(MeditationAudioManager.getDuration() / 1000));

                Toast.makeText(this, "세션 1 증가", Toast.LENGTH_SHORT).show();

                MeditationAudioManager.stop();
                meditationAudioManager.unbind();

                // 플레이 위치 초기화
                meditationAudioManager.idle_start();

                // 세션으로 이동
                Intent intent = new Intent(this, SessioinActivity.class);
                startActivity(intent);

                finish();

            }
            break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onSuccess(Integer id, String message) {
        switch (id) {
            case R.id.bt_close: {

                // 나가기 버튼
                finish();

                if(UtilAPI.s_bBookmark_update)
                {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }


                //Toast.makeText(getApplicationContext(),"나가기 선택",Toast.LENGTH_SHORT).show();

            }
            break;
//            case R.id.bt_download: {
//                // 다운로드
//
//            }
//            break;
            case R.id.bt_info: {

//                int duration =  (int)(meditationAudioManager.getDuration() / 1000);
//
//                int xxx = 0;
//
//                int position =  (int)meditationAudioManager.getContentPosition();
//
//                int duration11 =  (int)(meditationAudioManager.getContentPosition() / 1000);
//
//                int yyy22 = 0;
//
//                int position11 =  (int)meditationAudioManager.getCurrentPosition();
//
//                int durationddd =  (int)(meditationAudioManager.getContentPosition() / 1000);
//
//                int yyy11 = 0;

                // 정보
                Intent intent = new Intent(this, ContentsinfoActivity.class);
                startActivity(intent);

            }
            break;
            case R.id.bt_bookmark: {
                // 북마크

                UtilAPI.s_bBookmark_update = true;

                if (bBookmark_state) {
                    NetServiceManager.getinstance().sendFavoriteContents(meditationContents.uid, false);
                    bBookmark_state = false;

                    UtilAPI.setButtonBackground(this, binding.btBookmark, R.drawable.bookmark_btn);
                } else {
                    // 말풍선 띄워준다.
                    NetServiceManager.getinstance().sendFavoriteContents(meditationContents.uid, true);
                    bBookmark_state = true;

                    UtilAPI.setButtonBackground(this, binding.btBookmark, R.drawable.bookmark_btn_com);

                    CallWithDelay_balloon(2000, this);
                }
            }
            break;
        }
    }

    @Override
    public void onFailure(Integer id, String message) {

    }

    public void CallWithDelay_balloon(long miliseconds, final Activity activity) {

        binding.layoutFavoriteBalloon.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    binding.layoutFavoriteBalloon.setVisibility(View.GONE);

                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }, miliseconds);
    }
}
