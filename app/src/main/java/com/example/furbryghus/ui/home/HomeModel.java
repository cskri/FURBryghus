package com.example.furbryghus.ui.home;

import java.util.Date;

public class HomeModel {
    private String title;
    private String description;
    private String content;
    private Date date;
    private String imageLink;
    private String id;

    private HomeModel(){}

    private HomeModel(String id, String title, String description, String content, Date date){
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.date = date;
    }

    public String getId() {return id;}
    public void setId() {this.id = id;}

    public String getTitle() {return title; }
    public void setTitle(String title) {this.title=title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description ;}

    public String getContent() { return content;}
    public void setContent(String content) {this.content = content;}

    public Date getDate() {return date;}
    public void setDate(Date date) {this.date = date;}

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

}
