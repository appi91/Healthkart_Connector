package com.hk.HKConnector.Service;

import com.google.api.client.auth.oauth2.*;
import com.hk.HKConnector.Exception.*;

import java.io.*;
import java.security.*;
import java.util.*;

public interface GoogleSheetService {

    String processSheet(String buttonId, String googleSheetId, String email, Credential cr, Map<String, Object> stateMap) throws GeneralSecurityException, IOException, ClassNotFoundException, SheetProcessingException;

    String createSheet(String buttonId, String email, Credential cr) throws GeneralSecurityException, IOException;
}
