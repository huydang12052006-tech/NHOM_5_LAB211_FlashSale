package controller;

import java.time.LocalDateTime;
import java.util.List;

import model.Entity.User;
import model.Enum.UserRole;
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

    public void register() {
        String username = authView.inputUsername();
        String password = authView.inputPassword();

        if (findByUsername(username) != null) {
            authView.showUsernameExists();
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User(
                generateUserId(),
                now,
                now,
                username,
                password,
                UserRole.CUSTOMER,
                true
        );

        userRepository.save(user);
        authView.showRegisterSuccess();
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
        currentUser.setPasswordHash(newPassword);
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
        return storedPassword != null && storedPassword.equals(rawPassword);
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
}
