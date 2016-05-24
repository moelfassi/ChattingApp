package com.example.chattingapp.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.chattingapp.R;
import com.example.chattingapp.database.UsersDatabase;
import com.example.chattingapp.model.UserModel;
import com.example.chattingapp.utils.AsyncTaskCompleteListener;
import com.example.chattingapp.utils.Const;
import com.example.chattingapp.utils.SessionHelper;
import com.example.chattingapp.utils.VolleyHttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by elfassimounir on 5/18/16.
 */
public class GroupDetails extends AppCompatActivity implements AsyncTaskCompleteListener, Response.ErrorListener {

    RequestQueue requestQueue;
    UsersDatabase usersDatabase;
    ListView listView;
    TextView delete_group_tv, update_group_tv;
    EditText nameGrET;
    String myusername, deviceid;
    ProgressDialog progressDialog;
    ArrayList<UserModel> allusers;
    int listsize;
    SessionHelper sessionHelper;
    String adminid = "", groupname = "", groupid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        usersDatabase = new UsersDatabase(getApplicationContext());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));
        sessionHelper = new SessionHelper(this);
        deviceid = sessionHelper.getdeviceid();
        myusername = sessionHelper.getuserid();
        groupid = getIntent().getExtras().getString("groupid");
        nameGrET = (EditText) findViewById(R.id.grpNameEditText);
        delete_group_tv = (TextView)findViewById(R.id.txtdelete);
        update_group_tv = (TextView)findViewById(R.id.txtupdate);
        delete_group_tv.setVisibility(View.GONE);
        update_group_tv.setVisibility(View.GONE);
        nameGrET.setFocusable(false);
        listView = (ListView) findViewById(R.id.userListView);
        getgrouplist();

        delete_group_tv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(adminid.equals(myusername)) {
                    removeGroup();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.not_admin, Toast.LENGTH_LONG).show();
                }
            }
        });

        update_group_tv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(GroupDetails.this, GroupUpdate.class);
                i.putExtra("groupid",groupid);
                finish();
                startActivity(i);

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {

                Const.senderid = allusers.get(position).getUserid();

                if(allusers.get(position).getUserid().equals(myusername)) {
                    Toast.makeText(getApplicationContext(), R.string.not_mesgae_yrs, Toast.LENGTH_LONG).show();
                } else {
                    Intent i = new Intent(GroupDetails.this, ChatActivity.class);
                    finish();
                    startActivity(i);
                }
            }
        });
    }

    private void getgrouplist() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.GET_GROUP_LIST);
        map.put(Const.Params.USERID, myusername);
        map.put(Const.Params.DEVICE_ID, deviceid);
        map.put(Const.Params.GROUP_ID, groupid);
        progressDialog = ProgressDialog.show(getApplicationContext(), getString(R.string.getting_friends), getString(R.string.please_wait));
        requestQueue.add(new VolleyHttpRequest(Request.Method.POST, map, Const.ServiceCode.GET_GROUP_LIST, this, this));
    }

    private void removeGroup() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.REMOVE_GROUP);
        map.put(Const.Params.USERID, myusername);
        map.put(Const.Params.DEVICE_ID, deviceid);
        map.put(Const.Params.GROUP_ID, groupid);
        progressDialog = ProgressDialog.show(getApplicationContext(), getString(R.string.deleteing_group), getString(R.string.please_wait));
        requestQueue.add(new VolleyHttpRequest(Request.Method.POST, map, Const.ServiceCode.REMOVE_GROUP, this, this));
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        switch (serviceCode) {

            case Const.ServiceCode.REMOVE_GROUP:

                JSONObject obje;
                try {
                    obje = new JSONObject(response);
                    String success = obje.getString("success");
                    Const.status = success;

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                progressDialog.dismiss();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                finish();
                startActivity(intent);

                break;

            case Const.ServiceCode.GET_GROUP_LIST:

                try {
                    JSONObject obj = new JSONObject(response);
                    JSONObject groupobj = obj.getJSONObject("group");
                    String groupname = groupobj.getString("groupname");
                    String adminid = groupobj.getString("adminid");
                    String success = obj.getString("success");

                    if (success.equals("1")) {

                        JSONArray array = obj.getJSONArray("users");
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

                            if (usersDatabase.checkuser(userid) < 1) {
                                usersDatabase.addUser(new UserModel(userid, firstname, lastname, profilepic));
                            }
                        }

                        Const.status = success;
                        Const.alluserlist = userlist;
                        Const.adminid = adminid;
                        Const.groupname = groupname;
                    }else if (success.equals("2")) {
                        Const.status = success;
                    } else {
                        Const.status = success;
                        String message = obj.getString("message");
                        Const.message = message;
                    }
                } catch (JSONException e) {
                    Log.e("JSON Parser", "Error parsing data " + e.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                progressDialog.dismiss();
                if (Const.status.equalsIgnoreCase("1")) {

                    adminid = Const.adminid;
                    groupname = Const.groupname;
                    allusers = Const.alluserlist;
                    MembersAdapter img = new MembersAdapter(getApplicationContext(), allusers);
                    listView.setAdapter(img);
                    listsize = allusers.size();
                    setListViewHeightBasedOnChildren(listView);
                    nameGrET.setText("" + groupname);
                    if(adminid.equals(myusername)) {
                        delete_group_tv.setVisibility(View.VISIBLE);
                        update_group_tv.setVisibility(View.VISIBLE);
                    }
                }
                break;
        }
    }

    public class MembersAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<UserModel> allusers;

        public MembersAdapter(Context context, ArrayList<UserModel> userList) {
            super();
            this.mContext = context;
            this.allusers = userList;
        }

        @Override
        public int getCount() {
            return allusers.size();
        }

        @Override
        public Object getItem(int position) {
            return allusers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            if (convertView == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.member_item, parent, false);
                final ViewHolder holder = new ViewHolder();
                holder.txtmembername = (TextView) view.findViewById(R.id.txtmembername);
                holder.txtstatus = (TextView) view.findViewById(R.id.txtstatus);
                holder.txtadmin = (TextView) view.findViewById(R.id.txtadmin);
                view.setTag(holder);

            } else {
                view = convertView;
            }

            ViewHolder holder = (ViewHolder) view.getTag();

            holder.txtmembername.setTag(allusers.get(position).getUserid());
            holder.txtmembername.setText(allusers.get(position).getFirstname() + " " + allusers.get(position).getLastname());

            holder.txtstatus.setText(allusers.get(position).getStatus());
            holder.txtmembername.setTextColor(Color.BLACK);
            holder.txtstatus.setTextColor(Color.BLACK);
            if(allusers.get(position).getUserid().equals(adminid)) {
                holder.txtadmin.setText(R.string.admin);
                holder.txtadmin.setTextColor(getResources().getColor(R.color.colorAccent));
            }
            if(allusers.get(position).getUserid().equals(myusername)) {
                holder.txtadmin.setText(R.string.myself);
                holder.txtadmin.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            return view;
        }

        class ViewHolder {
            public TextView txtmembername, txtstatus, txtadmin;
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    public void onErrorResponse(VolleyError error) {}

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, MainActivity.class);
        finish();
        startActivity(i);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
