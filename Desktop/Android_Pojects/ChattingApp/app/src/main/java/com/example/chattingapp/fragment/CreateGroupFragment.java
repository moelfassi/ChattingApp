package com.example.chattingapp.fragment;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.example.chattingapp.activity.MainActivity;
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
 * Created by elfassimounir on 5/17/16.
 */
public class CreateGroupFragment extends Fragment implements View.OnClickListener, AsyncTaskCompleteListener, Response.ErrorListener {

    private RequestQueue requestQueue;
    Context context;
    private EditText grpNameEditText;
    private Button grpAddButton;
    String myusername = Const.username, deviceid;
    private ProgressDialog progress;
    ListView listView;
    private ArrayList<UserModel> allusers;
    int listsize;
    SessionHelper session;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_create_group, container, false);

        context = getActivity();
        requestQueue = Volley.newRequestQueue(context);
        allusers = new ArrayList<UserModel>();
        session = new SessionHelper(context);
        deviceid = session.getdeviceid();
        myusername = session.getuserid();
        grpNameEditText = (EditText) rootView.findViewById(R.id.grpNameEditText);
        grpAddButton = (Button)rootView. findViewById(R.id.grpAddButton);
        listView = (ListView)rootView. findViewById(R.id.userListView);
        grpAddButton.setOnClickListener(this);
        getfriends();
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
                    FriendsAdapter img = new FriendsAdapter(context, allusers);
                    listView.setAdapter(img);
                    listsize = allusers.size();
                    setListViewHeightBasedOnChildren(listView);
                }
                break;

            case Const.ServiceCode.ADD_NEW_GROUP:

                progress.dismiss();
                if (Const.status.equals("false")) {
                    session.logoutUser();
                } else {
                    grpNameEditText.setText("");
                    Intent i = new Intent(context, MainActivity.class);
                    startActivity(i);
                }
                break;
        }
    }

    class FriendsAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<UserModel> allusers;
        String mydocid = Const.username;

        public FriendsAdapter(Context context, ArrayList<UserModel> userList) {
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
                holder.checkbox.setTag(allusers.get(position));
            }
            else {
                view = convertView;
                ((ViewHolder) view.getTag()).checkbox.setTag(allusers.get(position));
            }

            ViewHolder holder = (ViewHolder) view.getTag();
            holder.checkbox.setChecked(allusers.get(position).isSelected());
            holder.checkbox.setText(allusers.get(position).getFirstname() + " " +allusers.get(position).getLastname());
            holder.checkbox.setTextColor(Color.BLACK);
            if(mydocid == allusers.get(position).getUserid()) {
                holder.checkbox.setChecked(true);
                holder.checkbox.setClickable(false);
            }
            return view;
        }

        private class ViewHolder {
            public CheckBox checkbox;
        }
    }


    @Override
    public void onClick(View v) {

        if (v == grpAddButton) {
            // check if empty group name
            if (isEmptyField(grpNameEditText))
                return;
            // check if doctors selected
            if (!isUserSelected())
                return;

            addNewGroup();
        }
    }

    private void addNewGroup() {

        progress = ProgressDialog.show(context, getString(R.string.creatin_group), getString(R.string.please_wait));

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

        for (int i = 0; i < allusers.size(); i++) {
            UserModel doc = allusers.get(i);
            if (doc.isSelected()) {

                if (doc.getUserid() != myusername) {
                    stringBuilder2.append(",");
                    stringBuilder2.append(doc.getFirstname() + doc.getLastname());
                }
            }
        }

        stringBuilder.deleteCharAt(0);
        stringBuilder2.deleteCharAt(0);
        String docIds = stringBuilder.toString();
        String username = stringBuilder2.toString();

        System.out.println("username--" + username.toString());
        String grpName = grpNameEditText.getText().toString();

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.ADD_NEW_GROUP);
        map.put(Const.Params.USERID, myusername);
        map.put(Const.Params.DEVICE_ID, deviceid);
        map.put(Const.Params.GROUPNAME, grpName);
        map.put(Const.Params.ADMIN_ID, myusername);
        map.put(Const.Params.MEMBER_ID, docIds);
        map.put(Const.Params.MEMBER_NAME, username);

        requestQueue.add(new VolleyHttpRequest(Request.Method.POST, map, Const.ServiceCode.ADD_NEW_GROUP, this, this));
    }

    private boolean isEmptyField(EditText editText) {
        boolean result = editText.getText().toString().length() <= 0;
        if (result)
            Toast.makeText(context, getString(R.string.group_name_required), Toast.LENGTH_SHORT).show();
        return result;
    }

    private boolean isUserSelected() {
        boolean result = false;

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
            Toast.makeText(context, getString(R.string.no_member), Toast.LENGTH_SHORT).show();
        return result;
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
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
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

}
