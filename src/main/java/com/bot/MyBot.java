package com.bot;

import lombok.SneakyThrows;
import lombok.experimental.StandardException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;

/**
 * Project name: grezizBot
 * Author: AvaZ
 * Data: 7/1/2024
 * Time: 1:41 PM
 **/

public class MyBot extends TelegramLongPollingBot {
    // Create an ArrayList to store the user's message
    ArrayList<String> userMessage = new ArrayList<>();
    String userName;

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

            SendMessage messageSend = new SendMessage();
            Message messageCome = update.getMessage();
            Long chatId = messageCome.getChatId();

            if(messageCome.getText().equals("/start")){
                messageSend.setChatId(chatId);
                messageSend.setText("Welcome our telegram bot !!! What's your name?");
            }else{
                userName = messageCome.getText();
                messageSend.setChatId(chatId);
                messageSend.setText("Hi: "+userName);
            }

            execute(messageSend);



    }

    @Override
    public String getBotUsername() {
        return "grezizbot";
    }

    @Override
    public String getBotToken() {
        return "6788773828:AAHS5JsuT3UGHNss8m_YL86Y4ne2gWmCumI";
    }

}
