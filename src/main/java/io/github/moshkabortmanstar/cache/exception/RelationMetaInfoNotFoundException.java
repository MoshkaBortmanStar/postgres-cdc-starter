package io.github.moshkabortmanstar.cache.exception;

import java.io.Serial;

public class RelationMetaInfoNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 7586785047424724267L;

    public RelationMetaInfoNotFoundException(String message) {
        super(message);
    }


}
