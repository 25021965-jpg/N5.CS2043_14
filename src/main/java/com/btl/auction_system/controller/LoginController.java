package com.btl.auction_system.controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

public class LoginController {

    // Tên biến phải trùng khớp 100% với fx:id bạn đặt trong Scene Builder
    @FXML
    private TextField userField;

    @FXML
    private PasswordField passField;

    // Hàm này phải trùng tên với On Action bạn đặt ở Bước 1
    @FXML
    void handleLogin(ActionEvent event) {
        String username = userField.getText();
        String password = passField.getText();

        if (username.equals("admin") && password.equals("123")) {
            System.out.println("Đăng nhập thành công!");
        } else {
            System.out.println("Sai tài khoản hoặc mật khẩu!");
        }
    }
}