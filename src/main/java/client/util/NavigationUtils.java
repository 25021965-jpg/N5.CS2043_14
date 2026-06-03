package client.util;

import client.controller.UserDataReceiver;

import client.manager.UserSession;
import client.manager.ViewCache;
import client.network.ClientSocket;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import model.User;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


public class NavigationUtils {

    private static Stage mainStage;
    private static String currentPage;

    public static void setCurrentPage(String page) {
        System.out.println("SET PAGE -> " + page);
        currentPage = page;
    }

    public static String getCurrentPage() {
        System.out.println("GET PAGE -> " + currentPage);
        return currentPage;
    }

    private static final Logger LOGGER =
            Logger.getLogger(NavigationUtils.class.getName());

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    // ==================== LOAD FXML ====================
    public static FXMLLoader loadFXML(
            String fxmlPath
    ) {

        URL url = NavigationUtils.class.getResource(fxmlPath);

        if (url == null) {
            throw new RuntimeException(
                    "FXML NOT FOUND: " + fxmlPath
            );
        }

        return new FXMLLoader(url);
    }

    // ==================== SWITCH SCENE ====================

    public static void switchScene(
            Stage stage,
            String fxmlPath,
            String title
    ) {

        switchScene(
                stage,
                fxmlPath,
                title,
                ClientSocket.getInstance(),
                UserSession.getCurrentUser()
        );
    }

    // ==================== SWITCH SCENE WITH DATA ====================
    public static void switchScene(
            Stage stage,
            String fxmlPath,
            String title,
            ClientSocket client,
            User user
    ) {
        try {
            Parent root = ViewCache.get(fxmlPath);
            if (root == null) {
                FXMLLoader loader = loadFXML(fxmlPath);
                root = loader.load();
                Object controller = loader.getController();
                root.getProperties().put("controller", controller);
                injectUserData(controller, client, user);
                ViewCache.put(fxmlPath, root);

            } else {
                Object controller =
                        root.getProperties()
                                .get("controller");

                injectUserData(controller, client, user
                );
            }
            applyStage(stage, root, title);

        } catch (IOException e) {
            LOGGER.severe("Navigation Error");
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
        }
    }

    public static <T> T switchSceneAndGetController(
            Stage stage,
            String fxmlPath,
            String title
    ) {

        try {
            Parent root = ViewCache.get(fxmlPath);
            Object controller;
            if (root == null) {
                FXMLLoader loader = loadFXML(fxmlPath);
                root = loader.load();
                controller = loader.getController();
                root.getProperties().put("controller", controller);
                ViewCache.put(
                        fxmlPath,
                        root
                );
            } else {
                controller = root.getProperties().get("controller");
            }
            applyStage(stage, root, title);
            return (T) controller;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== APPLY STAGE ====================
    private static void applyStage(
            Stage stage,
            Parent root,
            String title
    ) {
        if (stage.getScene() == null) {
            stage.setScene(new Scene(root));
        } else {
            stage.getScene().setRoot(root);
        }
        stage.setTitle(title);
        stage.show();
    }

    public static Stage getCurrentStage() {
        return mainStage;
    }

    // ==================== INJECT DATA ====================
    private static void injectUserData(
            Object controller,
            ClientSocket client,
            User user
    ) {
        if (controller == null) {
            return;
        }

        if (controller instanceof UserDataReceiver receiver) {
            receiver.setClient(client);
            receiver.setUser(user);
        }
    }
}

