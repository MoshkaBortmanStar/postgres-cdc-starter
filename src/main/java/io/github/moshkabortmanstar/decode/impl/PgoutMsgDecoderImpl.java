package io.github.moshkabortmanstar.decode.impl;


import io.github.moshkabortmanstar.cache.RelationMetaInfoCache;
import io.github.moshkabortmanstar.data.Column;
import io.github.moshkabortmanstar.data.RelationMetaInfo;
import io.github.moshkabortmanstar.data.RowChangesStructure;
import io.github.moshkabortmanstar.data.enums.DataType;
import io.github.moshkabortmanstar.data.enums.OperationEnum;
import io.github.moshkabortmanstar.decode.PgoutMsgDecoder;
import io.github.moshkabortmanstar.exception.RelationMetaInfoNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
public class PgoutMsgDecoderImpl implements PgoutMsgDecoder {

    public RelationMetaInfo crateRelationMetaInfo(ByteBuffer buffer) {
        var relationMsg = new String(buffer.array(), buffer.arrayOffset(), buffer.array().length - buffer.arrayOffset(),
                StandardCharsets.UTF_8);
        buffer = ByteBuffer.wrap(relationMsg.getBytes(StandardCharsets.UTF_8));
        Map<String, Column> columnsMap = new LinkedHashMap<>();
        String copyString = relationMsg;
        byte[] bytes = copyString.getBytes(StandardCharsets.UTF_8);
        relationMsg = relationMsg.substring(OperationEnum.RELATION.getAdditionallyBytes()); //lengthen unnecessary bytes
        String schema = decodeRelationMsg(relationMsg);
        log.info("Schema {}", schema);

        //one byte between schema and table name in UTF-8 code
        relationMsg = relationMsg.substring(schema.length() + 1);
        String tableName = decodeRelationMsg(relationMsg);
        log.info("Table name {}", tableName);

        //five byte between table name and columns in UTF-8 code
        relationMsg = relationMsg.substring(tableName.length() + OperationEnum.RELATION.getAdditionallyBytes());
        boolean isRelationMsgFinish = false;

        while (!isRelationMsgFinish && !relationMsg.isEmpty()) {
            //find column name
            String columnName = decodeRelationMsg(relationMsg);
            //find buffer position of column name
            var position = copyString.indexOf(columnName) + columnName.length() + 1;
            int bytePosition = findBytePosition(bytes, position);
            //set buffer position to column name for reading type
            buffer.position(bytePosition);
            int typeId = buffer.getInt();
            columnsMap.put(columnName, Column.builder().name(columnName).dataType(DataType.fromOid(typeId)).build());

            if (relationMsg.length() < columnName.length() + 2 * OperationEnum.RELATION.getAdditionallyBytes()) {
                isRelationMsgFinish = true;
                continue;
            }
            //ten byte between columns in UTF-8 code
            relationMsg = relationMsg.substring(columnName.length() + 10);
        }
        log.info("Columns {}", columnsMap);


        return RelationMetaInfo.builder()
                .schemaName(schema)
                .tableName(tableName)
                .columnsMap(columnsMap)
                .build();
    }

    private int findBytePosition(byte[] bytes, int charPosition) {
        return new String(bytes, StandardCharsets.UTF_8)
                .substring(0, charPosition)
                .getBytes(StandardCharsets.UTF_8).length;
    }


    /**
     * Create RowChangesStructure from byte message
     */
    public RowChangesStructure createRowChangesStructure(ByteBuffer byteMsg,
                                                         OperationEnum operation) {
        int relationId;
        if (operation == OperationEnum.TRUNCATE) {
            relationId = byteMsg.position(operation.getAdditionallyBytes()).getInt();
            var relationMetaInfo = getRelationMetaInfo(relationId);

            return createRowChangesStructure(relationMetaInfo,
                    createEmptyValuesList(relationMetaInfo.getColumnsMap().size()),
                    operation);
        }

        //get id for relation
        relationId = byteMsg.getInt();

        //switch to next byte
        byteMsg.get();
        short numberOfColumns = byteMsg.getShort();
        RelationMetaInfo relationMetaInfo = getRelationMetaInfo(relationId);

        // Search marker of operation
        int positionOfN = findPositionOfCharacter(byteMsg, operation);
        if (positionOfN == -1) {
            var strValue = new String(byteMsg.array(), StandardCharsets.UTF_8);
            log.info("String cannot be decoded correctly {}", strValue);
            throw new StringIndexOutOfBoundsException("String cannot be decoded correctly " + strValue);
        }
        // Set startPosition to the first byte from the marker
        byteMsg.position(positionOfN);
        //get column and value information
        List<String> listOfColumnsName = relationMetaInfo.getColumnsMap().keySet().stream().toList();
        List<String> listOfValues = createValuesList(listOfColumnsName, byteMsg);

        return createRowChangesStructure(relationMetaInfo, listOfValues, operation);
    }

    private List<String> createValuesList(List<String> listOfColumnsName, ByteBuffer byteMsg) {
        List<String> listOfValues = new LinkedList<>();
        for (String column : listOfColumnsName) {
            char type = (char) byteMsg.get();
            if (type == 't') { // textual data
                final String valueStr = convertToStringValue(byteMsg);
                listOfValues.add(valueStr);
            } else if (type == 'n') { // null data
                listOfValues.add("null");
            } else {
                log.trace("Unsupported type '{}' for column: '{}'", type, column);
            }
        }
        return listOfValues;
    }


    private RelationMetaInfo getRelationMetaInfo(int relationId) {
        RelationMetaInfo relationMetaInfo = RelationMetaInfoCache.get(relationId);
        if (relationMetaInfo == null) {
            log.error("RelationMetaInfo not found for relationKey {}", relationId);
            throw new RelationMetaInfoNotFoundException("RelationMetaInfo not found for relationKey " + relationId);
        }

        return relationMetaInfo;
    }


    private List<String> createEmptyValuesList(int size) {
        List<String> values = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            values.add("null");
        }
        return values;
    }


    private int findPositionOfCharacter(ByteBuffer buffer, OperationEnum operationEnum) {
        //switch on startPosition
        buffer.position(0);
        //if operation is update, then search for insert operation because in update operation we need to find new values, it's constant is 'N'
        var valueOperation = operationEnum == OperationEnum.DELETE ? OperationEnum.OLD_VALUE_REPLACED : OperationEnum.NEW_VALUE_REPLACED;

        //find position of operation
        while (buffer.hasRemaining()) {
            if ((char) buffer.get() == valueOperation.getConstant()) {
                return buffer.position() + valueOperation.getAdditionallyBytes();
            }
        }

        //operation not found
        return -1;
    }


    private String convertToStringValue(ByteBuffer buffer) {
        int length = buffer.getInt();
        byte[] value = new byte[length];
        buffer.get(value, 0, length);
        return new String(value, StandardCharsets.UTF_8);
    }

    private RowChangesStructure createRowChangesStructure(RelationMetaInfo relationMetaInfo, List<String> values, OperationEnum operationEnum) {
        List<String> columns = relationMetaInfo.getColumnsMap().keySet().stream().toList();
        Map<String, String> columnsData = new LinkedHashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            columnsData.put(columns.get(i), values.get(i));
        }

        return RowChangesStructure.builder()
                .tableName(relationMetaInfo.getTableName())
                .schemaName(relationMetaInfo.getSchemaName())
                .operationEnum(operationEnum)
                .columnsData(columnsData)
                .columnsType(relationMetaInfo.getColumnsMap())
                .build();
    }

    /**
     * Decode relation message
     * search for the first occurrence of the '\u0000' character and return the string up to that character
     *
     * @param msg string to decode
     * @return decoded string
     */
    private String decodeRelationMsg(String msg) {
        var sb = new StringBuilder();
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (c != '\u0000') {
                sb.append(c);
            } else {
                return sb.toString();
            }
        }
        return sb.toString();
    }

}
