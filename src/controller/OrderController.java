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
import model.Entity.OrderDetail;
import model.Entity.Payment;
import model.Enum.LockMechanism;
import model.Enum.OrderStatus;
import model.Enum.PaymentMethod;

import repository.CustomerRepository;
import repository.FlashSaleItemRepository;
import repository.OrderRepository;
import repository.OrderDetailRepository;
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
    public String placeOrder(String customerId,
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

        if (result) {
            String orderId = orderRepository.generateNextId();
            LocalDateTime now = LocalDateTime.now();
            double flashPrice = flashItem.getFlashPrice();
            double totalAmount = flashPrice * quantity;

            // Save Order
            Order order = new Order(
                    orderId,
                    now,
                    now,
                    customerId,
                    flashItem.getEventId(),
                    totalAmount,
                    OrderStatus.PENDING,
                    mechanism
            );
            orderRepository.save(order);

            // Save OrderDetail
            OrderDetailRepository detailRepo = new OrderDetailRepository();
            String detailId = detailRepo.generateNextId();
            OrderDetail detail = new OrderDetail(
                    detailId,
                    now,
                    now,
                    orderId,
                    flashItemId,
                    quantity,
                    flashPrice,
                    totalAmount
            );
            detailRepo.save(detail);

            return orderId;
        }

        return null;
    }

    public Customer getCustomerByUserId(String userId) {
        for (Customer c : customerRepository.findAll()) {
            if (c.getUserId() != null && c.getUserId().equalsIgnoreCase(userId)) {
                return c;
            }
        }
        // Fallback: auto-create if missing
        int maxNumber = 0;
        for (Customer customer : customerRepository.findAll()) {
            String id = customer.getId();
            if (id != null && id.matches("C\\d+")) {
                maxNumber = Math.max(maxNumber, Integer.parseInt(id.substring(1)));
            }
        }
        String newCustId = String.format("C%05d", maxNumber + 1);
        LocalDateTime now = LocalDateTime.now();
        Customer c = new Customer(
                newCustId,
                now,
                now,
                userId,
                "User_" + userId,
                "0000000000",
                "user_" + userId + "@gmail.com",
                model.Enum.CustomerTier.NORMAL,
                0.0,
                true
        );
        customerRepository.save(c);
        return c;
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

