package com.wpam.kupmi.model;

import android.util.Pair;

import com.wpam.kupmi.utils.UIDUtils;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.wpam.kupmi.utils.DateUtils.getDate;

public class Request {
    private String requestUID;
    private String requesterUID;
    private String supplierUID;
    private Date deadline;
    private String description;
    private List<String> tags;
    private Pair<Double, Double> location;


    // TODO: Location -> Address od Pawła
    private String locationAddress;
    private RequestState state;

    public Request() {
    }

    public Request(String requesterUID, String supplierUID, String deadlineText, String dateFormat,
                   Locale locale, String description, List<String> tags, Pair<Double, Double> location, int stateId) {
        this.requestUID = UIDUtils.getUID();
        this.requesterUID = requesterUID;
        this.supplierUID = supplierUID;
        this.deadline = getDate(deadlineText, dateFormat, locale);
        this.description = description;
        this.tags = tags;
        this.location = location;
        this.state = RequestState.getInstance(stateId);
    }

    public Request(String requesterUID, String supplierUID, Date deadline, String description,
                   List<String> tags, Pair<Double, Double> location, RequestState state) {
        this.requesterUID = requesterUID;
        this.supplierUID = supplierUID;
        this.deadline = deadline;
        this.description = description;
        this.tags = tags;
        this.location = location;
        this.state = state;
    }

    public String getRequestUID() {
        return requestUID;
    }

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

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Pair<Double, Double> getLocation() {
        return location;
    }

    public void setLocation(Pair<Double, Double> location) {
        this.location = location;
    }

    public RequestState getState() {
        return state;
    }

    public void setState(RequestState state) {
        this.state = state;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }
}
