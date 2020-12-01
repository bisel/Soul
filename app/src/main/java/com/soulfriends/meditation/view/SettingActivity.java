package com.soulfriends.meditation.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.google.firebase.auth.FirebaseAuth;
import com.soulfriends.meditation.R;
import com.soulfriends.meditation.databinding.SettingBinding;
import com.soulfriends.meditation.util.AuthManager;
import com.soulfriends.meditation.util.PreferenceManager;
import com.soulfriends.meditation.util.ResultListener;
import com.soulfriends.meditation.view.player.AudioPlayer;
import com.soulfriends.meditation.viewmodel.SettingViewModel;
import com.soulfriends.meditation.viewmodel.SettingViewModelFactory;

public class SettingActivity extends AppCompatActivity implements ResultListener {

    private SettingBinding binding;
    private SettingViewModel viewModel;
    private ViewModelStore viewModelStore = new ViewModelStore();
    private SettingViewModelFactory settingViewModelFactory;

    private FirebaseAuth mAuth;
    private final AuthManager authManager = new AuthManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        authManager.setResultListener(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting);
        binding.setLifecycleOwner(this);

        boolean bool_noti = PreferenceManager.getBoolean(this,"bool_noti");
        boolean sound_off = PreferenceManager.getBoolean(this,"sound_off");

        if (settingViewModelFactory == null) {
            settingViewModelFactory = new SettingViewModelFactory(this, this);
        }
        viewModel = new ViewModelProvider(this.getViewModelStore(), settingViewModelFactory).get(SettingViewModel.class);
        binding.setViewModel(viewModel);

        // switch 초기값 설정
        binding.switchSound.setChecked(!sound_off);

       // binding.switchNoti.setOnCheckedChangeListener(new noti_SwitchListener());
        binding.switchSound.setOnCheckedChangeListener(new sound_SwitchListener());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onSuccess(Integer id, String message) {
        switch (id) {
            case 100: {

                // 비밀번호 번경 이메일 성공일 경우
                String email = PreferenceManager.getString(this,"email");
                String result = email + getString(R.string.setting_email_balloon);

                binding.layoutBalloon.setVisibility(View.VISIBLE);
                binding.tvBalloon.setText(result);
                CallWithDelay_send_balloon(2000, this);
            }
            break;
            case R.id.ic_close: {

                // 나가기 버튼
                finish();

            }
            break;
            case R.id.tv_password_change: {

                // 비밀번호 변경
                String certification = PreferenceManager.getString(this,"certification");
                if(certification.contains("google")) {

                    // 소셜로그인은 여기서 비밀번호 변경을 할 수 없습니다.
                    binding.layoutBalloon.setVisibility(View.VISIBLE);
                    binding.tvBalloon.setText(getString(R.string.setting_google_balloon));
                    CallWithDelay_send_balloon(2000, this);
                }
                else if(certification.contains("email")) {
                    String email = PreferenceManager.getString(this,"email");
                    authManager.sendPasswordResetEmail(email);
                }
                else {

                }
            }
            break;
            case R.id.tv_logout: {

                // 로그 아웃
                mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();

                // 로그인 화면으로 전환
                finish();

                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("activity_name","setting_activity"); /*송신*/

                startActivity(intent);

            }
            break;
        }
    }

    @Override
    public void onFailure(Integer id, String message) {

    }

    public void CallWithDelay_send_balloon(long miliseconds, final Activity activity) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    binding.layoutBalloon.setVisibility(View.GONE);

                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }, miliseconds);
    }

    public class noti_SwitchListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked) {
                PreferenceManager.setBoolean(buttonView.getContext(),"bool_noti", true);
            }
            else {
                PreferenceManager.setBoolean(buttonView.getContext(),"bool_noti", false);
            }
        }
    }

    public class sound_SwitchListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked) {
                PreferenceManager.setBoolean(buttonView.getContext(),"sound_off", false);

                AudioPlayer.with(getApplicationContext()).playSound(R.raw.bgm, 1.0f);
                //AudioPlayer.instance().play();
            }
            else {
                PreferenceManager.setBoolean(buttonView.getContext(),"sound_off", true);
                AudioPlayer.instance().stop();
            }
        }
    }
}