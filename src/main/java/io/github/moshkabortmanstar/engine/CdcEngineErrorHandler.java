package io.github.moshkabortmanstar.engine;

@FunctionalInterface
public interface CdcEngineErrorHandler {

    void handleError(Throwable error, String engineName);


}
