package com.soulfriends.meditation.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.soulfriends.meditation.R;
import com.soulfriends.meditation.databinding.MainBinding;
import com.soulfriends.meditation.dlg.PsychologyDlg;
import com.soulfriends.meditation.model.MeditationContents;
import com.soulfriends.meditation.model.UserProfile;
import com.soulfriends.meditation.netservice.NetServiceManager;
import com.soulfriends.meditation.parser.ResultData;
import com.soulfriends.meditation.util.PreferenceManager;
import com.soulfriends.meditation.util.ResultListener;
import com.soulfriends.meditation.util.UtilAPI;
import com.soulfriends.meditation.view.player.AudioPlayer;
import com.soulfriends.meditation.view.player.MeditationAudioManager;
import com.soulfriends.meditation.view.player.PlaybackStatus;
import com.soulfriends.meditation.viewmodel.MainViewModel;
import com.soulfriends.meditation.viewmodel.MainViewModelFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity  implements ResultListener {

    private MainBinding binding;
    private MainViewModel viewModel;
    private ViewModelStore viewModelStore = new ViewModelStore();
    private MainViewModelFactory mainViewModelFactory;

    private HomeFragment homeFragment = new HomeFragment();
    private SleepFragment sleepFragment = new SleepFragment();
    private MeditationFragment meditationFragment = new MeditationFragment();
    private MusicFragment musicFragment = new MusicFragment();
    private ProfileFragment profileFragment = new ProfileFragment();

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListner;

    private MeditationContents meditationContents;

    MeditationAudioManager meditationAudioManager;

    private ArrayList<ResultData> resultData_list;

    private boolean bShowMiniPlayer;

    private String strPlaybackStatus;

    private boolean miniPlaying;

    @Override
    protected void onStart() {
        super.onStart();

        UtilAPI.s_bEvent_service_player_stop = false;

        // service onevent PlaybackStatus.STOPPED_END 체크
        if(UtilAPI.s_bEvent_service)
        {
            UtilAPI.s_bEvent_service = false;

            //PlaybackStatus.STOPPED_END 체크되어 session으로 이동 처리

            // 플레이 위치 초기화
            MeditationAudioManager.idle_start();

            // 프레임 레이어 조절
            UtilAPI.setMarginBottom(this, binding.container,0);

            binding.miniLayout.setVisibility(View.GONE);


            // 세션 카운트와 플레이 시간

            long duration = MeditationAudioManager.getDuration() / 1000;

            NetServiceManager.getinstance().Update_UserProfile_Play((int) (MeditationAudioManager.getDuration() / 1000));

            UserProfile userProfile = NetServiceManager.getinstance().getUserProfile();
            NetServiceManager.getinstance().setOnRecvValProfileListener(new NetServiceManager.OnRecvValProfileListener() {
                @Override
                public void onRecvValProfile(boolean validate) {
                    if (validate == true) {
                        int xx = 0;
                    } else {
                        int yy = 0;
                    }
                }
            });

            NetServiceManager.getinstance().sendValProfile(userProfile);

            // 세션으로 이동
            startActivity(new Intent(this, SessioinActivity.class));
            return;
        }

        UtilAPI.s_bEvent_service_main = true;

        strPlaybackStatus = PlaybackStatus.IDLE;

        EventBus.getDefault().register(this);

        binding.miniLayout.setVisibility(View.GONE);//mini_layout

        bShowMiniPlayer = false;

        miniPlaying = false;

        if(!UtilAPI.s_one_mini)
        {
            UtilAPI.s_one_mini = true;
        }
        else {
            if (meditationAudioManager.isPlayingAndPause()) {
                miniPlaying = true;

                start_mini_progressbar();

                bShowMiniPlayer = true;

                // 프레임 레이어 조절
                UtilAPI.setMarginBottom(this, binding.container, 54);

                meditationContents = NetServiceManager.getinstance().getCur_contents();

                String title = "";
                if (meditationContents != null) {
                    title = meditationContents.title;
                }

                viewModel.setTitleTo(title);
                binding.miniLayout.setVisibility(View.VISIBLE);//mini_layout

                UtilAPI.load_image(this, meditationContents.thumbnail, binding.ivMiniTitleIcon);

                if (meditationAudioManager.isPlaying()) {
                    UtilAPI.setImage(this, binding.ivPlay, R.drawable.miniplay_pause_btn);
                } else {
                    UtilAPI.setImage(this, binding.ivPlay, R.drawable.miniplay_play_btn);
                }
            } else {
                // 프레임 레이어 조절
                UtilAPI.setMarginBottom(this, binding.container, 0);

                //  배경음악 플레이
                if(AudioPlayer.instance() != null ) {
                    AudioPlayer.instance().update();
                }
            }
        }
    }

    @Override
    protected void onResume() {

        int xx =0;

        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setLifecycleOwner(this);

        if (mainViewModelFactory == null) {
            mainViewModelFactory = new MainViewModelFactory(this, this);
        }
        viewModel = new ViewModelProvider(this.getViewModelStore(), mainViewModelFactory).get(MainViewModel.class);
        binding.setViewModel(viewModel);


        meditationAudioManager = MeditationAudioManager.with(getApplicationContext());

        setUpNavigation();

        changeFragment(homeFragment, "HomeFragment");


        resultData_list = NetServiceManager.getinstance().xmlParser(R.xml.resultdata_result, this.getResources());
       // mAuth = FirebaseAuth.getInstance();
        //mAuth.signOut();

        //boolean login_first_check = PreferenceManager.getBoolean(this,"login_first_check");

        UserProfile userProfile = NetServiceManager.getinstance().getUserProfile();
        if(userProfile.donefirstpopup == 0) {
            // 첫 로그인 일 경우
            PsychologyDlg psychologyDlg = new PsychologyDlg(MainActivity.this);
            psychologyDlg.show();

            userProfile.donefirstpopup = 1;

            NetServiceManager.getinstance().setOnRecvValProfileListener(new NetServiceManager.OnRecvValProfileListener() {
                @Override
                public void onRecvValProfile(boolean validate) {
                    if (validate == true) {
                        int xx = 0;
                    } else {
                        int yy = 0;
                    }
                }
            });

            NetServiceManager.getinstance().sendValProfile(userProfile);

            //PreferenceManager.setBoolean(this,"login_first_check", true);
        }

        UtilAPI.s_Main_activity = this;
    }

    public void setUpNavigation(){
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        //NavHostFragment navHostFragment = (NavHostFragment)getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        //NavigationUI.setupWithNavController(bottomNavigationView, navHostFragment.getNavController());

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_fragment:
                        changeFragment(homeFragment, "HomeFragment");
                        return true;
                    case R.id.sleep_fragment:
                        changeFragment(sleepFragment, "SleepFragment");
                        return true;
                    case R.id.meditation_fragment:
                        changeFragment(meditationFragment, "MeditationFragment");
                        return true;
                    case R.id.music_fragment:
                        changeFragment(musicFragment, "MusicFragment");
                        return true;
                    case R.id.profile_fragment:
                        changeFragment(profileFragment, "ProfileFragment");
                        return true;
                }
                return false;
            }
        });
    }

    public void changeFragment(Fragment fragment, String tagFragmentName) {

        FragmentManager mFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        Fragment currentFragment = mFragmentManager.getPrimaryNavigationFragment();
        if (currentFragment != null) {
            fragmentTransaction.detach(currentFragment);
        }

        Fragment fragmentTemp = mFragmentManager.findFragmentByTag(tagFragmentName);
        if (fragmentTemp == null) {
            fragmentTemp = fragment;
            fragmentTransaction.add(R.id.container, fragmentTemp, tagFragmentName);
        } else {
            fragmentTransaction.attach(fragmentTemp);
        }

        fragmentTransaction.setPrimaryNavigationFragment(fragmentTemp);
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.commitNowAllowingStateLoss();

        if(tagFragmentName.equals("HomeFragment")){
            binding.imageView12.setImageResource(R.drawable.home_bg);
        }else if(tagFragmentName.equals("MeditationFragment")){
            binding.imageView12.setImageResource(R.drawable.meditation_bg);
        }else if(tagFragmentName.equals("SleepFragment")){
            binding.imageView12.setImageResource(R.drawable.sleep_bg);
        }else if(tagFragmentName.equals("MusicFragment")){
            binding.imageView12.setImageResource(R.drawable.music_bg);
        }else if(tagFragmentName.equals("ProfileFragment")){
            binding.imageView12.setImageResource(R.drawable.my_bg);
        }

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onSuccess(Integer id, String message) {
        switch (id) {
            case R.id.iv_close: {
                // 미니 플레이 닫기

//                UtilAPI.player_play_duration = (int)(meditationAudioManager.getDuration() / 1000);
//                UtilAPI.player_play_time = (int)(meditationAudioManager.getContentPosition() / 1000);
//                UtilAPI.Calc_PlayerCheck();

                // 프레임 레이어 조절
                UtilAPI.setMarginBottom(this, binding.container,0);

                MeditationAudioManager.stop();
                binding.miniLayout.setVisibility(View.GONE);//mini_layout
                meditationAudioManager.unbind();

                //  배경음악 플레이
                if(AudioPlayer.instance() != null) {
                    AudioPlayer.instance().update();
                }
            }
            break;
            case R.id.iv_playcontroler: {

                // 플레이 버튼
                if(MeditationAudioManager.isPlaying()) {
                    // 플레이 -> 정지
                    //Toast.makeText(getApplicationContext(),"플레이 -> 정지",Toast.LENGTH_SHORT).show();

                    UtilAPI.setImage(this, binding.ivPlay, R.drawable.miniplay_play_btn);

                    MeditationAudioManager.pause();
                }
                else
                {
                    // 정지 -> 플레이
                    //Toast.makeText(getApplicationContext(),"정지 -> 플레이",Toast.LENGTH_SHORT).show();
                    UtilAPI.setImage(this, binding.ivPlay, R.drawable.miniplay_pause_btn);

                    if(strPlaybackStatus == PlaybackStatus.STOPPED_END) {
                        MeditationAudioManager.resume();
                    }
                    else
                    {
                        MeditationAudioManager.play();
                    }
                }
            }
            break;
            case R.id.iv_Minibg:
            {
                // 미니 bg 선택시
                if(strPlaybackStatus == PlaybackStatus.STOPPED_END) {

                    // 플레이 위치 초기화
                    MeditationAudioManager.idle_start();

                    // 프레임 레이어 조절
                    UtilAPI.setMarginBottom(this, binding.container,0);

                    MeditationAudioManager.stop();
                    binding.miniLayout.setVisibility(View.GONE);
                    meditationAudioManager.unbind();

                    //  배경음악 플레이
                    //AudioPlayer.instance().update();

                    // 세션으로 이동
                    startActivity(new Intent(this, SessioinActivity.class));
                }
                else
                {
                    this.startActivity(new Intent(this, PlayerActivity.class));
                }
            }
            break;
            case R.id.meun_btn:
            {
                this.startActivity(new Intent(this, SettingActivity.class));
            }
            break;
        }
    }

    @Subscribe
    public void onEvent(String status) {

        if(strPlaybackStatus == status) return;

        strPlaybackStatus = status;

        switch (status) {

            case PlaybackStatus.TRACK_CHANGE:
            {
              //  if (player_track_count > 0) {

                NetServiceManager.getinstance().Update_UserProfile_Play((int) (MeditationAudioManager.getDuration() / 1000));

                UserProfile userProfile = NetServiceManager.getinstance().getUserProfile();
                NetServiceManager.getinstance().setOnRecvValProfileListener(new NetServiceManager.OnRecvValProfileListener() {
                    @Override
                    public void onRecvValProfile(boolean validate) {
                        if (validate == true) {
                            int xx = 0;
                        } else {
                            int yy = 0;
                        }
                    }
                });

                NetServiceManager.getinstance().sendValProfile(userProfile);

                    //Toast.makeText(this, "세션 반복 1 증가", Toast.LENGTH_SHORT).show();
               // }
                //player_track_count++;


                //Toast.makeText(getApplicationContext(),"[메인] 리플레이", Toast.LENGTH_SHORT).show();
            }
            break;
            case PlaybackStatus.PAUSED_NOTI: {

                // 플레이 -> 정지
                //Toast.makeText(getApplicationContext(),"[메인] 플레이 -> 정지", Toast.LENGTH_SHORT).show();
                UtilAPI.setImage(this, binding.ivPlay, R.drawable.miniplay_play_btn);

                //MeditationAudioManager.pause();

            }
            break;

            case PlaybackStatus.PLAYING_NOTI: {
                // 정지 -> 플레이
               // Toast.makeText(getApplicationContext(),"[메인]정지 -> 플레이", Toast.LENGTH_SHORT).show();
                UtilAPI.setImage(this, binding.ivPlay, R.drawable.miniplay_pause_btn);

                //MeditationAudioManager.play();

            }
            break;
            case PlaybackStatus.STOPPED_END:
            {
               // player_track_count = 0;
                miniPlaying = false;

                // 음악이 완료 정지 된 경우에 들어옴
                NetServiceManager.getinstance().Update_UserProfile_Play((int) (MeditationAudioManager.getDuration() / 1000));

                UserProfile userProfile = NetServiceManager.getinstance().getUserProfile();
                NetServiceManager.getinstance().setOnRecvValProfileListener(new NetServiceManager.OnRecvValProfileListener() {
                    @Override
                    public void onRecvValProfile(boolean validate) {
                        if (validate == true) {
                            int xx = 0;
                        } else {
                            int yy = 0;
                        }
                    }
                });

                NetServiceManager.getinstance().sendValProfile(userProfile);


                //Toast.makeText(this, "세션 1 증가", Toast.LENGTH_SHORT).show();

                // 플레이 완료가 되어 stop가 되면 play 버튼 상태로 변경
                // 플레이 -> 정지
                //Toast.makeText(getApplicationContext(),"[메인] 플레이 -> 정지", Toast.LENGTH_SHORT).show();
                UtilAPI.setImage(this, binding.ivPlay, R.drawable.miniplay_play_btn);


                //------------------------------------------------------------------
                // 미니 플레이어 플레이 완료 시 결과 화면 표시되게 수정
                //------------------------------------------------------------------
                // 플레이 위치 초기화
                MeditationAudioManager.idle_start();

                // 프레임 레이어 조절
                UtilAPI.setMarginBottom(this, binding.container,0);

                MeditationAudioManager.stop();
                binding.miniLayout.setVisibility(View.GONE);
                meditationAudioManager.unbind();

                //  배경음악 플레이
                //AudioPlayer.instance().update();

                // 세션으로 이동
                startActivity(new Intent(this, SessioinActivity.class));
            }
            break;
            case PlaybackStatus.STOP_NOTI: {

                // 프레임 레이어 조절
                UtilAPI.setMarginBottom(this, binding.container,0);

                MeditationAudioManager.stop();
                binding.miniLayout.setVisibility(View.GONE);//mini_layout
                meditationAudioManager.unbind();

                //  배경음악 플레이
                if(AudioPlayer.instance() != null) {
                    AudioPlayer.instance().update();
                }
            }
            break;
        }
    }


    public void start_mini_progressbar()
    {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if(MeditationAudioManager.isPlaying()) {
                    int progress = (int) ((MeditationAudioManager.getCurrentPosition() / (float) MeditationAudioManager.getDuration()) * 100.0f);

                    binding.miniProgressBar.setProgress(progress);
                }

            }
        } ;

        // 새로운 스레드 실행 코드. 1초 단위로 현재 시각 표시 요청.
        class NewRunnable implements Runnable {
            @Override
            public void run() {
                while (true) {

                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace() ;
                    }

                    // 메인 스레드에 runnable 전달.
                    runOnUiThread(runnable) ;
                }
            }
        }

        NewRunnable nr = new NewRunnable() ;
        Thread t = new Thread(nr) ;
        t.start() ;
    }

//    Handler handler = new Handler();
//    public void CallWithDelay_mini_progress(long miliseconds, final Activity activity, ProgressBar progressBar) {
//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                try {
//
//                    //binding.miniProgressBar.setProgress(50);
//
//                    while (miniPlaying)
//                    {
//                        Thread.sleep(1000);
//
//                        if(MeditationAudioManager.isPlaying()) {
//                            int progress = (int) ((MeditationAudioManager.getCurrentPosition() / (float) MeditationAudioManager.getDuration()) * 100.0f);
//
//                            CallWithDelay_miniProgress_Update(10, progress);
//                        }
//
//
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }, miliseconds);
//    }
//
//    public  void CallWithDelay_miniProgress_Update(long miliseconds, int progress) {
//
//        binding.miniProgressBar.setProgress(progress);
//
////        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
////            @Override
////            public void run() {
////                try {
////
////
////                } catch (Exception e) {
////                    e.printStackTrace();
////                    throw e;
////                }
////            }
////        }, miliseconds);
//    }

    @Override
    protected void onStop() {

        EventBus.getDefault().unregister(this);

        miniPlaying = false;


        UtilAPI.s_bEvent_service_main = false;

        super.onStop();
    }

    @Override
    public void onFailure(Integer id, String message) {

    }

    @Override
    protected void onDestroy() {

        if(UtilAPI.s_bDestroyPass) {

            UtilAPI.s_bDestroyPass = false;
        }
        else
        {

            UtilAPI.Release();
            meditationAudioManager.unbind();
            //meditationAudioManager.release_service();

            if(AudioPlayer.instance() != null) {
                AudioPlayer.instance().release();
            }

            UtilAPI.s_one_mini = false;
        }

        super.onDestroy();
    }
}