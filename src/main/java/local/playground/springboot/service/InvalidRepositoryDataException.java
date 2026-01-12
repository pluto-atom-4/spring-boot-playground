package local.playground.springboot.service;

/**
 * Exception thrown when repository returns invalid or unexpected data (nulls or wrong types) that
 * the service cannot process under a strict null-handling policy.
 */
public class InvalidRepositoryDataException extends RuntimeException {
    public InvalidRepositoryDataException() {
        super();
    }

    public InvalidRepositoryDataException(String message) {
        super(message);
    }

    public InvalidRepositoryDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidRepositoryDataException(Throwable cause) {
        super(cause);
    }
}

