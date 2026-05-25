package client.network.response.handler;

import client.controller.FavouriteController;
import client.manager.FavouriteManager;
import client.util.NavigationUtils;
import javafx.stage.Stage;

public class FavouriteHandler {

    public static void load(String data) {

        FavouriteManager.clear();
        FavouriteManager.loadFromResponse(data);

        FavouriteController ctrl = FavouriteController.getInstance();
        if (ctrl != null) ctrl.renderFavourite(data);
    }

    public static void empty() {
        FavouriteController ctrl = FavouriteController.getInstance();
        if (ctrl != null) ctrl.renderFavourite("FAVOURITES_EMPTY");
    }

    public static void addSuccess(Stage stage) {
        NavigationUtils.showToast(stage, "Added to favourites!");

        FavouriteController ctrl = FavouriteController.getInstance();
        if (ctrl != null) ctrl.loadFavourites();
    }

    public static void addFailed(String data, Stage stage) {
        NavigationUtils.showError("Add favourite failed: " + data);
    }

    public static void remove(String data) {
        FavouriteController ctrl = FavouriteController.getInstance();
        if (ctrl != null) ctrl.removeItemFromUI(data);
    }
}