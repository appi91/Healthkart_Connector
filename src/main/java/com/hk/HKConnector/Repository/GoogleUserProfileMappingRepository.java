package com.hk.HKConnector.Repository;

import com.hk.HKConnector.model.*;
import org.springframework.data.mongodb.repository.*;

public interface GoogleUserProfileMappingRepository extends MongoRepository<GoogleUserProfileMapping, String> {

    GoogleUserProfileMapping findByProfileId(String profileId);
}
