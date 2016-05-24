package com.example.chattingapp.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chattingapp.R;
import com.example.chattingapp.activity.GroupChatActivity;
import com.example.chattingapp.activity.ChatActivity;
import com.example.chattingapp.database.GroupDatabase;
import com.example.chattingapp.database.MessageDatabase;
import com.example.chattingapp.database.UsersDatabase;
import com.example.chattingapp.model.MessageModel;
import com.example.chattingapp.utils.Const;
import com.example.chattingapp.utils.ImageUtil;
import com.example.chattingapp.utils.SessionHelper;
import com.example.chattingapp.views.RoundedImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import github.ankushsachdeva.emojicon.EmojiconTextView;

/**
 * Created by elfassimounir on 4/29/16.
 */
public class MyMessagesFragment extends Fragment {


    private static final int ANIM_CURRENT_ACTIVITY_OUT = R.anim.push_left_out;
    private static final int ANIM_NEXT_ACTIVITY_IN = R.anim.push_right_in;

    Context context;
    ListView listView;
    String myusername;
    SessionHelper session;
    MessageDatabase dbmsg;
    ArrayList<MessageModel> msglist = new ArrayList<MessageModel>();
    UsersDatabase dbuser;
    SharedPreferences prefs;
    String status = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_my_messages, container, false);
        context = getActivity();

        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        status = prefs.getString("status", "Available");

        dbuser = new UsersDatabase(context);
        dbmsg = new MessageDatabase(context);
        listView = (ListView) rootView.findViewById(R.id.peerMsgList);
        session = new SessionHelper(context);
        myusername = session.getuserid();
        context.registerReceiver(broadcastReceiver, new IntentFilter("CHAT_MESSAGE_RECEIVED"));

        getdata();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {

                if (msglist.get(position).getChattype().equals("menu_chat")) {
                    Intent i = new Intent(context, ChatActivity.class);
                    i.putExtra("senderid",msglist.get(position).getFromuserid());
                    startActivity(i);
                    getActivity().overridePendingTransition(ANIM_NEXT_ACTIVITY_IN, ANIM_CURRENT_ACTIVITY_OUT);

                }
                if (msglist.get(position).getChattype().equals("group")) {
                    Intent i = new Intent(context, GroupChatActivity.class);
                    Const.groupid = msglist.get(position).getGroupid();
                    i.putExtra("groupid",msglist.get(position).getGroupid());
                    startActivity(i);
                    getActivity().overridePendingTransition(ANIM_NEXT_ACTIVITY_IN, ANIM_CURRENT_ACTIVITY_OUT);

                }
            }
        });

        return rootView;

    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getdata();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        try{
            context.unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void getdata() {

        List<MessageModel> messagelist = dbmsg.messagesList();

        if (msglist.size() > 0) {
            msglist.clear();
        }
        MessageModel ms;
        for (MessageModel msg : messagelist) {

            ms = new MessageModel();
            String groupid = "", senderid = "";
            String chattype = msg.getChattype();

            if (chattype.equals("group")) {

                groupid = msg.getGroupid();

                String firstname = msg.getFname();

                GroupDatabase db = new GroupDatabase(context);
                String groupname = "";

                groupname = db.getGroupname(groupid);

                String lastname = msg.getLname();

                String logintype = "group";
                String msgtype = msg.getMsgtype();
                String message  = "";
                if(msgtype != null) {
                    if(msgtype.equals("text") ) {
                        message = msg.getMessage();
                    } else if(msgtype.equals("audio")) {
                        message = "Audio";
                    } else if(msgtype.equals("video")) {
                        message = "Video";
                    } else if(msgtype.equals("photo")) {
                        message = "Photo";
                    }
                } else {
                    msgtype = "text";
                    message = msg.getMessage();
                }

                String timestamp = msg.getTimestamp();
                String isseen = msg.getIsseen();
                ms.setIsseen(isseen);
                ms.setGroupid(groupid);
                ms.setChattype(chattype);
                ms.setFname(firstname);
                ms.setLname(lastname);
                ms.setLogintype(logintype);
                ms.setMessage(message);
                ms.setTimestamp(timestamp);
                ms.setGroupname(groupname);
                msglist.add(ms);

            } else if (chattype.equals("menu_chat")) {

                senderid = msg.getFromuserid();

                String firstname = msg.getFname();

                String lastname = msg.getLname();

                String logintype = "";

                if(msg.getLogintype() != null) {
                    logintype = msg.getLogintype();
                }
                else {
                    logintype = "";
                }

                String msgtype = msg.getMsgtype();
                String message  = "";

                if(msgtype != null) {
                    if(msgtype.equals("text") ) {
                        message = msg.getMessage();
                    } else if(msgtype.equals("audio")) {
                        message = "Audio";
                    } else if(msgtype.equals("video")) {
                        message = "Video";
                    } else if(msgtype.equals("photo")) {
                        message = "Photo";
                    }
                } else {
                    msgtype = "text";
                    message = msg.getMessage();
                }

                String timestamp = msg.getTimestamp();
                String isseen = msg.getIsseen();
                ms.setIsseen(isseen);
                ms.setFromuserid(senderid);
                ms.setChattype(chattype);
                ms.setFname(firstname);
                ms.setLname(lastname);
                ms.setLogintype(logintype);
                ms.setMessage(message);

                ms.setTimestamp(timestamp);
                msglist.add(ms);
            }
        }

        if (msglist.size() > 0) {
            ListAdapter img = new ListAdapter(context);
            listView.setAdapter(img);
        }
    }

    public class ListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        ArrayList<MessageModel> amList = msglist;

        public ListAdapter(Context c) {
            mInflater = LayoutInflater.from(c);
        }

        public int getCount() {
            return amList.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View v, ViewGroup parent) {

            ViewHolder holder = null;

            if (v == null) {
                holder = new ViewHolder();
                v = mInflater.inflate(R.layout.list_item_message, null);

                holder.name = (TextView) v.findViewById(R.id.list_am_name_top);
                holder.sub_name = (TextView) v.findViewById(R.id.list_am_name_bottom);
                holder.content = (EmojiconTextView) v.findViewById(R.id.list_am_content);
                holder.time = (TextView) v.findViewById(R.id.list_am_time);
                holder.seencount = (TextView) v.findViewById(R.id.seencount);
                holder.img_icon = (RoundedImageView) v.findViewById(R.id.user_image);

                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            if (amList.get(position).getChattype().equalsIgnoreCase("group")) {

                holder.name.setText(amList.get(position).getGroupname());
                holder.name.setTag("group");

                holder.sub_name.setTag(amList.get(position).getGroupid());
                holder.sub_name.setText("");

                holder.content.setText(amList.get(position).getMessage());

                int seencount = dbmsg.getunseengroupcount(amList.get(position)
                        .getGroupid(), "no");
                if (seencount > 0) {
                    holder.seencount.setText("" + seencount);
                    holder.seencount.setVisibility(View.VISIBLE);
                    holder.content.setTextColor(getResources().getColor(R.color.material_blue_500));
                } else {
                    holder.seencount.setVisibility(View.INVISIBLE);
                    holder.seencount.setText("");
                }
            }

            if (amList.get(position).getChattype().equalsIgnoreCase("menu_chat")) {
                holder.name.setText(amList.get(position).getFname() + " " + amList.get(position).getLname());
                holder.name.setTag("menu_chat");
                holder.sub_name.setText("");
                holder.sub_name.setTag(amList.get(position).getFromuserid());

                int seencount = dbmsg.getunseeenindividual(amList.get(position).getFromuserid(), "no");
                if (seencount > 0) {
                    holder.seencount.setText("" + seencount);
                    holder.seencount.setVisibility(View.VISIBLE);
                    holder.content.setTextColor(getResources().getColor(R.color.material_blue_500));

                } else {
                    holder.seencount.setText("");
                    holder.seencount.setVisibility(View.INVISIBLE);
                }
            }

            String logintype = amList.get(position).getLogintype();

            if (logintype.equals("group")){
                ImageUtil.displayImage(holder.img_icon, "", null);
            }else if (logintype.equals("email")) {
                String userid = amList.get(position).getFromuserid();
                String photourl = Const.ServiceType.HOST_URL + dbuser.getPicUrl(userid);
                ImageUtil.displayImage(holder.img_icon, photourl, null);
            }

            holder.content.setText(amList.get(position).getMessage());
            String time = amList.get(position).getTimestamp();
            // time = getCurrentTimeStamp(time);
            time = gettime(Long.valueOf(time));
            holder.time.setText("" + time);

            return v;
        }

        class ViewHolder {
            EmojiconTextView content;
            TextView name, sub_name, time, seencount;
            RoundedImageView img_icon;
        }
    }

    public String gettime(long timestamp) {
        String time = "";
        if (DateUtils.isToday(timestamp)) {
            SimpleDateFormat sdf1 = new SimpleDateFormat("hh:mm");
            time = sdf1.format(new Date(timestamp));

        } else {
            SimpleDateFormat sdf1 = new SimpleDateFormat("MM/dd/yyyy");
            time = sdf1.format(new Date(timestamp));
        }
        return time;
    }
}