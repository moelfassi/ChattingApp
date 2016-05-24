package com.example.chattingapp.utils;

import com.example.chattingapp.model.UserModel;

import java.util.ArrayList;

/**
 * Created by elfassimounir on 5/3/16.
 */
public class Const {

    public static final String IMAGE_DIRECTORY = "ChattApp";
    public static final String URL = "url";
    public static String status = "";
    public static String message = "";
    public static int position = 0;
    public static String username = "";
    public static String senderid = "";
    public static String groupid = "";
    public static String fullname = "";

    public static String PROJECT_NUMBER = "272363476243";
    public static ArrayList<UserModel> alluserlist;

    public static String groupname = "";
    public static String adminid = "";

    // service parameters
    public class Params {

        public static final String USERID = "userid";
        public static final String EMAIL = "email";
        public static final String GROUPNAME = "groupname";
        public static final String ADMIN_ID = "adminid";
        public static final String PASSWORD = "password";
        public static final String MEMBER_ID = "memberid";
        public static final String MEMBER_NAME = "membername";
        public static final String FIRSTNAME = "firstname";
        public static final String LAST_NAME = "lastname";
        public static final String GENDER = "gender";
        public static final String IMAGE = "image";
        public static final String DEVICE_TOKEN = "gcmid";
        public static final String DEVICE_ID = "deviceid";
        public static final String PROFILE_ID = "profileid";
        public static final String FROM_ID = "fromid";
        public static final String TO_ID = "toid";
        public static final String MESSAGE = "message";
        public static final String MESSAGE_TYPE = "messagetype";
        public static final String FRIEND_ID = "friendid";
        public static final String GROUP_ID = "groupid";
        public static final String PICTURE = "profilepic";
        public static final String SEARCH = "search";
    }

    public class ServiceType {

        public static final String HOST_URL = "http://192.168.1.2/";
        public static final String LINK_FILE = "http://192.168.1.2/profilepics/";
        public static final String LINK_UPS = "http://192.168.1.2/uploads/";

        private static final String BASE_URL_CHAT = HOST_URL + "chat/";
        private static final String BASE_URL = HOST_URL + "user/";

        public static final String LOGIN = BASE_URL + "login";
        public static final String REGISTER = BASE_URL + "register";
        public static final String RESET = BASE_URL + "resetpassword";
        public static final String SEARCHUSER = BASE_URL + "searchuser";

        public static final String ADD_FRIEND = BASE_URL_CHAT + "addfriend";
        public static final String GET_USER_PROFILE = BASE_URL_CHAT + "getuserprofile";
        public static final String GET_MY_FRIENDS = BASE_URL_CHAT + "getmyfriends";
        public static final String GET_BLOCKED_FRIENDS = BASE_URL_CHAT + "getblockedfriend";
        public static final String GET_GROUP_LIST = BASE_URL_CHAT + "getgroupdetail";
        public static final String ADD_NEW_GROUP = BASE_URL_CHAT + "addnewgroup";
        public static final String REMOVE_GROUP = BASE_URL_CHAT + "removegroup";
        public static final String UPDATE_GROUP = BASE_URL_CHAT + "updategroup";
        public static final String UNBLOCK_FRIEND = BASE_URL_CHAT + "unblockfriend";
        public static final String SEND_MESSAGE = BASE_URL_CHAT + "sendmessage";
        public static final String SEND_GROUP_MESSAGE = BASE_URL_CHAT + "sendgroupmessage";
        public static final String SEND_FILE = BASE_URL_CHAT + "sendfile";
        public static final String BLOCK_FRIEND = BASE_URL_CHAT + "blockfriend";
        public static final String UPDATE_PASSWORD = BASE_URL_CHAT + "updatepassword";
        public static final String UPDATE_PROFILE = BASE_URL_CHAT + "updateprofile";
    }

    // service codes
    public class ServiceCode {
        public static final int REGISTER = 1;
        public static final int LOGIN = 2;
        public static final int RESET = 13;
        public static final int SEARCH = 14;
        public static final int ADD_FRIEND = 3;
        public static final int GET_USER_PROFILE = 4;
        public static final int GET_MY_FRIENDS = 5;
        public static final int SEND_MESSAGE = 6;
        public static final int SEND_FILE = 7;
        public static final int BLOCK_FRIEND = 8;
        public static final int GET_BLOCKED_FRIEND = 9;
        public static final int UNBLOCKED_FRIEND = 10;
        public static final int ADD_NEW_GROUP = 11;
        public static final int SEND_GROUP_MESSAGE = 12;
        public static final int GET_GROUP_LIST = 13;
        public static final int REMOVE_GROUP = 14;
        public static final int UPDATE_GROUP = 15;
        public static final int UPDATE_PASSWORD = 16;
        public static final int UPDATE_PROFILE = 17;

    }
}
