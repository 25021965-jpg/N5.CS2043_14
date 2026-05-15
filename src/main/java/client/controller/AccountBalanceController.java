package client.controller;

import client.manager.UserSession;
import client.network.ClientSocket;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.*;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import model.User;
import server.dao.UserDAO;

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
    private Button balanceBtn;

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

    @Override
    public void setUser(User user) {

        if (user == null) {
            System.out.println("Received NULL user");
            return;
        }

        this.currentUser = user;

        if (currentUser.getBalance() == null) {
            currentUser.setBalance(BigDecimal.ZERO);
        }

        updateBalance();
        loadTransactions();
    }

    @FXML
    public void initialize() {
        System.out.println("Account Balance Loaded");

        client = ClientSocket.getInstance();
        currentUser = UserSession.getCurrentUser();

        if (currentUser != null) {
            updateBalance();
            loadTransactions();
        }

        setupMenuEvents();
        setupBalanceEvents();
    }

    private void setupBalanceEvents() {
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

        balanceBtn.setOnAction(
                e -> openPage(
                        "/fxml/accountBalance-view.fxml",
                        "Account Balance"
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
        if (currentUser == null) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No user logged in."
            );
            return;
        }

        String input = depositField.getText().trim();

        if (input.isEmpty()) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Deposit Error",
                    "Please enter an amount."
            );
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(input);

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(
                        Alert.AlertType.ERROR,
                        "Deposit Error",
                        "Amount must be greater than 0."
                );
                return;
            }

            // tính số dư mới
            BigDecimal newBalance =
                    currentUser.getBalance().add(amount);

            // cập nhật user
            currentUser.setBalance(newBalance);

            boolean saved =
                    UserDAO.updateBalance(
                            currentUser.getUser_id(),
                            newBalance
                    );

            if(saved){
                UserSession.setCurrentUser(currentUser);
            }

            // lưu lịch sử
            currentUser.addTransaction(
                    "Deposit: +" + amount + " VNĐ"
            );

            updateBalance();
            loadTransactions();

            depositField.clear();

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Deposit Success",
                    "Deposit successful."
            );

            System.out.println(
                    "Deposited: " + amount
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
        if (currentUser == null) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No user logged in."
            );
            return;
        }

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

            BigDecimal newBalance = currentUser.getBalance().subtract(amount);


            currentUser.setBalance(newBalance);

            boolean saved =
                    UserDAO.updateBalance(
                            currentUser.getUser_id(),
                            newBalance
                    );

            if(saved){
                UserSession.setCurrentUser(currentUser);
            }

            currentUser.addTransaction(
                    "Withdraw: -" + amount + " VNĐ"
            );

            updateBalance();
            loadTransactions();

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

        if (currentUser.getBalance() == null) {
            currentUser.setBalance(BigDecimal.ZERO);
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

    private void loadTransactions() {

        transactionContainer.getChildren().clear();

        if (currentUser == null ||
                currentUser.getTransactions() == null ||
                currentUser.getTransactions().isEmpty()) {
            return;
        }

        for (String tx : currentUser.getTransactions()) {

            HBox card = new HBox(20);
            card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-border-color: #E2E8F0;
            -fx-padding: 15;
        """);

            VBox left = new VBox(5);

            Label title = new Label(
                    tx.startsWith("Deposit")
                            ? "Deposit Successfully"
                            : "Withdraw Successfully"
            );

            title.setStyle("""
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            -fx-text-fill: #0F172A;
        """);

            Label time = new Label(
                    java.time.LocalDateTime.now()
                            .format(
                                    java.time.format.DateTimeFormatter.ofPattern(
                                            "dd/MM/yyyy - HH:mm"
                                    )
                            )
            );

            time.setStyle("""
            -fx-text-fill: #64748B;
            -fx-font-size: 13px;
        """);

            left.getChildren().addAll(title, time);

            Region spacer = new Region();
            HBox.setHgrow(
                    spacer,
                    Priority.ALWAYS
            );

            Label amount = getLabel(tx);

            card.getChildren().addAll(
                    left,
                    spacer,
                    amount
            );

            transactionContainer.getChildren().add(card);
        }
    }

    private static Label getLabel(String tx) {
        Label amount = new Label(
                tx.replace("Deposit: ", "")
                        .replace("Withdraw: ", "")
        );

        amount.setStyle(tx.startsWith("Deposit")
                ? """
            -fx-text-fill: #16A34A;
            -fx-font-size: 18px;
            -fx-font-weight: bold;
        """
                : """
            -fx-text-fill: #EF4444;
            -fx-font-size: 18px;
            -fx-font-weight: bold;
        """
        );
        return amount;
    }

    @FXML
    private void handleLogout() {

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        alert.setTitle("Logout");
        alert.setHeaderText(null);
        alert.setContentText(
                "Are you sure you want to logout?"
        );

        ButtonType logoutButton =
                new ButtonType("Logout");

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

        if (result.isPresent()
                && result.get() == logoutButton) {

            ClientSocket socket =
                    ClientSocket.getInstance();

            if (socket != null) {
                socket.logout();
            }

            UserSession.setCurrentUser(null);

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
                "Are you sure you want to delete your account? This action cannot be undone."
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