package com.hk.HKConnector.Request;

import com.hk.HKConnector.Service.*;
import com.hk.HKConnector.model.embedded.*;
import lombok.*;
import org.springframework.util.*;

import java.util.*;

@Data
public class CreateBulkProcessConfigRequest extends GenericRequest{

    private String channelId;
    private String buttonName;
    private String targetProcessingUri;
    private String sheetName;
    private List<BulkProcessColumnDetail> columnDetails;
    private String infoTag;
    private String stateParam;
    private String toEmail;

    @Override
    public String validate() {
        if(StringUtils.isEmpty(channelId)) {
            return "ChannelId is mandatory";
        } else if(StringUtils.isEmpty((buttonName))) {
            return "Button name is mandatory";
        } else if (StringUtils.isEmpty(targetProcessingUri)) {
            return "Processing endpoint missing!!";
        } else if(CollectionUtils.isEmpty(columnDetails)) {
            return "Processing columns missing";
        } else if(StringUtils.isEmpty(sheetName)) {
            return "Excel sheet name missing";
        }
        if(!CollectionUtils.isEmpty(columnDetails)) {
            String validation = null;
            for(BulkProcessColumnDetail bpcd : columnDetails) {
                validation = bpcd.validate();
                if(!StringUtils.isEmpty(validation)) {
                    return validation;
                }
            }
        }

        return null;

    }
}
