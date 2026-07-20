``` mermaid
classDiagram
direction LR
	namespace APP {
        class Main {
	        -scanner: Scanner
	        -authController: AuthController
	        -cartController: CartController
	        -productController: ProductController
	        -flashSaleController: FlashSaleController
	        -orderController: OrderController
	        -simulatorController: SimulatorController
	        -customerRepository: CustomerRepository
	        -productView: ProductView
	        -orderView: OrderView
	        -flashSaleView: FlashSaleView
	        -userView: UserView
	        -simulatorView: SimulatorView
	        -configuredThreadCount: int
	        -selectedLockMechanism: LockMechanism
	        +main(args: String[]) void
        }
	}

	namespace MODEL {
        class BaseEntity {
	        -id: String
	        -createdAt: LocalDateTime
	        -updatedAt: LocalDateTime
            +getId() String
	        +setId(id: String) void
            +getCreatedAt() LocalDateTime
	        +setCreatedAt(createdAt: LocalDateTime) void
	        +getUpdatedAt() LocalDateTime
	        +setUpdatedAt(updatedAt: LocalDateTime) void
	        +toCsvLine() String
	        +fromCsvLine(csv: String) void
	        #formatDateTime(dateTime: LocalDateTime) String
	        #parseDateTime(value: String) LocalDateTime
	        #escapeCsv(value: String) String
        }

        class Product {
	        -name: String
	        -category: String
	        -originalPrice: double
	        -stockQty: int
	        -version: int
	        -status: SaleStatus
	        -sellerId: String
	        +getName() String
	        +setName(name: String) void
	        +getCategory() String
	        +setCategory(category: String) void
	        +getOriginalPrice() double
	        +setOriginalPrice(originalPrice: double) void
	        +getStockQty() int
	        +setStockQty(stockQty: int) void
	        +getVersion() int
	        +setVersion(version: int) void
	        +getStatus() SaleStatus
	        +setStatus(status: SaleStatus) void
	        +getSellerId() String
	        +setSellerId(sellerId: String) void
	        +toCsvLine() String
	        +fromCsvLine(csvLine: String) void
        }

        class FlashSaleEvent {
	        -eventName: String
	        -startTime: LocalDateTime
	        -endTime: LocalDateTime
	        -status: SaleStatus
	        +getEventName() String
	        +setEventName(eventName: String) void
	        +getStartTime() LocalDateTime
	        +setStartTime(startTime: LocalDateTime) void
	        +getEndTime() LocalDateTime
	        +setEndTime(endTime: LocalDateTime) void
	        +getStatus() SaleStatus
	        +setStatus(status: SaleStatus) void
	        +toCsvLine() String
	        +fromCsvLine(csvLine: String) void
        }

        class FlashSaleItem {
	        -eventId: String
	        -productId: String
	        -flashPrice: double
	        -limitedQty: int
	        -soldQty: int
	        -discountPercent: double
	        -version: int
	        -status: SaleStatus
	        +getEventId() String
	        +setEventId(eventId: String) void
	        +getProductId() String
	        +setProductId(productId: String) void
	        +getFlashPrice() double
	        +setFlashPrice(flashPrice: double) void
	        +getLimitedQty() int
	        +setLimitedQty(limitedQty: int) void
	        +getSoldQty() int
	        +setSoldQty(soldQty: int) void
	        +getDiscountPercent() double
	        +setDiscountPercent(discountPercent: double) void
	        +getVersion() int
	        +setVersion(version: int) void
	        +getStatus() SaleStatus
	        +setStatus(status: SaleStatus) void
	        +toCsvLine() String
	        +fromCsvLine(csvLine: String) void
        }

        class Customer {
	        -userId: String
	        -fullName: String
	        -phone: String
	        -email: String
	        -address: String
	        -tier: CustomerTier
	        -totalSpent: double
	        -active: boolean
	        +getUserId() String
	        +setUserId(userId: String) void
	        +getFullName() String
	        +setFullName(fullName: String) void
	        +getPhone() String
	        +setPhone(phone: String) void
	        +getEmail() String
	        +setEmail(email: String) void
	        +getAddress() String
	        +setAddress(address: String) void
	        +getTier() CustomerTier
	        +setTier(tier: CustomerTier) void
	        +getTotalSpent() double
	        +setTotalSpent(totalSpent: double) void
	        +isActive() boolean
	        +setActive(active: boolean) void
	        +toCsvLine() String
	        +fromCsvLine(csv: String) void
        }

        class Order {
	        -customerId: String
	        -totalAmount: double
	        -status: OrderStatus
	        -lockMechanism: LockMechanism
	        +getCustomerId() String
	        +setCustomerId(customerId: String) void
	        +getTotalAmount() double
	        +setTotalAmount(totalAmount: double) void
	        +getStatus() OrderStatus
	        +setStatus(status: OrderStatus) void
	        +getLockMechanism() LockMechanism
	        +setLockMechanism(lockMechanism: LockMechanism) void
	        +toCsvLine() String
	        +fromCsvLine(csv: String) void
        }

        class OrderDetail {
	        -orderId: String
	        -eventId: String
	        -flashItemId: String
	        -productId: String
	        -quantity: int
	        -unitPrice: double
	        -subTotal: double
	        +getOrderId() String
	        +setOrderId(orderId: String) void
	        +getEventId() String
	        +setEventId(eventId: String) void
	        +getFlashItemId() String
	        +setFlashItemId(flashItemId: String) void
	        +getProductId() String
	        +setProductId(productId: String) void
	        +getQuantity() int
	        +setQuantity(quantity: int) void
	        +getUnitPrice() double
	        +setUnitPrice(unitPrice: double) void
	        +getSubTotal() double
	        +setSubTotal(subTotal: double) void
	        +toCsvLine() String
	        +fromCsvLine(csv: String) void
        }

        class OrderTransaction {
	        -orderId: String
	        -threadName: String
	        -mechanism: LockMechanism
	        -success: boolean
	        -retryCount: int
	        -executionTimeMs: long
	        -negativeWriteTimeMs: long
	        -stockBefore: int
	        -stockAfter: int
	        -versionBefore: int
	        -versionAfter: int
	        -message: String
	        +getOrderId() String
	        +setOrderId(orderId: String) void
	        +getThreadName() String
	        +setThreadName(threadName: String) void
	        +getMechanism() LockMechanism
	        +setMechanism(mechanism: LockMechanism) void
	        +isSuccess() boolean
	        +setSuccess(success: boolean) void
	        +getRetryCount() int
	        +setRetryCount(retryCount: int) void
	        +getExecutionTimeMs() long
	        +setExecutionTimeMs(executionTimeMs: long) void
	        +getNegativeWriteTimeMs() long
	        +setNegativeWriteTimeMs(negativeWriteTimeMs: long) void
	        +getStockBefore() int
	        +setStockBefore(stockBefore: int) void
	        +getStockAfter() int
	        +setStockAfter(stockAfter: int) void
	        +getVersionBefore() int
	        +setVersionBefore(versionBefore: int) void
	        +getVersionAfter() int
	        +setVersionAfter(versionAfter: int) void
	        +getMessage() String
	        +setMessage(message: String) void
	        +toCsvLine() String
	        +fromCsvLine(csv: String) void
        }

        class CartItem {
	        -customerId: String
	        -flashItemId: String
	        -productId: String
	        -quantity: int
	        +getCustomerId() String
	        +setCustomerId(customerId: String) void
	        +getFlashItemId() String
	        +setFlashItemId(flashItemId: String) void
	        +getProductId() String
	        +setProductId(productId: String) void
	        +getQuantity() int
	        +setQuantity(quantity: int) void
	        +toCsvLine() String
	        +fromCsvLine(csv: String) void
        }

        class User {
	        -username: String
	        -passwordHash: String
	        -role: UserRole
	        -active: boolean
	        +getUsername() String
	        +setUsername(username: String) void
	        +getPasswordHash() String
	        +setPasswordHash(passwordHash: String) void
	        +getRole() UserRole
	        +setRole(role: UserRole) void
	        +isActive() boolean
	        +setActive(active: boolean) void
	        +toCsvLine() String
	        +fromCsvLine(csv: String) void
        }

        class Payment {
	        -orderId: String
	        -customerId: String
	        -paymentMethod: PaymentMethod
	        -amount: double
	        +getOrderId() String
	        +setOrderId(orderId: String) void
	        +getCustomerId() String
	        +setCustomerId(customerId: String) void
	        +getPaymentMethod() PaymentMethod
	        +setPaymentMethod(paymentMethod: PaymentMethod) void
	        +getAmount() double
	        +setAmount(amount: double) void
	        +toCsvLine() String
	        +fromCsvLine(csv: String) void
        }

	}

    namespace ENUM {
        class SaleStatus {
	        +UPCOMING
	        +ACTIVE
	        +ENDED
	        +DISABLED
        }

        class CustomerTier {
	        +NORMAL
	        +VIP
	        +PREMIUM
        }

        class UserRole {
	        +CUSTOMER
	        +SELLER
	        +ADMIN
        }

        class OrderStatus {
	        +PENDING
	        +SUCCESS
	        +FAILED
	        +CANCELLED
        }

        class LockMechanism {
	        +NO_LOCK
	        +SYNCHRONIZED
	        +FILE_LOCK
	        +OPTIMISTIC_LOCK
        }

        class PaymentMethod {
	        +CASH
	        +BANKING
        }

	}

	namespace REPOSITORY {
        class CsvRepository {
	        #filePath: String
	        #lock: Object
			#mapFromCsv(csvLine: String) T
	        +findAll() List~T~
	        +findById(id: String) T
	        +save(entity: T) void
	        +update(updatedEntity: T) boolean
	        +delete(id: String) boolean
			#rewriteFile(entities: List~T~) void
        }

        class UserRepository {
	        #mapFromCsv(csvLine: String) User
        }

		class CustomerRepository {
	        #mapFromCsv(csvLine: String) Customer
	        +updateAddress(customerId: String, address: String) boolean
        }

        class FlashSaleItemRepository {
	        -lockFilePath: String
	        -lastOptimisticRetryCount: ThreadLocal~Integer~
			#mapFromCsv(csvLine: String) FlashSaleItem
	        +sellWithNoLock(flashItemId: String, quantity: int) boolean
	        +sellWithSynchronized(flashItemId: String, quantity: int) boolean
	        +sellWithFileLock(flashItemId: String, quantity: int) boolean
	        +sellWithOptimisticLock(flashItemId: String, quantity: int) boolean
	        +getLastOptimisticRetryCount() int
	        +generateNextId() String
        }

        class FlashSaleRepository {
	        #mapFromCsv(csvLine: String) FlashSaleEvent
	        +generateNextId() String
        }

        class OrderRepository {
	        #mapFromCsv(csvLine: String) Order
			+getPurchasedQuantity(customerId: String, flashItemId: String) int
			+generateNextId() String
		}

		class OrderDetailRepository {
	        #mapFromCsv(csvLine: String) OrderDetail
	        +generateNextId() String
		}

        class OrderTransactionRepository {
	        +BENCHMARK_HEADER: String
	        #mapFromCsv(csvLine: String) OrderTransaction
	        +ensureBenchmarkHeader() void
	        +clearFile() void
        }

        class ProductRepository {
	        #mapFromCsv(csvLine: String) Product
	        +searchByKeyword(keyword: String) List~Product~
	        +generateNextId() String
        }

		class PaymentRepository {
	        #mapFromCsv(csvLine: String) Payment
	        +generateNextId() String
        }

        class CartRepository {
	        #mapFromCsv(csvLine: String) CartItem
	        +findByCustomerId(customerId: String) List~CartItem~
	        +addOrMergeItem(customerId: String, flashItemId: String, productId: String, quantity: int) CartItem
	        +removeCustomerItem(customerId: String, cartItemId: String) boolean
	        +clearByCustomerId(customerId: String) void
	        +generateNextId() String
        }

	}

	namespace CONTROLLER {
        class AuthController {
	        -userRepository: UserRepository
	        -authView: AuthView
	        -currentUser: User
	        +login() boolean
	        +loginAs(expectedRole: UserRole) User
	        +validateLogin() User
	        +register() void
	        +register(role: UserRole) void
	        +logout() void
	        +changePassword() void
	        +getCurrentUser() User
	        +approveAccount(userId: String) boolean
	        +suspendAccount(userId: String) boolean
	        +getUserById(userId: String) User
	        +getAllUsers() List~User~
	        +updateUserAccount(userId: String, username: String, role: UserRole) boolean
        }

        class ProductController {
	        -productRepository: ProductRepository
	        -userRepository: UserRepository
	        +getAllProducts() List~Product~
	        +getProductById(id: String) Product
	        +searchProducts(keyword: String) List~Product~
	        +getVisibleProductMap() Map~String Product~
	        +getProductsBySellerId(sellerId: String) List~Product~
	        +getAnyProductById(id: String) Product
	        +isVisibleForCustomers(product: Product) boolean
	        +createProduct(newProduct: Product) boolean
	        +createProduct(newProduct: Product, sellerId: String) boolean
	        +updateProduct(product: Product) boolean
	        +updateProductInfo(id: String, name: String, category: String) boolean
	        +updateProductPrice(id: String, price: double) boolean
	        +deleteProduct(id: String) boolean
        }

        class FlashSaleController {
	        -flashSaleRepository: FlashSaleRepository
	        -flashSaleItemRepository: FlashSaleItemRepository
	        -productRepository: ProductRepository
	        -userRepository: UserRepository
	        +getAllEvents() List~FlashSaleEvent~
	        +getEventById(eventId: String) FlashSaleEvent
	        +getFlashItemById(flashItemId: String) FlashSaleItem
	        +getFlashItemsByEventId(eventId: String) List~FlashSaleItem~
	        +getActiveFlashItemsByEventId(eventId: String) List~FlashSaleItem~
	        +createEvent(name: String) boolean
	        +createEvent(ignoredId: String, name: String) boolean
	        +updateEventStatus(eventId: String, status: SaleStatus) boolean
	        +updateEventName(eventId: String, newName: String) boolean
	        +updateEvent(eventId: String, name: String, status: SaleStatus, startTime: LocalDateTime, endTime: LocalDateTime) boolean
	        +assignProductToEvent(eventId: String, productId: String, flashPrice: double, limitedQty: int) FlashSaleItem
	        +validateAssignProduct(productId: String, limitedQty: int) Product
	        +getProductById(productId: String) Product
	        +updateFlashItem(eventId: String, productId: String, sellerId: String, discountPercent: double, limitedQty: int) boolean
        }

        class OrderController {
	        -flashSaleItemRepository: FlashSaleItemRepository
	        -customerRepository: CustomerRepository
	        -orderRepository: OrderRepository
	        -orderDetailRepository: OrderDetailRepository
	        -paymentRepository: PaymentRepository
	        -productRepository: ProductRepository
	        -userRepository: UserRepository
	        +placeFLashItem(customerId: String, flashItemId: String, quantity: int, mechanism: LockMechanism) String
	        +placeRegularOrder(customerId: String, productId: String, quantity: int, mechanism: LockMechanism) String
	        +placeRegularCartOrder(customerId: String, products: Map~String Integer~, mechanism: LockMechanism) String
	        +placeFLashItemCart(customerId: String, cart: Map~String Integer~, mechanism: LockMechanism) String
	        +checkoutCart(customerId: String, cartItems: List~CartItem~, mechanism: LockMechanism) List~String~
	        +getCustomerByUserId(userId: String) Customer
	        +getPurchasedQuantity(customerId: String, flashItemId: String) int
	        +getOrdersByCustomer(customerId: String) List~Order~
	        +getOrderById(orderId: String) Order
	        +getOrderDetailsByOrderId(orderId: String) List~OrderDetail~
	        +getProductNamesByOrderDetails(details: List~OrderDetail~) Map~String String~
	        +getSellerNamesByOrderDetails(details: List~OrderDetail~) Map~String String~
	        +getOrdersForSeller(sellerId: String) List~Order~
	        +getSellerOrderProductSummaryByOrder(sellerId: String, orders: List~Order~) Map~String String~
	        +getPendingOrdersForSeller(sellerId: String) List~Order~
	        +confirmOrderForSeller(orderId: String, sellerId: String) boolean
	        +cancelOrderForSeller(orderId: String, sellerId: String) boolean
	        +confirmOrder(orderId: String) boolean
	        +createPayment(orderId: String, method: PaymentMethod) boolean
	        +generatePaymentId() String
        }

        class CartController {
	        -cartRepository: CartRepository
	        -scanner: Scanner
	        -orderController: OrderController
	        -productController: ProductController
	        -flashSaleController: FlashSaleController
	        -productView: ProductView
	        -orderView: OrderView
	        -flashSaleView: FlashSaleView
	        +getCartByCustomer(customerId: String) List~CartItem~
	        +addItem(customerId: String, flashItemId: String, productId: String, quantity: int) void
	        +removeItem(customerId: String, cartItemId: String) boolean
	        +clearCart(customerId: String) void
	        +addFlashSaleItemsToCart(currentUser: User, selectedEventId: String, mechanism: LockMechanism) void
	        +addRegularProductsToCart(currentUser: User, mechanism: LockMechanism) void
	        +addRegularProductsToCart(currentUser: User, selectableProducts: List~Product~, mechanism: LockMechanism) void
	        +showCart(currentUser: User, mechanism: LockMechanism) void
	        +checkoutCart(customerId: String, mechanism: LockMechanism) void
        }

        class SimulatorController {
	        -flashSaleRepository: FlashSaleRepository
	        -orderRepository: OrderRepository
	        -orderTransactionRepository: OrderTransactionRepository
	        -productRepository: ProductRepository
	        -flashSaleItemRepository: FlashSaleItemRepository
	        -paymentRepository: PaymentRepository
	        -userRepository: UserRepository
	        -customerRepository: CustomerRepository
	        -fileLockGate: Object
	        -simulatorView: SimulatorView
	        -latestTransactions: List~OrderTransaction~
	        -latestSummaries: List~SimulationSummary~
	        +startSimulation() void
	        +startSimulation(threadCount: int) List~SimulationSummary~
	        +startSimulation(threadCount: int, selectedMechanism: LockMechanism) List~SimulationSummary~
	        +createThreads() void
	        +runConcurrentOrders() void
	        +measureTPS() double
	        +compareLockMechanisms() void
	        +generateSimulationReport() void
	        +getDashboardStats() Map~String Integer~
	        +getInventoryReport() List~FlashSaleItem~
	        +getNegativeStockItems() List~FlashSaleItem~
	        +getRetryRate() double
	        +getNegativeStockRate() double
	        +exportSimulationResult(threadCount: int, lock: LockMechanism) boolean
	        +getAllTransactions() List~OrderTransaction~
	        +getLatestTransactions() List~OrderTransaction~
	        +getLatestElapsedMs() long
	        +getLatestValidSuccessCount() int
	        +getLatestRawSuccessCount() int
        }

        class SimulationSummary {
	        -scenarioName: String
	        -mechanism: LockMechanism
	        -threadCount: int
	        -productCount: int
	        -stockLimit: int
	        -successCount: int
	        -failureCount: int
	        -retryCount: int
	        -negativeCount: int
	        -elapsedNanos: long
	        -averageExecutionTimeMs: double
	        -maxExecutionTimeMs: long
	        -finalSoldQty: int
	        +getMechanism() LockMechanism
	        +getScenarioName() String
	        +getThreadCount() int
	        +getStockLimit() int
	        +getProductCount() int
	        +getSuccessCount() int
	        +getFailureCount() int
	        +getRetryCount() int
	        +getNegativeCount() int
	        +getElapsedNanos() long
	        +getElapsedMs() long
	        +getFinalSoldQty() int
	        +getAverageExecutionTimeMs() double
	        +getMaxExecutionTimeMs() long
	        +getTps() double
	        +getValidSuccessCount() int
	        +getValidTps() double
	        +isOversold() boolean
	        +getGoal() String
	        +toExportLine() String
	        +toSummaryCsvLine() String
        }

	}

	namespace VIEW {
        class FlashSaleView {
	        -scanner: Scanner
	        +inputEventId() String
	        +inputNewEventName() String
	        +inputNewEventNameForCreation() String
	        +inputDateTime(label: String, currentValue: LocalDateTime) LocalDateTime
	        +inputFlashItemId() String
	        +inputEventStatus() SaleStatus
	        +displayEventSelection(events: List~FlashSaleEvent~) void
	        +displayActiveEvents(events: List~FlashSaleEvent~) void
	        +displayFlashSaleItemsInTable(items: List~FlashSaleItem~, pc: ProductController) void
	        +displayEventList(events: List~FlashSaleEvent~) void
	        +displayFlashItem(item: FlashSaleItem) void
	        +displayInventoryReport(items: List~FlashSaleItem~) void
	        +displayNegativeStockReport(items: List~FlashSaleItem~) void
	        +displayFlashSaleEvents(eventInfo: String) void
	        +displayFlashSaleItems(itemInfo: String) void
	        +showCreateEventResult(success: boolean) void
	        +showUpdateEventResult(success: boolean) void
	        +showUpdateEventNameResult(success: boolean) void
	        +showAssignProductResult(success: boolean, item: FlashSaleItem) void
	        +showEventNotFound() void
	        +showProductNotFound() void
	        +showExceedStockError(stockQty: int) void
	        +showCreateEventSuccess() void
	        +showUpdateStockResult(success: boolean) void
	        +formatFlashItem(item: FlashSaleItem) String
        }

        class OrderView {
	        -scanner: Scanner
	        +inputOrderId() String
	        +inputCustomerId() String
	        +inputFlashItemId() String
	        +inputProductId() String
	        +inputQuantity() int
	        +inputOrderType() int
	        +inputPaymentMethod() PaymentMethod
	        +displayOrder(order: Order) void
	        +displayOrder(order: Order, eventName: String) void
	        +displayOrderHistory(orders: List~Order~) void
	        +displaySellerOrderReview(orders: List~Order~, productSummaryByOrder: Map~String String~) void
	        +displayBuyerOrderHistory(orders: List~Order~) void
	        +displayBuyerOrderHistory(orders: List~Order~, eventNames: Map~String String~) void
	        +displayOrderDetails(details: List~OrderDetail~, productNames: Map~String String~, sellerNames: Map~String String~, eventNames: Map~String String~) void
	        +displayInventoryResult(item: FlashSaleItem) void
	        +displayPurchaseLimitResult(purchasedQty: int) void
	        +showOrderSuccess() void
	        +showOrderFailure() void
	        +showOrderNotFound() void
	        +showConfirmOrderResult(success: boolean) void
	        +showPaymentSaved() void
	        +showPlaceOrderError(message: String) void
        }

        class AuthView {
	        -scanner: Scanner
	        +inputUsername() String
	        +inputPassword() String
	        +inputNewPassword() String
	        +inputFullName() String
	        +inputPhone() String
	        +inputEmail() String
	        +inputUserRole() UserRole
	        +showLoginSuccess() void
	        +showLoginFailed() void
	        +showRegisterSuccess() void
	        +showUsernameExists() void
	        +showRegisterValidationError(message: String) void
	        +showLogoutSuccess() void
	        +showChangePasswordSuccess() void
	        +showChangePasswordFailed() void
        }

        class ProductView {
	        -scanner: Scanner
	        +inputProductData() Product
	        +inputProductId() String
	        +inputKeyword() String
	        +inputNewName() String
	        +inputNewCategory() String
	        +inputNewPrice() double
	        +inputCategory() String
	        +displayProducts(products: List~Product~) void
	        +displaySearchResults(products: List~Product~) void
	        +displayProductDetail(product: Product) void
	        +showProductNotFound() void
	        +showAddProductResult(success: boolean) void
	        +showUpdateProductResult(success: boolean) void
	        +showDeleteProductResult(success: boolean) void
	        +showEditInfoResult(success: boolean) void
	        +showEditPriceResult(success: boolean) void
        }

        class SimulatorView {
	        -scanner: Scanner
	        +inputLockMechanism() LockMechanism
	        +inputThreadCount() int
	        +showDashboard(counts: Map~String Integer~, threadCount: int, lock: LockMechanism) void
	        +showInventoryReport(items: List~FlashSaleItem~) void
	        +showNegativeStockReport(items: List~FlashSaleItem~) void
	        +showThroughputReport(total: int, rawSuccess: int, validSuccess: int, totalMs: long, tps: double) void
	        +showRetryRate(rate: double) void
	        +showNegativeStockRate(rate: double) void
	        +showThreadCountConfigured(count: int) void
	        +showLockMechanismConfigured(lock: LockMechanism) void
	        +showSimulationHeader(threadCount: int, lock: LockMechanism) void
	        +showBenchmarkDashboard(threadCount: int, scenarioCount: int, initialStock: int, orderCount: int, repeatRuns: int) void
	        +showBenchmarkProgressStart(mechanism: LockMechanism) void
	        +showBenchmarkProgressDone() void
	        +showExportResult(success: boolean, path: String) void
	        +displaySimulationResult() void
	        +displaySimulationResult(transactions: List~OrderTransaction~) void
	        +displayTPSReport() void
	        +displayTPSReport(tps: double) void
	        +displayRaceConditionResult() void
	        +displayRaceConditionResult(message: String) void
	        +displayLockComparison() void
	        +displayLockComparison(successCountByMechanism: Map~LockMechanism Integer~) void
	        +displayLockComparison(summaries: List~SimulationSummary~) void
	        +displayResearchQuestionEvaluation(summaries: List~SimulationSummary~) void
        }

        class UserView {
	        -scanner: Scanner
	        +inputUserId() String
	        +inputUserIdRequired() String
	        +inputNewUsername() String
	        +inputUserRole() UserRole
	        +displayUser(user: User) void
	        +displayUserList(users: List~User~) void
	        +showUserNotFound() void
	        +showAccountStatusResult(success: boolean) void
	        +showAccountUpdateResult(success: boolean) void
        }

	}

	namespace EXCEPTION {
        class FlashSaleException {
	        +FlashSaleException(message: String)
        }

        class FlashSaleExpiredException {
	        +FlashSaleExpiredException(message: String)
        }

        class InvalidQuantityException {
	        +InvalidQuantityException(message: String)
        }

        class OutOfStockException {
	        +OutOfStockException(message: String)
        }

        class PurchaseLimitExceededException {
	        +PurchaseLimitExceededException(message: String)
        }

        class VersionConflictException {
	        +VersionConflictException(message: String)
        }

	}

	<<abstract>> BaseEntity
	<<abstract>> CsvRepository
	<<enumeration>> SaleStatus
	<<enumeration>> CustomerTier
	<<enumeration>> OrderStatus
	<<enumeration>> LockMechanism
	<<enumeration>> PaymentMethod
    <<enumeration>> UserRole

    BaseEntity <|-- Product
    BaseEntity <|-- FlashSaleEvent
    BaseEntity <|-- FlashSaleItem
    BaseEntity <|-- Customer
    BaseEntity <|-- Order
    BaseEntity <|-- OrderDetail
    BaseEntity <|-- OrderTransaction
    BaseEntity <|-- CartItem
    BaseEntity <|-- User
    BaseEntity <|-- Payment

    CsvRepository <|-- ProductRepository
    CsvRepository <|-- OrderRepository
    CsvRepository <|-- FlashSaleRepository
    CsvRepository <|-- FlashSaleItemRepository
    CsvRepository <|-- UserRepository
    CsvRepository <|-- CustomerRepository
	CsvRepository <|-- PaymentRepository
	CsvRepository <|-- OrderDetailRepository
	CsvRepository <|-- OrderTransactionRepository
	CsvRepository <|-- CartRepository

    FlashSaleException <|-- FlashSaleExpiredException
    FlashSaleException <|-- InvalidQuantityException
    FlashSaleException <|-- OutOfStockException
    FlashSaleException <|-- PurchaseLimitExceededException
    FlashSaleException <|-- VersionConflictException

    Main ..> AuthController
    Main ..> CartController
    Main ..> ProductController
    Main ..> FlashSaleController
    Main ..> OrderController
    Main ..> SimulatorController
```
