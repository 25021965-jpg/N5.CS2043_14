package client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;

import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.*;

import java.io.IOException;

public class ItemCardController {
    @FXML private ImageView imgProduct;
    @FXML private Label lblName;
    @FXML private Label lblCategory;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblStep;
    @FXML private Label lblEndTime;
    private Auction auction;

    public void setData(Auction auction) {
        this.auction = auction;
        Item item = auction.getItem();

        lblName.setText("NAME: " + item.getName());

        String category =
                item.getCategory()
                        .name()
                        .replace("_", " ")
                        .toLowerCase();

        String[] words = category.split(" ");

        StringBuilder formattedCategory = new StringBuilder();

        for (String word : words) {

            formattedCategory.append(
                    word.substring(0, 1).toUpperCase()
                            + word.substring(1)
                            + " "
            );
        }

        lblCategory.setText(
                "Category: "
                        + formattedCategory.toString().trim()
        );

        lblCurrentPrice.setText("Current Price: " + auction.getCurrentPrice());

        lblStep.setText("Step: " + auction.getMinIncrement());

        if (item.getImages() != null && !item.getImages().isEmpty()) {
            try {
                // Lấy ảnh đầu tiên trong danh sách (vị trí số 0)
                String firstImagePath = item.getImages().get(0);

                Image image = new Image(firstImagePath);
                if (!image.isError()) {
                    imgProduct.setImage(image);
                } else {
                    System.out.println("Lỗi load ảnh tại: " + firstImagePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Nếu không có ảnh, có thể set một ảnh mặc định (placeholder)
            // imgProduct.setImage(new Image("/images/no-image.png"));
        }

        if (auction.getEndTime() != null) {
            // Định dạng lại thời gian
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
            lblEndTime.setText("End at: " + auction.getEndTime().format(formatter));
        }

    }
    @FXML
    private void handleCardClick(MouseEvent event) {
        try {
            // Đảm bảo tên file này khớp 100% với file trong thư mục resources/fxml
            String fxmlPath = "/fxml/items-view.fxml";

            java.net.URL fxmlLocation = getClass().getResource(fxmlPath);
            if (fxmlLocation == null) {
                System.err.println("Không tìm thấy file FXML tại: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Truyền dữ liệu sang trang chi tiết
            ItemViewController controller = loader.getController();
            if (controller != null) {
                controller.setAuctionData(this.auction);
            }

            // Chuyển màn hình
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Chi tiết sản phẩm: " + auction.getItem().getName());
            stage.show();

        } catch (IOException e) {
            System.err.println("Lỗi khi load trang chi tiết: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
