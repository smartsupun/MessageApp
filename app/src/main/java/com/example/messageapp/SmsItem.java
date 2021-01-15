package com.example.messageapp;


public class SmsItem {

    private String name;
    private String text;
    private String date;

    public SmsItem(String name, String text, String date) {
        this.name = name;
        this.text = text;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
