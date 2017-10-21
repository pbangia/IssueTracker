package exceptions;

/**
 * Created by priyankitbangia on 21/10/17.
 */
public class InvalidAuthStateException extends RuntimeException {
    public InvalidAuthStateException(String message) { super(message);}
}
