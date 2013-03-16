package cz.cvut.authserver.oauth2.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.test.web.server.MockMvc;

import static cz.cvut.authserver.oauth2.Factories.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.server.setup.MockMvcBuilders.standaloneSetup;

/**
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
@RunWith(MockitoJUnitRunner.class)
public class CheckTokenEndpointTest {

    private static final String
            CHECK_TOKEN_URI = "/check-token?access_token=",
            MIME_TYPE_JSON = "application/json;charset=UTF-8";

    // JSON attributes
    private static final String
            CLIENT_ID = "client_id",
            SCOPE = "scope",
            AUDIENCE = "audience",
            CLIENT_AUTHORITIES = "client_authorities",
            EXPIRES_IN = "expires_in",
            USER_ID = "user_id",
            USER_EMAIL = "user_email",
            USER_AUTHORITIES = "user_authorities";

    private @Mock ResourceServerTokenServices tokenServices;
    private @InjectMocks CheckTokenEndpoint checkTokenEndpoint;

    private MockMvc controller;



    public @Before void buildMocks() {
        controller = standaloneSetup(checkTokenEndpoint).build();
    }


    public @Test void check_non_existing_token() throws Exception {
        doThrow(InvalidTokenException.class).when(tokenServices).readAccessToken("666");

        controller.perform(get(CHECK_TOKEN_URI + 666)
                .accept(APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    public @Test void check_expired_token() throws Exception {
        doReturn(createExpiredAccessToken()).when(tokenServices).readAccessToken("expired");

        controller.perform(get(CHECK_TOKEN_URI + "expired")
                .accept(APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    public @Test void check_valid_token() throws Exception {
        OAuth2AccessToken expectedToken = createRandomAccessToken();
        OAuth2Authentication expectedAuth = createRandomOAuth2Authentication(false);

        doReturn(expectedToken).when(tokenServices).readAccessToken("valid_token");
        doReturn(expectedAuth).when(tokenServices).loadAuthentication("valid_token");

        controller.perform(get(CHECK_TOKEN_URI + "valid_token")
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().mimeType(MIME_TYPE_JSON))
                .andExpect(jsonPath(CLIENT_ID, equalTo(expectedAuth.getAuthorizationRequest().getClientId())))
                .andExpect(jsonPath(SCOPE, hasItems(expectedToken.getScope().toArray())))
                .andExpect(jsonPath(AUDIENCE, hasItems(expectedAuth.getAuthorizationRequest().getResourceIds().toArray())))
                .andExpect(jsonPath(CLIENT_AUTHORITIES, hasItems(expectedAuth.getAuthorities().toArray())))
                .andExpect(jsonPath(EXPIRES_IN, equalTo(expectedToken.getExpiresIn())))
                .andExpect(jsonPath(USER_ID, equalTo(expectedAuth.getUserAuthentication().getPrincipal())));
                //.andExpect(jsonPath(USER_EMAIL, )))
                //.andExpect(jsonPath(USER_AUTHORITIES, hasItems(AuthorityUtils.authorityListToSet(expectedAuth.getUserAuthentication().getAuthorities()).toArray()))); TODO
    }
}
