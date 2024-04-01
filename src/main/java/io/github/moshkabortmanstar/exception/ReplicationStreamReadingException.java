package io.github.moshkabortmanstar.exception;

import java.io.Serial;

public class ReplicationStreamReadingException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6618078218516378902L;

    public ReplicationStreamReadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReplicationStreamReadingException(String message) {
        super(message);
    }
}
