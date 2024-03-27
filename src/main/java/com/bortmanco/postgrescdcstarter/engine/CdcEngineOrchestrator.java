package com.bortmanco.postgrescdcstarter.engine;

/**
 * CdcEngineOrchestrator is an interface that orchestrates the CDC engine
 * Author: MoshkaBortman
 * */
public interface CdcEngineOrchestrator {

    /**
     * Start the engine
     * @param engineName - String that holds the engine name
     * */
    void startEngine(String engineName);

    /**
     * Stop the engine
     * @param engineName - String that holds the engine name
     * */
    void restartEngine(String engineName);


}
