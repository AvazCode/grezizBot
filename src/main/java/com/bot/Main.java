package com.bot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Project name: Default (Template) Project
 * Author: AvaZ
 * Data: 7/1/2024
 * Time: 1:11 PM
 **/
public class Main {
    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegram = new TelegramBotsApi(DefaultBotSession.class);
        telegram.registerBot(new MyBot());


    }
}