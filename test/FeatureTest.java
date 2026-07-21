import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

import controller.AuthController;
import controller.PaymentController;
import model.Entity.Payment;
import model.Entity.User;
import model.Enum.PaymentMethod;
import model.Enum.UserRole;
import repository.PaymentRepository;
import repository.UserRepository;
import session.UserSession;
import util.PasswordHasher;
import view.AuthView;
import view.LoginView;
import view.PaymentView;

/**
 * Feature test bám theo 8 prompt yêu cầu (User, UserRepository, AuthController,
 * LoginView, Payment, PaymentController, PaymentView, UserSession).
 *
 * Chạy hoàn toàn tự động trên file CSV tạm trong thư mục data/ và dọn sạch
 * sau khi kết thúc. In ra từng bước "lệnh sử dụng" và "kết quả trả về".
 *
 * Cách chạy (Windows PowerShell, JDK 17):
 *   $javac = "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot\bin\javac.exe"
 *   $java  = "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot\bin\java.exe"
 *   $files = (Get-ChildItem -Recurse -Filter *.java src).FullName + (Resolve-Path test\FeatureTest.java).Path
 *   & $javac -d out_test -encoding UTF-8 $files
 *   & $java "-Dfile.encoding=UTF-8" -cp out_test FeatureTest
 */
public class FeatureTest {

    private static int passed = 0;
    private static int failed = 0;

    private static final String USER_CSV = "data/_feature_test_users.csv";
    private static final String PAY_CSV = "data/_feature_test_payments.csv";

    public static void main(String[] args) throws Exception {
        deleteIfExists(USER_CSV);
        deleteIfExists(PAY_CSV);

        banner("FEATURE TEST - FLASH SALE MVC");

        testPrompt1_UserEntity();
        testPrompt2_UserRepository();
        testPrompt3_AuthController();
        testPrompt4_LoginView();
        testPrompt5_PaymentEntity();
        testPrompt6And7_PaymentControllerAndView();
        testPrompt8_UserSession();
        testUtil_PasswordHasher();

        System.out.println();
        System.out.println("===================================================");
        System.out.println("TOTAL: PASSED=" + passed + "  FAILED=" + failed);
        System.out.println("===================================================");

        deleteIfExists(USER_CSV);
        deleteIfExists(PAY_CSV);

        if (failed > 0) {
            System.exit(1);
        }
    }

    // =====================================================
    // PROMPT 1 - User Entity
    // =====================================================
    private static void testPrompt1_UserEntity() {
        banner("PROMPT 1 - User Entity");

        LocalDateTime now = LocalDateTime.parse("2026-07-18T14:00:00");
        System.out.println("> new User(\"U00001\", now, now, \"alice\", <hash>, CUSTOMER, true)");
        User user = new User("U00001", now, now, "alice", PasswordHasher.hash("pw"), UserRole.CUSTOMER, true);

        System.out.println("> user.toCsvLine()");
        String csv = user.toCsvLine();
        System.out.println("  => " + csv);

        System.out.println("> new User().fromCsvLine(csv)  (round-trip)");
        User parsed = new User();
        parsed.fromCsvLine(csv);
        System.out.println("  => id=" + parsed.getId() + ", username=" + parsed.getUsername()
                + ", role=" + parsed.getRole() + ", active=" + parsed.isActive());

        check("getId round-trip", "U00001".equals(parsed.getId()));
        check("getUsername round-trip", "alice".equals(parsed.getUsername()));
        check("getRole round-trip", parsed.getRole() == UserRole.CUSTOMER);
        check("isActive round-trip", parsed.isActive());
        check("createdAt round-trip", now.equals(parsed.getCreatedAt()));
        check("passwordHash round-trip", user.getPasswordHash().equals(parsed.getPasswordHash()));

        System.out.println("> user.setUsername(\"alice2\"); user.setActive(false)");
        user.setUsername("alice2");
        user.setActive(false);
        check("setter/getter username", "alice2".equals(user.getUsername()));
        check("setter/getter active", !user.isActive());
    }

    // =====================================================
    // PROMPT 2 - UserRepository
    // =====================================================
    private static void testPrompt2_UserRepository() {
        banner("PROMPT 2 - UserRepository (CSV storage)");

        UserRepository repo = new UserRepository(USER_CSV);

        System.out.println("> repo.generateNextId()  (repo rong)");
        String id1 = repo.generateNextId();
        System.out.println("  => " + id1);
        check("generateNextId rong = U00001", "U00001".equals(id1));

        LocalDateTime now = LocalDateTime.now();
        String hash = PasswordHasher.hash("pass123");
        System.out.println("> repo.register(bob / SELLER)  -> ghi CSV");
        repo.register(new User(id1, now, now, "bob", hash, UserRole.SELLER, true));

        System.out.println("> repo.findByUsername(\"BOB\")  (khong phan biet hoa/thuong)");
        User found = repo.findByUsername("BOB");
        System.out.println("  => " + (found == null ? "null" : found.getUsername() + " / " + found.getRole()));
        check("findByUsername case-insensitive", found != null && "bob".equals(found.getUsername()));
        check("findByUsername khong co -> null", repo.findByUsername("ghost") == null);

        System.out.println("> repo.validateLogin(\"bob\", <hash dung>)");
        User ok = repo.validateLogin("bob", hash);
        System.out.println("  => " + (ok == null ? "null" : ok.getUsername()));
        check("validateLogin dung hash", ok != null);
        System.out.println("> repo.validateLogin(\"bob\", \"sai\")");
        check("validateLogin sai hash -> null", repo.validateLogin("bob", "sai") == null);

        System.out.println("> repo.register(carol / inactive)");
        repo.register(new User(repo.generateNextId(), now, now, "carol", hash, UserRole.CUSTOMER, false));
        check("validateLogin user khoa -> null", repo.validateLogin("carol", hash) == null);

        System.out.println("> repo.updateUser(bob -> bob_updated)");
        found.setUsername("bob_updated");
        found.setUpdatedAt(LocalDateTime.now());
        boolean updated = repo.updateUser(found);
        check("updateUser tra ve true", updated);
        check("update: username cu bien mat", repo.findByUsername("bob") == null);
        check("update: username moi ton tai", repo.findByUsername("bob_updated") != null);

        System.out.println("> Noi dung file CSV sau cac thao tac:");
        printFile(USER_CSV);
        check("findAll() = 2 ban ghi", repo.findAll().size() == 2);
    }

    // =====================================================
    // PROMPT 3 - AuthController
    // =====================================================
    private static void testPrompt3_AuthController() {
        banner("PROMPT 3 - AuthController");

        deleteIfExists(USER_CSV);
        UserRepository repo = new UserRepository(USER_CSV);

        // Input mo phong theo thu tu cac lan doc:
        // register(SELLER): username, password
        // login():          username, password
        // loginAs(SELLER):  username, password
        // loginAs(ADMIN):   username, password
        String input = String.join("\n",
                "seller1", "pw123",   // register
                "seller1", "pw123",   // login
                "seller1", "pw123",   // loginAs SELLER
                "seller1", "pw123")   // loginAs ADMIN
                + "\n";
        AuthView authView = new AuthView(new Scanner(input));
        AuthController auth = new AuthController(repo, authView);

        System.out.println("> auth.register(UserRole.SELLER)");
        auth.register(UserRole.SELLER);
        User current = auth.getCurrentUser();
        check("register -> getCurrentUser != null", current != null);
        check("register -> role SELLER", current != null && current.getRole() == UserRole.SELLER);
        check("register -> luu vao repo", repo.findByUsername("seller1") != null);

        System.out.println("> auth.logout()");
        auth.logout();
        check("logout -> getCurrentUser null", auth.getCurrentUser() == null);

        System.out.println("> auth.login()  (seller1 / pw123)");
        boolean logged = auth.login();
        check("login tra ve true", logged);
        check("login -> getCurrentUser != null", auth.getCurrentUser() != null);

        System.out.println("> auth.loginAs(UserRole.SELLER)  (dung role)");
        User asSeller = auth.loginAs(UserRole.SELLER);
        check("loginAs(SELLER) thanh cong", asSeller != null && asSeller.getRole() == UserRole.SELLER);

        System.out.println("> auth.loginAs(UserRole.ADMIN)  (sai role -> tu choi)");
        User asAdmin = auth.loginAs(UserRole.ADMIN);
        check("loginAs(ADMIN) bi tu choi (null)", asAdmin == null);
    }

    // =====================================================
    // PROMPT 4 - LoginView
    // =====================================================
    private static void testPrompt4_LoginView() {
        banner("PROMPT 4 - LoginView (console menu)");

        // showAuthMenu -> "1"; showRoleNavigation -> "2" (SELLER);
        // inputUsername -> "bob"; inputPassword -> "secret"; confirmLogout -> "y"
        String input = "1\n2\nbob\nsecret\ny\n";
        LoginView view = new LoginView(new Scanner(input));

        System.out.println("> view.showAuthMenu()");
        String choice = view.showAuthMenu();
        System.out.println("  => choice=" + choice);
        check("showAuthMenu tra ve '1'", "1".equals(choice));

        System.out.println("> view.showRoleNavigation()  (nhap 2)");
        UserRole role = view.showRoleNavigation();
        System.out.println("  => role=" + role);
        check("showRoleNavigation -> SELLER", role == UserRole.SELLER);

        System.out.println("> view.inputUsername() / view.inputPassword()");
        String username = view.inputUsername();
        String password = view.inputPassword();
        check("inputUsername = bob", "bob".equals(username));
        check("inputPassword = secret", "secret".equals(password));

        System.out.println("> view.confirmLogout()  (nhap y)");
        boolean confirmed = view.confirmLogout();
        check("confirmLogout('y') = true", confirmed);

        // showRoleNavigation voi '0' -> null (back)
        LoginView view2 = new LoginView(new Scanner("0\n"));
        check("showRoleNavigation('0') = null (Back)", view2.showRoleNavigation() == null);
    }

    // =====================================================
    // PROMPT 5 - Payment Entity
    // =====================================================
    private static void testPrompt5_PaymentEntity() {
        banner("PROMPT 5 - Payment Entity");

        LocalDateTime now = LocalDateTime.parse("2026-07-18T14:10:00");
        System.out.println("> new Payment(\"PAY00001\", now, now, \"O00001\", \"C00001\", BANKING, 150000)");
        Payment payment = new Payment("PAY00001", now, now, "O00001", "C00001", PaymentMethod.BANKING, 150000.0);

        System.out.println("> payment.toCsvLine()");
        String csv = payment.toCsvLine();
        System.out.println("  => " + csv);

        System.out.println("> new Payment().fromCsvLine(csv)  (round-trip)");
        Payment parsed = new Payment();
        parsed.fromCsvLine(csv);
        System.out.println("  => id=" + parsed.getId() + ", orderId=" + parsed.getOrderId()
                + ", method=" + parsed.getPaymentMethod() + ", amount=" + parsed.getAmount());

        check("id round-trip", "PAY00001".equals(parsed.getId()));
        check("orderId round-trip", "O00001".equals(parsed.getOrderId()));
        check("customerId round-trip", "C00001".equals(parsed.getCustomerId()));
        check("paymentMethod round-trip", parsed.getPaymentMethod() == PaymentMethod.BANKING);
        check("amount round-trip", parsed.getAmount() == 150000.0);
        check("createdAt round-trip", now.equals(parsed.getCreatedAt()));
    }

    // =====================================================
    // PROMPT 6 & 7 - PaymentController + PaymentView
    // =====================================================
    private static void testPrompt6And7_PaymentControllerAndView() {
        banner("PROMPT 6 & 7 - PaymentController + PaymentView");

        deleteIfExists(PAY_CSV);
        PaymentRepository payRepo = new PaymentRepository(PAY_CSV);

        System.out.println("> PaymentView nhap '1' (CASH) + PaymentController.createPayment(...)");
        PaymentController ctrl = new PaymentController(payRepo, new PaymentView(new Scanner("1\n")));
        Payment p1 = ctrl.createPayment("O00001", "C00001", 150000.0);
        check("createPayment(menu) != null", p1 != null);
        check("method chon qua menu = CASH", p1 != null && p1.getPaymentMethod() == PaymentMethod.CASH);
        check("payment id = PAY00001", p1 != null && "PAY00001".equals(p1.getId()));
        check("da luu vao repository", payRepo.findById("PAY00001") != null);

        System.out.println("> createPayment(..., BANKING)  (overload chi dinh method)");
        Payment p2 = ctrl.createPayment("O00002", "C00002", 99000.0, PaymentMethod.BANKING);
        check("method = BANKING", p2.getPaymentMethod() == PaymentMethod.BANKING);
        check("payment id = PAY00002", "PAY00002".equals(p2.getId()));
        check("amount da luu", payRepo.findById("PAY00002").getAmount() == 99000.0);
        check("repo co 2 payment", payRepo.findAll().size() == 2);
        check("getPaymentById hoat dong", ctrl.getPaymentById("PAY00001") != null);

        System.out.println("> Noi dung file CSV payment:");
        printFile(PAY_CSV);
    }

    // =====================================================
    // PROMPT 8 - UserSession
    // =====================================================
    private static void testPrompt8_UserSession() {
        banner("PROMPT 8 - UserSession (Singleton)");

        UserSession s1 = UserSession.getInstance();
        UserSession s2 = UserSession.getInstance();
        check("getInstance() la singleton", s1 == s2);
        check("ban dau chua dang nhap", !s1.isLoggedIn());

        LocalDateTime now = LocalDateTime.now();
        User u = new User("U00001", now, now, "alice", PasswordHasher.hash("pw"), UserRole.CUSTOMER, true);
        System.out.println("> session.login(alice / CUSTOMER)");
        s1.login(u);
        System.out.println("  => isLoggedIn=" + s1.isLoggedIn() + ", role=" + s1.getCurrentRole()
                + ", loginTime=" + s1.getLoginTime());
        check("isLoggedIn sau login", s1.isLoggedIn());
        check("getCurrentUser dung", s1.getCurrentUser() == u);
        check("getCurrentRole = CUSTOMER", s1.getCurrentRole() == UserRole.CUSTOMER);
        check("hasRole(CUSTOMER) true", s1.hasRole(UserRole.CUSTOMER));
        check("hasRole(ADMIN) false", !s1.hasRole(UserRole.ADMIN));
        check("loginTime duoc ghi", s1.getLoginTime() != null);

        System.out.println("> session.logout()");
        s1.logout();
        check("khong con dang nhap sau logout", !s1.isLoggedIn());
        check("currentUser null sau logout", s1.getCurrentUser() == null);
        check("currentRole null sau logout", s1.getCurrentRole() == null);
    }

    // =====================================================
    // util - PasswordHasher (ho tro Prompt 2 & 3)
    // =====================================================
    private static void testUtil_PasswordHasher() {
        banner("UTIL - PasswordHasher (SHA-256)");

        String h1 = PasswordHasher.hash("secret");
        System.out.println("> PasswordHasher.hash(\"secret\") => " + h1);
        check("hash dai 64 ky tu", h1.length() == 64);
        check("hash tat dinh", h1.equals(PasswordHasher.hash("secret")));
        check("input khac -> hash khac", !h1.equals(PasswordHasher.hash("other")));
        check("matches() dung mat khau", PasswordHasher.matches("secret", h1));
        check("matches() sai mat khau", !PasswordHasher.matches("wrong", h1));
        check("matches() hash null", !PasswordHasher.matches("secret", null));
    }

    // =====================================================
    // Helpers
    // =====================================================
    private static void banner(String title) {
        System.out.println();
        System.out.println("==================================================================");
        System.out.println(" " + title);
        System.out.println("==================================================================");
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + name);
        } else {
            failed++;
            System.out.println("  [FAIL] " + name);
        }
    }

    private static void printFile(String path) {
        try {
            Path p = Paths.get(path);
            if (!Files.exists(p)) {
                System.out.println("  (file khong ton tai: " + path + ")");
                return;
            }
            List<String> lines = Files.readAllLines(p);
            if (lines.isEmpty()) {
                System.out.println("  (file rong)");
            }
            for (String line : lines) {
                System.out.println("  | " + line);
            }
        } catch (Exception e) {
            System.out.println("  (loi doc file: " + e.getMessage() + ")");
        }
    }

    private static void deleteIfExists(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (Exception ignored) {
        }
    }
}
