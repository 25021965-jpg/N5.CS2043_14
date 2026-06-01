package client.network.response.handler;

import client.controller.FavouriteController;
import client.controller.HomePageController;
import client.manager.ControllerRegistry;
import client.manager.FavouriteManager;
import client.util.NavigationUtils;

import javafx.application.Platform;
import javafx.stage.Stage;

public class FavouriteHandler {
    public static void load(String data) {
        FavouriteManager.clear();
        FavouriteManager.loadFromResponse(data);
        FavouriteController favourite = ControllerRegistry.get(FavouriteController.class);
        if (favourite != null) {
            favourite.renderFavourite(data);
        }
        HomePageController home =
                ControllerRegistry.get(
                        HomePageController.class
                );
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
        FavouriteController favourite =
                ControllerRegistry.get(FavouriteController.class);

        if (favourite != null) {
            favourite.renderFavourite(
                    "LIST_FAVOURITES_EMPTY"
            );
        }

        FavouriteManager.clear();
    }

    public static void addSuccess(Stage stage) {
        NavigationUtils.showToast(stage, "Added to favourites!");
        reloadFavourite();
    }

    public static void addFailed(String data) {
        NavigationUtils.showError("Add favourite failed: " + data);
    }

    public static void removeSuccess(String data) {
        System.out.println("Removed favourite: " + data);
        reloadFavourite();
    }

    public static void removeFailed(String data) {
        NavigationUtils.showError("Remove favourite failed: " + data);
    }

    private static void reloadFavourite() {
        FavouriteController favourite =
                ControllerRegistry.get(FavouriteController.class);

        if (favourite != null) {
            favourite.loadFavourites();
        }
    }
}