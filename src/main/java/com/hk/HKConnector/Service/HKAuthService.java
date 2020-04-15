package com.hk.HKConnector.Service;

import com.google.api.client.auth.oauth2.*;

import java.io.*;

public interface HKAuthService {

    AuthorizationCodeFlow getGoogleNewAuthorizedCodeFlow() throws IOException;

    //void redirectToUserForPermission(String fromUrl);

}
