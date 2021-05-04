package com.example.bulletinboard;

import java.io.Serializable;

public class NewPost implements Serializable {
    private String imageId;
    private String ImageId2;
    private String imageId3;
    private String title;
    private String price;
    private String tel;
    private String desc;
    private String key;
    private String UI;
    private String time;
    private String category;
    private String TotalViews;

    public String getImageId2() {
        return ImageId2;
    }

    public void setImageId2(String imageId2) {
        ImageId2 = imageId2;
    }

    public String getImageId3() {
        return imageId3;
    }

    public void setImageId3(String imageId3) {
        this.imageId3 = imageId3;
    }

    public String getTotalViews() {
        return TotalViews;
    }

    public void setTotalViews(String totalViews) {
        TotalViews = totalViews;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUI() {
        return UI;
    }

    public void setUI(String UI) {
        this.UI = UI;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
