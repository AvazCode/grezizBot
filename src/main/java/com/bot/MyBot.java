package com.bot;

import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

public class MyBot extends TelegramLongPollingBot {
    List<TelegramUser> users = new ArrayList<>();


    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
// Initial step with declaring ChatID and user message sender and catcher!
        SendMessage messageSend = new SendMessage();
        Message messageCome = update.getMessage();
        Long chatId = messageCome.getChatId();


        TelegramUser currentUser = null;

        currentUser = getCurrentUser(chatId, currentUser, messageSend);


        if (messageCome.getText().equals("/start") || currentUser.getUserState().equals(UserState.START)) {
            messageSend.setText("Welcome to our Telegram bot!\nWhat is your name?");
            currentUser.setUserState(UserState.FIRST_NAME);
        } else if (currentUser.getUserState().equals(UserState.FIRST_NAME)) {
            currentUser.setFirstName(messageCome.getText());
            messageSend.setText("Hello " + currentUser.getFirstName() + "\nWhat's your last name?");
            currentUser.setUserState(UserState.LAST_NAME);
        } else if (currentUser.getUserState().equals(UserState.LAST_NAME)) {
            currentUser.setLastName(messageCome.getText());
            messageSend.setText("What's your phone number?");
            currentUser.setUserState(UserState.PHONE_NUMBER);
        } else if (currentUser.getUserState().equals(UserState.PHONE_NUMBER)) {
            if(messageCome.getText().startsWith("+998") && messageCome.getText().length()==13){
                currentUser.setPhoneNumber(messageCome.getText());
                messageSend.setText("What's your email address?");
                currentUser.setUserState(UserState.EMAIL_ADDRESS);
            }else{
                messageSend.setText("Are you crazy !!! "+
                        currentUser.getFirstName()+"\nIt's WRONG 😒"+
                        "\nTry again -------------------------?");
            }

        } else if (currentUser.getUserState().equals(UserState.EMAIL_ADDRESS)) {
            currentUser.setEmailAddress(messageCome.getText());
            messageSend.setText("Congratulations! You have successfully logged in!\nWrite 'result' to see all information.");
            currentUser.setUserState(UserState.OFF);
        } else if (messageCome.getText().equals("result") &&
                currentUser.getUserState().equals(UserState.OFF)) {
            messageSend.setText(currentUser.toString());
            System.out.println(currentUser);
        } else {
            messageSend.setText("Something went wrong! Wait for an admins' response. Thanks ❤️");
        }

        execute(messageSend);
    }

    // This method tests current user position !
    private TelegramUser getCurrentUser(Long chatId, TelegramUser currentUser, SendMessage messageSend) {
        for (TelegramUser consumer : users) {
            if(consumer.getID().equals(chatId)) {
                currentUser = consumer;
                messageSend.setChatId(chatId);
                break;
            }
        }
        if (currentUser == null) {
            currentUser = new TelegramUser();
            currentUser.setID(chatId);
            messageSend.setChatId(chatId);
            users.add(currentUser);
        }
        return currentUser;
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