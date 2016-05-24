package com.example.chattingapp.activity;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chattingapp.R;
import com.example.chattingapp.utils.AsyncTaskCompleteListener;
import com.example.chattingapp.utils.ConnectionDetector;
import com.example.chattingapp.utils.Const;
import com.example.chattingapp.utils.MultiPartRequester;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.soundcloud.android.crop.Crop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by elfassimounir on 4/29/16.
 */
public class RegisterActivity extends Activity implements AsyncTaskCompleteListener,OnItemSelectedListener {

    private static final int MY_PERMISSION_READ_E_STORAGE = 123;
    private static final int MY_PERMISSION_WRIGHT_E_STORAGE = 125;
    private static final int MY_PERMISSION_CAMERA = 126;

    private static final int ANIM_CURRENT_ACTIVITY_IN = R.anim.push_left_in;
    private static final int ANIM_CURRENT_ACTIVITY_OUT = R.anim.push_left_out;
    private static final int ANIM_NEXT_ACTIVITY_IN = R.anim.push_right_in;

    EditText etemail, etfname, etlname, etpassword, etconfirmpass, etuserid;
    Button btnprofilepic, btnregister, btncancel;
    ProgressDialog progressDialog;
    ConnectionDetector connectionDetector;
    String itemGenger, email="", firstname="", password="", confirmpassword="", deviceid = "", selectedfile = "",
            lastname="", userid="", regid="", msg = "";
    Spinner spinner;

    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;
    public static final int progress_bar_type = 0;

    GoogleCloudMessaging gcm;
    boolean fileselected =  false;

    private Uri uri = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        deviceid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

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

        connectionDetector = new ConnectionDetector(this);

        etuserid = (EditText)findViewById(R.id.usrname);
        etemail = (EditText) findViewById(R.id.etemail);
        etfname = (EditText) findViewById(R.id.etfname);
        etlname = (EditText) findViewById(R.id.etlname);
        etpassword = (EditText) findViewById(R.id.etpassword);
        etconfirmpass = (EditText) findViewById(R.id.etconfirmpass);

        btnprofilepic = (Button) findViewById(R.id.btnprofile);
        btnregister = (Button) findViewById(R.id.btnregister);
        btncancel = (Button) findViewById(R.id.btncancel);

        btnprofilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectpic();
            }
        });
        btnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterActivity.this, MainLog.class);
                finish();
                startActivity(i);
                overridePendingTransition(ANIM_CURRENT_ACTIVITY_IN, ANIM_CURRENT_ACTIVITY_IN);
            }
        });

        regId();
    }

    public void regId() {

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

    public void register() {

        email = etemail.getText().toString();
        firstname = etfname.getText().toString();
        password = etpassword.getText().toString();
        confirmpassword = etconfirmpass.getText().toString();
        lastname = etlname.getText().toString();
        userid = etuserid.getText().toString();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (email.length() < 2) {
            etemail.setError(getString(R.string.enter_valid_email));
            etemail.setFocusable(true);
        } else if (userid.length() < 1 || userid.length() < 6 || userid.length() > 8 ) {
            etuserid.setError(getString(R.string.enter_vld_uname));
            etuserid.setFocusable(true);
        } else if (firstname.length() < 1) {
            etfname.setError(getString(R.string.enter_vld_fname));
            etfname.setFocusable(true);
        } else if (password.length() < 6  ) {
            etpassword.setError("Please enter valid password 6+ ch.");
            etpassword.setFocusable(true);
        } else if (confirmpassword.length() < 1) {
            etconfirmpass.setError("Please enter valid confirm password");
            etconfirmpass.setFocusable(true);
        } else if (lastname.length() < 1) {
            etlname.setError("Please enter valid last name");
            etlname.setFocusable(true);
        }else if (itemGenger == "Select Gender:") {
            Toast.makeText(getApplicationContext(), "Please select gender", Toast.LENGTH_LONG).show();
        }else {
            if (!password.equals(confirmpassword)) {
                etconfirmpass.setError("Password does not match");
                etconfirmpass.setFocusable(true);
            } else {
                if (connectionDetector.isConnectingToInternet()) {
                    if (email.contains("@")) {
                        if (email.contains(".")) {
                            registerServer();
                        } else {
                            etemail.setError(getString(R.string.enter_valid_email));
                            etemail.setFocusable(true);
                        }
                    } else {
                        etemail.setError(getString(R.string.enter_valid_email));
                        etemail.setFocusable(true);
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, getString(R.string.connect_internet), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void selectpic() {

        final Dialog myDialog = new Dialog(this);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        myDialog.setContentView(R.layout.choose_pic);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView btncamera = (TextView) myDialog.findViewById(R.id.btncamera);
        TextView btngallery = (TextView) myDialog.findViewById(R.id.btngallery);

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

                    if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.CAMERA)) {

                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_CAMERA);
                            myDialog.cancel();
                        }
                    }
                } else {
                    uri = Uri.fromFile(file);
                    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    i.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(i, REQUEST_CAMERA);
                    myDialog.cancel();
                }
            }
        });

        btngallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_FILE);
                myDialog.cancel();
            }
        });

        myDialog.show();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                if (data != null) {

                    uri = data.getData();
                    if (uri != null) {
                        beginCrop(uri);
                    } else {
                        Toast.makeText(RegisterActivity.this, getString(R.string.unable_select_image), Toast.LENGTH_LONG).show();
                    }
                }
            }

            else if (requestCode == REQUEST_CAMERA) {
                if (uri != null) {
                    beginCrop(uri);
                } else {
                    Toast.makeText(RegisterActivity.this, getString(R.string.unable_select_image), Toast.LENGTH_LONG).show();
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

            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

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
            Toast.makeText(RegisterActivity.this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        AlertDialog dialogDetails = null;
        switch (id) {
            case progress_bar_type: // we set this to 0
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(getString(R.string.sending_file));
                progressDialog.setIndeterminate(false);
                progressDialog.setMax(100);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(false);
                progressDialog.show();
                return progressDialog;

            default:
                return null;
        }
    }

    private void registerServer() {

        HashMap<String, String> map = new HashMap<String, String>();

        map.put(Const.URL, Const.ServiceType.REGISTER);
        map.put(Const.Params.USERID, etuserid.getText().toString());
        map.put(Const.Params.EMAIL, etemail.getText().toString());
        map.put(Const.Params.PASSWORD, etpassword.getText().toString());
        map.put(Const.Params.FIRSTNAME, etfname.getText().toString());
        map.put(Const.Params.LAST_NAME, etlname.getText().toString());
        map.put(Const.Params.GENDER, itemGenger);
        map.put(Const.Params.DEVICE_TOKEN, regid);
        map.put(Const.Params.DEVICE_ID, deviceid);
        map.put(Const.Params.PICTURE, selectedfile);

        showDialog(progress_bar_type);
        progressDialog.setProgress(0);
        new MultiPartRequester(this, map, Const.ServiceCode.REGISTER, this);
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {

        switch (serviceCode) {
            case Const.ServiceCode.REGISTER:

                progressDialog.dismiss();
                String success = "";
                try {
                    JSONObject jobj = new JSONObject(response);
                    success = jobj.getString("success");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (success.equals("1")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.success_register), Toast.LENGTH_LONG).show();
                    Intent i = new Intent(RegisterActivity.this, MainLog.class);
                    finish();
                    startActivity(i);
                    overridePendingTransition(ANIM_NEXT_ACTIVITY_IN, ANIM_CURRENT_ACTIVITY_OUT);
                } else if (success.equals("2")) {
                    Toast.makeText(getApplicationContext(), R.string.email_registered, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(RegisterActivity.this, MainLog.class);
        finish();
        startActivity(i);
        overridePendingTransition(ANIM_NEXT_ACTIVITY_IN, ANIM_CURRENT_ACTIVITY_OUT);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        itemGenger = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
