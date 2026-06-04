package exception;

public class PurchaseLimitExceededException
        extends FlashSaleException {

    public PurchaseLimitExceededException(String message) {
        super(message);
    }
}
