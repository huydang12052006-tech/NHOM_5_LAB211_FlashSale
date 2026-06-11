package repository;

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
}
    

    
