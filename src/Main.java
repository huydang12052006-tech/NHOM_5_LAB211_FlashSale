import controller.AuthController;
import controller.FlashSaleController;
import controller.OrderController;
import controller.ProductController;
import controller.SimulatorController;
import exception.FlashSaleException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import model.Entity.FlashSaleEvent;
import model.Entity.FlashSaleItem;
import model.Entity.Order;
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

        this.authController = new AuthController();
        this.productController = new ProductController(new repository.ProductRepository());
        this.flashSaleController = new FlashSaleController(
                new repository.FlashSaleRepository(),
                new repository.FlashSaleItemRepository(),
                new repository.ProductRepository());
        this.orderController = new OrderController();
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
        System.out.println("1. Guest");
        System.out.println("2. Customer");
        System.out.println("3. Seller");
        System.out.println("4. Admin");
        System.out.println("0. Exit");
        System.out.print("Choose actor: ");
    }

    private void showGuestMenu() {
        boolean back = false;

        while (!back) {
            System.out.println();
            System.out.println("===== GUEST USE CASES =====");
            System.out.println("1. Register account");
            System.out.println("2. Login");
            System.out.println("3. View events");
            System.out.println("4. View products");
            System.out.println("5. Search products");
            System.out.println("6. Check order status");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            if ("1".equals(choice)) {
                authController.register();
            } else if ("2".equals(choice)) {
                authController.login();
            } else if ("3".equals(choice)) {
                viewEvents();
            } else if ("4".equals(choice)) {
                viewProducts();
            } else if ("5".equals(choice)) {
                searchProducts();
            } else if ("6".equals(choice)) {
                checkOrderStatus();
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
            System.out.println("===== CUSTOMER USE CASES =====");
            System.out.println("1. View events");
            System.out.println("2. View products");
            System.out.println("3. Search products");
            System.out.println("4. Place order");
            System.out.println("5. View order history");
            System.out.println("6. Check order status");
            System.out.println("0. Logout & Back");
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
                viewOrderHistoryByCustomer();
            } else if ("6".equals(choice)) {
                checkOrderStatus();
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
            System.out.println("===== SELLER USE CASES =====");
            System.out.println("1. View products");
            System.out.println("2. Add product");
            System.out.println("3. Update product");
            System.out.println("4. Edit product information");
            System.out.println("5. Edit product price");
            System.out.println("6. Delete product");
            System.out.println("7. View events");
            System.out.println("8. Assign product to event");
            System.out.println("9. Set discount and limited quantity");
            System.out.println("10. Confirm order");
            System.out.println("0. Logout & Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            if ("1".equals(choice)) {
                viewProducts();
            } else if ("2".equals(choice)) {
                addProduct();
            } else if ("3".equals(choice)) {
                updateProduct();
            } else if ("4".equals(choice)) {
                editProductInformation();
            } else if ("5".equals(choice)) {
                editProductPrice();
            } else if ("6".equals(choice)) {
                deleteProduct();
            } else if ("7".equals(choice)) {
                viewEvents();
            } else if ("8".equals(choice) || "9".equals(choice)) {
                assignProductToEvent();
            } else if ("10".equals(choice)) {
                confirmOrder();
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
            System.out.println("===== ADMIN USE CASES =====");
            System.out.println("1. View system dashboard");
            System.out.println("2. View inventory report");
            System.out.println("3. View throughput report");
            System.out.println("4. View negative stock report");
            System.out.println("5. Configure thread count");
            System.out.println("6. Select lock mechanism");
            System.out.println("7. Run concurrent orders");
            System.out.println("8. Measure TPS");
            System.out.println("9. Measure retry rate");
            System.out.println("10. Measure negative stock rate");
            System.out.println("11. Export simulation result");
            System.out.println("12. Create flash event");
            System.out.println("13. Start flash event");
            System.out.println("14. End flash event");
            System.out.println("15. Edit flash sale information");
            System.out.println("16. Approve account");
            System.out.println("17. Suspend account");
            System.out.println("18. View account");
            System.out.println("19. Edit account");
            System.out.println("0. Logout & Back");
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
                changeFlashEventStatus(SaleStatus.ACTIVE);
            } else if ("14".equals(choice)) {
                changeFlashEventStatus(SaleStatus.ENDED);
            } else if ("15".equals(choice)) {
                editFlashSaleInformation();
            } else if ("16".equals(choice)) {
                changeAccountStatus(true);
            } else if ("17".equals(choice)) {
                changeAccountStatus(false);
            } else if ("18".equals(choice)) {
                viewAccount();
            } else if ("19".equals(choice)) {
                editAccount();
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

        List<FlashSaleItem> eventItems = flashSaleController.getFlashItemsByEventId(eventId);
        if (eventItems.isEmpty()) {
            System.out.println("No items in this event.");
            return;
        }

        System.out.println("\n===== PRODUCTS IN: " + event.getEventName() + " =====");
        flashSaleView.displayFlashSaleItemsInTable(eventItems, productController);

        System.out.println("\nWould you like to place an order? (y/n): ");
        String choice = scanner.nextLine().trim().toLowerCase();
        if ("y".equals(choice) || "yes".equals(choice)) {
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
                    orderController.confirmOrder(orderId);
                    System.out.println("[SUCCESS] Payment processed successfully");
                    System.out.println("Order " + orderId + " is now confirmed.");
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

            List<Product> allProducts = productController.getAllProducts();
            if (allProducts.isEmpty()) {
                System.out.println("No products available.");
                return;
            }

            productView.displayProducts(allProducts);

            String productId = orderView.inputProductId();
            Product product = productController.getProductById(productId);

            if (product == null) {
                System.out.println("[ERROR] Product not found");
                return;
            }

            int quantity = orderView.inputQuantity();

            if (quantity > product.getStockQty()) {
                System.out.println("[ERROR] Insufficient stock. Available: " + product.getStockQty());
                return;
            }

            System.out.println("\nProduct: " + product.getName());
            System.out.println("Price: " + product.getOriginalPrice());
            System.out.println("Quantity: " + quantity);
            System.out.println("Total: " + (product.getOriginalPrice() * quantity));

            PaymentMethod method = orderView.inputPaymentMethod();

            System.out.println("[INFO] Processing order...");
            System.out.println("[SUCCESS] Order created and confirmed.");

        } catch (NumberFormatException e) {
            orderView.showPlaceOrderError("Invalid input format");
        }
    }


    private void viewProducts() {
        productView.displayProducts(productController.getAllProducts());
    }

    private void searchProducts() {
        String keyword = productView.inputKeyword();
        productView.displaySearchResults(productController.searchProducts(keyword));
    }

    private void checkOrderStatus() {
        String orderId = orderView.inputOrderId();
        orderView.displayOrder(orderController.getOrderById(orderId));
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

    private void addProduct() {
        Product product = productView.inputProductData();
        boolean created = productController.createProduct(product);
        productView.showAddProductResult(created);
    }

    private void updateProduct() {
        Product product = productView.inputProductData();
        boolean updated = productController.updateProduct(product);
        productView.showUpdateProductResult(updated);
    }

    private void editProductInformation() {
        String productId = productView.inputProductId();
        Product product = productController.getProductById(productId);

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
        Product product = productController.getProductById(productId);

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
        boolean deleted = productController.deleteProduct(productId);
        productView.showDeleteProductResult(deleted);
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
        String[] data = flashSaleView.inputNewEvent();
        String id = data[0];
        String name = data[1];
        boolean success = flashSaleController.createEvent(id, name);
        flashSaleView.showCreateEventResult(success);
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

    private boolean ensureAuthenticated(String requiredRole) {
        User currentUser = authController.getCurrentUser();
        if (currentUser != null) {
            return validateUserRole(currentUser, requiredRole);
        }

        System.out.println();
        System.out.println("===== AUTHENTICATION REQUIRED =====");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("0. Back");
        System.out.print("Choose: ");

        String choice = scanner.nextLine().trim();

        if ("1".equals(choice)) {
            User loggedInUser = authController.validateLogin();
            if (loggedInUser != null) {
                if (validateUserRole(loggedInUser, requiredRole)) {
                    System.out.println("[SUCCESS] Login successful.");
                    return true;
                }
                return false;
            }
        } else if ("2".equals(choice)) {
            authController.register();
            User registeredUser = authController.getCurrentUser();
            if (registeredUser != null) {
                if (!validateUserRole(registeredUser, requiredRole)) {
                    return false;
                }
                return true;
            }
        }

        return false;
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

        if (!activeProducts.isEmpty()) {
            System.out.println("\nWould you like to place an order? (y/n): ");
            String choice = scanner.nextLine().trim().toLowerCase();
            if ("y".equals(choice) || "yes".equals(choice)) {
                orderRegularProduct();
            }
        }
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

            List<FlashSaleItem> eventItems = flashSaleController.getFlashItemsByEventId(eventId);
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
                    orderController.confirmOrder(orderId);
                    System.out.println("[SUCCESS] Payment processed successfully");
                    System.out.println("Order " + orderId + " is now confirmed.");
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
