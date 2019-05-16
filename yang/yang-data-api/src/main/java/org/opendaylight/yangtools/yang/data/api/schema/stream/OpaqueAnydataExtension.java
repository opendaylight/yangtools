/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.ExtensibleObject;
import org.opendaylight.yangtools.concepts.ObjectExtension;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueData;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataContainer;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataList;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataNode;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataValue;

/**
 * A {@link NormalizedNodeStreamWriterExtension} supporting streaming of opaque {@code anydata} nodes. As anydata
 * elements are not directly tied to a schema, we cannot infer their structure in the general case -- hence we have to
 * deal with both cases where we can normalize their layout and when we cannot.
 *
 * <p>
 * The problem of normalization is a tricky one, as XML and JSON encodings are different in that XML does not have
 * list encapsulation, whereas JSON does (and requires it). This means that for an {@code anydata} element in XML format
 * we cannot discern containers and single-element lists -- for that we would need know the data schema. JSON does not
 * suffer from this issue, as the encoding of containers and lists is different.
 *
 * <p>
 * We therefore have two modes of operation indicated by the event source  in
 * {@link #startOpaqueAnydataNode(NodeIdentifier, boolean)}, where it indicates whether the data source can accurately
 * discern between lists and containers -- which is the case if the source is JSON parser or the source has been
 * normalized to disambiguate the two cases.
 *
 * <p>
 * If the source indicates it cannot accurately discern containers and lists, the implementation of this interface can
 * react in any way it chooses -- a non-exhaustive list of actions it can take includes:
 * <ul>
 * <li>recording the inaccuracy and forwarding it</li>
 * <li>ignoring the anydata element completely</li>
 * <li>normalizing the stream with the help of schema, i.e. establishing accuracy</li>
 * <li>rejecting the node</li>
 * </ul>
 *
 * <p>
 * Child nodes of an {@code anydata} node may be formed through
 * {@link StreamWriter#startOpaqueContainer(NodeIdentifier)} and {@link StreamWriter#startOpaqueList(NodeIdentifier)}
 * methods.
 *
 * <p>
 * For an JSON-like stream, the following snippet would represent a single-node leaf-list encoded in anydata:
 * <pre>
 *     AnydataNormalizedNodeStreamWriter writer;
 *     NodeIdentifier id;
 *     NodeIdentifier listId;
 *     StreamWriter stream = writer.startAnyDataNode(id, true);
 *     stream.startOpaqueList(listId);
 *     stream.startOpaqueContainer(listId, 0);
 *     stream.opaqueValue("foo");
 *     stream.endNode();
 *     writer.endNode();
 *     writer.endNode();
 * </pre>
 *
 * <p>
 * For an XML-like stream, the following snippet would represent the same leaf-list:
 * <pre>
 *     AnydataNormalizedNodeStreamWriter writer;
 *     NodeIdentifier id;
 *     writer.startAnyDataNode(id, false);
 *     NodeIdentifier listId;
 *     writer.startOpaqueContainer(listId, 0);
 *     writer.opaqueValue("foo");
 *     writer.endNode();
 *     writer.endNode();
 * </pre>
 * Note how these two streams differ in their structure -- while the JSON has an explicit containment node, the XML does
 * not.
 *
 * <p>
 * Furthermore, in inaccurate-lists mode, the nodes forming a list can be interleaved with other children.
 * Implementations must account for this quirk and operate correctly when faced with it. They are encouraged to perform
 * reorder children to keep such elements together and adding a list encapsulation, but they are by no means required
 * to do so.
 *
 * @author Robert Varga
 */
@Beta
public interface OpaqueAnydataExtension extends NormalizedNodeStreamWriterExtension {
    /**
     * Start emitting a new anydata node identified by name. The content of the node should be set via the returned
     * {@link StreamWriter}.
     *
     * @param name The name of the anydata element
     * @param accurateLists true if the event source can accurately model list encapsulation
     * @return A {@link StreamWriter} which handles the node's interior events
     * @throws IOException if an underlying IO error occurs
     */
    @NonNull StreamWriter startOpaqueAnydataNode(NodeIdentifier name, boolean accurateLists) throws IOException;

    default void streamOpaqueAnydataNode(final NodeIdentifier name, final OpaqueData opaqueData) throws IOException {
        StreamWriter writer = startOpaqueAnydataNode(name, opaqueData.hasAccurateLists());
        writer.streamOpaqueDataNode(opaqueData.getRoot());
    }

    interface StreamWriter extends ExtensibleObject<StreamWriter, StreamWriterExtension> {
        /**
         * Start an opaque container element.
         *
         * @param name The name of the opaque element
         * @param childSizeHint Non-negative count of expected direct child nodes or
         *                      {@link NormalizedNodeStreamWriter#UNKNOWN_SIZE} if count is unknown. This is only hint
         *                      and should not fail writing of child events, if there are more events than count.
         * @throws IOException if an underlying IO error occurs
         */
        void startOpaqueContainer(NodeIdentifier name, int childSizeHint) throws IOException;

        default void startOpaqueContainer(final NodeIdentifier name) throws IOException {
            startOpaqueContainer(name, NormalizedNodeStreamWriter.UNKNOWN_SIZE);
        }

        /**
         * Start an opaque list element.
         *
         * @param name The name of the opaque element
         * @param childSizeHint Non-negative count of expected direct child nodes or
         *                      {@link NormalizedNodeStreamWriter#UNKNOWN_SIZE} if count is unknown. This is only hint
         *                      and should not fail writing of child events, if there are more events than count.
         * @throws IOException if an underlying IO error occurs
         */
        void startOpaqueList(NodeIdentifier name, int childSizeHint) throws IOException;

        default void startOpaqueList(final NodeIdentifier name) throws IOException {
            startOpaqueList(name, NormalizedNodeStreamWriter.UNKNOWN_SIZE);
        }

        /**
         * Emits end event for node.
         *
         * @throws IllegalStateException If there is no start* event to be closed.
         * @throws IOException if an underlying IO error occurs
         */
        void endNode() throws IOException;

        /**
         * Set the value of current node.
         *
         * @param value node value, must be effectively immutable
         * @throws NullPointerException if the argument is null
         * @throws IllegalArgumentException if the argument does not represents a valid value
         * @throws IllegalStateException if a value has already been set or this node has children.
         */
        void opaqueValue(@NonNull Object value) throws IOException;

        default void streamOpaqueDataNode(final OpaqueDataNode node) throws IOException {
            if (node instanceof OpaqueDataValue) {
                startOpaqueContainer(node.getIdentifier(), 0);
                opaqueValue(((OpaqueDataValue) node).getValue());
                endNode();
                return;
            }
            final List<? extends OpaqueDataNode> children;
            if (node instanceof OpaqueDataContainer) {
                children = ((OpaqueDataContainer) node).getChildren();
                startOpaqueContainer(node.getIdentifier(), children.size());
                for (OpaqueDataNode child : children) {
                    streamOpaqueDataNode(child);
                }
            } else if (node instanceof OpaqueDataList) {
                children = ((OpaqueDataList) node).getChildren();
                startOpaqueList(node.getIdentifier(), children.size());
            } else {
                throw new IllegalStateException("Unhandled node " + node);
            }

            for (OpaqueDataNode child : children) {
                streamOpaqueDataNode(child);
            }
            endNode();
        }
    }

    interface StreamWriterExtension extends ObjectExtension<StreamWriter, StreamWriterExtension> {

    }
}
