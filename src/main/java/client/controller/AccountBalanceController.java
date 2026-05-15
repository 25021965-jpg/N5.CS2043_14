package client.controller;

import client.network.ClientSocket;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.*;

import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import model.User;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class AccountBalanceController implements UserDataReceiver {

    private ClientSocket client;

    private User currentUser;

    @FXML
    private Button infoBtn;

    @FXML
    private Button historyBtn;

    @FXML
    private Button createdAuctionBtn;

    @FXML
    private Button favoriteBtn;

    @FXML
    private Button walletBtn;

    @FXML
    private Button logoutBtn;

    @FXML
    private Button deleteAccountBtn;

    @FXML
    private Button backHomeBtn;

    @FXML
    private Label balanceLabel;

    @FXML
    private TextField depositField;

    @FXML
    private TextField withdrawField;

    @FXML
    private Button depositBtn;

    @FXML
    private Button withdrawBtn;

    @FXML
    private VBox transactionContainer;

    public void setClient(
            ClientSocket client
    ) {

        this.client = client;
    }

    public void setUser(
            User user
    ) {

        this.currentUser = user;

        updateBalance();
    }

    @FXML
    public void initialize() {

        System.out.println(
                "Wallet Loaded"
        );

        setupMenuEvents();
        setupWalletEvents();
    }

    private void setupWalletEvents() {

        depositBtn.setOnAction(
                e -> handleDeposit()
        );

        withdrawBtn.setOnAction(
                e -> handleWithdraw()
        );
    }

    private void setupMenuEvents() {

        infoBtn.setOnAction(
                e -> openPage(
                        "/fxml/userProfile-view.fxml",
                        "Profile"
                )
        );

        historyBtn.setOnAction(
                e -> openPage(
                        "/fxml/auctionHistory-view.fxml",
                        "Auction History"
                )
        );

        createdAuctionBtn.setOnAction(
                e -> openPage(
                        "/fxml/yourAuctions-view.fxml",
                        "Your Auctions"
                )
        );

        favoriteBtn.setOnAction(
                e -> openPage(
                        "/fxml/Favourite-view.fxml",
                        "Favourite"
                )
        );

        walletBtn.setOnAction(
                e -> openPage(
                        "/fxml/wallet-view.fxml",
                        "E-Wallet"
                )
        );

        backHomeBtn.setOnAction(
                e -> openPage(
                        "/fxml/HomePage.fxml",
                        "Home"
                )
        );

        logoutBtn.setOnAction(
                e -> handleLogout()
        );

        deleteAccountBtn.setOnAction(
                e -> handleDeleteAccount()
        );
    }

    @FXML
    private void handleDeposit() {

        String input =
                depositField.getText().trim();

        if (input.isEmpty()) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Deposit Error",
                    "Please enter an amount."
            );

            return;
        }

        try {

            BigDecimal amount =
                    new BigDecimal(input);

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Deposit Error",
                        "Amount must be greater than 0."
                );

                return;
            }

            /*
                TODO:
                send deposit request to server
             */

            BigDecimal newBalance =
                    currentUser.getBalance().add(amount);
            currentUser.setBalance(newBalance);

            updateBalance();

            depositField.clear();

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Deposit Success",
                    "Deposit successful."
            );

            System.out.println(
                    "Deposited: "
                            + amount
            );

        } catch (NumberFormatException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Deposit Error",
                    "Invalid amount."
            );
        }
    }
    @FXML
    private void handleWithdraw() {

        String input =
                withdrawField.getText().trim();

        if (input.isEmpty()) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Withdraw Error",
                    "Please enter an amount."
            );

            return;
        }

        try {

            BigDecimal amount =new BigDecimal(input);

            if (amount.compareTo(BigDecimal.ZERO)  <= 0) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Withdraw Error",
                        "Amount must be greater than 0."
                );

                return;
            }

            if (amount.compareTo(currentUser.getBalance()) > 0) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Withdraw Error",
                        "Insufficient balance."
                );

                return;
            }

            /*
                TODO:
                send withdraw request to server
             */

            BigDecimal newBalance = currentUser.getBalance().subtract(amount);


            currentUser.setBalance(
                    newBalance
            );

            updateBalance();

            withdrawField.clear();

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Withdraw Success",
                    "Withdraw successful."
            );

            System.out.println(
                    "Withdrawn: "
                            + amount
            );

        } catch (NumberFormatException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Withdraw Error",
                    "Invalid amount."
            );
        }
    }
    @FXML
    private void updateBalance() {

        if (currentUser == null) {
            return;
        }

        NumberFormat format =
                NumberFormat.getInstance(
                        Locale.of(
                                "vi",
                                "VN"
                        )
                );

        balanceLabel.setText(
                format.format(
                        currentUser.getBalance()
                ) + " VNĐ"
        );
    }
    @FXML
    private void handleLogout() {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(
                "Logout"
        );

        alert.setHeaderText(null);

        alert.setContentText(
                "Are you sure you want to logout?"
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

            openPage(
                    "/fxml/login-view.fxml",
                    "Login"
            );
        }
    }
    @FXML
    private void handleDeleteAccount() {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(
                "Delete Account"
        );

        alert.setHeaderText(null);

        alert.setContentText(
                "Are you sure you want to delete your account? This action cannot be undone.3"
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

            openPage(
                    "/fxml/login-view.fxml",
                    "Login"
            );
        }
    }

    private void showAlert(
            Alert.AlertType type,
            String title,
            String content
    ) {

        Alert alert =
                new Alert(type);

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(content);

        alert.showAndWait();
    }

    private void openPage(
            String fxmlPath,
            String title
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

            Stage stage =
                    (Stage)
                            backHomeBtn
                                    .getScene()
                                    .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    title
            );

            stage.show();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.out.println("Cannot open: " + fxmlPath);
        }
    }

    private void passData(
            Object controller
    ) {

        if (
                controller instanceof UserDataReceiver c
        ) {

            c.setClient(client);

            c.setUser(currentUser);
        }
    }
}