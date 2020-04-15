package com.hk.HKConnector.Service.impl;

import ch.qos.logback.core.net.*;
import com.google.api.client.auth.oauth2.*;
import com.google.api.client.http.*;
import com.hk.HKConnector.Constants.*;
import com.hk.HKConnector.Controller.*;
import com.hk.HKConnector.Service.HKAuthService;
import com.hk.HKConnector.Util.*;
import org.apache.logging.log4j.message.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.logging.*;

@Service
public class HKAuthServiceImpl implements HKAuthService {

    private static final Logger logger = Logger.getLogger(HKAuthServiceImpl.class.getSimpleName());

    @Autowired
    protected HKGoogleAuthUtil hkGoogleAuthUtil;

    @Override
    public AuthorizationCodeFlow getGoogleNewAuthorizedCodeFlow() throws IOException {
        return hkGoogleAuthUtil.newAuthorizationCodeFlow();
    }

}