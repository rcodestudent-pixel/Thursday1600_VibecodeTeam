package com.example.demo;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import javafx.scene.input.KeyCode;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.geometry.Insets;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

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
    private Button sendButton;        // Кнопка отправки ()

    @FXML
    public void initialize() {
        loadModelsFromJson();
        setupAutoScroll();       // Настраиваем поведение скролла
        setupKeyboardShortcuts(); // Настраиваем горячие клавиши (Enter)
        // Инициализируем посредника (он сам проверит файлы и загрузит данные)
        dataManager = new DataManager();

        // Настраиваем слушатель изменений (чтобы UI реагировал на новые данные в DataManager)
        dataManager.getMessages().addListener((javafx.collections.ListChangeListener<DataManager.ChatMessage>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (DataManager.ChatMessage msg : change.getAddedSubList()) {
                        createMessageBubble(msg.getText(), msg.isUser());
                    }
                }
                if (change.wasRemoved()) {
                    chatContainer.getChildren().clear(); // Если история очищена
                }
            }
        });

        // Отрисовываем то, что уже было в файле при запуске
        dataManager.getMessages().forEach(msg -> createMessageBubble(msg.getText(), msg.isUser()));

        loadModelsFromJson();
        setupAutoScroll();
        setupKeyboardShortcuts();
    }

    @FXML
    public void handleSendMessage(){
        String text = messageInput.getText().trim();
        if (!text.isEmpty()) {
            dataManager.addMessage(text, true);
            messageInput.clear();
        }
    }

    private DataManager dataManager; // Посредник между интерфейсом и данными

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
        // 1. Внешний контейнер ряда
        HBox wrapper = new HBox();
        wrapper.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        VBox.setMargin(wrapper, new Insets(5, 15, 5, 15));

        // 2. Основной контейнер бабла (Белый фон)
        VBox bubble = new VBox(2);
        bubble.setMaxWidth(420);
        bubble.setPadding(new Insets(8, 12, 10, 12));

        // Стилизация: Белый фон, тонкая серая рамка и аккуратные углы
        String rounding = isUser ? "12 12 2 12" : "12 12 12 2";
        bubble.setStyle(
                "-fx-background-color: #FFFFFF; " +            // Белый цвет бабла
                        "-fx-background-radius: " + rounding + "; " +
                        "-fx-border-color: #E0E0E0; " +                // Тонкая рамка, чтобы бабл не терялся
                        "-fx-border-radius: " + rounding + "; " +
                        "-fx-border-width: 1px;"
        );

        // 3. Подпись (Имя) — Серая
        Label nameLabel = new Label(isUser ? "Вы" : "AI Assistant");
        nameLabel.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #888888;");

        // 4. Текст сообщения — Черный
        Label messageText = new Label(message);
        messageText.setWrapText(true);
        messageText.setStyle("-fx-font-size: 13px; -fx-text-fill: #202124; -fx-line-spacing: 1.2px;");

        // 5. Едва заметная тень для объема
        DropShadow softShadow = new DropShadow();
        softShadow.setRadius(3);
        softShadow.setOffsetY(1);
        softShadow.setColor(Color.rgb(0, 0, 0, 0.05)); // Очень слабая тень
        bubble.setEffect(softShadow);

        // Сборка
        bubble.getChildren().addAll(nameLabel, messageText);
        wrapper.getChildren().add(bubble);

        // Добавление в чат
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
