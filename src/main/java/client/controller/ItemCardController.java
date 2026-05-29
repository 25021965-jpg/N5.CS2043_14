package client.controller;

import client.network.ClientSocket;
import client.util.NavigationUtils;
import client.util.TextUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Label;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.input.MouseEvent;

import javafx.scene.shape.Rectangle;

import javafx.stage.Stage;

import model.Auction;
import model.Item;

import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.text.NumberFormat;

import java.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ItemCardController {

    // ==================== CONSTANTS ====================

    private static final double IMAGE_WIDTH = 235;

    private static final double IMAGE_HEIGHT = 165;

    private static final String DEFAULT_STATUS_STYLE =
            "-fx-text-fill:white;";

    private static final Map<String, Image> IMAGE_CACHE =
            new HashMap<>();

    private static final NumberFormat MONEY_FORMAT =
            NumberFormat.getCurrencyInstance(Locale.US);

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    // ==================== FXML ====================

    @FXML
    private ImageView imgProduct;

    @FXML
    private Label lblName;

    @FXML
    private Label lblCategory;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblCurrentPrice;

    @FXML
    private Label lblStep;

    @FXML
    private Label lblEndTime;

    // ==================== VARIABLES ====================

    private Auction auction;

    private Parent root;

    private ClientSocket client;

    // ==================== INITIALIZE ====================

    @FXML
    public void initialize() {

        setupImageView();
    }

    private void setupImageView() {

        Rectangle clip = new Rectangle();

        clip.setArcWidth(20);

        clip.setArcHeight(20);

        clip.widthProperty().bind(
                imgProduct.fitWidthProperty()
        );

        clip.heightProperty().bind(
                imgProduct.fitHeightProperty()
        );

        imgProduct.setClip(clip);

        imgProduct.setPreserveRatio(false);

        imgProduct.setSmooth(true);
    }

    // ==================== SETTERS ====================

    public void setClient(
            ClientSocket client
    ) {

        this.client = client;
    }

    public void setRoot(
            Parent root
    ) {

        this.root = root;
    }

    public Parent getRoot() {

        return root;
    }

    // ==================== LOAD DATA ====================

    public void setData(
            Auction auction
    ) {

        this.auction = auction;

        if (auction == null
                || auction.getItem() == null) {

            return;
        }

        Item item =
                auction.getItem();

        setupName(item);

        setupCategory(item);

        setupStatus();

        setupPrices();

        setupEndTime();

        loadImage(item);
    }

    private void setupName(
            Item item
    ) {

        lblName.setText(
                TextUtils.safeText(
                        item.getName()
                )
        );
    }

    private void setupCategory(
            Item item
    ) {

        String categoryText =
                item.getCategory() != null

                        ? TextUtils.formatCategory(
                        item.getCategory().name()
                )

                        : "Category: —";

        lblCategory.setText(categoryText);
    }

    private void setupStatus() {

        if (auction.getStatus() == null) {

            lblStatus.setText("Status: —");

            lblStatus.setStyle(DEFAULT_STATUS_STYLE);

            return;
        }

        String status =
                TextUtils.toTitleCase(
                        auction.getStatus().name()
                );

        lblStatus.setText(
                TextUtils.withLabel(
                        "Status",
                        status
                )
        );

        lblStatus.setStyle(
                getStatusStyle(
                        auction.getStatus().name()
                )
        );
    }

    private String getStatusStyle(
            String status
    ) {

        return switch (status) {

            case "ACTIVE" ->
                    "-fx-text-fill:#7CFC00;";

            case "ENDED" ->
                    "-fx-text-fill:#ff4d4d;";

            case "PENDING_APPROVAL" ->
                    "-fx-text-fill:#facc15;";

            default ->
                    DEFAULT_STATUS_STYLE;
        };
    }

    private void setupPrices() {

        lblCurrentPrice.setText(
                TextUtils.withLabel(
                        "Current Price",
                        MONEY_FORMAT.format(
                                auction.getCurrentPrice()
                        )
                )
        );

        lblStep.setText(
                TextUtils.withLabel(
                        "Step",
                        MONEY_FORMAT.format(
                                auction.getMinIncrement()
                        )
                )
        );
    }

    private void setupEndTime() {

        if (auction.getEndTime() == null) {

            lblEndTime.setText(
                    "End at: —"
            );

            return;
        }

        lblEndTime.setText(
                "End at: "
                        + auction.getEndTime()
                        .format(TIME_FORMAT)
        );
    }

    // ==================== IMAGE ====================

    private void loadImage(
            Item item
    ) {

        if (item.getImages() == null
                || item.getImages().isEmpty()) {

            imgProduct.setImage(
                    getFallbackImage()
            );

            return;
        }

        try {

            String imagePath =
                    item.getImages()
                            .getFirst();

            Image image =
                    IMAGE_CACHE.get(imagePath);

            if (image == null) {

                image =
                        createImage(imagePath);

                IMAGE_CACHE.put(
                        imagePath,
                        image
                );
            }

            imgProduct.setImage(image);

        } catch (Exception e) {

            System.out.println(
                    "Image error: "
                            + e.getMessage()
            );

            imgProduct.setImage(
                    getFallbackImage()
            );
        }
    }

    private Image createImage(
            String imagePath
    ) {

        if (imagePath.startsWith("http")) {

            return new Image(
                    imagePath,
                    IMAGE_WIDTH,
                    IMAGE_HEIGHT,
                    false,
                    true,
                    true
            );
        }

        File file =
                new File(imagePath);

        return file.exists()

                ? new Image(
                file.toURI().toString(),
                IMAGE_WIDTH,
                IMAGE_HEIGHT,
                false,
                true
        )

                : getFallbackImage();
    }

    private Image getFallbackImage() {

        URL url =
                getClass().getResource(
                        "/image/no-image.png"
                );

        if (url == null) {

            System.out.println(
                    "Missing fallback image"
            );

            return null;
        }

        return new Image(
                url.toExternalForm()
        );
    }

    // ==================== OPEN DETAIL ====================

    @FXML
    private void handleCardClick(MouseEvent event) {

        if (auction == null) return;

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        NavigationUtils.switchScene(
                stage,
                "/fxml/items-view.fxml",
                "Item Detail",
                client,
                null
        );
    }
}