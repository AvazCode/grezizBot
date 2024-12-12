package com.bot;

import java.io.Serializable;

public class TelegramUser implements Serializable {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String emailAddress;
    private String language;
    private UserState userState;

    public Long getID() {
        return id;
    }

    public void setID(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public UserState getUserState() {
        return userState;
    }

    public void setUserState(UserState userState) {
        this.userState = userState;
    }

    @Override
    public String toString() {
        return "User Info:\n" +
                "First Name: " + firstName + "\n" +
                "Last Name: " + lastName + "\n" +
                "Phone: " + phoneNumber + "\n" +
                "Email: " + emailAddress + "\n" +
                "Language: " + language;
    }
}
