package com.bortmanco.postgrescdcstarter.exception;

import java.io.Serial;

public class ThreadWaitReadingException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -3280226804970783682L;

    public ThreadWaitReadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
