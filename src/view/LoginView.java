package view;

import java.util.Scanner;

import model.Enum.UserRole;

/**
 * Console view for authentication flows.
 * View layer only: renders ASCII menus and collects/validates raw input.
 * Contains no business logic and performs no persistence.
 */
public class LoginView {

    private final Scanner scanner;

    public LoginView() {
        this.scanner = new Scanner(System.in);
    }

    public LoginView(Scanner scanner) {
        this.scanner = scanner;
    }

    // ==================================
    // Menus
    // ==================================

    public String showAuthMenu() {
        System.out.println();
        System.out.println("+==============================+");
        System.out.println("|        FLASH SALE LOGIN      |");
        System.out.println("+==============================+");
        System.out.println("| 1. Login                     |");
        System.out.println("| 2. Register                  |");
        System.out.println("| 3. Logout                    |");
        System.out.println("| 0. Exit                      |");
        System.out.println("+==============================+");
        System.out.print("Choose: ");
        return scanner.nextLine().trim();
    }

    public UserRole showRoleNavigation() {
        System.out.println();
        System.out.println("+==============================+");
        System.out.println("|        SELECT ROLE           |");
        System.out.println("+==============================+");
        System.out.println("| 1. Customer                  |");
        System.out.println("| 2. Seller                    |");
        System.out.println("| 3. Admin                     |");
        System.out.println("| 0. Back                      |");
        System.out.println("+==============================+");
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                return UserRole.CUSTOMER;
            case "2":
                return UserRole.SELLER;
            case "3":
                return UserRole.ADMIN;
            default:
                return null;
        }
    }

    // ==================================
    // Login form
    // ==================================

    public String inputUsername() {
        String username;
        do {
            System.out.print("Username: ");
            username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("[INVALID] Username cannot be empty.");
            }
        } while (username.isEmpty());
        return username;
    }

    public String inputPassword() {
        String password;
        do {
            System.out.print("Password: ");
            password = scanner.nextLine().trim();
            if (password.isEmpty()) {
                System.out.println("[INVALID] Password cannot be empty.");
            }
        } while (password.isEmpty());
        return password;
    }

    public void showLoginForm() {
        System.out.println();
        System.out.println("----- LOGIN -----");
    }

    public void showRegisterForm() {
        System.out.println();
        System.out.println("----- REGISTER -----");
    }

    // ==================================
    // Result messages
    // ==================================

    public void showLoginSuccess(String username) {
        System.out.println("[SUCCESS] Welcome, " + username + "!");
    }

    public void showLoginFailed() {
        System.out.println("[FAILED] Invalid username or password.");
    }

    public void showRegisterSuccess() {
        System.out.println("[SUCCESS] Account registered successfully.");
    }

    public void showRegisterFailed(String reason) {
        System.out.println("[FAILED] " + reason);
    }

    public void showLogoutSuccess() {
        System.out.println("[SUCCESS] You have been logged out.");
    }

    public void showLogoutMenu() {
        System.out.println();
        System.out.println("----- LOGOUT -----");
        System.out.print("Are you sure you want to log out? (y/n): ");
    }

    public boolean confirmLogout() {
        showLogoutMenu();
        String choice = scanner.nextLine().trim().toLowerCase();
        return "y".equals(choice) || "yes".equals(choice);
    }

    public void showMessage(String message) {
        System.out.println(message);
    }
}
