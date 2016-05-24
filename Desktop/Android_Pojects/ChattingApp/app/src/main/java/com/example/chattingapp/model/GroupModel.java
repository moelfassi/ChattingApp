package com.example.chattingapp.model;

/**
 * Created by elfassimounir on 4/30/16.
 */
public class GroupModel {
	
	String groupid, groupname, memberid, membername, timestamp, adminid;

    public GroupModel(String groupid, String groupname, String memberid, String membername, String timestamp, String adminid) {
        super();
        this.groupid = groupid;
        this.groupname = groupname;
        this.memberid = memberid;
        this.adminid = adminid;
        this.membername = membername;
        this.timestamp = timestamp;
    }

    public GroupModel() {
        super();
    }
	
	public String getAdminid() {
		return adminid;
	}

	public void setAdminid(String adminid) {
		this.adminid = adminid;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getGroupid() {
		return groupid;
	}

	public void setGroupid(String groupid) {
		this.groupid = groupid;
	}

	public String getGroupname() {
		return groupname;
	}

	public void setGroupname(String groupname) {
		this.groupname = groupname;
	}

	public String getMemberid() {
		return memberid;
	}

	public void setMemberid(String memberid) {
		this.memberid = memberid;
	}

	public String getMembername() {
		return membername;
	}

	public void setMembername(String membername) {
		this.membername = membername;
	}

}
