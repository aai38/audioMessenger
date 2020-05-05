package com.github.chagall.notificationlistenerexample;

public class Contact {

    private String name;
    private String number;
    private boolean favorite;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public Contact(String name, String number) {
        this.name = name;
        this.number = number;
        favorite = false;
    }

    public Contact () {

    }
}
