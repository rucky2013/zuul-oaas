package cz.cvut.zuul.oaas.api.resources;

import cz.cvut.zuul.oaas.api.models.ClientDTO;
import cz.cvut.zuul.oaas.api.models.JsonExceptionMapping;
import cz.cvut.zuul.oaas.api.models.TokenDetails;
import cz.cvut.zuul.oaas.dao.AccessTokenDAO;
import cz.cvut.zuul.oaas.models.PersistableAccessToken;
import cz.cvut.zuul.oaas.services.ClientsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;


/**
 * API for authorization server tokens' management.
 * 
 * @author Tomáš Maňo <tomasmano@gmail.com>
 */
@Controller
@RequestMapping("/v1/tokens")
public class TokensController {

    private AccessTokenDAO tokenDao;
    private ClientsService clientsService;
    
    @ResponseBody
    @RequestMapping(value = "{tokenValue}", method = GET)
    public TokenDetails getTokenDetails(@PathVariable String tokenValue) {
        PersistableAccessToken token = tokenDao.findOne(tokenValue);

        if (token == null) {
            throw new InvalidTokenException("Token was not recognised");
        }

        OAuth2Authentication authentication = token.getAuthentication();
        AuthorizationRequest clientAuth = authentication.getAuthorizationRequest();
        Authentication userAuth = authentication.getUserAuthentication();

        ClientDTO client = null; 
        try {
            client = clientsService.findClientById(token.getAuthenticatedClientId());
        } catch (NoSuchClientException ex) {
            throw new InvalidTokenException("Client doesn't exist anymore");
        }

        return new TokenDetails(token, clientAuth.isDenied(), client, userAuth);
    }
    
    @ResponseStatus(NO_CONTENT)
    @RequestMapping(value = "{tokenValue}", method = DELETE)
    public void invalidateToken(@PathVariable String tokenValue){
        if (! tokenDao.exists(tokenValue)) {
            throw new InvalidTokenException("Token was not recognised");
        }
        tokenDao.delete(tokenValue);
    }


    //////////  Exceptions Handling  //////////

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ResponseBody
    public JsonExceptionMapping handleTokenProblem(InvalidTokenException ex) {
        return new JsonExceptionMapping(CONFLICT.value(), ex.getOAuth2ErrorCode(), ex.getMessage());
    }

    
    //////////  Accessors  //////////

    @Autowired
    public void setTokenDao(AccessTokenDAO tokenDao) {
        this.tokenDao = tokenDao;
    }

    @Autowired
    public void setClientsService(ClientsService clientsService) {
        this.clientsService = clientsService;
    }

}