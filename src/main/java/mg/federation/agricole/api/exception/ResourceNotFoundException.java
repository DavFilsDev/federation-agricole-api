// exception/ResourceNotFoundException.java
package mg.federation.agricole.api.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}