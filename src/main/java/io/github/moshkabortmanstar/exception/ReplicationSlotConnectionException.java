package io.github.moshkabortmanstar.exception;

import java.io.Serial;

public class ReplicationSlotConnectionException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -5644057663337475301L;

    public ReplicationSlotConnectionException(String message) {
        super(message);
    }

    public ReplicationSlotConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

}
