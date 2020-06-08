package com.github.chagall.notificationlistenerexample;

public class ReceivedMessage {

    private String messageText;
    private String person;
    private String group;

    public ReceivedMessage(String messageText, String person, String group) {
        this.messageText = messageText;
        this.person = person;
        this.group = group;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
    public void addText(String text) {messageText += ". " + text;}

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getGroup() {
        return group;
    }
}
