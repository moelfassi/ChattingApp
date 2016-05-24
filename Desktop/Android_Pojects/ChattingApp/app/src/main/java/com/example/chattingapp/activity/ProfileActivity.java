package com.example.chattingapp.activity;

import android.Manifest;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.chattingapp.R;
import com.example.chattingapp.model.UserModel;
import com.example.chattingapp.utils.AsyncTaskCompleteListener;
import com.example.chattingapp.utils.ConnectionDetector;
import com.example.chattingapp.utils.Const;
import com.example.chattingapp.utils.SessionHelper;
import com.example.chattingapp.utils.VolleyHttpRequest;
import com.soundcloud.android.crop.Crop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by elfassimounir on 5/10/16.
 */
public class ProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, AsyncTaskCompleteListener, Response.ErrorListener {

    private static final int MY_PERMISSION_READ_E_STORAGE = 123;
    private static final int MY_PERMISSION_WRIGHT_E_STORAGE = 125;
    private static final int MY_PERMISSION_CAMERA = 126;

    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;

    Spinner spinner;
    RequestQueue requestQueue;
    ProgressDialog progressDialog;
    EditText etfname, etlname ;
    Button btnprofilepic, btnupdate, btncancel, btnchangepassword;
    ConnectionDetector connection;
    String firstname, lastname, gender, itemGender, newpass;
    Dialog builder;
    String deviceid = "",selectedfile = "";
    SessionHelper sessionHelper;
    String myusername;
    ArrayList<UserModel> userlist = new ArrayList<UserModel>();
    boolean fileselected =  false;
    Uri uri = null;
    Long filnamo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        sessionHelper = new SessionHelper(this);
        deviceid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        deviceid = sessionHelper.getdeviceid();
        myusername = sessionHelper.getuserid();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));

        connection = new ConnectionDetector(this);
        etfname = (EditText) findViewById(R.id.etfname);
        etlname = (EditText) findViewById(R.id.etlname);

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add(getString(R.string.select_gender));
        categories.add(getString(R.string.female));
        categories.add(getString(R.string.male));
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        btnprofilepic = (Button) findViewById(R.id.btnprofile);
        btnupdate = (Button) findViewById(R.id.btnupdate);
        btncancel = (Button) findViewById(R.id.btncancel);
        btnchangepassword = (Button)findViewById(R.id.btnchangepassword);

        btnprofilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectpic();
            }
        });
        btnupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });
        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProfileActivity.this, MainActivity.class);
                finish();
                startActivity(i);
            }
        });
        btnchangepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changPass();
            }
        });
        getUserProfile();
    }

    private void getUserProfile() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.GET_USER_PROFILE);
        map.put(Const.Params.USERID, myusername);
        map.put(Const.Params.DEVICE_ID, deviceid);
        map.put(Const.Params.PROFILE_ID, myusername);
        requestQueue.add(new VolleyHttpRequest(Request.Method.POST, map, Const.ServiceCode.GET_USER_PROFILE, this, this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent i = new Intent(this, MainActivity.class);
            finish();
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void update() {

        firstname = etfname.getText().toString();
        lastname = etlname.getText().toString();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (firstname.length() < 1) {
            etfname.setError(getString(R.string.plz_enter_firstname));
            etfname.setFocusable(true);
        } else if (lastname.length() < 1) {
            etlname.setError(getString(R.string.plz_enter_lastname));
            etlname.setFocusable(true);
        } else if (itemGender == getString(R.string.selct_gender)) {
            Toast.makeText(getApplicationContext(), R.string.plz_gender, Toast.LENGTH_LONG).show();
            etlname.setError(getString(R.string.plz_enter_lastname));
            etlname.setFocusable(true);
        } else {
            if (connection.isConnectingToInternet()) {
                UpdateProfile();
            } else {
                Toast.makeText(ProfileActivity.this, R.string.plz_connect_to_net, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void UpdateProfile() {

        sessionHelper.updateProfile(firstname, lastname);
        sessionHelper.setProfilepic("uploads/"+filnamo+".jpg");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.UPDATE_PROFILE);
        map.put(Const.Params.USERID, myusername);
        map.put(Const.Params.FIRSTNAME, firstname);
        map.put(Const.Params.LAST_NAME, lastname);
        map.put(Const.Params.GENDER, itemGender);
        map.put(Const.Params.PICTURE, selectedfile);
        progressDialog = ProgressDialog.show(getApplicationContext(), getString(R.string.updating_profile), getString(R.string.please_wait));
        requestQueue.add(new VolleyHttpRequest(Request.Method.POST, map, Const.ServiceCode.UPDATE_PROFILE, this, this));
    }

    public void selectpic() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(R.layout.choose_pic);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView btncamera = (TextView) dialog.findViewById(R.id.btncamera);
        TextView btngallery = (TextView) dialog.findViewById(R.id.btngallery);

        btncamera.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                Calendar cal = Calendar.getInstance();
                File file = new File(Environment.getExternalStorageDirectory(), (cal.getTimeInMillis() + ".jpg"));
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    file.delete();
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(ProfileActivity.this, Manifest.permission.CAMERA)) {

                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_CAMERA);
                            dialog.cancel();
                        }
                    }
                } else {
                    uri = Uri.fromFile(file);
                    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    i.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(i, REQUEST_CAMERA);
                    dialog.cancel();
                }
            }
        });

        btngallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_FILE);
                dialog.cancel();
            }
        });

        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {

            case MY_PERMISSION_WRIGHT_E_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Calendar cal = Calendar.getInstance();
                    File file = new File(Environment.getExternalStorageDirectory(), (cal.getTimeInMillis() + ".jpg"));
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return;
            }

            case MY_PERMISSION_READ_E_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Uri outputUri = Uri.fromFile(new File(this.getCacheDir(), (Calendar.getInstance()
                            .getTimeInMillis() + ".jpg")));
                    Crop.of(uri, outputUri).asSquare().start(this);
                }
                return;
            }

            case MY_PERMISSION_CAMERA:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Calendar cal = Calendar.getInstance();
                    File file = new File(Environment.getExternalStorageDirectory(), (cal.getTimeInMillis() + ".jpg"));
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        file.delete();
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    uri = Uri.fromFile(file);
                    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    i.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(i, REQUEST_CAMERA);

                }
                return;
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                if (data != null) {

                    uri = data.getData();
                    if (uri != null) {
                        beginCrop(uri);
                    } else {
                        Toast.makeText(ProfileActivity.this, getString(R.string.unable_select_image), Toast.LENGTH_LONG).show();
                    }
                }
            }

            else if (requestCode == REQUEST_CAMERA) {
                if (uri != null) {
                    beginCrop(uri);
                } else {
                    Toast.makeText(ProfileActivity.this, getString(R.string.unable_select_image), Toast.LENGTH_LONG).show();
                }
            }

            else if (requestCode == Crop.REQUEST_CROP){
                if (data != null) {
                    handleCrop(resultCode, data);
                }
            }
        }
    }

    private void beginCrop(Uri source) {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_READ_E_STORAGE);
                }
            }
        } else {
            Uri outputUri = Uri.fromFile(new File(this.getCacheDir(), (Calendar.getInstance().getTimeInMillis() + ".jpg")));
            Crop.of(source, outputUri).asSquare().start(this);
        }
    }

    private void handleCrop(int resultCode, Intent result) {

        if (resultCode == this.RESULT_OK) {

            Uri path = Crop.getOutput(result);
            File f = new File(getRealPathFromURI(path));

            Drawable d = Drawable.createFromPath(f.getAbsolutePath());
            btnprofilepic.setBackground(d);
            selectedfile = f.getAbsolutePath();
            fileselected = true;
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(ProfileActivity.this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    private void changPass() {

        builder = new Dialog(ProfileActivity.this, android.R.style.Theme_DeviceDefault_DialogWhenLarge_NoActionBar);
        builder.setContentView(R.layout.change_password);
        builder.setCanceledOnTouchOutside(true);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        builder.show();

        final EditText etnewpass = (EditText) builder.findViewById(R.id.etnewpass);
        final EditText etconfirmpass = (EditText) builder.findViewById(R.id.etnewconfpass);

        TextView txtconfirm = (TextView) builder.findViewById(R.id.txtok);
        TextView txtcancel = (TextView) builder.findViewById(R.id.txtcancel);

        txtconfirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                newpass = etnewpass.getText().toString();
                String confirmpass = etconfirmpass.getText().toString();

                if (newpass.length() < 6) {
                    Toast.makeText(ProfileActivity.this, R.string.min_char, Toast.LENGTH_LONG).show();
                } else if (confirmpass.length() < 6) {
                    Toast.makeText(ProfileActivity.this, R.string.confirm_pass, Toast.LENGTH_LONG).show();
                } else {

                    if (newpass.equals(confirmpass)) {
                        updatepassword();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.pass_not_match, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        txtcancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                builder.dismiss();
            }
        });
    }

    private void updatepassword(){

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.UPDATE_PASSWORD);
        map.put(Const.Params.USERID, myusername);
        map.put(Const.Params.DEVICE_ID, deviceid);
        map.put(Const.Params.PASSWORD, newpass);
        progressDialog = ProgressDialog.show(ProfileActivity.this, getString(R.string.changing_pass), getString(R.string.please_wait));
        requestQueue.add(new VolleyHttpRequest(Request.Method.POST, map, Const.ServiceCode.UPDATE_PASSWORD, this, this));

    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {

        String status = "";
        switch (serviceCode) {

            case Const.ServiceCode.UPDATE_PROFILE:

                progressDialog.dismiss();

                String success = "";
                try {
                    JSONObject jobj = new JSONObject(response);

                    success = jobj.getString("success");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (success.equals("1")) {
                    Toast.makeText(getApplicationContext(), R.string.succes_new_prof, Toast.LENGTH_LONG).show();
                    Intent i = new Intent(ProfileActivity.this, MainActivity.class);
                    finish();
                    startActivity(i);
                }

                break;
            case Const.ServiceCode.UPDATE_PASSWORD:

                JSONObject obj;
                try {
                    obj = new JSONObject(response);
                    String success2 = obj.getString("success");
                    Const.status = success2;

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                progressDialog.dismiss();
                if(Const.status.equals("false")) {
                    sessionHelper.logoutUser();
                } else {
                    if(Const.status.equals("1")) {
                        builder.dismiss();
                        Toast.makeText(getApplicationContext(), R.string.pass_upd_succ, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.try_again_pass, Toast.LENGTH_LONG).show();
                    }
                }

                break;

            case Const.ServiceCode.GET_USER_PROFILE:

                JSONObject obje;
                try {
                    obje = new JSONObject(response);
                    String success1 = obje.getString("success");
                    if (success1.equals("1")) {

                        JSONArray array = obje.getJSONArray("users");

                        ArrayList<UserModel> userlist = new ArrayList<UserModel>();
                        UserModel data;

                        for (int i = 0; i < array.length(); i++) {

                            data = new UserModel();

                            String userid = new String(array.getJSONObject(i).getString("userid").getBytes("UTF-8"), "UTF-8");
                            String firstname = new String(array.getJSONObject(i).getString("firstname").getBytes("UTF-8"), "UTF-8");
                            String lastname = new String(array.getJSONObject(i).getString("lastname").getBytes("UTF-8"), "UTF-8");
                            String profilepic = new String(array.getJSONObject(i).getString("profilepic").getBytes("UTF-8"), "UTF-8");
                            String gender = new String(array.getJSONObject(i).getString("gender").getBytes("UTF-8"), "UTF-8");
                            String statuos = new String(array.getJSONObject(i).getString("status").getBytes("UTF-8"), "UTF-8");

                            data.setGender(gender);
                            data.setUserid(userid);
                            data.setFirstname(firstname);
                            data.setLastname(lastname);
                            data.setProfilepic(profilepic);
                            data.setStatus(statuos);

                            userlist.add(data);
                        }

                        status = success1;
                        Const.alluserlist = userlist;
                    } else if (success1.equals("2")) {

                        status = success1;
                        String message = obje.getString("message");
                    } else {
                        status = success1;
                        String message = obje.getString("message");
                        message = message; //-
                    }
                } catch (JSONException e) {
                    Log.e("JSON Parser", "Error parsing data " + e.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (status.equalsIgnoreCase("1")) {

                    userlist = Const.alluserlist;
                    firstname = userlist.get(0).getFirstname();
                    lastname =  userlist.get(0).getLastname();
                    gender = userlist.get(0).getGender().toUpperCase();
                    etfname.setText("" + firstname);
                    etlname.setText("" + lastname);


                } else if (status.equalsIgnoreCase("false")) {
                    sessionHelper.logoutUser();
                }

                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        itemGender = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
