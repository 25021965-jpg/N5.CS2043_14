package client.controller;

import client.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

public class ForgotPassController extends BaseController {

    @FXML private TextField fullNameField;
    @FXML private DatePicker dobField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField verifyPasswordField;

    public void initialize() {
        System.out.println("Reset Password Loaded");
    }

    // ==================== RESET PASSWORD ====================
    @FXML
    private void handleResetPassword() {
        String fullName = fullNameField.getText().trim();
        LocalDate dob = dobField.getValue();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String verifyPassword = verifyPasswordField.getText().trim();
        if (!validateInput(
                fullName,
                dob,
                username,
                email,
                newPassword,
                verifyPassword
        )) {
            return;
        }

        client.sendForgotPassword(
                fullName,
                dob.toString(),
                username,
                email,
                newPassword
        );
    }

    // ==================== VALIDATION ====================
    private boolean validateInput(
            String fullName,
            LocalDate dob,
            String username,
            String email,
            String newPassword,
            String verifyPassword
    ) {

        if (fullName.isBlank()
                || dob == null
                || username.isBlank()
                || email.isBlank()
                || newPassword.isBlank()
                || verifyPassword.isBlank()) {

            AlertUtils.warning(
                    "Missing Information",
                    "Please fill all fields."
            );

            return false;
        }

        if (!newPassword.equals(verifyPassword)) {
            AlertUtils.error("Passwords do not match.");
            newPasswordField.clear();
            verifyPasswordField.clear();
            return false;
        }
        return true;
    }

    // ==================== BACK ====================
    @FXML
    private void handleBack() {
        navigate(
                (Stage) fullNameField
                        .getScene()
                        .getWindow(),
                "/fxml/login-view.fxml",
                "Login"
        );
    }
}
