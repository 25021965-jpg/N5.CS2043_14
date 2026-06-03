package client.controller;

import client.manager.UserSession;
import client.network.ClientSocket;
import client.util.NavigationUtils;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import model.Entity.User.User;

public abstract class BaseController
        implements UserDataReceiver {

    protected ClientSocket client;
    protected User currentUser;

    // ==================== USER DATA ====================

    @Override
    public void setClient(ClientSocket client) {
        this.client = client;
    }

    @Override
    public void setUser(User user) {
        this.currentUser = user;
    }
    // ==================== NAVIGATION ====================
    protected void switchScene(
            ActionEvent event,
            String fxml,
            String title
    ) {

        NavigationUtils.switchScene(
                getStage(event),
                fxml,
                title
        );
    }

    // ==================== STAGE ====================
    protected Stage getStage(ActionEvent event) {
        if (event.getSource() instanceof MenuItem menuItem) {

            return (Stage)
                    menuItem.getParentPopup()
                            .getOwnerWindow();
        }

        return (Stage)
                ((Node) event.getSource())
                        .getScene()
                        .getWindow();
    }

    protected Stage getStage(Control control) {

        if (control == null
                || control.getScene() == null) {
            return null;
        }

        return (Stage)
                control.getScene()
                        .getWindow();
    }

    protected void navigate(
            Stage stage,
            String fxml,
            String title
    ) {

        NavigationUtils.switchScene(
                stage,
                fxml,
                title,
                client,
                UserSession.getCurrentUser()
        );
    }

    // ==================== TEXT ====================

    protected String safeTrim(String text) {

        return text == null
                ? ""
                : text.trim();
    }

    // ==================== UI THREAD ====================

    protected void runUI(Runnable action) {
        if (action == null) {
            return;
        }
        if (javafx.application.Platform.isFxApplicationThread()) {
            action.run();
        } else {
            javafx.application.Platform.runLater(action);
        }
    }
}

