/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.List;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.ImmutableOffsetMap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedAnydata;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Utility for emitting a {@link YangInstanceIdentifier} into a {@link NormalizedNodeStreamWriter} as a set of
 * {@code startXXXNode} events. An example of usage would be something along the lines of:
 * <pre>
 *   <code>
 *       YangModelContext
 *       YangInstanceIdentifier id;
 *       var result = new NormalizedNodeResult();
 *       try (var writer = ImmutableNormalizedNodeStreamWriter.from(result)) {
 *           try (var iidWriter = YangInstanceIdentifierWriter.open(writer, ctx, id)) {
 *               // Here the state of 'writer' reflects the nodes in 'id'
 *           }
 *           // Here the writer is back to its initial state
 *       }
 *
 *       // NormalizedNode result, including the structure created from YangInstanceIdentifier
 *       var node = result.getResult();
 *   </code>
 * </pre>
 */
public final class YangInstanceIdentifierWriter implements AutoCloseable {
    private NormalizedNodeStreamWriter writer;
    private final int endNodeCount;

    private YangInstanceIdentifierWriter(final NormalizedNodeStreamWriter writer, final int endNodeCount) {
        this.writer = requireNonNull(writer);
        this.endNodeCount = endNodeCount;
    }

    /**
     * Open a writer, emitting events in target {@link NormalizedNodeStreamWriter}.
     *
     * @param writer Writer to enter
     * @param root Root container
     * @param path Path to enter
     * @return A writer instance
     * @throws IOException if the path cannot be entered
     */
    public static @NonNull YangInstanceIdentifierWriter open(final NormalizedNodeStreamWriter writer,
            final DataNodeContainer root, final YangInstanceIdentifier path) throws IOException {
        final var it = path.getPathArguments().iterator();
        if (!it.hasNext()) {
            return new YangInstanceIdentifierWriter(writer, 0);
        }

        // State tracking
        int endNodes = 0;
        Object parent = root;
        boolean reuse = false;
        boolean terminal = false;

        do {
            if (terminal) {
                throw new IOException(parent + " is a terminal node, cannot resolve " + ImmutableList.copyOf(it));
            }

            final var arg = it.next();
            if (arg instanceof NodeWithValue<?> nodeId) {
                if (!(parent instanceof LeafListSchemaNode)) {
                    throw new IOException(parent + " does not support leaf-list entry " + arg);
                }
                if (!reuse) {
                    throw new IOException(parent + " is already at its entry, cannot enter " + arg);
                }

                reuse = false;
                terminal = true;
                writer.startLeafSetEntryNode(nodeId);
            } else if (arg instanceof NodeIdentifierWithPredicates nodeId) {
                if (!(parent instanceof ListSchemaNode list)) {
                    throw new IOException(parent + " does not support map entry " + arg);
                }
                if (!reuse) {
                    throw new IOException(parent + " is already at its entry, cannot enter " + arg);
                }
                if (!list.getQName().equals(nodeId.getNodeType())) {
                    throw new IOException(parent + " expects a matching map entry, cannot enter " + arg);
                }

                final var key = list.getKeyDefinition();
                if (key.isEmpty()) {
                    throw new IOException(parent + " does not expect map entry " + arg);
                }
                if (key.size() != nodeId.size()) {
                    throw new IOException(parent + " expects " + key.size() + " predicates, cannot use " + arg);
                }

                reuse = false;
                writer.startMapEntryNode(normalizePredicates(nodeId, key), 1);
            } else if (arg instanceof NodeIdentifier nodeId) {
                if (reuse) {
                    if (!(parent instanceof ListSchemaNode list)) {
                        throw new IOException(parent + " expects an identifiable entry, cannot enter " + arg);
                    }

                    if (!list.getKeyDefinition().isEmpty()) {
                        throw new IOException(parent + " expects a map entry, cannot enter " + arg);
                    }
                    if (!list.getQName().equals(nodeId.getNodeType())) {
                        throw new IOException(parent + " expects a matching entry, cannot enter " + arg);
                    }

                    reuse = false;
                    writer.startUnkeyedListItem(nodeId, 1);
                    endNodes++;
                    continue;
                }

                final DataSchemaNode child;
                if (parent instanceof DataNodeContainer container) {
                    child = container.dataChildByName(nodeId.getNodeType());
                } else if (parent instanceof ChoiceSchemaNode choice) {
                    child = choice.findDataSchemaChild(nodeId.getNodeType()).orElse(null);
                } else {
                    throw new IOException("Unhandled parent " + parent + " when looking up " + arg);
                }

                if (child == null) {
                    throw new IOException("Failed to find child " + arg + " in parent " + parent);
                }

                // FIXME: check & repair augmentations (brr!)

                if (child instanceof ContainerLike) {
                    parent = child;
                    writer.startContainerNode(nodeId, 1);
                } else if (child instanceof ListSchemaNode list) {
                    parent = child;
                    reuse = true;
                    if (list.getKeyDefinition().isEmpty()) {
                        writer.startUnkeyedList(nodeId, 1);
                    } else if (list.isUserOrdered()) {
                        writer.startOrderedMapNode(nodeId, 1);
                    } else {
                        writer.startMapNode(nodeId, 1);
                    }
                } else if (child instanceof LeafSchemaNode) {
                    parent = child;
                    terminal = true;
                    writer.startLeafNode(nodeId);
                } else if (child instanceof ChoiceSchemaNode) {
                    parent = child;
                    writer.startChoiceNode(nodeId, 1);
                } else if (child instanceof LeafListSchemaNode leafList) {
                    parent = child;
                    reuse = true;
                    if (leafList.isUserOrdered()) {
                        writer.startOrderedLeafSet(nodeId, 1);
                    } else {
                        writer.startLeafSet(nodeId, 1);
                    }
                } else if (child instanceof AnydataSchemaNode) {
                    parent = child;
                    terminal = true;
                    writer.startAnydataNode(nodeId, NormalizedAnydata.class);
                } else if (child instanceof AnyxmlSchemaNode) {
                    parent = child;
                    terminal = true;
                    writer.startAnyxmlNode(nodeId, DOMSource.class);
                } else {
                    throw new IOException("Unhandled child " + child);
                }
            } else {
                throw new IOException("Unhandled argument " + arg);
            }

            endNodes++;
        } while (it.hasNext());

        return new YangInstanceIdentifierWriter(writer, endNodes);
    }

    @Override
    public void close() throws IOException {
        if (writer != null) {
            for (int i = 0; i < endNodeCount; ++i) {
                writer.endNode();
            }
            writer = null;
        }
    }

    private static NodeIdentifierWithPredicates normalizePredicates(final NodeIdentifierWithPredicates input,
            final List<QName> key) throws IOException {
        if (Iterables.elementsEqual(input.keySet(), key)) {
            return input;
        }

        final var builder = ImmutableMap.<QName, Object>builderWithExpectedSize(key.size());
        for (var qname : key) {
            final var value = input.getValue(qname);
            if (value == null) {
                throw new IOException("Cannot normalize " + input + " to " + key + ", missing value for " + qname);
            }
            builder.put(qname, value);
        }

        return NodeIdentifierWithPredicates.of(input.getNodeType(), ImmutableOffsetMap.orderedCopyOf(builder.build()));
    }
}
