package exceptions;

public class PermissionDeniedException extends RuntimeException {
    public PermissionDeniedException(String msg) {
    	super(msg);
    }
}
