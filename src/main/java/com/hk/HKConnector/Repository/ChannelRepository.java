package com.hk.HKConnector.Repository;

import com.hk.HKConnector.model.*;
import org.springframework.data.mongodb.repository.*;
import org.springframework.stereotype.*;

@Repository
public interface ChannelRepository extends MongoRepository<Channel, String> {
}
