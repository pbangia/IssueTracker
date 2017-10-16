package exceptions;

/**
 * Created by g.tiongco on 16/10/17.
 */
public class PasswordFormatException extends RuntimeException {

    public PasswordFormatException(String message) {
        super(message);
    }
}
