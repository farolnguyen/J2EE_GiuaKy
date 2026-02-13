# 📚 Story Station - Hệ thống Quản lý Nhà sách Trực tuyến

**Sinh viên:** Nguyễn Hải Đăng - 2280600668  
**Môn học:** Lập trình với Java Spring (J2EE)  
**Học viện:** HUTECH

---

## 🎯 Giới thiệu

Story Station là một ứng dụng web quản lý nhà sách trực tuyến được xây dựng bằng Spring Boot. Hệ thống cung cấp đầy đủ các tính năng từ quản lý sách, giỏ hàng, đặt hàng cho khách hàng đến bảng điều khiển quản trị toàn diện cho admin.

## ✨ Tính năng chính

### 🔐 Xác thực & Phân quyền
- **Đăng nhập/Đăng ký** với xác thực form truyền thống
- **OAuth2 Login** hỗ trợ 3 nhà cung cấp:
  - Google
  - GitHub  
  - Facebook
- **JWT Authentication** cho REST API
- **Phân quyền** dựa trên vai trò (ADMIN, USER)
- **Bảo mật tài khoản**: Khóa tài khoản sau nhiều lần đăng nhập sai
- **Quên mật khẩu** với OTP qua email

### 📖 Quản lý Sách
- Xem danh sách sách với **phân trang**
- **Tìm kiếm nâng cao**: theo tên, danh mục, khoảng giá
- **Lọc & sắp xếp**: theo giá, ngày xuất bản, độ mới
- **Chi tiết sách**: thông tin đầy đủ, sách liên quan
- **Featured books**: hiển thị sách nổi bật trên trang chủ
- **Quản lý tồn kho**: cảnh báo sách sắp hết hàng
- **Giảm giá**: hỗ trợ giảm giá theo phần trăm

### 🛒 Giỏ hàng & Đặt hàng
- **Thêm vào giỏ hàng** với kiểm tra tồn kho
- **Cập nhật số lượng** trong giỏ hàng
- **Xóa sản phẩm** khỏi giỏ hàng
- **Thanh toán** với nhiều phương thức
- **Theo dõi đơn hàng**: xem lịch sử và trạng thái
- **Hủy đơn hàng**: hoàn lại tồn kho tự động
- **Tính toán tổng tiền**: bao gồm giảm giá

### ❤️ Wishlist
- Thêm sách yêu thích vào danh sách mong muốn
- Quản lý danh sách wishlist
- Chuyển từ wishlist sang giỏ hàng

### 👤 Quản lý Người dùng
- **Hồ sơ cá nhân**: cập nhật thông tin, email, số điện thoại
- **Địa chỉ giao hàng**: quản lý địa chỉ giao hàng mặc định
- **Đổi mật khẩu**: thay đổi mật khẩu an toàn
- **Lịch sử đơn hàng**: xem tất cả đơn hàng đã đặt

### 🔧 Quản trị Admin
- **Dashboard tổng quan**:
  - Thống kê tổng số sách, đơn hàng, doanh thu
  - Cảnh báo sách sắp hết hàng
  - Đơn hàng gần đây
  - Biểu đồ thống kê

- **Quản lý Sách**:
  - CRUD đầy đủ (Thêm, Sửa, Xóa, Xem)
  - Bật/tắt hiển thị sách
  - Đánh dấu sách nổi bật
  - Quản lý tồn kho
  - Lịch sử thay đổi giá

- **Quản lý Danh mục**:
  - Thêm/Xóa danh mục
  - Kiểm tra ràng buộc trước khi xóa

- **Quản lý Đơn hàng**:
  - Xem tất cả đơn hàng
  - Lọc theo trạng thái
  - Cập nhật trạng thái đơn hàng
  - Xem chi tiết đơn hàng

- **Quản lý Người dùng**:
  - Xem danh sách người dùng
  - Bật/tắt tài khoản
  - Mở khóa tài khoản
  - Thăng/giáng cấp quyền Admin
  - Đổi mật khẩu người dùng

- **Báo cáo & Xuất dữ liệu**:
  - Báo cáo doanh thu
  - Xuất Excel (danh sách sách, đơn hàng)
  - Xuất PDF hóa đơn

### 🔔 Tính năng khác
- **Email Notifications**: Gửi email xác nhận đơn hàng
- **Audit Log**: Ghi lại các hoạt động quan trọng
- **Price History**: Theo dõi lịch sử thay đổi giá
- **Validation**: Kiểm tra dữ liệu đầu vào nghiêm ngặt
- **Error Handling**: Xử lý lỗi thân thiện với người dùng

## 🛠️ Công nghệ sử dụng

### Backend
- **Spring Boot 4.0.2**
- **Java 21**
- **Spring Data JPA** - ORM và database access
- **Spring Security** - Authentication & Authorization
- **Spring OAuth2 Client** - OAuth2 integration
- **JWT (JSON Web Token)** - API authentication
- **Spring Mail** - Email service
- **Lombok** - Giảm boilerplate code

### Frontend
- **Thymeleaf** - Template engine
- **HTML/CSS/JavaScript**
- **Bootstrap** (CSS framework - trong static resources)

### Database
- **MySQL** - Relational database
- **Hibernate** - JPA implementation

### Bảo mật
- **BCrypt** - Password encryption
- **JWT (jjwt 0.12.3)** - Token-based authentication
- **OAuth2** - Social login (Google, GitHub, Facebook)

### Export & Reporting
- **iText PDF 5.5.13.3** - PDF generation
- **Apache POI 5.2.5** - Excel generation

### Build Tool
- **Maven** - Dependency management & build

## 📁 Cấu trúc Project

```
-J2EE-StoryStation/
├── src/
│   ├── main/
│   │   ├── java/fit/hutech/spring/
│   │   │   ├── controllers/        # REST & MVC Controllers
│   │   │   │   ├── AdminController.java       # Quản trị admin
│   │   │   │   ├── AuthController.java        # API authentication
│   │   │   │   ├── BookController.java        # Quản lý sách
│   │   │   │   ├── CartController.java        # Giỏ hàng
│   │   │   │   ├── CheckoutController.java    # Thanh toán
│   │   │   │   ├── OrderController.java       # Đơn hàng
│   │   │   │   ├── ProfileController.java     # Hồ sơ người dùng
│   │   │   │   ├── WishlistController.java    # Danh sách yêu thích
│   │   │   │   └── ...
│   │   │   ├── entities/           # JPA Entities
│   │   │   │   ├── User.java              # Người dùng
│   │   │   │   ├── Book.java              # Sách
│   │   │   │   ├── Category.java          # Danh mục
│   │   │   │   ├── Order.java             # Đơn hàng
│   │   │   │   ├── CartItem.java          # Sản phẩm trong giỏ
│   │   │   │   ├── Invoice.java           # Hóa đơn
│   │   │   │   ├── Wishlist.java          # Wishlist
│   │   │   │   └── ...
│   │   │   ├── services/           # Business Logic
│   │   │   │   ├── BookService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   ├── CartService.java
│   │   │   │   ├── OAuthService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   ├── PdfService.java
│   │   │   │   └── ...
│   │   │   ├── repositories/       # Data Access Layer
│   │   │   ├── utils/             # Utilities
│   │   │   │   ├── SecurityConfig.java    # Spring Security config
│   │   │   │   └── JwtUtil.java           # JWT utilities
│   │   │   ├── filters/           # Custom filters
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   ├── handlers/          # Custom handlers
│   │   │   ├── validators/        # Custom validators
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── viewmodels/       # View Models
│   │   │   └── constants/        # Constants
│   │   └── resources/
│   │       ├── templates/         # Thymeleaf templates
│   │       │   ├── admin/        # Admin views
│   │       │   ├── book/         # Book views
│   │       │   ├── user/         # User views
│   │       │   ├── home/         # Home page
│   │       │   ├── orders/       # Order views
│   │       │   ├── profile/      # Profile views
│   │       │   └── ...
│   │       └── static/           # CSS, JS, Images
│   └── test/                     # Unit tests
├── pom.xml                       # Maven dependencies
├── data.sql                      # Sample data
├── OAUTH_SETUP_GUIDE.md         # Hướng dẫn cấu hình OAuth
└── README.md                     # File này
```

## 🚀 Cài đặt & Chạy ứng dụng

### Yêu cầu hệ thống
- **Java JDK 21** trở lên
- **Maven 3.6+**
- **MySQL 8.0+**
- **IDE**: IntelliJ IDEA, Eclipse, hoặc VS Code

### Bước 1: Clone Repository

```bash
git clone <repository-url>
cd -J2EE-StoryStation
```

### Bước 2: Cấu hình Database

1. Tạo database MySQL:
```sql
CREATE DATABASE storystation;
```

2. Tạo file `src/main/resources/application.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/storystation
spring.datasource.username=root
spring.datasource.password=your_password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Server Configuration
server.port=8080

# JWT Configuration
jwt.secret=your-secret-key-here-at-least-256-bits
jwt.expiration=86400000

# Email Configuration (Gmail example)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# OAuth2 Configuration (Optional - see OAUTH_SETUP_GUIDE.md)
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET

# GitHub OAuth (Optional)
spring.security.oauth2.client.registration.github.client-id=YOUR_GITHUB_CLIENT_ID
spring.security.oauth2.client.registration.github.client-secret=YOUR_GITHUB_CLIENT_SECRET

# Facebook OAuth (Optional)
spring.security.oauth2.client.registration.facebook.client-id=YOUR_FACEBOOK_APP_ID
spring.security.oauth2.client.registration.facebook.client-secret=YOUR_FACEBOOK_APP_SECRET
```

### Bước 3: Import dữ liệu mẫu (Optional)

```bash
mysql -u root -p storystation < data.sql
```

### Bước 4: Build và chạy ứng dụng

**Sử dụng Maven Wrapper:**
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

**Hoặc sử dụng Maven:**
```bash
mvn clean install
mvn spring-boot:run
```

**Hoặc chạy file JAR:**
```bash
mvn clean package
java -jar target/spring-0.0.1-SNAPSHOT.jar
```

### Bước 5: Truy cập ứng dụng

- **Trang chủ**: http://localhost:8080
- **Đăng nhập**: http://localhost:8080/login
- **Admin Panel**: http://localhost:8080/admin

### Tài khoản mặc định

Sau khi import `data.sql`, bạn cần tạo tài khoản admin thủ công hoặc đăng ký tài khoản mới, sau đó cấp quyền ADMIN trong database:

```sql
-- Tạo role ADMIN nếu chưa có
INSERT INTO role (id, name) VALUES (1, 'ADMIN'), (2, 'USER');

-- Cấp quyền ADMIN cho user (giả sử user_id = 1)
INSERT INTO user_role (user_id, role_id) VALUES (1, 1);
```

## 🔧 Cấu hình OAuth2 (Tùy chọn)

Để sử dụng đăng nhập qua Google, GitHub, hoặc Facebook, vui lòng xem hướng dẫn chi tiết trong file:
👉 **[OAUTH_SETUP_GUIDE.md](OAUTH_SETUP_GUIDE.md)**

## 📊 Database Schema

### Các bảng chính:

- **user**: Thông tin người dùng
- **role**: Vai trò (ADMIN, USER)
- **user_role**: Bảng trung gian user-role
- **book**: Thông tin sách
- **category**: Danh mục sách
- **cart_item**: Sản phẩm trong giỏ hàng
- **order**: Đơn hàng
- **order_item**: Chi tiết sản phẩm trong đơn hàng
- **invoice**: Hóa đơn
- **item_invoice**: Chi tiết hóa đơn
- **wishlist**: Danh sách yêu thích
- **price_history**: Lịch sử giá
- **audit_log**: Log hoạt động

## 🔐 Bảo mật

- **Password Encryption**: BCrypt với strength 10
- **JWT Token**: Hết hạn sau 24 giờ
- **Session Management**: Tối đa 1 session/user
- **CSRF Protection**: Bật cho form-based requests
- **XSS Prevention**: Thymeleaf auto-escaping
- **SQL Injection Prevention**: JPA Prepared Statements
- **Role-Based Access Control**: ADMIN/USER roles
- **Account Locking**: Sau 5 lần đăng nhập sai liên tiếp

## 📡 API Endpoints

### Authentication
- `POST /api/auth/login` - Đăng nhập (trả về JWT token)
- `POST /api/auth/register` - Đăng ký tài khoản

### Books (API)
- `GET /api/v1/books` - Lấy danh sách sách
- `GET /api/v1/books/{id}` - Lấy chi tiết sách
- `POST /api/v1/books` - Thêm sách (ADMIN only)
- `PUT /api/v1/books/{id}` - Cập nhật sách (ADMIN only)
- `DELETE /api/v1/books/{id}` - Xóa sách (ADMIN only)

**Lưu ý**: Các API endpoint yêu cầu JWT token trong header:
```
Authorization: Bearer <your-jwt-token>
```

## 🧪 Testing

```bash
# Chạy tất cả tests
mvn test

# Chạy tests với coverage
mvn test jacoco:report
```

## 📝 Các tính năng nổi bật

### 1. Tìm kiếm nâng cao
```java
bookService.advancedSearch(keyword, categoryId, minPrice, maxPrice, inStock, sortBy)
```

### 2. Quản lý tồn kho thông minh
- Tự động kiểm tra tồn kho khi thêm vào giỏ hàng
- Cảnh báo sách sắp hết (threshold configurable)
- Hoàn lại tồn kho khi hủy đơn hàng

### 3. OAuth2 Multi-Provider
- Hỗ trợ 3 OAuth providers: Google, GitHub, Facebook
- Tự động tạo tài khoản khi đăng nhập lần đầu
- Merge accounts based on email

### 4. Admin Dashboard
- Real-time statistics
- Low stock alerts
- Recent orders tracking
- Revenue reports

### 5. Email Notifications
- Order confirmation
- Password reset OTP
- Account notifications

## 🐛 Troubleshooting

### Lỗi: "Could not create connection to database"
- Kiểm tra MySQL đã chạy chưa
- Kiểm tra username/password trong `application.properties`
- Kiểm tra database đã được tạo chưa

### Lỗi: "Port 8080 already in use"
- Thay đổi port trong `application.properties`:
```properties
server.port=8081
```

### Lỗi OAuth2: "redirect_uri_mismatch"
- Kiểm tra callback URL trong OAuth app settings
- Đảm bảo URL khớp với `http://localhost:8080/login/oauth2/code/{provider}`

### Lỗi: "JWT token expired"
- Token hết hạn sau 24 giờ, cần login lại
- Hoặc tăng `jwt.expiration` trong properties

## 🤝 Đóng góp

Đây là project đồ án môn học. Mọi đóng góp và góp ý xin gửi về email sinh viên.

## 📄 License

Dự án này được phát triển cho mục đích học tập tại HUTECH.

## 📞 Liên hệ

**Sinh viên**: Nguyễn Hải Đăng  
**MSSV**: 2280600668  
**Trường**: HUTECH  

---

## 🎓 Ghi chú

Project này được phát triển như một phần của khóa học J2EE tại HUTECH, minh họa các khái niệm:
- Spring Boot framework
- RESTful API design
- Spring Security & OAuth2
- JPA/Hibernate ORM
- MVC architecture
- Service layer pattern
- Repository pattern
- DTO pattern
- JWT authentication
- Role-based authorization

**Last Updated**: 2026-02-13
