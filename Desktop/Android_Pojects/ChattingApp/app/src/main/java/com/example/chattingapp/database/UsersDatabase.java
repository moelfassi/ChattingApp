package com.example.chattingapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.chattingapp.model.UserModel;


/**
 * Created by elfassimounir on 4/30/16.
 */
public class UsersDatabase extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "userchat_db";

    private static final String TABLE_NAME = "usertable";

    public UsersDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String ID = "id";
    private static final String USERID = "userid";
    private static final String FIRSTNAME = "firstname";
    private static final String LASTNAME = "lastname";
    private static final String PROFILEPIC = "profilepic";

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + ID + " INTEGER PRIMARY KEY," + USERID + " TEXT, "
                + FIRSTNAME + " TEXT, " + LASTNAME + " TEXT, " + PROFILEPIC + " TEXT " + ")";

        db.execSQL(CREATE_TABLE);
        Log.i("onCreate","nn");
    }

    public void addUser(UserModel user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(USERID, user.getUserid());
        values.put(FIRSTNAME, user.getFirstname());
        values.put(LASTNAME, user.getLastname());
        values.put(PROFILEPIC, user.getProfilepic());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public String getPicUrl (String userid) {
        String profilepicurl = "";
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE "+ USERID+"=? LIMIT 1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] {userid});

        if (cursor.moveToFirst()) {
            do {
                profilepicurl = cursor.getString(cursor.getColumnIndexOrThrow(PROFILEPIC));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return profilepicurl;
    }

    public void deleteuser(String userid) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, USERID + "=?", new String[]{userid});

        db.close();
    }

    public int checkuser(String userid) {
        int counter = 0;
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + USERID + "=?";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { userid });

        counter = cursor.getCount();
        cursor.close();
        db.close();

        return counter;
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
    }
}
