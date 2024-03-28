package io.github.moshkabortmanstar.cache.data.enums;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public enum OperationEnum {

    //meta operation
    BEGIN((char) 66, 2),
    COMMIT((char) 67, 2),
    RELATION((char) 82, 5),

    //DML operation
    INSERT((char) 73, 2),
    UPDATE((char) 85, 2),
    DELETE((char) 68, 7),

    //DDL operation
    TRUNCATE((char) 84, 6),

    //value operation
    NEW_VALUE_REPLACED((char) 78, 2),
    OLD_VALUE_REPLACED((char) 79, 2),

    //unknown operation
    UNKNOWN_OPERATION((char) 42, 0);


    private final char constant;
    private final int additionallyBytes;

    OperationEnum(char constant, int additionallyBytes) {
        this.constant = constant;
        this.additionallyBytes = additionallyBytes;
    }


    //get OperationEnum by constant
    public static OperationEnum getOperationEnum(char constant) {
        for (OperationEnum operationEnum : OperationEnum.values()) {
            if (operationEnum.getConstant() == constant) {
                return operationEnum;
            }
        }
        log.error("Unknown operation constant: " + constant);
        return UNKNOWN_OPERATION;
    }

}
