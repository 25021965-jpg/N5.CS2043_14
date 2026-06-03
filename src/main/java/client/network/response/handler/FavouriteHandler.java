package client.network.response.handler;

import client.controller.UserFavouriteController;
import client.controller.UserHomePageController;
import client.manager.ControllerRegistry;
import client.manager.FavouriteManager;
import client.util.AlertUtils;

import client.util.ToastUtils;
import javafx.application.Platform;
import javafx.stage.Stage;

public class FavouriteHandler {


    public static void load(String data) {
        FavouriteManager.clear();
        FavouriteManager.loadFromResponse(data);
        UserFavouriteController favourite = ControllerRegistry.get(UserFavouriteController.class);
        if (favourite != null) {
            favourite.renderFavourite(data);
        }
        UserHomePageController home =
                ControllerRegistry.get(
                        UserHomePageController.class
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
        UserFavouriteController favourite = ControllerRegistry.get(UserFavouriteController.class);
        if (favourite != null) {
            favourite.renderFavourite(
                    "LIST_FAVOURITES_EMPTY"
            );
        }
        FavouriteManager.clear();
    }

    public static void addSuccess(Stage stage) {
        System.out.println("Add favourite success");
        ToastUtils.show(stage, "Added to favourites!");
        reloadFavourite();
    }

    public static void addFailed(String data) {
        System.out.println("Add favourite failed: " + data);
        AlertUtils.error("Add favourite failed: " + data);
    }

    public static void removeSuccess(String data) {
        System.out.println("Removed favourite: " + data);
        UserFavouriteController fav =
                ControllerRegistry.get(UserFavouriteController.class);
        if (fav != null) {
            fav.loadFavourites();
        }    }

    public static void removeFailed(String data) {
        AlertUtils.error("Remove favourite failed: " + data);
    }

    private static void reloadFavourite() {
        UserFavouriteController favourite = ControllerRegistry.get(UserFavouriteController.class);
        if (favourite != null) {
            favourite.loadFavourites();
        }
    }
}