# Feature Test Report — Flash Sale MVC

Báo cáo kiểm thử tính năng theo 8 prompt yêu cầu (User, UserRepository, AuthController,
LoginView, Payment, PaymentController, PaymentView, UserSession) + lớp hỗ trợ `util.PasswordHasher`.

- **File test**: `test/FeatureTest.java`
- **Kết quả**: **PASSED = 64 / FAILED = 0** (exit code 0)
- **Môi trường**: JDK 17 (`C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot`), Windows PowerShell
- **Dữ liệu**: dùng file CSV tạm `data/_feature_test_*.csv`, tự tạo và tự xóa sau khi chạy (không đụng dữ liệu thật)

---

## 1. Lệnh sử dụng

Mở PowerShell tại thư mục gốc dự án và chạy:

```powershell
# Đường dẫn JDK 17
$javac = "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot\bin\javac.exe"
$java  = "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot\bin\java.exe"

# Biên dịch toàn bộ src/ + file test vào thư mục out_test
$files = @()
$files += Get-ChildItem -Recurse -Filter *.java src | Select-Object -ExpandProperty FullName
$files += (Resolve-Path test\FeatureTest.java).Path
& $javac -d out_test -encoding UTF-8 $files

# Chạy test
& $java "-Dfile.encoding=UTF-8" -cp out_test FeatureTest

# (Tuỳ chọn) Dọn thư mục build tạm
Remove-Item -Recurse -Force out_test
```

> Lưu ý PowerShell: phải để `"-Dfile.encoding=UTF-8"` trong dấu nháy kép, nếu không PowerShell
> sẽ hiểu nhầm và báo lỗi `Could not find or load main class .encoding=UTF-8`.

Nếu `java`/`javac` đã có sẵn trong PATH, có thể thay `$javac`/`$java` bằng `javac`/`java`.

---

## 2. Kết quả trả về (output thực tế)

```
==================================================================
 FEATURE TEST - FLASH SALE MVC
==================================================================

==================================================================
 PROMPT 1 - User Entity
==================================================================
> new User("U00001", now, now, "alice", <hash>, CUSTOMER, true)
> user.toCsvLine()
  => U00001,2026-07-18T14:00,2026-07-18T14:00,alice,30c952fab122c3f9759f02a6d95c3758b246b4fee239957b2d4fee46e26170c4,CUSTOMER,true
> new User().fromCsvLine(csv)  (round-trip)
  => id=U00001, username=alice, role=CUSTOMER, active=true
  [PASS] getId round-trip
  [PASS] getUsername round-trip
  [PASS] getRole round-trip
  [PASS] isActive round-trip
  [PASS] createdAt round-trip
  [PASS] passwordHash round-trip
> user.setUsername("alice2"); user.setActive(false)
  [PASS] setter/getter username
  [PASS] setter/getter active

==================================================================
 PROMPT 2 - UserRepository (CSV storage)
==================================================================
> repo.generateNextId()  (repo rong)
  => U00001
  [PASS] generateNextId rong = U00001
> repo.register(bob / SELLER)  -> ghi CSV
> repo.findByUsername("BOB")  (khong phan biet hoa/thuong)
  => bob / SELLER
  [PASS] findByUsername case-insensitive
  [PASS] findByUsername khong co -> null
> repo.validateLogin("bob", <hash dung>)
  => bob
  [PASS] validateLogin dung hash
> repo.validateLogin("bob", "sai")
  [PASS] validateLogin sai hash -> null
> repo.register(carol / inactive)
  [PASS] validateLogin user khoa -> null
> repo.updateUser(bob -> bob_updated)
  [PASS] updateUser tra ve true
  [PASS] update: username cu bien mat
  [PASS] update: username moi ton tai
> Noi dung file CSV sau cac thao tac:
  | U00001,...,bob_updated,<hash>,SELLER,true
  | U00002,...,carol,<hash>,CUSTOMER,false
  [PASS] findAll() = 2 ban ghi

==================================================================
 PROMPT 3 - AuthController
==================================================================
> auth.register(UserRole.SELLER)
Username: Password: [SUCCESS] User registered successfully.
  [PASS] register -> getCurrentUser != null
  [PASS] register -> role SELLER
  [PASS] register -> luu vao repo
> auth.logout()
[SUCCESS] Logout successful.
  [PASS] logout -> getCurrentUser null
> auth.login()  (seller1 / pw123)
Username: Password: [SUCCESS] Login successful.
  [PASS] login tra ve true
  [PASS] login -> getCurrentUser != null
> auth.loginAs(UserRole.SELLER)  (dung role)
Username: Password: [SUCCESS] Login successful.
  [PASS] loginAs(SELLER) thanh cong
> auth.loginAs(UserRole.ADMIN)  (sai role -> tu choi)
Username: Password: [FAILED] Invalid username or password.
  [PASS] loginAs(ADMIN) bi tu choi (null)

==================================================================
 PROMPT 4 - LoginView (console menu)
==================================================================
> view.showAuthMenu()
+==============================+
|        FLASH SALE LOGIN      |
+==============================+
| 1. Login                     |
| 2. Register                  |
| 3. Logout                    |
| 0. Exit                      |
+==============================+
Choose:   => choice=1
  [PASS] showAuthMenu tra ve '1'
> view.showRoleNavigation()  (nhap 2)
+==============================+
|        SELECT ROLE           |
+==============================+
| 1. Customer                  |
| 2. Seller                    |
| 3. Admin                     |
| 0. Back                      |
+==============================+
Choose:   => role=SELLER
  [PASS] showRoleNavigation -> SELLER
> view.inputUsername() / view.inputPassword()
Username: Password:   [PASS] inputUsername = bob
  [PASS] inputPassword = secret
> view.confirmLogout()  (nhap y)
----- LOGOUT -----
Are you sure you want to log out? (y/n):   [PASS] confirmLogout('y') = true
  [PASS] showRoleNavigation('0') = null (Back)

==================================================================
 PROMPT 5 - Payment Entity
==================================================================
> new Payment("PAY00001", now, now, "O00001", "C00001", BANKING, 150000)
> payment.toCsvLine()
  => PAY00001,2026-07-18T14:10,2026-07-18T14:10,O00001,C00001,BANKING,150000.0
> new Payment().fromCsvLine(csv)  (round-trip)
  => id=PAY00001, orderId=O00001, method=BANKING, amount=150000.0
  [PASS] id round-trip
  [PASS] orderId round-trip
  [PASS] customerId round-trip
  [PASS] paymentMethod round-trip
  [PASS] amount round-trip
  [PASS] createdAt round-trip

==================================================================
 PROMPT 6 & 7 - PaymentController + PaymentView
==================================================================
> PaymentView nhap '1' (CASH) + PaymentController.createPayment(...)
+==============================+
|       PAYMENT METHOD         |
+==============================+
| 1. CASH                      |
| 2. BANKING                   |
+==============================+
Choose:
+==============================+
|       PAYMENT SUCCESS        |
+==============================+
Payment ID : PAY00001
Order ID   : O00001
Method     : CASH
Amount     : 150000 VND
Status     : PAID
+==============================+
  [PASS] createPayment(menu) != null
  [PASS] method chon qua menu = CASH
  [PASS] payment id = PAY00001
  [PASS] da luu vao repository
> createPayment(..., BANKING)  (overload chi dinh method)
... (PAYMENT SUCCESS: PAY00002 / BANKING / 99000 VND) ...
  [PASS] method = BANKING
  [PASS] payment id = PAY00002
  [PASS] amount da luu
  [PASS] repo co 2 payment
  [PASS] getPaymentById hoat dong
> Noi dung file CSV payment:
  | PAY00001,...,O00001,C00001,CASH,150000.0
  | PAY00002,...,O00002,C00002,BANKING,99000.0

==================================================================
 PROMPT 8 - UserSession (Singleton)
==================================================================
  [PASS] getInstance() la singleton
  [PASS] ban dau chua dang nhap
> session.login(alice / CUSTOMER)
  => isLoggedIn=true, role=CUSTOMER, loginTime=2026-07-18T14:29:21.5803153
  [PASS] isLoggedIn sau login
  [PASS] getCurrentUser dung
  [PASS] getCurrentRole = CUSTOMER
  [PASS] hasRole(CUSTOMER) true
  [PASS] hasRole(ADMIN) false
  [PASS] loginTime duoc ghi
> session.logout()
  [PASS] khong con dang nhap sau logout
  [PASS] currentUser null sau logout
  [PASS] currentRole null sau logout

==================================================================
 UTIL - PasswordHasher (SHA-256)
==================================================================
> PasswordHasher.hash("secret") => 2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b
  [PASS] hash dai 64 ky tu
  [PASS] hash tat dinh
  [PASS] input khac -> hash khac
  [PASS] matches() dung mat khau
  [PASS] matches() sai mat khau
  [PASS] matches() hash null

===================================================
TOTAL: PASSED=64  FAILED=0
===================================================
```

---

## 3. Thông tin liên quan (mapping prompt → tính năng đã kiểm)

| Prompt | Lớp | Tính năng đã kiểm | Số test |
|--------|-----|-------------------|:------:|
| 1 | `model.Entity.User` | constructor, getter/setter, `toCsvLine()`, `fromCsvLine()` round-trip | 8 |
| 2 | `repository.UserRepository` | `register`, `findByUsername` (case-insensitive), `validateLogin` (đúng/sai/khóa), `updateUser`, `generateNextId`, ghi/đọc CSV | 11 |
| 3 | `controller.AuthController` | `register`, `login`, `logout`, `getCurrentUser`, validate role (`loginAs`) | 9 |
| 4 | `view.LoginView` | menu auth, role navigation, login form, logout menu, input validation | 7 |
| 5 | `model.Entity.Payment` | constructor, getter/setter, `toCsvLine()`, `fromCsvLine()` round-trip | 6 |
| 6+7 | `controller.PaymentController`, `view.PaymentView` | chọn method (CASH/BANKING), tạo payment, lưu CSV, hiển thị success | 9 |
| 8 | `session.UserSession` | singleton, login/logout, role, login timestamp | 11 |
| — | `util.PasswordHasher` | hash SHA-256, `matches()` | 6 |
| | | **Tổng** | **64** |

### Ghi chú kỹ thuật
- Các View (`LoginView`, `PaymentView`) và `AuthView` được test bằng cách bơm input qua
  `new Scanner("...")` để mô phỏng người dùng gõ phím, nên test chạy hoàn toàn tự động.
- `validateLogin` của repository nhận mật khẩu **đã băm** (giữ tầng repository sạch business logic);
  việc băm nằm ở `util.PasswordHasher`.
- File CSV tạm được xóa tự động ở cuối test; thư mục `out_test` là build tạm, có thể xóa sau khi chạy.
