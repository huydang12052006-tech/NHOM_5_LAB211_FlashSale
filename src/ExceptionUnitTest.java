import controller.OrderController;
import exception.FlashSaleException;
import exception.FlashSaleExpiredException;
import exception.InvalidQuantityException;
import exception.OutOfStockException;
import exception.PurchaseLimitExceededException;
import exception.VersionConflictException;
import java.util.List;
import model.Entity.Customer;
import model.Entity.FlashSaleItem;
import model.Entity.Order;
import model.Entity.OrderDetail;
import model.Enum.LockMechanism;
import repository.CustomerRepository;
import repository.FlashSaleItemRepository;
import repository.OrderDetailRepository;
import repository.OrderRepository;

public class ExceptionUnitTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        CustomerRepository customerRepository = new CustomerRepository();
        FlashSaleItemRepository flashSaleItemRepository =
                new FlashSaleItemRepository();

        Customer customer = first(customerRepository.findAll(), "customer");
        FlashSaleItem flashItem =
                first(flashSaleItemRepository.findAll(), "flash item");

        OrderController controller = new OrderController();

        assertThrows(
                InvalidQuantityException.class,
                "quantity <= 0 throws InvalidQuantityException",
                () -> controller.placeOrder(
                        customer.getId(),
                        flashItem.getId(),
                        0,
                        LockMechanism.SYNCHRONIZED
                )
        );

        assertThrows(
                FlashSaleException.class,
                "unknown customer throws FlashSaleException",
                () -> controller.placeOrder(
                        "CUSTOMER_NOT_FOUND",
                        flashItem.getId(),
                        1,
                        LockMechanism.SYNCHRONIZED
                )
        );

        assertThrows(
                FlashSaleException.class,
                "unknown flash item throws FlashSaleException",
                () -> controller.placeOrder(
                        customer.getId(),
                        "FLASH_ITEM_NOT_FOUND",
                        1,
                        LockMechanism.SYNCHRONIZED
                )
        );

        PurchaseCase purchaseCase = findPurchaseLimitCase();
        assertThrows(
                PurchaseLimitExceededException.class,
                "more than 2 purchased items throws PurchaseLimitExceededException",
                () -> controller.placeOrder(
                        purchaseCase.customerId,
                        purchaseCase.flashItemId,
                        1,
                        LockMechanism.SYNCHRONIZED
                )
        );

        int tooMany = flashItem.getLimitedQty() - flashItem.getSoldQty() + 1;
        assertThrows(
                OutOfStockException.class,
                "selling more than remaining stock throws OutOfStockException",
                () -> flashSaleItemRepository.sellWithSynchronized(
                        flashItem.getId(),
                        tooMany
                )
        );

        assertThrows(
                VersionConflictException.class,
                "VersionConflictException can be thrown and caught",
                () -> {
                    throw new VersionConflictException("version conflict");
                }
        );

        assertThrows(
                FlashSaleExpiredException.class,
                "FlashSaleExpiredException can be thrown and caught",
                () -> {
                    throw new FlashSaleExpiredException("flash sale expired");
                }
        );

        System.out.println("\n====================================");
        System.out.println("EXCEPTION UNIT TEST");
        System.out.println("====================================");
        System.out.println("PASSED = " + passed);
        System.out.println("FAILED = " + failed);

        if (failed > 0) {
            throw new AssertionError("Some exception unit tests failed.");
        }
    }

    private static PurchaseCase findPurchaseLimitCase() {
        OrderRepository orderRepository = new OrderRepository();
        OrderDetailRepository detailRepository =
                new OrderDetailRepository();

        List<Order> orders = orderRepository.findAll();
        List<OrderDetail> details = detailRepository.findAll();

        for (Order order : orders) {
            for (OrderDetail detail : details) {
                if (!order.getId().equals(detail.getOrderId())) {
                    continue;
                }

                int purchasedQty = orderRepository.getPurchasedQuantity(
                        order.getCustomerId(),
                        detail.getFlashItemId()
                );

                if (purchasedQty >= 2) {
                    return new PurchaseCase(
                            order.getCustomerId(),
                            detail.getFlashItemId()
                    );
                }
            }
        }

        throw new AssertionError(
                "No customer/flash item pair with purchased quantity >= 2."
        );
    }

    private static <T> T first(List<T> values, String name) {
        if (values.isEmpty()) {
            throw new AssertionError("No " + name + " data found.");
        }

        return values.get(0);
    }

    private static void assertThrows(
            Class<? extends Throwable> expectedType,
            String testName,
            ThrowingRunnable action
    ) {
        try {
            action.run();
            fail(testName, "no exception was thrown");
        } catch (Throwable actual) {
            if (expectedType.isInstance(actual)) {
                pass(testName);
            } else {
                fail(
                        testName,
                        "expected " + expectedType.getSimpleName()
                                + " but got "
                                + actual.getClass().getSimpleName()
                                + ": "
                                + actual.getMessage()
                );
            }
        }
    }

    private static void pass(String testName) {
        passed++;
        System.out.println("[PASS] " + testName);
    }

    private static void fail(String testName, String reason) {
        failed++;
        System.out.println("[FAIL] " + testName + " -> " + reason);
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static class PurchaseCase {
        private final String customerId;
        private final String flashItemId;

        private PurchaseCase(String customerId, String flashItemId) {
            this.customerId = customerId;
            this.flashItemId = flashItemId;
        }
    }
}
