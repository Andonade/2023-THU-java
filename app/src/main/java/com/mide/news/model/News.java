package com.mide.news.model;

import java.util.ArrayList;

public class News {
    private String title;
    private ArrayList<String> picUrls;
    private String date;
    private String publisher;
    private String category;

    public News(String title, ArrayList<String> picUrls, String date, String publisher, String category) {
        this.title = title;
        this.picUrls = picUrls;
        this.date = date;
        this.publisher = publisher;
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getPicUrls() {
        return picUrls;
    }

    public void addPicUrl(String picUrl) { picUrls.add(picUrl); }

    public String getDate() {
        return date;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getCategory() {
        return category;
    }
}
