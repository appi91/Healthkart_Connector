package com.hk.HKConnector.Repository;

import com.hk.HKConnector.model.*;
import org.springframework.data.mongodb.repository.*;

import java.util.*;

public interface GoogleSheetStatusRepository extends MongoRepository<GoogleSheetStatus, String> {

    Optional<GoogleSheetStatus> findBySheetId(String sheetId);
}
