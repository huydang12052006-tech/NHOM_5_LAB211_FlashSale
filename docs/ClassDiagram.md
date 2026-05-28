``` mermaid
classDiagram
direction LR

%% =========================
%% BASE ENTITY
%% =========================
namespace MODEL{
class BaseEntity {
    <<abstract>>

    -id: String
    -createdAt: LocalDateTime
    -updatedAt: LocalDateTime

    +toCsvLine() String
    +fromCsvLine(csv: String) void
}

%% =========================
%% ENTITIES
%% =========================

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
    -id: String
    -username: String
    -passwordHash: String
    -role: String
    -active: boolean
    -createdAt: LocalDateTime
    -updatedAt: LocalDateTime

    +getId() String
    +setId(id: String) void

    +getUsername() String
    +setUsername(username: String) void

    +getPasswordHash() String
    +setPasswordHash(passwordHash: String) void

    +getRole() String
    +setRole(role: String) void

    +isActive() boolean
    +setActive(active: boolean) void

    +getCreatedAt() LocalDateTime
    +setCreatedAt(createdAt: LocalDateTime) void

    +getUpdatedAt() LocalDateTime
    +setUpdatedAt(updatedAt: LocalDateTime) void
}

class Payment {
    -id: String
    -orderId: String
    -customerId: String
    -paymentMethod: PaymentMethod
    -amount: double
    -createdAt: LocalDateTime

    +getId() String
    +setId(id: String) void

    +getOrderId() String
    +setOrderId(orderId: String) void

    +getCustomerId() String
    +setCustomerId(customerId: String) void

    +getPaymentMethod() PaymentMethod
    +setPaymentMethod(paymentMethod: PaymentMethod) void

    +getAmount() double
    +setAmount(amount: double) void

    +getCreatedAt() LocalDateTime
    +setCreatedAt(createdAt: LocalDateTime) void
}
}
%% =========================
%% ENUMS
%% =========================
namespace ENUM{
class SaleStatus {
    <<enumeration>>

    +UPCOMING: SaleStatus
    +ACTIVE: SaleStatus
    +ENDED: SaleStatus
    +DISABLED: SaleStatus
}

class CustomerTier {
    <<enumeration>>

    +NORMAL: CustomerTier
    +VIP: CustomerTier
    +PREMIUM: CustomerTier
}

class OrderStatus {
    <<enumeration>>

    +PENDING: OrderStatus
    +SUCCESS: OrderStatus
    +FAILED: OrderStatus
    +CANCELLED: OrderStatus
}

class LockMechanism {
    <<enumeration>>

    +NO_LOCK: LockMechanism
    +SYNCHRONIZED: LockMechanism
    +FILE_LOCK: LockMechanism
    +OPTIMISTIC_LOCK: LockMechanism
}

class PaymentMethod {
    <<enumeration>>

    +CASH: PaymentMethod
    +BANKING: PaymentMethod
}
}
%% =========================
%% REPOSITORY LAYER
%% =========================
namespace REPOSITORY{
class CsvRepository {
    -filePath: String

    +findAll(): List<T>
    +findById(id: String): T
    +save(entity: T): void
    +update(entity: T): void
    +delete(id: String): void

    +readAll() List<T>
    +writeAll(data: List<T>) void
}

class FlashSaleRepository {
    -csvRepository: CsvRepository

    +saveEvent(event: FlashSaleEvent) void
    +updateEvent(event: FlashSaleEvent) void
    +findEventById(id: String) FlashSaleEvent
    +findAllEvents() List~FlashSaleEvent~

    +saveFlashItem(item: FlashSaleItem) void
    +updateFlashItem(item: FlashSaleItem) void
    +findFlashItemsByEvent(eventId: String) List~FlashSaleItem~
}

class OrderRepository {
    -csvRepository: CsvRepository

    +saveOrder(order: Order) void
    +saveOrderDetail(detail: OrderDetail) void
    +findOrderById(id: String) Order
    +findOrdersByCustomer(customerId: String) List~Order~
    +updateOrderStatus(id: String,status: OrderStatus) void
}

class ProductRepository {
    -csvRepository: CsvRepository

    +saveProduct(product: Product) void
    +updateProduct(product: Product) void
    +deleteProduct(id: String) boolean
    +findProductById(id: String) Product
    +findAllProducts() List~Product~
    +findByCategory(category: String) List~Product~
}
}
%% =========================
%% CONTROLLER LAYER
%% =========================
namespace CONTROLLER{
class FlashSaleController {
    -flashSaleRepository: FlashSaleRepository
    -productRepository: ProductRepository
    +createFlashSaleEvent() void
    +addProductToEvent()() void
    +deleteFlashSaleEvent() void
}

class OrderController {
    -orderRepository: OrderRepository
    -orderView: OrderView
    +placeOrder() void
    +cancelOrder() void
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
}

%% =========================
%% VIEW LAYER
%% =========================
namespace VIEW{
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


