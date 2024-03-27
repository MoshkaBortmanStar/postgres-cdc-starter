package com.bortmanco.postgrescdcstarter.data.enums;

public enum DataType {

    BIGINT(20), // BIGSERIAL(20), SERIAL(20)
    BIT(1560),
    BIT_VARYING(1562),
    BOOLEAN(16),
    BOX(603),
    BYTEA(17),
    CHARACTER(1042),
    DATE(1082),
    INET(869),
    INTEGER(23),
    JSON(114),
    JSONB(3823),
    LINE(628),
    LSEG(601),
    MACADDR(829),
    MACADDR8(774),
    MONEY(790),
    NUMERIC(1775), // DECIMAL(1775) too
    PATH(602),
    PG_LSN(3311),
    PG_SNAPSHOT(5103),
    POINT(600),
    POLYGON(604),
    REAL(751),
    SMALLINT(21), // SMALLSERIAL(21),
    TEXT(25),
    TIME(1083),
    TIME_WITH_TIME_ZONE(1263), //INTERVAL(1263) too
    TIMESTAMP(1114),
    TSQUERY(3615),
    TSVECTOR(3614),
    UUID(3055),  //TXID_SNAPSHOT(3055) too
    VARCHAR(1043), // CHARACTER_VARYING(1043)
    XML(239);

    private int oid;

    DataType(int oid) {
        this.oid = oid;
    }

    public static DataType fromOid(int oid) {
        for (DataType dt : DataType.values()) {
            if (dt.oid == oid) {
                return dt;
            }
        }

        //default TEXT because text possible to store any data type
        return TEXT;
    }
}
