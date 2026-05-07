package com.example.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String DIRECTORY_PATH = "data";
    private static final String FILE_PATH = DIRECTORY_PATH + "/chat_history.json";

    private final ObjectMapper mapper;
    private final ObservableList<ChatMessage> messages = FXCollections.observableArrayList();

    public DataManager() {
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        checkAndInitializeStorage();
    }

    // --- ВНУТРЕННИЙ КЛАСС МОДЕЛИ ---
    public static class ChatMessage {
        private String text;
        private boolean user;

        public ChatMessage() {} // Обязательно для Jackson

        public ChatMessage(String text, boolean user) {
            this.text = text;
            this.user = user;
        }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public boolean isUser() { return user; }
        public void setUser(boolean user) { this.user = user; }
    }
    // ------------------------------

    private void checkAndInitializeStorage() {
        try {
            File directory = new File(DIRECTORY_PATH);
            if (!directory.exists()) directory.mkdirs();

            File file = new File(FILE_PATH);
            if (!file.exists()) {
                saveToFile();
            } else {
                loadFromFile(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFromFile(File file) {
        try {
            // Jackson автоматически поймет, что нужно использовать внутренний класс ChatMessage
            List<ChatMessage> loaded = mapper.readValue(file, new TypeReference<List<ChatMessage>>() {});
            if (loaded != null) messages.setAll(loaded);
        } catch (IOException e) {
            System.err.println("[DataManager] Ошибка чтения JSON. Файл будет перезаписан при сохранении.");
        }
    }

    public void addMessage(String text, boolean isUser) {
        messages.add(new ChatMessage(text, isUser));
        saveToFile();
    }

    private void saveToFile() {
        try {
            mapper.writeValue(new File(FILE_PATH), new ArrayList<>(messages));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<ChatMessage> getMessages() {
        return messages;
    }
}
