package com.sales.numax.model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class ApplicationMenu {

    String code;
    String title;
    String description;
    String imgUrl;
    Drawable image;
    public  ApplicationMenu(){

    }

    public String getTitle() {
        return title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }
}
