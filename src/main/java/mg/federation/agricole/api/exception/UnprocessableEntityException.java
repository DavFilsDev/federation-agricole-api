package mg.federation.agricole.api.exception;

// Exception sans @ResponseStatus
public class UnprocessableEntityException extends RuntimeException {
    public UnprocessableEntityException(String message) {
        super(message);
    }
}
