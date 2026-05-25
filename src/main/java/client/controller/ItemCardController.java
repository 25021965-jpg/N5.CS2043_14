package client.controller;

import client.network.ClientSocket;
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

    // ==================== FXML ====================

    @FXML private ImageView imgProduct;

    @FXML private Label lblName;
    @FXML private Label lblCategory;
    @FXML private Label lblStatus;

    @FXML private Label lblCurrentPrice;
    @FXML private Label lblStep;
    @FXML private Label lblEndTime;

    @FXML
    public void initialize() {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(20);
        clip.setArcHeight(20);

        clip.widthProperty().bind(imgProduct.fitWidthProperty());
        clip.heightProperty().bind(imgProduct.fitHeightProperty());

        imgProduct.setClip(clip);

        imgProduct.setPreserveRatio(false);
        imgProduct.setSmooth(true);
    }

    // ==================== VARIABLES ====================

    private static final Map<String, Image> IMAGE_CACHE =
            new HashMap<>();

    private Auction auction;
    private Parent root;
    private ClientSocket client;


    // ==================== SETTERS ====================

    public void setClient(ClientSocket client) {
        this.client = client;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    public Parent getRoot() {
        return root;
    }


    // ==================== LOAD DATA ====================

    public void setData(Auction auction) {

        this.auction = auction;

        if (auction == null || auction.getItem() == null)
            return;

        Item item = auction.getItem();

        NumberFormat moneyFormatter =
                NumberFormat.getCurrencyInstance(
                        Locale.US
                );


        // ---------- NAME ----------

        lblName.setText(item.getName());


        // ---------- CATEGORY ----------

        if (item.getCategory() != null) {

            String raw =
                    item.getCategory()
                            .name()
                            .replace("_", " ")
                            .toLowerCase();

            StringBuilder sb =
                    new StringBuilder();

            for (String word : raw.split(" ")) {

                sb.append(
                                Character.toUpperCase(
                                        word.charAt(0)
                                )
                        )
                        .append(
                                word.substring(1)
                        )
                        .append(" ");
            }

            lblCategory.setText(
                    TextUtils.formatCategory(
                            item.getCategory().name()
                    )
            );

        }

        else {lblCategory.setText("Category: —");
        }



        // ---------- STATUS ----------
        if (auction.getStatus() != null) {
            lblStatus.setText(
                    TextUtils.withLabel(
                            "Status",
                            TextUtils.toTitleCase(
                                    auction.getStatus().name()
                            )
                    )
            );

            switch (auction.getStatus()) {

                case ACTIVE ->

                        lblStatus.setStyle(
                                "-fx-text-fill:#7CFC00;"
                        );

                case ENDED ->

                        lblStatus.setStyle(
                                "-fx-text-fill:#ff4d4d;"
                        );

                case PENDING_APPROVAL ->

                        lblStatus.setStyle(
                                "-fx-text-fill:#facc15;"
                        );

                default ->

                        lblStatus.setStyle(
                                "-fx-text-fill:white;"
                        );
            }

        }

        else {

            lblStatus.setText("Status: —");
        }


        //PRICE
        lblCurrentPrice.setText(
                TextUtils.withLabel(
                        "Current Price",
                        moneyFormatter.format(auction.getCurrentPrice())
                )
        );

        lblStep.setText(
                TextUtils.withLabel(
                        "Step",
                        moneyFormatter.format(auction.getMinIncrement())
                )
        );


        // ---------- END TIME ----------

        if (auction.getEndTime() != null) {

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(
                            "HH:mm dd/MM/yyyy"
                    );

            lblEndTime.setText(
                    "End at: "
                            + auction
                            .getEndTime()
                            .format(formatter)
            );
        }



        // ---------- IMAGE ----------

        loadImage(item);
    }



    // ==================== IMAGE ====================

    private void loadImage(Item item) {

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
                    IMAGE_CACHE.get(
                            imagePath
                    );

            if (image == null) {

                File file =
                        new File(
                                imagePath
                        );

                image =
                        file.exists()

                                ? new Image(
                                file.toURI()
                                .toString(),

                                235,
                                165,

                                false,
                                true
                        )

                                : getFallbackImage();

                IMAGE_CACHE.put(
                        imagePath,
                        image
                );
            }

            imgProduct.setImage(
                    image
            );

        }

        catch (Exception e) {

            System.out.println(
                    "Image error: "
                            + e.getMessage()
            );

            imgProduct.setImage(
                    getFallbackImage()
            );
        }
    }



    private Image getFallbackImage() {

        URL url =
                getClass()
                        .getResource(
                                "/image/no-image.png"
                        );

        if (url != null) {

            return new Image(
                    url.toExternalForm()
            );
        }

        System.out.println(
                "Missing fallback image"
        );

        return null;
    }



    // ==================== OPEN DETAIL ====================

    @FXML
    private void handleCardClick(
            MouseEvent event
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(

                            getClass()
                                    .getResource(
                                            "/fxml/items-view.fxml"
                                    )
                    );

            Parent root =
                    loader.load();

            ItemViewController controller =
                    loader.getController();

            controller.setAuctionData(
                    auction
            );

            Stage stage =
                    (Stage)
                            ((Node)
                                    event.getSource())

                                    .getScene()

                                    .getWindow();

            stage.setScene(
                    new Scene(root)
            );
            stage.show();

        }

        catch (IOException e) {
            System.out.println(
                    "Error: "
                            + e.getMessage()
            );
        }
    }
}