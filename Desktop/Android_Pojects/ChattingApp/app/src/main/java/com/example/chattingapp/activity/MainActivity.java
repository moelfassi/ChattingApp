package com.example.chattingapp.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chattingapp.R;
import com.example.chattingapp.fragment.CreateGroupFragment;
import com.example.chattingapp.fragment.FriendsListFragment;
import com.example.chattingapp.fragment.SearchUsersFragment;
import com.example.chattingapp.fragment.BlockedFriendsFragment;
import com.example.chattingapp.fragment.MyMessagesFragment;
import com.example.chattingapp.utils.Const;
import com.example.chattingapp.utils.ImageUtil;
import com.example.chattingapp.utils.SessionHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by elfassimounir on 4/30/16.
 */

public class MainActivity extends AppCompatActivity {

    SessionHelper sessionHelper;
    String userid, profileurl, deviceid, name;
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionHelper = new SessionHelper(this);
        userid = sessionHelper.getuserid();
        name = sessionHelper.firstname() + " " + sessionHelper.lastname();
        profileurl = sessionHelper.getProfilePic();
        deviceid = sessionHelper.getdeviceid();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);

        View headerLayout = nvDrawer.inflateHeaderView(R.layout.nav_header);

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        ImageView ivHeaderPhoto = (ImageView) headerLayout.findViewById(R.id.imageView);
        ImageUtil.displayImage(ivHeaderPhoto, Const.ServiceType.HOST_URL+sessionHelper.getProfilePic(), null);

        TextView ivHeaderText = (TextView) headerLayout.findViewById(R.id.tvheader);
        TextView ivHeaderName = (TextView) headerLayout.findViewById(R.id.tvheadername);
        ivHeaderText.setText(userid);

        ivHeaderName.setText(name);
        drawerToggle = setupDrawerToggle();

        mDrawer.setDrawerListener(drawerToggle);
        setupDrawerContent(nvDrawer);

        Fragment fragment = null;
        Class fragmentClass;
        fragmentClass = MyMessagesFragment.class;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment = null;
        Class fragmentClass;
        switch(menuItem.getItemId()) {
            case R.id.nav_first_fragment:
                fragmentClass = MyMessagesFragment.class;
                break;
            case R.id.nav_second_fragment:
                fragmentClass = SearchUsersFragment.class;
                break;
            case R.id.nav_third_fragment:
                fragmentClass = FriendsListFragment.class;
                break;
            case R.id.nav_fourth_fragment:
                fragmentClass = BlockedFriendsFragment.class;
                break;
            case R.id.nav_fifth_fragment:
                fragmentClass = CreateGroupFragment.class;
                break;
            case R.id.nav_sixth_fragment:
                fragmentClass = CreateGroupFragment.class;
                break;
            case R.id.nav_sev_fragment:
                fragmentClass = BlockedFriendsFragment.class;
                break;
            case R.id.nav_eighth_fragment:
                fragmentClass = SearchUsersFragment.class;
                break;

            default:
                fragmentClass = MyMessagesFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }

    public void shareapp() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, "Share");
        startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                    .setTitle(R.string.really_exit)
                    .setMessage(R.string.sure_exit)
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0,
                                                    int arg1) {
                                    Intent intent = new Intent(Intent.ACTION_MAIN);
                                    intent.addCategory(Intent.CATEGORY_HOME);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    finish();
                                    startActivity(intent);
                                }
                            }).create().show();

    }
}
