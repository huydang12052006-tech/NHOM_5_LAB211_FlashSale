package controller;

import exception.InvalidQuantityException;
import exception.OutOfStockException;
import exception.PurchaseLimitExceededException;
import exception.FlashSaleException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.Entity.Customer;
import model.Entity.FlashSaleItem;
import model.Entity.Order;
import model.Entity.Payment;
import model.Enum.LockMechanism;
import model.Enum.OrderStatus;
import model.Enum.PaymentMethod;

import repository.CustomerRepository;
import repository.FlashSaleItemRepository;
import repository.OrderRepository;
import repository.PaymentRepository;

public class OrderController {

    private final FlashSaleItemRepository flashSaleItemRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public OrderController() {
        this.flashSaleItemRepository = new FlashSaleItemRepository();
        this.customerRepository = new CustomerRepository();
        this.orderRepository = new OrderRepository();
        this.paymentRepository = new PaymentRepository();
    }

    /*
        Main order placement flow
     */
    public boolean placeOrder(String customerId,
                              String flashItemId,
                              int quantity,
                              LockMechanism mechanism)
            throws IOException, FlashSaleException {

        // =========================
        // VALIDATE QUANTITY
        // =========================
        if (quantity <= 0) {
            throw new InvalidQuantityException(
                    "Quantity must be greater than 0"
            );
        }

        // =========================
        // VALIDATE CUSTOMER
        // =========================
        Customer customer = customerRepository.findById(customerId);
        if (customer == null) {
            throw new FlashSaleException(
                    "Customer not found"
            );
        }

        // =========================
        // VALIDATE FLASH ITEM
        // =========================
        FlashSaleItem flashItem = flashSaleItemRepository.findById(flashItemId);
        if (flashItem == null) {
            throw new FlashSaleException(
                    "FlashSaleItem not found"
            );
        }

        // =========================
        // VALIDATE PURCHASE LIMIT
        // mỗi customer tối đa 2 item/event
        // =========================
        int purchasedQty = orderRepository.getPurchasedQuantity(customerId, flashItemId);
        if (purchasedQty + quantity > 2) {
            throw new PurchaseLimitExceededException(
                    "Maximum 2 items per customer/event"
            );
        }

        // =========================
        // VALIDATE STOCK
        // =========================
        int remainingStock = flashItem.getLimitedQty() - flashItem.getSoldQty();
        if (remainingStock < quantity) {
            throw new OutOfStockException(
                    "Not enough stock"
            );
        }

        // =========================
        // CHOOSE LOCK MECHANISM
        // =========================
        boolean result;
        switch (mechanism) {
            case NO_LOCK:
                result = flashSaleItemRepository.sellWithNoLock(flashItemId, quantity);
                break;
            case SYNCHRONIZED:
                result = flashSaleItemRepository.sellWithSynchronized(flashItemId, quantity);
                break;
            case FILE_LOCK:
                result = flashSaleItemRepository.sellWithFileLock(flashItemId, quantity);
                break;
            case OPTIMISTIC_LOCK:
                result = flashSaleItemRepository.sellWithOptimisticLock(flashItemId, quantity);
                break;
            default:
                throw new FlashSaleException("Unknown lock mechanism");
        }

        return result;
    }

    public int getPurchasedQuantity(String customerId, String flashItemId) {
        return orderRepository.getPurchasedQuantity(customerId, flashItemId);
    }

    public List<Order> getOrdersByCustomer(String customerId) {
        List<Order> result = new ArrayList<Order>();
        for (Order order : orderRepository.findAll()) {
            if (customerId.equalsIgnoreCase(order.getCustomerId())) {
                result.add(order);
            }
        }
        return result;
    }

    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId);
    }

    public boolean confirmOrder(String orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            return false;
        }
        order.setStatus(OrderStatus.SUCCESS);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.update(order);
    }

    public boolean createPayment(String orderId, PaymentMethod method) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment(
                generatePaymentId(),
                now,
                now,
                order.getId(),
                order.getCustomerId(),
                method,
                order.getTotalAmount()
        );

        paymentRepository.save(payment);
        return true;
    }

    public String generatePaymentId() {
        return paymentRepository.generateNextId();
    }
}

