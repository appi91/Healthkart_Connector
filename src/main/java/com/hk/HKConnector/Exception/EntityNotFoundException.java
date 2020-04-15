package com.hk.HKConnector.Exception;

import com.hk.HKConnector.Constants.*;
import lombok.*;

@Getter
public class EntityNotFoundException extends RuntimeException {

    private final String code;

    public EntityNotFoundException(HKConnectorExceptionCodes code) {
        super(code.getMessage());
        this.code = code.getCode();
    }

    public EntityNotFoundException(String message, Throwable cause, HKConnectorExceptionCodes code) {
        super(message, cause);
        this.code = code.getCode();
    }
    public EntityNotFoundException(String message, HKConnectorExceptionCodes code) {
        super(message);
        this.code = code.getCode();
    }
    public EntityNotFoundException(Throwable cause, HKConnectorExceptionCodes code) {
        super(cause);
        this.code = code.getCode();
    }
}
