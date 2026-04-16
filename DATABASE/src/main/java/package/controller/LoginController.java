package com.yourpackage.controller;

import com.yourpackage.dao.UserDAO;
import com.yourpackage.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMessage;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Vui lòng nhập đầy đủ thông tin");
            return;
        }
        User user = userDAO.login(username, password);
        if (user != null) {
            // Lưu thông tin user đã đăng nhập (ví dụ dùng biến static)
            AppState.setCurrentUser(user);
            lblMessage.setText("Đăng nhập thành công! Chào " + user.getUsername());
            // Chuyển sang màn hình dashboard
            // ... load dashboard.fxml
        } else {
            lblMessage.setText("Sai tên đăng nhập hoặc mật khẩu");
        }
    }

    @FXML
    private void handleRegister() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Vui lòng nhập đầy đủ thông tin");
            return;
        }
        boolean success = userDAO.register(username, password);
        if (success) {
            lblMessage.setText("Đăng ký thành công! Mời đăng nhập.");
            txtUsername.clear();
            txtPassword.clear();
        } else {
            lblMessage.setText("Tên đăng nhập đã tồn tại");
        }
    }
}
