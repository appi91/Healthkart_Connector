package com.hk.HKConnector.Service;

import com.hk.HKConnector.Request.*;
import com.hk.HKConnector.Response.*;
import com.hk.HKConnector.model.*;
import org.springframework.data.domain.*;

import java.util.*;

public interface BulkProcessConfigService {

    GenericApiResponse createChannel(CreateChannelRequest createChannelRequest);

    GenericApiResponse createBulkProcessConfig(CreateBulkProcessConfigRequest createBulkProcessConfigRequest);

    BulkProcessConfiguration fetchProcessingConfigurationById(String id);

    List<Channel> fetchAllChannels();

    String updateChannel(UpdateChannelRequest updateChannelRequest);

    Page<BulkProcessConfiguration> findAllBulkProcessConfigPagination(int pageNum, int perPage);

    GenericApiResponse updateBulkProcessConfig(UpdateBulkProcessConfigRequest updateBulkProcessConfigRequest);

    List<BulkProcessConfiguration> fetchProcessingConfigurationByName(String name);
}