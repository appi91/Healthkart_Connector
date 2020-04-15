package com.hk.HKConnector.Util;

import com.hk.HKConnector.Constants.*;
import com.sun.research.ws.wadl.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.client.*;

@Component
public class HkcRestClient<T> {

    private RestTemplate restTemplate;

    //Default value empty
    @Value("${clientId}")
    private String clientId;

    public ResponseEntity<T> post(String json, HttpHeaders headers , String url, Class<T> t ) {
        restTemplate = new RestTemplate();
        if(!url.startsWith("http")) {
            url = "https://" + url;
        }

        if(clientId != null) {
            headers.add(CoreConstants.HEADER_CLIENTID, clientId);
        }
        HttpEntity<T> requestEntity = new HttpEntity<T>((T) json, headers);
        ResponseEntity<T> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, t);
        return responseEntity;
    }

    //TODO : add all the required methods for rest
}
