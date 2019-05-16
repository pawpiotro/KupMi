package com.wpam.kupmi.firebase.database.model;

public class DbRequestDetails
{
    private String description;
    private String locationAddress;

    public DbRequestDetails() {}

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }
}
