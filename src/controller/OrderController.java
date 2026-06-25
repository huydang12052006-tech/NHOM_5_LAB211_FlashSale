package controller;

import exception.InvalidQuantityException;
import exception.OutOfStockException;
import exception.PurchaseLimitExceededException;
import exception.FlashSaleException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.Entity.Customer;
import model.Entity.CartItem;
import model.Entity.FlashSaleItem;
import model.Entity.Order;
import model.Entity.OrderDetail;
import model.Entity.Payment;
import model.Entity.Product;
import model.Enum.LockMechanism;
import model.Enum.OrderStatus;
import model.Enum.PaymentMethod;

import repository.CustomerRepository;
import repository.FlashSaleItemRepository;
import repository.OrderRepository;
import repository.OrderDetailRepository;
import repository.PaymentRepository;
import repository.ProductRepository;

public class OrderController {

    private final FlashSaleItemRepository flashSaleItemRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;

    public OrderController() {
        this.flashSaleItemRepository = new FlashSaleItemRepository();
        this.customerRepository = new CustomerRepository();
        this.orderRepository = new OrderRepository();
        this.orderDetailRepository = new OrderDetailRepository();
        this.paymentRepository = new PaymentRepository();
        this.productRepository = new ProductRepository();
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
                    flashItem.getProductId(),
                    quantity,
                    flashPrice,
                    totalAmount
            );
            detailRepo.save(detail);

            return orderId;
        }

        return null;
    }

    public String placeRegularOrder(String customerId, String productId, int quantity,
                                    LockMechanism mechanism) throws FlashSaleException {
        Map<String, Integer> products = new LinkedHashMap<String, Integer>();
        products.put(productId, quantity);
        return placeRegularCartOrder(customerId, products, mechanism);
    }

    public String placeRegularCartOrder(String customerId, Map<String, Integer> products,
                                        LockMechanism mechanism) throws FlashSaleException {
        if (products == null || products.isEmpty()) {
            throw new FlashSaleException("Cart is empty");
        }
        if (customerRepository.findById(customerId) == null) {
            throw new FlashSaleException("Customer not found");
        }

        Map<String, Product> cartProducts = new LinkedHashMap<String, Product>();
        double totalAmount = 0.0;
        for (Map.Entry<String, Integer> entry : products.entrySet()) {
            int quantity = entry.getValue() == null ? 0 : entry.getValue();
            Product product = productRepository.findById(entry.getKey());
            if (quantity <= 0) {
                throw new InvalidQuantityException("Quantity must be greater than 0");
            }
            if (product == null || product.getStatus() != model.Enum.SaleStatus.ACTIVE) {
                throw new FlashSaleException("Product not found or not active");
            }
            if (product.getStockQty() < quantity) {
                throw new OutOfStockException("Not enough stock for " + product.getId());
            }
            cartProducts.put(product.getId(), product);
            totalAmount += product.getOriginalPrice() * quantity;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<String, Integer> entry : products.entrySet()) {
            Product product = cartProducts.get(entry.getKey());
            product.setStockQty(product.getStockQty() - entry.getValue());
            product.setUpdatedAt(now);
            if (!productRepository.update(product)) {
                throw new FlashSaleException("Unable to update product stock");
            }
        }

        String orderId = orderRepository.generateNextId();
        orderRepository.save(new Order(orderId, now, now, customerId, null,
                totalAmount, OrderStatus.PENDING, mechanism));
        OrderDetailRepository detailRepository = new OrderDetailRepository();
        for (Map.Entry<String, Integer> entry : products.entrySet()) {
            Product product = cartProducts.get(entry.getKey());
            int quantity = entry.getValue();
            double subTotal = product.getOriginalPrice() * quantity;
            detailRepository.save(new OrderDetail(detailRepository.generateNextId(), now, now,
                    orderId, null, product.getId(), quantity, product.getOriginalPrice(), subTotal));
        }
        return orderId;
    }

    /**
     * Creates one order and one detail per cart entry. The current CSV schema
     * stores a single event on an order, so a cart is intentionally restricted
     * to items from one event.
     */
    public String placeCartOrder(String customerId, Map<String, Integer> cart,
                                 LockMechanism mechanism)
            throws IOException, FlashSaleException {
        if (cart == null || cart.isEmpty()) {
            throw new FlashSaleException("Cart is empty");
        }
        Customer customer = customerRepository.findById(customerId);
        if (customer == null) {
            throw new FlashSaleException("Customer not found");
        }

        Map<String, FlashSaleItem> items = new LinkedHashMap<String, FlashSaleItem>();
        String eventId = null;
        double totalAmount = 0.0;
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            int quantity = entry.getValue() == null ? 0 : entry.getValue();
            FlashSaleItem item = flashSaleItemRepository.findById(entry.getKey());
            if (item == null || quantity <= 0) {
                throw new FlashSaleException("Invalid cart item: " + entry.getKey());
            }
            if (eventId == null) {
                eventId = item.getEventId();
            } else if (!eventId.equals(item.getEventId())) {
                throw new FlashSaleException("Cart items must belong to one flash event");
            }
            if (getPurchasedQuantity(customerId, item.getId()) + quantity > 2) {
                throw new PurchaseLimitExceededException("Maximum 2 items per customer/event");
            }
            if (item.getLimitedQty() - item.getSoldQty() < quantity) {
                throw new OutOfStockException("Not enough stock for " + item.getId());
            }
            items.put(item.getId(), item);
            totalAmount += item.getFlashPrice() * quantity;
        }

        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            boolean sold;
            switch (mechanism) {
                case NO_LOCK:
                    sold = flashSaleItemRepository.sellWithNoLock(entry.getKey(), entry.getValue());
                    break;
                case SYNCHRONIZED:
                    sold = flashSaleItemRepository.sellWithSynchronized(entry.getKey(), entry.getValue());
                    break;
                case FILE_LOCK:
                    sold = flashSaleItemRepository.sellWithFileLock(entry.getKey(), entry.getValue());
                    break;
                case OPTIMISTIC_LOCK:
                    sold = flashSaleItemRepository.sellWithOptimisticLock(entry.getKey(), entry.getValue());
                    break;
                default:
                    throw new FlashSaleException("Unknown lock mechanism");
            }
            if (!sold) {
                throw new FlashSaleException("Unable to reserve cart item: " + entry.getKey());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        String orderId = orderRepository.generateNextId();
        orderRepository.save(new Order(orderId, now, now, customerId, eventId,
                totalAmount, OrderStatus.PENDING, mechanism));

        OrderDetailRepository detailRepository = new OrderDetailRepository();
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            FlashSaleItem item = items.get(entry.getKey());
            double subTotal = item.getFlashPrice() * entry.getValue();
            detailRepository.save(new OrderDetail(detailRepository.generateNextId(), now, now,
                    orderId, item.getId(), item.getProductId(), entry.getValue(), item.getFlashPrice(), subTotal));
        }
        return orderId;
    }

    public List<String> checkoutCart(String customerId, List<CartItem> cartItems,
                                     LockMechanism mechanism) throws IOException, FlashSaleException {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new FlashSaleException("Cart is empty");
        }
        Map<String, Integer> regularProducts = new LinkedHashMap<String, Integer>();
        Map<String, Map<String, Integer>> flashItemsByEvent = new LinkedHashMap<String, Map<String, Integer>>();
        for (CartItem cartItem : cartItems) {
            if (cartItem.getFlashItemId() != null) {
                FlashSaleItem item = flashSaleItemRepository.findById(cartItem.getFlashItemId());
                if (item == null) {
                    throw new FlashSaleException("Flash sale item no longer exists: " + cartItem.getFlashItemId());
                }
                Map<String, Integer> eventItems = flashItemsByEvent.get(item.getEventId());
                if (eventItems == null) {
                    eventItems = new LinkedHashMap<String, Integer>();
                    flashItemsByEvent.put(item.getEventId(), eventItems);
                }
                eventItems.put(item.getId(), eventItems.getOrDefault(item.getId(), 0) + cartItem.getQuantity());
            } else if (cartItem.getProductId() != null) {
                regularProducts.put(cartItem.getProductId(),
                        regularProducts.getOrDefault(cartItem.getProductId(), 0) + cartItem.getQuantity());
            } else {
                throw new FlashSaleException("Cart item has no product reference: " + cartItem.getId());
            }
        }

        List<String> orderIds = new ArrayList<String>();
        if (!regularProducts.isEmpty()) {
            orderIds.add(placeRegularCartOrder(customerId, regularProducts, mechanism));
        }
        for (Map<String, Integer> flashItems : flashItemsByEvent.values()) {
            orderIds.add(placeCartOrder(customerId, flashItems, mechanism));
        }
        return orderIds;
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

    public List<OrderDetail> getOrderDetailsByOrderId(String orderId) {
        List<OrderDetail> result = new ArrayList<OrderDetail>();
        for (OrderDetail detail : orderDetailRepository.findAll()) {
            if (orderId != null && orderId.equalsIgnoreCase(detail.getOrderId())) {
                result.add(detail);
            }
        }
        return result;
    }

    public Map<String, String> getProductNamesByOrderDetails(List<OrderDetail> details) {
        Map<String, String> productNames = new LinkedHashMap<String, String>();
        if (details == null) {
            return productNames;
        }
        for (OrderDetail detail : details) {
            Product product = null;
            if (detail.getProductId() != null) {
                product = productRepository.findById(detail.getProductId());
            } else if (detail.getFlashItemId() != null) {
                FlashSaleItem item = flashSaleItemRepository.findById(detail.getFlashItemId());
                product = item == null ? null : productRepository.findById(item.getProductId());
            }
            productNames.put(detail.getId(), product == null ? "Unknown product" : product.getName());
        }
        return productNames;
    }

    public List<Order> getOrdersForSeller(String sellerId) {
        List<Order> result = new ArrayList<Order>();
        ProductRepository productRepository = new ProductRepository();
        OrderDetailRepository detailRepository = new OrderDetailRepository();
        for (Order order : orderRepository.findAll()) {
            for (OrderDetail detail : detailRepository.findAll()) {
                if (!order.getId().equals(detail.getOrderId())) {
                    continue;
                }
                Product product = null;
                if (detail.getProductId() != null) {
                    product = productRepository.findById(detail.getProductId());
                } else if (detail.getFlashItemId() != null) {
                    FlashSaleItem item = flashSaleItemRepository.findById(detail.getFlashItemId());
                    product = item == null ? null : productRepository.findById(item.getProductId());
                }
                if (product != null && sellerId.equalsIgnoreCase(product.getSellerId())) {
                    result.add(order);
                    break;
                }
            }
        }
        return result;
    }

    public List<Order> getPendingOrdersForSeller(String sellerId) {
        List<Order> result = new ArrayList<Order>();
        for (Order order : getOrdersForSeller(sellerId)) {
            if (order.getStatus() == OrderStatus.PENDING) {
                result.add(order);
            }
        }
        return result;
    }

    public boolean confirmOrderForSeller(String orderId, String sellerId) {
        for (Order order : getPendingOrdersForSeller(sellerId)) {
            if (order.getId().equals(orderId)) {
                return confirmOrder(orderId);
            }
        }
        return false;
    }

    public boolean cancelOrderForSeller(String orderId, String sellerId) {
        for (Order order : getPendingOrdersForSeller(sellerId)) {
            if (order.getId().equals(orderId)) {
                order.setStatus(OrderStatus.CANCELLED);
                order.setUpdatedAt(LocalDateTime.now());
                return orderRepository.update(order);
            }
        }
        return false;
    }

    public boolean confirmOrder(String orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null || order.getStatus() != OrderStatus.PENDING) {
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

