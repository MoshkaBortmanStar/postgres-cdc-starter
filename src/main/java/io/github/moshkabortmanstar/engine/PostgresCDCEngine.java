package io.github.moshkabortmanstar.engine;

import org.postgresql.replication.PGReplicationStream;

/**
 * PostgresCDCEngine is an interface reads the changes from the Postgres and send it to the consumers
 * engine starts in a new thread using the run method
 * Author: MoshkaBortman
 * */
public interface PostgresCDCEngine extends Runnable {

    /**
     * Set up the replication stream engine
     * @return PGReplicationStream
     * */
    PGReplicationStream setUpReplicationStreamEngine();

    /**
     * Stop the engine
     * */
    void stopEngine();

}

