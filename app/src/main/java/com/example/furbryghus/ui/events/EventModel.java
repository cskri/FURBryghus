package com.example.furbryghus.ui.events;

import java.util.Date;
import java.util.List;

public class EventModel {

    private String id;
    private String name;
    private String description;
    private String imageLink;
    private Date date;
    private double price;
    private List<String> participants;

    private EventModel(){}

    private EventModel(String id, String name, String description, String imageLink, Date date, double price, List<String> participants){
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageLink = imageLink;
        this.date = date;
        this.price = price;
        this.participants = participants;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLing(String imageLink) {
        this.imageLink = imageLink;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }
}
