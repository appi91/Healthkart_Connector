package com.hk.HKConnector.Request;

import lombok.*;
import org.springframework.util.*;

@Data
public class UpdateBulkProcessConfigRequest extends CreateBulkProcessConfigRequest {

    private String id;

    @Override
    public String validate() {
        String validation = super.validate();
        if(StringUtils.isEmpty(validation)) {
            if(id == null) {
                validation = "Id to be updated not found!!";
            }
        }

        return validation;
    }
}
