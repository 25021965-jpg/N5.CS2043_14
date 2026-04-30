module com.btl.auction_system {
    requires javafx.controls;
    requires javafx.fxml;

    // Cho phép JavaFX quét các file FXML trong thư mục view
    opens com.btl.auction_system.view to javafx.fxml;

    // Cho phép JavaFX "nhìn thấy" các biến @FXML trong Controller
    opens com.btl.auction_system.controller to javafx.fxml;

    exports com.btl.auction_system;
}