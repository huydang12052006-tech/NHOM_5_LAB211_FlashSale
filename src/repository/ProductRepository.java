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

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}
