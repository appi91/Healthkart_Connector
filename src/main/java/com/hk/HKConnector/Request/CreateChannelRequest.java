package com.hk.HKConnector.Request;

import lombok.*;
import org.springframework.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class CreateChannelRequest extends GenericRequest{

    private String name;
    private String channelEndPoint;


    @Override
    public String validate() {
        if(StringUtils.isEmpty(name)) {
            return "Channel name is mandatory";
        } else if(StringUtils.isEmpty(channelEndPoint)) {
            return "Channel Endpoint is mandatory";
        } else {
            return null;
        }
    }
}
