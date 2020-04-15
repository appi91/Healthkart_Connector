package com.hk.HKConnector.model;

import com.hk.HKConnector.model.embedded.*;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.*;

import java.util.*;

@Getter
@Setter
@Document(collection = "bulk_process_configuration")
public class BulkProcessConfiguration {

    @Id
    private String id;
    private String channelId;
    private String buttonName;
    private String targetProcessingUri;
    private String sheetName;
    private List<BulkProcessColumnDetail> columnDetails;
    private Boolean active;
    private Date createdAt;
    private Date updatedAt;
    private String createDate;
    private String updateDate;
    private String infoTag;
    private String stateParam;
    private String toEmail;

}
