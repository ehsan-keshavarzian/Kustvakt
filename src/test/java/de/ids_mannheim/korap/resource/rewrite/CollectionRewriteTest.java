package de.ids_mannheim.korap.resource.rewrite;

import com.fasterxml.jackson.databind.JsonNode;
import de.ids_mannheim.korap.config.BeanConfigTest;
import de.ids_mannheim.korap.config.KustvaktConfiguration;
import de.ids_mannheim.korap.exceptions.KustvaktException;
import de.ids_mannheim.korap.query.serialize.QuerySerializer;
import de.ids_mannheim.korap.resources.Corpus;
import de.ids_mannheim.korap.security.ac.ResourceFinder;
import de.ids_mannheim.korap.user.User;
import de.ids_mannheim.korap.utils.JsonUtils;
import org.junit.Test;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author hanl
 * @date 03/09/2015
 */
public class CollectionRewriteTest extends BeanConfigTest {

    private static String simple_add_query = "[pos=ADJA]";

    private static KustvaktConfiguration config;


    @Override
    public void initMethod () throws KustvaktException {
        helper().runBootInterfaces();
        config = helper().getContext().getConfiguration();
    }


    @Deprecated
    @Test
    public void test2 () {
        Pattern p = Pattern.compile("([\\.\\w]+)\\((.+)\\)");
        String cl = de.ids_mannheim.korap.security.ac.SecurityManager.class
                .getCanonicalName();
        Matcher m = p.matcher(cl);
        while (m.find())
            System.out.println("group 1 " + m.group(1));

    }


    @Test
    public void testCollectionNodeRemoveCorpusIdNoErrors () {
        RewriteHandler handler = new RewriteHandler();
        handler.insertBeans(helper().getContext());
        handler.add(CollectionConstraint.class);
        QuerySerializer s = new QuerySerializer();
        s.setQuery(simple_add_query, "poliqarp");
        s.setCollection("textClass=politik & corpusSigle=WPD");
        String result = s.toJSON();
        JsonNode node = JsonUtils.readTree(handler.process(result,
                User.UserFactory.getUser("test_user")));
        assertNotNull(node);
        assertEquals(1, node.at("/collection/operands").size());
    }

    @Test
    public void testCollectionNodeDeletionNotification() {

    }


    @Test
    public void testCollectionNodeRemoveAllCorpusIdNoErrors () {
        RewriteHandler handler = new RewriteHandler();
        handler.insertBeans(helper().getContext());
        handler.add(CollectionConstraint.class);
        QuerySerializer s = new QuerySerializer();
        s.setQuery(simple_add_query, "poliqarp");
        s.setCollection("corpusSigle=BRZ13 & corpusSigle=WPD");
        String result = s.toJSON();
        JsonNode node = JsonUtils.readTree(handler.process(result,
                User.UserFactory.getUser("test_user")));
        //        System.out.println("RESULTING REWR NODE " + node);
        assertNotNull(node);
        assertEquals(0, node.at("/collection/operands").size());
    }


    @Test
    public void testCollectionNodeRemoveGroupedCorpusIdNoErrors () {
        RewriteHandler handler = new RewriteHandler();
        handler.insertBeans(helper().getContext());
        handler.add(CollectionConstraint.class);
        QuerySerializer s = new QuerySerializer();
        s.setQuery(simple_add_query, "poliqarp");
        s.setCollection("(corpusSigle=BRZ13 & textClass=Wissenschaft) & corpusSigle=WPD");
        String result = s.toJSON();
        JsonNode node = JsonUtils.readTree(handler.process(result,
                User.UserFactory.getUser("test_user")));

        assertNotNull(node);
        assertEquals("koral:docGroup", node.at("/collection/operands/0/@type")
                .asText());
        assertEquals("textClass",
                node.at("/collection/operands/0/operands/0/key").asText());
    }


    //fixme: will probably fail when one doc groups are being refactored
    @Test
    public void testCollectionCleanEmptyDocGroupNoErrors () {
        RewriteHandler handler = new RewriteHandler();
        handler.insertBeans(helper().getContext());
        handler.add(CollectionConstraint.class);
        handler.add(CollectionCleanupFilter.class);
        QuerySerializer s = new QuerySerializer();
        s.setQuery(simple_add_query, "poliqarp");
        s.setCollection("(corpusSigle=BRZ13 & corpusSigle=WPD) & textClass=Wissenschaft & textClass=Sport");
        String result = s.toJSON();

        JsonNode node = JsonUtils.readTree(handler.process(result,
                User.UserFactory.getUser("test_user")));


        assertNotNull(node);
        assertEquals("koral:docGroup", node.at("/collection/@type").asText());
        assertEquals(2, node.at("/collection/operands").size());

        assertEquals("textClass", node.at("/collection/operands/0/key")
                .asText());
        assertEquals("textClass", node.at("/collection/operands/1/key")
                .asText());
    }


    @Test
    public void testCollectionCleanMoveOneDocFromGroupUpNoErrors () {
        RewriteHandler handler = new RewriteHandler();
        handler.insertBeans(helper().getContext());
        handler.add(CollectionConstraint.class);
        handler.add(CollectionCleanupFilter.class);
        QuerySerializer s = new QuerySerializer();
        s.setQuery(simple_add_query, "poliqarp");
        s.setCollection("(corpusSigle=BRZ13 & textClass=Wissenschaft)");
        String result = s.toJSON();
        JsonNode node = JsonUtils.readTree(handler.process(result,
                User.UserFactory.getUser("test_user")));
        assertNotNull(node);
        assertEquals("koral:doc", node.at("/collection/@type").asText());
    }


    @Test
    public void testCollectionCleanEmptyGroupAndMoveOneFromGroupUpNoErrors () {
        RewriteHandler handler = new RewriteHandler();
        handler.insertBeans(helper().getContext());
        handler.add(CollectionConstraint.class);
        handler.add(CollectionCleanupFilter.class);
        QuerySerializer s = new QuerySerializer();
        s.setQuery(simple_add_query, "poliqarp");
        s.setCollection("(corpusSigle=BRZ13 & corpusSigle=WPD) & textClass=Wissenschaft");
        String result = s.toJSON();
        JsonNode node = JsonUtils.readTree(handler.process(result,
                User.UserFactory.getUser("test_user")));

        assertNotNull(node);
        assertEquals("koral:doc", node.at("/collection/@type").asText());
        assertEquals("textClass", node.at("/collection/key").asText());
    }


    @Test
    public void testCollectionRemoveAndMoveOneFromGroupUpNoErrors () {
        RewriteHandler handler = new RewriteHandler();
        handler.insertBeans(helper().getContext());
        handler.add(CollectionConstraint.class);
        handler.add(CollectionCleanupFilter.class);
        QuerySerializer s = new QuerySerializer();
        s.setQuery(simple_add_query, "poliqarp");
        s.setCollection("(docID=random & textClass=Wissenschaft) & corpusSigle=WPD");
        String org = s.toJSON();
        JsonNode node = JsonUtils.readTree(handler.process(org,
                User.UserFactory.getUser("test_user")));
        System.out.println(node);
        assertNotNull(node);
        assertEquals("koral:docGroup", node.at("/collection/@type").asText());
        assertEquals(2, node.at("/collection/operands").size());
        assertEquals("koral:doc", node.at("/collection/operands/0/@type")
                .asText());
    }


    @Test
    public void testPublicCollectionRewriteEmptyAdd () throws KustvaktException {
        RewriteHandler handler = new RewriteHandler();
        handler.insertBeans(helper().getContext());
        handler.add(PublicCollection.class);

        QuerySerializer s = new QuerySerializer();
        s.setQuery(simple_add_query, "poliqarp");
        String org = s.toJSON();
        JsonNode node = JsonUtils.readTree(handler.process(org,
                User.UserFactory.getUser("test_user")));
        assertNotNull(node);
        assertEquals("corpusSigle", node.at("/collection/operands/0/key").asText());
        assertEquals("corpusSigle", node.at("/collection/operands/1/key").asText());
        assertEquals("koral:rewrite", node.at("/collection/rewrites/@type").asText());
    }


    @Test
    public void testPublicCollectionRewriteNonEmptyAdd ()
            throws KustvaktException {
        RewriteHandler handler = new RewriteHandler();
        handler.insertBeans(helper().getContext());
        handler.add(PublicCollection.class);

        try {
            QuerySerializer s = new QuerySerializer();
            s.setQuery(simple_add_query, "poliqarp");
            s.setCollection("(docSigle=WPD_AAA & textClass=wissenschaft)");
            String org = s.toJSON();
            JsonNode node = JsonUtils.readTree(handler.process(org,
                    User.UserFactory.getUser("test_user")));
            assertNotNull(node);
            assertEquals(2, node.at("/collection/operands").size());
            assertEquals("corpusSigle", node.at("/collection/operands/1/operands/0/key").asText());
            assertEquals("corpusSigle", node.at("/collection/operands/1/operands/1/key").asText());
            assertEquals("koral:rewrite", node.at("/collection/rewrites/@type").asText());

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }



}
