package de.ids_mannheim.korap.web.service.full;

import java.util.List;
import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.container.ResourceFilters;

import de.ids_mannheim.korap.auditing.AuditRecord;
import de.ids_mannheim.korap.config.BeansFactory;
import de.ids_mannheim.korap.exceptions.KustvaktException;
import de.ids_mannheim.korap.exceptions.StatusCodes;
import de.ids_mannheim.korap.handlers.DocumentDao;
import de.ids_mannheim.korap.interfaces.db.AuditingIface;
import de.ids_mannheim.korap.resources.Document;
import de.ids_mannheim.korap.resources.KustvaktResource;
import de.ids_mannheim.korap.resources.Permissions;
import de.ids_mannheim.korap.resources.ResourceFactory;
import de.ids_mannheim.korap.security.PolicyCondition;
import de.ids_mannheim.korap.security.ac.PolicyBuilder;
import de.ids_mannheim.korap.server.KustvaktServer;
import de.ids_mannheim.korap.user.User;
import de.ids_mannheim.korap.utils.JsonUtils;
import de.ids_mannheim.korap.utils.TimeUtils;
import de.ids_mannheim.korap.web.filter.AdminFilter;
import de.ids_mannheim.korap.web.filter.PiwikFilter;
import de.ids_mannheim.korap.web.utils.KustvaktResponseHandler;

/**
 * @author hanl, margaretha 
 * Created date 6/11/14. 
 * Last update: 04/2017
 */
@Path(KustvaktServer.API_VERSION + "/admin")
@ResourceFilters({ AdminFilter.class, PiwikFilter.class })
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class AdminService {

    private static Logger jlog = LoggerFactory.getLogger(AdminService.class);

    private AuditingIface auditingController;
    private DocumentDao documentDao;


    public AdminService () {
        this.auditingController = BeansFactory.getKustvaktContext()
                .getAuditingProvider();
        this.documentDao = new DocumentDao(
                BeansFactory.getKustvaktContext().getPersistenceClient());
    }

    // EM: not documented and tested, not sure what the purpose of the service is
    @GET
    @Path("audit/{type}")
    public Response getAudits (@PathParam("type") String type,
            @QueryParam("from") String from, @QueryParam("until") String until,
            @QueryParam("day") Boolean day, @QueryParam("limit") String limit,
            @Context Locale locale) {
        DateTime from_date, until_date;

        if (from == null)
            from_date = TimeUtils.getNow();
        else
            from_date = TimeUtils.getTime(from);
        if (until == null)
            until_date = TimeUtils.getNow();
        else
            until_date = TimeUtils.getTime(until);

        int integer_limit;
        boolean dayOnly = Boolean.valueOf(day);
        try {
            integer_limit = Integer.valueOf(limit);
        }
        catch (NumberFormatException | NullPointerException e) {
            throw KustvaktResponseHandler.throwit(StatusCodes.ILLEGAL_ARGUMENT);
        }
        String result = JsonUtils.toJSON(auditingController.retrieveRecords(
                AuditRecord.CATEGORY.valueOf(type.toUpperCase()), from_date,
                until_date, dayOnly, integer_limit));
        // limit number of records to return
        return Response.ok(result).build();
    }


    @POST
    @Path("createPolicies/{id}")
    public Response addResourcePolicy (@PathParam("id") String persistentid,
            @QueryParam("type") String type, @QueryParam("name") String name,
            @QueryParam("description") String description,
            @QueryParam("group") String group,
            @QueryParam("perm") List<String> permissions,
            @QueryParam("loc") String loc,
            @QueryParam("expire") String duration, @Context HttpContext context)
            throws KustvaktException {

        if (type == null | type.isEmpty()) {
            KustvaktException e = new KustvaktException(
                    StatusCodes.MISSING_ARGUMENT,
                    "The value of parameter type is missing.");
            throw KustvaktResponseHandler.throwit(e);
        }
        else if (name == null | name.isEmpty()) {
            KustvaktException e = new KustvaktException(
                    StatusCodes.MISSING_ARGUMENT,
                    "The value of parameter name is missing.");
            throw KustvaktResponseHandler.throwit(e);
        }
        else if (description == null | description.isEmpty()) {
            KustvaktException e = new KustvaktException(
                    StatusCodes.MISSING_ARGUMENT,
                    "The value of parameter description is missing.");
            throw KustvaktResponseHandler.throwit(e);
        }
        else if (group == null | group.isEmpty()) {
            KustvaktException e = new KustvaktException(
                    StatusCodes.MISSING_ARGUMENT,
                    "The value of parameter group is missing.");
            throw KustvaktResponseHandler.throwit(e);
        }
        else if (permissions == null | permissions.isEmpty()) {
            KustvaktException e = new KustvaktException(
                    StatusCodes.MISSING_ARGUMENT,
                    "The value of parameter permissions is missing.");
            throw KustvaktResponseHandler.throwit(e);
        }


        try {
            KustvaktResource resource = ResourceFactory.getResource(type);
            resource.setPersistentID(persistentid);
            resource.setDescription(description);
            resource.setName(name);

            Permissions.Permission[] p = Permissions
                    .read(permissions.toArray(new String[0]));

            User user = (User) context.getProperties().get("user");

            PolicyBuilder pb = new PolicyBuilder(user)
                    .setConditions(new PolicyCondition(group))
                    .setResources(resource);

            if (loc != null && !loc.isEmpty()){
                pb.setLocation(loc);
            }
            if (duration != null && !duration.isEmpty()){
                long now = TimeUtils.getNow().getMillis();
                pb.setContext(now,
                        now + TimeUtils.convertTimeToSeconds(duration));
            }
            pb.setPermissions(p);
            pb.create();
        }
        catch (KustvaktException e) {
            throw KustvaktResponseHandler.throwit(e);
        }

        return Response.ok().build();
    }


    @POST
    @Path("doc/{id}/add")
    @Deprecated
    public Response addDocument (@PathParam("id") String id) {
        Document document = new Document(id);
        try {
            this.documentDao.storeResource(document, null);
        }
        catch (KustvaktException e) {
            throw KustvaktResponseHandler.throwit(e);
        }
        return Response.ok().build();
    }

}
