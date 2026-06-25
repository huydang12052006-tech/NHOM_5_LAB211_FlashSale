package controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import model.Entity.FlashSaleEvent;
import model.Entity.FlashSaleItem;
import model.Entity.Product;
import model.Entity.User;
import model.Enum.SaleStatus;
import repository.FlashSaleItemRepository;
import repository.FlashSaleRepository;
import repository.ProductRepository;
import repository.UserRepository;

public class FlashSaleController {

    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public FlashSaleController(FlashSaleRepository flashSaleRepository,
                               FlashSaleItemRepository flashSaleItemRepository,
                               ProductRepository productRepository) {
        this.flashSaleRepository = flashSaleRepository;
        this.flashSaleItemRepository = flashSaleItemRepository;
        this.productRepository = productRepository;
        this.userRepository = new UserRepository();
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

    /** Returns only sale items and products that customers can currently buy. */
    public List<FlashSaleItem> getActiveFlashItemsByEventId(String eventId) {
        List<FlashSaleItem> result = new java.util.ArrayList<FlashSaleItem>();
        Map<String, Product> productsById = productMap(productRepository.findAll());
        Set<String> activeSellerIds = activeUserIds();
        for (FlashSaleItem item : getFlashItemsByEventId(eventId)) {
            Product product = productsById.get(item.getProductId());
            if (item.getStatus() == SaleStatus.ACTIVE
                    && product != null && product.getStatus() == SaleStatus.ACTIVE
                    && isSellerActive(product, activeSellerIds)
                    && item.getLimitedQty() > item.getSoldQty()) {
                result.add(item);
            }
        }
        return result;
    }

    // ==================================
    // Create Event
    // ==================================

    public boolean createEvent(String name) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now;
        LocalDateTime end = start.plusHours(2);

        FlashSaleEvent event = new FlashSaleEvent(flashSaleRepository.generateNextId(), now, now, name, start, end, SaleStatus.UPCOMING);
        flashSaleRepository.save(event);
        return true;
    }

    public boolean createEvent(String ignoredId, String name) {
        return createEvent(name);
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

    public boolean updateEvent(String eventId, String name, SaleStatus status,
                               LocalDateTime startTime, LocalDateTime endTime) {
        FlashSaleEvent event = flashSaleRepository.findById(eventId);
        if (event == null || endTime.isBefore(startTime)) {
            return false;
        }
        event.setEventName(name);
        event.setStatus(status);
        event.setStartTime(startTime);
        event.setEndTime(endTime);
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
        Product product = productRepository.findById(productId);
        return isSellerActive(product) ? product : null;
    }

    private boolean isSellerActive(Product product) {
        return isSellerActive(product, activeUserIds());
    }

    private boolean isSellerActive(Product product, Set<String> activeSellerIds) {
        if (product == null || product.getSellerId() == null) {
            return false;
        }
        return activeSellerIds.contains(product.getSellerId());
    }

    private Set<String> activeUserIds() {
        Set<String> activeUserIds = new HashSet<String>();
        for (User user : userRepository.findAll()) {
            if (user.isActive()) {
                activeUserIds.add(user.getId());
            }
        }
        return activeUserIds;
    }

    private Map<String, Product> productMap(List<Product> products) {
        Map<String, Product> productsById = new LinkedHashMap<String, Product>();
        for (Product product : products) {
            productsById.put(product.getId(), product);
        }
        return productsById;
    }

    public boolean updateFlashItem(String eventId, String productId, String sellerId,
                                   double discountPercent, int limitedQty) {
        if (discountPercent < 0 || discountPercent >= 100 || limitedQty <= 0) {
            return false;
        }
        Product product = productRepository.findById(productId);
        if (product == null || !sellerId.equalsIgnoreCase(product.getSellerId())
                || limitedQty > product.getStockQty()) {
            return false;
        }
        for (FlashSaleItem item : flashSaleItemRepository.findAll()) {
            if (eventId.equals(item.getEventId()) && productId.equals(item.getProductId())) {
                if (limitedQty < item.getSoldQty()) {
                    return false;
                }
                item.setDiscountPercent(discountPercent);
                item.setFlashPrice(product.getOriginalPrice() * (100.0 - discountPercent) / 100.0);
                item.setLimitedQty(limitedQty);
                item.setUpdatedAt(LocalDateTime.now());
                return flashSaleItemRepository.update(item);
            }
        }
        return false;
    }
}
