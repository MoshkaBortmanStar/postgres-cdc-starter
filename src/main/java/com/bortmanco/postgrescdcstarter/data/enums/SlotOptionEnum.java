package com.bortmanco.postgrescdcstarter.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SlotOptionEnum {

    PROTO_VERSION("proto_version"),
    PUBLICATION_NAME("publication_names"),
    PGOUTPUT("pgoutput");

    private final String optionName;


}
