package io.github.moshkabortmanstar.decode.impl;


import io.github.moshkabortmanstar.cache.RelationMetaInfoCache;
import io.github.moshkabortmanstar.data.RowChangesStructure;
import io.github.moshkabortmanstar.decode.PgoutHendler;
import io.github.moshkabortmanstar.decode.PgoutMsgDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Consumer;

import static io.github.moshkabortmanstar.data.enums.OperationEnum.getOperationEnum;


@Slf4j
public class PgoutHendlerImpl implements PgoutHendler {

    private final PgoutMsgDecoder pgoutMsgDecoder;

    public PgoutHendlerImpl(PgoutMsgDecoder pgoutMsgDecoder) {
        this.pgoutMsgDecoder = pgoutMsgDecoder;
    }

    @Override
    public void decodeHandle(ByteBuffer buffer,
                             List<RowChangesStructure> rowChangesStructureList,
                             Consumer<List<RowChangesStructure>> changesStructureConsumer) {
        if (buffer.remaining() < 1) {
            log.info("Buffer is empty");
            return;
        }

        var operationByte = (char) buffer.get();
        var operation = getOperationEnum(operationByte);
        var cobyBuffer = buffer.duplicate();

        switch (operation) {
            case BEGIN:
                log.info("Transaction {} start, size of changes {}", operation.name(), rowChangesStructureList.size());
                break;
            case RELATION:
                int relationId = buffer.getInt();
                var relationDto = pgoutMsgDecoder.crateRelationMetaInfo(buffer);
                RelationMetaInfoCache.put(relationId, relationDto);
                log.info("RelationDto {}", relationDto);
                break;
            case INSERT, UPDATE, DELETE, TRUNCATE:
                rowChangesStructureList.add(pgoutMsgDecoder.createRowChangesStructure(cobyBuffer, operation));
                break;
            case COMMIT:
                changesStructureConsumer.accept(rowChangesStructureList);
                rowChangesStructureList.clear();
                log.info("Transaction {} end", operation.name());
                break;
            default:
                log.error("Unsupported command: {}", operationByte);
        }

    }


}
