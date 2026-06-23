package repository;

import java.util.ArrayList;
import java.util.List;
import model.Entity.Product;

public class ProductRepository extends CsvRepository<Product> {

    public ProductRepository() {
        super("data/products.csv");
    }

    public ProductRepository(String filePath) {
        super(filePath);
    }

    @Override
    protected Product mapFromCsv(String csvLine) {

        Product product = new Product();

        try {
            product.fromCsvLine(csvLine);
            return product;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Searches products by keyword matching id, name, or category (case-insensitive).
     */
    public List<Product> searchByKeyword(String keyword) {
        List<Product> result = new ArrayList<Product>();

        if (keyword == null || keyword.isEmpty()) {
            return result;
        }

        String lower = keyword.toLowerCase();

        for (Product product : findAll()) {
            if (containsIgnoreCase(product.getId(), lower)
                    || containsIgnoreCase(product.getName(), lower)
                    || containsIgnoreCase(product.getCategory(), lower)) {
                result.add(product);
            }
        }

        return result;
    }

    public String generateNextId() {
        int maxNumber = 0;
        for (Product product : findAll()) {
            String id = product.getId();
            if (id != null && id.matches("P\\d+")) {
                maxNumber = Math.max(maxNumber, Integer.parseInt(id.substring(1)));
            }
        }
        return String.format("P%05d", maxNumber + 1);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}
