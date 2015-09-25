package de.ids_mannheim.korap.security.ac;

import de.ids_mannheim.korap.config.BeanConfiguration;
import de.ids_mannheim.korap.exceptions.EmptyResultException;
import de.ids_mannheim.korap.exceptions.KustvaktException;
import de.ids_mannheim.korap.exceptions.NotAuthorizedException;
import de.ids_mannheim.korap.interfaces.PolicyHandlerIface;
import de.ids_mannheim.korap.resources.KustvaktResource;
import de.ids_mannheim.korap.resources.Permissions;
import de.ids_mannheim.korap.security.PolicyCondition;
import de.ids_mannheim.korap.user.User;
import de.ids_mannheim.korap.utils.KustvaktLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author hanl
 * @date 04/03/2014
 */
public class ConditionManagement {

    private static final Logger errorLogger = LoggerFactory
            .getLogger(KustvaktLogger.ERROR_LOG);
    private static final Logger secErrorLogger = LoggerFactory
            .getLogger(KustvaktLogger.SECURITY_LOG);
    private User user;
    private PolicyHandlerIface policydao;

    public ConditionManagement(User user) {
        this.user = user;
        this.policydao = BeanConfiguration.getBeans().getPolicyDbProvider();

    }

    /**
     * adds a user to an existing group
     *
     * @param usernames
     * @param condition
     * @param admin
     */
    // todo: conflict resolution
    // fixme: not applicable to korap system roles
    // only works if there is a policy with that condition and permission set, if not, create one!
    public void addUser(List<String> usernames, PolicyCondition condition,
            boolean admin) throws NotAuthorizedException, KustvaktException {
        if (policydao.matchCondition(this.user, condition.getSpecifier(), true)
                == 1) {
            policydao.addToCondition(usernames, condition, admin);
        }else
            secErrorLogger.error("Users could not be added to condition '{}'",
                    condition.getSpecifier());
    }

    public void addUser(String username, PolicyCondition condition,
            boolean admin) throws NotAuthorizedException, KustvaktException {
        addUser(Arrays.asList(username), condition, admin);
    }

    public void removeUser(List<String> users, PolicyCondition condition)
            throws KustvaktException {
        if (policydao.matchCondition(this.user, condition.getSpecifier(), true)
                == 1) {
            policydao.removeFromCondition(users, condition);
        }
    }

    public Set<String> getMembers(PolicyCondition condition) {
        try {
            if (policydao
                    .matchCondition(this.user, condition.getSpecifier(), true)
                    == 1) {
                return new HashSet<>(
                        policydao.getUsersFromCondition(condition));
            }
        }catch (KustvaktException e) {
            return Collections.emptySet();
        }
        return Collections.emptySet();
    }

    @Deprecated
    public void addUser(KustvaktResource resource, String user,
            Permissions.PERMISSIONS... pps)
            throws NotAuthorizedException, KustvaktException,
            EmptyResultException {
        addUser(resource, Arrays.asList(user), pps);
    }

    @Deprecated
    public void addUser(KustvaktResource resource, List<String> users,
            Permissions.PERMISSIONS... pps)
            throws NotAuthorizedException, KustvaktException,
            EmptyResultException {
        SecurityManager policies = SecurityManager
                .findbyId(resource.getId(), this.user);
        PolicyCondition c = policies.getExtensional(pps);
        if (c != null)
            this.addUser(users, c, false);
        else {
            PolicyCondition ex = new PolicyCondition();
            new PolicyBuilder(this.user).setResources(resource)
                    .addCondition(ex.getSpecifier()).setPermissions(pps)
                    .create();
            this.addUser(users, ex, false);
        }
    }

}