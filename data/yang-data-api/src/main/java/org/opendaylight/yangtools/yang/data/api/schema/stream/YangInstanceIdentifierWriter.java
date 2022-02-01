/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.impl.schema.InstanceIdToCompositeNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.InstanceIdToNodes.OpaqueNormalization;
import org.opendaylight.yangtools.yang.data.impl.schema.InstanceIdToSimpleNodes;
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

        int count = 1;
        var parent = root;
        while (true) {
            final var id = it.next();
            if (id instanceof AugmentationIdentifier) {
                final var nodeId = (AugmentationIdentifier) id;
                writer.startAugmentationNode(nodeId);
                parent = enterAugmentation(parent, nodeId);
            } else if (id instanceof NodeIdentifier) {
                final var nodeId = (NodeIdentifier) id;





            } else if (id instanceof NodeIdentifierWithPredicates) {
                final var nodeId = (NodeIdentifierWithPredicates) id;

            } else if (id instanceof NodeWithValue) {
                final var nodeId = (NodeWithValue<?>) id;



            } else {
                throw new IOException("Unsupported path argument " + id);
            }

            final var qname = id.getNodeType();
            final var child = parent.dataChildByName(qname);
            if (child == null) {
                throw new IOException("Failed to find child " + qname + " in parent " + parent);
            }

            if (potential instanceof ContainerLike) {
                return new InstanceIdToCompositeNodes.ContainerTransformation((ContainerLike) potential);
            } else if (potential instanceof ListSchemaNode) {
                return fromListSchemaNode((ListSchemaNode) potential);
            } else if (potential instanceof LeafSchemaNode) {
                return new InstanceIdToSimpleNodes.LeafNormalization((LeafSchemaNode) potential);
            } else if (potential instanceof ChoiceSchemaNode) {
                return new InstanceIdToCompositeNodes.ChoiceNodeNormalization((ChoiceSchemaNode) potential);
            } else if (potential instanceof LeafListSchemaNode) {
                return fromLeafListSchemaNode((LeafListSchemaNode) potential);
            } else if (potential instanceof AnydataSchemaNode) {
                return new OpaqueNormalization((AnydataSchemaNode) potential);
            } else if (potential instanceof AnyxmlSchemaNode) {
                return new OpaqueNormalization((AnyxmlSchemaNode) potential);
            }



            // FIXME: finish this up: dispatch on the various nodes
            // FIXME: note that leaf-list is not a NNContainer and has entries :()


            if (!it.hasNext()) {
                break;
            }

            if (!(child instanceof DataNodeContainer)) {
                throw new IOException("Attempted to enter past terminal node " + child);
            }
            count++;
        }

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

    private static AugmentationSchemaNode enterAugmentation(final DataNodeContainer parent,
            final AugmentationIdentifier id) throws IOException {
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
            schema.getChildNodes().stream().map(DataSchemaNode::getQName).collect(Collectors.toSet()));
    }
}
