package com.hk.HKConnector.Response;

import lombok.*;
import org.springframework.http.*;

import java.io.*;
import java.util.*;

@Data
public class GenericApiResponse implements Serializable {

    private String status;
    private Integer code;
    private List<String> messages = new LinkedList<>();
    private Object result;
    private boolean error;

    public void buildError(HttpStatus status, String errorMessage) {
        this.status = status.getReasonPhrase();
        this.code = status.value();
        this.getMessages().add(errorMessage);
        this.error = true;
    }

    public void buildSuccess(HttpStatus status, Object result) {
        this.status = status.getReasonPhrase();
        this.code = status.value();
        this.result = result;
    }

    public void build(HttpStatus status) {
        this.status = status.getReasonPhrase();
        this.code = status.value();
    }
}
