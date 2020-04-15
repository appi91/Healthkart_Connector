package com.hk.HKConnector.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.*;

import java.util.*;

@Getter
@Setter
@Document(collection = "google_sheet_status")
public class GoogleSheetStatus {

    @Id
    private String id;
    private String sheetId;
    private String buttonId;
    private String status;
    private String createBy;
    private String createDt;
    private Date createdOn;
    private String processedBy;
    private String processDt;
    private Date processedOn;
}
