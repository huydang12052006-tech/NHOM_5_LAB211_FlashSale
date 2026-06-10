package repository;

import model.Entity.Payment;

public class PaymentRepository extends CsvRepository<Payment> {

    public PaymentRepository() {
        super("data/payments.csv");
    }

    public PaymentRepository(String filePath) {
        super(filePath);
    }
    // --- Quản lý Event ---

    @Override
    protected Payment mapFromCsv(String csvLine) {

        Payment payment = new Payment();

        try {
            payment.fromCsvLine(csvLine);
            return payment;
        } catch (Exception e) {
            return null;
        }
    }
}
    

    
