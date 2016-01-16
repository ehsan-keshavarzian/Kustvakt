package de.ids_mannheim.korap.config;

import de.ids_mannheim.korap.exceptions.KustvaktException;
import de.ids_mannheim.korap.interfaces.db.EntityHandlerIface;
import de.ids_mannheim.korap.interfaces.db.PersistenceClient;
import de.ids_mannheim.korap.user.Attributes;
import de.ids_mannheim.korap.user.User;
import org.junit.Assert;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * creates a test user that can be used to access protected functions
 *
 * @author hanl
 * @date 16/10/2015
 */
public class UserTestHelper {

    private static final String[] credentials = new String[] { "test1",
            "testPass2015" };

    public static boolean setup() {
        boolean r = BeanConfiguration.hasContext();
        if (r) {
            EntityHandlerIface dao = BeanConfiguration.getBeans()
                    .getUserDBHandler();
            Map m = new HashMap<>();
            m.put(Attributes.USERNAME, credentials[0]);
            m.put(Attributes.PASSWORD, credentials[1]);

            Assert.assertNotNull("userdatabase handler must not be null", dao);

            try {
                BeanConfiguration.getBeans().getAuthenticationManager()
                        .createUserAccount(m, false);
            }catch (KustvaktException e) {
                // do nothing
                return false;
            }
        }
        return r;
    }

    public static boolean drop() {
        boolean r = BeanConfiguration.hasContext();
        if (r) {
            EntityHandlerIface dao = BeanConfiguration.getBeans()
                    .getUserDBHandler();
            try {
                User us = dao.getAccount(credentials[0]);
                dao.deleteAccount(us.getId());
            }catch (KustvaktException e) {
                // do nothing
            }
        }
        return r;
    }

    public static boolean truncateAll() {
        boolean r = BeanConfiguration.hasContext();
        if (r) {
            String sql = "SELECT Concat('TRUNCATE TABLE ', TABLE_NAME) FROM INFORMATION_SCHEMA.TABLES";
            final Set<String> queries = new HashSet<>();
            PersistenceClient cl = BeanConfiguration.getBeans()
                    .getPersistenceClient();
            NamedParameterJdbcTemplate source = (NamedParameterJdbcTemplate) cl
                    .getSource();

            source.query(sql, new RowCallbackHandler() {
                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    queries.add(rs.getString(1));

                }
            });
            System.out.println(queries);
            for (String query : queries)
                source.update(query, new HashMap<String, Object>());
        }
        return r;
    }

    public static final String[] getCredentials() {
        return Arrays.copyOf(credentials, 2);
    }

    private UserTestHelper() {
    }

}