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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
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
 * Created by elfassimounir on 5/19/16.
 */
public class GroupUpdate extends AppCompatActivity implements AsyncTaskCompleteListener, Response.ErrorListener {

    private ProgressDialog progressDialog;
    RequestQueue requestQueue;
    private EditText grpNameEditText;
    private Button buttonUpdGrp;
    String myusername, deviceid;
    String adminid = "", groupname = "", groupid = "";
    ListView listView;
    private ArrayList<UserModel> allusers;
    private ArrayList<UserModel> groupusers;
    int listsize;
    SessionHelper sessionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_update);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));

        sessionHelper = new SessionHelper(this);
        deviceid = sessionHelper.getdeviceid();
        myusername = sessionHelper.getuserid();
        groupid = getIntent().getExtras().getString("groupid");
        grpNameEditText = (EditText) findViewById(R.id.grpNameEditText);
        buttonUpdGrp = (Button) findViewById(R.id.grpUpdateButton);
        buttonUpdGrp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isEmptyField(grpNameEditText))
                    return;
                if (!isUserSelected())
                    return;
                updategroup();

            }
        });

        listView = (ListView) findViewById(R.id.userListView);

        allusers = new ArrayList<UserModel>();
        groupusers = new ArrayList<UserModel>();

        getfriends();
    }

    public void updategroup(){

        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilder2 = new StringBuilder();
        for (int i = 0; i < allusers.size(); i++) {
            UserModel doc = allusers.get(i);

            if (doc.isSelected()) {
                if (doc.getUserid() != myusername) {
                    stringBuilder.append(",");
                    stringBuilder.append(doc.getUserid());
                }
            }
        }

        stringBuilder.append(",");
        stringBuilder.append(myusername);

        for (int i = 0; i < allusers.size(); i++) {
            UserModel doc = allusers.get(i);

            if (doc.isSelected()) {

                if (doc.getUserid() != myusername) {
                    stringBuilder2.append(",");
                    stringBuilder2.append(doc.getFirstname() + doc.getLastname());
                }
            }
        }

        stringBuilder2.append(",");
        stringBuilder2.append(sessionHelper.firstname() + " "+ sessionHelper.lastname());

        stringBuilder.deleteCharAt(0);
        stringBuilder2.deleteCharAt(0);
        String docIds = stringBuilder.toString();
        String username = stringBuilder2.toString();
        String grpName = grpNameEditText.getText().toString();

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.UPDATE_GROUP);
        map.put(Const.Params.USERID, myusername);
        map.put(Const.Params.DEVICE_ID, deviceid);
        map.put(Const.Params.GROUP_ID, groupid);
        map.put(Const.Params.MEMBER_NAME, username);
        map.put(Const.Params.GROUPNAME, grpName);
        map.put(Const.Params.ADMIN_ID, myusername);
        map.put(Const.Params.MEMBER_ID, docIds);
        progressDialog = ProgressDialog.show(getApplicationContext(), getString(R.string.updating_group), getString(R.string.please_wait));
        requestQueue.add(new VolleyHttpRequest(Request.Method.POST, map, Const.ServiceCode.UPDATE_GROUP, this, this));
    }

    private void getfriends() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.GET_MY_FRIENDS);
        map.put(Const.Params.USERID, myusername);
        map.put(Const.Params.DEVICE_ID, deviceid);
        progressDialog = ProgressDialog.show(getApplicationContext(), getString(R.string.getting_friends), getString(R.string.please_wait));
        requestQueue.add(new VolleyHttpRequest(Request.Method.POST, map, Const.ServiceCode.GET_MY_FRIENDS, this, this));
    }

    private void getgrouplist() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.GET_GROUP_LIST);
        map.put(Const.Params.USERID, myusername);
        map.put(Const.Params.DEVICE_ID, deviceid);
        map.put(Const.Params.GROUP_ID, groupid);
        requestQueue.add(new VolleyHttpRequest(Request.Method.POST, map, Const.ServiceCode.GET_GROUP_LIST, this, this));
    }

    private boolean isEmptyField(EditText editText) {
        boolean result = editText.getText().toString().length() <= 0;
        if (result)
            Toast.makeText(getApplicationContext(), getString(R.string.group_name_required), Toast.LENGTH_SHORT).show();
        return result;
    }

    private boolean isUserSelected() {
        boolean result = true;

        Log.i("allusers0" ," "+allusers.size());
        for (int i = 0; i < allusers.size(); i++) {
            UserModel doc = allusers.get(i);
            if (doc.isSelected) {

                result = true;

                if (doc.userid == myusername) {
                    result = false;
                }
                break;
            }
        }

        if (!result)
            Toast.makeText(getApplicationContext(), R.string.no_member_selct, Toast.LENGTH_SHORT).show();
        return result;
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        switch (serviceCode) {

            case Const.ServiceCode.UPDATE_GROUP:

                progressDialog.dismiss();
                JSONObject obj;
                try {
                    obj = new JSONObject(response);
                    String success = obj.getString("success");
                    Const.status = success;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (Const.status.equals("false")) {
                    sessionHelper.logoutUser();
                } else {
                    grpNameEditText.setText("");
                    Intent i = new Intent(GroupUpdate.this, MainActivity.class);
                    finish();
                    startActivity(i);
                }

                break;

            case Const.ServiceCode.GET_MY_FRIENDS:

                UsersDatabase dbuser = new UsersDatabase(getApplicationContext());
                try {
                    JSONObject objtos = new JSONObject(response);

                    String success = objtos.getString("success");
                    progressDialog.dismiss();
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
                    allusers = Const.alluserlist;
                    getgrouplist();
                }

                break;

            case Const.ServiceCode.GET_GROUP_LIST:

                UsersDatabase dbuser1 = new UsersDatabase(getApplicationContext());
                try {
                    JSONObject obje = new JSONObject(response);
                    JSONObject groupobj = obje.getJSONObject("group");
                    String groupname = groupobj.getString("groupname");
                    String adminid = groupobj.getString("adminid");
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
                            String status = new String(array.getJSONObject(i).getString("status").getBytes("UTF-8"), "UTF-8");

                            data.setGender(gender);
                            data.setUserid(userid);
                            data.setFirstname(firstname);
                            data.setLastname(lastname);
                            data.setProfilepic(profilepic);
                            data.setStatus(status);

                            userlist.add(data);

                            if (dbuser1.checkuser(userid) < 1) {
                                dbuser1.addUser(new UserModel(userid, firstname, lastname, profilepic));
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
                        String message = obje.getString("message");
                        Const.message = message;
                    }
                } catch (JSONException e) {
                    Log.e("JSON Parser", "Error parsing data " + e.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (Const.status.equalsIgnoreCase("1")) {

                    adminid = Const.adminid;
                    groupname = Const.groupname;
                    groupusers = Const.alluserlist;

                    MembersAdapter img = null;
                    if(groupusers.size() > allusers.size()) {
                        img = new MembersAdapter(getApplicationContext(), groupusers);
                    } else {
                        img = new MembersAdapter(getApplicationContext(), allusers);
                    }

                    listView.setAdapter(img);
                    listsize = allusers.size();
                    grpNameEditText.setText("" + groupname);
                    setListViewHeightBasedOnChildren(listView);
                }
                break;
        }
    }

    public class MembersAdapter extends BaseAdapter {

        private Context mContext;
        private ArrayList<UserModel> users;

        public MembersAdapter(Context context, ArrayList<UserModel> userList) {
            super();
            this.mContext = context;
            this.users = userList;
        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int position) {
            return users.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = null;
            if(convertView == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.add_members, parent, false);
                final ViewHolder holder = new ViewHolder();
                holder.checkbox = (CheckBox) view.findViewById(R.id.checkBox);
                holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        UserModel element = (UserModel) holder.checkbox.getTag();
                        element.setSelected(buttonView.isChecked());
                    }
                });
                view.setTag(holder);
                holder.checkbox.setTag(users.get(position));
            } else {
                view = convertView;
                ((ViewHolder) view.getTag()).checkbox.setTag(users.get(position));
            }

            ViewHolder holder = (ViewHolder) view.getTag();
            holder.checkbox.setChecked(users.get(position).isSelected());
            holder.checkbox.setText(users.get(position).getFirstname() + " " +users.get(position).getLastname());
            holder.checkbox.setTextColor(Color.BLACK);
            if(myusername == users.get(position).getUserid()) {

                holder.checkbox.setChecked(true);
                holder.checkbox.setClickable(false);
            }

            for(int i =0; i < groupusers.size(); i++) {
                if(groupusers.get(i).getUserid().equals(users.get(position).getUserid())) {
                    holder.checkbox.setChecked(true);
                    holder.checkbox.setClickable(true);
                }
            }
            return view;
        }

        class ViewHolder {
            public CheckBox checkbox;
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, MainActivity.class);
        finish();
        startActivity(i);
    }

}
