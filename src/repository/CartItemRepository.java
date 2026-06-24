package repository;

import model.Entity.CartItem;

public class CartItemRepository extends CsvRepository<CartItem> {

    public CartItemRepository() {
        super("data/cart_items.csv");
    }

    public CartItemRepository(String filePath) {
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
}
