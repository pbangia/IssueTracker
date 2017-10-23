package exceptions;

public class AdminCannotBeenAssignedException extends RuntimeException {
	public AdminCannotBeenAssignedException(String msg) {
		super(msg);
	}
}
