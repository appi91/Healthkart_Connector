package com.hk.HKConnector.Controller;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.*;
import com.google.gson.*;
import com.google.gson.reflect.*;
import com.hk.HKConnector.Constants.*;
import com.hk.HKConnector.Exception.*;
import com.hk.HKConnector.Repository.*;
import com.hk.HKConnector.Service.*;
import com.hk.HKConnector.Util.*;
import com.hk.HKConnector.model.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;

import javax.servlet.http.*;
import javax.ws.rs.*;
import java.io.*;
import java.security.*;
import java.util.*;
import java.util.logging.*;

@Slf4j
@Controller
@RequestMapping("/process")
public class ProcessController {

    @Autowired
    private HKAuthService hkAuthService;

    @Autowired
    private GoogleSheetService googleSheetService;

    @Autowired
    private GoogleUserProfileMappingRepository googleUserProfileMappingRepository;

    @Value("${endpoint.hkc}")
    private String hkcEndpoint;

    @Value("${google.redirect.sheet.create}")
    private String createSheetResourceRedirectURI;

    @Value("${google.redirect.sheet.process}")
    private String processSheetResourceRedirectURI;

    @GetMapping("/get/sheet/{buttonId}")
    public ModelAndView getSheetDetails(HttpServletRequest request, @PathVariable("buttonId") String buttonId,
                                  @QueryParam("code") String code) {
        String sheetUrl = null;
        ModelAndView mv;
        try {
            AuthorizationCodeFlow authorizationCodeFlow = hkAuthService.getGoogleNewAuthorizedCodeFlow();
            if(StringUtils.isEmpty(code)) {
                StringBuffer redirectUrl = new StringBuffer(hkcEndpoint).append(createSheetResourceRedirectURI);
                Map<String, Object> stateMapParams = new HashMap<>();
                stateMapParams.put(CoreConstants.BUTTON_ID, buttonId);
                String stateEncoded = HKGoogleAuthUtil.populateStateMapFromRequest(request, stateMapParams);
                GenericUrl url = authorizationCodeFlow.newAuthorizationUrl().setRedirectUri(redirectUrl.toString()).setState(stateEncoded);
                // the user will only see the consent page the first time they go
                // through the sequence
                url.set("approval_prompt", "auto");
                return new ModelAndView("redirect:" + url.build());
            } else {
                //Fetching token
                TokenResponse tokenResponse = authorizationCodeFlow.newTokenRequest(code).setRedirectUri(request.getRequestURL().toString()).execute();
                //If token is expired -> Refresh token | Not required as
                if(tokenResponse == null) {

                }
                //On Success Create sheet
                String profileId = ((GoogleTokenResponse) tokenResponse).parseIdToken().getPayload().getSubject();
                String email = ((GoogleTokenResponse) tokenResponse).parseIdToken().getPayload().getEmail();
                if(googleUserProfileMappingRepository.findByProfileId(profileId) == null) {
                    //ProfileId and Email mapping
                    GoogleUserProfileMapping gpm = new GoogleUserProfileMapping();
                    gpm.setProfileId(profileId);
                    gpm.setEmailId(email);
                    gpm.setCreateDt(new Date());
                    gpm.setComments(GoogleConstants.GOOGLE_PROFILE_FOUND_WHILE_SHEET_CREATION);
                    googleUserProfileMappingRepository.save(gpm);
                }
                Credential cr = authorizationCodeFlow.createAndStoreCredential(tokenResponse, profileId);
                sheetUrl = googleSheetService.createSheet(buttonId,email, cr);
            }
        } catch (IOException | GeneralSecurityException | EntityNotFoundException e) {
            log.error(MessageConstants.CRITICAL_ALERT + " Exception in fetching AuthorizationCodeFlow " + e.getMessage(), e);
            mv = new ModelAndView("ProcessingError.html");
            mv.addObject("error", e.getMessage());
            return mv;
        } catch (Exception e) {
            log.error(MessageConstants.CRITICAL_ALERT + " Exception in fetching AuthorizationCodeFlow " + e.getMessage(), e);
            mv = new ModelAndView("ProcessingError.html");
            mv.addObject("error", e.getMessage());
            return mv;
        }

        return new ModelAndView("redirect:" + sheetUrl);
    }

    @GetMapping("/google/sheet/create")
    public ModelAndView getSheetDetailsRedirected(HttpServletRequest request,
                                  @QueryParam("code") String code,
                                  @QueryParam("state") String state) {
        String sheetUrl = null;
        ModelAndView mv;
        try {
            AuthorizationCodeFlow authorizationCodeFlow = hkAuthService.getGoogleNewAuthorizedCodeFlow();
            if(StringUtils.isEmpty(code)) {
                GenericUrl url = authorizationCodeFlow.newAuthorizationUrl().setRedirectUri(request.getRequestURL().toString()).setState(state);
                // the user will only see the consent page the first time they go
                // through the sequence
                url.set("approval_prompt", "auto");
                return new ModelAndView("redirect:" + url.build());
            } else {
                //Fetching token
                TokenResponse tokenResponse = authorizationCodeFlow.newTokenRequest(code).setRedirectUri(request.getRequestURL().toString()).execute();
                Map<String, Object> stateMap = new HashMap<>();
                if(!StringUtils.isEmpty(state)) {
                    String decodedStateString = HkUtil.getDecodedStringEncodedByBase64(state);
                    stateMap = new Gson().fromJson(
                            decodedStateString, new TypeToken<HashMap<String, Object>>() {}.getType());
                }
                //On Success Create sheet
                String profileId = ((GoogleTokenResponse) tokenResponse).parseIdToken().getPayload().getSubject();
                String email = ((GoogleTokenResponse) tokenResponse).parseIdToken().getPayload().getEmail();
                if(googleUserProfileMappingRepository.findByProfileId(profileId) == null) {
                    //ProfileId and Email mapping
                    GoogleUserProfileMapping gpm = new GoogleUserProfileMapping();
                    gpm.setProfileId(profileId);
                    gpm.setEmailId(email);
                    gpm.setCreateDt(new Date());
                    gpm.setComments(GoogleConstants.GOOGLE_PROFILE_FOUND_WHILE_SHEET_CREATION);
                    googleUserProfileMappingRepository.save(gpm);
                }
                Credential cr = authorizationCodeFlow.createAndStoreCredential(tokenResponse, profileId);
                sheetUrl = googleSheetService.createSheet(stateMap.get(CoreConstants.BUTTON_ID).toString(),email, cr);
            }
        } catch (IOException | GeneralSecurityException | EntityNotFoundException e) {
            log.error(MessageConstants.CRITICAL_ALERT + " Exception in fetching AuthorizationCodeFlow " + e.getMessage(), e);
            mv = new ModelAndView("ProcessingError.html");
            mv.addObject("error", e.getMessage());
            return mv;
        } catch (Exception e) {
            log.error(MessageConstants.CRITICAL_ALERT + " Exception in fetching AuthorizationCodeFlow " + e.getMessage(), e);
            mv = new ModelAndView("ProcessingError.html");
            mv.addObject("error", e.getMessage());
            return mv;
        }

        return new ModelAndView("redirect:" + sheetUrl);
    }


    public String redirectToUserForPermission(String fromUrl) {
        try {
            AuthorizationCodeFlow authorizationCodeFlow = hkAuthService.getGoogleNewAuthorizedCodeFlow();
            GenericUrl url = authorizationCodeFlow.newAuthorizationUrl().setRedirectUri(fromUrl);
            // the user will only see the consent page the first time they go
            // through the sequence
            url.set("approval_prompt", "auto");
            return "redirect:" + url.build();
        } catch (IOException e) {
            log.error(MessageConstants.CRITICAL_ALERT + " Exception in fetching AuthorizationCodeFlow " + e.getMessage(), e);
        }

        return null;
    }

    @GetMapping("/start/sheet/{buttonId}/{googleSheetId}")
    public ModelAndView startProcessingGoogleSheetForUserButton(HttpServletRequest request, @PathVariable("buttonId") String buttonId,
                                                                @PathVariable("googleSheetId") String googleSheetId,
                                                                @QueryParam("code") String code,
                                                                @QueryParam("state") String state) {
        //Check for google Authorization
        ModelAndView mv;
        try {
            AuthorizationCodeFlow authorizationCodeFlow = hkAuthService.getGoogleNewAuthorizedCodeFlow();
            if(StringUtils.isEmpty(code)) {
                String stateEncoded;
                Map<String, Object> stateMap = new HashMap<>();
                if(!StringUtils.isEmpty(state)) {
                    String decodedStateString = HkUtil.getDecodedStringEncodedByBase64(state);
                    stateMap = new Gson().fromJson(
                            decodedStateString, new TypeToken<HashMap<String, Object>>() {}.getType());
                }
                stateMap.put(CoreConstants.BUTTON_ID, buttonId);
                stateMap.put(CoreConstants.GOOGLE_SHEET_ID, googleSheetId);
                stateEncoded = HKGoogleAuthUtil.populateStateMapFromRequest(request, stateMap);
                StringBuffer redirectUrl = new StringBuffer(hkcEndpoint).append(processSheetResourceRedirectURI);
                GenericUrl url = authorizationCodeFlow.newAuthorizationUrl().setRedirectUri(redirectUrl.toString()).setState(stateEncoded);
                // the user will only see the consent page the first time they go
                // through the sequence
                url.set("approval_prompt", "auto");

                return new ModelAndView("redirect:" + url.build());
            } else {
                //Fetching token
                TokenResponse tokenResponse = authorizationCodeFlow.newTokenRequest(code).setRedirectUri(request.getRequestURL().toString()).execute();

                //On Success process sheet
                String profileId = ((GoogleTokenResponse) tokenResponse).parseIdToken().getPayload().getSubject();
                Credential cr = authorizationCodeFlow.createAndStoreCredential(tokenResponse, profileId);
                String email = ((GoogleTokenResponse) tokenResponse).parseIdToken().getPayload().getEmail();
                if(googleUserProfileMappingRepository.findByProfileId(profileId) == null) {
                    //ProfileId and Email mapping
                    GoogleUserProfileMapping gpm = new GoogleUserProfileMapping();
                    gpm.setProfileId(profileId);
                    gpm.setEmailId(email);
                    gpm.setCreateDt(new Date());
                    gpm.setComments(GoogleConstants.GOOGLE_PROFILE_FOUND_WHILE_SHEET_PROCESSING);
                    googleUserProfileMappingRepository.save(gpm);
                }
                Map<String, Object> stateMap = new HashMap<>();
                if(!StringUtils.isEmpty(state)) {
                    String decodedStateString = HkUtil.getDecodedStringEncodedByBase64(state);
                    stateMap = new Gson().fromJson(
                            decodedStateString, new TypeToken<HashMap<String, Object>>() {}.getType());
                }
                googleSheetService.processSheet(buttonId, googleSheetId, email, cr, stateMap);
                mv = new ModelAndView("sheetProcessed.html");
                mv.addObject("sheet", GoogleConstants.GOOGLE_SHEET_URI_FORMAT.replace("spreadsheetId", googleSheetId));
            }
        } catch (IOException | EntityNotFoundException | GeneralSecurityException | ClassNotFoundException | SheetProcessingException e) {

            log.error(MessageConstants.CRITICAL_ALERT + " Exception in sheet processing " + e.getMessage(), e);
            mv = new ModelAndView("sheetProcessingError.html");
            mv.addObject("sheet", GoogleConstants.GOOGLE_SHEET_URI_FORMAT.replace("spreadsheetId", googleSheetId));
            mv.addObject("error", e.getMessage());
        } catch (Exception e) {
            log.error(MessageConstants.CRITICAL_ALERT + " Exception in sheet processing " + e.getMessage(), e);
            mv = new ModelAndView("sheetProcessingError.html");
            mv.addObject("sheet", GoogleConstants.GOOGLE_SHEET_URI_FORMAT.replace("spreadsheetId", googleSheetId));
            mv.addObject("error", e.getMessage());
        }

        return mv;
    }

    @GetMapping("/google/sheet/start")
    public ModelAndView startProcessingGoogleSheetForUserButtonRedirected(HttpServletRequest request,
                                                                @QueryParam("code") String code,
                                                                @QueryParam("state") String state) {
        //Check for google Authorization
        ModelAndView mv;
        String googleSheetId = "";
        try {
            AuthorizationCodeFlow authorizationCodeFlow = hkAuthService.getGoogleNewAuthorizedCodeFlow();
            Map<String, Object> stateMap = new HashMap<>();
            if(!StringUtils.isEmpty(state)) {
                String decodedStateString = HkUtil.getDecodedStringEncodedByBase64(state);
                stateMap = new Gson().fromJson(
                        decodedStateString, new TypeToken<HashMap<String, Object>>() {}.getType());
            }
            if(stateMap.containsKey(CoreConstants.GOOGLE_SHEET_ID)) {
                googleSheetId = stateMap.get(CoreConstants.GOOGLE_SHEET_ID).toString();
            }
            if(StringUtils.isEmpty(code)) {
                GenericUrl url = authorizationCodeFlow.newAuthorizationUrl().setRedirectUri(request.getRequestURL().toString()).setState(state);
                // the user will only see the consent page the first time they go
                // through the sequence
                url.set("approval_prompt", "auto");

                return new ModelAndView("redirect:" + url.build());
            } else {
                //Fetching token
                TokenResponse tokenResponse = authorizationCodeFlow.newTokenRequest(code).setRedirectUri(request.getRequestURL().toString()).execute();

                //On Success process sheet
                String profileId = ((GoogleTokenResponse) tokenResponse).parseIdToken().getPayload().getSubject();
                Credential cr = authorizationCodeFlow.createAndStoreCredential(tokenResponse, profileId);
                String email = ((GoogleTokenResponse) tokenResponse).parseIdToken().getPayload().getEmail();
                if(googleUserProfileMappingRepository.findByProfileId(profileId) == null) {
                    //ProfileId and Email mapping
                    GoogleUserProfileMapping gpm = new GoogleUserProfileMapping();
                    gpm.setProfileId(profileId);
                    gpm.setEmailId(email);
                    gpm.setCreateDt(new Date());
                    gpm.setComments(GoogleConstants.GOOGLE_PROFILE_FOUND_WHILE_SHEET_PROCESSING);
                    googleUserProfileMappingRepository.save(gpm);
                }
                googleSheetService.processSheet(stateMap.get(CoreConstants.BUTTON_ID).toString(), stateMap.get(CoreConstants.GOOGLE_SHEET_ID).toString(), email, cr, stateMap);
                mv = new ModelAndView("sheetProcessed.html");
                mv.addObject("sheet", GoogleConstants.GOOGLE_SHEET_URI_FORMAT.replace("spreadsheetId", stateMap.get(CoreConstants.GOOGLE_SHEET_ID).toString()));
            }
        } catch (IOException | EntityNotFoundException | GeneralSecurityException | ClassNotFoundException | SheetProcessingException e) {

            log.error(MessageConstants.CRITICAL_ALERT + " Exception in sheet processing " + e.getMessage(), e);
            mv = new ModelAndView("sheetProcessingError.html");
            mv.addObject("sheet", GoogleConstants.GOOGLE_SHEET_URI_FORMAT.replace("spreadsheetId", googleSheetId));
            mv.addObject("error", e.getMessage());
        } catch (Exception e) {
            log.error(MessageConstants.CRITICAL_ALERT + " Exception in sheet processing " + e.getMessage(), e);
            mv = new ModelAndView("sheetProcessingError.html");
            mv.addObject("sheet", GoogleConstants.GOOGLE_SHEET_URI_FORMAT.replace("spreadsheetId", googleSheetId));
            mv.addObject("error", e.getMessage());
        }

        return mv;
    }


}
