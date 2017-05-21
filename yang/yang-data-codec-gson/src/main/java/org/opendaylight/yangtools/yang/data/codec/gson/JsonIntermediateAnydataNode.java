package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.annotations.Beta;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.util.Map;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Intermediate representation of an anydata node. This is a result of a parse operation, which may or may not have
 * previously resolved the anydata node completely.
 *
 * <p>
 * This is required for cases when the parsing context is dependent on other leaves in the parsing unit, as the order
 * of nodes is undefined and all nodes may not be known when the node was parsed. the {@link #resolve(Map)} method
 * will be invoked once all other nodes at current level have been parsed and resolved, just before this parsing level
 * is exited.
 *
 * @author Robert Varga
 */
@Beta
@FunctionalInterface
public interface JsonIntermediateAnydataNode {
    /**
     * Perform second-stage parsing of this node. The sibling map contains all provided siblings.
     *
     * @param siblings Map of provided siblings
     * @return Resolved normalized node.
     * @throws IOException if an I/O error occurs
     */
    DataContainerChild<NodeIdentifier, ?> resolve(Map<PathArgument, NormalizedNode<?, ?>> siblings) throws IOException;

    static <T extends JsonAnydataValue<T>> JsonIntermediateAnydataNode forUnknownData(final NodeIdentifier identifier,
            final JsonReader in, final JsonParserStreamMode<T> mode) throws IOException {
        // We need to capture reader state eagerly, hence we may as well create the entire node
        final JsonAnydataNode<T> node = JsonAnydataNode.create(identifier, mode.createAnydataValue(in));
        return siblings -> node;
    }
}
