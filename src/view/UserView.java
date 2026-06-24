package view;

import java.util.List;
import java.util.Scanner;

import model.Entity.User;
import model.Enum.UserRole;

public class UserView {

    private final Scanner scanner;

    public UserView() {
        this.scanner = new Scanner(System.in);
    }

    // ==================================
    // Input Methods
    // ==================================

    public String inputUserId() {
        System.out.print("User ID (or blank for all): ");
        return scanner.nextLine().trim();
    }

    public String inputUserIdRequired() {
        System.out.print("User ID: ");
        return scanner.nextLine().trim();
    }

    public String inputNewUsername() {
        System.out.print("New username: ");
        return scanner.nextLine().trim();
    }

    public UserRole inputUserRole() {
        System.out.println("Role:");
        System.out.println("1. CUSTOMER");
        System.out.println("2. SELLER");
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();

        if ("2".equals(choice)) {
            return UserRole.SELLER;
        }
        return UserRole.CUSTOMER;
    }

    // ==================================
    // Display Methods
    // ==================================

    public void displayUser(User user) {
        System.out.println(formatUser(user));
    }

    public void displayUserList(List<User> users) {
        System.out.println("===== ACCOUNTS =====");

        if (users == null || users.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }

        for (User user : users) {
            System.out.println(formatUser(user));
        }
    }

    // ==================================
    // Result Messages
    // ==================================

    public void showUserNotFound() {
        System.out.println("[FAILED] User not found.");
    }

    public void showAccountStatusResult(boolean success) {
        System.out.println(success
                ? "[SUCCESS] Account status updated."
                : "[FAILED] Unable to update account status.");
    }

    public void showAccountUpdateResult(boolean success) {
        System.out.println(success
                ? "[SUCCESS] Account updated."
                : "[FAILED] Unable to update account.");
    }

    // ==================================
    // Format
    // ==================================

    private String formatUser(User user) {
        return user.getId()
                + " | username=" + user.getUsername()
                + " | role=" + user.getRole()
                + " | active=" + user.isActive();
    }
}
