package client.controller;

import client.manager.UserSession;
import client.network.ClientSocket;
import client.util.AlertUtils;
import client.util.NavigationUtils;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;

import javafx.scene.Node;

import javafx.scene.control.Button;

import javafx.stage.Stage;

public class UserSidebarController {

    @FXML private Button infoBtn;
    @FXML private Button historyBtn;
    @FXML private Button createdAuctionBtn;
    @FXML private Button favoriteBtn;
    @FXML private Button balanceBtn;
    @FXML private Button logoutBtn;
    @FXML private Button deleteAccountBtn;

    // ================= INIT =================
    @FXML
    public void initialize() {
        refreshActivePage();
    }

    public void refreshActivePage() {
        resetButtons();
        String currentPage = NavigationUtils.getCurrentPage();
        if (currentPage == null) {
            return;
        }

        switch (currentPage) {
            case "PROFILE" -> setActive(infoBtn);
            case "HISTORY" -> setActive(historyBtn);
            case "CREATED_AUCTIONS" -> setActive(createdAuctionBtn);
            case "FAVOURITE" -> setActive(favoriteBtn);
            case "BALANCE" -> setActive(balanceBtn);
        }
    }

    // ================= STYLES =================
    private static final String NORMAL_STYLE = """
            -fx-background-color: #334155;
            -fx-text-fill: white;
            -fx-font-size: 15px;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
            """;

    private static final String ACTIVE_STYLE = """
            -fx-background-color: #162336;
            -fx-text-fill: white;
            -fx-font-size: 15px;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
            """;

    // ================= ACTIVE BUTTON =================

    private void resetButtons() {
        infoBtn.setStyle(NORMAL_STYLE);
        historyBtn.setStyle(NORMAL_STYLE);
        createdAuctionBtn.setStyle(NORMAL_STYLE);
        favoriteBtn.setStyle(NORMAL_STYLE);
        balanceBtn.setStyle(NORMAL_STYLE);
    }

    private void setActive(Button button) {
        resetButtons();
        button.setStyle(ACTIVE_STYLE);
    }

    // ================= NAVIGATION =================
    @FXML
    private void handleProfile(ActionEvent event) {
        NavigationUtils.setCurrentPage("PROFILE");
        switchPage(
                event,
                "/fxml/userProfile-view.fxml",
                "Profile"
        );
    }

    @FXML
    private void handleHistory(ActionEvent event) {
        NavigationUtils.setCurrentPage("HISTORY");
        switchPage(
                event,
                "/fxml/auctionHistory-view.fxml",
                "Auction History"
        );
    }

    @FXML
    private void handleCreatedAuction(ActionEvent event) {
        NavigationUtils.setCurrentPage("CREATED_AUCTIONS");
        switchPage(
                event,
                "/fxml/userCreatedAuctions-view.fxml",
                "Created Auctions"
        );
    }

    @FXML
    private void handleFavourite(ActionEvent event) {
        NavigationUtils.setCurrentPage("FAVOURITE");
        switchPage(
                event,
                "/fxml/userFavourite-view.fxml",
                "Favourite"
        );
    }

    @FXML
    private void handleBalance(ActionEvent event) {
        NavigationUtils.setCurrentPage("BALANCE");
        switchPage(
                event,
                "/fxml/userAccountBalance-view.fxml",
                "Balance"
        );
    }

    @FXML
    private void handleBackHome(ActionEvent event) {
        NavigationUtils.setCurrentPage("HOME");
        resetButtons();
        switchPage(
                event,
                "/fxml/userHomePage-view.fxml",
                "Home"
        );
    }

    // ================= LOGOUT =================
    @FXML
    private void handleLogout() {
        boolean confirmed =
                AlertUtils.confirm(
                        "Logout",
                        "Are you sure you want to logout?"
                );

        if (!confirmed) {
            return;
        }

        ClientSocket socket = ClientSocket.getInstance();

        if (socket != null) {
            socket.logout();
        }

        UserSession.setCurrentUser(null);
        NavigationUtils.setCurrentPage(null);
        Stage stage =
                (Stage)
                        logoutBtn
                                .getScene()
                                .getWindow();

        NavigationUtils.switchScene(
                stage,
                "/fxml/login-view.fxml",
                "Login"
        );
    }

    // ================= DELETE ACCOUNT =================
    @FXML
    private void handleDeleteAccount() {
        boolean confirmed =
                AlertUtils.confirm(
                        "Delete Account",
                        "Are you sure you want to delete your account? This action cannot be undone."
                );

        if (!confirmed) {
            return;
        }

        /*
            TODO:
            - delete account from database
            - send delete request to server
         */

        UserSession.setCurrentUser(null);
        NavigationUtils.setCurrentPage(null);

        Stage stage =
                (Stage)
                        deleteAccountBtn
                                .getScene()
                                .getWindow();

        NavigationUtils.switchScene(
                stage,
                "/fxml/login-view.fxml",
                "Login"
        );
    }

    // ================= HELPER =================

    private void switchPage(
            ActionEvent event,
            String fxmlPath,
            String title
    ) {

        Stage stage =
                (Stage)
                        ((Node)
                                event.getSource())
                                .getScene()
                                .getWindow();

        NavigationUtils.switchScene(
                stage,
                fxmlPath,
                title,
                ClientSocket.getInstance(),
                UserSession.getCurrentUser()
        );
    }
}