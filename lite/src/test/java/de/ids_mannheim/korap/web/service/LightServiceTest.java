package de.ids_mannheim.korap.web.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.lucene.LucenePackage;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jersey.api.client.ClientResponse;

import de.ids_mannheim.korap.exceptions.KustvaktException;
import de.ids_mannheim.korap.query.serialize.QuerySerializer;
import de.ids_mannheim.korap.utils.JsonUtils;

/**
 * EM: FIX ME: Database restructure
 * 
 * Created by hanl on 29.04.16.
 */
@Ignore
public class LightServiceTest extends FastJerseyLightTest {

    @Override
    public void initMethod () throws KustvaktException {}


    @Test
    public void testQueryTrace () {
        ClientResponse response = resource().path(getAPIVersion())
                .path("search").queryParam("q", "[orth=das]")
                .queryParam("ql", "poliqarp").queryParam("context", "sentence")
                .queryParam("count", "13")
                .method("TRACE", ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        String query = response.getEntity(String.class);
        JsonNode node = JsonUtils.readTree(query);
        assertNotNull(node);
        assertEquals("orth", node.at("/query/wrap/layer").asText());
        assertEquals("opennlp", node.at("/query/wrap/foundry").asText());
        assertEquals("sentence", node.at("/meta/context").asText());
        assertEquals("13", node.at("/meta/count").asText());
    }


    @Test
    public void testbuildAndPostQuery () {
        ClientResponse response = resource().path(getAPIVersion())
                .path("search").queryParam("q", "[orth=das]")
                .queryParam("ql", "poliqarp")
                .queryParam("cq", "corpusSigle=WPD | corpusSigle=GOE")
                .method("TRACE", ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());

        String query = response.getEntity(String.class);
        JsonNode node = JsonUtils.readTree(query);
        assertNotNull(node);

        response = resource().path(getAPIVersion()).path("search")
                .post(ClientResponse.class, query);

        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        String matches = response.getEntity(String.class);
        JsonNode match_node = JsonUtils.readTree(matches);
        assertNotEquals(0, match_node.path("matches").size());
    }


    @Test
    public void testQueryGet () {
        ClientResponse response = resource().path(getAPIVersion())
                .path("search").queryParam("q", "[orth=das]")
                .queryParam("ql", "poliqarp").queryParam("context", "sentence")
                .queryParam("count", "13").get(ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        String query = response.getEntity(String.class);
        JsonNode node = JsonUtils.readTree(query);
        assertNotNull(node);
        assertEquals("orth", node.at("/query/wrap/layer").asText());
        assertEquals("base/s:s", node.at("/meta/context").asText());
        assertEquals("13", node.at("/meta/count").asText());
        assertNotEquals(0, node.at("/matches").size());
    }


    @Test
    public void testFoundryRewrite () {
        ClientResponse response = resource().path(getAPIVersion())
                .path("search").queryParam("q", "[orth=das]")
                .queryParam("ql", "poliqarp").queryParam("context", "sentence")
                .queryParam("count", "13").get(ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        String query = response.getEntity(String.class);
        JsonNode node = JsonUtils.readTree(query);
        assertNotNull(node);
        assertEquals("orth", node.at("/query/wrap/layer").asText());
        assertEquals("opennlp", node.at("/query/wrap/foundry").asText());
    }


    @Test
    public void testQueryPost () {
        QuerySerializer s = new QuerySerializer();
        s.setQuery("[orth=das]", "poliqarp");

        ClientResponse response = resource().path(getAPIVersion())
                .path("search").post(ClientResponse.class, s.toJSON());
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        String query = response.getEntity(String.class);
        JsonNode node = JsonUtils.readTree(query);
        assertNotNull(node);
        assertEquals("orth", node.at("/query/wrap/layer").asText());
        assertNotEquals(0, node.at("/matches").size());
    }


    @Test
    public void testParameterField () {
        ClientResponse response = resource().path(getAPIVersion())
                .path("search").queryParam("q", "[orth=das]")
                .queryParam("ql", "poliqarp")
                .queryParam("fields", "author, docSigle")
                .queryParam("context", "sentence").queryParam("count", "13")
                .get(ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        String query = response.getEntity(String.class);
        JsonNode node = JsonUtils.readTree(query);
        assertNotNull(node);
        assertEquals("orth", node.at("/query/wrap/layer").asText());
        assertNotEquals(0, node.at("/matches").size());
        assertEquals("[\"author, docSigle\"]", node.at("/meta/fields")
                .toString());
    }

	@Test
	public void testMatchInfoGetWithoutSpans () {
        ClientResponse response = resource()
			.path(getAPIVersion())
			.path("corpus/GOE/AGA/01784/p36-46/matchInfo")
			.queryParam("foundry", "*")
			.queryParam("spans", "false")
			.get(ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
					 response.getStatus());
        String ent = response.getEntity(String.class);
        JsonNode node = JsonUtils.readTree(ent);
        assertNotNull(node);
        assertEquals("GOE/AGA/01784", node.at("/textSigle").asText());
        assertEquals("Belagerung von Mainz", node.at("/title").asText());
	};

	@Test
	public void testMatchInfoGet2 () {
        ClientResponse response = resource()
			.path(getAPIVersion())
			.path("corpus/GOE/AGA/01784/p36-46/matchInfo")
			.queryParam("foundry", "*")
			.get(ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
					 response.getStatus());
        String ent = response.getEntity(String.class);
        JsonNode node = JsonUtils.readTree(ent);
        assertNotNull(node);
        assertEquals("GOE/AGA/01784", node.at("/textSigle").asText());
        assertEquals("Belagerung von Mainz", node.at("/title").asText());
	};

    @Test
    public void testCQParameter () {
        ClientResponse response = resource().path(getAPIVersion())
                .path("search").queryParam("q", "[orth=das]")
                .queryParam("ql", "poliqarp")
                .queryParam("fields", "author, docSigle")
                .queryParam("context", "sentence").queryParam("count", "13")
                .queryParam("cq", "textClass=Politik & corpus=WPD")
                .method("TRACE", ClientResponse.class);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        String query = response.getEntity(String.class);
        JsonNode node = JsonUtils.readTree(query);
        assertNotNull(node);
        assertEquals("orth", node.at("/query/wrap/layer").asText());
        assertEquals("Politik", node.at("/collection/operands/0/value")
                .asText());
        assertEquals("WPD", node.at("/collection/operands/1/value").asText());

        response = resource().path(getAPIVersion()).path("search")
                .queryParam("q", "[orth=das]").queryParam("ql", "poliqarp")
                .queryParam("fields", "author, docSigle")
                .queryParam("context", "sentence").queryParam("count", "13")
                .queryParam("cq", "textClass=Politik & corpus=WPD")
                .get(ClientResponse.class);
        String version = LucenePackage.get().getImplementationVersion();;
        System.out.println("VERSION "+ version);
        System.out.println("RESPONSE "+ response);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                response.getStatus());
        query = response.getEntity(String.class);
        node = JsonUtils.readTree(query);
        assertNotNull(node);
        assertEquals("orth", node.at("/query/wrap/layer").asText());
        assertEquals("Politik", node.at("/collection/operands/0/value")
                .asText());
        assertEquals("WPD", node.at("/collection/operands/1/value").asText());
    }

}
