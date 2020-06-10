package com.github.chagall.notificationlistenerexample;

import java.util.ArrayList;

public class ReceivedMessage {

    private String messageText;
    private ArrayList<String> persons;
    private String group;

    public ReceivedMessage(String messageText, String person, String group) {
        this.messageText = messageText;
        this.persons = new ArrayList<>();
        this.persons.add(person);
        this.group = group;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
    public void addText(String text) {messageText += ". " + text;}

    public ArrayList<String> getPersons() {
        return persons;
    }
    public void addPerson(String person){persons.add(person);}

    public void setPersons(ArrayList<String> persons) {
        this.persons = persons;
    }

    public String getGroup() {
        return group;
    }
}
