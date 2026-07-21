package view;

import java.util.Scanner;

import model.Entity.Payment;
import model.Enum.PaymentMethod;

/**
 * Console view for payments.
 * View layer only: renders the payment-method menu and payment results.
 * Contains no business logic and performs no persistence.
 */
public class PaymentView {

    private final Scanner scanner;

    public PaymentView() {
        this.scanner = new Scanner(System.in);
    }

    public PaymentView(Scanner scanner) {
        this.scanner = scanner;
    }

    public PaymentMethod choosePaymentMethod() {
        while (true) {
            System.out.println();
            System.out.println("+==============================+");
            System.out.println("|       PAYMENT METHOD         |");
            System.out.println("+==============================+");
            System.out.println("| 1. CASH                      |");
            System.out.println("| 2. BANKING                   |");
            System.out.println("+==============================+");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();
            if ("1".equals(choice)) {
                return PaymentMethod.CASH;
            }
            if ("2".equals(choice)) {
                return PaymentMethod.BANKING;
            }
            System.out.println("[INVALID] Please choose 1 (CASH) or 2 (BANKING).");
        }
    }

    public void showPaymentSuccess(Payment payment) {
        System.out.println();
        System.out.println("+==============================+");
        System.out.println("|       PAYMENT SUCCESS        |");
        System.out.println("+==============================+");
        if (payment != null) {
            System.out.println("Payment ID : " + payment.getId());
            System.out.println("Order ID   : " + payment.getOrderId());
            System.out.println("Method     : " + payment.getPaymentMethod());
            System.out.printf("Amount     : %.0f VND%n", payment.getAmount());
        }
        System.out.println("Status     : PAID");
        System.out.println("+==============================+");
    }

    public void showPaymentFailed(String reason) {
        System.out.println("[FAILED] " + reason);
    }
}
