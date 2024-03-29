package io.github.moshkabortmanstar.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum class for Postgres connection properties for replication stream
 * All the properties are defined as enum
 * Author : MoshkaBortman
 * */
public class PostgresConnectionProperty {

    @Getter
    @RequiredArgsConstructor
    public enum Param {
        ASSUME_MIN_SERVER_VERSION("assumeMinServerVersion"),
        KEEP_ALIVE("keepAlive"),
        PASSWORD("password"),
        PREFER_QUERY_MODE("preferQueryMode"),
        REPLICATION("replication"),
        URL("url"),
        USER("user");

        private final String parameter;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Value {
        DATABASE("database"),
        SIMPLE("simple"),
        TRUE("true"),
        ASSUME_VERSION_VALUE("10.0");

        private final String propertyValue;
    }

}
