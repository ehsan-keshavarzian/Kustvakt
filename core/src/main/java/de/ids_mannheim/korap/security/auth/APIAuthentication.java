package de.ids_mannheim.korap.security.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import de.ids_mannheim.korap.config.JWTSigner;
import de.ids_mannheim.korap.config.KustvaktCacheable;
import de.ids_mannheim.korap.config.KustvaktConfiguration;
import de.ids_mannheim.korap.exceptions.KustvaktException;
import de.ids_mannheim.korap.exceptions.StatusCodes;
import de.ids_mannheim.korap.interfaces.AuthenticationIface;
import de.ids_mannheim.korap.config.Attributes;
import de.ids_mannheim.korap.user.TokenContext;
import de.ids_mannheim.korap.user.User;
import de.ids_mannheim.korap.utils.NamingUtils;
import de.ids_mannheim.korap.utils.StringUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import java.text.ParseException;
import java.util.Map;

/**
 * Created by hanl on 5/23/14.
 */
public class APIAuthentication implements AuthenticationIface {

    private JWTSigner signedToken;
    private Cache invalided = CacheManager.getInstance().getCache(
            "id_tokens_inv");
    //private Cache id_tokens = CacheManager.getInstance().getCache("id_tokens");


    public APIAuthentication (KustvaktConfiguration config) {
        this.signedToken = new JWTSigner(config.getSharedSecret(),
                config.getIssuer(), config.getTokenTTL());
    }


    @Override
    public TokenContext getTokenContext(String authToken)
            throws KustvaktException {
        TokenContext context;
        //Element ein = invalided.get(authToken);
            try {
                context = signedToken.getTokenContext(authToken);
                context.setTokenType(Attributes.API_AUTHENTICATION);
            }
            catch (JOSEException | ParseException ex) {
                throw new KustvaktException(StatusCodes.ILLEGAL_ARGUMENT);
            }
        //context = (TokenContext) e.getObjectValue();
        //throw new KustvaktException(StatusCodes.EXPIRED);
        return context;
    }


    @Override
    public TokenContext createTokenContext(User user, Map<String, Object> attr)
            throws KustvaktException {
        TokenContext c = new TokenContext();
        c.setUsername(user.getUsername());
        SignedJWT jwt = signedToken.createJWT(user, attr);
        try {
            c.setExpirationTime(jwt.getJWTClaimsSet().getExpirationTimeClaim());
        }
        catch (ParseException e) {
            throw new KustvaktException(StatusCodes.ILLEGAL_ARGUMENT);
        }
        c.setTokenType(Attributes.API_AUTHENTICATION);
        c.setToken(jwt.serialize());
        //id_tokens.put(new Element(c.getToken(), c));
        return c;
    }


    // todo: cache and set expiration to token expiration. if token in that cache, it is not to be used anymore!
    //    @CacheEvict(value = "id_tokens", key = "#token")
    @Override
    public void removeUserSession (String token) throws KustvaktException {
        // invalidate token!
        invalided.put(new Element(token, null));
    }


    @Override
    public TokenContext refresh (TokenContext context) throws KustvaktException {
        return null;
    }


    @Override
    public String getIdentifier () {
        return Attributes.API_AUTHENTICATION;
    }

}
