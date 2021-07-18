package com.example.mychat;

/**
 * This class contains the link between 2 users, which is only the date that they became friends
 */
public class Friend {

    public String date;

    public Friend(String date) {
        this.date = date;
    }
    public Friend(){};

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
