package de.ids_mannheim.korap.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Ignore;
import org.junit.Test;

import de.ids_mannheim.korap.config.Attributes;
import de.ids_mannheim.korap.config.BeansFactory;
import de.ids_mannheim.korap.config.BeanConfigTest;
import de.ids_mannheim.korap.config.TestHelper;
import de.ids_mannheim.korap.exceptions.KustvaktException;
import de.ids_mannheim.korap.interfaces.db.EntityHandlerIface;
import de.ids_mannheim.korap.interfaces.db.UserDataDbIface;
import de.ids_mannheim.korap.user.User;
import de.ids_mannheim.korap.user.UserDetails;
import de.ids_mannheim.korap.user.UserSettings;
import de.ids_mannheim.korap.user.Userdata;

;

/**
 * @author hanl
 * @date 13/02/2015
 */
public class UserDaoTest extends BeanConfigTest {

    @Test
    public void userput () throws KustvaktException {
        User user1 = helper().getContext().getUserDBHandler()
                .getAccount((String) TestHelper.getUserCredentials().get(Attributes.USERNAME));
        assertEquals("user creation failed", true, user1.getId() != -1);
    }


    @Test
    public void userputBatch () {
        //todo:
    }


    @Test
    public void userget () throws KustvaktException {
        User user1 = helper().getContext().getUserDBHandler()
                .getAccount((String) TestHelper.getUserCredentials().get(Attributes.USERNAME));
        assertEquals("User Retrieval does not work",
                TestHelper.getUserCredentials().get(Attributes.USERNAME), user1.getUsername());
    }


    @Test
    @Ignore
    public void testUserdetailsGetNonExistent () throws KustvaktException {
        helper().setupSimpleAccount("userdbtest", "userdbTest");
        User user = helper().getContext().getUserDBHandler()
                .getAccount("userdbtest");
        UserDataDbIface dao = BeansFactory.getTypeFactory()
                .getTypeInterfaceBean(
                        helper().getContext().getUserDataProviders(),
                        UserDetails.class);
        Userdata data = dao.get(user);
        assertNull(data);
        helper().dropUser("userdbtest");
    }


    @Test
    @Ignore
    public void testUserSettingsGetNonExistent () throws KustvaktException {
        helper().setupSimpleAccount("userdbtest", "userdbTest");
        User user = helper().getContext().getUserDBHandler()
                .getAccount("userdbtest");
        UserDataDbIface dao = BeansFactory.getTypeFactory()
                .getTypeInterfaceBean(
                        helper().getContext().getUserDataProviders(),
                        UserSettings.class);
        Userdata data = dao.get(user);
        assertNull(data);
        helper().dropUser("userdbtest");

    }


    // username cannot currently be changed
    @Test
    @Ignore
    public void updateUsername () throws KustvaktException {
        User user1 = helper().getContext().getUserDBHandler()
                .getAccount((String) TestHelper.getUserCredentials().get(Attributes.USERNAME));
        user1.setUsername("new_user");
        helper().getContext().getUserDBHandler().updateAccount(user1);
        User u2 = helper().getContext().getUserDBHandler()
                .getAccount("new_user");
        assertEquals("not found", user1.getUsername(), u2.getUsername());
    }


    @Test
    public void userupdate () throws KustvaktException {
        EntityHandlerIface dao = helper().getContext().getUserDBHandler();
        User user1 = dao.getAccount((String) TestHelper.getUserCredentials()
                .get(Attributes.USERNAME));
        user1.setAccountLocked(true);
        dao.updateAccount(user1);
        assertEquals("not valid", true, dao.getAccount(user1.getUsername())
                .isAccountLocked());
    }


    @Override
    public void initMethod () throws KustvaktException {
        helper().setupAccount();
//        helper().runBootInterfaces();
    }
}
