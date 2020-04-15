package com.hk.HKConnector.Exception;

import com.hk.HKConnector.Constants.*;

public class SheetProcessingException extends RuntimeException {

    private final String code;

    public SheetProcessingException(HKConnectorExceptionCodes code) {
        super(code.getMessage());
        this.code = code.getCode();
    }

    public SheetProcessingException(String message, Throwable cause, HKConnectorExceptionCodes code) {
        super(message, cause);
        this.code = code.getCode();
    }
    public SheetProcessingException(String message, HKConnectorExceptionCodes code) {
        super(message);
        this.code = code.getCode();
    }
    public SheetProcessingException(Throwable cause, HKConnectorExceptionCodes code) {
        super(cause);
        this.code = code.getCode();
    }
}
