package com.example.chattingapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.chattingapp.model.GroupModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elfassimounir on 3/30/16.
 */
public class GroupDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "groupchat_db";

    public GroupDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String TABLE_NAME = "grouptable";

    private static final String ID = "id";
    private static final String GROUPID = "groupid";
    private static final String GROUPNAME = "groupname";
    private static final String MEMBERID = "memberid";
    private static final String MEMBERNAME = "membername";
    private static final String ADMINID = "adminid";
    private static final String TIMESTAMP = "timestamp";

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + ID + " INTEGER PRIMARY KEY," + GROUPID + " TEXT, "
                + GROUPNAME + " TEXT, " +  MEMBERID + " TEXT, " +  ADMINID + " TEXT, " +  MEMBERNAME + " TEXT, "
                +  TIMESTAMP + " INTEGER" + ")";

        db.execSQL(CREATE_TABLE);
    }

    public void createGroup(GroupModel grp) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(GROUPID, grp.getGroupid());
        values.put(GROUPNAME, grp.getGroupname());
        values.put(MEMBERID, grp.getMemberid());
        values.put(MEMBERNAME, grp.getMembername());
        values.put(ADMINID, grp.getAdminid());
        values.put(TIMESTAMP, grp.getTimestamp());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void updateGroupName(String groupid, String groupname) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(GROUPNAME, groupname);

        db.update(TABLE_NAME, values, GROUPID + " = ?", new String[]{groupid});
    }

    public void deleteGroup(String groupid) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, GROUPID + "=?", new String[]{groupid});

        db.close();
    }

    public String getGroupname(String groupid) {
        String groupname = "";

        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE "+GROUPID+"=? GROUP BY "+GROUPID;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] {groupid});

        if (cursor.moveToFirst()) {
            do {
                groupname = cursor.getString(cursor.getColumnIndexOrThrow(GROUPNAME));

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return groupname;
    }

    public int groupUsersNbr(String groupid, String memberid) {
        int count = 0;

        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE "+GROUPID+"=? AND "+MEMBERID +"=?";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] {groupid, memberid});

        count = cursor.getCount();

        cursor.close();
        db.close();

        return count;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
    }
}
