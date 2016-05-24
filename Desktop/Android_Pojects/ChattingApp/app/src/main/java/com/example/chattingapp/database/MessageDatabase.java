package com.example.chattingapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.chattingapp.model.GroupModel;
import com.example.chattingapp.model.MessageModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elfassimounir on 3/29/
 */

public class MessageDatabase extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "messagechat_db";

    public MessageDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String TABLE_NAME = "messagetable";

    private static final String ID = "id";
    private static final String SENDER_FNAME = "senderfname";
    private static final String SENDER_LNAME = "senderlname";
    private static final String MESSAGE = "message";
    private static final String SENDER_USERID = "senderuserid";
    private static final String MY_USERID = "myuserid";
    private static final String CHATTYPE = "chattype";
    private static final String KEY_ISMINE = "ismine";
    private static final String TIMESTAMP = "timestamp";
    private static final String GROUPID = "groupid";
    private static final String ISSEEN = "isseen";
    private static final String MESSAGETYPE = "msgtype";

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY," + SENDER_FNAME
                + " TEXT, " + SENDER_LNAME + " TEXT, " + MESSAGE
                + " TEXT, " + KEY_ISMINE + " TEXT, " + SENDER_USERID
                + " TEXT, " + MY_USERID + " TEXT, " + ISSEEN + " TEXT, " + CHATTYPE + " TEXT, "
                + MESSAGETYPE + " TEXT, " + GROUPID + " TEXT, " + TIMESTAMP + " INTEGER" + ")";
        db.execSQL(CREATE_TABLE);
    }

    public void createMeassage(MessageModel messageModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(SENDER_FNAME, messageModel.getFname());
        values.put(SENDER_LNAME, messageModel.getLname());
        values.put(MESSAGE, messageModel.getMessage());
        values.put(SENDER_USERID, messageModel.getFromuserid());
        values.put(CHATTYPE, messageModel.getChattype());
        values.put(MY_USERID, messageModel.getMyuserid());
        values.put(KEY_ISMINE, messageModel.getIsmine());
        values.put(TIMESTAMP, messageModel.getTimestamp());
        values.put(GROUPID, messageModel.getGroupid());
        values.put(ISSEEN, messageModel.getIsseen());
        values.put(MESSAGETYPE, messageModel.getMsgtype());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<MessageModel> userDataList(String senderid) {
        List<MessageModel> messageModelList = new ArrayList<MessageModel>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + SENDER_USERID + "=? AND " + CHATTYPE
                + " =? ORDER BY TIMESTAMP";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { senderid, "individual" });

        if (cursor.moveToFirst()) {
            do {
                MessageModel messageModel = new MessageModel();

                messageModel.setFname(cursor.getString(cursor.getColumnIndexOrThrow(SENDER_FNAME)));
                messageModel.setLname(cursor.getString(cursor.getColumnIndexOrThrow(SENDER_LNAME)));
                messageModel.setGroupid(cursor.getString(cursor.getColumnIndexOrThrow(GROUPID)));
                messageModel.setFromuserid(cursor.getString(cursor.getColumnIndexOrThrow(SENDER_USERID)));
                messageModel.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(MESSAGE)));
                messageModel.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(TIMESTAMP)));
                messageModel.setIsmine(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ISMINE)));
                messageModel.setMsgtype(cursor.getString(cursor.getColumnIndexOrThrow(MESSAGETYPE)));

                messageModelList.add(messageModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return messageModelList;
    }

    public void deleteuser(String userid) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, SENDER_USERID + "=?", new String[]{userid});

        db.close();
    }

    public void deletegroup(String groupid) {

        System.out.println("title--" + groupid);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, GROUPID + "=?", new String[]{groupid});

        db.close();
    }

    public String getname(String senderid) {
        String name = "";
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + SENDER_USERID + "=?";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { senderid });

        if (cursor.moveToFirst()) {
            do {
                GroupModel grp = new GroupModel();
                String fname = cursor.getString(cursor.getColumnIndexOrThrow(SENDER_FNAME));
                String lname = cursor.getString(cursor.getColumnIndexOrThrow(SENDER_LNAME));

                name = fname + " " + lname;
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return name;
    }

    public List<MessageModel> groupMessageList(String groupid) {
        List<MessageModel> messageModelList = new ArrayList<MessageModel>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + GROUPID + "=? AND " + CHATTYPE + "=? ORDER BY TIMESTAMP";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { groupid, "group" });

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                MessageModel messageModel = new MessageModel();

                messageModel.setFname(cursor.getString(cursor.getColumnIndexOrThrow(SENDER_FNAME)));
                messageModel.setLname(cursor.getString(cursor.getColumnIndexOrThrow(SENDER_LNAME)));
                messageModel.setGroupid(cursor.getString(cursor.getColumnIndexOrThrow(GROUPID)));
                messageModel.setFromuserid(cursor.getString(cursor.getColumnIndexOrThrow(SENDER_USERID)));
                messageModel.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(MESSAGE)));
                messageModel.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(TIMESTAMP)));
                messageModel.setIsmine(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ISMINE)));
                messageModel.setMsgtype(cursor.getString(cursor.getColumnIndexOrThrow(MESSAGETYPE)));

                messageModelList.add(messageModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return messageModelList;
    }

    public void updategroupseen(String groupid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ISSEEN, "yes");
        db.update(TABLE_NAME, values, GROUPID + " = ?", new String[]{groupid});
    }

    public void updateindividualseen(String senderid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ISSEEN, "yes");
        db.update(TABLE_NAME, values, SENDER_USERID + " = ?", new String[] { senderid });
    }

    public int getallgroupmsgcount(String groupid) {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + GROUPID + "=? ORDER BY TIMESTAMP";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { groupid });

        return cursor.getCount();
    }

    public List<MessageModel> messagesList() {
        List<MessageModel> messageModelList = new ArrayList<MessageModel>();

        String selectQuery = "SELECT  * FROM messagetable WHERE chattype='menu_chat' GROUP BY  senderuserid UNION SELECT * FROM messagetable WHERE chattype='group' GROUP BY groupid ORDER BY TIMESTAMP DESC;";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                MessageModel messageModel = new MessageModel();

                messageModel.setFname(cursor.getString(cursor.getColumnIndexOrThrow(SENDER_FNAME)));
                messageModel.setLname(cursor.getString(cursor.getColumnIndexOrThrow(SENDER_LNAME)));
                messageModel.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(MESSAGE)));
                messageModel.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(TIMESTAMP)));
                messageModel.setIsmine(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ISMINE)));
                messageModel.setChattype(cursor.getString(cursor.getColumnIndexOrThrow(CHATTYPE)));
                messageModel.setGroupid(cursor.getString(cursor.getColumnIndexOrThrow(GROUPID)));
                messageModel.setFromuserid(cursor.getString(cursor.getColumnIndexOrThrow(SENDER_USERID)));
                messageModel.setIsseen(cursor.getString(cursor.getColumnIndexOrThrow(ISSEEN)));
                messageModel.setMsgtype(cursor.getString(cursor.getColumnIndexOrThrow(MESSAGETYPE)));

                messageModelList.add(messageModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return messageModelList;
    }

    public int getunseengroupcount(String groupid, String flag) {
        int count = 0;
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + GROUPID + "=? AND " + ISSEEN + "=? AND " + CHATTYPE + "=?";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { groupid, flag, "group" });

        count = cursor.getCount();

        cursor.close();
        db.close();

        return count;
    }

    public int getunseeenindividual(String senderid, String flag) {
        int count = 0;
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + SENDER_USERID + "=? AND " + ISSEEN + "=? AND " + CHATTYPE + "=?";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { senderid, flag, "menu_chat" });

        count = cursor.getCount();

        cursor.close();
        db.close();

        return count;
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
    }
}
