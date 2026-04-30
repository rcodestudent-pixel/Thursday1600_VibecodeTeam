package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import javafx.scene.input.KeyCode;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GUI_Controller {

    @FXML
    private MenuButton modelSelector;

    @FXML
    private ScrollPane chatScrollPane; // Для управления прокруткой

    @FXML
    private VBox chatContainer;       // Сюда будем добавлять сообщения

    @FXML
    private TextArea messageInput;    // Поле, откуда берем текст

    @FXML
    private Button sendButton;        // Кнопка отправки (↑)

    @FXML
    public void initialize() {
        loadModelsFromJson();
        setupAutoScroll();       // Настраиваем поведение скролла
        setupKeyboardShortcuts(); // Настраиваем горячие клавиши (Enter)
    }

    @FXML
    public void handleSendMessage(){
        String text = messageInput.getText().trim();
        if (!text.isEmpty()) {
            createMessageBubble(text, true); // Создаем бабл пользователя
            messageInput.clear();           // Очищаем поле
        }
    }

    private void loadModelsFromJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File("data/models.json"));
            JsonNode modelsArray = root.get("models");

            if (modelsArray != null && modelsArray.isArray()) {
                // Очищаем старые пункты, если они были
                modelSelector.getItems().clear();

                for (JsonNode node : modelsArray) {
                    String modelName = node.asText();

                    // 1. Создаем новый пункт меню
                    MenuItem newItem = new MenuItem(modelName);

                    // 2. Добавляем действие при клике (опционально)
                    newItem.setOnAction(event -> {
                        modelSelector.setText(modelName + " ↓");
                        System.out.println("Выбрана модель: " + modelName);
                    });

                    // 3. Добавляем созданный пункт в MenuButton
                    modelSelector.getItems().add(newItem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createMessageBubble(String message, boolean isUser) {
        HBox wrapper = new HBox();
        wrapper.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Label bubble = new Label(message);
        bubble.setWrapText(true);
        bubble.setMaxWidth(500); // Ограничение ширины для переноса строк

        // Присваиваем класс из CSS в зависимости от отправителя
        bubble.getStyleClass().add(isUser ? "chat-bubble-user" : "chat-bubble-ai");

        wrapper.getChildren().add(bubble);
        chatContainer.getChildren().add(wrapper);
    }

    private void setupAutoScroll() {
        chatContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            // Устанавливаем вертикальную прокрутку на максимум (1.0)
            chatScrollPane.setVvalue(1.0);
        });
    }

    private void setupKeyboardShortcuts() {
        messageInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (event.isShiftDown()) {
                    // Если зажат Shift, добавляем новую строку
                    messageInput.appendText("\n");
                } else {
                    // Иначе — отправляем сообщение
                    handleSendMessage();
                    // Подавляем событие, чтобы в очищенном поле не появился лишний перенос
                    event.consume();
                }
            }
        });
    }
}
