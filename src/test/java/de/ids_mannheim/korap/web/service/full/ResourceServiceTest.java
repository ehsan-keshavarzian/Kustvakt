package de.ids_mannheim.korap.web.service.full;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientResponse;

import de.ids_mannheim.korap.config.Attributes;
import de.ids_mannheim.korap.exceptions.KustvaktException;
import de.ids_mannheim.korap.handlers.ResourceDao;
import de.ids_mannheim.korap.query.serialize.QuerySerializer;
import de.ids_mannheim.korap.resources.KustvaktResource;
import de.ids_mannheim.korap.security.auth.BasicHttpAuth;
import de.ids_mannheim.korap.user.User;
import de.ids_mannheim.korap.utils.JsonUtils;
import de.ids_mannheim.korap.web.service.FastJerseyTest;

/**
 * @author hanl
 * @date 14/01/2016
 */
public class ResourceServiceTest extends FastJerseyTest {
    private static ObjectMapper mapper = new ObjectMapper();
    
    @BeforeClass
    public static void configure () throws Exception {
        FastJerseyTest.setPackages("de.ids_mannheim.korap.web.service.full",
                "de.ids_mannheim.korap.web.filter",
                "de.ids_mannheim.korap.web.utils");
        
        //containerURI = "https://localhost/";
    }


    @Test
    public void testSearchSimpleAuthorized () {
        ClientResponse response = resource()
                .path(getAPIVersion())
                .path("search")
                .queryParam("q", "[orth=die]")
                .queryParam("ql", "poliqarp")
                .header(Attributes.AUTHORIZATION,
                        BasicHttpAuth.encode("kustvakt", "kustvakt2015"))
                .get(ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());

        JsonNode node = JsonUtils.readTree(response.getEntity(String.class));
        assertNotNull(node);
        assertNotEquals(0, node.path("matches").size());
    }


    @Test
    public void testSearchSimpleWithCQAuthorized () {
        ClientResponse response = resource()
                .path(getAPIVersion())
                .path("search")
                .queryParam("q", "[orth=das]")
                .queryParam("ql", "poliqarp")
                .queryParam("cq", "textClass=politik & corpusSigle=WPD")
                .header(Attributes.AUTHORIZATION,
                        BasicHttpAuth.encode("kustvakt", "kustvakt2015"))
                .get(ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());

        JsonNode node = JsonUtils.readTree(response.getEntity(String.class));
        assertNotNull(node);
        assertEquals(2, node.at("/collection/operands").size());
        assertEquals("textClass", node.at("/collection/operands/0/key")
                .asText());
        assertEquals("corpusSigle", node.at("/collection/operands/1/key")
                .asText());
        assertEquals("koral:token", node.at("/query/@type").asText());
    }


    @Test
    public void testSearchSimpleDemo () {
        ClientResponse response = resource().path(getAPIVersion())
                .path("search").queryParam("q", "[orth=der]")
                .queryParam("ql", "poliqarp").get(ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        String ent = response.getEntity(String.class);
        JsonNode node = JsonUtils.readTree(ent);
        assertNotNull(node);
        assertNotEquals(0, node.path("matches").size());
    }


    @Test
    public void testSearchSentenceMeta () {
        ClientResponse response = resource().path(getAPIVersion())
                .path("search").queryParam("q", "[orth=der]")
                .queryParam("ql", "poliqarp").queryParam("context", "sentence")
                .get(ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        String ent = response.getEntity(String.class);
        JsonNode node = JsonUtils.readTree(ent);
        assertNotNull(node);
        assertNotEquals(0, node.path("matches").size());
        assertEquals("base/s:s", node.at("/meta/context").asText());
        assertNotEquals("${project.version}", "/meta/version");
    }


    @Test
    public void testSearchSimpleCQL () {
        QuerySerializer s = new QuerySerializer();
        s.setQuery("(der) or (das)", "CQL");

        ClientResponse response = resource().path(getAPIVersion())
                .path("search").post(ClientResponse.class, s.toJSON());
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        String ent = response.getEntity(String.class);

        JsonNode node = JsonUtils.readTree(ent);
        assertNotNull(node);
        assertNotEquals(0, node.path("matches").size());
    }


    @Test
    public void testSearchRawQuery () {
        QuerySerializer s = new QuerySerializer();
        s.setQuery("[orth=der]", "poliqarp");
        //        s.setCollection("corpusSigle=WPD");

        ClientResponse response = resource().path(getAPIVersion())
                .path("search").post(ClientResponse.class, s.toJSON());
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        String ent = response.getEntity(String.class);


        JsonNode node = JsonUtils.readTree(ent);
        assertNotNull(node);
        assertNotEquals(0, node.path("matches").size());
    }


    @Test
    public void testCollectionsGetPublic () {
        ClientResponse response = resource().path(getAPIVersion())
                .path("collection").get(ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());

        JsonNode node = JsonUtils.readTree(response.getEntity(String.class));
        assertNotNull(node);
        assertNotEquals(0, node.size());
    }


    @Test
    public void testCollectionsGet () {
        ClientResponse response = resource()
                .path(getAPIVersion())
                .path("collection")
                .header(Attributes.AUTHORIZATION,
                        BasicHttpAuth.encode("kustvakt", "kustvakt2015"))
                .get(ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());

        JsonNode node = JsonUtils.readTree(response.getEntity(String.class));
        assertNotNull(node);
        assertTrue(node.isArray());
        assertNotEquals(0, node.size());
    }


    @Test
    public void testCorporaGet () {
        ClientResponse response = resource().path(getAPIVersion())
                .path("corpus").get(ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        String ent = response.getEntity(String.class);
        JsonNode node = JsonUtils.readTree(ent);
        assertNotNull(node);
        assertTrue(node.isArray());
        assertNotEquals(0, node.size());
    }


    @Test
    public void testFoundriesGet () {
        ClientResponse response = resource().path(getAPIVersion())
                .path("foundry").get(ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        String ent = response.getEntity(String.class);
        JsonNode node = JsonUtils.readTree(ent);
        assertNotNull(node);
        assertTrue(node.isArray());
        assertNotEquals(0, node.size());
    }


    // create a simple test collection for user kustvakt, otherwise test fails
    @Test
    @Ignore
    public void testStats () {
        ClientResponse response = resource()
                .path(getAPIVersion())
                .path("collection")
                .header(Attributes.AUTHORIZATION,
                        BasicHttpAuth.encode("kustvakt", "kustvakt2015"))
                .get(ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());

        JsonNode node = JsonUtils.readTree(response.getEntity(String.class));
        assertNotNull(node);
        assertNotEquals(0, node.size());
        String id = node.path(1).path("id").asText();

        response = resource()
                .path(getAPIVersion())
                .path("collection")
                .path(id)
                .path("stats")
                .header(Attributes.AUTHORIZATION,
                        BasicHttpAuth.encode("kustvakt", "kustvakt2015"))
                .get(ClientResponse.class);

        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        node = JsonUtils.readTree(response.getEntity(String.class));
        assertNotNull(node);
        assertNotNull(node);
        int docs = node.path("documents").asInt();
        assertNotEquals(0, docs);
        assertTrue(docs < 15);
    }


    // todo:
    @Test
    @Ignore
    public void testCollecionGet () {
        ClientResponse response = resource().path(getAPIVersion())
                .path("collection").path("id").get(ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        String ent = response.getEntity(String.class);
        JsonNode node = JsonUtils.readTree(ent);
        assertNotNull(node);
        assertNotEquals(0, node.size());
    }

    @Test
    public void testResourceStore() throws KustvaktException, JsonProcessingException, IOException {
    	// resource store service
        ClientResponse response = resource()
                .path(getAPIVersion())
                .path("virtualcollection")
                .queryParam("filter", "false")
                .queryParam("name", "Goethe")
                .queryParam("description", "Goethe corpus")
                .header(Attributes.AUTHORIZATION,
                        BasicHttpAuth.encode("kustvakt", "kustvakt2015"))
                .post(ClientResponse.class);
        
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        
        String ent = response.getEntity(String.class);
        
        JsonNode node = JsonUtils.readTree(ent);
        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals("Goethe", node.path("name").asText());
        assertEquals("Goethe corpus", node.path("description").asText());

        String id = node.path("id").asText();
        		
        // check if the resource is in the db
        ResourceDao<?> dao = new ResourceDao<>(helper().getContext()
                .getPersistenceClient());
        assertEquals("sqlite", helper().getContext().getPersistenceClient()
                .getDatabase());

        assertNotEquals(0, dao.size());
        KustvaktResource res = dao.findbyId(id,
                User.UserFactory.getDemoUser());
        assertNotNull(res);
        Assert.assertEquals("Goethe",res.getName().toString());
        
//         no update resource service
        response = resource()
                .path(getAPIVersion())
                .path("virtualcollection")
                .path(id)
                .queryParam("name", "Goethe")
                .header(Attributes.AUTHORIZATION,
                        BasicHttpAuth.encode("kustvakt", "kustvakt2015"))
                .post(ClientResponse.class);
        
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
        JsonNode errorNode = mapper.readTree(response.getEntityInputStream());
        assertEquals(
                "[No change has found.]",
                errorNode.get("errors").get(0).get(2).asText());
        
        // update resource service
        response = resource()
                .path(getAPIVersion())
                .path("virtualcollection")
                .path(id)
                .queryParam("name", "Goethe collection")
                .header(Attributes.AUTHORIZATION,
                        BasicHttpAuth.encode("kustvakt", "kustvakt2015"))
                .post(ClientResponse.class);
        
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        
        res = dao.findbyId(id,
                User.UserFactory.getDemoUser());
        assertNotNull(res);
        Assert.assertEquals("Goethe collection",res.getName().toString());
        
        
        // delete resource service
    	response = resource()
                .path(getAPIVersion())
                .path("virtualcollection")
                .path(id)
                .header(Attributes.AUTHORIZATION,
                        BasicHttpAuth.encode("kustvakt", "kustvakt2015"))
                .delete(ClientResponse.class);
        
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        
        // check if the resource is *not* in the db anymore
        dao = new ResourceDao<>(helper().getContext()
                .getPersistenceClient());
        assertEquals("sqlite", helper().getContext().getPersistenceClient()
                .getDatabase());

        res = dao.findbyId(id,
                User.UserFactory.getDemoUser());
        assertEquals(null,res);
    }


    @Test
    public void testCorpusGet () {
        ClientResponse response = resource().path(getAPIVersion())
                .path("corpus").path("WPD15").get(ClientResponse.class);
        String ent = response.getEntity(String.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());

        JsonNode node = JsonUtils.readTree(ent);
        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals("WPD15", node.path("id").asText());
    }

    @Test
    public void testCorpusGet2 () {
    	ClientResponse response = resource().path(getAPIVersion())
                .path("corpus").path("GOE").get(ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        String ent = response.getEntity(String.class);
        JsonNode node = JsonUtils.readTree(ent);
        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals("GOE", node.path("id").asText());
    }
    
    
    @Test
    @Ignore
    public void testCorpusGetUnauthorized () {
        ClientResponse response = resource().path(getAPIVersion())
                .path("corpus").path("BRZ20").get(ClientResponse.class);
        assertEquals(ClientResponse.Status.UNAUTHORIZED.getStatusCode(),
                response.getStatus());
        String ent = response.getEntity(String.class);
        JsonNode node = JsonUtils.readTree(ent);
        assertNotNull(node);
        assertTrue(node.isObject());
        assertNotNull(node);
    }

    @Test
    public void testFoundryGet () {
        ClientResponse response = resource().path(getAPIVersion())
                .path("foundry").path("tt").get(ClientResponse.class);
        String ent = response.getEntity(String.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());

        JsonNode node = JsonUtils.readTree(ent);
        assertNotNull(node);
        assertNotEquals(0, node.size());
    }


    

    @Test
    public void testSerializationQueryWithCorpusThroughFilteredPublic () {
        ClientResponse response = resource().path(getAPIVersion())
                .path("corpus/WPD15/search").queryParam("q", "[orth=der]")
                .queryParam("ql", "poliqarp").queryParam("context", "base/s:s")
                .method("TRACE", ClientResponse.class);
        String ent = response.getEntity(String.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        //String ent = response.getEntity(String.class);
        JsonNode node = JsonUtils.readTree(ent);
        assertNotNull(node);
        assertEquals("corpusSigle", node.at("/collection/key").asText());
        assertEquals("WPD15", node.at("/collection/value").asText());
    }


    @Test
    public void testSerializationQueryWithCorpus () {
        ClientResponse response = resource()
                .path(getAPIVersion())
                .path("corpus/WPD15/search")
                .queryParam("q", "[orth=der]")
                .queryParam("ql", "poliqarp")
                .header(Attributes.AUTHORIZATION,
                        BasicHttpAuth.encode("kustvakt", "kustvakt2015"))
                .method("TRACE", ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        String ent = response.getEntity(String.class);
        JsonNode node = JsonUtils.readTree(ent);
        assertNotNull(node);
        assertEquals("koral:doc", node.at("/collection/@type").asText());
        assertEquals("corpusSigle", node.at("/collection/key").asText());
    }

    @Test
    public void testSerializationQueryWithCollection () {
    	// Add Virtual Collection
    	ClientResponse response = resource()
                .path(getAPIVersion())
                .path("virtualcollection")
                .queryParam("filter", "false")
                .queryParam("name", "Weimarer Werke")
                .queryParam("description", "Goethe-Werke in Weimar (seit 1775)")
                .header(Attributes.AUTHORIZATION,
                        BasicHttpAuth.encode("kustvakt", "kustvakt2015"))
                .post(ClientResponse.class, "creationDate since 1775 & corpusSigle=GOE");
        
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        
        String ent = response.getEntity(String.class);
        
        JsonNode node = JsonUtils.readTree(ent);
        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals("Weimarer Werke", node.path("name").asText());

        // Get virtual collections
        response = resource()
                .path(getAPIVersion())
                .path("collection")
                .header(Attributes.AUTHORIZATION,
                        BasicHttpAuth.encode("kustvakt", "kustvakt2015"))
                .get(ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        ent = response.getEntity(String.class);
        node = JsonUtils.readTree(ent);
        assertNotNull(node);

        Iterator it = node.elements();
        String id = null;
        while (it.hasNext()) {
            JsonNode next = (JsonNode) it.next();
            if ("Weimarer Werke".equals(next.path("name").asText()))
                id = next.path("id").asText();
        }
        assertNotNull(id);
        assertFalse(id.isEmpty());

        response = resource()
                .path(getAPIVersion())
                .path("collection")
                .path(id)
                .path("search")
                .queryParam("q", "[orth=der]")
                .queryParam("ql", "poliqarp")
                .queryParam("context", "base/s:s")
                .header(Attributes.AUTHORIZATION,
                        BasicHttpAuth.encode("kustvakt", "kustvakt2015"))
                .method("TRACE", ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        ent = response.getEntity(String.class);
        node = JsonUtils.readTree(ent);
        assertNotNull(node);

        assertEquals("koral:docGroup", node.at("/collection/@type").asText());
        assertEquals("koral:doc", node.at("/collection/operands/0/@type")
                .asText());
        assertEquals("koral:doc", node.at("/collection/operands/1/@type")
                .asText());
        assertEquals("creationDate", node.at("/collection/operands/0/key")
                .asText());
        assertEquals("corpusSigle", node.at("/collection/operands/1/key")
                .asText());

    }


    @Test
    public void testSearchQueryPublicCorpora () {
        ClientResponse response = resource().path(getAPIVersion())
                .path("search").queryParam("q", "[orth=der]")
                .queryParam("ql", "poliqarp").get(ClientResponse.class);
        assertEquals(response.getStatus(),
                ClientResponse.Status.OK.getStatusCode());
        String ent = response.getEntity(String.class);
		JsonNode node = JsonUtils.readTree(ent);
        assertNotNull(node);
        assertEquals("koral:docGroup", node.at("/collection/@type").asText());
        assertEquals("operation:or", node.at("/collection/operation").asText());
        assertNotEquals(0, node.at("/collection/operands").size());
    }


    // use trace for this
    @Test
    @Ignore
    public void testSerializationMeta () {
        ClientResponse response = resource().path(getAPIVersion())
                .path("search").queryParam("context", "sentence")
                .queryParam("q", "[pos=ADJA]").queryParam("ql", "poliqarp")

                .method("TRACE", ClientResponse.class);
        assertEquals(response.getStatus(),
                ClientResponse.Status.OK.getStatusCode());
    }


    @Test
    public void testMatchInfoGet () {
    }


    @Test
    public void testMatchInfoSave () {

    }


    @Test
    public void testMatchInfoDelete () {

    }


    @Test
    public void testGetMatches () {

    }


    @Override
    public void initMethod () throws KustvaktException {
        helper().runBootInterfaces();
    }
}
