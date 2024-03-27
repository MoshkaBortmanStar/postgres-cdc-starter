package com.bortmanco.postgrescdcstarter.exception;

import java.io.Serial;

public class SetupReplicationEngineException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -2135811617040774476L;

    public SetupReplicationEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
