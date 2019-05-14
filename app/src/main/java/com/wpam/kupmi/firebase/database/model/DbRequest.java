package com.wpam.kupmi.firebase.database.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.util.List;

@IgnoreExtraProperties
public class DbRequest
{
    private String requesterUID;
    private String supplierUID;
    private String deadline;
    private String description;
    private String tag;
    private Long state;
    private String locationAddress;

    public DbRequest() {}

    public String getRequesterUID() {
        return requesterUID;
    }

    public void setRequesterUID(String requesterUID) {
        this.requesterUID = requesterUID;
    }

    public String getSupplierUID() {
        return supplierUID;
    }

    public void setSupplierUID(String supplierUID) {
        this.supplierUID = supplierUID;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }
}
