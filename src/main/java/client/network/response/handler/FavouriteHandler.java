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

        FavouriteController favCtrl =
                FavouriteController.getInstance();

        if (favCtrl != null) {
            favCtrl.renderFavourite(data);
        }

        // THÊM ĐOẠN NÀY
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

        FavouriteController ctrl =
                FavouriteController.getInstance();

        if (ctrl != null) {
            ctrl.renderFavourite(
                    "LIST_FAVOURITES_EMPTY"
            );
        }
    }

    public static void addSuccess(Stage stage) {

        NavigationUtils.showToast(
                stage,
                "Added to favourites!"
        );

        FavouriteController ctrl =
                FavouriteController.getInstance();

        if (ctrl != null) {
            ctrl.loadFavourites();
        }
    }

    public static void addFailed(String data,
                                 Stage stage) {

        NavigationUtils.showError(
                "Add favourite failed: " + data
        );
    }

    public static void remove(String data) {

        FavouriteController ctrl =
                FavouriteController.getInstance();

        if (ctrl != null) {
            ctrl.removeItemFromUI(data);
        }
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