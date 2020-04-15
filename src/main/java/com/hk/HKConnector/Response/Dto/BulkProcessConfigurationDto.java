package com.hk.HKConnector.Response.Dto;

import com.hk.HKConnector.model.embedded.*;
import lombok.*;

import java.util.*;

@Data
public class BulkProcessConfigurationDto {

    private String id;
    private String channelId;
    private String buttonName;
    private String targetProcessingUri;
    private String sheetName;
    private List<BulkProcessColumnDetail> columnDetails;
    private Boolean active;
    private String createDate;
    private String updateDate;
}
