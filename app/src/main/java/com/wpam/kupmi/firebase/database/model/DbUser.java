package com.wpam.kupmi.firebase.database.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class DbUser
{
    private String email;
    private String phoneNumber;
    private Long reputation;

    public DbUser() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Long getReputation() {
        return reputation;
    }

    public void setReputation(Long reputation) {
        this.reputation = reputation;
    }
}
