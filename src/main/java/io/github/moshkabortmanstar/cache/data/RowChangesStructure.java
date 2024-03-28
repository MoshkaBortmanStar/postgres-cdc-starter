package io.github.moshkabortmanstar.cache.data;

import io.github.moshkabortmanstar.cache.data.enums.OperationEnum;
import lombok.Builder;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RowChangesStructure is a data class that holds the data of the row changes
 * Author: MoshkaBortman
 * */
@Data
@Builder
public class RowChangesStructure {

    private String tableName;
    private String schemaName;
    private OperationEnum operationEnum;
    @Builder.Default
    private Map<String, String> columnsData = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, Column> columnsType = new LinkedHashMap<>();

}
