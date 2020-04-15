package com.hk.HKConnector.Request;

import lombok.*;
import org.springframework.util.*;

@Data
public class UpdateChannelRequest extends CreateChannelRequest {

    private String cId;

    @Override
    public String validate() {
        String validation = super.validate();
        if(validation != null) {
            if(StringUtils.isEmpty(getCId())) {
                return "Id to be updated not found!!";
            }
        }

        return validation;
    }
}
