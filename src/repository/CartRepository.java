package repository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Entity.CartItem;

public class CartRepository extends CsvRepository<CartItem> {

    public CartRepository() {
        super("data/cart_items.csv");
    }

    public CartRepository(String filePath) {
        super(filePath);
    }

    @Override
    protected CartItem mapFromCsv(String csvLine) {
        try {
            CartItem item = new CartItem();
            item.fromCsvLine(csvLine);
            return item;
        } catch (Exception e) {
            return null;
        }
    }

    public List<CartItem> findByCustomerId(String customerId) {
        List<CartItem> result = new ArrayList<CartItem>();
        for (CartItem item : findAll()) {
            if (customerId != null && customerId.equalsIgnoreCase(item.getCustomerId())) {
                result.add(item);
            }
        }
        return result;
    }

    public CartItem addOrMergeItem(String customerId, String flashItemId, String productId, int quantity) {
        for (CartItem item : findByCustomerId(customerId)) {
            if (same(item.getFlashItemId(), flashItemId) && same(item.getProductId(), productId)) {
                item.setQuantity(item.getQuantity() + quantity);
                item.setUpdatedAt(LocalDateTime.now());
                update(item);
                return item;
            }
        }

        LocalDateTime now = LocalDateTime.now();
        CartItem item = new CartItem(generateNextId(), now, now,
                customerId, flashItemId, productId, quantity);
        save(item);
        return item;
    }

    public boolean removeCustomerItem(String customerId, String cartItemId) {
        CartItem item = findById(cartItemId);
        if (item == null || customerId == null || !customerId.equalsIgnoreCase(item.getCustomerId())) {
            return false;
        }
        try {
            return delete(cartItemId);
        } catch (IOException e) {
            return false;
        }
    }

    public void clearByCustomerId(String customerId) {
        for (CartItem item : findByCustomerId(customerId)) {
            removeCustomerItem(customerId, item.getId());
        }
    }

    public String generateNextId() {
        int maxNumber = 0;
        for (CartItem item : findAll()) {
            String id = item.getId();
            if (id != null && id.matches("CI\\d+")) {
                maxNumber = Math.max(maxNumber, Integer.parseInt(id.substring(2)));
            }
        }
        return String.format("CI%06d", maxNumber + 1);
    }

    private boolean same(String first, String second) {
        return first == null ? second == null : first.equals(second);
    }
}
