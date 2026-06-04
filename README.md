# 🔨 Auction System

Ứng dụng đấu giá trực tuyến theo mô hình **Client – Server** sử dụng **TCP Socket**, cho phép nhiều người dùng đồng thời tham gia phiên đấu giá theo thời gian thực. Người dùng có thể đặt giá thầu, theo dõi kết quả trực tiếp và thanh toán sau khi thắng đấu giá. Quản trị viên có thể kiểm duyệt, giám sát và quản lý toàn bộ phiên đấu giá, sản phẩm và tài khoản người dùng.

---

## ⚙️ Công nghệ & Môi trường

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 21 |
| UI | JavaFX 21.0.6 + FXML |
| Build tool | Apache Maven 3.x |
| Cơ sở dữ liệu | TiDB Cloud (MySQL-compatible, port 4000) |
| Connection pool | HikariCP 5.1.0 |
| Lưu trữ ảnh | Cloudinary |
| Băm mật khẩu | jBCrypt 0.4 |
| Logging | Logback 1.5.13 |
| Unit test | JUnit 5.11 + Mockito 5.11 |
| CI/CD | GitHub Actions |

**Yêu cầu cài đặt:**

- Java JDK 21 trở lên [tải tại đây](https://adoptium.net/)
- Apache Maven 3.8+ [tải tại đây](https://maven.apache.org/download.cgi)
- Tài khoản TiDB Cloud (hoặc instance MySQL/TiDB tự host)
- Tài khoản Cloudinary (để upload ảnh sản phẩm)

---

## 📁 Cấu trúc thư mục

```
Auction_System/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── client/                   # Toàn bộ code phía Client
│   │   │   │   ├── controller/           # JavaFX Controller (MVC)
│   │   │   │   ├── manager/              # Quản lý trạng thái: Session, AuctionState, AutoBid...
│   │   │   │   ├── network/              # Socket kết nối server, xử lý response
│   │   │   │   │   └── response/
│   │   │   │   │       ├── handler/      # Xử lý từng loại phản hồi từ server
│   │   │   │   │       └── parser/       # Parse dữ liệu auction, bid, user...
│   │   │   │   ├── service/              # Dịch vụ tầng client
│   │   │   │   └── util/                 # Tiện ích UI, navigation, toast...
│   │   │   ├── common/                   # Dùng chung client & server
│   │   │   │   ├── Command.java          # Enum toàn bộ lệnh giao tiếp
│   │   │   │   ├── CommandBuilder.java   # Xây dựng chuỗi lệnh gửi đi
│   │   │   │   └── ResponseType.java     # Enum kiểu phản hồi từ server
│   │   │   ├── model/                    # Domain model (Auction, Bid, User, Item...)
│   │   │   │   ├── Entity/
│   │   │   │   │   ├── Item/             # Electronics, Fashion, Vehicles... (Kế thừa Item)
│   │   │   │   │   └── User/             # Admin, Seller, Bidder (Kế thừa User)
│   │   │   │   └── Factory/              # Factory tạo Item và User theo role/category
│   │   │   └── server/                   # Toàn bộ code phía Server
│   │   │       ├── dao/                  # Data Access Object (CRUD với TiDB)
│   │   │       ├── exception/            # Custom exception
│   │   │       ├── manager/              # RoomManager, AuctionTimerManager
│   │   │       ├── network/              # ServerSocket, ClientHandler, CommandDispatcher
│   │   │       │   └── handler/          # Xử lý từng nhóm lệnh (Auth, Bid, Auction...)
│   │   │       ├── observer/             # Observer pattern cho trạng thái phiên đấu giá
│   │   │       └── service/              # Business logic: Auction, Auth, Bid, AutoBid
│   │   └── resources/
│   │       └── fxml/                     # Giao diện FXML cho từng màn hình
│   └── test/                             # Unit test & Integration test
│       └── java/
│           ├── client/                   # Test client-side
│           ├── common/                   # Test common
│           ├── model/                    # Test model
│           └── server/                   # Test DAO, service, network
├── .github/workflows/ci.yml             # GitHub Actions CI pipeline
└── pom.xml
```

---

## 🔑 Cấu hình biến môi trường

Hệ thống đọc cấu hình kết nối từ **biến môi trường** (không hardcode). Cần thiết lập các biến sau trước khi chạy:

| Biến | Mô tả |
|---|---|
| `TIDB_HOST` | Host TiDB Cloud (ví dụ: `gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com`) |
| `TIDB_USER` | Username TiDB Cloud |
| `TIDB_PASS` | Password TiDB Cloud |
| `CLOUDINARY_NAME` | Cloud name trên Cloudinary |
| `CLOUDINARY_KEY` | API Key Cloudinary |
| `CLOUDINARY_SECRET` | API Secret Cloudinary |

**Linux / macOS:**

```bash
export TIDB_HOST=<your-tidb-host>
export TIDB_USER=<your-tidb-user>
export TIDB_PASS=<your-tidb-pass>
export CLOUDINARY_NAME=<your-cloud-name>
export CLOUDINARY_KEY=<your-api-key>
export CLOUDINARY_SECRET=<your-api-secret>
```

**Windows (Command Prompt):**

```cmd
set TIDB_HOST=<your-tidb-host>
set TIDB_USER=<your-tidb-user>
set TIDB_PASS=<your-tidb-pass>
set CLOUDINARY_NAME=<your-cloud-name>
set CLOUDINARY_KEY=<your-api-key>
set CLOUDINARY_SECRET=<your-api-secret>
```

**Windows (PowerShell):**

```powershell
$env:TIDB_HOST="<your-tidb-host>"
$env:TIDB_USER="<your-tidb-user>"
$env:TIDB_PASS="<your-tidb-pass>"
$env:CLOUDINARY_NAME="<your-cloud-name>"
$env:CLOUDINARY_KEY="<your-api-key>"
$env:CLOUDINARY_SECRET="<your-api-secret>"
```

---

## 🚀 Câu lệnh chạy chương trình

### 1. Clone và cài đặt dependencies

```bash
git clone <repository-url>
cd Auction_System
mvn install -DskipTests
```

### 2. Chạy Server (bắt buộc khởi động trước)

Server lắng nghe trên **cổng 9999**. Cần thiết lập biến môi trường trước (xem mục trên).

**Linux / macOS:**

```bash
mvn exec:java -Dexec.mainClass="server.network.ServerApp"
```

**Windows (Command Prompt / PowerShell):**

```cmd
mvn exec:java -Dexec.mainClass="server.network.ServerApp"
```

Khi khởi động thành công, terminal sẽ hiển thị:

```
--- Auction System Server Starting ---
✓ HikariCP Connection Pool initialized
✓ TiDB Cloud: Database and tables initialized successfully
Server is listening on port: 9999
Waiting for clients to connect...
```

### 3. Chạy Client (sau khi Server đã chạy)

**Linux / macOS:**

```bash
mvn javafx:run
```

**Windows (Command Prompt / PowerShell):**

```cmd
mvn javafx:run
```

> **Lưu ý:** Cần đặt biến môi trường trong **cùng phiên terminal** trước khi chạy lệnh `mvn`. Nếu dùng IDE (IntelliJ), thêm biến môi trường vào Run Configuration.

---

## 📋 Hướng dẫn chạy theo thứ tự

> ⚠️ **Quan trọng:** Server phải được khởi động trước Client.

```
Bước 1: Thiết lập biến môi trường (TIDB_HOST, TIDB_USER, TIDB_PASS, CLOUDINARY_*)
         ↓
Bước 2: Mở terminal 1 → chạy Server
         mvn exec:java -Dexec.mainClass="server.network.ServerApp"
         → Chờ thông báo "Server is listening on port: 9999"
         ↓
Bước 3: Mở terminal 2 (hoặc nhiều terminal khác) → chạy Client
         mvn javafx:run
         → Cửa sổ đăng nhập sẽ hiện ra
         ↓
Bước 4: Đăng nhập
         - Admin mặc định: username = admin / password = admin123
         - Hoặc đăng ký tài khoản mới
```

Có thể mở **nhiều cửa sổ Client** đồng thời để kiểm thử đấu giá nhiều người dùng.

---

## ✅ Danh sách chức năng đã hoàn thành

### 🔐 Xác thực

- [x] Đăng ký tài khoản mới (phân quyền: Bidder/Seller)
- [x] Đăng nhập / Đăng xuất
- [x] Quên mật khẩu (đặt lại qua email)
- [x] Bảo mật mật khẩu bằng BCrypt

### 👤 Người dùng – Trang chủ & Hồ sơ

- [x] Trang chủ hiển thị danh sách phiên đấu giá (lọc theo trạng thái, tìm kiếm theo tên)
- [x] Xem chi tiết phiên đấu giá và sản phẩm
- [x] Xem và cập nhật hồ sơ cá nhân
- [x] Xoá tài khoản

### 💰 Quản lý số dư

- [x] Nạp tiền vào ví
- [x] Rút tiền từ ví
- [x] Xem lịch sử giao dịch
- [x] Hệ thống **virtual balance** (số dư ảo tạm khấu trừ khi đặt giá, hoàn trả nếu thua)
- [x] Thanh toán phiên đấu giá đã thắng

### 🏷️ Tạo & Quản lý đấu giá (Seller)

- [x] Tạo phiên đấu giá mới với sản phẩm, hình ảnh (upload lên Cloudinary), giá khởi điểm, bước giá tối thiểu, thời gian bắt đầu/kết thúc
- [x] Xem danh sách phiên đấu giá do mình tạo
- [x] Phân loại sản phẩm: Electronics, Fashion, Accessories, Collectibles, Home Appliances, Vehicles, Other

### ⚡ Đấu giá trực tuyến (Bidder)

- [x] Tham gia / Rời phòng đấu giá
- [x] Đặt giá thầu theo thời gian thực (broadcast tức thì đến tất cả người trong phòng)
- [x] Xem lịch sử giá thầu trong phiên
- [x] **Auto-Bid:** Đặt giá tự động theo ngưỡng tối đa, ưu tiên người đăng ký trước
- [x] **Anti-Snipe:** Tự động gia hạn thêm 60 giây nếu có giá thầu trong 60 giây cuối (tối đa 5 lần)
- [x] Xem lịch sử các phiên đấu giá đã tham gia

### ❤️ Yêu thích

- [x] Thêm / Xoá phiên đấu giá khỏi danh sách yêu thích
- [x] Xem danh sách yêu thích

### 🛡️ Quản trị viên (Admin)

- [x] Xem và quản lý danh sách người dùng (xoá, đổi vai trò)
- [x] Xem và quản lý danh sách sản phẩm (xoá, cập nhật)
- [x] Duyệt phiên đấu giá chờ xét duyệt
- [x] Dừng / Tiếp tục / Huỷ phiên đấu giá đang diễn ra
- [x] Xem lịch sử toàn bộ phiên đấu giá

### 🧪 Kiểm thử & CI

- [x] Unit test cho Model, DAO, Service, Command
- [x] Integration test với TiDB Cloud (rollback sau mỗi test)
- [x] GitHub Actions CI pipeline tự động build và chạy test khi push/PR

---

## 📎 Tài liệu & Demo

| Tài nguyên | Link |
|---|---|
| 📄 Báo cáo PDF |[PDF](https://drive.google.com/file/d/1leS7vCn9OS9lQ_eiMxi_8PTG-98CVV6x/view?usp=drive_link)
| 🎬 Video demo |[Video](https://docs.google.com/videos/d/1qPXQ2ZzdNhWElzpjfuAcX_yHlvpYwT6w9sZbpVDRJjg/edit?scene=id.g59002b2b_0_2#scene=id.g59002b2b_0_2)

---

## 🗄️ Cơ sở dữ liệu

Database `auction_system` được tự động khởi tạo khi Server chạy lần đầu. Bao gồm các bảng: `users`, `items`, `item_images`, `auctions`, `bids`, `favourites`, `transactions`.

Tài khoản Admin mặc định được tạo tự động nếu chưa tồn tại:

```
Username : admin
Password : admin123
```

> Khuyến nghị đổi mật khẩu admin sau lần đăng nhập đầu tiên.
