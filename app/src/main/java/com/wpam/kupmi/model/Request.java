package com.wpam.kupmi.model;

import android.content.Context;
import android.util.Pair;
import com.wpam.kupmi.utils.DateUtils;
import com.wpam.kupmi.utils.UIDUtils;
import java.util.Calendar;

public class Request {
    private String requestUID;
    private String requesterUID;
    private String supplierUID;
    private Calendar deadline;
    private String title;
    private String description;
    private Pair<Double, Double> location;
    private String locationAddress;
    private RequestTag tag;
    private RequestState state;

    public Request() {
        this.requestUID = UIDUtils.getUID();
    }

    public Request(String requestUID)
    {
        this.requestUID = requestUID;
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

    public Calendar getDeadline() {
        return deadline;
    }

    public void setDeadline(Calendar deadline) {
        this.deadline = deadline;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Pair<Double, Double> getLocation() {
        return location;
    }

    public void setLocation(Pair<Double, Double> location) {
        this.location = location;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public RequestTag getTag() {
        return tag;
    }

    public void setTag(RequestTag tag) {
        this.tag = tag;
    }

    public RequestState getState() {
        return state;
    }

    public void setState(RequestState state) {
        this.state = state;
    }

    public String toString(Context ctx) {
        StringBuilder stringBuilder = new StringBuilder();
        String lineSeparator = System.lineSeparator();

        stringBuilder.append("Request UID: ").append(requestUID);
        stringBuilder.append(lineSeparator);
        stringBuilder.append("Requester UID: ").append(requesterUID);
        stringBuilder.append(lineSeparator);
        stringBuilder.append("Supplier UID: ").append(supplierUID);
        stringBuilder.append(lineSeparator);
        stringBuilder.append("Deadline: ").append(DateUtils.getDateText(deadline, ctx));
        stringBuilder.append(lineSeparator);
        stringBuilder.append("Title: ").append(title);
        stringBuilder.append(lineSeparator);
        stringBuilder.append("Description: ").append(description);
        stringBuilder.append(lineSeparator);
        stringBuilder.append("Location: ").append(location.first).append(" ").append(location.second);
        stringBuilder.append(lineSeparator);
        stringBuilder.append("Location address: ").append(locationAddress);
        stringBuilder.append(lineSeparator);
        stringBuilder.append("Tag: ").append(tag.name());
        stringBuilder.append(lineSeparator);
        stringBuilder.append("State: ").append(state.name());
        stringBuilder.append(lineSeparator);

        return stringBuilder.toString();
    }
}
