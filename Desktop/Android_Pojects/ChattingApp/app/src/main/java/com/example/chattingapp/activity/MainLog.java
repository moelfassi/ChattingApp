package com.example.chattingapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.chattingapp.R;
import com.example.chattingapp.utils.SessionHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by elfassimounir on 4/29/16.
 */
public class MainLog extends Activity {

    private static final int ANIM_CURRENT_ACTIVITY_OUT = R.anim.push_left_out;
    private static final int ANIM_NEXT_ACTIVITY_IN = R.anim.push_right_in;

    String deviceid = "";
    Button btnregister, btnemailsignin;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mainlog);

        deviceid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.example.chattingapp", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
        }

        btnregister = (Button) findViewById(R.id.btnregister);
        btnemailsignin = (Button) findViewById(R.id.btnemailsignin);
        btnregister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainLog.this, RegisterActivity.class);
                finish();
                startActivity(i);
                overridePendingTransition(ANIM_NEXT_ACTIVITY_IN, ANIM_CURRENT_ACTIVITY_OUT);
            }
        });

        btnemailsignin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainLog.this, LoginActivity.class);
                finish();
                startActivity(i);
                overridePendingTransition(ANIM_NEXT_ACTIVITY_IN, ANIM_CURRENT_ACTIVITY_OUT);
            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.really_exit)
                .setMessage(R.string.sure_exit)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_HOME);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                finish();
                                startActivity(intent);
                            }
                        }).create().show();
    }

}
