package com.bortmanco.postgrescdcstarter.exception;

import java.io.Serial;

public class ReaplicationStreamReadingException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6618078218516378902L;

    public ReaplicationStreamReadingException(String message, Throwable cause) {
        super(message, cause);
    }

}
