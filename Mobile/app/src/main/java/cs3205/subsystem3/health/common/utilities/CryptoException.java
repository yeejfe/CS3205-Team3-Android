package cs3205.subsystem3.health.common.utilities;

/**
 * Created by danwen on 27/10/17.
 */

public class CryptoException extends Exception {

    public CryptoException() {}

    public CryptoException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public CryptoException(String message) {
        super(message, new Throwable());
    }
}