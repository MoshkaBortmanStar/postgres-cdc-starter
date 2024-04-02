package io.github.moshkabortmanstar.service.impl;


import io.github.moshkabortmanstar.data.RowChangesStructure;
import io.github.moshkabortmanstar.data.enums.PostgresConnectionProperty;
import io.github.moshkabortmanstar.service.ReplicationSlotPublicationService;
import org.postgresql.PGConnection;
import org.postgresql.replication.ReplicationSlotInfo;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * ReplicationSlotPublicationUtil is a utility class that contains methods for managing replication slots and publications
 * Author: MoshkaBortman
 */
public class ReplicationSlotPublicationServiceImpl implements ReplicationSlotPublicationService {

    public static final String HEARTBEAT_TABLE = "heartbeat_";
    private static final String SCHEMA_NAME = "public";
    private static final String SLOT_EXIST_QUERY = "SELECT slot_name FROM pg_replication_slots WHERE slot_name = ?";
    private static final String PUBLICATION_EXIST_QUERY = "SELECT pubname FROM pg_publication WHERE pubname = ?";
    private static final String CREATE_PUBLICATION_QUERY = "CREATE PUBLICATION %s;";
    private static final String CREATE_PUBLICATION_FOR_ALL_TABLES_QUERY = "CREATE PUBLICATION %s FOR ALL TABLES IN SCHEMA %s";
    private static final String DROP_PUBLICATION_QUERY = "DROP PUBLICATION %s;";
    private static final String ADD_TABLE_TO_PUBLICATION_QUERY = "ALTER PUBLICATION %s ADD TABLE %s;";
    private static final String ALTER_TABLE_REPLICA_IDENTITY_FULL = "ALTER TABLE %s REPLICA IDENTITY FULL;";
    private static final String ADD_ALL_TABLES_TO_PUBLICATION_QUERY = "ALTER PUBLICATION %s ADD TABLES IN SCHEMA %s;";
    private static final String DROP_TABLE_FROM_PUBLICATION_QUERY = "ALTER PUBLICATION %s DROP TABLE %s;";
    private static final String CRATE_HEARTBEAT_TABLE = "CREATE TABLE IF NOT EXISTS %s (id SERIAL PRIMARY KEY, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";
    private static final String INSERT_HEARTBEAT_TABLE = "INSERT INTO %s DEFAULT VALUES;";
    private static final String UPDATE_HEARTBEAT_TABLE = "UPDATE %s SET created_at = CURRENT_TIMESTAMP WHERE id = 1;";

    public void dropPublication(Connection connection, String publicationName) throws SQLException {
        executeUpdate(connection, String.format(DROP_PUBLICATION_QUERY, publicationName));
    }

    public void addTableToPublication(Connection connection, String publicationName, String tableName) throws SQLException {
        String query;
        if (tableName.contains("*")) {
            var schema = tableName.substring(0, tableName.indexOf("."));
            query = String.format(ADD_ALL_TABLES_TO_PUBLICATION_QUERY, publicationName, schema);
        } else {
            query = String.format(ADD_TABLE_TO_PUBLICATION_QUERY, publicationName, tableName);
        }
        executeUpdate(connection, query);
    }

    public void dropTableFromPublication(Connection connection, String publicationName, String tableName) throws SQLException {
        executeUpdate(connection, String.format(DROP_TABLE_FROM_PUBLICATION_QUERY, publicationName, tableName));
    }

    public boolean isSlotExist(Connection connection, String slotName) throws SQLException {
        return executeQuery(connection, SLOT_EXIST_QUERY, slotName);
    }

    public boolean isPublicationExist(Connection connection, String publicationName) throws SQLException {
        return executeQuery(connection, PUBLICATION_EXIST_QUERY, publicationName);
    }

    public void createPublication(Connection connection, String publicationName) throws SQLException {
        executeUpdate(connection, String.format(CREATE_PUBLICATION_QUERY, publicationName));
    }

    public void createPublicationForAllTablesInSchema(Connection connection, String publicationName, String schemaName) throws SQLException {
        executeUpdate(connection, String.format(CREATE_PUBLICATION_FOR_ALL_TABLES_QUERY, publicationName, schemaName));
    }

    public ReplicationSlotInfo createReplicationSlot(PGConnection connection, String slotName) throws SQLException {
        return connection.getReplicationAPI()
                .createReplicationSlot()
                .logical()
                .withSlotName(slotName)
                .withOutputPlugin("pgoutput")
                .make();
    }

    public void dropReplicationSlot(PGConnection connection, String slotName) throws SQLException {
        connection.getReplicationAPI()
                .dropReplicationSlot(slotName);
    }

    public void createHeartbeatTable(Connection connection, String tableName) throws SQLException {
        executeUpdate(connection, String.format(CRATE_HEARTBEAT_TABLE, tableName));
        createFullReplicaIdentity(connection, tableName);
    }

    public void initializeFirstRowHeartbeatTable(Connection connection, String heartbeatTable) throws SQLException {
        executeUpdate(connection, String.format(INSERT_HEARTBEAT_TABLE, heartbeatTable));
    }

    public void updateHeartbeatTable(Connection connection, String heartbeatTable) throws SQLException {
        executeUpdate(connection, String.format(UPDATE_HEARTBEAT_TABLE, heartbeatTable));
    }

    public void createFullReplicaIdentity(Connection connection, String tableName) throws SQLException {
        executeUpdate(connection, String.format(ALTER_TABLE_REPLICA_IDENTITY_FULL, tableName));
    }

    public String generateHeartbeatTableName(String slotName) {
        return SCHEMA_NAME + "." + HEARTBEAT_TABLE + slotName;
    }

    public boolean isHeartbeatTable(String heartbeatTableName, RowChangesStructure rowChangesStructure) {
        if (rowChangesStructure == null) {
            return false;
        }

        return heartbeatTableName.equals(rowChangesStructure.getSchemaName() + "." + rowChangesStructure.getTableName());
    }

    public Connection creteConnectionForReplication(DataSourceProperties dataSourceProperties) throws SQLException {
        var props = new Properties();
        props.setProperty(PostgresConnectionProperty.Param.REPLICATION.getParameter(), PostgresConnectionProperty.Value.DATABASE.getPropertyValue());
        props.setProperty(PostgresConnectionProperty.Param.USER.getParameter(), dataSourceProperties.getUsername());
        props.setProperty(PostgresConnectionProperty.Param.PASSWORD.getParameter(), dataSourceProperties.getPassword());
        props.setProperty(PostgresConnectionProperty.Param.ASSUME_MIN_SERVER_VERSION.getParameter(), PostgresConnectionProperty.Value.ASSUME_VERSION_VALUE.getPropertyValue());
        props.setProperty(PostgresConnectionProperty.Param.PREFER_QUERY_MODE.getParameter(), PostgresConnectionProperty.Value.SIMPLE.getPropertyValue());
        props.setProperty(PostgresConnectionProperty.Param.KEEP_ALIVE.getParameter(), PostgresConnectionProperty.Value.TRUE.getPropertyValue());

        return DriverManager.getConnection(dataSourceProperties.getUrl(), props);
    }

    private boolean executeQuery(Connection connection, String query, String parameter) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, parameter);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

    private void executeUpdate(Connection connection, String query) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.execute();
        }
    }
}
