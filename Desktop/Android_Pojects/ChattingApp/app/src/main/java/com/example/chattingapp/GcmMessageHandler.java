package com.example.chattingapp;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.example.chattingapp.activity.MainActivity;
import com.example.chattingapp.database.GroupDatabase;
import com.example.chattingapp.database.MessageDatabase;
import com.example.chattingapp.model.GroupModel;
import com.example.chattingapp.model.MessageModel;
import com.example.chattingapp.utils.SessionHelper;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by elfassimounir on 3/30/16.
 */
public class GcmMessageHandler extends IntentService {

    Handler handler;
    Context context;
    SessionHelper session;
    MessageDatabase dbmsg;
    GroupDatabase dbgroup;
    boolean groupalert;
    SharedPreferences prefs;
    public GcmMessageHandler() {
        super("GcmIntentService");
    }

    public static final String TAG = "GCMNotifIntentService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        context = getApplicationContext();
        session = new SessionHelper(context);
        dbmsg = new MessageDatabase(context);
        dbgroup = new GroupDatabase(context);
        final Bundle extras = intent.getExtras();

        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        groupalert = prefs.getBoolean("cbalert", true);
        // notifies user
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        if(session.isLoggedIn()) {
            String messageType = gcm.getMessageType(intent);

            if (!extras.isEmpty()) {
                if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                    // sendNotification("Send error: " + extras.toString());
                    Log.i(TAG, "Gcm.MESSAGE_TYPE_SEND_ERROR @ " + GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR);

                } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                    // sendNotification("Deleted messages on server: "
                    // + extras.toString());
                    Log.i(TAG, "MESSAGE_TYPE_DELETED@ " + GoogleCloudMessaging.MESSAGE_TYPE_DELETED);

                } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                    Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                    Log.i(TAG, "Received: " + extras.toString());

                    String chattype = "";
                    String myuserid = session.getuserid();
                    String message = intent.getExtras().getString("message");
                    chattype = intent.getExtras().getString("chattype");
                    String firstname = intent.getExtras().getString("title"); // firstname
                    String senderid = intent.getExtras().getString("senderid");
                    String logintype = intent.getExtras().getString("logintype");
                    String sender_lname = intent.getExtras().getString("sender_lname");
                    String msgtype = intent.getExtras().getString("messagetype");
                    System.out.println("msgtype==="+msgtype);

                    if(chattype.equals("individual")) {
                        Date todaydate = new Date();

                        String date = String.valueOf(todaydate.getTime());
                        System.out.println("senderid==="+senderid);
                        dbmsg.createMeassage(new MessageModel(firstname, sender_lname, message, myuserid, senderid, date, chattype, "no", "", "no", msgtype));
                        generateNotification(context, firstname, message, senderid, "individual");

                    } else if(chattype.equals("group")) {
                        Date todaydate = new Date();

                        String date = String.valueOf(todaydate.getTime());
                        String groupid = intent.getExtras().getString("groupid");

                        System.out.println("groupid === "+groupid);

                        dbmsg.createMeassage(new MessageModel(firstname, sender_lname, message, myuserid, senderid, date, chattype, "no", groupid, "no", msgtype));
                        generateNotification(context, firstname, message, groupid, "group");

                    } else if(chattype.equals("alert")) {
                        Date todaydate = new Date();

                        String date = String.valueOf(todaydate.getTime());
                        String groupid = intent.getExtras().getString("groupid");
                        String memberid =  intent.getExtras().getString("memberid");
                        String membername =  intent.getExtras().getString("membername");
                        String groupname =  intent.getExtras().getString("title");
                        String adminid =  intent.getExtras().getString("adminid");

                        if(memberid.contains(",")) {
                            List<String> memidlist = Arrays.asList(memberid.split("\\s*,\\s*"));
                            List<String> memnamelist = Arrays.asList(membername.split("\\s*,\\s*"));

                            for(int i=0; i < memidlist.size(); i++) {
                                String memid = memidlist.get(i).toString();
                                String memname = memnamelist.get(i).toString();
                                dbgroup.createGroup(new GroupModel(groupid, groupname, memid, memname, date, adminid));
                            }
                        }
                        else {
                            dbgroup.createGroup(new GroupModel(groupid, groupname, memberid, membername, date, adminid));
                        }
                        generateNotification(context, firstname, message, groupid, "group");

                    } else if(chattype.equals("delete")) {
                        String groupid = intent.getExtras().getString("groupid");
                        dbgroup.deleteGroup(groupid);
                        dbmsg.deletegroup(groupid);

                    } else if(chattype.equals("update")) {
                        Date todaydate = new Date();

                        String date = String.valueOf(todaydate.getTime());
                        String groupid = intent.getExtras().getString("groupid");
                        String memberid =  intent.getExtras().getString("memberid");
                        String membername =  intent.getExtras().getString("membername");
                        String groupname =  intent.getExtras().getString("title");
                        String adminid =  intent.getExtras().getString("adminid");

                        dbgroup.updateGroupName(groupid, groupname);

                        if(memberid.contains(",")) {
                            List<String> memidlist = Arrays.asList(memberid.split("\\s*,\\s*"));
                            List<String> memnamelist = Arrays.asList(membername.split("\\s*,\\s*"));

                            for(int i=0; i < memidlist.size(); i++) {
                                String memid = memidlist.get(i).toString();
                                String memname = memnamelist.get(i).toString();
                                System.out.println("memid---"+memid);
                                if(dbgroup.groupUsersNbr(groupid, memid) < 1) {
                                    dbgroup.createGroup(new GroupModel(groupid, groupname, memid, memname, date, adminid));
                                }
                            }
                        }
                        else {
                            if(dbgroup.groupUsersNbr(groupid, memberid) < 1) {
                                dbgroup.createGroup(new GroupModel(groupid, groupname, memberid, membername, date, adminid));
                            }
                        }
                        generateNotification(context, firstname, message, groupid, "group");
                    }
                    Intent i = new Intent("CHAT_MESSAGE_RECEIVED");
                    i.putExtra("message", extras.getString("message"));
                    context.sendBroadcast(i);
                }
            }
        }
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }

    public void generateNotification(Context context, String title, String message, String id, String type) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification ;

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setAction("splash");
        notificationIntent.putExtra("FromNotification", true);

        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setAutoCancel(false);
        builder.setTicker(message);
        builder.setContentTitle(title);
        builder.setContentText("You have a new message");
        builder.setSmallIcon(R.drawable.logo_notif);
        builder.setContentIntent(intent);
        builder.setSubText(message);   //API level 16
        builder.setNumber(100);
        builder.setAutoCancel(true);
        builder.setSound(soundUri);
        builder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
        builder.setLights(Color.BLUE, 2000, 2000);
        builder.setOngoing(false);
        builder.build();

        notification = builder.getNotification();
        //int notificationid = Integer.parseInt(id);
        //BigInteger   df = BigInteger.pa;
        notificationManager.notify(11, notification);
    }
}
