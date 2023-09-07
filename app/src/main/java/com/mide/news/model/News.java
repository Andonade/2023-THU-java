package com.mide.news.model;

import java.util.ArrayList;

public class News {
    private String title;
    private ArrayList<String> picUrls;
    private String date;
    private String publisher;
    private String category;
    private String content;
    private String videoUrl;
    private String newsId;

    public News(String title, ArrayList<String> picUrls, String date, String publisher, String category, String content, String videoUrl, String newsId) {
        this.title = title;
        this.picUrls = picUrls;
        this.date = date;
        this.publisher = publisher;
        this.category = category;
        this.content = content;
        this.videoUrl = videoUrl;
        this.newsId = newsId;
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

    public String getNewsId() { return newsId; }

    public void setNewsId(String newsId) { this.newsId = newsId; }
}
