package controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import exception.FlashSaleException;
import model.Entity.CartItem;
import model.Entity.Customer;
import model.Entity.FlashSaleEvent;
import model.Entity.FlashSaleItem;
import model.Entity.Product;
import model.Entity.User;
import model.Enum.LockMechanism;
import model.Enum.PaymentMethod;
import model.Enum.SaleStatus;
import repository.CartRepository;
import view.FlashSaleView;
import view.OrderView;
import view.ProductView;

public class CartController {

    private final CartRepository cartRepository;
    private final Scanner scanner;
    private final OrderController orderController;
    private final ProductController productController;
    private final FlashSaleController flashSaleController;
    private final ProductView productView;
    private final OrderView orderView;
    private final FlashSaleView flashSaleView;

    public CartController() {
        this(new CartRepository());
    }

    public CartController(CartRepository cartRepository) {
        this(cartRepository, null, null, null, null, null, null, null);
    }

    public CartController(CartRepository cartRepository,
                          Scanner scanner,
                          OrderController orderController,
                          ProductController productController,
                          FlashSaleController flashSaleController,
                          ProductView productView,
                          OrderView orderView,
                          FlashSaleView flashSaleView) {
        this.cartRepository = cartRepository;
        this.scanner = scanner;
        this.orderController = orderController;
        this.productController = productController;
        this.flashSaleController = flashSaleController;
        this.productView = productView;
        this.orderView = orderView;
        this.flashSaleView = flashSaleView;
    }

    public List<CartItem> getCartByCustomer(String customerId) {
        return cartRepository.findByCustomerId(customerId);
    }

    public void addItem(String customerId, String flashItemId, String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        cartRepository.addOrMergeItem(customerId, flashItemId, productId, quantity);
    }

    public boolean removeItem(String customerId, String cartItemId) {
        return cartRepository.removeCustomerItem(customerId, cartItemId);
    }

    public void clearCart(String customerId) {
        cartRepository.clearByCustomerId(customerId);
    }

    public void addFlashSaleItemsToCart(User currentUser, String selectedEventId, LockMechanism mechanism) {
        requireCartFlowDependencies();
        try {
            Customer customer = orderController.getCustomerByUserId(currentUser.getId());
            List<FlashSaleEvent> activeEvents = new java.util.ArrayList<FlashSaleEvent>();
            for (FlashSaleEvent event : flashSaleController.getAllEvents()) {
                if (event.getStatus() == SaleStatus.ACTIVE) {
                    activeEvents.add(event);
                }
            }
            if (activeEvents.isEmpty()) {
                System.out.println("No active flash-sale event is available.");
                return;
            }

            String eventId = selectedEventId;
            if (eventId == null) {
                flashSaleView.displayActiveEvents(activeEvents);
                eventId = flashSaleView.inputEventId();
            }
            FlashSaleEvent selectedEvent = flashSaleController.getEventById(eventId);
            if (selectedEvent == null || selectedEvent.getStatus() != SaleStatus.ACTIVE) {
                flashSaleView.showEventNotFound();
                return;
            }

            List<FlashSaleItem> eventItems = flashSaleController.getActiveFlashItemsByEventId(eventId);
            flashSaleView.displayFlashSaleItemsInTable(eventItems, productController);
            Map<String, Integer> selectedItems = new LinkedHashMap<String, Integer>();
            boolean adding = true;
            while (adding) {
                String flashItemId = flashSaleView.inputFlashItemId();
                FlashSaleItem item = flashSaleController.getFlashItemById(flashItemId);
                if (item == null || !eventId.equals(item.getEventId())
                        || !containsFlashItem(eventItems, flashItemId)) {
                    System.out.println("[FAILED] Item is not in the selected event.");
                } else {
                    int quantity = orderView.inputQuantity();
                    selectedItems.put(flashItemId, selectedItems.getOrDefault(flashItemId, 0) + quantity);
                }
                System.out.print("Add another product to this order? (y/n): ");
                String choice = scanner.nextLine().trim().toLowerCase();
                adding = "y".equals(choice) || "yes".equals(choice);
            }
            if (selectedItems.isEmpty()) {
                System.out.println("No item was selected.");
                return;
            }
            for (Map.Entry<String, Integer> entry : selectedItems.entrySet()) {
                FlashSaleItem item = flashSaleController.getFlashItemById(entry.getKey());
                addItem(customer.getId(), item.getId(), item.getProductId(), entry.getValue());
            }
            chooseCartAction(customer.getId(), mechanism);
        } catch (NumberFormatException e) {
            orderView.showPlaceOrderError("Quantity, discount, and limit must be numbers.");
        }
    }

    public void addRegularProductsToCart(User currentUser, LockMechanism mechanism) {
        requireCartFlowDependencies();
        List<Product> activeProducts = new java.util.ArrayList<Product>();
        for (Product product : productController.getAllProducts()) {
            if (product.getStatus() == SaleStatus.ACTIVE) {
                activeProducts.add(product);
            }
        }
        addRegularProductsToCart(currentUser, activeProducts, mechanism);
    }

    public void addRegularProductsToCart(User currentUser, List<Product> selectableProducts,
                                         LockMechanism mechanism) {
        requireCartFlowDependencies();
        try {
            Customer customer = orderController.getCustomerByUserId(currentUser.getId());
            Map<String, Integer> selectedProducts = new LinkedHashMap<String, Integer>();
            boolean adding = true;
            while (adding) {
                productView.displayProducts(selectableProducts);
                String productId = orderView.inputProductId();
                Product product = productController.getProductById(productId);
                if (product == null || product.getStatus() != SaleStatus.ACTIVE
                        || !containsProduct(selectableProducts, productId)) {
                    System.out.println("[FAILED] Product not found or not active.");
                } else {
                    int quantity = orderView.inputQuantity();
                    selectedProducts.put(productId, selectedProducts.getOrDefault(productId, 0) + quantity);
                }
                System.out.print("Add another regular product? (y/n): ");
                String choice = scanner.nextLine().trim().toLowerCase();
                adding = "y".equals(choice) || "yes".equals(choice);
            }
            if (selectedProducts.isEmpty()) {
                return;
            }
            for (Map.Entry<String, Integer> entry : selectedProducts.entrySet()) {
                addItem(customer.getId(), null, entry.getKey(), entry.getValue());
            }
            chooseCartAction(customer.getId(), mechanism);
        } catch (NumberFormatException e) {
            orderView.showPlaceOrderError("Quantity must be a number.");
        }
    }

    public void showCart(User currentUser, LockMechanism mechanism) {
        requireCartFlowDependencies();
        Customer customer = orderController.getCustomerByUserId(currentUser.getId());
        List<CartItem> items = getCartByCustomer(customer.getId());
        System.out.println("\n===== MY CART =====");
        if (items.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }

        double total = displayCartItems(items);
        System.out.printf("Cart total: %.0f%n", total);
        System.out.println("1. Checkout");
        System.out.println("2. Remove an item");
        System.out.println("0. Back");
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();
        if ("1".equals(choice)) {
            checkoutCart(customer.getId(), mechanism, items, false);
        } else if ("2".equals(choice)) {
            System.out.print("Cart item ID: ");
            String cartItemId = scanner.nextLine().trim();
            System.out.println(removeItem(customer.getId(), cartItemId)
                    ? "[SUCCESS] Item removed from cart." : "[FAILED] Cart item not found.");
        }
    }

    public void checkoutCart(String customerId, LockMechanism mechanism) {
        requireCartFlowDependencies();
        checkoutCart(customerId, mechanism, getCartByCustomer(customerId), true);
    }

    private void checkoutCart(String customerId, LockMechanism mechanism,
                              List<CartItem> cartItems, boolean showItems) {
        try {

            if (showItems) {
                System.out.println("\n===== CHECKOUT CART =====");
                double total = displayCartItems(cartItems);
                System.out.printf("Cart total: %.0f%n", total);
            }
            List<CartItem> selectedItems = inputCheckoutItems(cartItems);
            if (selectedItems.isEmpty()) {
                System.out.println("[INFO] No cart item was selected for checkout.");
                return;
            }

            List<String> orderIds = orderController.checkoutCart(customerId, selectedItems, mechanism);
            PaymentMethod method = orderView.inputPaymentMethod();
            boolean paymentsSaved = true;
            for (String orderId : orderIds) {
                paymentsSaved = orderController.createPayment(orderId, method) && paymentsSaved;
            }
            if (paymentsSaved) {
                for (CartItem item : selectedItems) {
                    removeItem(customerId, item.getId());
                }
                System.out.println("[SUCCESS] Checkout completed. Pending order(s): " + orderIds);
            } else {
                System.out.println("[FAILED] Payment could not be saved. Cart was kept.");
            }
        } catch (IOException e) {
            orderView.showPlaceOrderError("IO error: " + e.getMessage());
        } catch (FlashSaleException e) {
            orderView.showPlaceOrderError(e.getMessage());
        }
    }

    private double displayCartItems(List<CartItem> items) {
        double total = 0.0;
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            Product product = item.getProductId() == null ? null : productController.getProductById(item.getProductId());
            String name = product == null ? "Unknown product" : product.getName();
            double price = 0.0;
            if (item.getFlashItemId() != null) {
                FlashSaleItem flashItem = flashSaleController.getFlashItemById(item.getFlashItemId());
                price = flashItem == null ? 0.0 : flashItem.getFlashPrice();
            } else if (product != null) {
                price = product.getOriginalPrice();
            }
            total += price * item.getQuantity();
            System.out.printf("%d. %s | %s | qty=%d | unit=%.0f | subtotal=%.0f%n",
                    i + 1, item.getId(), name, item.getQuantity(), price, price * item.getQuantity());
        }
        return total;
    }

    private List<CartItem> inputCheckoutItems(List<CartItem> items) {
        System.out.print("Cart item numbers to checkout (blank = all): ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return new java.util.ArrayList<CartItem>(items);
        }

        Map<Integer, CartItem> selectedItems = new LinkedHashMap<Integer, CartItem>();
        String[] tokens = input.split("[,\\s]+");
        for (String token : tokens) {
            try {
                int index = Integer.parseInt(token);
                if (index < 1 || index > items.size()) {
                    System.out.println("[FAILED] Invalid cart item number: " + token);
                    return new java.util.ArrayList<CartItem>();
                }
                selectedItems.put(index, items.get(index - 1));
            } catch (NumberFormatException e) {
                System.out.println("[FAILED] Cart item numbers must be numeric.");
                return new java.util.ArrayList<CartItem>();
            }
        }
        return new java.util.ArrayList<CartItem>(selectedItems.values());
    }

    private void chooseCartAction(String customerId, LockMechanism mechanism) {
        System.out.println("1. Add selected products to cart");
        System.out.println("2. Pay now");
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();
        if ("1".equals(choice)) {
            System.out.println("[SUCCESS] Products were saved in your cart.");
        } else if ("2".equals(choice)) {
            checkoutCart(customerId, mechanism);
        } else {
            System.out.println("[INFO] Products were saved in your cart. You can checkout later.");
        }
    }

    private boolean containsProduct(List<Product> products, String productId) {
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsFlashItem(List<FlashSaleItem> items, String flashItemId) {
        for (FlashSaleItem item : items) {
            if (item.getId().equals(flashItemId)) {
                return true;
            }
        }
        return false;
    }

    private void requireCartFlowDependencies() {
        if (scanner == null || orderController == null || productController == null
                || flashSaleController == null || productView == null || orderView == null
                || flashSaleView == null) {
            throw new IllegalStateException("Cart flow dependencies are not configured.");
        }
    }
}
