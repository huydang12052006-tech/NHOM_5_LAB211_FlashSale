package exception;

public class OutOfStockException
        extends FlashSaleException {

    public OutOfStockException(String message) {
        super(message);
    }
}
