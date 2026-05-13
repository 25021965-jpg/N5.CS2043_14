package client.controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import server.dao.DatabaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class ForgotPassController {

    @FXML
    private TextField fullNameField;

    @FXML
    private DatePicker dobField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField verifyPasswordField;

    /*
        RESET PASSWORD
     */
    @FXML
    private void handleResetPassword() {

        String fullName = fullNameField.getText().trim();
        LocalDate dob = dobField.getValue();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String verifyPassword = verifyPasswordField.getText().trim();

        // CHECK EMPTY
        if (fullName.isEmpty()
                || dob == null
                || username.isEmpty()
                || email.isEmpty()
                || newPassword.isEmpty()
                || verifyPassword.isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Missing Information",
                    "Please fill all fields."
            );

            return;
        }

        // CHECK PASSWORD MATCH
        if (!newPassword.equals(verifyPassword)) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Password Error",
                    "Passwords do not match."
            );

            newPasswordField.clear();
            verifyPasswordField.clear();

            return;
        }

        try {

            Connection conn = DatabaseService.getConnection();

            /*
                CHECK USER INFO
             */
            String sql =
                    "SELECT * FROM users " +
                            "WHERE fullname = ? " +
                            "AND dob = ? " +
                            "AND username = ? " +
                            "AND email = ?";

            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, fullName);
            pst.setDate(2, java.sql.Date.valueOf(dob));
            pst.setString(3, username);
            pst.setString(4, email);

            ResultSet rs = pst.executeQuery();

            /*
                USER FOUND
             */
            if (rs.next()) {

                String updateSql =
                        "UPDATE users SET password = ? WHERE email = ?";

                PreparedStatement updatePst =
                        conn.prepareStatement(updateSql);

                updatePst.setString(1, newPassword);
                updatePst.setString(2, email);

                updatePst.executeUpdate();

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Password reset successfully!"
                );

                clearAllFields();

            }

            /*
                USER NOT FOUND
             */
            else {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Verification Failed",
                        "Information does not match our records.\nPlease re-enter all information."
                );

                clearAllFields();
            }

            conn.close();

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    "Something went wrong."
            );
        }
    }

    /*
        BACK TO LOGIN
     */
    @FXML
    private void handleBack(ActionEvent event) {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/login-view.fxml")
            );

            Stage stage = (Stage) ((Button) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        CLEAR ALL FIELDS
     */
    private void clearAllFields() {

        fullNameField.clear();
        dobField.setValue(null);
        usernameField.clear();
        emailField.clear();
        newPasswordField.clear();
        verifyPasswordField.clear();
    }

    /*
        ALERT
     */
    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}