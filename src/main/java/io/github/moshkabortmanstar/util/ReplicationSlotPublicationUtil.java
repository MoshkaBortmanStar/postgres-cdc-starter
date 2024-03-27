package io.github.moshkabortmanstar.util;

import io.github.moshkabortmanstar.data.RowChangesStructure;
import org.postgresql.PGConnection;
import org.postgresql.replication.ReplicationSlotInfo;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import static io.github.moshkabortmanstar.data.enums.PostgresConnectionProperty.Param.ASSUME_MIN_SERVER_VERSION;
import static io.github.moshkabortmanstar.data.enums.PostgresConnectionProperty.Param.KEEP_ALIVE;
import static io.github.moshkabortmanstar.data.enums.PostgresConnectionProperty.Param.PASSWORD;
import static io.github.moshkabortmanstar.data.enums.PostgresConnectionProperty.Param.PREFER_QUERY_MODE;
import static io.github.moshkabortmanstar.data.enums.PostgresConnectionProperty.Param.REPLICATION;
import static io.github.moshkabortmanstar.data.enums.PostgresConnectionProperty.Param.USER;
import static io.github.moshkabortmanstar.data.enums.PostgresConnectionProperty.Value.ASSUME_VERSION_VALUE;
import static io.github.moshkabortmanstar.data.enums.PostgresConnectionProperty.Value.DATABASE;
import static io.github.moshkabortmanstar.data.enums.PostgresConnectionProperty.Value.SIMPLE;
import static io.github.moshkabortmanstar.data.enums.PostgresConnectionProperty.Value.TRUE;

/**
 * ReplicationSlotPublicationUtil is a utility class that contains methods for managing replication slots and publications
 * Author: MoshkaBortman
 * */
public class ReplicationSlotPublicationUtil {

    private ReplicationSlotPublicationUtil() {
    }

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

    /**
     * Drop the publication
     * @param connection - connection to the database
     * @param publicationName - name of the publication
     * @throws SQLException - if an error occurred while dropping the publication
    * */
    public static void dropPublication(Connection connection, String publicationName) throws SQLException {
        executeUpdate(connection, String.format(DROP_PUBLICATION_QUERY, publicationName));
    }

    /**
     * Add table to the publication
     * @param connection - connection to the database
     * @param publicationName - name of the publication
     * @param tableName - name of the table
     * @throws SQLException - if an error occurred while adding the table to the publication
     * */
    public static void addTableToPublication(Connection connection, String publicationName, String tableName) throws SQLException {
        String query;
        if (tableName.contains("*")) {
            var schema = tableName.substring(0, tableName.indexOf("."));
            query = String.format(ADD_ALL_TABLES_TO_PUBLICATION_QUERY, publicationName, schema);
        } else {
            query = String.format(ADD_TABLE_TO_PUBLICATION_QUERY, publicationName, tableName);
        }
        executeUpdate(connection, query);
    }

    /**
     * Drop table from the publication
     * @param connection - connection to the database
     * @param publicationName - name of the publication
     * @param tableName - name of the table
     * @throws SQLException - if an error occurred while dropping the table from the publication
     * */
    public static void dropTableFromPublication(Connection connection, String publicationName, String tableName) throws SQLException {
        executeUpdate(connection, String.format(DROP_TABLE_FROM_PUBLICATION_QUERY, publicationName, tableName));
    }

/**
     * Check if the slot exists
     * @param connection - connection to the database
     * @param slotName - name of the slot
     * @return boolean - true if the slot exists, false otherwise
     * @throws SQLException - if an error occurred while checking the slot
     * */
    public static boolean isSlotExist(Connection connection, String slotName) throws SQLException {
        return executeQuery(connection, SLOT_EXIST_QUERY, slotName);
    }

    /**
     * Check if the publication exists
     * @param connection - connection to the database
     * @param publicationName - name of the publication
     * @return boolean - true if the publication exists, false otherwise
     * @throws SQLException - if an error occurred while checking the publication
     * */
    public static boolean isPublicationExist(Connection connection, String publicationName) throws SQLException {
        return executeQuery(connection, PUBLICATION_EXIST_QUERY, publicationName);
    }

    /**
     * Create publication
     * @param connection - connection to the database
     * @param publicationName - name of the publication
     * @throws SQLException - if an error occurred while creating the publication
     * */
    public static void createPublication(Connection connection, String publicationName) throws SQLException {
        executeUpdate(connection, String.format(CREATE_PUBLICATION_QUERY, publicationName));
    }

    /**
     * Create publication for all tables in the schema
     * @param connection - connection to the database
     * @param publicationName - name of the publication
     * @param schemaName - name of the schema
     * @throws SQLException - if an error occurred while creating the publication
     * */
    public static void createPublicationForAllTablesInSchema(Connection connection, String publicationName, String schemaName) throws SQLException {
        executeUpdate(connection, String.format(CREATE_PUBLICATION_FOR_ALL_TABLES_QUERY, publicationName, schemaName));
    }

    /**
     * Create replication slot
     * @param connection - connection to the database
     * @param slotName - name of the slot
     * @return ReplicationSlotInfo - information about the created slot
     * @throws SQLException - if an error occurred while creating the slot
     * */
    public static ReplicationSlotInfo createReplicationSlot(PGConnection connection, String slotName) throws SQLException {
        return connection.getReplicationAPI()
                .createReplicationSlot()
                .logical()
                .withSlotName(slotName)
                .withOutputPlugin("pgoutput")
                .make();
    }

    /**
     * Drop replication slot
     * @param connection - connection to the database
     * @param slotName - name of the slot
     * @throws SQLException - if an error occurred while dropping the slot
     * */
    public static void dropReplicationSlot(PGConnection connection, String slotName) throws SQLException {
        connection.getReplicationAPI()
                .dropReplicationSlot(slotName);
    }

    /**
     * Create heartbeat table, the table contains the id and created_at columns
     * The table is used to send the heartbeat to the consumer and clean the replication slot
     * @param connection - connection to the database
     * @param tableName - name of the table
     * @throws SQLException - if an error occurred while creating the table
     * */
    public static void createHeartbeatTable(Connection connection, String tableName) throws SQLException {
        executeUpdate(connection, String.format(CRATE_HEARTBEAT_TABLE, tableName));
        createFullReplicaIdentity(connection, tableName);
    }

    public static void initializeFirstRowHeartbeatTable(Connection connection, String heartbeatTable) throws SQLException {
        executeUpdate(connection, String.format(INSERT_HEARTBEAT_TABLE, heartbeatTable));
    }

    /**
     * Update heartbeat table for cleaning the replication slot
     * @param connection - connection to the database
     * @param heartbeatTable - name of the heartbeat table
     * */
    public static void updateHeartbeatTable(Connection connection, String heartbeatTable) throws SQLException {
        executeUpdate(connection, String.format(UPDATE_HEARTBEAT_TABLE, heartbeatTable));
    }

    /**
     * Create full replica identity for the table
     * @param connection - connection to the database
     * @param tableName - name of the table
     * @throws SQLException - if an error occurred while creating the replica identity
     * */
    public static void createFullReplicaIdentity(Connection connection, String tableName) throws SQLException {
        executeUpdate(connection, String.format(ALTER_TABLE_REPLICA_IDENTITY_FULL, tableName));
    }

    /**
     * Generate heartbeat table name
     * @param slotName - name of the slot
     * @return String - name of the heartbeat table
     * */
    public static String generateHeartbeatTableName(String slotName) {
        return SCHEMA_NAME + "." + HEARTBEAT_TABLE + slotName;
    }

    /**
     * Check if the table is a heartbeat table
     * @param heartbeatTableName - name of the heartbeat table
     * @param rowChangesStructure - structure of the row changes
     * @return boolean - true if the table is a heartbeat table, false otherwise
     * */
    public static boolean isHeartbeatTable(String heartbeatTableName, RowChangesStructure rowChangesStructure) {
        if (rowChangesStructure == null) {
            return false;
        }

        return heartbeatTableName.equals(rowChangesStructure.getSchemaName() + "." + rowChangesStructure.getTableName());
    }

    /**
     * Create connection for replication
     * @param dataSourceProperties - properties of the data source
     * @return Connection - connection for replication
     * */
    public static Connection creteConnectionForReplication(DataSourceProperties dataSourceProperties) {
        try {
            var props = new Properties();
            props.setProperty(REPLICATION.getParameter(), DATABASE.getPropertyValue());
            props.setProperty(USER.getParameter(), dataSourceProperties.getUsername());
            props.setProperty(PASSWORD.getParameter(), dataSourceProperties.getPassword());
            props.setProperty(ASSUME_MIN_SERVER_VERSION.getParameter(), ASSUME_VERSION_VALUE.getPropertyValue());
            props.setProperty(PREFER_QUERY_MODE.getParameter(), SIMPLE.getPropertyValue());
            props.setProperty(KEEP_ALIVE.getParameter(), TRUE.getPropertyValue());

            return DriverManager.getConnection(dataSourceProperties.getUrl(), props);
        } catch (SQLException e) {
            throw new RuntimeException("Error while creating connection for replication", e);
        }
    }


    private static boolean executeQuery(Connection connection, String query, String parameter) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, parameter);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

    private static void executeUpdate(Connection connection, String query) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.execute();
        }
    }
}
