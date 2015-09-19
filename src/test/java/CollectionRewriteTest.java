import com.fasterxml.jackson.databind.JsonNode;
import de.ids_mannheim.korap.config.BeanConfiguration;
import de.ids_mannheim.korap.config.KustvaktConfiguration;
import de.ids_mannheim.korap.query.serialize.QuerySerializer;
import de.ids_mannheim.korap.resource.rewrite.CollectionCleanupFilter;
import de.ids_mannheim.korap.resource.rewrite.CollectionConstraint;
import de.ids_mannheim.korap.resource.rewrite.RewriteHandler;
import de.ids_mannheim.korap.utils.JsonUtils;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author hanl
 * @date 03/09/2015
 */
public class CollectionRewriteTest {

    private static String simple_add_query = "[pos=ADJA]";

    private static KustvaktConfiguration config;

    @BeforeClass
    public static void init() {
        BeanConfiguration.loadClasspathContext();
        config = BeanConfiguration.getBeans().getConfiguration();
    }

    @Test
    public void testCollectionNodeRemoveCorpusIdNoErrors() {
        RewriteHandler handler = new RewriteHandler();
        handler.add(new CollectionConstraint());
        QuerySerializer s = new QuerySerializer();
        s.setQuery(simple_add_query, "poliqarp");
        s.setCollection("textClass=politik & corpusID=WPD");
        String result = s.toJSON();
        JsonNode node = JsonUtils.readTree(handler.apply(result, null));
        assert node != null;
        assert node.at("/collection/operands").size() == 1;
    }

    @Test
    public void testCollectionNodeRemoveAllCorpusIdNoErrors() {
        RewriteHandler handler = new RewriteHandler();
        handler.add(new CollectionConstraint());
        QuerySerializer s = new QuerySerializer();
        s.setQuery(simple_add_query, "poliqarp");
        s.setCollection("corpusID=BRZ13 & corpusID=WPD");
        String result = s.toJSON();
        JsonNode node = JsonUtils.readTree(handler.apply(result, null));

        assert node != null;
        assert node.at("/collection/operands").size() == 0;
    }

    @Test
    public void testCollectionNodeRemoveGroupedCorpusIdNoErrors() {
        RewriteHandler handler = new RewriteHandler();
        handler.add(new CollectionConstraint());
        QuerySerializer s = new QuerySerializer();
        s.setQuery(simple_add_query, "poliqarp");
        s.setCollection(
                "(corpusID=BRZ13 & textClass=Wissenschaft) & corpusID=WPD");
        String result = s.toJSON();
        JsonNode node = JsonUtils.readTree(handler.apply(result, null));

        assert node != null;
        assert node.at("/collection/operands/0/@type").asText()
                .equals("koral:docGroup");
        assert node.at("/collection/operands/0/operands/0/key").asText()
                .equals("textClass");
    }

    //fixme: will probably fail when one doc groups are being refactored
    @Test
    public void testCollectionCleanEmptyDocGroupNoErrors() {
        RewriteHandler handler = new RewriteHandler();
        handler.add(new CollectionConstraint());
        handler.add(new CollectionCleanupFilter());
        QuerySerializer s = new QuerySerializer();
        s.setQuery(simple_add_query, "poliqarp");
        s.setCollection(
                "(corpusID=BRZ13 & corpusID=WPD) & textClass=Wissenschaft & textClass=Sport");
        String result = s.toJSON();
        JsonNode node = JsonUtils.readTree(handler.apply(result, null));
        assert node != null;
        assert node.at("/collection/@type").asText().equals("koral:docGroup");
        assert node.at("/collection/operands").size() == 2;
        assert node.at("/collection/operands/0/key").asText()
                .equals("textClass");
        assert node.at("/collection/operands/1/key").asText()
                .equals("textClass");
    }

    @Test
    public void testCollectionCleanMoveOneDocFromGroupUpNoErrors() {
        RewriteHandler handler = new RewriteHandler();
        handler.add(new CollectionConstraint());
        handler.add(new CollectionCleanupFilter());
        QuerySerializer s = new QuerySerializer();
        s.setQuery(simple_add_query, "poliqarp");
        s.setCollection("(corpusID=BRZ13 & textClass=Wissenschaft)");
        String result = s.toJSON();
        JsonNode node = JsonUtils.readTree(handler.apply(result, null));
        assert node != null;
        assert node.at("/collection/@type").asText().equals("koral:doc");
    }

    @Test
    public void testCollectionCleanEmptyGroupAndMoveOneFromGroupUpNoErrors() {
        RewriteHandler handler = new RewriteHandler();
        handler.add(new CollectionConstraint());
        handler.add(new CollectionCleanupFilter());
        QuerySerializer s = new QuerySerializer();
        s.setQuery(simple_add_query, "poliqarp");
        s.setCollection(
                "(corpusID=BRZ13 & corpusID=WPD) & textClass=Wissenschaft");
        String result = s.toJSON();
        JsonNode node = JsonUtils.readTree(handler.apply(result, null));
        assert node != null;
        assert node.at("/collection/@type").asText().equals("koral:doc");
        assert node.at("/collection/key").asText().equals("textClass");
    }

    @Test
    public void testCollectionRemoveAndMoveOneFromGroupUpNoErrors() {
        RewriteHandler handler = new RewriteHandler();
        handler.add(new CollectionConstraint());
        handler.add(new CollectionCleanupFilter());
        QuerySerializer s = new QuerySerializer();
        s.setQuery(simple_add_query, "poliqarp");
        s.setCollection(
                "(docID=random & textClass=Wissenschaft) & corpusID=WPD");
        String result = s.toJSON();
        JsonNode node = JsonUtils.readTree(handler.apply(result, null));
        System.out.println("original node " + result);
        System.out.println("result node " + node);
        assert node != null;
        assert node.at("/collection/@type").asText().equals("koral:docGroup");
        assert node.at("/collection/operands").size() == 2;
    }

}