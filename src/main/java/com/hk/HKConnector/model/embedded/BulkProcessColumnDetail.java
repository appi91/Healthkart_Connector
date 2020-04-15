package com.hk.HKConnector.model.embedded;

import lombok.*;
import org.springframework.util.*;

@Getter
@Setter
public class BulkProcessColumnDetail {

    private String jsonKeyName;
    private String excelDisplayName;
    private String dataType;

    public String validate() {
        if(StringUtils.isEmpty(jsonKeyName)) {
            return "Key name missing";
        } else if(StringUtils.isEmpty(excelDisplayName)) {
            return "Excel display name missing";
        } else if(StringUtils.isEmpty(dataType)) {
            return "Key data type missing";
        } else {
            return null;
        }
    }
}
