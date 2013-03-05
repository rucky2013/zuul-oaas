package cz.cvut.authserver.oauth2.dao;

import cz.cvut.authserver.oauth2.models.PersistableAccessToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.Collection;

/**
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public interface AccessTokenDAO extends CrudRepository<PersistableAccessToken, String> {

    PersistableAccessToken findOneByAuthentication(OAuth2Authentication authentication);

    Collection<OAuth2AccessToken> findByClientId(String clientId);

    Collection<OAuth2AccessToken> findByUserName(String userName);

    void deleteByRefreshToken(OAuth2RefreshToken refreshToken);
}
