package session;

import java.time.LocalDateTime;

import model.Entity.User;
import model.Enum.UserRole;

/**
 * Singleton that holds the currently logged-in user for the console session.
 * No framework, no file handling: pure in-memory session state.
 */
public final class UserSession {

    private static UserSession instance;

    private User currentUser;
    private LocalDateTime loginTime;

    private UserSession() {
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
        this.loginTime = LocalDateTime.now();
    }

    public void logout() {
        this.currentUser = null;
        this.loginTime = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public UserRole getCurrentRole() {
        return currentUser == null ? null : currentUser.getRole();
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean hasRole(UserRole role) {
        return currentUser != null && currentUser.getRole() == role;
    }
}
