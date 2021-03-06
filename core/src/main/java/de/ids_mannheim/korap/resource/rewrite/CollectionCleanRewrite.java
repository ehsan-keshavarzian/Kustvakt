package de.ids_mannheim.korap.resource.rewrite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.ids_mannheim.korap.config.BeanInjectable;
import de.ids_mannheim.korap.config.ContextHolder;
import de.ids_mannheim.korap.config.KustvaktConfiguration;
import de.ids_mannheim.korap.user.User;
import de.ids_mannheim.korap.utils.JsonUtils;
import edu.emory.mathcs.backport.java.util.Arrays;

import java.util.Iterator;

/**
 * @author hanl
 * @date 28/07/2015
 */
public class CollectionCleanRewrite implements RewriteTask.RewriteNodeAt {

    @Override
    public JsonNode rewriteQuery (KoralNode node, KustvaktConfiguration config,
            User user) {
        return process(node.rawNode());
    }


    private JsonNode process (JsonNode root) {
        JsonNode sub = root;
        if (root.isObject()) {
            if (root.has("operands")) {
                JsonNode node = root.at("/operands");
                Iterator<JsonNode> it = node.elements();
                while (it.hasNext()) {
                    JsonNode n = it.next();
                    JsonNode s = process(n);
                    if (s == null)
                        it.remove();
                }

                int count = node.size();
                // remove group element and replace with single doc
                if (count == 1)
                    sub = node.path(0);
                // indicate empty group
                else if (count == 0) // can't do anything here -- fixme: edge case?!
                    return null;
            }

            // what happens to array nodes?
            if (!root.equals(sub)) {
                if (sub.isObject()) {
                    ObjectNode ob = (ObjectNode) root;
                    ob.remove(Arrays.asList(new String[] { "@type",
                            "operation", "operands" }));
                    ob.putAll((ObjectNode) sub);
                }
            }
        }
        return root;
    }


    @Override
    public JsonNode rewriteResult (KoralNode node) {
        return null;
    }


    @Override
    public String at () {
        return "/collection";
    }
}
