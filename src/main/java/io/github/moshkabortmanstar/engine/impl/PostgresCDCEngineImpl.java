package io.github.moshkabortmanstar.engine.impl;


import io.github.moshkabortmanstar.cache.RelationMetaInfoCache;
import io.github.moshkabortmanstar.data.RowChangesStructure;
import io.github.moshkabortmanstar.data.enums.OperationEnum;
import io.github.moshkabortmanstar.data.enums.SlotOptionEnum;
import io.github.moshkabortmanstar.decode.PgoutHendler;
import io.github.moshkabortmanstar.engine.CdcEngineErrorHandler;
import io.github.moshkabortmanstar.engine.CdcEngineOrchestrator;
import io.github.moshkabortmanstar.engine.PostgresCDCEngine;
import io.github.moshkabortmanstar.exception.ReplicationSlotConnectionException;
import io.github.moshkabortmanstar.exception.ReplicationStreamReadingException;
import io.github.moshkabortmanstar.exception.SetupReplicationEngineException;
import io.github.moshkabortmanstar.service.ReplicationSlotPublicationService;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.postgresql.replication.PGReplicationStream;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Builder
public class PostgresCDCEngineImpl implements PostgresCDCEngine {

    private String slotName;
    private String engineName;
    private boolean isRunning;
    private CdcEngineOrchestrator orchestrator;
    private DataSourceProperties properties;
    private PgoutHendler pgoutHendler;
    private ReplicationSlotPublicationService replicationSlotPublicationService;
    @Builder.Default
    private CdcEngineErrorHandler errorHandler = (error, strEngineName) -> log.error("Stop engine {} with error {}", strEngineName, error.getMessage());
    private Consumer<List<RowChangesStructure>> changesStructureConsumer;

    /**
     * The main method of the engine, it reads the data from the replication stream and decodes it
     * WARNING: This method using connection with required parameters:
     * replication=database
     * preferQueryMode=simple
     * assumeMinServerVersion=10
     * You need to provide these parameters in your overridden method setupReplicationStreamEngine
     * You can use standard implementation replicationSlotPublicationService.setupReplicationStreamEngine (replicationSlotPublicationService.creteConnectionForReplication)
     */
    @Override
    public void run() {
        Flux.create(sink -> {
                    try (var stream = setUpReplicationStreamEngine()) {

                        isRunning = true;
                        var listOfTransaction = new LinkedList<RowChangesStructure>();
                        log.info("Engine {} started", engineName);

                        while (isRunning) {

                            ByteBuffer msg = stream.readPending();
                            if (msg == null) {
                                TimeUnit.MILLISECONDS.sleep(100L);
                                continue;
                            }

                            var operation = pgoutHendler.decodeHandle(msg,
                                    listOfTransaction,
                                    rowChangesStructuresList -> {
                                        if (replicationSlotPublicationService.isHeartbeatTable(replicationSlotPublicationService.generateHeartbeatTableName(slotName), rowChangesStructuresList.get(0))) {
                                            log.info("Heartbeat {}, clean space in replication slot", replicationSlotPublicationService.generateHeartbeatTableName(slotName));
                                        } else {
                                            sink.next(rowChangesStructuresList);
                                        }
                                    }
                            );

                            if (operation == OperationEnum.COMMIT) {
                                changesStructureConsumer.accept(listOfTransaction);
                                listOfTransaction.clear();
                                stream.setAppliedLSN(stream.getLastReceiveLSN());
                                stream.setFlushedLSN(stream.getLastReceiveLSN());
                            }
                        }
                    } catch (SQLException | SetupReplicationEngineException conException) {
                        sink.error(new ReplicationSlotConnectionException("Error connection to replication slot", conException));
                    } catch (Exception e) {
                        log.error("Error during engine {} run, error {}, message cannot be committed", engineName, e.getMessage());
                        sink.error(new ReplicationStreamReadingException("Error during engine run", e));
                    }
                }).subscribeOn(Schedulers.boundedElastic())
                .doOnNext(rowChangesStructuresList -> changesStructureConsumer.accept((List<RowChangesStructure>) rowChangesStructuresList))
                .doOnError(e -> {
                    log.error("Error during engine {} run, error {}", engineName, e.getMessage());
                    if (e instanceof ReplicationSlotConnectionException) {
                        orchestrator.restartEngine(engineName);  // Restart engine if connection error
                    } else {
                        errorHandler.handleError(e, engineName);
                    }
                })
                .subscribe();
    }

    @Override
    public PGReplicationStream setUpReplicationStreamEngine() {
        try {
            // 0. Clean thread cache
            RelationMetaInfoCache.clear();
            log.info("Starting engine {}, clear cache", engineName);

            // 1. Get the connection and unwrap it to PGConnection
            var connection = replicationSlotPublicationService.creteConnectionForReplication(properties);
            var streamConnection = connection.unwrap(PGConnection.class);

            // 2. Check replication slot, create if not exists
            if (!replicationSlotPublicationService.isSlotExist(connection, slotName)) {
                log.info("Slot {} does not exist, creating", slotName);
                replicationSlotPublicationService.createReplicationSlot(streamConnection, slotName);
            }

            // 3. Check publication, create if not exists, name of publication is the same as slot name
            if (!replicationSlotPublicationService.isPublicationExist(connection, slotName)) {
                replicationSlotPublicationService.createPublication(connection, slotName);
            }

            // 4. Create heartbeat table
            var heartbeatTable = replicationSlotPublicationService.generateHeartbeatTableName(slotName);
            replicationSlotPublicationService.createHeartbeatTable(connection, heartbeatTable);

            // 5. Add heartbeat table to publication
            addHeartbeatTableToPublication(connection, heartbeatTable);

            // 6. Create a replication stream
            return streamConnection.getReplicationAPI()
                    .replicationStream()
                    .logical()
                    .withSlotName(slotName)
                    .withSlotOption(SlotOptionEnum.PROTO_VERSION.getOptionName(), "1")
                    .withSlotOption(SlotOptionEnum.PUBLICATION_NAME.getOptionName(), slotName)
                    .start();
        } catch (SQLException e) {
            log.error("Error during setup of replication stream", e);
            throw new SetupReplicationEngineException("Error during setup of replication stream", e);
        }

    }

    private void addHeartbeatTableToPublication(Connection connection, String heartbeatTable) throws SQLException {
        try {
            replicationSlotPublicationService.addTableToPublication(connection, slotName, heartbeatTable);
            replicationSlotPublicationService.initializeFirstRowHeartbeatTable(connection, heartbeatTable);
        } catch (SQLException e) {
            log.warn("Table already added to table {}, error {}", heartbeatTable, e.getMessage());
        }
    }

    public void stopEngine() {
        log.info("Initiating stop of engine {}", engineName);
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }
}


