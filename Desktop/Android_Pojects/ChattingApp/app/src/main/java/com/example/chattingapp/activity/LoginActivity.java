package com.example.chattingapp.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.chattingapp.R;
import com.example.chattingapp.utils.AsyncTaskCompleteListener;
import com.example.chattingapp.utils.ConnectionDetector;
import com.example.chattingapp.utils.Const;
import com.example.chattingapp.utils.SessionHelper;
import com.example.chattingapp.utils.VolleyHttpRequest;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Created by elfassimounir on 4/29/16.
 */
public class LoginActivity extends Activity implements AsyncTaskCompleteListener, Response.ErrorListener {

    private RequestQueue requestQueue;
    private static final int ANIM_CURRENT_ACTIVITY_IN = R.anim.push_left_in;
    private static final int ANIM_CURRENT_ACTIVITY_OUT = R.anim.push_left_out;
    private static final int ANIM_NEXT_ACTIVITY_IN = R.anim.push_right_in;

    Dialog dialog;
    EditText etemail, etpassword;
    Button btnlogin, btncancel;
    TextView btnforgotpass;
    ConnectionDetector connection;
    String email, password, userid, regid, deviceid, firstname, lastname, profilepic;
    String msg = "";
    SessionHelper sessionHelper;
    GoogleCloudMessaging gcm;
    SharedPreferences prefs;
    ProgressDialog progressDialog;
    String resetemail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        requestQueue = Volley.newRequestQueue(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        sessionHelper = new SessionHelper(this);

        deviceid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        connection = new ConnectionDetector(this);

        etemail = (EditText) findViewById(R.id.etemail);
        etpassword = (EditText) findViewById(R.id.etpassword);
        btnlogin = (Button) findViewById(R.id.btnlogin);
        btncancel = (Button) findViewById(R.id.btncancel);
        btnforgotpass = (TextView) findViewById(R.id.btnforgotpass);

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gologin();
            }
        });
        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, MainLog.class);
                finish();
                startActivity(i);
                overridePendingTransition(ANIM_CURRENT_ACTIVITY_IN, ANIM_CURRENT_ACTIVITY_IN);
            }
        });
        btnforgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goforgetpass();
            }
        });

        RegId();
    }

    public void reset(String rEmail){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.RESET);
        map.put(Const.Params.EMAIL, rEmail);
        progressDialog = ProgressDialog.show(LoginActivity.this, getString(R.string.req_pass), getString(R.string.please_wait));
        requestQueue.add(new VolleyHttpRequest(Request.Method.POST, map, Const.ServiceCode.RESET, this, this));
    }

    public void RegId() {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {

                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(Const.PROJECT_NUMBER);
                    msg = "Device registered, registration ID=" + regid;

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            }
        }.execute(null, null, null);
    }

    private void goforgetpass() {

        dialog = new Dialog(LoginActivity.this, android.R.style.Theme_DeviceDefault_DialogWhenLarge_NoActionBar);
        dialog.setContentView(R.layout.forget_password);
        dialog.setCanceledOnTouchOutside(true);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        dialog.show();

        final EditText etemail = (EditText) dialog.findViewById(R.id.etemail);

        TextView txtconfirm = (TextView) dialog.findViewById(R.id.txtok);
        TextView txtcancel = (TextView) dialog.findViewById(R.id.txtcancel);

        txtconfirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                resetemail = etemail.getText().toString();

                if (resetemail.length() < 1) {
                    Toast.makeText(LoginActivity.this, getString(R.string.enter_email), Toast.LENGTH_LONG).show();
                } else if (!resetemail.contains("@")) {
                    Toast.makeText(LoginActivity.this, getString(R.string.enter_valid_email), Toast.LENGTH_LONG).show();
                } else {
                    reset(resetemail);
                }
            }
        });

        txtcancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void gologin() {
        email = etemail.getText().toString();

        password = etpassword.getText().toString();

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (email.length() < 2) {
            etemail.setError(getString(R.string.enter_valid_email));
            etemail.setFocusable(true);
        } else if (password.length() < 6) {
            etpassword.setError(getString(R.string.enter_valid_pass));
            etpassword.setFocusable(true);
        } else {

            if (connection.isConnectingToInternet()) {
                if (email.contains("@")) {
                    if (email.contains(".")) {
                        login();
                    } else {
                        etemail.setError(getString(R.string.enter_valid_email));
                        etemail.setFocusable(true);
                    }
                } else {
                    etemail.setError(getString(R.string.enter_valid_email));
                    etemail.setFocusable(true);
                }
            } else {
                Toast.makeText(LoginActivity.this, R.string.connect_internet, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void login() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.LOGIN);
        map.put(Const.Params.EMAIL, etemail.getText().toString());
        map.put(Const.Params.PASSWORD, etpassword.getText().toString());
        map.put(Const.Params.DEVICE_TOKEN, regid);
        map.put(Const.Params.DEVICE_ID,deviceid);
        progressDialog = ProgressDialog.show(LoginActivity.this, getString(R.string.register), getString(R.string.please_wait));
        requestQueue.add(new VolleyHttpRequest(Request.Method.POST, map, Const.ServiceCode.LOGIN, this, this));
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {

        switch (serviceCode) {
            case Const.ServiceCode.LOGIN:

                try {
                    JSONObject obj = new JSONObject(response);

                    String success = obj.getString("success");
                    progressDialog.dismiss();

                    if (success.equals("1")) {
                        JSONArray array = obj.getJSONArray("users");

                        String status = "Available";
                        for (int i = 0; i < array.length(); i++) {

                            userid = new String(array.getJSONObject(i).getString("userid").getBytes("ISO-8859-1"), "UTF-8");
                            firstname = new String(array.getJSONObject(i).getString("firstname").getBytes("ISO-8859-1"), "UTF-8");
                            lastname = new String(array.getJSONObject(i).getString("lastname").getBytes("ISO-8859-1"), "UTF-8");
                            status = new String(array.getJSONObject(i).getString("status").getBytes("ISO-8859-1"), "UTF-8");
                            profilepic = new String(array.getJSONObject(i).getString("profilepic").getBytes("ISO-8859-1"), "UTF-8");

                        }
                        Const.username = userid;
                        sessionHelper.setlogintype("email");
                        sessionHelper.createLoginSession(userid, firstname, lastname, deviceid);

                        sessionHelper.setProfilepic(profilepic);

                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        i.setAction("splash");
                        finish();
                        startActivity(i);
                        overridePendingTransition(ANIM_NEXT_ACTIVITY_IN, ANIM_CURRENT_ACTIVITY_OUT);

                        SharedPreferences.Editor se = prefs.edit();
                        se.putString("status", status);
                        se.apply();//-commit
                    } else {
                        etpassword.setFocusable(true);
                        etpassword.setError("UserName or Password invalid");
                        etpassword.setFocusable(true);
                    }

                } catch (JSONException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;

            case Const.ServiceCode.RESET:

                try {
                    JSONObject obj = new JSONObject(response);
                    String success = obj.getString("success");
                    progressDialog.dismiss();
                    if (success.equals("1")) {
                        Toast.makeText(getApplicationContext(),R.string.check_email, Toast.LENGTH_LONG).show();
                        Toast.makeText(this,R.string.check_email,Toast.LENGTH_LONG);
                        Intent i = new Intent(LoginActivity.this, MainLog.class);
                        i.setAction("splash");
                        dialog.dismiss();
                        finish();
                        startActivity(i);
                        overridePendingTransition(ANIM_NEXT_ACTIVITY_IN, ANIM_CURRENT_ACTIVITY_OUT);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.check_email_error, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            default:
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.i("onErrorResponse", ": " + error.getMessage());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(LoginActivity.this, MainLog.class);
        finish();
        startActivity(i);
        overridePendingTransition(ANIM_NEXT_ACTIVITY_IN, ANIM_CURRENT_ACTIVITY_OUT);
    }
}
