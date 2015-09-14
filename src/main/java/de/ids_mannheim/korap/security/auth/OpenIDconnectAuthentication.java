package de.ids_mannheim.korap.security.auth;

import com.nimbusds.jwt.SignedJWT;
import de.ids_mannheim.korap.config.JWTSigner;
import de.ids_mannheim.korap.config.KustvaktConfiguration;
import de.ids_mannheim.korap.exceptions.KustvaktException;
import de.ids_mannheim.korap.exceptions.StatusCodes;
import de.ids_mannheim.korap.handlers.OAuthDb;
import de.ids_mannheim.korap.interfaces.AuthenticationIface;
import de.ids_mannheim.korap.interfaces.PersistenceClient;
import de.ids_mannheim.korap.user.Attributes;
import de.ids_mannheim.korap.user.TokenContext;
import de.ids_mannheim.korap.user.User;
import de.ids_mannheim.korap.utils.StringUtils;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.text.ParseException;
import java.util.Map;

/**
 * @author hanl
 * @date 12/11/2014
 */
public class OpenIDconnectAuthentication implements AuthenticationIface {

    private OAuthDb database;
    private KustvaktConfiguration config;

    public OpenIDconnectAuthentication(KustvaktConfiguration config,
            PersistenceClient client) {
        this.database = new OAuthDb(client);
        this.config = config;
    }

    @Cacheable(value = "id_tokens", key = "#authToken")
    @Override
    public TokenContext getUserStatus(String authToken)
            throws KustvaktException {
        authToken = StringUtils.stripTokenType(authToken);
        return this.database.getContext(authToken);
    }

    @Override
    public TokenContext createUserSession(User user, Map<String, Object> attr)
            throws KustvaktException {
        JWTSigner signer = new JWTSigner(
                ((String) attr.get(Attributes.CLIENT_SECRET)).getBytes(),
                config.getIssuer(), config.getTokenTTL());
        TokenContext c = new TokenContext(user.getUsername());
        SignedJWT jwt = signer.createJWT(user, attr);
        try {
            c.setExpirationTime(jwt.getJWTClaimsSet().getExpirationTimeClaim());
        }catch (ParseException e) {
            throw new KustvaktException(StatusCodes.ILLEGAL_ARGUMENT);
        }
        c.setTokenType(Attributes.OPENID_AUTHENTICATION);
        c.setToken(jwt.serialize());
        CacheManager.getInstance().getCache("id_tokens")
                .put(new Element(c.getToken(), c));
        return c;
    }

    @CacheEvict(value = "id_tokens", key = "#token")
    @Override
    public void removeUserSession(String token) throws KustvaktException {
        // emit token from cache only
    }

    @Override
    public TokenContext refresh(TokenContext context) throws KustvaktException {
        throw new UnsupportedOperationException("method not supported");
    }

    @Override
    public String getIdentifier() {
        return Attributes.OPENID_AUTHENTICATION;
    }
}
