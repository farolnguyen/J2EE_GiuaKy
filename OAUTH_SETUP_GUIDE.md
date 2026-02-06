# OAuth Setup Guide - GitHub & Facebook

## 1. GitHub OAuth Setup

### Bước 1: Tạo GitHub OAuth App

1. Truy cập https://github.com/settings/developers
2. Click **"New OAuth App"**
3. Điền thông tin:
   ```
   Application name: Story Station
   Homepage URL: http://localhost:8080
   Authorization callback URL: http://localhost:8080/login/oauth2/code/github
   ```
4. Click **"Register application"**
5. Sau khi tạo xong, bạn sẽ thấy:
   - **Client ID**: Copy giá trị này
   - Click **"Generate a new client secret"**
   - **Client Secret**: Copy giá trị này (chỉ hiện 1 lần!)

### Bước 2: Thêm vào application.properties

Mở file `src/main/resources/application.properties` và thêm:

```properties
# GitHub OAuth
spring.security.oauth2.client.registration.github.client-id=YOUR_GITHUB_CLIENT_ID
spring.security.oauth2.client.registration.github.client-secret=YOUR_GITHUB_CLIENT_SECRET
spring.security.oauth2.client.registration.github.scope=read:user,user:email
```

**Thay thế:**
- `YOUR_GITHUB_CLIENT_ID` bằng Client ID từ bước 1
- `YOUR_GITHUB_CLIENT_SECRET` bằng Client Secret từ bước 1

---

## 2. Facebook OAuth Setup

### Bước 1: Tạo Facebook App

1. Truy cập https://developers.facebook.com/apps/
2. Click **"Create App"**
3. Chọn **"Consumer"** hoặc **"Other"** → Click **"Next"**
4. Điền thông tin:
   ```
   App name: Story Station
   App contact email: your-email@example.com
   ```
5. Click **"Create App"**

### Bước 2: Cấu hình Facebook Login

1. Trong Dashboard của app, tìm **"Facebook Login"** → Click **"Set Up"**
2. Chọn **"Web"**
3. Điền **Site URL**: `http://localhost:8080`
4. Click **"Save"** và **"Continue"**

### Bước 3: Cấu hình Valid OAuth Redirect URIs

1. Sidebar trái → **Facebook Login** → **Settings**
2. Thêm vào **Valid OAuth Redirect URIs**:
   ```
   http://localhost:8080/login/oauth2/code/facebook
   ```
3. Click **"Save Changes"**

### Bước 4: Lấy App ID và App Secret

1. Sidebar trái → **Settings** → **Basic**
2. Copy **App ID**
3. Click **"Show"** bên cạnh **App Secret** → Copy giá trị

### Bước 5: Thêm vào application.properties

Mở file `src/main/resources/application.properties` và thêm:

```properties
# Facebook OAuth
spring.security.oauth2.client.registration.facebook.client-id=YOUR_FACEBOOK_APP_ID
spring.security.oauth2.client.registration.facebook.client-secret=YOUR_FACEBOOK_APP_SECRET
spring.security.oauth2.client.registration.facebook.scope=email,public_profile
```

**Thay thế:**
- `YOUR_FACEBOOK_APP_ID` bằng App ID từ bước 4
- `YOUR_FACEBOOK_APP_SECRET` bằng App Secret từ bước 4

---

## 3. Test OAuth Login

### Sau khi cấu hình xong:

1. Restart ứng dụng:
   ```bash
   ./mvnw spring-boot:run
   ```

2. Truy cập http://localhost:8080/login

3. Bạn sẽ thấy 3 nút login:
   - **Continue with Google** ✅ (đã hoạt động)
   - **Continue with GitHub** 🆕 (vừa setup)
   - **Continue with Facebook** 🆕 (vừa setup)

4. Test từng OAuth provider:
   - Click vào nút
   - Đăng nhập bằng tài khoản tương ứng
   - Authorize app
   - Redirect về trang chủ → Login thành công!

---

## 4. Xử lý Production

Khi deploy lên production (VD: https://yourdomain.com):

### GitHub:
1. Vào GitHub OAuth App settings
2. Update **Homepage URL**: `https://yourdomain.com`
3. Update **Authorization callback URL**: `https://yourdomain.com/login/oauth2/code/github`

### Facebook:
1. Vào Facebook App Dashboard
2. **Settings** → **Basic** → **App Domains**: Thêm `yourdomain.com`
3. **Facebook Login** → **Settings** → **Valid OAuth Redirect URIs**: Thêm `https://yourdomain.com/login/oauth2/code/facebook`
4. Chuyển app sang **Live Mode** (thay vì Development Mode)

### application.properties:
- Tạo `application-prod.properties` với URLs production
- Hoặc dùng environment variables:
  ```properties
  spring.security.oauth2.client.registration.github.client-id=${GITHUB_CLIENT_ID}
  spring.security.oauth2.client.registration.github.client-secret=${GITHUB_CLIENT_SECRET}
  ```

---

## 5. Troubleshooting

### Lỗi: redirect_uri_mismatch
- **Nguyên nhân**: Callback URL không khớp
- **Giải pháp**: Kiểm tra lại callback URL trong OAuth App settings phải giống chính xác với URL trong code

### Lỗi: invalid_client
- **Nguyên nhân**: Client ID hoặc Client Secret sai
- **Giải pháp**: Copy lại Client ID và Secret, restart app

### Lỗi: Email already registered
- **Nguyên nhân**: Email đã đăng ký bằng phương thức khác
- **Giải pháp**: Đăng nhập bằng phương thức đã đăng ký trước đó

---

## 6. Security Notes

⚠️ **QUAN TRỌNG:**

1. **KHÔNG commit** `application.properties` có chứa Client Secret vào Git
2. Thêm vào `.gitignore`:
   ```
   src/main/resources/application.properties
   src/main/resources/application-*.properties
   ```
3. Tạo file `application.properties.template` để team khác biết cần config gì:
   ```properties
   # Template - Copy to application.properties and fill in values
   spring.security.oauth2.client.registration.github.client-id=YOUR_GITHUB_CLIENT_ID
   spring.security.oauth2.client.registration.github.client-secret=YOUR_GITHUB_CLIENT_SECRET
   # ... etc
   ```
4. Dùng environment variables cho production

---

## Summary

✅ **Hoàn tất setup khi:**
- Tạo được GitHub OAuth App → có Client ID & Secret
- Tạo được Facebook App → có App ID & Secret  
- Thêm credentials vào `application.properties`
- Test login thành công với cả 3 providers (Google, GitHub, Facebook)
