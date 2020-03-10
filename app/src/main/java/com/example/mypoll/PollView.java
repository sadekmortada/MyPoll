package com.example.mypoll;

import android.graphics.Bitmap;

public class PollView {
    private Bitmap bitmap;
    private String title,owner,date,key,type,choices,url,details,ownerId;
    public PollView(String key, Bitmap bitmap, String title, String owner,String ownerId, String date, String type, String choices, String url, String details){
        this.key=key;
        this.bitmap=bitmap;
        this.title=title;
        this.owner=owner;
        this.date=date;
        this.type=type;
        this.choices=choices;
        this.url=url;
        this.details=details;
        this.ownerId=ownerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChoices() {
        return choices;
    }

    public void setChoices(String choices) {
        this.choices = choices;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
