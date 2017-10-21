package Assignment;

public class IssueAlreadyClosedException extends RuntimeException {
    public IssueAlreadyClosedException(String msg) {
    	super(msg);
    }
}
