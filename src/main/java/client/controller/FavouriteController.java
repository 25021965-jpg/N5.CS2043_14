package client.controller;

import client.network.ClientSocket;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.*;

import javafx.stage.Stage;

import model.User;

import java.io.IOException;
import java.util.Optional;

public class FavouriteController implements UserDataReceiver {

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
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private ComboBox<String> categoryFilterComboBox;

    @FXML
    public void initialize() {

        System.out.println(
                "Favourite Page Loaded"
        );

        setupStatusFilter();
        setupCategoryFilter();
        setupMenuEvents();
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
    }

    private void setupStatusFilter() {

        statusFilterComboBox.getItems().addAll(
                "ALL",
                "ACTIVE",
                "FINISHED",
                "CANCELLED"
        );

        statusFilterComboBox.setValue(
                "ALL"
        );

        statusFilterComboBox.setOnAction(
                e -> filterFavourite()
        );
    }

    private void setupCategoryFilter() {

        categoryFilterComboBox.getItems().addAll(
                "ALL",
                "ACCESSORIES",
                "COLLECTIBLES",
                "ELECTRONICS",
                "FASHION",
                "HOME APPLIANCES",
                "VEHICLES",
                "OTHER"
        );

        categoryFilterComboBox.setValue(
                "ALL"
        );

        categoryFilterComboBox.setOnAction(
                e -> filterFavourite()
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

        favoriteBtn.setOnAction(
                e -> openPage(
                        "/fxml/favourite-view.fxml",
                        "Favourite"
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
                e -> {
                    // TODO:
                    // open history page
                }
        );

        balanceBtn.setOnAction(
                e -> {
                    // TODO:
                    // open balance page
                }
        );
    }

    private void filterFavourite() {

        String selectedStatus =
                statusFilterComboBox.getValue();

        String selectedCategory =
                categoryFilterComboBox.getValue();

        System.out.println(
                "Filter Status: "
                        + selectedStatus
        );

        System.out.println(
                "Filter Category: "
                        + selectedCategory
        );

        /*
            TODO:
            - filter favourite auction
            - update UI
         */
    }

    @FXML
    private void handleRemoveFavourite() {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(
                "Remove Favourite"
        );

        alert.setHeaderText(null);

        alert.setContentText(
                "Remove this auction from favourite? This auction will no longer appear in your favourite list."
        );

        ButtonType removeButton =
                new ButtonType(
                        "Remove"
                );

        ButtonType cancelButton =
                new ButtonType(
                        "Cancel",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        alert.getButtonTypes().setAll(
                removeButton,
                cancelButton
        );

        DialogPane dialogPane =
                alert.getDialogPane();


        Optional<ButtonType> result =
                alert.showAndWait();

        if (
                result.isPresent()
                        &&
                        result.get() == removeButton
        ) {

            System.out.println(
                    "Favourite removed!"
            );

            /*
                TODO:
                - remove favourite from database
                - refresh UI
             */
        }
    }

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
                "Are you sure you want to logout? You will need to login again."
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

        DialogPane dialogPane =
                alert.getDialogPane();

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

        DialogPane dialogPane =
                alert.getDialogPane();

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