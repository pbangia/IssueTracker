package exceptions;

public class EmptyUsernameException extends RuntimeException {

    public EmptyUsernameException(String message) {
        super(message);
    }
}
