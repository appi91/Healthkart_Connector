package com.hk.HKConnector.Constants;

import lombok.*;

@AllArgsConstructor
@Getter
public enum EnumGoogleSheetStatus {

    CREATED("CREATED", "Created"),
    IN_PROCESS("IN_PROCESS", "In Process"),
    PROCESSED_SUCCESS("PROCESSED_SUCCESS", "Processed Successfuly"),
    PROCESSED_WITH_ERROR("PROCESSED_WITH_ERROR", "Processed With Error");


    String code;
    String status;

}
