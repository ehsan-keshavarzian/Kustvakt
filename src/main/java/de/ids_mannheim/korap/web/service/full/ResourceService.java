package de.ids_mannheim.korap.web.service.full;//package de.ids_mannheim.korap.ext.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.spi.container.ResourceFilters;
import de.ids_mannheim.korap.config.BeanConfiguration;
import de.ids_mannheim.korap.config.KustvaktConfiguration;
import de.ids_mannheim.korap.exceptions.EmptyResultException;
import de.ids_mannheim.korap.exceptions.KustvaktException;
import de.ids_mannheim.korap.exceptions.NotAuthorizedException;
import de.ids_mannheim.korap.exceptions.StatusCodes;
import de.ids_mannheim.korap.interfaces.AuthenticationManagerIface;
import de.ids_mannheim.korap.query.serialize.MetaQueryBuilder;
import de.ids_mannheim.korap.query.serialize.QuerySerializer;
import de.ids_mannheim.korap.resource.rewrite.FoundryInject;
import de.ids_mannheim.korap.resource.rewrite.RewriteHandler;
import de.ids_mannheim.korap.resources.*;
import de.ids_mannheim.korap.security.ac.ResourceFinder;
import de.ids_mannheim.korap.security.ac.ResourceHandler;
import de.ids_mannheim.korap.security.ac.SecurityManager;
import de.ids_mannheim.korap.user.TokenContext;
import de.ids_mannheim.korap.user.User;
import de.ids_mannheim.korap.utils.CollectionQueryBuilder3;
import de.ids_mannheim.korap.utils.JsonUtils;
import de.ids_mannheim.korap.utils.KustvaktLogger;
import de.ids_mannheim.korap.utils.StringUtils;
import de.ids_mannheim.korap.web.ClientsHandler;
import de.ids_mannheim.korap.web.KustvaktServer;
import de.ids_mannheim.korap.web.SearchKrill;
import de.ids_mannheim.korap.web.TRACE;
import de.ids_mannheim.korap.web.filter.AuthFilter;
import de.ids_mannheim.korap.web.filter.DefaultFilter;
import de.ids_mannheim.korap.web.filter.PiwikFilter;
import de.ids_mannheim.korap.web.utils.KustvaktResponseHandler;
import org.slf4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

/**
 * @author hanl
 * @date 29/01/2014
 */
@Path(KustvaktServer.API_VERSION + "/")
@ResourceFilters({ AuthFilter.class, DefaultFilter.class, PiwikFilter.class })
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ResourceService {

    private static Logger jlog = KustvaktLogger
            .getLogger(ResourceService.class);

    private SearchKrill searchKrill;
    private ResourceHandler resourceHandler;
    private AuthenticationManagerIface controller;
    private ClientsHandler graphDBhandler;
    private KustvaktConfiguration config;
    private RewriteHandler processor;

    public ResourceService() {
        this.controller = BeanConfiguration.getBeans()
                .getAuthenticationManager();
        this.config = BeanConfiguration.getBeans().getConfiguration();
        this.resourceHandler = new ResourceHandler();
        this.searchKrill = new SearchKrill(config.getIndexDir());

        UriBuilder builder = UriBuilder.fromUri("http://10.0.10.13").port(9997);
        this.graphDBhandler = new ClientsHandler(builder.build());

        this.processor = new RewriteHandler(config);
        this.processor.add(FoundryInject.class);
    }

    /**
     * retrieve resources dependent by type. determines based on
     * the user's permission or resource owner if the user can access the resource.
     *
     * @param locale
     * @param context
     * @param type
     * @return valid resources in json format
     */
    @GET
    @Path("{type}")
    public Response getResources(@Context Locale locale,
            @Context SecurityContext context, @PathParam("type") String type) {
        TokenContext ctx = (TokenContext) context.getUserPrincipal();
        Set<KustvaktResource> resources = new HashSet<>();
        type = StringUtils.normalize(type);
        Class cl_type = ResourceFactory.getResourceClass(type);
        if (cl_type == null) {
            //todo return bad request response
        }

        try {
            User user = controller.getUser(ctx.getUsername());

            resources = ResourceFinder
                    .search(user, ResourceFactory.getResourceClass(type));
        }catch (KustvaktException e) {
            throw KustvaktResponseHandler.throwit(e);
        }

        Set values = new HashSet();
        for (KustvaktResource resource : resources) {
            // fixme: remove -- to costly
            //            if (cl_type.equals(VirtualCollection.class)) {
            //                VirtualCollection c = (VirtualCollection) resource;
            //                CollectionQueryBuilder3 query = new CollectionQueryBuilder3();
            //                query.setBaseQuery(c.getQuery());
            //                String stats = searchKrill.getStatistics(query.toJSON());
            //
            //                c.setStats(JsonUtils.readSimple(stats, Map.class));
            //
            //            }else if (cl_type.equals(Corpus.class)) {
            //                Corpus c = (Corpus) resource;
            //                CollectionQueryBuilder3 query = new CollectionQueryBuilder3();
            //                query.addQuery("corpusID=" + c.getPersistentID());
            //                String stats = searchKrill.getStatistics(query.toJSON());
            //                c.setStats(JsonUtils.readSimple(stats, Map.class));
            //            }
            values.add(resource.toMap());
        }
        return Response.ok(JsonUtils.toJSON(values)).build();
    }

    @GET
    @Path("{type}/{id}/{child}")
    public Response getResource(@Context SecurityContext context,
            @Context Locale locale, @PathParam("type") String type,
            @PathParam("id") String id, @PathParam("child") String child) {
        return getResource(context, locale, type,
                StringUtils.joinResources(id, child));
    }

    /**
     * @param context
     * @param locale
     * @param id
     * @param type
     * @return
     */
    @GET
    @Path("{type}/{id}")
    public Response getResource(@Context SecurityContext context,
            @Context Locale locale, @PathParam("type") String type,
            @PathParam("id") String id) {
        TokenContext ctx = (TokenContext) context.getUserPrincipal();
        type = StringUtils.normalize(type);
        Class cl_type = ResourceFactory.getResourceClass(type);
        KustvaktResource resource;
        try {
            User user = controller.getUser(ctx.getUsername());
            if (StringUtils.isInteger(id))
                resource = resourceHandler
                        .findbyIntId(Integer.valueOf(id), user);
            else
                resource = resourceHandler.findbyStrId(id, user,
                        ResourceFactory.getResourceClass(type));
        }catch (KustvaktException e) {
            throw KustvaktResponseHandler.throwit(e);
        }
        CollectionQueryBuilder3 query = new CollectionQueryBuilder3();
        if (cl_type.equals(VirtualCollection.class)) {
            VirtualCollection c = (VirtualCollection) resource;
            query.setBaseQuery(c.getQuery());
        }else if (cl_type.equals(Corpus.class)) {
            Corpus c = (Corpus) resource;
            query.addQuery("corpusID=" + c.getPersistentID());
        }

        return Response.ok(JsonUtils.toJSON(resource.toMap())).build();
    }

    //    @GET
    //    @Path("colloc")
    //    public Response getCollocationsAll(@Context SecurityContext ctx,
    //            @Context Locale locale, @QueryParam("props") String properties,
    //            @QueryParam("sfskip") Integer sfs,
    //            @QueryParam("sflimit") Integer limit, @QueryParam("q") String query,
    //            @QueryParam("ql") String ql, @QueryParam("context") Integer context,
    //            @QueryParam("foundry") String foundry,
    //            @QueryParam("paths") Boolean wPaths) {
    //        TokenContext tokenContext = (TokenContext) ctx.getUserPrincipal();
    //        ColloQuery.ColloQueryBuilder builder;
    //        CollectionQueryBuilder3 cquery = new CollectionQueryBuilder3();
    //        String result;
    //        try {
    //            User user = controller.getUser(tokenContext.getUsername());
    //            Set<VirtualCollection> resources = ResourceFinder
    //                    .search(user, VirtualCollection.class);
    //            for (KustvaktResource c : resources)
    //                cquery.addResource(((VirtualCollection) c).getQuery());
    //
    //            builder = functions
    //                    .buildCollocations(query, ql, properties, context, limit,
    //                            sfs, foundry, new ArrayList<Dependency>(), wPaths,
    //                            cquery);
    //
    //            result = graphDBhandler
    //                    .getResponse("distCollo", "q", builder.build().toJSON());
    //        }catch (KustvaktException e) {
    //            throw KustvaktResponseHandler.throwit(e);
    //        }catch (JsonProcessingException e) {
    //            throw KustvaktResponseHandler.throwit(StatusCodes.ILLEGAL_ARGUMENT);
    //        }
    //        return Response.ok(result).build();
    //    }

    //    /**
    //     * @param locale
    //     * @param properties a json object string containing field, op and value for the query
    //     * @param query
    //     * @param context
    //     * @return
    //     */
    //    @GET
    //    @Path("{type}/{id}/colloc")
    //    public Response getCollocations(@Context SecurityContext ctx,
    //            @Context Locale locale, @QueryParam("props") String properties,
    //            @QueryParam("sfskip") Integer sfs,
    //            @QueryParam("sflimit") Integer limit, @QueryParam("q") String query,
    //            @QueryParam("ql") String ql, @QueryParam("context") Integer context,
    //            @QueryParam("foundry") String foundry,
    //            @QueryParam("paths") Boolean wPaths, @PathParam("id") String id,
    //            @PathParam("type") String type) {
    //        ColloQuery.ColloQueryBuilder builder;
    //        type = StringUtils.normalize(type);
    //        id = StringUtils.decodeHTML(id);
    //        TokenContext tokenContext = (TokenContext) ctx.getUserPrincipal();
    //        String result;
    //        try {
    //            CollectionQueryBuilder3 cquery = new CollectionQueryBuilder3();
    //            try {
    //                User user = controller.getUser(tokenContext.getUsername());
    //
    //                KustvaktResource resource = this.resourceHandler
    //                        .findbyStrId(id, user, type);
    //
    //                if (resource instanceof VirtualCollection)
    //                    cquery.addResource(
    //                            ((VirtualCollection) resource).getQuery());
    //                else if (resource instanceof Corpus)
    //                    cquery.addMetaFilter("corpusID",
    //                            resource.getPersistentID());
    //                else
    //                    throw KustvaktResponseHandler
    //                            .throwit(StatusCodes.ILLEGAL_ARGUMENT,
    //                                    "Type parameter not supported", type);
    //
    //            }catch (KustvaktException e) {
    //                throw KustvaktResponseHandler.throwit(e);
    //            }catch (NumberFormatException ex) {
    //                throw KustvaktResponseHandler
    //                        .throwit(StatusCodes.ILLEGAL_ARGUMENT);
    //            }
    //
    //            builder = functions
    //                    .buildCollocations(query, ql, properties, context, limit,
    //                            sfs, foundry, new ArrayList<Dependency>(), wPaths,
    //                            cquery);
    //
    //            result = graphDBhandler
    //                    .getResponse("distCollo", "q", builder.build().toJSON());
    //
    //        }catch (JsonProcessingException e) {
    //            throw KustvaktResponseHandler.throwit(StatusCodes.ILLEGAL_ARGUMENT);
    //        }catch (KustvaktException e) {
    //            throw KustvaktResponseHandler.throwit(e);
    //        }
    //
    //        return Response.ok(result).build();
    //    }
    @POST
    @Path("colloc")
    public Response getCollocatioBase(@QueryParam("q") String query) {
        String result;
        try {
            result = graphDBhandler.getResponse("distCollo", "q", query);
        }catch (KustvaktException e) {
            throw KustvaktResponseHandler.throwit(e);
        }
        return Response.ok(result).build();
    }

    @Deprecated
    public Response postMatchFavorite() {
        return Response.ok().build();
    }

    @TRACE
    @Path("search")
    public Response buildQuery(@Context Locale locale,
            @Context SecurityContext securityContext, @QueryParam("q") String q,
            @QueryParam("ql") String ql, @QueryParam("v") String v,
            @QueryParam("context") String context,
            @QueryParam("cutoff") Boolean cutoff,
            @QueryParam("count") Integer pageLength,
            @QueryParam("offset") Integer pageIndex,
            @QueryParam("page") Integer startPage,
            @QueryParam("ref") String reference, @QueryParam("cq") String cq) {
        TokenContext ctx = (TokenContext) securityContext.getUserPrincipal();
        QuerySerializer ss;
        CollectionQueryBuilder3 cquery = new CollectionQueryBuilder3();
        User user;
        try {
            user = controller.getUser(ctx.getUsername());
            Set<Corpus> resources = ResourceFinder.search(user, Corpus.class);
            for (KustvaktResource corpus : resources)
                cquery.addQuery("corpusID=" + corpus.getPersistentID());
        }catch (KustvaktException e) {
            throw KustvaktResponseHandler.throwit(e);
        }

        ss = new QuerySerializer().setQuery(q, ql, v);
        if (cq != null)
            ss.setCollection(cq);

        MetaQueryBuilder meta = new MetaQueryBuilder();
        if (pageIndex != null)
            meta.addEntry("startIndex", pageIndex);
        if (pageIndex == null && startPage != null)
            meta.addEntry("startPage", startPage);
        if (pageLength != null)
            meta.addEntry("count", pageLength);
        if (context != null)
            meta.setSpanContext(context);
        meta.addEntry("cutOff", cutoff);

        ss.setMeta(meta);

        String query = this.processor.preProcess(ss.toJSON(), user);
        return Response.ok(query).build();
    }

    /**
     * currently only supports either no reference at all in which case all corpora are retrieved or a corpus name like "WPD".
     * No virtual collections supported!
     *
     * @param locale
     * @param q
     * @param ql
     * @param v
     * @param pageLength
     * @param pageIndex
     * @return
     */
    // todo: test
    @TRACE
    @Path("{type}/{id}/search")
    public Response buildQuery(@Context Locale locale,
            @Context SecurityContext securityContext, @QueryParam("q") String q,
            @QueryParam("ql") String ql, @QueryParam("v") String v,
            @QueryParam("context") String context,
            @QueryParam("cutoff") Boolean cutoff,
            @QueryParam("count") Integer pageLength,
            @QueryParam("offset") Integer pageIndex,
            @QueryParam("page") Integer startPage,
            @PathParam("type") String type, @PathParam("id") String id,
            @QueryParam("cq") String cq) {
        TokenContext ctx = (TokenContext) securityContext.getUserPrincipal();
        type = StringUtils.normalize(type);
        id = StringUtils.decodeHTML(id);
        QuerySerializer ss;
        //fixme: not used anywhere!
        CollectionQueryBuilder3 cquery = new CollectionQueryBuilder3();
        if (cq != null)
            cquery.setBaseQuery(cq);

        try {
            User user = controller.getUser(ctx.getUsername());

            KustvaktResource resource;
            if (StringUtils.isInteger(id))
                resource = this.resourceHandler
                        .findbyIntId(Integer.valueOf(id), user);
            else
                resource = this.resourceHandler.findbyStrId(id, user,
                        ResourceFactory.getResourceClass(type));

            if (resource instanceof VirtualCollection)
                cquery.addQuery(((VirtualCollection) resource).getQuery());
            else if (resource instanceof Corpus)
                cquery.addQuery("corpusID=" + resource.getPersistentID());

        }catch (KustvaktException e) {
            KustvaktLogger.ERROR_LOGGER.error("Exception encountered!", e);
            throw KustvaktResponseHandler.throwit(e);
        }

        ss = new QuerySerializer().setQuery(q, ql, v);

        // todo: parse resources
        if (cq != null)
            ss.setCollection(cq);

        MetaQueryBuilder meta = new MetaQueryBuilder();
        if (pageIndex != null)
            meta.addEntry("startIndex", pageIndex);
        if (pageIndex == null && startPage != null)
            meta.addEntry("startPage", startPage);
        if (pageLength != null)
            meta.addEntry("count", pageLength);
        if (context != null)
            meta.setSpanContext(context);
        if (cutoff != null)
            meta.addEntry("cutOff", cutoff);

        ss.setMeta(meta.raw());

        // todo: policy parsing before return
        return Response.ok(ss.toJSON()).build();
    }

    @POST
    @Path("search")
    public Response queryRaw(@Context SecurityContext context,
            @Context Locale locale, @QueryParam("engine") String engine,
            String jsonld) {
        TokenContext ctx = (TokenContext) context.getUserPrincipal();
        //        KustvaktConfiguration.BACKENDS eng = KustvaktConfiguration
        //                .chooseBackend(engine);

        // todo: should be possible to add the meta part to the query serialization
        try {
            User user = controller.getUser(ctx.getUsername());
            jsonld = this.processor.preProcess(jsonld, user);
        }catch (KustvaktException e) {
            throw KustvaktResponseHandler.throwit(e);
        }
        jlog.info("Serialized search: {}", jsonld);

        String result = searchKrill.search(jsonld);
        KustvaktLogger.QUERY_LOGGER.trace("The result set: {}", result);
        return Response.ok(result).build();
    }

    @GET
    @Path("search")
    public Response searchbyNameAll(@Context SecurityContext securityContext,
            @Context Locale locale, @QueryParam("q") String q,
            @QueryParam("ql") String ql, @QueryParam("v") String v,
            @QueryParam("context") String ctx,
            @QueryParam("cutoff") Boolean cutoff,
            @QueryParam("count") Integer pageLength,
            @QueryParam("offset") Integer pageIndex,
            @QueryParam("page") Integer pageInteger,
            @QueryParam("cq") String cq, @QueryParam("engine") String engine) {
        TokenContext context = (TokenContext) securityContext
                .getUserPrincipal();
        CollectionQueryBuilder3 cquery = new CollectionQueryBuilder3();
        KustvaktConfiguration.BACKENDS eng = this.config.chooseBackend(engine);
        User user;
        // todo: not added to query!!
        try {
            user = controller.getUser(context.getUsername());
            Set<Corpus> resources = ResourceFinder.search(user, Corpus.class);
            for (KustvaktResource resource : resources)
                cquery.addQuery("corpusID=" + resource.getPersistentID());
        }catch (KustvaktException e) {
            KustvaktLogger.ERROR_LOGGER.error("Exception encountered!", e);
            throw KustvaktResponseHandler.throwit(e);
        }
        String result;
        QuerySerializer serializer = new QuerySerializer();
        serializer.setQuery(q, ql, v);

        // todo: parse for security reasons
        if (cq != null)
            serializer.setCollection(cq);

        MetaQueryBuilder meta = new MetaQueryBuilder();
        meta.addEntry("startIndex", pageIndex);
        meta.addEntry("startPage", pageInteger);
        meta.setSpanContext(ctx);
        meta.addEntry("count", pageLength);
        // todo: what happened to cutoff?
        meta.addEntry("cutoff", cutoff);
        //        meta.addMeta(pageIndex, pageInteger, pageLength, ctx, cutoff);
        // fixme: should only apply to CQL queries per default!
        //        meta.addEntry("itemsPerResource", 1);
        serializer.setMeta(meta.raw());

        // policy rewrite!
        String query = this.processor.preProcess(serializer.toJSON(), user);

        jlog.info("the serialized query {}", query);

        if (eng.equals(KustvaktConfiguration.BACKENDS.NEO4J)) {
            MultivaluedMap map = new MultivaluedMapImpl();
            map.add("q", query);
            map.add("count", String.valueOf(pageLength));
            map.add("lctxs",
                    String.valueOf(meta.getSpanContext().getLeft_size()));
            map.add("rctxs",
                    String.valueOf(meta.getSpanContext().getRight_size()));
            try {
                result = this.graphDBhandler.getResponse(map, "distKwic");
            }catch (KustvaktException e) {
                throw KustvaktResponseHandler.throwit(e);
            }
        }else
            result = searchKrill.search(query);
        KustvaktLogger.QUERY_LOGGER.trace("The result set: {}", result);
        return Response.ok(result).build();
    }

    /**
     * String search, String ql, List<String> parents,
     * String cli, String cri, int cls, int crs,
     * int num, int page, boolean cutoff)
     * param context will be like this: context: "3-t,2-c"
     * <p/>
     * id does not have to be an integer. name is also possible, in which case a type reference is required
     *
     * @param securityContext
     * @param locale
     * @return
     */
    //fixme: does not use policyrewrite!
    @GET
    @Path("/{type}/{id}/search")
    public Response searchbyName(@Context SecurityContext securityContext,
            @Context Locale locale, @QueryParam("q") String query,
            @QueryParam("ql") String ql, @QueryParam("v") String v,
            @QueryParam("context") String ctx,
            @QueryParam("cutoff") Boolean cutoff,
            @QueryParam("count") Integer pageLength,
            @QueryParam("offset") Integer pageIndex,
            @QueryParam("page") Integer pageInteger, @PathParam("id") String id,
            @PathParam("type") String type, @QueryParam("cq") String cq,
            @QueryParam("raw") Boolean raw,
            @QueryParam("engine") String engine) {
        // ref is a virtual collection id!
        TokenContext context = (TokenContext) securityContext
                .getUserPrincipal();
        KustvaktConfiguration.BACKENDS eng = this.config.chooseBackend(engine);
        type = StringUtils.normalize(type);
        id = StringUtils.decodeHTML(id);
        raw = raw == null ? false : raw;

        try {
            User user = controller.getUser(context.getUsername());
            MetaQueryBuilder meta = new MetaQueryBuilder();

            if (!raw) {
                QuerySerializer s = new QuerySerializer();
                s.setQuery(query, ql, v);
                CollectionQueryBuilder3 builder = new CollectionQueryBuilder3();

                KustvaktResource resource;
                if (StringUtils.isInteger(id))
                    resource = this.resourceHandler
                            .findbyIntId(Integer.valueOf(id), user);
                else
                    resource = this.resourceHandler.findbyStrId(id, user,
                            ResourceFactory.getResourceClass(type));

                if (resource instanceof VirtualCollection)
                    builder.addQuery(((VirtualCollection) resource).getQuery());
                else if (resource instanceof Corpus)
                    builder.addQuery("corpusID=" + resource.getPersistentID());
                else
                    throw KustvaktResponseHandler
                            .throwit(StatusCodes.ILLEGAL_ARGUMENT,
                                    "Type parameter not supported", type);

                //                functions.createQuery(id, type, user, query, ql, v);

                meta.addEntry("startIndex", pageIndex);
                meta.addEntry("startPage", pageInteger);
                meta.setSpanContext(ctx);
                meta.addEntry("count", pageLength);
                // todo: what happened to cutoff?
                meta.addEntry("cutoff", cutoff);
                // should only apply to CQL queries
                //                meta.addEntry("itemsPerResource", 1);
                s.setMeta(meta);

                query = s.toJSON();
                //                PolicyParser parser = new PolicyParser(user);
                //                query = parser.parse(s.toJSON());
            }
            String result;
            try {

                // rewrite process
                query = this.processor.preProcess(query, user);

                if (eng.equals(KustvaktConfiguration.BACKENDS.NEO4J)) {
                    if (raw)
                        throw KustvaktResponseHandler
                                .throwit(StatusCodes.ILLEGAL_ARGUMENT,
                                        "raw not supported!", null);
                    MultivaluedMap map = new MultivaluedMapImpl();
                    map.add("q", query);
                    map.add("count", String.valueOf(pageLength));
                    map.add("lctxs", String.valueOf(
                            meta.getSpanContext().getLeft_size()));
                    map.add("rctxs", String.valueOf(
                            meta.getSpanContext().getRight_size()));
                    result = this.graphDBhandler.getResponse(map, "distKwic");
                }else
                    result = searchKrill.search(query);

            }catch (Exception e) {
                KustvaktLogger.ERROR_LOGGER
                        .error("Exception for serialized query: " + query, e);
                throw KustvaktResponseHandler
                        .throwit(500, e.getMessage(), null);
            }

            KustvaktLogger.QUERY_LOGGER.trace("The result set: {}", result);
            return Response.ok(result).build();
        }catch (KustvaktException e) {
            KustvaktLogger.ERROR_LOGGER.error("Exception encountered!", e);
            throw KustvaktResponseHandler.throwit(e);
        }

    }

    @POST
    @Path("stats")
    public Response getStats(@Context SecurityContext context,
            @Context Locale locale, String json) {
        CollectionQueryBuilder3 builder = new CollectionQueryBuilder3();
        builder.addQuery(json);
        String stats = searchKrill.getStatistics(builder.toJSON());

        if (stats.contains("-1"))
            throw KustvaktResponseHandler.throwit(StatusCodes.EMPTY_RESULTS);

        return Response.ok(stats).build();
    }

    @GET
    @Path("{type}/{id}/{child}/stats")
    public Response getStatisticsbyName(@Context SecurityContext context,
            @Context Locale locale, @PathParam("type") String type,
            @PathParam("id") String id, @PathParam("child") String child) {
        return getStatisticsbyId(context, locale, type,
                StringUtils.joinResources(id, child));
    }

    @GET
    @Path("{type}/{id}/stats")
    public Response getStatisticsbyId(@Context SecurityContext context,
            @Context Locale locale, @PathParam("type") String type,
            @PathParam("id") String id) {
        TokenContext ctx = (TokenContext) context.getUserPrincipal();
        type = StringUtils.normalize(type);
        id = StringUtils.decodeHTML(id);

        Class sl = ResourceFactory.getResourceClass(type);
        if (!sl.equals(VirtualCollection.class) & !sl.equals(Corpus.class))
            throw KustvaktResponseHandler.throwit(StatusCodes.ILLEGAL_ARGUMENT,
                    "Requested Resource type not supported", type);

        try {
            User user = controller.getUser(ctx.getUsername());
            KustvaktResource resource;
            if (StringUtils.isInteger(id))
                resource = this.resourceHandler
                        .findbyIntId(Integer.valueOf(id), user);
            else
                resource = this.resourceHandler.findbyStrId(id, user,
                        ResourceFactory.getResourceClass(type));

            //todo ?!
            CollectionQueryBuilder3 query = new CollectionQueryBuilder3();
            if (resource instanceof VirtualCollection) {
                query.setBaseQuery(((VirtualCollection) resource).getQuery());
            }else if (resource instanceof Corpus) {
                query.addQuery("corpusID=" + resource.getName());
            }

            // rewrite process
            String qstr = this.processor.preProcess(query.toJSON(), user);
            return Response.ok(searchKrill.getStatistics(qstr)).build();
        }catch (KustvaktException e) {
            e.printStackTrace();
            throw KustvaktResponseHandler.throwit(e);
        }
    }

    /**
     * @param context
     * @param locale
     * @param json
     * @return
     */
    //todo: rename
    @POST
    @Path("collection_raw")
    public Response createRawCollection(@Context SecurityContext context,
            @Context Locale locale, String json) {
        TokenContext c = (TokenContext) context.getUserPrincipal();
        VirtualCollection cache = ResourceFactory.getCachedCollection(json);
        User user;
        try {
            user = controller.getUser(c.getUsername());
        }catch (KustvaktException e) {
            throw KustvaktResponseHandler.throwit(e);
        }

        VirtualCollection tmp = resourceHandler
                .getCache(cache.getId(), VirtualCollection.class);
        if (tmp == null) {
            CollectionQueryBuilder3 cquery = new CollectionQueryBuilder3()
                    .setBaseQuery(cache.getQuery());
            String query = this.processor.preProcess(cache.getQuery(), user);
            String stats = searchKrill.getStatistics(query);
            cache.setStats(JsonUtils.readSimple(stats, Map.class));
            resourceHandler.cache(cache);
        }else
            cache = tmp;

        Map vals = new HashMap();
        vals.put("id", cache.getId());
        vals.put("statistics", cache.getStats());
        return Response.ok(JsonUtils.toJSON(vals)).build();
    }

    @POST
    @Path("{type}/{id}")
    public Response updateResource(@Context SecurityContext context,
            @Context Locale locale, @PathParam("type") String type,
            @PathParam("id") String id, String json) {
        TokenContext ctx = (TokenContext) context.getUserPrincipal();
        User user;
        try {
            user = controller.getUser(ctx.getUsername());
            KustvaktResource resource = this.resourceHandler
                    .findbyStrId(id, user,
                            ResourceFactory.getResourceClass(type));
            JsonNode node = JsonUtils.readTree(json);

            if (node.isObject() && !node.path("name").isMissingNode()) {
                String s = node.path("name").asText();
                if (s.equals("null") || s.isEmpty())
                    throw KustvaktResponseHandler
                            .throwit(StatusCodes.ILLEGAL_ARGUMENT,
                                    "Name must be set", "name");
                resource.setName(s);
            }

            if (node.isObject() && !node.path("description").isMissingNode())
                resource.setDescription(node.path("description").asText());
            this.resourceHandler.updateResources(user, resource);
        }catch (KustvaktException e) {
            KustvaktLogger.ERROR_LOGGER.error("Exception encountered!", e);
            throw KustvaktResponseHandler.throwit(e);
        }
        return Response.ok().build();
    }

    //todo: change or deprecate
    @POST
    @Path("nv/{type}")
    public Response storeResource(@Context SecurityContext context,
            @Context Locale locale, @PathParam("type") String type,
            @QueryParam("name") String name,
            @QueryParam("description") String description,
            // deprecate -> if you want to store a resource based on another, build the query first yourself or via a function
            @QueryParam("ref") String reference,
            @QueryParam("cache") Boolean cache,
            @QueryParam("query") String query) {
        TokenContext ctx = (TokenContext) context.getUserPrincipal();
        cache = cache != null ? cache : false;
        type = StringUtils.normalize(type);
        reference = StringUtils.decodeHTML(reference);
        Map vals = new HashMap();
        User user;
        try {
            user = controller.getUser(ctx.getUsername());
        }catch (KustvaktException e) {
            KustvaktLogger.ERROR_LOGGER.error("Exception encountered!", e);
            throw KustvaktResponseHandler.throwit(e);
        }
        if (VirtualCollection.class
                .equals(ResourceFactory.getResourceClass(type))) {
            VirtualCollection cachetmp, collection;

            String base;
            if (reference != null && !reference.equals("null")) {
                try {
                    base = resourceHandler.findbyStrId(reference, user,
                            VirtualCollection.class).getQuery();
                }catch (KustvaktException e) {
                    throw KustvaktResponseHandler.throwit(e);
                }

            }else if (query != null)
                base = query;
            else
                // todo: throw exception response for no resource to save!
                return null;

            CollectionQueryBuilder3 cquery = new CollectionQueryBuilder3();
            cquery.setBaseQuery(base);

            cachetmp = ResourceFactory.getCachedCollection(cquery.toJSON());

            // see if collection was cached!
            VirtualCollection tmp = resourceHandler
                    .getCache(cachetmp.getId(), VirtualCollection.class);
            // if not cached, fill with stats values
            if (tmp == null) {
                String stats = searchKrill.getStatistics(cquery.toJSON());
                cachetmp.setStats(JsonUtils.readSimple(stats, Map.class));
            }

            if (!cache) {
                collection = ResourceFactory
                        .getPermanentCollection(cachetmp, name, description,
                                user.getId());
                vals = collection.toMap();
                try {
                    resourceHandler.storeResources(user, collection);
                }catch (KustvaktException e) {
                    KustvaktLogger.ERROR_LOGGER
                            .error("Exception encountered!", e);
                    throw KustvaktResponseHandler.throwit(e);
                }
            }else {
                resourceHandler.cache(cachetmp);
                vals = cachetmp.toMap();
            }
        }
        return Response.ok(JsonUtils.toJSON(vals)).build();
    }

    /**
     * store a virtual collection. Retrieve cached entry first and then store VCollection
     *
     * @param context
     * @param locale
     * @param query
     * @return
     */
    @POST
    @Path("{type}")
    public Response storeResource(@Context SecurityContext context,
            @Context Locale locale, @PathParam("type") String type,
            @QueryParam("filter") Boolean filter,
            @QueryParam("name") String name,
            @QueryParam("description") String description,
            @QueryParam("ref") String reference,
            @QueryParam("cache") Boolean cache, String query) {
        TokenContext ctx = (TokenContext) context.getUserPrincipal();
        filter = filter != null ? filter : false;
        cache = cache != null ? cache : false;
        type = StringUtils.normalize(type);
        reference = StringUtils.decodeHTML(reference);
        Map vals = new HashMap();
        User user;
        try {
            user = controller.getUser(ctx.getUsername());
        }catch (KustvaktException e) {
            KustvaktLogger.ERROR_LOGGER.error("Exception encountered!", e);
            throw KustvaktResponseHandler.throwit(e);
        }
        if (VirtualCollection.class
                .equals(ResourceFactory.getResourceClass(type))) {

            VirtualCollection cachetmp, collection;
            Object read = JsonUtils.readTree(query);
            CollectionQueryBuilder3 cquery = new CollectionQueryBuilder3();
            if (reference != null && !reference.equals("null")) {
                try {
                    cquery.addQuery(resourceHandler.findbyStrId(reference, user,
                            VirtualCollection.class).getQuery());
                }catch (KustvaktException e) {
                    throw KustvaktResponseHandler.throwit(e);
                }
                // todo: 11.01
                //                if (!filter)
                //                    cquery.addMetaExtendQuery(query);
                //                else
                //                    cquery.addMetaFilterQuery(query);
                //            }else {
                //                if (read != null)
                //                    cquery.addResource(query);
                //                else
                //                    cquery.addMetaFilterQuery(query);
            }
            cachetmp = ResourceFactory.getCachedCollection(cquery.toJSON());

            // see if vc was cached!
            VirtualCollection tmp = resourceHandler
                    .getCache(cachetmp.getId(), VirtualCollection.class);

            // if not cached, fill with stats values
            if (tmp == null) {
                String stats = searchKrill.getStatistics(cquery.toJSON());
                cachetmp.setStats(JsonUtils.readSimple(stats, Map.class));
            }

            if (!cache && !user.isDemo()) {
                collection = ResourceFactory
                        .getPermanentCollection(cachetmp, name, description,
                                user.getId());
                vals = collection.toMap();
                try {
                    resourceHandler.storeResources(user, collection);
                }catch (KustvaktException e) {
                    KustvaktLogger.ERROR_LOGGER
                            .error("Exception encountered!", e);
                    throw KustvaktResponseHandler.throwit(e);
                }
            }else {
                resourceHandler.cache(cachetmp);
                vals = cachetmp.toMap();
            }
        }
        return Response.ok(JsonUtils.toJSON(vals)).build();
    }

    @DELETE
    @Path("{type}/{id}/{child}")
    public Response deleteResourcewChild(@Context SecurityContext context,
            @Context Locale locale, @PathParam("type") String type,
            @PathParam("id") String id, @PathParam("child") String child) {
        return deleteResource(context, locale, type,
                StringUtils.joinResources(id, child));
    }

    @DELETE
    @Path("{type}/{id}")
    public Response deleteResource(@Context SecurityContext context,
            @Context Locale locale, @PathParam("type") String type,
            @PathParam("id") String id) {
        TokenContext ctx = (TokenContext) context.getUserPrincipal();
        type = StringUtils.normalizeHTML(type);
        id = StringUtils.decodeHTML(id);
        try {
            User user = controller.getUser(ctx.getUsername());
            KustvaktResource r = ResourceFactory.getResource(type);
            r.setPersistentID(id);
            //todo: eliminate the need to find the resource first!
            resourceHandler.deleteResources(user, r);
        }catch (KustvaktException e) {
            KustvaktLogger.ERROR_LOGGER.error("Exception encountered!", e);
            throw KustvaktResponseHandler.throwit(e);
        }

        return Response.ok().build();
    }

    //fixme: only allowed for corpus?!
    @GET
    @Path("/corpus/{id}/{docid}/{rest}/matchInfo")
    public Response getMatchInfo(@Context SecurityContext ctx,
            @Context Locale locale, @PathParam("id") String id,
            @PathParam("docid") String docid, @PathParam("rest") String rest,
            @QueryParam("foundry") Set<String> foundries,
            @QueryParam("layer") Set<String> layers,
            @QueryParam("spans") Boolean spans) {
        TokenContext tokenContext = (TokenContext) ctx.getUserPrincipal();
        spans = spans != null ? spans : false;
        String matchid = searchKrill.getMatchId(id, docid, rest);

        if (layers == null || layers.isEmpty())
            layers = new HashSet<>();

        boolean match_only = foundries == null || foundries.isEmpty();

        User user;
        try {
            user = controller.getUser(tokenContext.getUsername());
        }catch (KustvaktException e) {
            KustvaktLogger.ERROR_LOGGER.error("Exception encountered!", e);
            throw KustvaktResponseHandler.throwit(e);
        }

        String results;
        // fixme: checks for policy matching
        // fixme: currently disabled, due to mishab in foundry/layer spec
        // fixme:
        if (foundries != null && foundries.size() > 1000) {
            Set<String> f_list = new HashSet<>();
            Set<String> l_list = new HashSet<>();

            for (String spl : new ArrayList<>(foundries)) {
                try {
                    de.ids_mannheim.korap.security.ac.SecurityManager manager = SecurityManager
                            .init(spl, user, Permissions.PERMISSIONS.READ);
                    if (!manager.isAllowed())
                        continue;

                    String[] sep = StringUtils.splitAnnotations(spl);
                    if (spl != null) {
                        f_list.add(sep[0]);
                        l_list.add(sep[1]);
                    }
                    results = searchKrill
                            .getMatch(matchid, new ArrayList<>(f_list),
                                    new ArrayList<>(l_list), spans, false,
                                    true);
                }catch (EmptyResultException e) {
                    throw KustvaktResponseHandler
                            .throwit(StatusCodes.EMPTY_RESULTS,
                                    "Resource not found!", id);
                }catch (NotAuthorizedException e) {
                    throw KustvaktResponseHandler
                            .throwit(StatusCodes.PERMISSION_DENIED,
                                    "Permission denied", id);
                }

            }
            // all foundries shall be returned
        }else if (foundries != null && foundries.contains("*")) {
            Set<Layer> resources;
            try {
                resources = ResourceFinder.search(user, Layer.class);
            }catch (KustvaktException e) {
                KustvaktLogger.ERROR_LOGGER.error("Exception encountered!", e);
                throw KustvaktResponseHandler.throwit(e);
            }
            // returns foundries and layers.
            // todo: needs testing!
            foundries = new HashSet<>();
            layers = new HashSet<>();
            for (Layer r : resources) {
                String[] spl = StringUtils.splitAnnotations(r.getName());
                if (spl != null) {
                    foundries.add(spl[0]);
                    layers.add(spl[1]);
                }
            }
        }
        try {
            if (!match_only)
                results = searchKrill
                        .getMatch(matchid, new ArrayList<>(foundries),
                                new ArrayList<>(layers), spans, false, true);
            else
                results = searchKrill.getMatch(matchid);
        }catch (Exception e) {
            KustvaktLogger.ERROR_LOGGER.error("Exception encountered!", e);
            throw KustvaktResponseHandler
                    .throwit(StatusCodes.ILLEGAL_ARGUMENT, e.getMessage(), "");
        }
        return Response.ok(results).build();
    }

    // todo:?!
    @POST
    @Path("match/{id}/save")
    @Deprecated
    public Response save(@PathParam("{id}") String id,
            @QueryParam("d") String description,
            @Context SecurityContext context) {
        TokenContext ctx = (TokenContext) context.getUserPrincipal();
        // save match for user and later retrieval!

        //        KustvaktResource match = new QueryMatch(id);
        //        match.setDescription(description);
        //        match.setCreated(TimeUtils.getNow().getMillis());
        //        try {
        //            this.resourceHandler.storeResources(controller.getUser(ctx), match);
        //        } catch (KustvaktException | NotAuthorizedException e) {
        //            throw MappedHTTPResponse.throwit(e);
        //        }

        return Response.ok().build();
    }

    @GET
    @Path("matches")
    @Deprecated
    public Response get(@Context SecurityContext context) {
        TokenContext ctx = (TokenContext) context.getUserPrincipal();
        //todo save match for user and later retrieval!
        //todo: retrieve matches in range! --choices: date, document, id (matchid)
        return Response.ok().build();
    }

    @POST
    @Path("match/{id}/delete")
    @Deprecated
    public Response remove(@PathParam("{id}") String id,
            @Context SecurityContext context) {
        TokenContext ctx = (TokenContext) context.getUserPrincipal();
        // save match for user and later retrieval!
        try {
            this.resourceHandler
                    .deleteResources(this.controller.getUser(ctx.getUsername()),
                            id);
        }catch (KustvaktException e) {
            throw KustvaktResponseHandler.throwit(e);
        }

        return Response.ok().build();
    }

}