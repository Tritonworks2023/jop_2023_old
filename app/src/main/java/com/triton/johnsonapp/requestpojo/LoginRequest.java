package com.triton.johnsonapp.requestpojo;

public class LoginRequest {


    /**
     * user_id : 12345
     * user_password : 12345
     * last_login_time : 20-10-2021 11:00 AM
     */

    private String user_id;
    private String user_password;
    private String last_login_time;
    private String device_id;
    private String login_lat;
    private String login_long;
    private  String login_address;

    public String getLogin_lat() {
        return login_lat;
    }

    public void setLogin_lat(String login_lat) {
        this.login_lat = login_lat;
    }

    public String getLogin_long() {
        return login_long;
    }

    public void setLogin_long(String login_long) {
        this.login_long = login_long;
    }

    public String getLogin_address() {
        return login_address;
    }

    public void setLogin_address(String login_address) {
        this.login_address = login_address;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_password() {
        return user_password;
    }

    public void setUser_password(String user_password) {
        this.user_password = user_password;
    }

    public String getLast_login_time() {
        return last_login_time;
    }

    public void setLast_login_time(String last_login_time) {
        this.last_login_time = last_login_time;
    }


    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }
}
