package controller;

import java.time.LocalDateTime;

import model.Entity.Payment;
import model.Enum.PaymentMethod;
import repository.PaymentRepository;
import view.PaymentView;

/**
 * Controller layer for payments.
 * Coordinates the PaymentView (input/output) and PaymentRepository (storage).
 * Performs no direct file handling and holds no persistence logic itself.
 */
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentView paymentView;

    public PaymentController() {
        this(new PaymentRepository(), new PaymentView());
    }

    public PaymentController(PaymentRepository paymentRepository, PaymentView paymentView) {
        this.paymentRepository = paymentRepository;
        this.paymentView = paymentView;
    }

    /**
     * Creates a payment, letting the customer choose the payment method
     * through the view, then persists it via the repository.
     */
    public Payment createPayment(String orderId, String customerId, double amount) {
        PaymentMethod method = paymentView.choosePaymentMethod();
        return createPayment(orderId, customerId, amount, method);
    }

    /**
     * Creates a payment with an explicit method and persists it.
     */
    public Payment createPayment(String orderId, String customerId, double amount, PaymentMethod method) {
        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment(
                paymentRepository.generateNextId(),
                now,
                now,
                orderId,
                customerId,
                method,
                amount
        );

        paymentRepository.save(payment);
        paymentView.showPaymentSuccess(payment);
        return payment;
    }

    public Payment getPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId);
    }
}
