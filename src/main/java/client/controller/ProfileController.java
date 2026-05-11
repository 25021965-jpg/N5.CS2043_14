package client.controller;

import client.network.ClientSocket;
import model.User;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ProfileController {

    @FXML
    private Label fullNameLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label dobLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private PasswordField oldPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField verifyNewPasswordField;

    @FXML
    private TextField oldPasswordTextField;

    @FXML
    private TextField newPasswordTextField;

    @FXML
    private TextField verifyNewPasswordTextField;

    @FXML
    private Button toggleOldBtn;

    @FXML
    private Button toggleNewBtn;

    @FXML
    private Button toggleVerifyBtn;

    private ClientSocket client;

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
    }

    public void setClient(
            ClientSocket client
    ) {

        this.client = client;
    }

    public void setUser(
            User user
    ) {

        this.currentUser = user;

        if (user == null) {
            return;
        }

        fullNameLabel.setText(
                user.getFullname()
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

    @FXML
    private void handleBackHome(
            ActionEvent event
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/user-view.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            UserViewController controller =
                    loader.getController();

            controller.setClient(client);

            controller.setUser(currentUser);

            Stage stage =
                    (Stage)
                            ((Node) event.getSource())
                                    .getScene()
                                    .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Auction"
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void setupPasswordToggle(
            PasswordField passwordField,
            TextField textField,
            Button button
    ) {

        button.setOnAction(e -> {

            if (textField.isVisible()) {

                passwordField.setText(
                        textField.getText()
                );

                passwordField.setVisible(true);
                passwordField.setManaged(true);

                textField.setVisible(false);
                textField.setManaged(false);

            } else {

                textField.setText(
                        passwordField.getText()
                );

                textField.setVisible(true);
                textField.setManaged(true);

                passwordField.setVisible(false);
                passwordField.setManaged(false);
            }
        });
    }

    @FXML
    private void resetPassword() {

        if (currentUser == null) {

            showAlert(
                    "Error",
                    "No user data!"
            );

            return;
        }

        String oldPass =
                oldPasswordField.isVisible()
                        ? oldPasswordField.getText()
                        : oldPasswordTextField.getText();

        String newPass =
                newPasswordField.isVisible()
                        ? newPasswordField.getText()
                        : newPasswordTextField.getText();

        String verifyPass =
                verifyNewPasswordField.isVisible()
                        ? verifyNewPasswordField.getText()
                        : verifyNewPasswordTextField.getText();

        if (oldPass.isEmpty()) {

            showAlert(
                    "Error",
                    "Enter old password!"
            );

            return;
        }

        if (!oldPass.equals(
                currentUser.getPassword()
        )) {

            showAlert(
                    "Error",
                    "Old password incorrect!"
            );

            return;
        }

        if (newPass.isEmpty()) {

            showAlert(
                    "Error",
                    "Enter new password!"
            );

            return;
        }

        if (newPass.length() < 6) {

            showAlert(
                    "Error",
                    "Password must be at least 6 characters!"
            );

            return;
        }

        if (!newPass.equals(verifyPass)) {

            showAlert(
                    "Error",
                    "Confirm password does not match!"
            );

            return;
        }

        currentUser.setPassword(newPass);

        showAlert(
                "Success",
                "Password changed successfully!"
        );

        clearPasswordFields();
    }

    private void clearPasswordFields() {

        oldPasswordField.clear();

        newPasswordField.clear();

        verifyNewPasswordField.clear();

        oldPasswordTextField.clear();

        newPasswordTextField.clear();

        verifyNewPasswordTextField.clear();
    }

    private void showAlert(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}
