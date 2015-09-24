package de.ids_mannheim.korap.security.ac;

import de.ids_mannheim.korap.exceptions.KustvaktException;
import de.ids_mannheim.korap.interfaces.PolicyHandlerIface;
import de.ids_mannheim.korap.interfaces.ResourceOperationIface;
import de.ids_mannheim.korap.resources.KustvaktResource;
import de.ids_mannheim.korap.resources.Permissions;
import de.ids_mannheim.korap.resources.ResourceFactory;
import de.ids_mannheim.korap.security.PermissionsBuffer;
import de.ids_mannheim.korap.user.User;
import de.ids_mannheim.korap.utils.KustvaktLogger;
import org.slf4j.Logger;

import java.util.*;

/**
 * Created by hanl on 3/20/14.
 */
public class ResourceFinder {

    private static final Logger log = KustvaktLogger.initiate(ResourceFinder.class);
    private static PolicyHandlerIface policydao;

    private List<KustvaktResource.Container> containers;
    private User user;

    private ResourceFinder(User user) {
        this.containers = new ArrayList<>();
        this.user = user;
    }

    public static final void setProviders(PolicyHandlerIface policyHandler, ResourceHandler handler) {
        ResourceFinder.policydao = policyHandler;
//        ResourceFinder.handler = handler;
    }

    public static <T extends KustvaktResource> Set<T> search(String path, boolean asParent,
                                                          User user, Class<T> clazz, Permissions.PERMISSIONS... perms)
            throws KustvaktException {
        ResourceFinder cat = init(path, asParent, user, clazz, perms);
        return cat.getResources();
    }

    private static <T extends KustvaktResource> ResourceFinder init(String path, boolean asParent,
                                                                User user, Class<T> clazz, Permissions.PERMISSIONS... perms) throws
            KustvaktException {
        ResourceFinder cat = new ResourceFinder(user);
        PermissionsBuffer buffer = new PermissionsBuffer();
        if (perms.length == 0)
            buffer.addPermissions(Permissions.PERMISSIONS.READ);
        buffer.addPermissions(perms);
        cat.retrievePolicies(path, buffer.getPbyte(), clazz, asParent);
        return cat;
    }

    //todo: needs to be much faster!
    public static <T extends KustvaktResource> ResourceFinder init(User user, Class<T> clazz)
            throws KustvaktException {
        return init(null, true, user, clazz, Permissions.PERMISSIONS.READ);
    }

    public static <T extends KustvaktResource> Set<T> search(String name, boolean asParent, User user, String type)
            throws KustvaktException {
        return (Set<T>) search(name, asParent, user, ResourceFactory
                .getResourceClass(type), Permissions.PERMISSIONS.READ);
    }

    // todo: should this be working?
    public static <T extends KustvaktResource> Set<T> search(User user, Class<T> clazz)
            throws KustvaktException {
        return search(null, true, user, clazz, Permissions.PERMISSIONS.READ);
    }

    private void retrievePolicies(String path, Byte b, Class type, boolean parent) throws
            KustvaktException {
        if (user == null | type == null)
            return;
        if (parent)
            this.containers = policydao.getDescending(path, user, b, type);
        else
            this.containers = policydao.getAscending(path, user, b, type);
    }


    public <T extends KustvaktResource> Set<T> getResources() {
        return evaluateResources();
    }

    private <T extends KustvaktResource> Set<T> evaluateResources() {
        Set<T> resources = new HashSet<>();
        if (this.containers != null) {
            for (KustvaktResource.Container c : this.containers) {
                ResourceOperationIface<T> iface = SecurityManager.getHandlers().get(c.getType());
                if (iface == null)
                    iface = SecurityManager.getHandlers().get(KustvaktResource.class);

                try {
                    T resource = (T) iface.findbyId(c.getPersistentID(), this.user);
                    PolicyEvaluator e = PolicyEvaluator.setFlags(user, resource);
                    resource.setManaged(e.getFlag("managed", false));
                    resources.add(resource);
                } catch (KustvaktException e) {
                    // don't handle connection error or no handler registered!
                    KustvaktLogger.ERROR_LOGGER.error("Error while retrieving containers '{}' ", this.containers);
                    return Collections.emptySet();
                }
            }
        }
        return resources;
    }

    public Set<String> getIds() {
        Set<String> resources = new HashSet<>();
        for (KustvaktResource.Container c : this.containers)
            resources.add(c.getPersistentID());
        return resources;
    }

}
