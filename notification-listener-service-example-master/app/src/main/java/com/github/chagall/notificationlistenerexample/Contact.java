package com.github.chagall.notificationlistenerexample;

public class Contact implements java.io.Serializable{

    private String name;
    private boolean favorite;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }


    public Contact() {

    }
}
