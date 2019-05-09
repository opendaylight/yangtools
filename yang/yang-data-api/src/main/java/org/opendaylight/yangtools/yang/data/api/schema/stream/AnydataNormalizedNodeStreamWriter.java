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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

/**
 * A {@link NormalizedNodeStreamWriterExtension} supporting streaming of {@code anydata} nodes. As anydata elements are
 * not directly tied to a schema, we cannot infer their structure in the general case -- hence we have to deal with both
 * cases where we can normalize their layout and when we cannot.
 *
 * <p>
 * The problem of normalization is a tricky one, as XML and JSON encodings are different in that XML does not have
 * list encapsulation, whereas JSON does (and requires it). This means that for an {@code anydata} element in XML format
 * we cannot discern containers and single-element lists -- for that we would need know the data schema. JSON does not
 * suffer from this issue, as the encoding of containers and lists is different.
 *
 * <p>
 * We therefore have two modes of operation indicated by the event source  in
 * {@link #startAnydataNode(NodeIdentifier, boolean)}, where it indicates whether the data source can accurately discern
 * between lists and containers -- which is the case if the source is JSON parser or the source has been normalized to
 * disambiguate the two cases.
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
 * For an JSON-like stream, the following snippet would represent a single-node leaf-list encoded in anydata:
 * <pre>
 *     AnydataNormalizedNodeStreamWriter writer;
 *     NodeIdentifier id;
 *     writer.startAnyDataNode(id, true);
 *     NodeIdentifier listId;
 *     writer.startOpaqueList(listId);
 *     writer.startOpaqueContainer(listId, 0);
 *     writer.opaqueValue("foo");
 *     writer.endNode();
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
 * <p>
 * Child nodes of an {@code anydata} node may be formed through normal {@link NormalizedNodeStreamWriter} methods when
 * the internal data has been normalized (which implies the source should have indicated accurate-lists mode), or
 * through {@link #startOpaqueContainer(NodeIdentifier)}, {@link #startOpaqueList(NodeIdentifier)} family of methods.
 *
 * @author Robert Varga
 */
@Beta
public interface AnydataNormalizedNodeStreamWriter extends NormalizedNodeStreamWriterExtension {
    /**
     * Start a new anydata node, identified by a name. It semantically opens the data tree content of the node, hence
     * normal {@link NormalizedNodeStreamWriter} events may be emitted in the scope of this node, just as if
     * {@link NormalizedNodeStreamWriter#startContainerNode(NodeIdentifier, int)} was invoked, except this event does
     * not imply structural kind of the node -- it could either be a semantic container or a semantic list. This is
     * established by the structural event invoked.
     *
     * @param name The name of the anydata element
     * @param accurateLists true if the event source can accurately model list encapsulation
     * @throws IOException if an underlying IO error occurs
     */
    void startAnydataNode(NodeIdentifier name, boolean accurateLists) throws IOException;

    /**
     *
     * @param name The name of the opaque element
     * @param childSizeHint Non-negative count of expected direct child nodes or
     *                      {@link NormalizedNodeStreamWriter#UNKNOWN_SIZE} if count is unknown. This is only hint and
     *                      should not fail writing of child events, if there are more events than count.
     * @throws IOException
     */
    void startOpaqueContainer(NodeIdentifier name, int childSizeHint) throws IOException;

    default void startOpaqueContainer(NodeIdentifier name) throws IOException {
        startOpaqueContainer(name, NormalizedNodeStreamWriter.UNKNOWN_SIZE);
    }

    void startOpaqueList(NodeIdentifier name, int childSizeHint) throws IOException;

    default void startOpaqueList(NodeIdentifier name) throws IOException {
        startOpaqueList(name, NormalizedNodeStreamWriter.UNKNOWN_SIZE);
    }
}
