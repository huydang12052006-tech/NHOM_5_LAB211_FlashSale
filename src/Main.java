import controller.AuthController;
import controller.CartController;
import controller.FlashSaleController;
import controller.OrderController;
import controller.ProductController;
import controller.SimulatorController;
import exception.FlashSaleException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import model.Entity.FlashSaleEvent;
import model.Entity.FlashSaleItem;
import model.Entity.Order;
import model.Entity.OrderDetail;
import model.Entity.OrderTransaction;
import model.Entity.Product;
import model.Entity.User;
import model.Enum.LockMechanism;
import model.Enum.PaymentMethod;
import model.Enum.SaleStatus;
import model.Enum.UserRole;
import view.FlashSaleView;
import view.OrderView;
import view.ProductView;
import view.SimulatorView;
import view.UserView;

public class Main {

    private final Scanner scanner;
    private final AuthController authController;
    private final CartController cartController;
    private final ProductController productController;
    private final FlashSaleController flashSaleController;
    private final OrderController orderController;
    private final SimulatorController simulatorController;

    private final ProductView productView;
    private final OrderView orderView;
    private final FlashSaleView flashSaleView;
    private final UserView userView;
    private final SimulatorView simulatorView;

    private int configuredThreadCount;
    private LockMechanism selectedLockMechanism;

    public Main() {
        this.scanner = new Scanner(System.in);

        this.productView = new ProductView();
        this.orderView = new OrderView();
        this.flashSaleView = new FlashSaleView();
        this.userView = new UserView();
        this.simulatorView = new SimulatorView();

        this.productController = new ProductController(new repository.ProductRepository());
        this.flashSaleController = new FlashSaleController(
                new repository.FlashSaleRepository(),
                new repository.FlashSaleItemRepository(),
                new repository.ProductRepository());
        this.orderController = new OrderController();
        this.authController = new AuthController(new repository.UserRepository(), new view.AuthView(scanner));
        this.cartController = new CartController(
                new repository.CartRepository(),
                scanner,
                orderController,
                productController,
                flashSaleController,
                productView,
                orderView,
                flashSaleView);
        this.simulatorController = new SimulatorController();

        this.configuredThreadCount = 100;
        this.selectedLockMechanism = LockMechanism.NO_LOCK;
    }

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        boolean running = true;

        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();

            if ("1".equals(choice)) {
                showGuestMenu();
            } else if ("2".equals(choice)) {
                showCustomerMenu();
            } else if ("3".equals(choice)) {
                showSellerMenu();
            } else if ("4".equals(choice)) {
                showAdminMenu();
            } else if ("0".equals(choice)) {
                running = false;
            } else {
                System.out.println("Invalid choice.");
            }
        }

        System.out.println("Goodbye.");
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("===== FLASH SALE SYSTEM =====");
        System.out.println("1. Browse as Guest");
        System.out.println("2. Customer Shopping");
        System.out.println("3. Seller Center");
        System.out.println("4. Administration");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private void showGuestMenu() {
        boolean back = false;

        while (!back) {
            System.out.println();
            System.out.println("===== WELCOME =====");
            System.out.println("1. Create an Account");
            System.out.println("2. Sign In");
            System.out.println("3. Browse Flash Sales");
            System.out.println("4. Browse Products");
            System.out.println("5. Find Products");
            System.out.println("0. Back to Home");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            if ("1".equals(choice)) {
                authController.register();
            } else if ("2".equals(choice)) {
                UserRole role = inputLoginActor();
                if (role != null && authController.loginAs(role) != null) {
                    openMenuForRole(role);
                }
            } else if ("3".equals(choice)) {
                viewEvents();
            } else if ("4".equals(choice)) {
                viewProducts();
            } else if ("5".equals(choice)) {
                searchProducts();
            } else if ("0".equals(choice)) {
                back = true;
            } else {
                System.out.println("Invalid choice.");
            }
                    
        }
    }

    private void showCustomerMenu() {
        if (!ensureAuthenticated("CUSTOMER")) {
            return;
        }

        boolean back = false;

        while (!back) {
            System.out.println();
            System.out.println("===== MY SHOPPING =====");
            System.out.println("1. Browse Flash Sales");
            System.out.println("2. Browse Products");
            System.out.println("3. Find Products");
            System.out.println("4. Place order");
            System.out.println("5. My Cart");
            System.out.println("6. My Orders");
            System.out.println("0. Sign Out");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            if ("1".equals(choice)) {
                viewActiveEvents();
            } else if ("2".equals(choice)) {
                viewActiveProducts();
            } else if ("3".equals(choice)) {
                searchProducts();
            } else if ("4".equals(choice)) {
                placeOrder();
            } else if ("5".equals(choice)) {
                cartController.showCart(authController.getCurrentUser(), selectedLockMechanism);
            } else if ("6".equals(choice)) {
                viewMyOrders();
            } else if ("0".equals(choice)) {
                authController.logout();
                back = true;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private void showSellerMenu() {
        if (!ensureAuthenticated("SELLER")) {
            return;
        }

        boolean back = false;

        while (!back) {
            System.out.println();
            System.out.println("===== SELLER CENTER =====");
            System.out.println("1. My Products");
            System.out.println("2. Add a Product");
            System.out.println("3. Edit a Product");
            System.out.println("4. Remove a Product");
            System.out.println("5. Browse Sale Events");
            System.out.println("6. Add a Product to a Sale");
            System.out.println("7. Update Sale Price and Quantity");
            System.out.println("8. Review Customer Orders");
            System.out.println("0. Sign Out");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            if ("1".equals(choice)) {
                viewSellerProducts();
            } else if ("2".equals(choice)) {
                addProduct();
            } else if ("3".equals(choice)) {
                manageSellerProduct();
            } else if ("4".equals(choice)) {
                deleteProduct();
            } else if ("5".equals(choice)) {
                viewEventsForSeller();
            } else if ("6".equals(choice)) {
                assignOwnProductToEvent();
            } else if ("7".equals(choice)) {
                updateOwnFlashItem();
            } else if ("8".equals(choice)) {
                reviewPendingSellerOrders();
            } else if ("0".equals(choice)) {
                authController.logout();
                back = true;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private void showAdminMenu() {
        if (!ensureAuthenticated("ADMIN")) {
            return;
        }

        boolean back = false;

        while (!back) {
            System.out.println();
            System.out.println("===== ADMINISTRATION =====");
            System.out.println("1. System Overview");
            System.out.println("2. Stock Overview");
            System.out.println("3. Sales Activity");
            System.out.println("4. Stock Alerts");
            System.out.println("5. Set Test Order Quantity");
            System.out.println("6. Choose Order Processing Mode");
            System.out.println("7. Run Order Test");
            System.out.println("8. View Orders per Second");
            System.out.println("9. View Retry Rate");
            System.out.println("10. View Stock Issue Rate");
            System.out.println("11. Save Test Report");
            System.out.println("12. Create a Sale Event");
            System.out.println("13. Edit a Sale Event");
            System.out.println("14. Manage Accounts");
            System.out.println("15. View Account Details");
            System.out.println("0. Sign Out");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            if ("1".equals(choice)) {
                viewSystemDashboard();
            } else if ("2".equals(choice)) {
                viewInventoryReport();
            } else if ("3".equals(choice)) {
                viewThroughputReport();
            } else if ("4".equals(choice)) {
                viewNegativeStockReport();
            } else if ("5".equals(choice)) {
                configureThreadCount();
            } else if ("6".equals(choice)) {
                selectedLockMechanism = simulatorView.inputLockMechanism();
                System.out.println("Selected lock mechanism: " + selectedLockMechanism);
            } else if ("7".equals(choice)) {
                runConcurrentOrders();
            } else if ("8".equals(choice)) {
                System.out.println("TPS: " + simulatorController.measureTPS());
            } else if ("9".equals(choice)) {
                measureRetryRate();
            } else if ("10".equals(choice)) {
                measureNegativeStockRate();
            } else if ("11".equals(choice)) {
                exportSimulationResult();
            } else if ("12".equals(choice)) {
                createFlashEvent();
            } else if ("13".equals(choice)) {
                manageFlashEvent();
            } else if ("14".equals(choice)) {
                manageAccount();
            } else if ("15".equals(choice)) {
                viewAccount();
            } else if ("0".equals(choice)) {
                authController.logout();
                back = true;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private void viewEvents() {
        List<FlashSaleEvent> allEvents = flashSaleController.getAllEvents();
        List<FlashSaleEvent> activeEvents = new java.util.ArrayList<>();
        for (FlashSaleEvent event : allEvents) {
            if (event.getStatus() == SaleStatus.ACTIVE) {
                activeEvents.add(event);
            }
        }

        flashSaleView.displayEventSelection(activeEvents);

        if (!activeEvents.isEmpty()) {
            System.out.println("\nWould you like to view products in an event? (y/n): ");
            String choice = scanner.nextLine().trim().toLowerCase();
            if ("y".equals(choice) || "yes".equals(choice)) {
                browseEventProducts();
            }
        }
    }

    private void browseEventProducts() {
        String eventId = flashSaleView.inputEventId();
        FlashSaleEvent event = flashSaleController.getEventById(eventId);

        if (event == null || event.getStatus() != SaleStatus.ACTIVE) {
            System.out.println("[ERROR] Event not found or not active");
            return;
        }

        List<FlashSaleItem> eventItems = flashSaleController.getActiveFlashItemsByEventId(eventId);
        if (eventItems.isEmpty()) {
            System.out.println("No items in this event.");
            return;
        }

        System.out.println("\n===== PRODUCTS IN: " + event.getEventName() + " =====");
        flashSaleView.displayFlashSaleItemsInTable(eventItems, productController);

        if (authController.getCurrentUser() != null
                && authController.getCurrentUser().getRole() == UserRole.CUSTOMER) {
            orderFlashSaleFromEvent(eventId, eventItems);
        }
    }

    private void orderFlashSaleFromEvent(String eventId, List<FlashSaleItem> eventItems) {
        try {
            User currentUser = authController.getCurrentUser();
            if (currentUser == null) {
                System.out.println("[ERROR] Not authenticated");
                return;
            }

            model.Entity.Customer customer = orderController.getCustomerByUserId(currentUser.getId());
            if (customer == null) {
                System.out.println("[ERROR] Customer record not found");
                return;
            }

            String flashItemId = flashSaleView.inputFlashItemId();
            FlashSaleItem item = flashSaleController.getFlashItemById(flashItemId);

            if (item == null || !item.getEventId().equals(eventId)) {
                System.out.println("[ERROR] Flash item not found in event");
                return;
            }

            int quantity = orderView.inputQuantity();

            String orderId = orderController.placeOrder(
                    customer.getId(),
                    flashItemId,
                    quantity,
                    selectedLockMechanism);

            if (orderId != null) {
                System.out.println("[SUCCESS] Order created: " + orderId);

                PaymentMethod method = orderView.inputPaymentMethod();
                boolean paymentSuccess = orderController.createPayment(orderId, method);

                if (paymentSuccess) {
                    System.out.println("[SUCCESS] Payment processed successfully");
                    System.out.println("Order " + orderId + " is PENDING seller review.");
                } else {
                    System.out.println("[FAILED] Payment failed");
                }
            } else {
                System.out.println("[FAILED] Order placement failed");
            }
        } catch (NumberFormatException e) {
            orderView.showPlaceOrderError("Invalid input format");
        } catch (IOException e) {
            orderView.showPlaceOrderError("IO error: " + e.getMessage());
        } catch (FlashSaleException e) {
            orderView.showPlaceOrderError(e.getMessage());
        }
    }


    private void placeOrder() {
        int orderType = orderView.inputOrderType();

        if (orderType == 2) {
            orderRegularProduct();
        } else {
            shopeeCheckout();
        }
    }

    private void orderRegularProduct() {
        List<Product> allProducts = productController.getAllProducts();
        orderRegularProductFromProducts(allProducts, true);
    }

    private void orderRegularProductFromProducts(List<Product> selectableProducts, boolean showProducts) {
        try {
            User currentUser = authController.getCurrentUser();
            if (currentUser == null) {
                System.out.println("[ERROR] Not authenticated");
                return;
            }

            model.Entity.Customer customer = orderController.getCustomerByUserId(currentUser.getId());
            if (customer == null) {
                System.out.println("[ERROR] Customer record not found");
                return;
            }

            System.out.println("\n===== ORDER REGULAR PRODUCT =====");

            if (selectableProducts == null || selectableProducts.isEmpty()) {
                System.out.println("No products available.");
                return;
            }

            if (showProducts) {
                productView.displayProducts(selectableProducts);
            }

            String productId = orderView.inputProductId();
            Product product = productController.getProductById(productId);

            if (product == null || !containsProduct(selectableProducts, productId)) {
                System.out.println("[ERROR] Product not found in this list");
                return;
            }

            int quantity = orderView.inputQuantity();

            if (quantity <= 0) {
                System.out.println("[ERROR] Quantity must be greater than 0");
                return;
            }
            if (quantity > product.getStockQty()) {
                System.out.println("[ERROR] Insufficient stock. Available: " + product.getStockQty());
                return;
            }

            System.out.println("\nProduct: " + product.getName());
            System.out.println("Price: " + product.getOriginalPrice());
            System.out.println("Quantity: " + quantity);
            System.out.println("Total: " + (product.getOriginalPrice() * quantity));

            PaymentMethod method = orderView.inputPaymentMethod();

            String orderId = orderController.placeRegularOrder(customer.getId(), productId,
                    quantity, selectedLockMechanism);
            if (orderController.createPayment(orderId, method)) {
                System.out.println("[SUCCESS] Order " + orderId
                        + " is PENDING and waiting for seller review.");
            } else {
                System.out.println("[FAILED] Payment could not be saved for order " + orderId);
            }

        } catch (NumberFormatException e) {
            orderView.showPlaceOrderError("Invalid input format");
        } catch (FlashSaleException e) {
            orderView.showPlaceOrderError(e.getMessage());
        }
    }


    private void viewProducts() {
        List<Product> products = productController.getAllProducts();
        productView.displayProducts(products);
        offerPlaceOrderFromProducts(products);
    }

    private void viewSellerProducts() {
        productView.displayProducts(productController.getProductsBySellerId(authController.getCurrentUser().getId()));
    }

    private void searchProducts() {
        String keyword = productView.inputKeyword();
        List<Product> results = productController.searchProducts(keyword);
        productView.displaySearchResults(results);
        offerPlaceOrderFromProducts(results);
    }

    private void offerPlaceOrderFromProducts(List<Product> products) {
        User currentUser = authController.getCurrentUser();
        if (currentUser == null || currentUser.getRole() != UserRole.CUSTOMER || products.isEmpty()) {
            return;
        }
        orderRegularProductFromProducts(products, false);
    }

    private boolean containsProduct(List<Product> products, String productId) {
        if (products == null || productId == null) {
            return false;
        }
        for (Product product : products) {
            if (productId.equalsIgnoreCase(product.getId())) {
                return true;
            }
        }
        return false;
    }

    private void checkOrderStatus() {
        System.out.print("Order ID: ");
        String orderId = scanner.nextLine().trim();
        Order order = orderController.getOrderById(orderId);
        orderView.displayOrder(order, getEventName(order));
        if (order != null) {
            offerOrderDetailView(order.getCustomerId());
        }
    }

    private void checkInventory() {
        String flashItemId = flashSaleView.inputFlashItemId();
        FlashSaleItem item = flashSaleController.getFlashItemById(flashItemId);
        flashSaleView.displayFlashItem(item);
    }

    private void validatePurchaseLimit() {
        String customerId = orderView.inputCustomerId();
        String flashItemId = flashSaleView.inputFlashItemId();
        int purchasedQty = orderController.getPurchasedQuantity(customerId, flashItemId);
        orderView.displayPurchaseLimitResult(purchasedQty);
    }

    private void placeOrderFromInput() {
        try {
            String customerId = orderView.inputCustomerId();
            String flashItemId = flashSaleView.inputFlashItemId();
            int quantity = orderView.inputQuantity();

            String orderId = orderController.placeOrder(
                    customerId,
                    flashItemId,
                    quantity,
                    selectedLockMechanism);

            if (orderId != null) {
                orderView.showOrderSuccess();
            } else {
                orderView.showOrderFailure();
            }
        } catch (NumberFormatException e) {
            orderView.showPlaceOrderError("Quantity must be a number.");
        } catch (IOException e) {
            orderView.showPlaceOrderError("IO error: " + e.getMessage());
        } catch (FlashSaleException e) {
            orderView.showPlaceOrderError(e.getMessage());
        }
    }

    private void createPayment() {
        String orderId = orderView.inputOrderId();
        Order order = orderController.getOrderById(orderId);
        if (order == null) {
            orderView.showOrderNotFound();
            return;
        }

        PaymentMethod method = orderView.inputPaymentMethod();
        boolean success = orderController.createPayment(orderId, method);
        if (success) {
            orderView.showPaymentSaved();
        } else {
            orderView.showOrderNotFound();
        }
    }

    private void viewOrderHistoryByCustomer() {
        String customerId = orderView.inputCustomerId();
        List<Order> orders = orderController.getOrdersByCustomer(customerId);
        orderView.displayOrderHistory(orders);
    }

    private void viewMyOrders() {
        model.Entity.Customer customer = orderController.getCustomerByUserId(authController.getCurrentUser().getId());
        List<Order> orders = orderController.getOrdersByCustomer(customer.getId());
        orderView.displayBuyerOrderHistory(orders, getEventNames(orders));
        if (!orders.isEmpty()) {
            offerOrderDetailView(customer.getId());
        }
    }

    private void offerOrderDetailView(String customerId) {
        System.out.print("View order details? (y/n): ");
        String choice = scanner.nextLine().trim().toLowerCase();
        if (!"y".equals(choice) && !"yes".equals(choice)) {
            return;
        }

        System.out.print("Order ID: ");
        String orderId = scanner.nextLine().trim();
        Order order = orderController.getOrderById(orderId);
        if (order == null || (customerId != null && !customerId.equalsIgnoreCase(order.getCustomerId()))) {
            orderView.showOrderNotFound();
            return;
        }

        List<OrderDetail> details = orderController.getOrderDetailsByOrderId(orderId);
        orderView.displayOrderDetails(details,
                orderController.getProductNamesByOrderDetails(details),
                orderController.getSellerNamesByOrderDetails(details),
                getEventNamesByDetails(details));
    }

    private Map<String, String> getEventNames(List<Order> orders) {
        Map<String, String> eventNames = new LinkedHashMap<String, String>();
        for (Order order : orders) {
            eventNames.put(order.getId(), getEventName(order));
        }
        return eventNames;
    }

    private String getEventName(Order order) {
        if (order == null) {
            return "";
        }
        List<OrderDetail> details = orderController.getOrderDetailsByOrderId(order.getId());
        Map<String, String> names = new LinkedHashMap<String, String>();
        for (OrderDetail detail : details) {
            if (detail.getEventId() == null) {
                continue;
            }
            FlashSaleEvent event = flashSaleController.getEventById(detail.getEventId());
            names.put(detail.getEventId(), event == null ? detail.getEventId() : event.getEventName());
        }
        if (names.isEmpty()) {
            return "Regular Product";
        }
        return String.join(", ", names.values());
    }

    private Map<String, String> getEventNamesByDetails(List<OrderDetail> details) {
        Map<String, String> eventNames = new LinkedHashMap<String, String>();
        for (OrderDetail detail : details) {
            if (detail.getEventId() == null) {
                eventNames.put(detail.getId(), "Regular Product");
                continue;
            }
            FlashSaleEvent event = flashSaleController.getEventById(detail.getEventId());
            eventNames.put(detail.getId(), event == null ? detail.getEventId() : event.getEventName());
        }
        return eventNames;
    }

    private void addProduct() {
        Product product = productView.inputProductData();
        boolean created = productController.createProduct(product, authController.getCurrentUser().getId());
        productView.showAddProductResult(created);
    }

    private void manageSellerProduct() {
        String sellerId = authController.getCurrentUser().getId();
        List<Product> products = productController.getProductsBySellerId(sellerId);
        productView.displayProducts(products);
        if (products.isEmpty()) {
            return;
        }
        String productId = productView.inputProductId();
        Product product = productController.getAnyProductById(productId);
        if (product == null || !sellerId.equalsIgnoreCase(product.getSellerId())) {
            productView.showProductNotFound();
            return;
        }
        String newName = productView.inputNewName();
        String newCategory = productView.inputNewCategory();
        double newPrice = productView.inputNewPrice();
        System.out.print("New stock quantity: ");
        int newStock = Integer.parseInt(scanner.nextLine().trim());
        product.setName(newName);
        product.setCategory(newCategory);
        product.setOriginalPrice(newPrice);
        product.setStockQty(newStock);
        productView.showUpdateProductResult(productController.updateProduct(product));
    }

    private void updateProduct() {
        Product product = productView.inputProductData();
        boolean updated = productController.updateProduct(product);
        productView.showUpdateProductResult(updated);
    }

    private void editProductInformation() {
        String productId = productView.inputProductId();
        Product product = productController.getAnyProductById(productId);

        if (product == null) {
            productView.showProductNotFound();
            return;
        }

        String newName = productView.inputNewName();
        String newCategory = productView.inputNewCategory();
        boolean success = productController.updateProductInfo(productId, newName, newCategory);
        productView.showEditInfoResult(success);
    }

    private void editProductPrice() {
        String productId = productView.inputProductId();
        Product product = productController.getAnyProductById(productId);

        if (product == null) {
            productView.showProductNotFound();
            return;
        }

        double newPrice = productView.inputNewPrice();
        boolean success = productController.updateProductPrice(productId, newPrice);
        productView.showEditPriceResult(success);
    }

    private void deleteProduct() {
        String productId = productView.inputProductId();
        Product product = productController.getAnyProductById(productId);
        if (product == null || !authController.getCurrentUser().getId().equalsIgnoreCase(product.getSellerId())) {
            productView.showProductNotFound();
            return;
        }
        boolean deleted = productController.deleteProduct(productId);
        productView.showDeleteProductResult(deleted);
    }

    private void viewEventsForSeller() {
        List<FlashSaleEvent> events = flashSaleController.getAllEvents();
        flashSaleView.displayEventList(events);
        if (events.isEmpty()) {
            return;
        }
        System.out.print("Add one of your products to an event now? (y/n): ");
        String choice = scanner.nextLine().trim().toLowerCase();
        if ("y".equals(choice) || "yes".equals(choice)) {
            assignOwnProductToEvent();
        }
    }

    private void assignOwnProductToEvent() {
        flashSaleView.displayEventList(flashSaleController.getAllEvents());
        String eventId = flashSaleView.inputEventId();
        if (flashSaleController.getEventById(eventId) == null) {
            flashSaleView.showEventNotFound();
            return;
        }
        List<Product> products = productController.getProductsBySellerId(authController.getCurrentUser().getId());
        productView.displayProducts(products);
        if (products.isEmpty()) {
            return;
        }
        String productId = productView.inputProductId();
        Product product = productController.getAnyProductById(productId);
        if (product == null || !authController.getCurrentUser().getId().equalsIgnoreCase(product.getSellerId())) {
            flashSaleView.showProductNotFound();
            return;
        }
        System.out.print("Discount percent (0-99): ");
        double discountPercent = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Limited quantity: ");
        int limitedQty = Integer.parseInt(scanner.nextLine().trim());
        if (discountPercent < 0 || discountPercent >= 100 || limitedQty <= 0 || limitedQty > product.getStockQty()) {
            flashSaleView.showExceedStockError(product.getStockQty());
            return;
        }
        double flashPrice = product.getOriginalPrice() * (100.0 - discountPercent) / 100.0;
        FlashSaleItem item = flashSaleController.assignProductToEvent(eventId, productId, flashPrice, limitedQty);
        flashSaleView.showAssignProductResult(item != null, item);
    }

    private void updateOwnFlashItem() {
        flashSaleView.displayEventList(flashSaleController.getAllEvents());
        String eventId = flashSaleView.inputEventId();
        List<Product> products = new java.util.ArrayList<Product>();
        for (FlashSaleItem item : flashSaleController.getFlashItemsByEventId(eventId)) {
            Product candidate = productController.getAnyProductById(item.getProductId());
            if (candidate != null && authController.getCurrentUser().getId().equalsIgnoreCase(candidate.getSellerId())) {
                products.add(candidate);
            }
        }
        productView.displayProducts(products);
        if (products.isEmpty()) {
            System.out.println("No products of yours are assigned to this event.");
            return;
        }
        String productId = productView.inputProductId();
        Product product = productController.getAnyProductById(productId);
        if (product == null || !authController.getCurrentUser().getId().equalsIgnoreCase(product.getSellerId())) {
            flashSaleView.showProductNotFound();
            return;
        }
        System.out.print("New discount percent (0-99): ");
        double discountPercent = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("New limited quantity: ");
        int limitedQty = Integer.parseInt(scanner.nextLine().trim());
        boolean success = flashSaleController.updateFlashItem(eventId, productId,
                authController.getCurrentUser().getId(), discountPercent, limitedQty);
        System.out.println(success ? "[SUCCESS] Flash item updated; sale price was calculated automatically."
                : "[FAILED] Flash item was not found or the values are invalid.");
    }

    private void confirmSellerOrder() {
        String sellerId = authController.getCurrentUser().getId();
        List<Order> orders = orderController.getOrdersForSeller(sellerId);
        orderView.displaySellerOrderReview(orders,
                orderController.getSellerOrderProductSummaryByOrder(sellerId, orders));
        if (orders.isEmpty()) {
            return;
        }
        String orderId = orderView.inputOrderId();
        orderView.showConfirmOrderResult(orderController.confirmOrderForSeller(orderId, sellerId));
    }

    private void reviewPendingSellerOrders() {
        String sellerId = authController.getCurrentUser().getId();
        List<Order> orders = orderController.getPendingOrdersForSeller(sellerId);
        orderView.displaySellerOrderReview(orders,
                orderController.getSellerOrderProductSummaryByOrder(sellerId, orders));
        if (orders.isEmpty()) {
            return;
        }
        String orderId = orderView.inputOrderId();
        System.out.println("1. Confirm order");
        System.out.println("2. Cancel order");
        System.out.print("Choose review action: ");
        String action = scanner.nextLine().trim();
        boolean success = "1".equals(action)
                ? orderController.confirmOrderForSeller(orderId, sellerId)
                : "2".equals(action)
                        && orderController.cancelOrderForSeller(orderId, sellerId);
        System.out.println(success ? "[SUCCESS] Order review completed."
                : "[FAILED] Only pending orders of your products can be reviewed.");
    }

    private void assignProductToEvent() {
        String eventId = flashSaleView.inputEventId();
        FlashSaleEvent event = flashSaleController.getEventById(eventId);

        if (event == null) {
            flashSaleView.showEventNotFound();
            return;
        }

        String productId = productView.inputProductId();
        Product product = flashSaleController.getProductById(productId);

        if (product == null) {
            flashSaleView.showProductNotFound();
            return;
        }

        double flashPrice = productView.inputNewPrice();
        int limitedQty = orderView.inputQuantity();

        if (limitedQty > product.getStockQty()) {
            flashSaleView.showExceedStockError(product.getStockQty());
            return;
        }

        FlashSaleItem item = flashSaleController.assignProductToEvent(eventId, productId, flashPrice, limitedQty);
        boolean success = (item != null);
        flashSaleView.showAssignProductResult(success, item);
    }

    private void confirmOrder() {
        String orderId = orderView.inputOrderId();
        boolean success = orderController.confirmOrder(orderId);
        orderView.showConfirmOrderResult(success);
    }

    private void viewSystemDashboard() {
        Map<String, Integer> counts = simulatorController.getDashboardStats();
        simulatorView.showDashboard(counts, configuredThreadCount, selectedLockMechanism);
    }

    private void viewInventoryReport() {
        List<FlashSaleItem> items = simulatorController.getInventoryReport();
        simulatorView.showInventoryReport(items);
    }

    private void viewThroughputReport() {
        List<OrderTransaction> transactions = simulatorController.getAllTransactions();
        int success = 0;
        long totalTime = 0L;

        for (OrderTransaction transaction : transactions) {
            if (transaction.isSuccess()) {
                success++;
            }
            totalTime += transaction.getExecutionTimeMs();
        }

        simulatorView.showThroughputReport(transactions.size(), success, totalTime, simulatorController.measureTPS());
    }

    private void viewNegativeStockReport() {
        List<FlashSaleItem> items = simulatorController.getNegativeStockItems();
        simulatorView.showNegativeStockReport(items);
    }

    private void configureThreadCount() {
        configuredThreadCount = simulatorView.inputThreadCount();
        simulatorView.showThreadCountConfigured(configuredThreadCount);
    }

    private void runConcurrentOrders() {
        simulatorView.showSimulationHeader(configuredThreadCount, selectedLockMechanism);
        simulatorController.startSimulation();
    }

    private void measureRetryRate() {
        double rate = simulatorController.getRetryRate();
        simulatorView.showRetryRate(rate);
    }

    private void measureNegativeStockRate() {
        double rate = simulatorController.getNegativeStockRate();
        simulatorView.showNegativeStockRate(rate);
    }

    private void exportSimulationResult() {
        boolean success = simulatorController.exportSimulationResult(configuredThreadCount, selectedLockMechanism);
        simulatorView.showExportResult(success, "docs/simulation_result.txt");
    }

    private void createFlashEvent() {
        String name = flashSaleView.inputNewEventNameForCreation();
        boolean success = flashSaleController.createEvent(name);
        flashSaleView.showCreateEventResult(success);
    }

    private void manageFlashEvent() {
        List<FlashSaleEvent> events = flashSaleController.getAllEvents();
        flashSaleView.displayEventList(events);
        String eventId = flashSaleView.inputEventId();
        FlashSaleEvent event = flashSaleController.getEventById(eventId);
        if (event == null) {
            flashSaleView.showEventNotFound();
            return;
        }
        System.out.print("New event name [" + event.getEventName() + "] (blank = keep): ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            name = event.getEventName();
        }
        SaleStatus status = flashSaleView.inputEventStatus();
        try {
            java.time.LocalDateTime start = flashSaleView.inputDateTime("Start time", event.getStartTime());
            java.time.LocalDateTime end = flashSaleView.inputDateTime("End time", event.getEndTime());
            flashSaleView.showUpdateEventNameResult(
                    flashSaleController.updateEvent(eventId, name, status, start, end));
        } catch (java.time.format.DateTimeParseException e) {
            System.out.println("[FAILED] Use date-time format yyyy-MM-ddTHH:mm:ss.");
        }
    }

    private void changeFlashEventStatus(SaleStatus status) {
        String eventId = flashSaleView.inputEventId();
        boolean success = flashSaleController.updateEventStatus(eventId, status);
        flashSaleView.showUpdateEventResult(success);
    }

    private void editFlashSaleInformation() {
        String eventId = flashSaleView.inputEventId();
        FlashSaleEvent event = flashSaleController.getEventById(eventId);

        if (event == null) {
            flashSaleView.showEventNotFound();
            return;
        }

        String newName = flashSaleView.inputNewEventName();
        boolean success = flashSaleController.updateEventName(eventId, newName);
        flashSaleView.showUpdateEventNameResult(success);
    }

    private void changeAccountStatus(boolean active) {
        String userId = userView.inputUserIdRequired();
        boolean success = active
                ? authController.approveAccount(userId)
                : authController.suspendAccount(userId);
        userView.showAccountStatusResult(success);
    }

    private void viewAccount() {
        String userId = userView.inputUserId();

        if (userId.isEmpty()) {
            userView.displayUserList(authController.getAllUsers());
            return;
        }

        User user = authController.getUserById(userId);
        if (user == null) {
            userView.showUserNotFound();
        } else {
            userView.displayUser(user);
        }
    }

    private void editAccount() {
        String userId = userView.inputUserIdRequired();
        User user = authController.getUserById(userId);

        if (user == null) {
            userView.showUserNotFound();
            return;
        }

        String newUsername = userView.inputNewUsername();
        UserRole newRole = userView.inputUserRole();
        boolean success = authController.updateUserAccount(userId, newUsername, newRole);
        userView.showAccountUpdateResult(success);
    }

    private void manageAccount() {
        userView.displayUserList(authController.getAllUsers());
        String userId = userView.inputUserIdRequired();
        User user = authController.getUserById(userId);
        if (user == null) {
            userView.showUserNotFound();
            return;
        }
        System.out.println("1. Approve account");
        System.out.println("2. Suspend account");
        System.out.println("3. Edit username and role");
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();
        if ("1".equals(choice)) {
            userView.showAccountStatusResult(authController.approveAccount(userId));
        } else if ("2".equals(choice)) {
            userView.showAccountStatusResult(authController.suspendAccount(userId));
        } else if ("3".equals(choice)) {
            String username = userView.inputNewUsername();
            UserRole role = userView.inputUserRole();
            userView.showAccountUpdateResult(authController.updateUserAccount(userId, username, role));
        } else {
            System.out.println("Invalid choice.");
        }
    }

    private boolean ensureAuthenticated(String requiredRole) {
        User currentUser = authController.getCurrentUser();
        if (currentUser != null) {
            return validateUserRole(currentUser, requiredRole);
        }

        System.out.println();
        System.out.println("===== AUTHENTICATION REQUIRED =====");
        System.out.println("1. Login");
        if (!"ADMIN".equalsIgnoreCase(requiredRole)) {
            System.out.println("2. Register");
        }
        System.out.println("0. Back");
        System.out.print("Choose: ");

        String choice = scanner.nextLine().trim();

        if ("1".equals(choice)) {
            User loggedInUser = authController.loginAs(UserRole.valueOf(requiredRole.toUpperCase()));
            if (loggedInUser != null) {
                return true;
            }
        } else if ("2".equals(choice) && !"ADMIN".equalsIgnoreCase(requiredRole)) {
            authController.register(UserRole.valueOf(requiredRole.toUpperCase()));
            User registeredUser = authController.getCurrentUser();
            if (registeredUser != null) {
                return true;
            }
        }

        return false;
    }

    private UserRole inputLoginActor() {
        System.out.println("Login as:");
        System.out.println("1. Customer");
        System.out.println("2. Seller");
        System.out.println("3. Admin");
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();
        if ("1".equals(choice)) return UserRole.CUSTOMER;
        if ("2".equals(choice)) return UserRole.SELLER;
        if ("3".equals(choice)) return UserRole.ADMIN;
        System.out.println("Invalid actor.");
        return null;
    }

    private void openMenuForRole(UserRole role) {
        if (role == UserRole.CUSTOMER) {
            showCustomerMenu();
        } else if (role == UserRole.SELLER) {
            showSellerMenu();
        } else if (role == UserRole.ADMIN) {
            showAdminMenu();
        }
    }

    private boolean validateUserRole(User user, String requiredRole) {
        if (user == null) {
            return false;
        }

        UserRole userRole = user.getRole();
        boolean hasValidRole = false;

        if ("CUSTOMER".equalsIgnoreCase(requiredRole) && userRole == UserRole.CUSTOMER) {
            hasValidRole = true;
        } else if ("SELLER".equalsIgnoreCase(requiredRole) && userRole == UserRole.SELLER) {
            hasValidRole = true;
        } else if ("ADMIN".equalsIgnoreCase(requiredRole) && userRole == UserRole.ADMIN) {
            hasValidRole = true;
        }

        if (!hasValidRole) {
            System.out.println("[FAILED] Access denied. Your role is " + userRole +
                             " but " + requiredRole + " role is required.");
            authController.logout();
            return false;
        }

        return true;
    }


    private void viewActiveEvents() {
        List<FlashSaleEvent> allEvents = flashSaleController.getAllEvents();
        List<FlashSaleEvent> activeEvents = new java.util.ArrayList<>();
        for (FlashSaleEvent event : allEvents) {
            if (event.getStatus() == SaleStatus.ACTIVE) {
                activeEvents.add(event);
            }
        }
        flashSaleView.displayEventSelection(activeEvents);

        if (!activeEvents.isEmpty()) {
            System.out.println("\nWould you like to view products in an event? (y/n): ");
            String choice = scanner.nextLine().trim().toLowerCase();
            if ("y".equals(choice) || "yes".equals(choice)) {
                browseEventProducts();
            }
        }
    }

    private void viewActiveProducts() {
        List<Product> allProducts = productController.getAllProducts();
        List<Product> activeProducts = new java.util.ArrayList<>();
        for (Product product : allProducts) {
            if (product.getStatus() == SaleStatus.ACTIVE) {
                activeProducts.add(product);
            }
        }
        productView.displayProducts(activeProducts);
        offerPlaceOrderFromProducts(activeProducts);
    }

    private void shopeeCheckout() {
        try {
            User currentUser = authController.getCurrentUser();
            if (currentUser == null) {
                System.out.println("[ERROR] Not authenticated");
                return;
            }

            model.Entity.Customer customer = orderController.getCustomerByUserId(currentUser.getId());
            if (customer == null) {
                System.out.println("[ERROR] Customer record not found");
                return;
            }

            System.out.println("\n===== FLASH SALE ORDER =====");

            List<FlashSaleEvent> activeEvents = new java.util.ArrayList<>();
            for (FlashSaleEvent event : flashSaleController.getAllEvents()) {
                if (event.getStatus() == SaleStatus.ACTIVE) {
                    activeEvents.add(event);
                }
            }

            if (activeEvents.isEmpty()) {
                System.out.println("No active events available.");
                return;
            }

            flashSaleView.displayActiveEvents(activeEvents);

            String eventId = flashSaleView.inputEventId();
            FlashSaleEvent event = flashSaleController.getEventById(eventId);

            if (event == null || event.getStatus() != SaleStatus.ACTIVE) {
                System.out.println("[ERROR] Event not found or not active");
                return;
            }

            List<FlashSaleItem> eventItems = flashSaleController.getActiveFlashItemsByEventId(eventId);
            if (eventItems.isEmpty()) {
                System.out.println("No items in this event.");
                return;
            }

            flashSaleView.displayFlashSaleItemsInTable(eventItems, productController);

            String flashItemId = flashSaleView.inputFlashItemId();
            FlashSaleItem item = flashSaleController.getFlashItemById(flashItemId);

            if (item == null || !item.getEventId().equals(eventId)) {
                System.out.println("[ERROR] Flash item not found in event");
                return;
            }

            int quantity = orderView.inputQuantity();

            String orderId = orderController.placeOrder(
                    customer.getId(),
                    flashItemId,
                    quantity,
                    selectedLockMechanism);

            if (orderId != null) {
                System.out.println("[SUCCESS] Order created: " + orderId);

                PaymentMethod method = orderView.inputPaymentMethod();
                boolean paymentSuccess = orderController.createPayment(orderId, method);

                if (paymentSuccess) {
                    System.out.println("[SUCCESS] Payment processed successfully");
                    System.out.println("Order " + orderId + " is PENDING seller review.");
                } else {
                    System.out.println("[FAILED] Payment failed");
                }
            } else {
                System.out.println("[FAILED] Order placement failed");
            }
        } catch (NumberFormatException e) {
            orderView.showPlaceOrderError("Invalid input format");
        } catch (IOException e) {
            orderView.showPlaceOrderError("IO error: " + e.getMessage());
        } catch (FlashSaleException e) {
            orderView.showPlaceOrderError(e.getMessage());
        }
    }
}
