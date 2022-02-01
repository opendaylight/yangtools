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
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Iterator;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedAnydata;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Utility for emitting a {@link YangInstanceIdentifier} into a {@link NormalizedNodeStreamWriter} as a set of
 * {@code startXXXNode} events.
 */
public final class YangInstanceIdentifierWriter implements AutoCloseable {
    private NormalizedNodeStreamWriter writer;
    private final int endNodeCount;

    private YangInstanceIdentifierWriter(final NormalizedNodeStreamWriter writer, final int endNodeCount) {
        this.writer = requireNonNull(writer);
        this.endNodeCount = endNodeCount;
    }

    public static @NonNull YangInstanceIdentifierWriter open(final NormalizedNodeStreamWriter writer,
            final DataNodeContainer root, final YangInstanceIdentifier path) throws IOException {

        final var it = path.getPathArguments().iterator();
        if (!it.hasNext()) {
            return new YangInstanceIdentifierWriter(writer, 0);
        }

        int count = 0;
        Object parent = root;
        do {
            final var id = it.next();
            if (id instanceof AugmentationIdentifier) {
                final var nodeId = (AugmentationIdentifier) id;
                writer.startAugmentationNode(nodeId);
                parent = enterAugmentation(parent, nodeId);

                count++;
                continue;
            } else if (id instanceof NodeIdentifierWithPredicates) {
                final var nodeId = (NodeIdentifierWithPredicates) id;
                writer.startMapEntryNode(nodeId, 1);

                count++;
                continue;
            } else if (!(id instanceof NodeIdentifier) && !(id instanceof NodeWithValue)) {
                throw new IOException("Unsupported path argument " + id);
            }

            final var qname = id.getNodeType();
            final DataSchemaNode child;
            if (parent instanceof DataNodeContainer) {
                child = ((DataNodeContainer) parent).dataChildByName(qname);
            } else if (parent instanceof ChoiceSchemaNode) {
                child = ((ChoiceSchemaNode) parent).findDataSchemaChild(qname).orElse(null);
            } else {
                throw new IOException("Unhandled parent " + parent + " when looking up " + qname);
            }

            if (child == null) {
                throw new IOException("Failed to find child " + qname + " in parent " + parent);
            }

            if (child instanceof ContainerLike) {
                writer.startContainerNode((NodeIdentifier) id, 1);
                parent = child;
            } else if (child instanceof ListSchemaNode) {
                final var list = (ListSchemaNode) child;
                final var nodeId = (NodeIdentifier) id;
                if (list.getKeyDefinition().isEmpty()) {
                    writer.startUnkeyedList(nodeId, 1);
                } else if (list.isUserOrdered()) {
                    writer.startOrderedMapNode(nodeId, 1);
                } else {
                    writer.startMapNode(nodeId, 1);
                }
                parent = child;
            } else if (child instanceof LeafSchemaNode) {
                writer.startLeafNode((NodeIdentifier) id);
                checkTerminalNode(it, child);
            } else if (child instanceof ChoiceSchemaNode) {
                writer.startChoiceNode((NodeIdentifier) id, 1);
                parent = child;
            } else if (child instanceof LeafListSchemaNode) {
                if (((LeafListSchemaNode) child).isUserOrdered()) {
                    writer.startOrderedLeafSet((NodeIdentifier) id, 1);
                } else {
                    writer.startLeafSet((NodeIdentifier) id, 1);
                }

                // FIXME: not really terminal, we we need to deal with that separately
                checkTerminalNode(it, child);
            } else if (child instanceof AnydataSchemaNode) {
                writer.startAnydataNode((NodeIdentifier) id, NormalizedAnydata.class);
                checkTerminalNode(it, child);
            } else if (child instanceof AnyxmlSchemaNode) {
                writer.startAnyxmlNode((NodeIdentifier) id, DOMSource.class);
                checkTerminalNode(it, child);
            }

            count++;
        } while (it.hasNext());

        return new YangInstanceIdentifierWriter(writer, count);
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

    private static void checkTerminalNode(final Iterator<?> iterator, final DataSchemaNode terminalNode)
            throws IOException {
        if (iterator.hasNext()) {
            throw new IOException("Attempted to enter past terminal node " + terminalNode + " with "
                + ImmutableList.copyOf(iterator));
        }
    }

    private static AugmentationSchemaNode enterAugmentation(final Object parent, final AugmentationIdentifier id)
            throws IOException {
        if (parent instanceof AugmentationTarget) {
            for (var augment : ((AugmentationTarget) parent).getAvailableAugmentations()) {
                if (id.equals(augmentationIdentifierFrom(augment))) {
                    return augment;
                }
            }
        }
        throw new IOException("Cannot find augmentation " + id + " in parent " + parent);
    }

    // FIXME: duplicate of data.util.DataSchemaContextNode.augmentationIdentifierFrom()
    static @NonNull AugmentationIdentifier augmentationIdentifierFrom(final AugmentationSchemaNode schema) {
        return new AugmentationIdentifier(
            schema.getChildNodes().stream().map(DataSchemaNode::getQName).collect(ImmutableSet.toImmutableSet()));
    }
}
