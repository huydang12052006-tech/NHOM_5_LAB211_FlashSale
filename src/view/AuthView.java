package view;

import java.util.Scanner;
import model.Enum.UserRole;

public class AuthView {

    private final Scanner scanner;

    public AuthView() {
        this.scanner = new Scanner(System.in);
    }

    public String inputUsername() {
        System.out.print("Username: ");
        return scanner.nextLine().trim();
    }

    public String inputPassword() {
        System.out.print("Password: ");
        return scanner.nextLine().trim();
    }

    public String inputNewPassword() {
        System.out.print("New password: ");
        return scanner.nextLine().trim();
    }

    public UserRole inputUserRole() {
        System.out.println("Select role:");
        System.out.println("1. Customer");
        System.out.println("2. Seller");
        System.out.println("3. Admin");
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();

        if ("2".equals(choice)) return UserRole.SELLER;
        if ("3".equals(choice)) return UserRole.ADMIN;
        return UserRole.CUSTOMER;
    }

    public void showLoginSuccess() {
        System.out.println("[SUCCESS] Login successful.");
    }

    public void showLoginFailed() {
        System.out.println("[FAILED] Invalid username or password.");
    }

    public void showRegisterSuccess() {
        System.out.println("[SUCCESS] User registered successfully.");
    }

    public void showUsernameExists() {
        System.out.println("[FAILED] Username already exists.");
    }

    public void showLogoutSuccess() {
        System.out.println("[SUCCESS] Logout successful.");
    }

    public void showChangePasswordSuccess() {
        System.out.println("[SUCCESS] Password changed successfully.");
    }

    public void showChangePasswordFailed() {
        System.out.println("[FAILED] Unable to change password.");
    }
}
