package io.github.moshkabortmanstar.decode;


import io.github.moshkabortmanstar.data.RowChangesStructure;
import io.github.moshkabortmanstar.data.enums.OperationEnum;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Consumer;

/**
 * PgoutHendler is an interface that decode the message, Find operation type and call the consumer
 * Author: MoshkaBortman
 * */
public interface PgoutHendler {

    /**
     * Decode the message, Find operation type and call the consumer
     * @param buffer - ByteBuffer that holds the message data
     * @param rowChangesStructureList - List of RowChangesStructure that holds the data of the row changes
     * @param changesStructureConsumer - Consumer that accept the List of RowChangesStructure
    * */
    OperationEnum decodeHandle(ByteBuffer buffer,
                               List<RowChangesStructure> rowChangesStructureList,
                               Consumer<List<RowChangesStructure>> changesStructureConsumer);


}
