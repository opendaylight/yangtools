package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.annotations.Beta;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.util.Map;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode;

/**
 * Plugin support for anydata nodes. An implementation of this interface needs to be provided for resolution of anydata
 * nodes.
 *
 * @author Robert Varga
 */
@Beta
@FunctionalInterface
public interface JsonAnydataSupport {
    /**
     * Parse an anydata node.
     *
     * @param schema Schema node
     * @param siblings Sibling nodes known at this point
     * @param in Input stream
     * @param capture Capture factory
     * @return An intermediate anydata node representation
     * @throws IOException
     */
    JsonIntermediateAnydataNode parseAnydata(AnyDataSchemaNode schema, Map<PathArgument, NormalizedNode<?, ?>> siblings,
            JsonReader in, JsonParserStreamMode<?> mode) throws IOException;

    static JsonAnydataSupport forUnknownData() {
        return (schema, siblings, in, mode) ->
            JsonIntermediateAnydataNode.forUnknownData(NodeIdentifier.create(schema.getQName()), in, mode);
    }
}
