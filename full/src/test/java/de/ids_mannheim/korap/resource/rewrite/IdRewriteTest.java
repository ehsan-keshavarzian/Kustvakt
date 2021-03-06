package de.ids_mannheim.korap.resource.rewrite;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import de.ids_mannheim.korap.config.BeanConfigTest;
import de.ids_mannheim.korap.exceptions.KustvaktException;
import de.ids_mannheim.korap.query.serialize.QuerySerializer;
import de.ids_mannheim.korap.utils.JsonUtils;

/**
 * @author hanl
 * @date 21/10/2015
 */
public class IdRewriteTest extends BeanConfigTest {

    @Test
    public void insertTokenId () throws KustvaktException {
        RewriteHandler handler = new RewriteHandler();
        handler.insertBeans(helper().getContext());
        assertTrue(handler.add(IdWriter.class));

        String query = "[surface=Wort]";
        QuerySerializer s = new QuerySerializer();
        s.setQuery(query, "poliqarp");

        String value = handler.processQuery(s.toJSON(), null);
        JsonNode result = JsonUtils.readTree(value);

        assertNotNull(result);
        assertTrue(result.path("query").has("idn"));
    }


    @Test
    public void testIdWriterTest () throws KustvaktException {
        RewriteHandler handler = new RewriteHandler();
        handler.insertBeans(helper().getContext());
        assertTrue(handler.add(IdWriter.class));

        QuerySerializer s = new QuerySerializer();
        s.setQuery("[base=Haus]", "poliqarp");
        String result = handler.processQuery(s.toJSON(), null);
        JsonNode node = JsonUtils.readTree(result);
        assertNotNull(node);
        assertFalse(node.at("/query/wrap").isMissingNode());
        assertFalse(node.at("/query/idn").isMissingNode());
    }


    @Override
    public void initMethod () throws KustvaktException {

    }
}
