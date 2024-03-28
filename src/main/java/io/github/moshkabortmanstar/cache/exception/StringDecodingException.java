package io.github.moshkabortmanstar.cache.exception;

import java.io.Serial;

public class StringDecodingException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6151585887066438745L;

    public StringDecodingException(String message) {
        super(message);
    }

}
