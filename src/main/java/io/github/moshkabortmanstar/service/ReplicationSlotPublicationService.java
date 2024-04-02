package io.github.moshkabortmanstar.service;

import io.github.moshkabortmanstar.data.RowChangesStructure;
import org.postgresql.PGConnection;
import org.postgresql.replication.ReplicationSlotInfo;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import java.sql.Connection;
import java.sql.SQLException;

public interface ReplicationSlotPublicationService {

    /**
     * Drop the publication
     *
     * @param connection      - connection to the database
     * @param publicationName - name of the publication
     * @throws SQLException - if an error occurred while dropping the publication
     */
    void dropPublication(Connection connection, String publicationName) throws SQLException;

    /**
     * Add table to the publication
     *
     * @param connection      - connection to the database
     * @param publicationName - name of the publication
     * @param tableName       - name of the table
     * @throws SQLException - if an error occurred while adding the table to the publication
     */
    void addTableToPublication(Connection connection, String publicationName, String tableName) throws SQLException;

    /**
     * Drop table from the publication
     *
     * @param connection      - connection to the database
     * @param publicationName - name of the publication
     * @param tableName       - name of the table
     * @throws SQLException - if an error occurred while dropping the table from the publication
     */
    void dropTableFromPublication(Connection connection, String publicationName, String tableName) throws SQLException;

    /**
     * Check if the slot exists
     *
     * @param connection - connection to the database
     * @param slotName   - name of the slot
     * @return boolean - true if the slot exists, false otherwise
     * @throws SQLException - if an error occurred while checking the slot
     */
    boolean isSlotExist(Connection connection, String slotName) throws SQLException;


    /**
     * Check if the publication exists
     *
     * @param connection      - connection to the database
     * @param publicationName - name of the publication
     * @return boolean - true if the publication exists, false otherwise
     * @throws SQLException - if an error occurred while checking the publication
     */
    boolean isPublicationExist(Connection connection, String publicationName) throws SQLException;


    /**
     * Create publication
     *
     * @param connection      - connection to the database
     * @param publicationName - name of the publication
     * @throws SQLException - if an error occurred while creating the publication
     */
    public void createPublication(Connection connection, String publicationName) throws SQLException;


    /**
     * Create publication for all tables in the schema
     *
     * @param connection      - connection to the database
     * @param publicationName - name of the publication
     * @param schemaName      - name of the schema
     * @throws SQLException - if an error occurred while creating the publication
     */
    public void createPublicationForAllTablesInSchema(Connection connection, String publicationName, String schemaName) throws SQLException;


    /**
     * Create replication slot
     *
     * @param connection - connection to the database
     * @param slotName   - name of the slot
     * @return ReplicationSlotInfo - information about the created slot
     * @throws SQLException - if an error occurred while creating the slot
     */
    ReplicationSlotInfo createReplicationSlot(PGConnection connection, String slotName) throws SQLException;


    /**
     * Drop replication slot
     *
     * @param connection - connection to the database
     * @param slotName   - name of the slot
     * @throws SQLException - if an error occurred while dropping the slot
     */
    void dropReplicationSlot(PGConnection connection, String slotName) throws SQLException;


    /**
     * Create heartbeat table, the table contains the id and created_at columns
     * The table is used to send the heartbeat to the consumer and clean the replication slot
     *
     * @param connection - connection to the database
     * @param tableName  - name of the table
     * @throws SQLException - if an error occurred while creating the table
     */
    void createHeartbeatTable(Connection connection, String tableName) throws SQLException;


    void initializeFirstRowHeartbeatTable(Connection connection, String heartbeatTable) throws SQLException;


    /**
     * Update heartbeat table for cleaning the replication slot
     *
     * @param connection     - connection to the database
     * @param heartbeatTable - name of the heartbeat table
     */
    void updateHeartbeatTable(Connection connection, String heartbeatTable) throws SQLException;


    /**
     * Create full replica identity for the table
     *
     * @param connection - connection to the database
     * @param tableName  - name of the table
     * @throws SQLException - if an error occurred while creating the replica identity
     */
    void createFullReplicaIdentity(Connection connection, String tableName) throws SQLException;


    /**
     * Generate heartbeat table name
     *
     * @param slotName - name of the slot
     * @return String - name of the heartbeat table
     */
    String generateHeartbeatTableName(String slotName);


    /**
     * Check if the table is a heartbeat table
     *
     * @param heartbeatTableName  - name of the heartbeat table
     * @param rowChangesStructure - structure of the row changes
     * @return boolean - true if the table is a heartbeat table, false otherwise
     */
    boolean isHeartbeatTable(String heartbeatTableName, RowChangesStructure rowChangesStructure);


    /**
     * Create connection for replication
     *
     * @param dataSourceProperties - properties of the data source
     * @return Connection - connection for replication
     */
    Connection creteConnectionForReplication(DataSourceProperties dataSourceProperties) throws SQLException;

}
