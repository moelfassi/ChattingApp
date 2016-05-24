package com.example.chattingapp.model;

/**
 * Created by elfassimounir on 4/30/16.
 */

public class UserModel {

	public String userid,firstname, lastname, profilepic, logintype, city, country, gender, status;
	public boolean isSelected;

    public UserModel(String userid, String firstname, String lastname, String profilepic) {
        super();
        this.userid = userid;
        this.firstname = firstname;
        this.lastname = lastname;
        this.profilepic = profilepic;

    }

    public UserModel() {
        super();
    }
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
		System.out.println("isSelected  --->"+isSelected);
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getProfilepic() {
		return profilepic;
	}

	public void setProfilepic(String profilepic) {
		this.profilepic = profilepic;
	}

	public String getLogintype() {
		return logintype;
	}

	public void setLogintype(String logintype) {
		this.logintype = logintype;
	}
}
