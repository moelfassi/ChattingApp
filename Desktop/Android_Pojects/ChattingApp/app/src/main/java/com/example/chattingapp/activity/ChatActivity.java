package com.example.chattingapp.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.chattingapp.R;
import com.example.chattingapp.database.MessageDatabase;
import com.example.chattingapp.database.UsersDatabase;
import com.example.chattingapp.model.MessageModel;
import com.example.chattingapp.model.UserModel;
import com.example.chattingapp.utils.AsyncTaskCompleteListener;
import com.example.chattingapp.utils.Const;
import com.example.chattingapp.utils.ImageUtil;
import com.example.chattingapp.utils.MultiPartRequester;
import com.example.chattingapp.utils.OnSwipeTouchListener;
import com.example.chattingapp.utils.SessionHelper;
import com.example.chattingapp.utils.VolleyHttpRequest;
import com.example.chattingapp.views.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import github.ankushsachdeva.emojicon.EmojiconEditText;
import github.ankushsachdeva.emojicon.EmojiconGridView;
import github.ankushsachdeva.emojicon.EmojiconTextView;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.emoji.Emojicon;

/**
 * Created by elfassimounir on 5/13/16.
 */
public class ChatActivity extends AppCompatActivity implements AsyncTaskCompleteListener, Response.ErrorListener  {

    private static final int MY_PERMISSION_READ_E_STORAGE = 120;
    private static final int MY_PERMISSION_RECORD_AUDIO = 121;
    private static final int MY_PERMISSION_CAMERA = 122;
    private static final int MY_PERMISSION_CAMERA_vid = 123;

    private static final int ANIM_CURRENT_ACTIVITY_OUT = R.anim.push_left_out;
    private static final int ANIM_NEXT_ACTIVITY_IN = R.anim.push_right_in;

    private static final int MY_CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int MY_IMAGE_PICK_REQUEST_CODE = 200;
    private static final int MY_CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 300;
    private static final int MY_AUDIO_REQUEST_CODE = 400;

    public static final int progress_bar_type = 0;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int MEDIA_TYPE_SOUND = 3;

    RequestQueue requestQueue;
    ProgressDialog progressDialog;
    Dialog dialog;
    ArrayList<UserModel> userlist = new ArrayList<UserModel>();
    ArrayList<UserModel> userlistos;
    ArrayList<MessageModel> messageModelList = new ArrayList<MessageModel>();
    EmojiconEditText emoji_edittext;
    Button btn_file;
    ImageView btn_send,btn_emoji;
    String filePath, filename, senderid, myuserid, message, deviceid, name, profileid, messagetype;
    String fullname = "";
    String status = "";

    SessionHelper sessionHelper;
    MessageDatabase messageDatabase;
    ListView listView;
    TextView txtalert, txtname;
    RoundedImageView roundedImageView;

    boolean hasAttachment = false;
    int millis = 0;
    Uri selectedattachment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        registerReceiver(broadcastReceiver, new IntentFilter("CHAT_MESSAGE_RECEIVED"));

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        messageDatabase = new MessageDatabase(getApplicationContext());
        emoji_edittext = (EmojiconEditText) findViewById(R.id.et_message);
        btn_send = (ImageView) findViewById(R.id.btn_send);
        btn_file = (Button) findViewById(R.id.btn_file);
        btn_emoji = (ImageView) findViewById(R.id.btn_emoji);
        btn_emoji.setImageResource(R.drawable.smiley);
        listView = (ListView) findViewById(R.id.listview);
        listView.setOnTouchListener(new OnSwipeTouchListener(ChatActivity.this){

            public void onSwipeRight() {
                Intent i = new Intent(ChatActivity.this, MainActivity.class);
                finish();
                startActivity(i);
                overridePendingTransition(ANIM_NEXT_ACTIVITY_IN, ANIM_CURRENT_ACTIVITY_OUT);
            }
        });

        registerForContextMenu(listView);

        sessionHelper = new SessionHelper(this);
        deviceid = sessionHelper.getdeviceid();
        senderid = getIntent().getExtras().getString("senderid");
        fullname = getIntent().getExtras().getString("fullname");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));

        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.my_actionbar, null);
        txtalert = (TextView) mCustomView.findViewById(R.id.txt_emptylist);
        roundedImageView = (RoundedImageView) mCustomView.findViewById(R.id.imge);
        txtname = (TextView) mCustomView.findViewById(R.id.txtname);

        toolbar.addView(mCustomView);

        txtname.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(ChatActivity.this, ProfileActivity.class);
                profileid = senderid;
                startActivity(i);
            }
        });

        if (fullname.length() > 0) {
            txtname.setText("" + fullname);

        } else {
            name = messageDatabase.getname(senderid);
            fullname = name;
            txtname.setText("" + name);
        }

        myuserid = sessionHelper.getuserid();

        getUserProfile();
        emoji();
        getmessages();

        messageDatabase.updateindividualseen(senderid);

        btn_send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendNewMessage();
            }
        });
        btn_file.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AddAttachement();
            }
        });
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getmessages();
        }
    };

    private void getUserProfile() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.GET_USER_PROFILE);
        map.put(Const.Params.USERID, myuserid);
        map.put(Const.Params.DEVICE_ID, deviceid);
        map.put(Const.Params.PROFILE_ID, senderid);
        requestQueue.add(new VolleyHttpRequest(Request.Method.POST, map, Const.ServiceCode.GET_USER_PROFILE, this, this));
    }

    void sendNewMessage() {

        if (!hasAttachment) {
            messagetype = "text";
            message = emoji_edittext.getText().toString();
            if (message.length() > 0) {
                sendMesage();
            }
            emoji_edittext.setText("");
            Date todaydate = new Date();

            String date = String.valueOf(todaydate.getTime());
            messageDatabase.createMeassage(new MessageModel(fullname, "", message, myuserid, senderid, date, "individual", "yes", "", "yes", messagetype));
            getmessages();
        }
    }

    private void sendMesage() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.SEND_MESSAGE);
        map.put(Const.Params.FROM_ID, myuserid);
        map.put(Const.Params.TO_ID, senderid);
        map.put(Const.Params.MESSAGE, message);
        map.put(Const.Params.DEVICE_ID, deviceid);
        map.put(Const.Params.MESSAGE_TYPE, messagetype);
        requestQueue.add(new VolleyHttpRequest(Request.Method.POST, map, Const.ServiceCode.SEND_MESSAGE, this, this));
    }

    public void getmessages() {

        List<MessageModel> messagelist = messageDatabase.userDataList(senderid);
        if (messagelist.size() > 0) {
            messageModelList.clear();
            MessageModel messages;
            for (MessageModel msg : messagelist) {
                messages = new MessageModel();
                String fname = msg.getFname();
                String lname = msg.getLname();
                String tstamp = msg.getTimestamp();
                String message = msg.getMessage();
                String me = msg.getIsmine();
                String msgtype = "";

                if(msg.getMsgtype() != null) {
                    msgtype = msg.getMsgtype();
                } else {
                    msgtype = "";
                }

                messages.setMessage(message);
                messages.setFname(fname);
                messages.setLname(lname);
                messages.setTimestamp(tstamp);
                messages.setIsmine(me);
                messages.setMsgtype(msgtype);

                messageModelList.add(messages);
            }

            txtalert.setVisibility(View.INVISIBLE);

            MessageAdapter adapter = new MessageAdapter(getApplicationContext(), messageModelList);
            adapter.notifyDataSetChanged();
            listView.setAdapter(adapter);
            listView.setSelection(messageModelList.size() - 1);
        }
        int seencount = messageDatabase.getunseeenindividual(senderid, "no");
        if (seencount > 0) {
            messageDatabase.updateindividualseen(senderid);
        }
        noNotification();
    }

    @Override
    public void onErrorResponse(VolleyError error) {}

    public static class MessageAdapter extends BaseAdapter {

        public static Context mContext;
        public static ArrayList<MessageModel> mMessageModels;

        public MessageAdapter(Context context, ArrayList<MessageModel> amList) {
            super();

            MessageAdapter.mContext = context;
            MessageAdapter.mMessageModels = amList;
        }

        @Override
        public int getCount() {
            return mMessageModels.size();
        }

        @Override
        public Object getItem(int position) {
            return mMessageModels.get(position);
        }

        @SuppressWarnings("deprecation")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MessageModel messageModel1 = (MessageModel) this.getItem(position);

            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, parent, false);
                holder.message = (EmojiconTextView) convertView.findViewById(R.id.message_text);
                holder.thumb = (ImageView) convertView.findViewById(R.id.thumb);
                holder.message.setPadding(10, 10, 10, 10);
                convertView.setTag(holder);
            } else
                holder = (ViewHolder) convertView.getTag();

            if (messageModel1.getMsgtype().equalsIgnoreCase("text")) {

                holder.message.setVisibility(View.VISIBLE);
                holder.thumb.setVisibility(View.GONE);
                holder.message.setText(messageModel1.getMessage());

                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.message.getLayoutParams();

                if (messageModel1.getIsmine().equals("yes")) {
                    holder.message.setBackgroundResource(R.drawable.message_bubble_sent);

                    int padding_in_dp = 19;  // 6 dps
                    final float scale = mContext.getResources().getDisplayMetrics().density;
                    int padding_in_px = (int) (padding_in_dp * scale + 0.5f);

                    holder.message.setPadding(18, 15, padding_in_px, 15);
                    holder.message.setTextColor(mContext.getResources().getColor(R.color.Black));
                    lp.gravity = Gravity.RIGHT;
                    lp.leftMargin = 50;
                    lp.rightMargin = 20;
                }
                // If not mine then it is from sender to show orange background and
                // align to left
                else {
                    holder.message.setBackgroundResource(R.drawable.message_bubble_received);

                    int padding_in_dp = 19;  // 6 dps
                    final float scale = mContext.getResources().getDisplayMetrics().density;
                    int padding_in_px = (int) (padding_in_dp * scale + 0.5f);

                    holder.message.setPadding(padding_in_px, 15, 18, 15);
                    holder.message.setTextColor(mContext.getResources().getColor(R.color.white));
                    lp.gravity = Gravity.LEFT;
                    lp.rightMargin = 50;
                    lp.leftMargin = 20;
                }
                holder.message.setLayoutParams(lp);

            } else {
                LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) holder.thumb.getLayoutParams();
                holder.message.setVisibility(View.GONE);
                holder.thumb.setVisibility(View.VISIBLE);
                holder.thumb.setPadding(15, 15, 15, 15);
                if(messageModel1.getMsgtype().equals("video")) {
                    final String url = Const.ServiceType.LINK_UPS + messageModel1.getMessage().toString();
                    Picasso.with(mContext)
                            .load(R.drawable.placeholder_video)
                            .placeholder(R.drawable.placeholder_video)
                            .into(holder.thumb);

                    holder.thumb.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setDataAndType(Uri.parse(url), "video/*");
                            mContext.startActivity(intent);
                        }
                    });
                } else if (messageModel1.getMsgtype().equals("audio")){
                    final String url = Const.ServiceType.LINK_UPS + messageModel1.getMessage();
                    Picasso.with(mContext)
                            .load(R.drawable.placeholder_audio)
                            .placeholder(R.drawable.placeholder_audio)
                            .into(holder.thumb);

                    holder.thumb.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Uri myUri = Uri.parse(url);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setDataAndType(myUri, "audio/*");
                            mContext.startActivity(intent);
                        }
                    });
                } else if (messageModel1.getMsgtype().equals("photo")){
                    final String url =Const.ServiceType.LINK_UPS+ messageModel1.getMessage();
                    Picasso.with(mContext)
                            .load(url)
                            .resize(80, 80)
                            .centerCrop()
                            .placeholder(R.drawable.placeholder_image)
                            .into(holder.thumb);

                    Log.i("url "," "+url);
                    holder.thumb.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setDataAndType(Uri.parse(url), "image/*");
                            mContext.startActivity(intent);
                        }
                    });
                }
                if (messageModel1.getIsmine().equals("yes")) {
                    holder.thumb.setBackgroundResource(R.drawable.message_bubble_sent);
                    holder.thumb.setPadding(15, 15, 35, 15);
                    lp1.gravity = Gravity.RIGHT;
                    lp1.topMargin = 10;
                    lp1.bottomMargin = 10;
                    lp1.leftMargin = 50;
                    lp1.rightMargin = 20;
                }
                // If not mine then it is from sender to show orange background and
                // align to left
                else {
                    holder.thumb.setBackgroundResource(R.drawable.message_bubble_received);
                    holder.thumb.setPadding(35, 15, 15, 15);
                    lp1.gravity = Gravity.LEFT;
                    lp1.topMargin = 10;
                    lp1.bottomMargin = 10;
                    lp1.rightMargin = 50;
                    lp1.leftMargin = 20;
                }
                holder.thumb.setLayoutParams(lp1);
            }
            return convertView;
        }

        class ViewHolder {
            EmojiconTextView  message;
            ImageView thumb;
        }

        @Override
        public long getItemId(int position) {
            // Unimplemented, because we aren't using Sqlite.
            return 0;
        }
    }

    public void noNotification() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            notificationManager.cancel(Integer.valueOf(senderid));
        } catch (Exception e){

        }
    }

    public void AddAttachement() {

        dialog = new Dialog(ChatActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setContentView(R.layout.add_attachement);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);

        dialog.show();
        LinearLayout ln = (LinearLayout) dialog.findViewById(R.id.view_Linlay);
        TextView textNewphoto = (TextView) dialog.findViewById(R.id.txtnewphoto);
        TextView textLocalpic = (TextView) dialog.findViewById(R.id.txtexistphoto);
        TextView textNewvid = (TextView) dialog.findViewById(R.id.txtnewVideo);
        TextView txtNewAudio = (TextView) dialog.findViewById(R.id.txtnewAudio);
        TextView cancel = (TextView) dialog.findViewById(R.id.txtcancel);

        ln.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        textNewphoto.setOnClickListener(new View.OnClickListener() {

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(ChatActivity.this, Manifest.permission.CAMERA)) {


                    } else {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_CAMERA);
                    }
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    selectedattachment = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedattachment);
                    // start the image capture Intent
                    startActivityForResult(intent, MY_CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                    dialog.dismiss();
                }
            }
        });

        textLocalpic.setOnClickListener(new View.OnClickListener() {

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {


                    } else {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_READ_E_STORAGE);
                    }
                }  else {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    // start the image capture Intent
                    startActivityForResult(intent, MY_IMAGE_PICK_REQUEST_CODE);
                    dialog.dismiss();
                }
            }
        });

        textNewvid.setOnClickListener(new View.OnClickListener() {

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(ChatActivity.this, Manifest.permission.CAMERA)) {


                    } else {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_CAMERA_vid);
                    }
                } else {

                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    selectedattachment = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                    // set video quality
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedattachment); // set the image file
                    startActivityForResult(intent, MY_CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
                    dialog.dismiss();

                }
            }
        });

        txtNewAudio.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(ChatActivity.this, Manifest.permission.RECORD_AUDIO)) {


                    } else {
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSION_RECORD_AUDIO);
                    }
                } else {
                    Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                    startActivityForResult(intent, 400);
                    dialog.dismiss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case MY_PERMISSION_CAMERA: {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                selectedattachment = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedattachment);
                startActivityForResult(intent, MY_CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                dialog.dismiss();
                return;
            }

            case MY_PERMISSION_READ_E_STORAGE: {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, MY_IMAGE_PICK_REQUEST_CODE);
                dialog.dismiss();
                return;
            }

            case MY_PERMISSION_CAMERA_vid:{
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                selectedattachment = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedattachment); // set the image file
                // name
                // start the video capture Intent
                startActivityForResult(intent, MY_CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
                dialog.dismiss();
                return;
            }
            case MY_PERMISSION_RECORD_AUDIO:{
                Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                startActivityForResult(intent, 400);
                dialog.dismiss();
                return;
            }
        }
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    public File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Const.IMAGE_DIRECTORY);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            filename = "IMG_" + timeStamp + ".jpg";
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            filename = "VID_" + timeStamp + ".mp4";
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        } else if (type == MEDIA_TYPE_SOUND) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "AUD_" + timeStamp + ".m4a");
        } else {
            return null;
        }
        System.out.println("mediafile---" + mediaFile);
        return mediaFile;
    }

    public void emoji() {
        final View rootView = findViewById(R.id.root_view);
        final EmojiconsPopup popup = new EmojiconsPopup(rootView, this);

        //Will automatically set size according to the soft keyboard size
        popup.setSizeForSoftKeyboard();

        //Set on emojicon click listener
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                emoji_edittext.append(emojicon.getEmoji());
            }
        });

        //Set on backspace click listener
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                emoji_edittext.dispatchKeyEvent(event);
            }
        });

        //If the emoji popup is dismissed, change btnsmiley to smiley icon
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(btn_emoji, R.drawable.smiley);
            }
        });

        //If the text keyboard closes, also dismiss the emoji popup
        popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {
                if(popup.isShowing())
                    popup.dismiss();
            }
        });

        //On emoji clicked, add it to edittext
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                emoji_edittext.append(emojicon.getEmoji());
            }
        });

        //On backspace clicked, emulate the KEYCODE_DEL key event
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                emoji_edittext.dispatchKeyEvent(event);
            }
        });

        // To toggle between text keyboard and emoji keyboard keyboard(Popup)
        btn_emoji.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //If popup is not showing => emoji keyboard is not visible, we need to show it
                if(!popup.isShowing()){

                    //If keyboard is visible, simply show the emoji popup
                    if(popup.isKeyBoardOpen()){
                        popup.showAtBottom();
                        changeEmojiKeyboardIcon(btn_emoji, R.drawable.ic_action_keyboard);
                    }

                    //else, open the text keyboard first and immediately after that show the emoji popup
                    else{
                        emoji_edittext.setFocusableInTouchMode(true);
                        emoji_edittext.requestFocus();
                        popup.showAtBottomPending();
                        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(emoji_edittext, InputMethodManager.SHOW_IMPLICIT);
                        changeEmojiKeyboardIcon(btn_emoji, R.drawable.ic_action_keyboard);
                    }
                }

                //If popup is showing, simply dismiss it to show the undelying text keyboard
                else{
                    popup.dismiss();
                }
            }
        });
    }

    public void changeEmojiKeyboardIcon(ImageView iconToBeChanged, int drawableResourceId){
        iconToBeChanged.setImageResource(drawableResourceId);
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {

        switch (serviceCode) {

            case Const.ServiceCode.BLOCK_FRIEND:

                JSONObject obj;
                try {
                    obj = new JSONObject(response);
                    String success = obj.getString("success");
                    status = success;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status.equals("false")) {
                    sessionHelper.logoutUser();
                } else {
                    UsersDatabase dbuser = new UsersDatabase(getApplicationContext());
                    dbuser.deleteuser(senderid);
                    messageDatabase.deleteuser(senderid);
                    dbuser.close();
                    messageDatabase.close();

                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    finish();
                    startActivity(i);
                    overridePendingTransition(ANIM_NEXT_ACTIVITY_IN, ANIM_CURRENT_ACTIVITY_OUT);
                }
                break;

            case Const.ServiceCode.SEND_FILE:

                String responseString = null;
                JSONObject obja;
                try {
                    obja = new JSONObject(response);
                    String success = obja.getString("success");
                    status = success;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (status.equals("1")) {
                    responseString = "OK";
                } else {
                    responseString = "Error occurred! Http Status Code: " + serviceCode;
                }

                if(responseString.equalsIgnoreCase("OK")) {
                    message = filename;
                    sendMesage();
                    emoji_edittext.setText("");
                    Date todaydate = new Date();
                    hasAttachment = false;
                    String date = String.valueOf(todaydate.getTime());
                    messageDatabase.createMeassage(new MessageModel(fullname, "", message, myuserid, senderid, date, "individual", "yes",
                            "", "yes", messagetype));
                    getmessages();
                }

                break;

            case Const.ServiceCode.SEND_MESSAGE:

                JSONObject objto;
                try {
                    objto = new JSONObject(response);
                    String success = objto.getString("success");
                    status = success;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (status.equals("false")) {
                    sessionHelper.logoutUser();
                } else if (status.equals("block")) {
                    Toast.makeText(getApplicationContext(), "User blocked!", Toast.LENGTH_LONG).show();
                }

                break;

            case Const.ServiceCode.GET_USER_PROFILE:

                JSONObject obje;
                try {
                    obje = new JSONObject(response);
                    String success = obje.getString("success");
                    if (success.equals("1")) {

                        JSONArray array = obje.getJSONArray("users");

                        System.out.println("array ---" + array.length());

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

                        status = success;
                        userlistos = userlist;
                    } else if (success.equals("2")) {

                        status = success;
                        String message = obje.getString("message");
                    } else {
                        status = success;
                        String message = obje.getString("message");
                    }
                } catch (JSONException e) {
                    Log.e("JSON Parser", "Error parsing data " + e.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (status.equalsIgnoreCase("1")) {

                    userlist = userlistos;
                    String photourl = Const.ServiceType.HOST_URL + userlist.get(0).getProfilepic();
                    ImageUtil.displayImage(roundedImageView, photourl, null);

                } else if (status.equalsIgnoreCase("false")) {
                    sessionHelper.logoutUser();
                }

                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        try{
            getApplicationContext().unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK) {

            if (requestCode == MY_CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {

                messagetype = "photo";
                hasAttachment = true;
                filePath = selectedattachment.getPath();

                HashMap<String, String> map = new HashMap<String, String>();

                map.put(Const.URL, Const.ServiceType.SEND_FILE);
                map.put(Const.Params.PICTURE, filePath);

                new MultiPartRequester(this, map, Const.ServiceCode.SEND_FILE, this);

            } else if (requestCode == MY_IMAGE_PICK_REQUEST_CODE) {

                Uri selectedImageUri = data.getData();
                filePath = getRealPathFromURI(selectedImageUri);
                messagetype = "photo";
                hasAttachment = true;
                filename = new String (new File(filePath).getName());
                sendFile(selectedImageUri);
            }

            else if (requestCode == MY_CAMERA_CAPTURE_VIDEO_REQUEST_CODE ) {

                messagetype = "video";
                MediaPlayer mp = MediaPlayer.create(this, selectedattachment);
                millis = mp.getDuration();
                mp.release();
                if (millis < 45000) {
                    hasAttachment = true;
                    filePath = selectedattachment.getPath();

                    HashMap<String, String> map = new HashMap<String, String>();

                    map.put(Const.URL, Const.ServiceType.SEND_FILE);
                    map.put(Const.Params.PICTURE, filePath);
                    new MultiPartRequester(this, map, Const.ServiceCode.SEND_FILE, this);

                } else {
                    hasAttachment = false;
                    warningFile("Video");
                }
            }

            else if (requestCode == MY_AUDIO_REQUEST_CODE) {
                Uri selectedImageUri = data.getData();
                filePath = getRealPathFromURI(selectedImageUri);
                messagetype = "audio";
                MediaPlayer mp = MediaPlayer.create(this, selectedImageUri);
                millis =  mp.getDuration();
                mp.release();
                if(millis < 45000) {
                    filename = new String (new File(filePath).getName());

                    sendFile(selectedImageUri);
                }else {
                    warningFile("Audio");
                }
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] imgdata = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        android.database.Cursor cursor = managedQuery(contentUri, imgdata, // Which
                null, // WHERE clause; which rows to return (all rows)
                null, // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void sendFile(Uri uri) {
        File f = new File(getRealPathFromURI(uri));
        String selectedfile = f.getAbsolutePath();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.SEND_FILE);
        map.put(Const.Params.PICTURE, selectedfile);
        new MultiPartRequester(this, map, Const.ServiceCode.SEND_FILE, this);
    }

    public void warningFile(String filetype) {
        AlertDialog.Builder confirm = new AlertDialog.Builder(this);
        confirm.setTitle(filetype + getString(R.string.too_long));
        confirm.setMessage(getString(R.string.select_new) + filetype);
        confirm.setNegativeButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                AddAttachement();
            }
        });
        confirm.show().show();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(ChatActivity.this, MainActivity.class);
        finish();
        startActivity(i);
        overridePendingTransition(ANIM_NEXT_ACTIVITY_IN, ANIM_CURRENT_ACTIVITY_OUT);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("file_uri", selectedattachment);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // get the file url
        selectedattachment = savedInstanceState.getParcelable("file_uri");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.block) {
            Alertblockfriend();
            return true;
        } else if (id == R.id.delete) {
            deleteconversation();
            return true;
        } else if (id == android.R.id.home) {
            Intent i = new Intent(this, MainActivity.class);
            finish();
            startActivity(i);
            overridePendingTransition(ANIM_NEXT_ACTIVITY_IN, ANIM_CURRENT_ACTIVITY_OUT);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void Alertblockfriend() {
        // set the flag to true so the next activity won't start up
        new AlertDialog.Builder(this)
                .setTitle(R.string.quest_block_fr)
                .setMessage(R.string.sure_block_fr)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface arg0, int arg1) {
                                blockFriend();
                            }
                        }).create().show();
    }

    private void blockFriend() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.BLOCK_FRIEND);
        map.put(Const.Params.USERID, myuserid);
        map.put(Const.Params.FRIEND_ID, senderid);
        map.put(Const.Params.DEVICE_ID, deviceid);
        requestQueue.add(new VolleyHttpRequest(Request.Method.POST, map, Const.ServiceCode.BLOCK_FRIEND, this, this));
    }

    public void deleteconversation() {
        messageDatabase.deleteuser(senderid);
        Intent i = new Intent(ChatActivity.this, ChatActivity.class);
        finish();
        startActivity(i);
        overridePendingTransition(ANIM_NEXT_ACTIVITY_IN, ANIM_CURRENT_ACTIVITY_OUT);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
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

}
