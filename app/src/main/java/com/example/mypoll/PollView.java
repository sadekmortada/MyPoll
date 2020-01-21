package com.example.mypoll;

import android.graphics.Bitmap;

public class PollView {
    private Bitmap bitmap;
    private String title,owner,date;
    public PollView(Bitmap bitmap,String title,String owner,String date){
        this.bitmap=bitmap;
        this.title=title;
        this.owner=owner;
        this.date=date;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
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
