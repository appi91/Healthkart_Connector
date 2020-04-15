package com.hk.HKConnector.Service.impl;

import com.hk.HKConnector.Constants.*;
import com.hk.HKConnector.Exception.*;
import com.hk.HKConnector.Repository.*;
import com.hk.HKConnector.Request.*;
import com.hk.HKConnector.Response.*;
import com.hk.HKConnector.Service.*;
import com.hk.HKConnector.model.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;

import java.text.*;
import java.util.*;

@Slf4j
@Service
public class BulkProcessConfigServiceImpl implements BulkProcessConfigService {

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private BulkProcessConfigurationRepository bulkProcessConfigurationRepository;

    @Value("${endpoint.hkc}")
    private String hkcEndpoint;

    @Value("${uri.sheet.create}")
    private String createSheetResourceURI;

    @Value("${uri.sheet.process}")
    private String processSheetResourceURI;

    @Override
    public GenericApiResponse createChannel(CreateChannelRequest createChannelRequest) {
        GenericApiResponse genericApiResponse = new GenericApiResponse();
        String validation = createChannelRequest.validate();
        if(StringUtils.isEmpty(validation)) {
            Channel c = new Channel();
            c.setName(createChannelRequest.getName());
            c.setChannelEndPoint(createChannelRequest.getChannelEndPoint());
            c.setActive(true);
            c.setCreatedAt(new Date());
            c.setUpdatedAt(new Date());

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            c.setCreateDate(formatter.format(new Date()));
            c.setUpdateDate(formatter.format(new Date()));
            channelRepository.save(c);
            genericApiResponse.build(HttpStatus.CREATED);
        } else {
            genericApiResponse.buildError(HttpStatus.EXPECTATION_FAILED, validation);
        }

        return genericApiResponse;
    }

    @Override
    public GenericApiResponse createBulkProcessConfig(CreateBulkProcessConfigRequest createBulkProcessConfigRequest) {
        GenericApiResponse genericApiResponse = new GenericApiResponse();
        String validation = createBulkProcessConfigRequest.validate();
        if (StringUtils.isEmpty(validation)) {
            if(channelRepository.findById(createBulkProcessConfigRequest.getChannelId()).orElse(null) == null) {
                genericApiResponse.buildError(HttpStatus.EXPECTATION_FAILED, "Selected channel doesn't exists!!");
                return genericApiResponse;
            }
            BulkProcessConfiguration bpc = new BulkProcessConfiguration();
            bpc.setActive(true);
            bpc.setButtonName(createBulkProcessConfigRequest.getButtonName());
            bpc.setChannelId(createBulkProcessConfigRequest.getChannelId());
            bpc.setColumnDetails(createBulkProcessConfigRequest.getColumnDetails());
            bpc.setTargetProcessingUri(createBulkProcessConfigRequest.getTargetProcessingUri());
            bpc.setSheetName(createBulkProcessConfigRequest.getSheetName());
            bpc.setInfoTag(createBulkProcessConfigRequest.getInfoTag());
            bpc.setStateParam(createBulkProcessConfigRequest.getStateParam());
            bpc.setToEmail(createBulkProcessConfigRequest.getToEmail());
            bpc.setCreatedAt(new Date());
            bpc.setUpdatedAt(new Date());

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            bpc.setCreateDate(formatter.format(new Date()));
            bpc.setUpdateDate(formatter.format(new Date()));
            bpc = bulkProcessConfigurationRepository.save(bpc);
            Map<String, String> buttonDetails = new HashMap<>();
            buttonDetails.put(ResponseConstants.BUTTON_ID, bpc.getId());
            buttonDetails.put(ResponseConstants.BUTTON_NAME, bpc.getButtonName());
            //TODO : Move these endpoint to configuration
            StringBuffer bulkCreateEndPoint = new StringBuffer();
            bulkCreateEndPoint.append(hkcEndpoint).append(createSheetResourceURI).append(bpc.getId());
            StringBuffer processEndPoint = new StringBuffer();
            processEndPoint.append(hkcEndpoint).append(processSheetResourceURI).append(bpc.getId()).append("/sid");
            if(bpc.getStateParam() != null && !bpc.getStateParam().isBlank()) {
                processEndPoint.append("?state=${").append(bpc.getStateParam()).append("}");
            }
            buttonDetails.put(ResponseConstants.BULK_CREATE_END_POINT, bulkCreateEndPoint.toString());
            buttonDetails.put(ResponseConstants.PROCESS_END_POINT, processEndPoint.toString() );
            buttonDetails.put(ResponseConstants.INFO_TAG, bpc.getInfoTag());
            genericApiResponse.buildSuccess(HttpStatus.CREATED, buttonDetails);
        } else {
            genericApiResponse.buildError(HttpStatus.EXPECTATION_FAILED, validation);
        }

        return genericApiResponse;
    }

    @Override
    public BulkProcessConfiguration fetchProcessingConfigurationById(String id) {
        return bulkProcessConfigurationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(HKConnectorExceptionCodes.HKC_101));
    }

    @Override
    public List<Channel> fetchAllChannels() {
       return channelRepository.findAll();
    }

    @Override
    public String updateChannel(UpdateChannelRequest updateChannelRequest) {
        String validation = updateChannelRequest.validate();
        if(StringUtils.isEmpty(validation)) {
            Channel c = channelRepository.findById(updateChannelRequest.getCId()).orElse(null);
            if(c == null) {
                return "Entity to be updated not found!!";
            }
            c.setName(updateChannelRequest.getName());
            c.setChannelEndPoint(updateChannelRequest.getChannelEndPoint());
            //c.setActive(true);
            c.setUpdatedAt(new Date());

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            c.setUpdateDate(formatter.format(new Date()));
            channelRepository.save(c);
            //Success
            return null;
        } else {
            return  validation;
        }
    }

    @Override
    public Page<BulkProcessConfiguration> findAllBulkProcessConfigPagination(int pageNum, int perPage) {
        Pageable pageable = PageRequest.of(pageNum, perPage, Sort.by(Sort.Direction.DESC, "updatedAt")) ;
        return bulkProcessConfigurationRepository.findAll(pageable);
    }

    @Override
    public GenericApiResponse updateBulkProcessConfig(UpdateBulkProcessConfigRequest updateBulkProcessConfigRequest) {
        String validation = updateBulkProcessConfigRequest.validate();
        GenericApiResponse genericApiResponse = new GenericApiResponse();
        if(StringUtils.isEmpty(validation)) {
            BulkProcessConfiguration bpc = bulkProcessConfigurationRepository.findById(updateBulkProcessConfigRequest.getId()).orElse(null);
            if(bpc == null) {
                return genericApiResponse;
            }
            bpc.setButtonName(updateBulkProcessConfigRequest.getButtonName());
            bpc.setChannelId(updateBulkProcessConfigRequest.getChannelId());
            bpc.setColumnDetails(updateBulkProcessConfigRequest.getColumnDetails());
            bpc.setTargetProcessingUri(updateBulkProcessConfigRequest.getTargetProcessingUri());
            bpc.setSheetName(updateBulkProcessConfigRequest.getSheetName());
            bpc.setInfoTag(updateBulkProcessConfigRequest.getInfoTag());
            bpc.setStateParam(updateBulkProcessConfigRequest.getStateParam());
            bpc.setToEmail(updateBulkProcessConfigRequest.getToEmail());
            bpc.setCreatedAt(new Date());
            bpc.setUpdatedAt(new Date());

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            bpc.setCreateDate(formatter.format(new Date()));
            bpc.setUpdateDate(formatter.format(new Date()));
            bpc = bulkProcessConfigurationRepository.save(bpc);
            Map<String, String> buttonDetails = new HashMap<>();
            buttonDetails.put(ResponseConstants.BUTTON_ID, bpc.getId());
            buttonDetails.put(ResponseConstants.BUTTON_NAME, bpc.getButtonName());
            //TODO : Move these endpoint to configuration
            StringBuffer bulkCreateEndPoint = new StringBuffer();
            bulkCreateEndPoint.append(hkcEndpoint).append(createSheetResourceURI).append(bpc.getId());
            StringBuffer processEndPoint = new StringBuffer();
            processEndPoint.append(hkcEndpoint).append(processSheetResourceURI).append(bpc.getId()).append("/sid");
            if(bpc.getStateParam() != null && !bpc.getStateParam().isBlank()) {
                processEndPoint.append("?state=${").append(bpc.getStateParam()).append("}");
            }

            buttonDetails.put(ResponseConstants.BULK_CREATE_END_POINT, bulkCreateEndPoint.toString());
            buttonDetails.put(ResponseConstants.PROCESS_END_POINT, processEndPoint.toString() );
            buttonDetails.put(ResponseConstants.INFO_TAG, bpc.getInfoTag());
            //Success
            genericApiResponse.buildSuccess(HttpStatus.OK, buttonDetails);
        } else {
            genericApiResponse.buildError(HttpStatus.BAD_REQUEST, validation);
        }
        return genericApiResponse;
    }

    @Override
    public List<BulkProcessConfiguration> fetchProcessingConfigurationByName(String name) {
            return bulkProcessConfigurationRepository.findByButtonName(name).orElseThrow(() -> new EntityNotFoundException(HKConnectorExceptionCodes.HKC_101));
    }


}
