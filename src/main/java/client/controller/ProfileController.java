package client.controller;

import client.manager.UserSession;
import client.network.ClientSocket;
import client.util.TextUtils;

import javafx.fxml.FXML;

import javafx.scene.control.*;

import model.User;
import server.dao.UserDAO;

public class ProfileController implements UserDataReceiver {

    @FXML private Label fullNameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label dobLabel;
    @FXML private Label emailLabel;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField verifyNewPasswordField;
    @FXML private TextField oldPasswordTextField;
    @FXML private TextField newPasswordTextField;
    @FXML private TextField verifyNewPasswordTextField;
    @FXML private Button toggleOldBtn;
    @FXML private Button toggleNewBtn;
    @FXML private Button toggleVerifyBtn;

    private User currentUser;

    @FXML
    public void initialize() {
        setupPasswordToggle(
                oldPasswordField,
                oldPasswordTextField,
                toggleOldBtn
        );

        setupPasswordToggle(
                newPasswordField,
                newPasswordTextField,
                toggleNewBtn
        );

        setupPasswordToggle(
                verifyNewPasswordField,
                verifyNewPasswordTextField,
                toggleVerifyBtn
        );
        currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            setUser(currentUser);
        } else {
            System.out.println("UserSession is NULL");
        }
    }

    public void setClient(ClientSocket client) {}

    public void setUser(User user) {
        this.currentUser = user;
        if (user == null) {
            return;
        }
        fullNameLabel.setText(
                TextUtils.toTitleCase(
                        user.getFullname()
                )
        );

        usernameLabel.setText(
                user.getUsername()
        );

        emailLabel.setText(
                user.getEmail()
        );

        dobLabel.setText(
                user.getDob()
        );
    }

    private void setupPasswordToggle(
            PasswordField passwordField,
            TextField textField,
            Button button
    ) {

        button.setOnAction(e -> {
            boolean showingText = textField.isVisible();
            if (showingText) {
                passwordField.setText(textField.getText());
            } else {
                textField.setText(passwordField.getText());
            }

            passwordField.setVisible(showingText);
            passwordField.setManaged(showingText);

            textField.setVisible(!showingText);
            textField.setManaged(!showingText);
        });
    }

    @FXML
    private void resetPassword() {
        if (currentUser == null) {
            showError("No user data!");
            return;
        }

        String oldPass =
                getPasswordValue(
                        oldPasswordField,
                        oldPasswordTextField
                );

        String newPass =
                getPasswordValue(
                        newPasswordField,
                        newPasswordTextField
                );

        String verifyPass =
                getPasswordValue(
                        verifyNewPasswordField,
                        verifyNewPasswordTextField
                );

        User dbUser =
                UserDAO.findByUsernameOrEmail(
                        currentUser.getUsername()
                );

        if (dbUser == null ||
                !oldPass.equals(
                        dbUser.getPassword()
                )) {

            showError("Old password incorrect!");
            return;
        }

        if (newPass.length() < 6) {
            showError("Password must be at least 6 chars!");
            return;
        }

        if (!newPass.equals(verifyPass)) {
            showError("Confirm password mismatch!");
            return;
        }

        boolean updated =
                UserDAO.updatePassword(
                        currentUser.getUser_id(),
                        newPass
                );

        if (updated) {
            currentUser.setPassword(newPass);
            UserSession.setCurrentUser(currentUser);
            showInfo();
            clearPasswordFields();

        } else {
            showError("Update password failed!");
        }
    }

    private String getPasswordValue(
            PasswordField passwordField,
            TextField textField
    ) {

        return passwordField.isVisible()
                ? passwordField.getText()
                : textField.getText();
    }

    private void clearPasswordFields() {

        oldPasswordField.clear();
        newPasswordField.clear();
        verifyNewPasswordField.clear();

        oldPasswordTextField.clear();
        newPasswordTextField.clear();
        verifyNewPasswordTextField.clear();
    }

    private void showInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText("Password changed successfully!");
        alert.showAndWait();
    }

    private void showError(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setHeaderText(null);

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }
}