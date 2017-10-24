package exceptions;

public class AdminCannotSetAnIssueToResolved extends RuntimeException {

    public AdminCannotSetAnIssueToResolved(String message) {
        super(message);
    }
}
