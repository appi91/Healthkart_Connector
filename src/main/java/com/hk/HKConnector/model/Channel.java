package com.hk.HKConnector.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.*;

import java.util.*;

@Getter
@Setter
@Document(collection = "channel")
public class Channel {

    @Id
    private String id;
    private String name;
    private String channelEndPoint;
    private Boolean active;
    private Date createdAt;
    private Date updatedAt;
    private String createDate;
    private String updateDate;
}
