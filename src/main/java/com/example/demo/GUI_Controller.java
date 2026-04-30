package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;

public class GUI_Controller {

    @FXML
    private MenuButton modelSelector;

    @FXML
    public void initialize() {
        loadModelsFromJson();
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
}
