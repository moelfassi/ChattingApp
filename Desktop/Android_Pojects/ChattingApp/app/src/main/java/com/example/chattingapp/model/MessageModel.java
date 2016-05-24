package com.example.chattingapp.model;

public class MessageModel {

	String fname, lname, message, myuserid, fromuserid, timestamp, logintype,
			chattype, ismine, groupid, isseen, msgtype;

	String groupname = "";

    public MessageModel(String fname, String lname, String message, String myuserid,
                        String fromuserid, String timestamp,
                        String chattype, String ismine, String groupid, String isseen, String msgtype) {
        super();
        this.fname = fname;
        this.lname = lname;
        this.message = message;
        this.myuserid = myuserid;
        this.fromuserid = fromuserid;
        this.timestamp = timestamp;
        this.chattype = chattype;
        this.ismine = ismine;
        this.groupid = groupid;
        this.isseen = isseen;
        this.msgtype = msgtype;
    }

    public MessageModel() {
        super();
    }

    public String getIsseen() {
		return isseen;
	}

	public void setIsseen(String isseen) {
		this.isseen = isseen;
	}

	public String getGroupname() {
		return groupname;
	}

	public void setGroupname(String groupname) {
		this.groupname = groupname;
	}

	public String getLogintype() {
		return logintype;
	}

	public void setLogintype(String logintype) {
		this.logintype = logintype;
	}

	public String getIsmine() {
		return ismine;
	}

	public void setIsmine(String ismine) {
		this.ismine = ismine;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public String getLname() {
		return lname;
	}

	public void setLname(String lname) {
		this.lname = lname;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMyuserid() {
		return myuserid;
	}

	public void setMyuserid(String myuserid) {
		this.myuserid = myuserid;
	}

	public String getFromuserid() {
		return fromuserid;
	}

	public void setFromuserid(String fromuserid) {
		this.fromuserid = fromuserid;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getChattype() {
		return chattype;
	}

	public void setChattype(String chattype) {
		this.chattype = chattype;
	}

	public String getGroupid() {
		return groupid;
	}

	public void setGroupid(String groupid) {
		this.groupid = groupid;
	}

	public String getMsgtype() {
		return msgtype;
	}

	public void setMsgtype(String msgtype) {
		this.msgtype = msgtype;
	}
}
