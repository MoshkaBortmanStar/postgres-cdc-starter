package io.github.moshkabortmanstar.decode.impl;


import io.github.moshkabortmanstar.cache.RelationMetaInfoCache;
import io.github.moshkabortmanstar.data.RowChangesStructure;
import io.github.moshkabortmanstar.data.enums.OperationEnum;
import io.github.moshkabortmanstar.decode.PgoutHendler;
import io.github.moshkabortmanstar.decode.PgoutMsgDecoder;
import io.github.moshkabortmanstar.exception.ReplicationStreamReadingException;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Consumer;

import static io.github.moshkabortmanstar.data.enums.OperationEnum.UNKNOWN_OPERATION;
import static io.github.moshkabortmanstar.data.enums.OperationEnum.getOperationEnum;


@Slf4j
public class PgoutHendlerImpl implements PgoutHendler {

    private final PgoutMsgDecoder pgoutMsgDecoder;

    public PgoutHendlerImpl(PgoutMsgDecoder pgoutMsgDecoder) {
        this.pgoutMsgDecoder = pgoutMsgDecoder;
    }

    @Override
    public OperationEnum decodeHandle(ByteBuffer buffer,
                                      List<RowChangesStructure> rowChangesStructureList,
                                      Consumer<List<RowChangesStructure>> changesStructureConsumer) {
        if (buffer.remaining() < 1) {
            log.warn("Buffer is empty");
            throw new ReplicationStreamReadingException("Buffer is empty");
        }
        var operationByte = (char) buffer.get();
        var operation = getOperationEnum(operationByte);
        var cobyBuffer = buffer.duplicate();

        switch (operation) {
            case BEGIN:
                log.info("Transaction {} start, size of changes {}", operation.name(), rowChangesStructureList.size());
                return operation;
            case RELATION:
                int relationId = buffer.getInt();
                var relationDto = pgoutMsgDecoder.crateRelationMetaInfo(buffer);
                RelationMetaInfoCache.put(relationId, relationDto);
                log.info("RelationDto {}", relationDto);
                return operation;
            case INSERT, UPDATE, DELETE, TRUNCATE:
                rowChangesStructureList.add(pgoutMsgDecoder.createRowChangesStructure(cobyBuffer, operation));
                return operation;
            case COMMIT:
                log.info("Transaction {} end", operation.name());
                return operation;
            default:
                log.error("Unsupported command: {}", operationByte);
                return UNKNOWN_OPERATION;
        }
    }

}