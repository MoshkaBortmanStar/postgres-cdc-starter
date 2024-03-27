package com.bortmanco.postgrescdcstarter.decode;


import com.bortmanco.postgrescdcstarter.data.RowChangesStructure;

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
    void decodeHandle(ByteBuffer buffer,
                      List<RowChangesStructure> rowChangesStructureList,
                      Consumer<List<RowChangesStructure>> changesStructureConsumer);


}
