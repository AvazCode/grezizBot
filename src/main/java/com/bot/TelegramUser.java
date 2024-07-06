package com.bot;

import lombok.*;

/**
 * Project name: grezizBot
 * Author: AvaZ
 * Data: 7/5/2024
 * Time: 6:13 PM
 **/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TelegramUser {
    private UserState userState;
    private Long ID;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String emailAddress;

    @Override
    public String toString() {
        return "TelegramUser {" + "\n" +
                "    userState   : " + userState + "," + "\n" +
                "    ID          : " + ID + "," + "\n" +
                "    firstName   : '" + firstName + '\'' + "," + "\n" +
                "    lastName    : '" + lastName + '\'' + "," + "\n" +
                "    phoneNumber : '" + phoneNumber + '\'' + "," + "\n" +
                "    emailAddress: '" + emailAddress + '\'' + "\n" +
                '}';
    }

}
