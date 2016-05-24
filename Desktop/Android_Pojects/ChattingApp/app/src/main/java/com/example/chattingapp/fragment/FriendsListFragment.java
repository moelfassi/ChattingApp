package com.example.chattingapp.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.chattingapp.R;
import com.example.chattingapp.activity.ChatActivity;
import com.example.chattingapp.database.UsersDatabase;
import com.example.chattingapp.model.UserModel;
import com.example.chattingapp.utils.AsyncTaskCompleteListener;
import com.example.chattingapp.utils.Const;
import com.example.chattingapp.utils.ImageUtil;
import com.example.chattingapp.utils.SessionHelper;
import com.example.chattingapp.utils.VolleyHttpRequest;
import com.example.chattingapp.views.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by elfassimounir on 5/16/16.
 */
public class FriendsListFragment extends Fragment implements AsyncTaskCompleteListener, Response.ErrorListener  {

    private RequestQueue requestQueue;
    ListView listView;
    Context context;
    private ProgressDialog progress;
    String myusername, deviceid, selecteduser;
    SessionHelper session;
    UsersDatabase dbuser;
    ArrayList<UserModel> userlist = new ArrayList<UserModel>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_friendslist, container, false);

        context = getActivity();

        requestQueue = Volley.newRequestQueue(context);

        listView = (ListView) rootView.findViewById(R.id.friendslistview);
        session = new SessionHelper(context);
        deviceid = session.getdeviceid();
        myusername = session.getuserid();

        dbuser = new UsersDatabase(context);

        getfriends();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                TextView sub_name = (TextView) view.findViewById(R.id.list_am_name_bottom);
                TextView name = (TextView) view.findViewById(R.id.list_am_name_top);
                Intent i = new Intent(context, ChatActivity.class);
                Const.senderid = sub_name.getTag().toString();
                Const.fullname = name.getText().toString();
                startActivity(i);
            }
        });
        return rootView;
    }

    private void getfriends() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.GET_MY_FRIENDS);
        map.put(Const.Params.USERID, myusername);
        map.put(Const.Params.DEVICE_ID, deviceid);
        progress = ProgressDialog.show(context, getString(R.string.getting_friends), getString(R.string.please_wait));
        requestQueue.add(new VolleyHttpRequest(Request.Method.POST, map, Const.ServiceCode.GET_MY_FRIENDS, this, this));
    }


    @Override
    public void onTaskCompleted(String response, int serviceCode) {

        switch (serviceCode) {

            case Const.ServiceCode.GET_MY_FRIENDS:

                UsersDatabase dbuser = new UsersDatabase(context);
                try {
                    JSONObject objtos = new JSONObject(response);

                    String success = objtos.getString("success");
                    progress.dismiss();
                    if (success.equals("1")) {

                        JSONArray array = objtos.getJSONArray("users");
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
                            String status = new String(array.getJSONObject(i).getString("status").getBytes("UTF-8"), "UTF-8");

                            data.setGender(gender);
                            data.setUserid(userid);
                            data.setFirstname(firstname);
                            data.setLastname(lastname);
                            data.setProfilepic(profilepic);
                            data.setStatus(status);

                            userlist.add(data);

                            if (dbuser.checkuser(userid) < 1) {
                                dbuser.addUser(new UserModel(userid, firstname, lastname, profilepic));
                            }
                        }

                        Const.status = success;
                        Const.alluserlist = userlist;
                    } else if (success.equals("2")) {
                        Const.status = success;
                        String message = objtos.getString("message");
                    } else {
                        Const.status = success;
                        String message = objtos.getString("message");
                        Const.message = message;
                    }
                } catch (JSONException e) {
                    Log.e("JSON Parser", "Error parsing data " + e.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (Const.status.equalsIgnoreCase("1")) {
                    userlist = Const.alluserlist;
                    FriendsAdapter adapter = new FriendsAdapter(context);
                    adapter.notifyDataSetChanged();
                    listView.setAdapter(adapter);
                } else if (Const.status.equalsIgnoreCase("false")) {
                    session.logoutUser();
                }
                break;
        }

    }

    public class FriendsAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        ArrayList<UserModel> amList = Const.alluserlist;

        public FriendsAdapter(Context c) {
            mInflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            return amList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {

            ViewHolder holder = null;

            if (v == null) {
                holder = new ViewHolder();
                v = mInflater.inflate(R.layout.user_search_row, null);

                holder.name = (TextView) v.findViewById(R.id.list_am_name_top);
                holder.sub_name = (TextView) v.findViewById(R.id.list_am_name_bottom);
                holder.content = (TextView) v.findViewById(R.id.list_am_content);
                holder.img_icon = (RoundedImageView) v.findViewById(R.id.user_image);
                holder.btnaddfriend = (Button) v.findViewById(R.id.btnaddfriend);
                holder.btnaddfriend.setVisibility(View.GONE);

                v.setTag(holder);

            } else {
                holder = (ViewHolder) v.getTag();
            }

            holder.name.setText(amList.get(position).getFirstname() + " " + amList.get(position).getLastname());
            holder.name.setTag("menu_chat");
            holder.btnaddfriend.setTag("" + amList.get(position).getUserid());
            holder.sub_name.setText(amList.get(position).getStatus());
            holder.sub_name.setTag(amList.get(position).getUserid());

            String photourl = Const.ServiceType.HOST_URL + amList.get(position).getProfilepic();
            ImageUtil.displayImage(holder.img_icon, photourl, null);

            holder.content.setText(amList.get(position).getCity() + ", " + amList.get(position).getCountry());
            return v;
        }

        class ViewHolder {
            TextView name, sub_name, content;
            RoundedImageView img_icon;
            Button btnaddfriend;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

}
