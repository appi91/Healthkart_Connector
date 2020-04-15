package com.hk.HKConnector.Util;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.*;
import com.google.api.client.json.*;
import com.google.api.client.json.jackson2.*;
import com.google.api.client.util.store.*;
import com.google.api.services.sheets.v4.*;
import com.google.gson.*;
import com.hk.HKConnector.Constants.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;

import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/*
 *
 * This has code reference from https://developers.google.com/sheets/api/quickstart/java focused on authentication and authorization
 *
 */
@Component
public class HKGoogleAuthUtil {


    //TODO: Remove hard coding | make it env spedific
    //private static final String CREDENTIALS_FILE_PATH = "/env/local/credentials.json";
    public static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS,
            "https://www.googleapis.com/auth/userinfo.email");

    public static final String TOKENS_DIRECTORY_PATH = "tokens";
    /**
     * Creates and returns a new {@link AuthorizationCodeFlow} for this app.
     */
    public AuthorizationCodeFlow newAuthorizationCodeFlow() throws IOException {
        // Load client secrets.
        InputStream in = HKGoogleAuthUtil.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        //TODO : Move this from directory to db
        return new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(), new JacksonFactory(),clientSecrets,
                SCOPES).setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH))).setAccessType("offline").build();
    }

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    /*private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = HKGoogleAuthUtil.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        //LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }*/

    public static String populateStateMapFromRequest(HttpServletRequest request, Map<String, Object> paramsForStateMap) {
        Map<String, Object> stateMap = new HashMap<>();
        Cookie jSession = HkUtil.getCookie(request, CookieConstant.COOKIE_JSESSION_ID);
        Map<String, Object> cookieMap = new HashMap<>();
        cookieMap.put(CookieConstant.COOKIE_JSESSION_ID, jSession);
        stateMap.put(HttpHeaders.COOKIE, cookieMap);
        stateMap.putAll(paramsForStateMap);
        Gson gson = new Gson();
        String json = gson.toJson(stateMap);
        return HkUtil.getBase64EncodedString(json);
    }
}
