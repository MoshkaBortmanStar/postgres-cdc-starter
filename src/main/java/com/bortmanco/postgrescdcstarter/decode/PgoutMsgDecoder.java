package com.bortmanco.postgrescdcstarter.decode;



import com.bortmanco.postgrescdcstarter.data.RelationMetaInfo;
import com.bortmanco.postgrescdcstarter.data.RowChangesStructure;
import com.bortmanco.postgrescdcstarter.data.enums.OperationEnum;

import java.nio.ByteBuffer;

/**
 * PgoutMsgDecoder is an interface that decode the message and create the RelationMetaInfo and RowChangesStructure
 * Author: MoshkaBortman
 * */
public interface PgoutMsgDecoder {

    /**
     * Crate RelationMetaInfo from the buffer
     * @param buffer - ByteBuffer that holds the message data
     * @return RelationMetaInfo - RelationMetaInfo that holds the meta information of the relation (table)
    * */
    RelationMetaInfo crateRelationMetaInfo(ByteBuffer buffer);

    /**
     * Create RowChangesStructure from the buffer
     * @param byteMsg - ByteBuffer that holds the message data
     * @param operation - OperationEnum that holds the operation type
     * @return RowChangesStructure
    * */
    RowChangesStructure createRowChangesStructure(ByteBuffer byteMsg,
                                                  OperationEnum operation);

}
