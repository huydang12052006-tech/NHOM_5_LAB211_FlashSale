package exception;

public class VersionConflictException
        extends FlashSaleException {

    public VersionConflictException(String message) {
        super(message);
    }
}
