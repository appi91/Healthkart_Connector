package com.hk.HKConnector.Controller;

import com.hk.HKConnector.Constants.*;
import com.hk.HKConnector.Exception.*;
import com.hk.HKConnector.Request.*;
import com.hk.HKConnector.Response.*;
import com.hk.HKConnector.Service.*;
import com.hk.HKConnector.Util.*;
import com.hk.HKConnector.model.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;

import javax.ws.rs.*;
import java.util.*;

@Controller
@RequestMapping("/bulk/process")
public class BulkProcessConfigController {

    @Autowired
    private BulkProcessConfigService bulkProcessConfigService;

    @Value("${endpoint.hkc}")
    private String hkcEndpoint;

    @Value("${uri.sheet.create}")
    private String createSheetResourceURI;

    @Value("${uri.sheet.process}")
    private String processSheetResourceURI;

    @PostMapping("/create/channel")
    public @ResponseBody GenericApiResponse createChannel(@RequestBody CreateChannelRequest createChannelRequest) {
        return bulkProcessConfigService.createChannel(createChannelRequest);
    }

    @PostMapping("/create/bulkConfig")
    public @ResponseBody GenericApiResponse createBulkProcessConfig(@RequestBody CreateBulkProcessConfigRequest createBulkProcessConfigRequest) {
        return bulkProcessConfigService.createBulkProcessConfig(createBulkProcessConfigRequest);
    }

    @GetMapping("/get/channels")
    public @ResponseBody GenericApiResponse getChannels() {
        GenericApiResponse genericApiResponse = new GenericApiResponse();
        List<Channel> channels = bulkProcessConfigService.fetchAllChannels();
        genericApiResponse.buildSuccess(HttpStatus.OK, channels);
        return  genericApiResponse;
    }

    @GetMapping("/page/allChannels")
    public ModelAndView getChannelView() {
        ModelAndView mv = new ModelAndView("dashboard.html");
        List<Channel> channels = bulkProcessConfigService.fetchAllChannels();
        mv.addObject("channels", channels);
        return mv;
    }

    @GetMapping("/get/params/create/config")
    public @ResponseBody GenericApiResponse getParamsForCreateConfig() {
        GenericApiResponse genericApiResponse = new GenericApiResponse();
        List<Channel> channels = bulkProcessConfigService.fetchAllChannels();
        Map<String, Object> response = new HashMap<>();
        response.put(ResponseConstants.CHANNELS, channels);
        response.put(ResponseConstants.DATA_TYPES, HkUtil.getDataTypesAllowedOnBulkConfig());
        genericApiResponse.buildSuccess(HttpStatus.OK, response);
        return  genericApiResponse;
    }

    @PutMapping("/edit/channel/{id}")
    public @ResponseBody GenericApiResponse updateChannel(@PathVariable("id") String channelId,
                                                          @RequestBody UpdateChannelRequest updateChannelRequest) {
        GenericApiResponse genericApiResponse = new GenericApiResponse();
        if(!channelId.equalsIgnoreCase(updateChannelRequest.getCId())) {
            genericApiResponse.buildError(HttpStatus.UNPROCESSABLE_ENTITY, "Mismatch in entity id to be updated!");
        } else {
            String response = bulkProcessConfigService.updateChannel(updateChannelRequest);
            if(StringUtils.isEmpty(response)) {
                genericApiResponse.buildSuccess(HttpStatus.OK, "Updated successfully!!");
            } else {
                genericApiResponse.buildError(HttpStatus.UNPROCESSABLE_ENTITY, response);
            }
        }

        return genericApiResponse;
    }


    @GetMapping("/get/page/bulkConfig")
    public @ResponseBody
    GenericApiResponse getAllBulkConfigPagination(@DefaultValue("0") @RequestParam("pgNo") Integer pageNum, @DefaultValue("10") @RequestParam("perPg") Integer perPage) {

        Page<BulkProcessConfiguration> bpcPage = bulkProcessConfigService.findAllBulkProcessConfigPagination(pageNum, perPage);
        GenericApiResponse genericApiResponse = new GenericApiResponse();
        Map<String, Object> response = new HashMap<>();
        response.put("page", bpcPage);
        StringBuffer bulkCreateEndPoint = new StringBuffer();
        bulkCreateEndPoint.append(hkcEndpoint).append(createSheetResourceURI).append("bid");
        StringBuffer processEndPoint = new StringBuffer();
        processEndPoint.append(hkcEndpoint).append(processSheetResourceURI).append("bid").append("/sid");
        processEndPoint.append("?state=${").append("sParam").append("}");
        response.put(ResponseConstants.BULK_CREATE_END_POINT, bulkCreateEndPoint.toString());
        response.put(ResponseConstants.PROCESS_END_POINT, processEndPoint.toString() );

        genericApiResponse.buildSuccess(HttpStatus.OK, response);
        return genericApiResponse;
    }

    @PutMapping("/edit/bulkConfig/{id}")
    public @ResponseBody GenericApiResponse updateChannel(@PathVariable("id") String bulkConfigId,
                                                          @RequestBody UpdateBulkProcessConfigRequest
                                                                  updateBulkProcessConfigRequest) {
        GenericApiResponse genericApiResponse = new GenericApiResponse();
        if(!bulkConfigId.equalsIgnoreCase(updateBulkProcessConfigRequest.getId())) {
            genericApiResponse.buildError(HttpStatus.UNPROCESSABLE_ENTITY, "Mismatch in entity id to be updated!");
        } else {
            genericApiResponse = bulkProcessConfigService.updateBulkProcessConfig(updateBulkProcessConfigRequest);
        }

        return genericApiResponse;
    }

    @GetMapping("/search/bulkConfig")
    public @ResponseBody GenericApiResponse getBulkConfigDetail(@QueryParam("id") String id,
                                                                @QueryParam("buttonName") String buttonName ) {
        GenericApiResponse genericApiResponse = new GenericApiResponse();
        try {
            Map<String, Object> response = new HashMap<>();
            if (id != null && !id.isBlank()) {
                BulkProcessConfiguration bpc = bulkProcessConfigService.fetchProcessingConfigurationById(id);
                List<BulkProcessConfiguration> bpcList = new LinkedList<>();
                bpcList.add(bpc);
                response.put("config", bpcList);
            } else if (buttonName != null && !buttonName.isBlank()) {
                List<BulkProcessConfiguration> bpcList = bulkProcessConfigService.fetchProcessingConfigurationByName(buttonName);
                response.put("config", bpcList);
            } else {
                genericApiResponse.build(HttpStatus.BAD_REQUEST);
            }

            if(genericApiResponse.getCode() == null || HttpStatus.valueOf(genericApiResponse.getCode()).is2xxSuccessful()) {
                StringBuffer bulkCreateEndPoint = new StringBuffer();
                bulkCreateEndPoint.append(hkcEndpoint).append(createSheetResourceURI).append("bid");
                StringBuffer processEndPoint = new StringBuffer();
                processEndPoint.append(hkcEndpoint).append(processSheetResourceURI).append("bid").append("/sid");
                processEndPoint.append("?state=${").append("sParam").append("}");
                response.put(ResponseConstants.BULK_CREATE_END_POINT, bulkCreateEndPoint.toString());
                response.put(ResponseConstants.PROCESS_END_POINT, processEndPoint.toString());
                genericApiResponse.buildSuccess(HttpStatus.OK, response);
            }
        } catch (EntityNotFoundException e) {
            genericApiResponse.buildSuccess(HttpStatus.NO_CONTENT, "Button Not found!!");
        }


        return genericApiResponse;
    }
}
