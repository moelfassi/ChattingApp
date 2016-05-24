package com.example.chattingapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.chattingapp.R;
import com.example.chattingapp.utils.Const;
import com.example.chattingapp.utils.SessionHelper;

/**
 * Created by elfassimounir on 4/30/16.
 */
public class SplashActivity extends Activity {

    private static final int ANIM_CURRENT_ACTIVITY_OUT = R.anim.push_left_out;
    private static final int ANIM_NEXT_ACTIVITY_IN = R.anim.push_right_in;
    private static final int TIME = 2000;
    SessionHelper sessionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionHelper = new SessionHelper(this);

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {

            @Override
            public void run() {

                if(sessionHelper.isLoggedIn()) {
                        Intent i = new Intent(SplashActivity.this, MainActivity.class);
                        i.setAction("splash");
                        finish();
                        startActivity(i);
                        overridePendingTransition(ANIM_NEXT_ACTIVITY_IN, ANIM_CURRENT_ACTIVITY_OUT);
                } else {
                        Intent i = new Intent(SplashActivity.this, MainLog.class);
                        finish();
                        startActivity(i);
                        overridePendingTransition(ANIM_NEXT_ACTIVITY_IN, ANIM_CURRENT_ACTIVITY_OUT);
                }
            }
        }, TIME);
    }
}
