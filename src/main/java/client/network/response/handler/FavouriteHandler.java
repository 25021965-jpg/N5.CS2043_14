package client.network.response.handler;

import client.controller.FavouriteController;
import client.controller.HomePageController;
import client.manager.FavouriteManager;
import client.util.NavigationUtils;

import javafx.application.Platform;
import javafx.stage.Stage;

public class FavouriteHandler {

    public static void load(String data) {

        FavouriteManager.clear();

        FavouriteManager.loadFromResponse(data);

        FavouriteController favourite =
                FavouriteController.getInstance();

        if (favourite != null) {

            favourite.renderFavourite(data);
        }

        HomePageController home =
                HomePageController.getInstance();

        if (home != null) {

            Platform.runLater(() ->
                    home.updateAuctionList(
                            new java.util.ArrayList<>(
                                    home.getAuctionList()
                            )
                    )
            );
        }
    }

    public static void empty() {

        System.out.println(
                "Favourite list empty"
        );
    }

    public static void addSuccess(Stage stage) {

        NavigationUtils.showToast(
                stage,
                "Added to favourites!"
        );
    }

    public static void addFailed(
            String data,
            Stage stage
    ) {

        NavigationUtils.showError(
                "Add favourite failed: " + data
        );
    }

    public static void remove(String data) {

        System.out.println(
                "Removed favourite: " + data
        );
    }

    public static void removeFailed(
            String data,
            Stage stage
    ) {

        NavigationUtils.showError(
                "Remove favourite failed: " + data
        );
    }
}