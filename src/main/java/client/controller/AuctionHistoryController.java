package client.controller;

import client.network.ClientSocket;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.*;

import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import model.User;

import java.io.IOException;
import java.util.Optional;

public class AuctionHistoryController implements UserDataReceiver {

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
    private ComboBox<String> filterComboBox;

    @FXML
    private VBox historyContainer;

    public void setClient(
            ClientSocket client
    ) {

        this.client = client;
    }

    public void setUser(
            User user
    ) {

        this.currentUser = user;
    }

    @FXML
    public void initialize() {

        System.out.println(
                "AuctionHistory Loaded"
        );

        setupFilter();
        setupMenuEvents();
    }

    private void setupFilter() {

        filterComboBox.getItems().addAll(
                "All",
                "Winning",
                "Losing",
                "Ended"
        );

        filterComboBox.setValue(
                "All"
        );

        filterComboBox.setOnAction(
                e -> handleFilter()
        );
    }

    private void setupMenuEvents() {

        infoBtn.setOnAction(
                e -> openPage(
                        "/fxml/userProfile-view.fxml",
                        "Profile"
                )
        );

        createdAuctionBtn.setOnAction(
                e -> openPage(
                        "/fxml/yourAuctions-view.fxml",
                        "Your Auctions"
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

        historyBtn.setOnAction(
                e -> openPage(
                        "/fxml/auctionHistory-view.fxml",
                        "Auction History"
                )
        );

        favoriteBtn.setOnAction(
                e -> openPage(
                        "/fxml/Favourite-view.fxml",
                        "Favourite"

                )
        );

        balanceBtn.setOnAction(
                e -> {
                    // TODO:
                    // open balance page
                }
        );
    }

    private void handleFilter() {

        String selected =
                filterComboBox.getValue();

        System.out.println(
                "Filter: "
                        + selected
        );

        /*
            TODO:
            - filter history by status
         */
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
                    "Login"
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

            openPage(
                    "/fxml/login-view.fxml",
                    "Login"
            );
        }
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