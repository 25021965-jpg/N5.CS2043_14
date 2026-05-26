package client.controller;

import client.manager.UserSession;
import client.network.ClientSocket;
import client.network.response.ResponseHandler;
import client.util.NavigationUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Auction;
import model.Category;
import model.User;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class HomePageController {

    // ── Singleton & layout constants ──
    private static HomePageController instance;
    private static final int MAX_COLUMNS = 3;

    // mỗi lần switchScene tạo controller mới → initialized = false → cho phép
    // initialize() chạy request một lần duy nhất cho vòng đời controller đó.
    private boolean initialized = false;

    // ── Data ──
    private final List<Auction> auctionList = new ArrayList<>();
    // Cache controller theo auctionId để tái dùng node, tránh load FXML lại
    private final Map<String, ItemCardController> cardCache = new HashMap<>();

    // ── Filter state ──
    private Category selectedCategory = null;
    private String   selectedStatus   = null;

    // ── FXML bindings ──
    @FXML private GridPane  itemGrid;
    @FXML private TextField txtSearch;
    @FXML private Button btnAll, btnAccessories, btnCollectibles, btnElectronics,
            btnFashion, btnHomeAppliances, btnVehicles, btnOther;

    // ── Lifecycle ──

    public HomePageController() { instance = this; }

    public static HomePageController getInstance() { return instance; }

    public List<Auction> getAuctionList() { return auctionList; }

    @FXML
    public void initialize() {
        if (initialized) return;   // chặn gọi lại khi quay về trang
        instance = this;
        initialized = true;

        Platform.runLater(() -> {
            // Gán stage cho ResponseHandler để toast/alert có Stage tham chiếu
            if (itemGrid != null && itemGrid.getScene() != null) {
                ResponseHandler.setMainStage((Stage) itemGrid.getScene().getWindow());
            }

            highlight(btnAll);
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

            User user = UserSession.getCurrentUser();
            if (user != null) {
                // Gửi đúng 1 lần: LIST + LIST_FAVOURITES
                ClientSocket.getInstance().sendList();
                ClientSocket.getInstance().sendGetFavourite(user.getUser_id());
            }
        });
    }

    /**
     * Việc gửi request đã được xử lý trong initialize() dựa trên UserSession.
     */
    public void setUser(User user) {
        UserSession.setCurrentUser(user);
    }

    // ── Data update ────────────────

    public void updateAuctionList(List<Auction> auctions) {
        // Đảm bảo luôn chạy trên FX thread
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> updateAuctionList(auctions));
            return;
        }

        // Bỏ qua nếu dữ liệu không đổi (so sánh id + price + status)
        if (isSameData(auctionList, auctions)) return;

        auctionList.clear();
        auctionList.addAll(auctions);
        applyFilters();
    }

    /** So sánh nhanh hai list theo 3 trường quan trọng, tránh re-render thừa. */
    private boolean isSameData(List<Auction> old, List<Auction> next) {
        if (old.size() != next.size()) return false;
        for (int i = 0; i < next.size(); i++) {
            Auction o = old.get(i), n = next.get(i);
            if (!Objects.equals(o.getAuction_id(), n.getAuction_id())
                    || !o.getCurrentPrice().equals(n.getCurrentPrice())
                    || o.getStatus() != n.getStatus()) {
                return false;
            }
        }
        return true;
    }

    // ── Filter & search ──

    @FXML private void handleSearch(ActionEvent event) { applyFilters(); }

    @FXML private void showAll()           { highlight(btnAll);           selectedCategory = null;                    selectedStatus = null; applyFilters(); }
    @FXML private void showAccessories()   { highlight(btnAccessories);   filterByCategory(Category.ACCESSORIES);   }
    @FXML private void showCollectibles()  { highlight(btnCollectibles);  filterByCategory(Category.COLLECTIBLES);  }
    @FXML private void showElectronics()   { highlight(btnElectronics);   filterByCategory(Category.ELECTRONICS);   }
    @FXML private void showFashion()       { highlight(btnFashion);       filterByCategory(Category.FASHION);       }
    @FXML private void showHomeAppliances(){ highlight(btnHomeAppliances);filterByCategory(Category.HOME_APPLIANCES);}
    @FXML private void showVehicles()      { highlight(btnVehicles);      filterByCategory(Category.VEHICLES);      }
    @FXML private void showOther()         { highlight(btnOther);         filterByCategory(Category.OTHER);         }

    @FXML private void showAllStatus()  { selectedStatus = null;         applyFilters(); }
    @FXML private void showActive()     { selectedStatus = "ACTIVE";     applyFilters(); }
    @FXML private void showEnded()      { selectedStatus = "ENDED";      applyFilters(); }
    @FXML private void showCancelled()  { selectedStatus = "CANCELLED";  applyFilters(); }
    @FXML private void showUpcoming()   { selectedStatus = "UPCOMING";   applyFilters(); }

    private void filterByCategory(Category cat) { selectedCategory = cat; applyFilters(); }

    private void applyFilters() {
        if (txtSearch == null) return;
        String kw = txtSearch.getText().trim().toLowerCase();

        List<Auction> filtered = auctionList.stream().filter(a -> {
            if (a == null || a.getItem() == null || a.getItem().getName() == null) return false;
            boolean matchText = kw.isEmpty() || a.getItem().getName().toLowerCase().contains(kw);
            boolean matchCat  = selectedCategory == null || a.getItem().getCategory() == selectedCategory;
            boolean matchStat = selectedStatus   == null
                    || (a.getStatus() != null && a.getStatus().name().equalsIgnoreCase(selectedStatus));
            return matchText && matchCat && matchStat;
        }).toList();

        refreshGrid(filtered);
    }

    // ── Grid rendering ───

    /**
     * Cập nhật lại Grid hiển thị dựa trên danh sách đã lọc.
     * Giải thích: Thay vì cố gắng ẩn/hiện node cũ, ta clear() toàn bộ Grid và vẽ lại
     * từ cache (Map<String, ItemCardController>). Cách này đảm bảo không bao giờ
     * bị lặp card và luôn đúng vị trí sau khi filter.
     */
    private void refreshGrid(List<Auction> list) {
        // 1. Xóa sạch các node hiện tại trong GridPane để chuẩn bị vẽ lại
        itemGrid.getChildren().clear();

        int col = 0;
        int row = 0;

        // 2. Duyệt qua danh sách đã lọc
        for (Auction auction : list) {
            String id = auction.getAuction_id();

            // Lấy controller từ cache nếu đã tồn tại, tránh load lại FXML gây lag
            ItemCardController ctrl = cardCache.get(id);
            Parent root;

            if (ctrl == null) {
                // Nếu chưa có trong cache thì mới load mới từ FXML
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/itemsCard-view.fxml"));
                    root = loader.load();
                    ctrl = loader.getController();

                    root.setUserData(id);
                    ctrl.setRoot(root);
                    ctrl.setClient(ClientSocket.getInstance());

                    // Lưu vào cache để tái sử dụng sau này
                    cardCache.put(id, ctrl);
                } catch (IOException e) {
                    System.err.println("[HomePage] Card load error: " + e.getMessage());
                    continue; // Bỏ qua card lỗi
                }
            } else {
                // Nếu đã có trong cache thì lấy lại root cũ (đã tồn tại trong bộ nhớ)
                root = ctrl.getRoot();
            }

            // 3. Luôn cập nhật data mới nhất vào card (quan trọng để hiển thị đúng giá/status)
            ctrl.setData(auction);

            // 4. Đưa card vào GridPane theo vị trí tính toán
            itemGrid.add(root, col, row);

            // 5. Tăng vị trí cho card kế tiếp
            col++;
            if (col == MAX_COLUMNS) {
                col = 0;
                row++;
            }
        }

        // 6. Dọn dẹp cache: Xóa những card không còn xuất hiện trong danh sách mới (tiết kiệm RAM)
        Set<String> currentIds = list.stream().map(Auction::getAuction_id).collect(Collectors.toSet());
        cardCache.keySet().removeIf(id -> !currentIds.contains(id));
    }

    // ── Screen transitions ──

    public void openLiveAuction(Auction auction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/liveAuction-view.fxml"));
            Parent root = loader.load();
            LiveAuctionController ctrl = loader.getController();

            ctrl.setUser(UserSession.getCurrentUser());
            ctrl.setAuctionData(
                    auction.getAuction_id(),
                    auction.getItem().getName(),
                    auction.getItem().getDescription(),
                    auction.getCurrentPrice().toString(),
                    auction.getMinIncrement().toString(),
                    auction.getFloorPrice() != null ? auction.getFloorPrice().toString() : "0",
                    auction.getEndTime().toString(),
                    (auction.getItem().getImages() != null && !auction.getItem().getImages().isEmpty())
                            ? auction.getItem().getImages().get(0) : null
            );

            Stage stage = (Stage) itemGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Live Auction – " + auction.getItem().getName());
            stage.show();

        } catch (Exception e) {
            System.err.println("[HomePage] openLiveAuction error: " + e.getMessage());
            NavigationUtils.showError("Cannot open live auction: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateAuction(ActionEvent event) {
        NavigationUtils.switchScene(getStage(event), "/fxml/createAuction-view.fxml", "Create Auction");
    }

    @FXML private void openProfile(ActionEvent e)      { NavigationUtils.switchScene(getStage(e), "/fxml/userProfile-view.fxml",    "My Profile");      }
    @FXML private void openHistory(ActionEvent e)      { NavigationUtils.switchScene(getStage(e), "/fxml/auctionHistory-view.fxml", "Auction History"); }
    @FXML private void openYourAuctions(ActionEvent e) { NavigationUtils.switchScene(getStage(e), "/fxml/myAuctions-view.fxml",     "My Auctions");     }
    @FXML private void openFavourite(ActionEvent e)    { NavigationUtils.switchScene(getStage(e), "/fxml/Favourite-view.fxml",      "Favorites");       }

    @FXML
    private void openBalance(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/accountBalance-view.fxml"));
            Parent root = loader.load();
            AccountBalanceController ctrl = loader.getController();

            User user = UserSession.getCurrentUser();
            if (user != null) ctrl.setUser(user);

            // openBalance được gọi từ MenuItem (popup) nên cần lấy Stage theo cách khác
            Stage stage = (event.getSource() instanceof MenuItem mi)
                    ? (Stage) mi.getParentPopup().getOwnerWindow()
                    : (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Account Balance");
            stage.show();

        } catch (Exception e) {
            System.err.println("[HomePage] openBalance error: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        if (NavigationUtils.showConfirm("Logout", "Are you sure you want to logout?")) {
            UserSession.setCurrentUser(null);
            initialized = false;
            NavigationUtils.switchScene(getStage(event), "/fxml/login-view.fxml", "Login");
        }
    }

    @FXML
    private void handleDeleteAccount(ActionEvent event) {
        if (NavigationUtils.showConfirm("Danger", "Permanently delete account? This cannot be undone.")) {
            NavigationUtils.switchScene(getStage(event), "/fxml/login-view.fxml", "Login");
        }
    }

    // ── Helpers ──

    private void resetCategoryStyles() {
        final String DEFAULT = "-fx-background-color: #d4af37;";
        btnAll.setStyle(DEFAULT); btnAccessories.setStyle(DEFAULT); btnCollectibles.setStyle(DEFAULT);
        btnElectronics.setStyle(DEFAULT); btnFashion.setStyle(DEFAULT);
        btnHomeAppliances.setStyle(DEFAULT); btnVehicles.setStyle(DEFAULT); btnOther.setStyle(DEFAULT);
    }

    private void highlight(Button btn) {
        resetCategoryStyles();
        btn.setStyle("-fx-background-color: #ffe082;");
    }

    private Stage getStage(ActionEvent event) {
        if (event.getSource() instanceof MenuItem mi)
            return (Stage) mi.getParentPopup().getOwnerWindow();
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    public boolean isInitialized() { return initialized; }
}