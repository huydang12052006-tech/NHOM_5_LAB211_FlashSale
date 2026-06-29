package repository;

import java.time.LocalDateTime;
import model.Entity.Customer;

public class CustomerRepository extends CsvRepository<Customer> {

    public CustomerRepository() {
        super("data/customers.csv");
    }

    public CustomerRepository(String filePath) {
        super(filePath);
    }
    // --- Quản lý Event ---

    @Override
    protected Customer mapFromCsv(String csvLine) {

        Customer customer = new Customer();

        try {
            customer.fromCsvLine(csvLine);
            return customer;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean updateAddress(String customerId, String address) {
        Customer customer = findById(customerId);
        if (customer == null) {
            return false;
        }
        customer.setAddress(address);
        customer.setUpdatedAt(LocalDateTime.now());
        return update(customer);
    }
}
    

    
