package com.hk.HKConnector.Constants;

public enum HKConnectorExceptionCodes {

    HKC_101("HKC_101", "Document not found for given Id"),
    HKC_102("HKC_102", "No Spreadsheet found with given Id!!"),
    HKC_103("HKC_103", "Spreadsheet is either in process state or already processed!!"),
    HKC_104("HKC_104", "Spreadsheet and button id aren't linked!!");

    private String code;
    private String message;

    HKConnectorExceptionCodes(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
