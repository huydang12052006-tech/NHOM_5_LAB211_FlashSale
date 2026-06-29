package controller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

import model.Entity.Customer;
import model.Entity.User;
import model.Enum.CustomerTier;
import model.Enum.UserRole;
import repository.CustomerRepository;
import repository.UserRepository;
import view.AuthView;

public class AuthController {

    private final UserRepository userRepository;
    private final AuthView authView;
    private User currentUser;

    public AuthController() {
        this(new UserRepository(), new AuthView());
    }

    public AuthController(UserRepository userRepository, AuthView authView) {
        this.userRepository = userRepository;
        this.authView = authView;
    }

    public boolean login() {
        String username = authView.inputUsername();
        String password = authView.inputPassword();

        User user = findByUsername(username);

        if (user == null
                || !user.isActive()
                || !passwordMatches(password, user.getPasswordHash())) {
            authView.showLoginFailed();
            return false;
        }

        currentUser = user;
        authView.showLoginSuccess();
        return true;
    }

    public User loginAs(UserRole expectedRole) {
        User user = validateLogin();
        if (user == null) {
            return null;
        }
        if (user.getRole() != expectedRole) {
            authView.showLoginFailed();
            currentUser = null;
            return null;
        }
        authView.showLoginSuccess();
        return user;
    }

    public User validateLogin() {
        String username = authView.inputUsername();
        String password = authView.inputPassword();

        User user = findByUsername(username);

        if (user == null
                || !user.isActive()
                || !passwordMatches(password, user.getPasswordHash())) {
            authView.showLoginFailed();
            return null;
        }

        currentUser = user;
        return user;
    }


    public void register() {
        register(authView.inputUserRole());
    }

    public void register(UserRole role) {
        if (role == UserRole.ADMIN) {
            throw new IllegalArgumentException("Admin accounts cannot be registered.");
        }
        String username = authView.inputUsername();
        String password = authView.inputPassword();

        if (findByUsername(username) != null) {
            authView.showUsernameExists();
            return;
        }

        String fullName = null;
        String phone = null;
        String email = null;
        if (role == UserRole.CUSTOMER) {
            fullName = authView.inputFullName();
            phone = authView.inputPhone();
            email = authView.inputEmail();
            if (!isValidCustomerRegistration(fullName, phone, email)) {
                return;
            }
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User(
                generateUserId(),
                now,
                now,
                username,
                sha256Hash(password),
                role,
                true
        );

        userRepository.save(user);
        currentUser = user;

        // Auto create Customer record for CUSTOMER role only
        if (role == UserRole.CUSTOMER) {
            CustomerRepository customerRepository = new CustomerRepository();
            String customerId = generateCustomerId(customerRepository);
            Customer customer = new Customer(
                    customerId,
                    now,
                    now,
                    user.getId(),
                    fullName,
                    phone,
                    email,
                    "",
                    CustomerTier.NORMAL,
                    0.0,
                    true
            );
            customerRepository.save(customer);
        }

        authView.showRegisterSuccess();
    }

    private boolean isValidCustomerRegistration(String fullName, String phone, String email) {
        if (fullName == null || fullName.trim().isEmpty() || fullName.contains(",")) {
            authView.showRegisterValidationError("Full name is required and cannot contain a comma.");
            return false;
        }
        if (phone == null || !phone.matches("0\\d{9,10}")) {
            authView.showRegisterValidationError("Phone must start with 0 and contain 10-11 digits.");
            return false;
        }
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            authView.showRegisterValidationError("Email format is invalid.");
            return false;
        }
        return true;
    }

    private String generateCustomerId(CustomerRepository repo) {
        int maxNumber = 0;
        for (Customer customer : repo.findAll()) {
            String id = customer.getId();
            if (id != null && id.matches("C\\d+")) {
                int number = Integer.parseInt(id.substring(1));
                maxNumber = Math.max(maxNumber, number);
            }
        }
        return String.format("C%05d", maxNumber + 1);
    }

    public void logout() {
        currentUser = null;
        authView.showLogoutSuccess();
    }

    public void changePassword() {
        if (currentUser == null && !login()) {
            authView.showChangePasswordFailed();
            return;
        }

        String newPassword = authView.inputNewPassword();
        currentUser.setPasswordHash(sha256Hash(newPassword));
        currentUser.setUpdatedAt(LocalDateTime.now());

        if (userRepository.update(currentUser)) {
            authView.showChangePasswordSuccess();
        } else {
            authView.showChangePasswordFailed();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    private User findByUsername(String username) {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }

        return null;
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        return storedPassword != null && storedPassword.equals(sha256Hash(rawPassword));
    }

    private static String sha256Hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String generateUserId() {
        int maxNumber = 0;

        for (User user : userRepository.findAll()) {
            String id = user.getId();

            if (id != null && id.matches("U\\d+")) {
                int number = Integer.parseInt(id.substring(1));
                maxNumber = Math.max(maxNumber, number);
            }
        }

        return String.format("U%05d", maxNumber + 1);
    }

    public boolean approveAccount(String userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return false;
        }
        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.update(user);
    }

    public boolean suspendAccount(String userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return false;
        }
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.update(user);
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean updateUserAccount(String userId, String username, UserRole role) {
        User user = userRepository.findById(userId);
        if (user == null || role == UserRole.ADMIN) {
            return false;
        }
        user.setUsername(username);
        if (user.getRole() != UserRole.ADMIN) {
            user.setRole(role);
        }
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.update(user);
    }
}
