package com.soulfriends.meditation.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.soulfriends.meditation.R;
import com.soulfriends.meditation.netservice.NetServiceManager;
import com.soulfriends.meditation.util.AuthManager;
import com.soulfriends.meditation.util.UtilAPI;
import com.soulfriends.meditation.view.player.AudioPlayer;

public class IntroActivity extends AppCompatActivity {


    AuthManager authManager = new AuthManager();

    Boolean bLogin = false;

    @Override
    protected void onStart() {
        super.onStart();
        authManager.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        authManager.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        bLogin = authManager.IsLogin(this);

        CallWithDelay(2000, this, bLogin);

        NetServiceManager.getinstance().init(getResources());

        UtilAPI.SetFullScreen(getWindow());

        //FirebaseAuth.getInstance().signOut();

        // 배경음
        AudioPlayer.with(getApplicationContext()).playSound(R.raw.bgm, 1.0f);


    }

    public static void CallWithDelay(long miliseconds, final Activity activity, Boolean bLogin) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (bLogin) {

                        activity.startActivity(new Intent(activity, LoadingActivity.class));

                        activity.overridePendingTransition(0, 0);

                        activity.finish();

                    } else {

                        activity.startActivity(new Intent(activity, LoginActivity.class));

                        activity.finish();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }, miliseconds);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onDestroy() {


        super.onDestroy();
    }

}