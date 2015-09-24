package de.ids_mannheim.korap.web.service;

import de.ids_mannheim.korap.config.AuthCodeInfo;
import de.ids_mannheim.korap.config.BeanConfiguration;
import de.ids_mannheim.korap.config.ClientInfo;
import de.ids_mannheim.korap.exceptions.KustvaktException;
import de.ids_mannheim.korap.ext.config.BeanHelperExtension;
import de.ids_mannheim.korap.ext.security.oauth2.OAuth2Handler;
import de.ids_mannheim.korap.interfaces.EncryptionIface;
import de.ids_mannheim.korap.user.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author hanl
 * @date 13/05/2015
 */

//works
public class OAuth2HandlerTest {

    private static ClientInfo info;
    private static OAuth2Handler handler;
    private static EncryptionIface crypto;
    private static final String SCOPE = "search preferences queries account";
    private static final KorAPUser user = User.UserFactory.getUser("test_user");

    @BeforeClass
    public static void setup() throws KustvaktException {
        BeanConfiguration.loadClasspathContext("classpath-config.xml");
        BeanConfiguration.setCustomBeansHolder(new BeanHelperExtension());
        handler = new OAuth2Handler(
                BeanConfiguration.getBeans().getPersistenceClient());
        crypto = BeanConfiguration.getBeans().getEncryption();
        info = new ClientInfo(crypto.createID(), crypto.createToken());
        info.setConfidential(true);
        //todo: support for subdomains?!
        info.setUrl("http://localhost:8080/api/v0.1");
        info.setRedirect_uri("testwebsite/login");

        user.setPassword("testPassword123");
        BeanConfiguration.getBeans().getUserDBHandler().createAccount(user);
        handler.registerClient(info, user);
    }

    @AfterClass
    public static void drop() throws KustvaktException {
        handler.removeClient(info, user);
        BeanConfiguration.getBeans().getUserDBHandler()
                .deleteAccount(user.getId());
    }

    @Test
    public void testStoreAuthorizationCodeThrowsNoException()
            throws KustvaktException {
        String auth_code = crypto.createToken();
        AuthCodeInfo codeInfo = new AuthCodeInfo(info.getClient_id(),
                auth_code);
        codeInfo.setScopes(SCOPE);

        handler.authorize(codeInfo, user);
        codeInfo = handler.getAuthorization(auth_code);
        Assert.assertNotNull("client is null!", codeInfo);
    }

    @Test
    public void testAuthorizationCodeRemoveThrowsNoException()
            throws KustvaktException {
        String auth_code = crypto.createToken();
        AuthCodeInfo codeInfo = new AuthCodeInfo(info.getClient_id(),
                auth_code);
        codeInfo.setScopes(SCOPE);

        handler.authorize(codeInfo, user);
        String t = crypto.createToken();
        handler.addToken(codeInfo.getCode(), t, 7200);

        TokenContext ctx = handler.getContext(t);
        Assert.assertNotNull("context is null", ctx);

        AuthCodeInfo c2 = handler.getAuthorization(codeInfo.getCode());
        Assert.assertNull("clearing authorization failed", c2);
    }

    @Test
    public void testStoreAccessCodeViaAuthCodeThrowsNoException() {

    }

    @Test
    public void testDeleteAccessCodesByUserDeleteCascade() {

    }

    @Test
    public void testAccessTokenbyUserDeleteCascade() {

    }
}
