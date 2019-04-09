package me.wooy.proxy.ui;

import io.vertx.core.json.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Controller {
    @FXML
    private ListView<String> listView;
    @FXML
    private TextField localPortTextField;
    @FXML
    private TextField remoteAddressTextField;
    @FXML
    private TextField remotePortTextField;
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField passwordTextField;

    private Number selected = -1;

    @FXML
    protected void onRemoveButtonClicked(ActionEvent event) {
        if (this.selected.intValue() < 0) return;
        listView.getItems().remove(selected.intValue());
        Main.info.remove(selected.intValue());
        Utils.INSTANCE.saveInfo(Main.saveFile,Main.info);
        this.localPortTextField.clear();
        this.remotePortTextField.clear();
        this.remoteAddressTextField.clear();
        this.usernameTextField.clear();
        this.passwordTextField.clear();
        this.selected = -1;
    }

    @FXML
    protected void onAddButtonClicked(ActionEvent event) {
        Integer localPort = new Integer(localPortTextField.getText());
        String host = remoteAddressTextField.getText();
        Integer remotePort = new Integer(remotePortTextField.getText());
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        JsonObject config = new JsonObject();
        config.put("proxy.type", "socks5")
                .put("local.port", localPort);
        config.put("remote.ip", host)
                .put("remote.port", remotePort)
                .put("user", username)
                .put("pass", password)
                .put("offset", 0);
        Main.info.add(config);
        Utils.INSTANCE.saveInfo(Main.saveFile, Main.info);
        listView.getItems().add(config.getString("remote.ip") + ":" + config.getInteger("remote.port"));
        this.selected = listView.getItems().size()-1;
    }

    @FXML
    protected void onConfirmButtonClicked(ActionEvent event) {
        Integer localPort = new Integer(localPortTextField.getText());
        String host = remoteAddressTextField.getText();
        Integer remotePort = new Integer(remotePortTextField.getText());
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        JsonObject config = new JsonObject();
        config.put("proxy.type", "socks5")
                .put("local.port", localPort);
        config.put("remote.ip", host)
                .put("remote.port", remotePort)
                .put("user", username)
                .put("pass", password)
                .put("offset", 0);
        if (selected.intValue() >= 0) {
            Main.info.getJsonObject(selected.intValue()).put("selected", true);
            Utils.INSTANCE.saveInfo(Main.saveFile,Main.info);
        }
        Main.vertx.eventBus().publish("config-modify", config);
        Tray.INSTANCE.getSystemTray().setStatus("Connecting...");
        ((Stage) localPortTextField.getScene().getWindow()).close();
    }

    @FXML
    protected void initialize() {
        listView.getSelectionModel()
                .selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() < 0) return;
            this.selected = newValue;
            JsonObject config = Main.info.getJsonObject(this.selected.intValue());
            localPortTextField.setText(String.valueOf(config.getInteger("local.port")));
            remoteAddressTextField.setText(config.getString("remote.ip"));
            remotePortTextField.setText(String.valueOf(config.getInteger("remote.port")));
            usernameTextField.setText(config.getString("user"));
            passwordTextField.setText(config.getString("pass"));
        });
        Main.info.stream().forEach((value) -> {
            JsonObject config = (JsonObject) value;
            if (config.containsKey("selected") && config.getBoolean("selected")) {
                localPortTextField.setText(String.valueOf(config.getInteger("local.port")));
                remoteAddressTextField.setText(config.getString("remote.ip"));
                remotePortTextField.setText(String.valueOf(config.getInteger("remote.port")));
                usernameTextField.setText(config.getString("user"));
                passwordTextField.setText(config.getString("pass"));
            }
            listView.getItems().add(config.getString("remote.ip") + ":" + config.getInteger("remote.port"));
        });
    }
}