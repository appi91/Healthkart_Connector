package com.hk.HKConnector.Repository;

import com.hk.HKConnector.model.*;
import org.springframework.data.mongodb.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface BulkProcessConfigurationRepository extends MongoRepository<BulkProcessConfiguration, String> {

    Optional<List<BulkProcessConfiguration>> findByButtonName(String buttonName);
}
