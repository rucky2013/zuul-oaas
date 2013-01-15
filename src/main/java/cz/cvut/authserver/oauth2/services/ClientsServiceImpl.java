package cz.cvut.authserver.oauth2.services;

import cz.cvut.authserver.oauth2.generators.OAuth2ClientCredentialsGenerator;
import java.lang.String;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.BaseClientDetails;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationService;
import org.springframework.security.oauth2.provider.NoSuchClientException;

/**
 *
 *
 * @author Tomas Mano <tomasmano@gmail.com>
 */
public class ClientsServiceImpl implements ClientsService {

    private ClientDetailsService clientDetailsService;
    
    private ClientRegistrationService clientRegistrationService;
    
    private OAuth2ClientCredentialsGenerator oauth2ClientCredentialsGenerator;
    
    
    //////////  Business methods  //////////

    @Override
    public ClientDetails findClientDetailsById(String clientId) throws OAuth2Exception {
        return clientDetailsService.loadClientByClientId(clientId);
    }

    @Override
    public void createClientDetails(BaseClientDetails client) throws Exception {
       
        // generate oauth2 client credentials
        String clientId = oauth2ClientCredentialsGenerator.generateClientId();
        String clientSecret = oauth2ClientCredentialsGenerator.generateClientSecret();

        // setting necessary fields
        client.setClientId(clientId);
        client.setClientSecret(clientSecret);
        client.setAuthorities(AuthorityUtils.createAuthorityList("ROLE_CLIENT"));

        // save
        clientRegistrationService.addClientDetails(client);
    }

    @Override
    public void removeClientDetails(String clientId) throws NoSuchClientException {
        clientRegistrationService.removeClientDetails(clientId);
    }

    @Override
    public void resetClientSecret(String clientId) throws NoSuchClientException {
        String newSecret = oauth2ClientCredentialsGenerator.generateClientSecret();
        clientRegistrationService.updateClientSecret(clientId, newSecret);
    }

    @Override
    public void addScopeToClientDetails(String clientId, String scope) throws Exception {
        ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
        if (client.getScope().isEmpty()) {
            client = handleEmptyCollection(client, "scope");
        }
        client.getScope().add(scope);
        clientRegistrationService.updateClientDetails(client);
    }

    @Override
    public void removeScopeFromClientDetails(String clientId, String scope) throws Exception {
        ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
        if (client.getScope().contains(scope)) {
            client.getScope().remove(scope);
            clientRegistrationService.updateClientDetails(client);
        }
    }

    @Override
    public void addGrantToClientDetails(String clientId, String grantType) throws Exception {
        ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
        if (client.getAuthorizedGrantTypes().isEmpty()) {
            client = handleEmptyCollection(client, "authorizedGrantTypes");
        }
        client.getAuthorizedGrantTypes().add(grantType);
        clientRegistrationService.updateClientDetails(client);
    }

    @Override
    public void deleteGrantFromClientDetails(String clientId, String grantType) throws Exception {
        ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
        if (client.getAuthorizedGrantTypes().contains(grantType)) {
            client.getAuthorizedGrantTypes().remove(grantType);
            clientRegistrationService.updateClientDetails(client);
        }
    }

    @Override
    public void addRoleToClientDetails(String clientId, String role) throws Exception {
        ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
        if (client.getAuthorities().isEmpty()) {
            client = handleEmptyCollection(client, "authorities");
        }
        client.getAuthorities().addAll(AuthorityUtils.createAuthorityList(role));
        clientRegistrationService.updateClientDetails(client);
    }

    @Override
    public void deleteRoleFromClientDetails(String clientId, String role) throws Exception {
        ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
        GrantedAuthority removed = new SimpleGrantedAuthority(role);
        if (client.getAuthorities().contains(removed)) {
            client.getAuthorities().remove(removed);
            clientRegistrationService.updateClientDetails(client);
        }
    }
    
    /**
     * Helper method for dealing with the immutable Collections of the
     * BaseClientDetails. Initialize inner collection by the given attribute
     * with muttable collection.
     *
     * @param client which immutable collection will be change for mutable collection
     * @param attr attribute's name which collection will be initialized
     * @return ClientDetails with mutable Collection by the given attr
     */
    private ClientDetails handleEmptyCollection(ClientDetails client, String attr) throws Exception {
        BaseClientDetails baseClientDetails = (BaseClientDetails) client;
        Class clazz = baseClientDetails.getClass();
        Method setter = clazz.getMethod(String.format("set%s", StringUtils.capitalize(attr)), Collection.class);
        setter.invoke(baseClientDetails, new LinkedHashSet<String>());
        client = baseClientDetails;
        return client;
    }
    
    //////////  Getters / Setters  //////////

    public ClientDetailsService getClientDetailsService() {
        return clientDetailsService;
    }

    public void setClientDetailsService(ClientDetailsService clientDetailsService) {
        this.clientDetailsService = clientDetailsService;
    }

    public ClientRegistrationService getClientRegistrationService() {
        return clientRegistrationService;
    }

    public void setClientRegistrationService(ClientRegistrationService clientRegistrationService) {
        this.clientRegistrationService = clientRegistrationService;
    }

    public OAuth2ClientCredentialsGenerator getOauth2ClientCredentialsGenerator() {
        return oauth2ClientCredentialsGenerator;
    }

    public void setOauth2ClientCredentialsGenerator(OAuth2ClientCredentialsGenerator oauth2ClientCredentialsGenerator) {
        this.oauth2ClientCredentialsGenerator = oauth2ClientCredentialsGenerator;
    }
}