package repository;

import model.Entity.Payment;

public class PaymentRepository extends CsvRepository<Payment> {

    public PaymentRepository() {
        super("data/payments.csv");
    }

    public PaymentRepository(String filePath) {
        super(filePath);
    }

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

    /**
     * Generates the next payment ID based on the max existing PAYxxxxx.
     */
    public String generateNextId() {
        int maxNumber = 0;

        for (Payment payment : findAll()) {
            String id = payment.getId();
            if (id != null && id.matches("PAY\\d+")) {
                maxNumber = Math.max(maxNumber, Integer.parseInt(id.substring(3)));
            }
        }

        return String.format("PAY%05d", maxNumber + 1);
    }
}

    

    
