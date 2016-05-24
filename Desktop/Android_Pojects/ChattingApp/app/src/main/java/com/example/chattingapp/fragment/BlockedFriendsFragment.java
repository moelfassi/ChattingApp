package com.example.chattingapp.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
 * Created by elfassi
 */
public class BlockedFriendsFragment extends Fragment implements AsyncTaskCompleteListener, Response.ErrorListener {

    private RequestQueue requestQueue;
    ListView listView;
    ProgressDialog progressDialog;
    String myusername, deviceid, selecteduserid;
    SessionHelper sessionHelper;
    String status;
    ArrayList<UserModel> userModels;
    UsersDatabase usersDatabase;
    ArrayList<UserModel> userlist = new ArrayList<UserModel>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_blocked_friends, container, false);

        requestQueue = Volley.newRequestQueue(getActivity());
        listView = (ListView) rootView.findViewById(R.id.blockedlist);
        sessionHelper = new SessionHelper(getActivity());
        deviceid = sessionHelper.getdeviceid();
        myusername = sessionHelper.getuserid();

        usersDatabase = new UsersDatabase(getActivity());

        getBlockedFriends();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                TextView tvsenderid = (TextView) view.findViewById(R.id.list_am_name_bottom);
                TextView tvname = (TextView) view.findViewById(R.id.list_am_name_top);
                Intent i = new Intent(getActivity(), ChatActivity.class);
                i.putExtra("senderid",tvsenderid.getTag().toString());
                i.putExtra("fullname",tvname.getText().toString());
                startActivity(i);
            }
        });
        return rootView;
    }

    private void getBlockedFriends() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.GET_BLOCKED_FRIENDS);
        map.put(Const.Params.USERID, myusername);
        map.put(Const.Params.DEVICE_ID, deviceid);
        progressDialog = ProgressDialog.show(getActivity(), getString(R.string.getting_blocked_friends), getString(R.string.please_wait));
        requestQueue.add(new VolleyHttpRequest(Request.Method.POST, map, Const.ServiceCode.GET_BLOCKED_FRIEND, this, this));
    }

    private void unBlockFriend() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.UNBLOCK_FRIEND);
        map.put(Const.Params.USERID, myusername);
        map.put(Const.Params.DEVICE_ID, deviceid);
        map.put(Const.Params.FRIEND_ID, selecteduserid);

        requestQueue.add(new VolleyHttpRequest(Request.Method.POST, map, Const.ServiceCode.UNBLOCKED_FRIEND, this, this));
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        switch (serviceCode) {
            case Const.ServiceCode.GET_BLOCKED_FRIEND:

                JSONObject obj ;
                try {
                    obj = new JSONObject(response);
                    progressDialog.dismiss();

                    String success = obj.getString("success");
                    if (success.equals("1")) {

                        JSONArray array = obj.getJSONArray("users");
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

                            if (usersDatabase.checkuser(userid) < 1) {
                                usersDatabase.addUser(new UserModel(userid, firstname, lastname, profilepic));
                            }
                        }

                        status = success;
                        userModels = userlist;
                    } else if (success.equals("2")) {

                        status = success;
                        String message = obj.getString("message");
                    } else {
                        status = success;
                        String message = obj.getString("message");
                        Const.message = message;
                    }

                } catch (JSONException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (status.equalsIgnoreCase("1")) {

                    userlist = userModels ;
                    BlockedAdapter img = new BlockedAdapter(getActivity());
                    img.notifyDataSetChanged();
                    listView.setAdapter(img);
                }else if(status.equalsIgnoreCase("false")) {
                    sessionHelper.logoutUser();
                }else if(status.equalsIgnoreCase("0")) {
                    Toast.makeText(getActivity(), getString(R.string.no_user_blocked), Toast.LENGTH_LONG).show();
                }

                break;

            case Const.ServiceCode.UNBLOCKED_FRIEND:

                JSONObject obje;
                try {
                    obje = new JSONObject(response);
                    String success = obje.getString("success");
                    status = success;

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (status.equalsIgnoreCase("1")) {

                    if(userlist.size() > 0) {
                        userlist.clear();
                    }

                    Fragment fragment = null;
                    Class fragmentClass;
                    fragmentClass = BlockedFriendsFragment.class;
                    try {
                        fragment = (Fragment) fragmentClass.newInstance();
                    } catch (java.lang.InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();


                }else if(status.equalsIgnoreCase("false")) {
                    sessionHelper.logoutUser();
                }else {
                    Toast.makeText(getActivity(), getString(R.string.no_user), Toast.LENGTH_LONG).show();
                }

                break;
        }
    }

    public class BlockedAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        ArrayList<UserModel> amList = userModels;

        public BlockedAdapter(Context c) {
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
                v = mInflater.inflate(R.layout.blockeduserrow, null);

                holder.name = (TextView) v.findViewById(R.id.list_am_name_top);
                holder.sub_name = (TextView) v.findViewById(R.id.list_am_name_bottom);
                holder.content = (TextView) v.findViewById(R.id.list_am_content);
                holder.btnunblock = (Button) v.findViewById(R.id.btnunblock);
                holder.img_icon = (RoundedImageView) v.findViewById(R.id.image_user);

                holder.btnunblock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selecteduserid = v.getTag().toString();
                        unBlockFriend();
                    }
                });

                v.setTag(holder);

            } else {
                holder = (ViewHolder) v.getTag();
            }

            holder.name.setText(amList.get(position).getFirstname() + " " + amList.get(position).getLastname());
            holder.name.setTag("individual");
            holder.btnunblock.setTag(amList.get(position).getUserid());
            holder.sub_name.setText(amList.get(position).getStatus());
            holder.sub_name.setTag(amList.get(position).getUserid());

            String photourl = Const.ServiceType.HOST_URL + amList.get(position).getProfilepic();
            ImageUtil.displayImage(holder.img_icon, photourl, null);

            return v;
        }

        class ViewHolder {
            TextView name, sub_name, content;
            RoundedImageView img_icon;
            Button btnunblock;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {}
}
