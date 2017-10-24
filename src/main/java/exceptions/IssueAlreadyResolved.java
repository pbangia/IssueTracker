package exceptions;

public class IssueAlreadyResolved extends RuntimeException {

    public IssueAlreadyResolved(String message) {
        super(message);
    }
}