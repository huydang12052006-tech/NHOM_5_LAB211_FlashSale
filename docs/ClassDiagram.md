``` mermaid
classDiagram
direction LR
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
        }

        class Product {
	        -name: String
	        -category: String
	        -originalPrice: double
	        -stockQty: int
	        -version: int
	        -active: boolean
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
	        +isActive() boolean
	        +setActive(active: boolean) void
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
        }

        class Customer {
	        -fullName: String
	        -phone: String
	        -email: String
	        -tier: CustomerTier
	        -totalSpent: double
	        -active: boolean
	        +getFullName() String
	        +setFullName(fullName: String) void
	        +getPhone() String
	        +setPhone(phone: String) void
	        +getEmail() String
	        +setEmail(email: String) void
	        +getTier() CustomerTier
	        +setTier(tier: CustomerTier) void
	        +getTotalSpent() double
	        +setTotalSpent(totalSpent: double) void
	        +isActive() boolean
	        +setActive(active: boolean) void
        }

        class Order {
	        -customerId: String
	        -eventId: String
	        -totalAmount: double
	        -status: OrderStatus
	        -lockMechanism: LockMechanism
	        +getCustomerId() String
	        +setCustomerId(customerId: String) void
	        +getEventId() String
	        +setEventId(eventId: String) void
	        +getTotalAmount() double
	        +setTotalAmount(totalAmount: double) void
	        +getStatus() OrderStatus
	        +setStatus(status: OrderStatus) void
	        +getLockMechanism() LockMechanism
	        +setLockMechanism(lockMechanism: LockMechanism) void
        }

        class OrderDetail {
	        -orderId: String
	        -flashItemId: String
	        -quantity: int
	        -unitPrice: double
	        -subTotal: double
	        +getOrderId() String
	        +setOrderId(orderId: String) void
	        +getFlashItemId() String
	        +setFlashItemId(flashItemId: String) void
	        +getQuantity() int
	        +setQuantity(quantity: int) void
	        +getUnitPrice() double
	        +setUnitPrice(unitPrice: double) void
	        +getSubTotal() double
	        +setSubTotal(subTotal: double) void
        }

        class OrderTransaction {
	        -orderId: String
	        -threadName: String
	        -mechanism: LockMechanism
	        -success: boolean
	        -retryCount: int
	        -executionTimeMs: long
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
	        +getMessage() String
	        +setMessage(message: String) void
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
	        +getRole() String
	        +setRole(role: String) void
	        +isActive() boolean
	        +setActive(active: boolean) void
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
}

	}
namespace ENUM {
        class SaleStatus {
	        +UPCOMING: SaleStatus
	        +ACTIVE: SaleStatus
	        +ENDED: SaleStatus
	        +DISABLED: SaleStatus
        }

        class CustomerTier {
	        +NORMAL: CustomerTier
	        +VIP: CustomerTier
	        +PREMIUM: CustomerTier
        }

        class UserRole {
	        +CUSTOMER: UserRole
	        +SELLER: UserRole
	        +ADMIN: UserRole
        }

        class OrderStatus {
	        +PENDING: OrderStatus
	        +SUCCESS: OrderStatus
	        +FAILED: OrderStatus
	        +CANCELLED: OrderStatus
        }

        class LockMechanism {
	        +NO_LOCK: LockMechanism
	        +SYNCHRONIZED: LockMechanism
	        +FILE_LOCK: LockMechanism
	        +OPTIMISTIC_LOCK: LockMechanism
        }

        class PaymentMethod {
	        +CASH: PaymentMethod
	        +BANKING: PaymentMethod
        }

	}

	namespace REPOSITORY {
        class CsvRepository {
	        -filePath: String
	        +findAll() List~T~
	        +findById(id: String) T
	        +save(entity: T) void
	        +update(entity: T) void
	        +delete(id: String) void
        }

        class UserRepository {
	        +findAll() List~User~
	        +findById(id: String) User
	        +existsByUsername(username: String) boolean
	        +login(username: String,password: String) User
	        +register(user: User) void
	        +changePassword(userId: String,newPassword: String) void
        }

		class CustomerRepository {
	        +findAll() List~Customer~
	        +findById(id: String) Customer
	        +save(entity: ) void
	        +update(customer: Customer) void
	        +delete(id: String) void
			        }

        class FlashSaleItemRepository {
	        +sellWithNoLock(String flashItemId,int quantity) boolean
	        +sellWithSynchronized(String flashItemId,int quantity) boolean
	        +sellWithFileLock(String flashItemId,int quantity) boolean
	        +sellWithOptimisticLock(String flashItemId,int quantity) boolean
        }

        class FlashSaleRepository {
	        +findById(id: String) FlashSaleEvent
	        +findAll() List~FlashSaleEvent~
	        +save(item: FlashSaleItem) void
	        +update(item: FlashSaleItem) void
	        +delete(id: String) void
        }

        class OrderRepository {
	        +save(order: Order) void
	        +findById(id: String) Order
	        +findAll(id: String) List~Order~
	        +delete(id: String) void
	        +findOrdersByCustomer(customerId: String) List~Order~
	        +update(id: String,status: OrderStatus) void
        }

        class ProductRepository {
	        +save(product: Product) void
	        +update(product: Product) void
	        +delete(id: String) void
	        +findById(id: String) Product
	        +findAll() List~Product~
	        +findByCategory(category: String) List~Product~
        }

	}
	
	namespace CONTROLLER {
        class OrderController {
	        -orderRepository: OrderRepository
	        -orderView: OrderView
	        +placeOrder() void
	        +cancelOrder() void
        }

        class AuthController {
    -userRepository: UserRepository
    -authView: AuthView

    +login() boolean
    +register() void
    +logout() void
    +changePassword() void
}

        class ProductController {
	        -productRepository: ProductRepository
	        -productView: ProductView
	        +createProduct() void
	        +updateProduct() void
	        +deleteProduct() void
	        +searchProductByCategory() void
        }


        class SimulatorController {
	        -flashSaleRepository: FlashSaleRepository
	        -orderRepository: OrderRepository
	        -simulatorView: SimulatorView
	        +startSimulation() void
	        +createThreads() void
	        +runConcurrentOrders() void
	        +measureTPS() double
	        +compareLockMechanisms() void
	        +generateSimulationReport() void
        }

        class FlashSaleController {
    -flashSaleRepository: FlashSaleRepository
    -productRepository: ProductRepository
    +createFlashSaleEvent() void
    +addProductToEvent() void
    +deleteFlashSaleEvent() void
}

        }

	
	namespace VIEW {
        class FlashSaleView {
	        +displayFlashSaleEvents() void
	        +displayFlashSaleItems() void
	        +showCreateEventSuccess() void
	        +showUpdateStockResult() void
        }

        class OrderView {
	        +displayOrder() void
	        +displayOrderHistory() void
	        +showOrderSuccess() void
	        +showOrderFailure() void
        }

        class AuthView {
    -scanner: Scanner

    +inputUsername() String
    +inputPassword() String
    +showLoginSuccess() void
    +showLoginFailed() void
    +showRegisterSuccess() void
    +showUsernameExists() void
}

        class ProductView {
	        -scanner: Scanner
	        +displayProducts() void
	        +displayProductDetail() void
	        +inputProductData() Product
	        +inputCategory() String
        }

        class SimulatorView {
	        +displaySimulationResult() void
	        +displayTPSReport() void
	        +displayRaceConditionResult() void
	        +displayLockComparison() void
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
    BaseEntity <|-- User 
    BaseEntity <|-- Payment

    CsvRepository <|-- ProductRepository
    CsvRepository <|-- OrderRepository
    CsvRepository <|-- FlashSaleRepository
    CsvRepository <|-- FlashSaleItemRepository
    CsvRepository <|-- UserRepository
    CsvRepository <|-- 	CustomerRepository
