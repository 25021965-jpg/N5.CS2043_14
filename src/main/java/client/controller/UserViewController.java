package client.controller;

import java.io.IOException;

import client.network.ClientSocket;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.AuctionItem;

import java.io.IOException;

    public class UserViewController {
        private ClientSocket client;

        @FXML
        private GridPane itemGrid; // Kết nối với GridPane trong FXML

        @FXML
        private TextField txtSearch;

        @FXML
        private Button btnCreate;

        // Biến để quản lý vị trí thêm card (3 cột)
        private int column = 0;
        private int row = 0;

        /**
         * Hàm này dùng để thêm một Card mới vào danh sách hiển thị
         */
        public void addNewAuctionCard(AuctionItem item) {
            try {
                // 1. Load file FXML của cái Card
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/itemsCard-view.fxml"));
                Node card = loader.load();

                // 2. Lấy controller của Card để đổ dữ liệu vào
                ItemCardController cardController = loader.getController();
                cardController.setData(item);

                // 3. Tính toán vị trí: nếu đầy 3 cột thì xuống hàng
                if (column == 3) {
                    column = 0;
                    row++;
                }

                // 4. Thêm vào GridPane
                itemGrid.add(card, column++, row);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @FXML
        void handleCreateAuction(ActionEvent event) {
            try {
                // Chuyển từ UserView sang trang CreateAuction
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/createAuction-view.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                System.out.println("Không tìm thấy file createAuction-view.fxml");
            }
        }

        @FXML
        void handleSearch(ActionEvent event) {
            String keyword = txtSearch.getText();
            System.out.println("Đang tìm kiếm: " + keyword);
            // Viết logic lọc card ở đây
        }
        public void setClient(ClientSocket client) {
            this.client = client;


        }
    }

