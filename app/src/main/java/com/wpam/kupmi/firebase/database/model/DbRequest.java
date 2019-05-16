package com.wpam.kupmi.firebase.database.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class DbRequest
{
    // If app client is requester, userUID = supplier UID
    // If app client is supplier, userUID = requester UID
    private String userUID;
    private String deadline;
    private String title;
    private String tag;
    private Long state;

    public DbRequest() {}

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Long getState() {
        return state;
    }

    public void setState(Long state) {
        this.state = state;
    }
}
