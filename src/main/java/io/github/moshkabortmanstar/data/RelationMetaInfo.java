package io.github.moshkabortmanstar.data;

import lombok.Builder;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * RelationMetaInfo is a data class that holds the meta information of the relation (table)
 * Author: MoshkaBortman
* */
@Data
@Builder
public class RelationMetaInfo {

    private String schemaName;
    private String tableName;

    @Builder.Default
    private Map<String, Column> columnsMap = new LinkedHashMap<>();

}
