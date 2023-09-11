package com.java.chenyitao.model;

import java.util.ArrayList;

public class News {
    private String title;
    private ArrayList<String> picUrls;
    private String date;
    private String publisher;
    private String category;
    private String content;
    private String videoUrl;
    private String newsID;

    public News(String title, ArrayList<String> picUrls, String date, String publisher, String category, String content, String videoUrl, String newsID) {
        this.title = title;
        this.picUrls = picUrls;
        this.date = date;
        this.publisher = publisher;
        this.category = category;
        this.content = content;
        this.videoUrl = videoUrl;
        this.newsID = newsID;
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

    public String getContent() { return content; }

    public String getVideoUrl() { return videoUrl; }

    public String getNewsID() { return newsID; }

    public void setNewsID(String newsId) { this.newsID = newsId; }
}
