package client.controller;

import client.network.ClientSocket;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.*;

import javafx.stage.Stage;

import model.User;

import java.io.IOException;
import java.util.Optional;

public class ProfileController implements UserDataReceiver {

    @FXML
    private Button logoutBtn;

    @FXML
    private Button deleteAccountBtn;

    @FXML
    private Button historyBtn;

    @FXML
    private Button favoriteBtn;

    @FXML
    private Button balanceBtn;

    @FXML
    private Button createdAuctionBtn;

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

        logoutBtn.setOnAction(
                e -> handleLogout()
        );

        deleteAccountBtn.setOnAction(
                e -> handleDeleteAccount()
        );

        createdAuctionBtn.setOnAction(
                e -> openPage(
                        "/fxml/yourAuctions-view.fxml",
                        "Your Auctions",
                        null
                )
        );

        historyBtn.setOnAction(
                e -> {
                    // TODO:
                    // open history page
                }
        );

        favoriteBtn.setOnAction(
                e -> {
                    // TODO:
                    // open favourite page
                }
        );

        balanceBtn.setOnAction(
                e -> {
                    // TODO:
                    // open balance page
                }
        );
    }

    public void setClient(ClientSocket client) {

        this.client = client;
    }

    public void setUser(User user) {

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

        openPage(
                "/fxml/HomePage.fxml",
                "Auction",
                event
        );
    }

    private void setupPasswordToggle(
            PasswordField passwordField,
            TextField textField,
            Button button
    ) {

        button.setOnAction(e -> {

            boolean showingText =
                    textField.isVisible();

            if (showingText) {

                passwordField.setText(
                        textField.getText()
                );

            } else {

                textField.setText(
                        passwordField.getText()
                );
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

            showError(
                    "No user data!"
            );

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

        if (oldPass.isEmpty()) {

            showError(
                    "Enter old password!"
            );

            return;
        }

        if (!oldPass.equals(
                currentUser.getPassword()
        )) {

            showError(
                    "Old password incorrect!"
            );

            return;
        }

        if (newPass.isEmpty()) {

            showError(
                    "Enter new password!"
            );

            return;
        }

        if (newPass.length() < 6) {

            showError(
                    "Password must be at least 6 characters!"
            );

            return;
        }

        if (!newPass.equals(
                verifyPass
        )) {

            showError(
                    "Confirm password does not match!"
            );

            return;
        }

        currentUser.setPassword(
                newPass
        );

        showInfo(
                "Password changed successfully!"
        );

        clearPasswordFields();
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

    private void handleLogout() {

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        alert.setTitle(
                "Logout"
        );

        alert.setHeaderText(
                "Are you sure you want to logout?"
        );

        alert.setContentText(
                "You will need to login again."
        );

        ButtonType logoutButton =
                new ButtonType(
                        "Logout"
                );

        ButtonType cancelButton =
                new ButtonType(
                        "Cancel",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        alert.getButtonTypes().setAll(
                logoutButton,
                cancelButton
        );

        Optional<ButtonType> result =
                alert.showAndWait();

        if (
                result.isPresent()
                        &&
                        result.get() == logoutButton
        ) {

            System.out.println(
                    "Logout clicked"
            );

            openPage(
                    "/fxml/login-view.fxml",
                    "Login",
                    null
            );
        }
    }

    private void handleDeleteAccount() {

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        alert.setTitle(
                "Delete Account"
        );

        alert.setHeaderText(
                "Are you sure you want to delete your account?"
        );

        alert.setContentText(
                "This action cannot be undone."
        );

        ButtonType deleteButton =
                new ButtonType(
                        "Delete"
                );

        ButtonType cancelButton =
                new ButtonType(
                        "Cancel",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        alert.getButtonTypes().setAll(
                deleteButton,
                cancelButton
        );

        Optional<ButtonType> result =
                alert.showAndWait();

        if (
                result.isPresent()
                        &&
                        result.get() == deleteButton
        ) {

            System.out.println(
                    "Account deleted"
            );

        /*
            TODO:
            - delete account from server
            - clear session
         */

            openPage(
                    "/fxml/login-view.fxml",
                    "Login",
                    null
            );
        }
    }


    private void showInfo(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setHeaderText(null);

        alert.setContentText(
                message
        );

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

    private void openPage(
            String fxmlPath,
            String title,
            ActionEvent event
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    fxmlPath
                            )
                    );

            Parent root =
                    loader.load();

            passData(
                    loader.getController()
            );

            Stage stage;

            if (event != null) {

                stage =
                        (Stage)
                                ((Node)
                                        event.getSource())
                                        .getScene()
                                        .getWindow();

            } else {

                stage =
                        (Stage)
                                logoutBtn
                                        .getScene()
                                        .getWindow();
            }

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    title
            );

            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            System.out.println(
                    "Cannot open: "
                            + fxmlPath
            );
        }
    }

    private void passData(Object controller) {

        if (controller instanceof UserDataReceiver c) {

            c.setClient(client);
            c.setUser(currentUser);
        }
    }
}