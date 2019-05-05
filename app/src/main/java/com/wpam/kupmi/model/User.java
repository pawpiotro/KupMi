package com.wpam.kupmi.model;

public class User
{
    private String userUID;
    private String name;
    private String email;
    private String phoneNumber;
    private Long reputation;

    public User() {}
    public User(String userUID, String name, String email, String phoneNumber, Long reputation)
    {
        this.userUID = userUID;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.reputation = reputation;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
