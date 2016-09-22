package com.yahoo.berniak.georeminderr;

/**
 * Created by krzysztofberniak on 22.09.16.
 */
public class Reminder {
    private long id;
    private String name;
    private String familyName;
    private String position;
    private String phone;
    private String webLink;

    Reminder(long id, String name, String familyName, String position, String phone) {
        this.id = id;
        this.name = name;
        this.familyName = familyName;
        this.position = position;
        this.phone = phone;
        this.webLink = null;
    }

    public String getWebLink() {
        return webLink;
    }

    public void setWebLink(String webLink) {
        this.webLink = webLink;
    }

    public Reminder() {

    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getFamilyName() {
        return familyName;
    }
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }
    public String getPosition() {
        return position;
    }
    public void setPosition(String position) {
        this.position = position;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }


    @Override
    public String toString() {
        return name + " " + familyName + " (" + position + ")";
    }
}
