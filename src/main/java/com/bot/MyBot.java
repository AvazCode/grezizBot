package com.bot;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.sql.*;
import java.util.*;

public class MyBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(MyBot.class);
    private Connection connection;

    public MyBot() {
        connectToDatabase();
        testDatabaseConnection();
    }

    private void connectToDatabase() {
        try {
            String dbUrl = System.getenv("DB_URL");
            String dbUser = System.getenv("DB_USER");
            String dbPassword = System.getenv("DB_PASSWORD");

            // Fallback to defaults if environment variables are not set
            if (dbUrl == null || dbUser == null || dbPassword == null) {
                dbUrl = "jdbc:postgresql://localhost:5432/telegram_bot";
                dbUser = "postgres";
                dbPassword = "redragon";
            }

            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            logger.info("Connected to PostgreSQL database!");
        } catch (SQLException e) {
            logger.error("Database connection failed: {}", e.getMessage());
        }
    }

    public void testDatabaseConnection() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1");
            if (rs.next()) {
                logger.info("Database connection is working!");
            }
        } catch (SQLException e) {
            logger.error("Database connection test failed: {}", e.getMessage());
        }
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage() == null || update.getMessage().getText() == null) {
            logger.warn("Received an update without a valid message.");
            return;
        }

        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String userInput = message.getText();
        SendMessage response = new SendMessage();
        response.setChatId(chatId);

        try {
            TelegramUser currentUser = getUserFromDatabase(chatId);

            if (userInput.equals("/start") || currentUser.getUserState().equals(UserState.START)) {
                response.setText("Choose your language / Выберите язык / Tilni tanlang:");
                response.setReplyMarkup(languageSelectionKeyboard());
                currentUser.setUserState(UserState.SELECT_LANGUAGE);
                updateUserInDatabase(currentUser);
            } else if (userInput.equals("/help")) {
                response.setText("Available Commands:\n"
                        + "/start - Restart registration\n"
                        + "/help - Show this help message\n"
                        + "result - Show your saved information\n"
                        + "/restart - Reset your registration");
            } else if (userInput.equals("/restart")) {
                currentUser.setUserState(UserState.START);
                updateUserInDatabase(currentUser);
                response.setText("Your registration has been reset. Type /start to begin again.");
            } else if (currentUser.getUserState().equals(UserState.SELECT_LANGUAGE)) {
                handleLanguageSelection(userInput, currentUser, response);
            } else {
                handleUserRegistration(userInput, currentUser, response);
            }

            execute(response);
        } catch (Exception e) {
            logger.error("Failed to process update: {}", e.getMessage());
            response.setText("An error occurred. Please try again later.");
            execute(response);
        }
    }

    private void handleLanguageSelection(String userInput, TelegramUser currentUser, SendMessage response) throws SQLException {
        if (userInput.equals("English") || userInput.equals("Русский") || userInput.equals("O'zbek")) {
            currentUser.setLanguage(userInput);
            response.setText(getLocalizedMessage("Welcome to our Telegram bot!\nWhat is your name?", currentUser.getLanguage()));
            response.setReplyMarkup(new ReplyKeyboardRemove(true));
            currentUser.setUserState(UserState.FIRST_NAME);
            updateUserInDatabase(currentUser);
        } else {
            response.setText("Invalid selection! Please choose your language.");
            response.setReplyMarkup(languageSelectionKeyboard());
        }
    }

    private void handleUserRegistration(String userInput, TelegramUser currentUser, SendMessage response) throws SQLException {
        switch (currentUser.getUserState()) {
            case FIRST_NAME:
                currentUser.setFirstName(userInput);
                response.setText(getLocalizedMessage("What's your last name?", currentUser.getLanguage()));
                currentUser.setUserState(UserState.LAST_NAME);
                break;
            case LAST_NAME:
                currentUser.setLastName(userInput);
                response.setText(getLocalizedMessage("What's your phone number (format: +998XXXXXXXXX)?", currentUser.getLanguage()));
                currentUser.setUserState(UserState.PHONE_NUMBER);
                break;
            case PHONE_NUMBER:
                if (userInput.matches("^\\+998[0-9]{9}$")) {
                    currentUser.setPhoneNumber(userInput);
                    response.setText(getLocalizedMessage("What's your email address?", currentUser.getLanguage()));
                    currentUser.setUserState(UserState.EMAIL_ADDRESS);
                } else {
                    response.setText(getLocalizedMessage("Invalid phone number format! Please try again.", currentUser.getLanguage()));
                }
                break;
            case EMAIL_ADDRESS:
                if (userInput.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    currentUser.setEmailAddress(userInput);
                    response.setText(getLocalizedMessage("Congratulations! Registration complete. Type 'result' to see your information.", currentUser.getLanguage()));
                    currentUser.setUserState(UserState.OFF);
                } else {
                    response.setText(getLocalizedMessage("Invalid email format. Please try again.", currentUser.getLanguage()));
                }
                break;
            case OFF:
                if (userInput.equals("result")) {
                    response.setText(currentUser.toString());
                } else {
                    response.setText(getLocalizedMessage("Invalid command or input.", currentUser.getLanguage()));
                }
                break;
        }
        updateUserInDatabase(currentUser);
    }

    private TelegramUser getUserFromDatabase(Long chatId) throws SQLException {
        String query = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, chatId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                TelegramUser user = new TelegramUser();
                user.setID(rs.getLong("id"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setEmailAddress(rs.getString("email_address"));
                user.setLanguage(rs.getString("language"));
                user.setUserState(UserState.valueOf(rs.getString("user_state")));
                return user;
            }
        }
        TelegramUser newUser = new TelegramUser();
        newUser.setID(chatId);
        newUser.setUserState(UserState.START);
        saveUserToDatabase(newUser);
        return newUser;
    }

    private void saveUserToDatabase(TelegramUser user) throws SQLException {
        String query = "INSERT INTO users (id, first_name, last_name, phone_number, email_address, language, user_state) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, user.getID());
            ps.setString(2, user.getFirstName());
            ps.setString(3, user.getLastName());
            ps.setString(4, user.getPhoneNumber());
            ps.setString(5, user.getEmailAddress());
            ps.setString(6, user.getLanguage());
            ps.setString(7, user.getUserState().name());
            ps.executeUpdate();
        }
    }

    private void updateUserInDatabase(TelegramUser user) throws SQLException {
        String query = "UPDATE users SET first_name = ?, last_name = ?, phone_number = ?, email_address = ?, language = ?, user_state = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getPhoneNumber());
            ps.setString(4, user.getEmailAddress());
            ps.setString(5, user.getLanguage());
            ps.setString(6, user.getUserState().name());
            ps.setLong(7, user.getID());
            ps.executeUpdate();
        }
    }

    private String getLocalizedMessage(String message, String language) {
        Map<String, String> translations = Map.of(
                "English", message,
                "Русский", "Перевод на русский: " + message,
                "O'zbek", "Tarjima o'zbekcha: " + message
        );
        return translations.getOrDefault(language, message);
    }

    private ReplyKeyboardMarkup languageSelectionKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("English"));
        row.add(new KeyboardButton("Русский"));
        row.add(new KeyboardButton("O'zbek"));
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        return keyboardMarkup;
    }

    @Override
    public String getBotUsername() {
        return "grezizbot";
    }

    @Override
    public String getBotToken() {
        return System.getenv("TELEGRAM_BOT_TOKEN");
    }
}
