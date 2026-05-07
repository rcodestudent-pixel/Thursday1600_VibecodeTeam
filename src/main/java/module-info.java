module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.fasterxml.jackson.databind;

    opens com.example.demo to javafx.fxml, com.fasterxml.jackson.databind;

    exports com.example.demo;
}