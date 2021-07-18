package com.example.mychat;

public class Chat {

    public String name,lastImage,thumbImage;

    public Chat(String name, String lastImage, String thumbImage) {
        this.name = name;
        this.lastImage = lastImage;
        this.thumbImage = thumbImage;
    }
    public Chat(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastImage() {
        return lastImage;
    }

    public void setLastImage(String lastImage) {
        this.lastImage = lastImage;
    }

    public String getThumbImage() {
        return thumbImage;
    }

    public void setThumbImage(String thumbImage) {
        this.thumbImage = thumbImage;
    }
}
