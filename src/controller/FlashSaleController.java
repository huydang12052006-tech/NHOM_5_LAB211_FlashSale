package controller;

import java.time.LocalDateTime;
import java.util.List;

import model.Entity.FlashSaleEvent;
import model.Entity.FlashSaleItem;
import model.Entity.Product;
import model.Enum.SaleStatus;
import repository.FlashSaleItemRepository;
import repository.FlashSaleRepository;
import repository.ProductRepository;

public class FlashSaleController {

    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final ProductRepository productRepository;

    public FlashSaleController(FlashSaleRepository flashSaleRepository,
                               FlashSaleItemRepository flashSaleItemRepository,
                               ProductRepository productRepository) {
        this.flashSaleRepository = flashSaleRepository;
        this.flashSaleItemRepository = flashSaleItemRepository;
        this.productRepository = productRepository;
    }

    // ==================================
    // Read
    // ==================================

    public List<FlashSaleEvent> getAllEvents() {
        return flashSaleRepository.findAll();
    }

    public FlashSaleEvent getEventById(String eventId) {
        return flashSaleRepository.findById(eventId);
    }

    public FlashSaleItem getFlashItemById(String flashItemId) {
        return flashSaleItemRepository.findById(flashItemId);
    }

    public List<FlashSaleItem> getFlashItemsByEventId(String eventId) {
        List<FlashSaleItem> result = new java.util.ArrayList<>();
        for (FlashSaleItem item : flashSaleItemRepository.findAll()) {
            if (item.getEventId().equals(eventId)) {
                result.add(item);
            }
        }
        return result;
    }

    // ==================================
    // Create Event
    // ==================================

    public boolean createEvent(String id, String name) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now;
        LocalDateTime end = start.plusHours(2);

        FlashSaleEvent event = new FlashSaleEvent(id, now, now, name, start, end, SaleStatus.UPCOMING);
        flashSaleRepository.save(event);
        return true;
    }

    // ==================================
    // Update Event
    // ==================================

    public boolean updateEventStatus(String eventId, SaleStatus status) {
        FlashSaleEvent event = flashSaleRepository.findById(eventId);

        if (event == null) {
            return false;
        }

        event.setStatus(status);
        event.setUpdatedAt(LocalDateTime.now());
        return flashSaleRepository.update(event);
    }

    public boolean updateEventName(String eventId, String newName) {
        FlashSaleEvent event = flashSaleRepository.findById(eventId);

        if (event == null) {
            return false;
        }

        event.setEventName(newName);
        event.setUpdatedAt(LocalDateTime.now());
        return flashSaleRepository.update(event);
    }

    // ==================================
    // Assign Product to Event
    // ==================================

    /**
     * Validates business rules and creates a FlashSaleItem linking product to event.
     *
     * @return the created FlashSaleItem, or null if validation fails (check errorCode).
     */
    public FlashSaleItem assignProductToEvent(String eventId,
                                              String productId,
                                              double flashPrice,
                                              int limitedQty) {

        FlashSaleEvent event = flashSaleRepository.findById(eventId);
        if (event == null) {
            return null; // event not found
        }

        Product product = productRepository.findById(productId);
        if (product == null) {
            return null; // product not found
        }

        if (limitedQty > product.getStockQty()) {
            // caller must check: limitedQty > product.getStockQty() beforehand
            return null;
        }

        double discountPercent = Math.round(
                (1.0 - flashPrice / product.getOriginalPrice()) * 10000.0) / 100.0;

        LocalDateTime now = LocalDateTime.now();
        String itemId = flashSaleItemRepository.generateNextId();

        FlashSaleItem item = new FlashSaleItem(
                itemId, now, now,
                eventId, productId,
                flashPrice, limitedQty, 0,
                discountPercent, 1,
                SaleStatus.ACTIVE
        );

        flashSaleItemRepository.save(item);
        return item;
    }

    /**
     * Validates that limitedQty does not exceed product stock.
     * Returns the product if valid, null if product not found or qty exceeds stock.
     */
    public Product validateAssignProduct(String productId, int limitedQty) {
        Product product = productRepository.findById(productId);
        if (product == null) {
            return null;
        }
        if (limitedQty > product.getStockQty()) {
            return null;
        }
        return product;
    }

    public Product getProductById(String productId) {
        return productRepository.findById(productId);
    }
}
