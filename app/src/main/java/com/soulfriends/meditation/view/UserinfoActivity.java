package com.soulfriends.meditation.view;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.google.firebase.auth.FirebaseAuth;
import com.soulfriends.meditation.R;
import com.soulfriends.meditation.databinding.ActivityUserinfoBinding;
import com.soulfriends.meditation.model.UserProfile;
import com.soulfriends.meditation.netservice.NetServiceManager;
import com.soulfriends.meditation.util.PreferenceManager;
import com.soulfriends.meditation.util.ResultListener;
import com.soulfriends.meditation.viewmodel.UserinfoViewModel;
import com.soulfriends.meditation.viewmodel.UserinfoViewModelFactory;

public class UserinfoActivity extends AppCompatActivity implements ResultListener {

    private ActivityUserinfoBinding binding;
    private UserinfoViewModel viewModel;
    private ViewModelStore viewModelStore = new ViewModelStore();
    private UserinfoViewModelFactory userinfoViewModelFactory;

    private FirebaseAuth mAuth;

    private UserProfile userProfile;

    private boolean bSuccess_nickname;
    private boolean bSuccess_gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_userinfo);
        binding.setLifecycleOwner(this);

        userProfile = new UserProfile();

        if (userinfoViewModelFactory == null) {
            userinfoViewModelFactory = new UserinfoViewModelFactory(this, this);
        }
        viewModel = new ViewModelProvider(this.getViewModelStore(), userinfoViewModelFactory).get(UserinfoViewModel.class);
        binding.setViewModel(viewModel);

        bSuccess_nickname = false;
        bSuccess_gender = false;

//        NetServiceManager.getinstance().setOnRecvValProfileListener(new NetServiceManager.OnRecvValProfileListener() {
//            @Override
//            public void onRecvValProfile(boolean validate) {
//                Toast.makeText(getApplicationContext(),"success db",Toast.LENGTH_SHORT).show();
//            }
//        });
        //---------------------------------------------
        // NetServiceManager
        //---------------------------------------------
        //NetServiceManager.getinstance().init();
        NetServiceManager.getinstance().setOnRecvValNickNameListener(new NetServiceManager.OnRecvValNickNameListener(){
            @Override
            public void onRecvValNickName(boolean validate) {

                DoRecvValNickName(validate);
            }
        });

        NetServiceManager.getinstance().setOnRecvValProfileListener(new NetServiceManager.OnRecvValProfileListener() {
            @Override
            public void onRecvValProfile(boolean validate) {
                DoRecvValProfile(validate);
            }
        });

        NetServiceManager.getinstance().setOnRecvContentsListener(new NetServiceManager.OnRecvContentsListener() {
            @Override
            public void onRecvContents(boolean validate) {

                DoRecvContents(validate);
            }
        });
    }

    private void DoRecvContents(boolean validate)
    {
        binding.progressBar.setVisibility(View.GONE);

        if(validate) {

            // uid 와 닉네임 저장
            String uid = NetServiceManager.getinstance().getUserProfile().uid;
            String nickname = NetServiceManager.getinstance().getUserProfile().nickname;

            PreferenceManager.setString(this,"uid", uid);
            PreferenceManager.setString(this,"nickname", nickname);

            String key = uid + "##" + nickname;
            PreferenceManager.setString(this,"uid_nickname", key);


            finish();
            this.startActivity(new Intent(this, MainActivity.class));
        }
        else
        {

        }

    }

    private void DoRecvValProfile(boolean validate)
    {
        if(validate)
        {
            // 성공을 하면

            UserProfile userProfile = NetServiceManager.getinstance().getUserProfile();
            userProfile.uid = PreferenceManager.getString(this,"uid");

            NetServiceManager.getinstance().recvUserProfile(userProfile.uid);

            NetServiceManager.getinstance().setOnRecvProfileListener(new NetServiceManager.OnRecvProfileListener() {
                @Override
                public void onRecvProfile(boolean validate, int errorcode) {

                    if(errorcode == 0) {
                        NetServiceManager.getinstance().recvContentsExt();
                    }
                }
            });

            //NetServiceManager.getinstance().recvContentsExt();
        }
        else
        {
            // 다시 보낸다.
            UserProfile userProfile = NetServiceManager.getinstance().getUserProfile();
            userProfile.uid = mAuth.getCurrentUser().getUid();
            userProfile.nickname = viewModel.getNickname().getValue();
            NetServiceManager.getinstance().sendValProfile(userProfile);
        }
    }

    private void DoRecvValNickName(boolean validate) {

        // 닉네임을 보내고 이벤트 받아서 아래 로직으로 ui 처리해야 한다.
        if (validate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                binding.buttonNickname.setBackground(ContextCompat.getDrawable(this, R.drawable.check_enabled));
            } else {
                binding.buttonNickname.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.check_enabled));
            }

            // 사용 가능한 닉네임 입니다.
            binding.tvCheckNickname.setText(getString(R.string.use_ok_nickname));

            bSuccess_nickname = true;

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                binding.buttonNickname.setBackground(ContextCompat.getDrawable(this, R.drawable.check_btn_disabled));
            } else {
                binding.buttonNickname.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.check_btn_disabled));
            }

            // 이미 사용중인 닉네임 입니다.
            binding.tvCheckNickname.setText(getString(R.string.used_nickname));

            bSuccess_nickname = false;
        }

        Check_IsButton();

        binding.progressBar.setVisibility(View.GONE);
    }

    private void hideKeyBoard() {

        InputMethodManager imm;
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if(imm.isAcceptingText())
        {
            View view = this.getCurrentFocus();
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void Check_IsButton()
    {
        // 회원정보 저장 버튼 활성화 여부 체크
        if(bSuccess_nickname && bSuccess_gender) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                binding.buttonOk.setBackground(ContextCompat.getDrawable(this, R.drawable.btn));
            } else {
                binding.buttonOk.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.btn));
            }
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                binding.buttonOk.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_member_disable));
            } else {
                binding.buttonOk.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.btn_member_disable));
            }
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onSuccess(Integer id, String message) {
        switch (id) {
            case R.id.button_nickname: {
                // 중복 검사 버튼

                hideKeyBoard();
                binding.editNickname.clearFocus();

                // 예외처리
                //1 ~ 6자로 제한
                String nickname = viewModel.getNickname().getValue();
                if(nickname.length() < 1)
                {

                    break;
                }

                binding.progressBar.setVisibility(View.VISIBLE);

                NetServiceManager.getinstance().sendValNickName(viewModel.getNickname().getValue());
            }
            break;
            case R.id.button_man: {
                userProfile.gender = 1;

                bSuccess_gender = true;

                // man
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    binding.buttonMan.setBackground(ContextCompat.getDrawable(this, R.drawable.man_selected));
                } else {
                    binding.buttonMan.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.man_selected));
                }

                // woman
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    binding.buttonWoman.setBackground(ContextCompat.getDrawable(this, R.drawable.man));
                } else {
                    binding.buttonWoman.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.man));
                }

                Check_IsButton();
            }
            break;
            case R.id.button_woman: {
                userProfile.gender = 2;

                bSuccess_gender = true;

                // woman
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    binding.buttonWoman.setBackground(ContextCompat.getDrawable(this, R.drawable.man_selected));
                } else {
                    binding.buttonWoman.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.man_selected));
                }

                // man
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    binding.buttonMan.setBackground(ContextCompat.getDrawable(this, R.drawable.man));
                } else {
                    binding.buttonMan.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.man));
                }

                Check_IsButton();

            }
            break;

            case R.id.button_ok: {

                if(!bSuccess_nickname) {
                    break;
                }

                if(!bSuccess_gender) {
                    binding.tvGenderSelect.setVisibility(View.VISIBLE);
                    break;
                }

                binding.progressBar.setVisibility(View.VISIBLE);

                UserProfile userProfile = NetServiceManager.getinstance().getUserProfile();
                userProfile.uid = mAuth.getCurrentUser().getUid();
                userProfile.nickname = viewModel.getNickname().getValue();
                NetServiceManager.getinstance().sendValProfile(userProfile);
                //myRef.setValue(userProfile);

                //this.startActivity(new Intent(this, LoadingActivity.class));

               // int xx = 0;

//                FirebaseDatabase.getInstance()
//                                    .getReference("images")
//                                    .child(imageUid)
//                                    .child("comments")
//                                    .push()
//                                    .setValue(comment);

//                databaseReference
//                        .child("users")
//                        .child(firebaseUser.getUid())
//                        .setValue(User.fromFirebaseUser(application.firebaseUser))
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                startActivity(MainActivity.createIntent(SplashActivity.this));
//                                finish();
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                showSnackbar(R.string.error_user_registration_failed);
//                            }
//                        })
//                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                Log.d(TAG, "complete");
//                            }
//                        });

            }
            break;
        }
    }

    @Override
    public void onFailure(Integer id, String message) {
        switch (id) {
            case R.id.button_nickname: {

            }
            break;

            case R.id.button_ok: {
            }
            break;
        }
    }
}