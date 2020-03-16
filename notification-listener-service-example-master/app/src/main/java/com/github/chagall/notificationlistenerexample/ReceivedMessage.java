package com.github.chagall.notificationlistenerexample;

public class ReceivedMessage {

    private String messageText;
    private String person;

    public ReceivedMessage(String messageText, String person) {
        this.messageText = messageText;
        this.person = person;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }
}
