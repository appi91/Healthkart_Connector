package com.hk.HKConnector.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.*;

import java.util.*;

@Getter
@Setter
@Document(collection = "google_user_profile_mapping")
public class GoogleUserProfileMapping {

    @Id
    private String id;
    private String profileId;
    private String emailId;
    private String comments;
    private Date createDt;

}
