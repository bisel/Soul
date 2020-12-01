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

    private int player_track_count;

    @Override
    protected void onStart() {
        super.onStart();

        player_track_count = 0;

        EventBus.getDefault().register(this);

        binding.miniLayout.setVisibility(View.GONE);//mini_layout

        bShowMiniPlayer = false;

        if(!UtilAPI.s_one_mini)
        {
            UtilAPI.s_one_mini = true;
        }
        else {
            if (meditationAudioManager.isPlayingAndPause()) {
//            if(meditationAudioManager.getService() != null)
//            {
//                Toast.makeText(getApplicationContext(),"메인 ## 음악 플레이 중  ",Toast.LENGTH_SHORT).show();
//            }

//            if (meditationAudioManager.getServiceBound()) {
//                Toast.makeText(getApplicationContext(),"메인 %% 음악 플레이 중  ",Toast.LENGTH_SHORT).show();
//            }

                //Toast.makeText(getApplicationContext(),"메인 - 음악 플레이 중 ",Toast.LENGTH_SHORT).show();

                bShowMiniPlayer = true;

                // 프레임 레이어 조절
                UtilAPI.setMarginBottom(this, binding.container, 60);

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
                AudioPlayer.instance().update();
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

        boolean login_first_check = PreferenceManager.getBoolean(this,"login_first_check");
        if(login_first_check == false) {
            // 첫 로그인 일 경우
            PsychologyDlg psychologyDlg = new PsychologyDlg(MainActivity.this);
            psychologyDlg.show();
            PreferenceManager.setBoolean(this,"login_first_check", true);
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
                AudioPlayer.instance().update();
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

                    MeditationAudioManager.play_ex();
                }
            }
            break;
            case R.id.iv_Minibg:
            {
                // 미니 bg 선택시
                this.startActivity(new Intent(this, PlayerActivity.class));
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

        switch (status) {

            case PlaybackStatus.TRACK_CHANGE:
            {
                if (player_track_count > 0) {
                    UserProfile userProfile = NetServiceManager.getinstance().getUserProfile();
                    userProfile.playtime += (int) (MeditationAudioManager.with(null).getDuration() / 1000);
                    userProfile.sessionnum += 1;

                    NetServiceManager.getinstance().sendValProfile(userProfile);
                }
                player_track_count++;


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

                //MeditationAudioManager.play_ex();

            }
            break;
            case PlaybackStatus.STOPPED_EX:
            {
                //UtilAPI.player_stop_count++;

                UserProfile userProfile = NetServiceManager.getinstance().getUserProfile();
                userProfile.playtime += (int)(MeditationAudioManager.with(null).getDuration() / 1000);
                userProfile.sessionnum += 1;

                NetServiceManager.getinstance().sendValProfile(userProfile);

                // 플레이 완료가 되어 stop가 되면 play 버튼 상태로 변경
                // 플레이 -> 정지
                //Toast.makeText(getApplicationContext(),"[메인] 플레이 -> 정지", Toast.LENGTH_SHORT).show();
                UtilAPI.setImage(this, binding.ivPlay, R.drawable.miniplay_play_btn);
            }
            break;
            case PlaybackStatus.STOP_NOTI: {

                // 프레임 레이어 조절
                UtilAPI.setMarginBottom(this, binding.container,0);

                MeditationAudioManager.stop();
                binding.miniLayout.setVisibility(View.GONE);//mini_layout
                meditationAudioManager.unbind();

                //  배경음악 플레이
                AudioPlayer.instance().update();
            }
            break;
        }
    }

    @Override
    protected void onStop() {

        EventBus.getDefault().unregister(this);

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
            UtilAPI.Release_Session();
            meditationAudioManager.unbind();
            //meditationAudioManager.release_service();
            AudioPlayer.instance().release();

            UtilAPI.s_one_mini = false;
        }

        super.onDestroy();
    }
}