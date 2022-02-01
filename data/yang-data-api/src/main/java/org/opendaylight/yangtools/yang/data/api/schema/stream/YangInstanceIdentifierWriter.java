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
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedAnydata;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression;

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

                count++;
                continue;
            } else if (id instanceof NodeIdentifierWithPredicates) {
                final var nodeId = (NodeIdentifierWithPredicates) id;
                writer.startMapEntryNode(nodeId, NormalizedNodeStreamWriter.UNKNOWN_SIZE);

                count++;
                continue;
            } else if (!(id instanceof NodeIdentifier) && !(id instanceof NodeWithValue)) {
                throw new IOException("Unsupported path argument " + id);
            }

            final var qname = id.getNodeType();
            final var child = parent.dataChildByName(qname);
            if (child == null) {
                throw new IOException("Failed to find child " + qname + " in parent " + parent);
            }

            if (child instanceof ContainerLike) {
                writer.startContainerNode((NodeIdentifier) id, NormalizedNodeStreamWriter.UNKNOWN_SIZE);
                parent = (DataNodeContainer) child;
            } else if (child instanceof ListSchemaNode) {
                writer.startMapNode((NodeIdentifier) id, NormalizedNodeStreamWriter.UNKNOWN_SIZE);
                parent = (DataNodeContainer) child;
            } else if (child instanceof LeafSchemaNode) {
                // TODO does it make sense to open this writer at leaf node? we cannot really write the value of the
                // node in any way once were at this point
                writer.startLeafNode((NodeIdentifier) id);
                checkTerminalNode(it, child);
            } else if (child instanceof ChoiceSchemaNode) {
                writer.startChoiceNode((NodeIdentifier) id, NormalizedNodeStreamWriter.UNKNOWN_SIZE);
                parent = new CaseLookupSchemaNode((ChoiceSchemaNode) child, ((ChoiceSchemaNode) child).getCases());
            } else if (child instanceof LeafListSchemaNode) {
                writer.startLeafSet((NodeIdentifier) id, NormalizedNodeStreamWriter.UNKNOWN_SIZE);
                checkTerminalNode(it, child);
            } else if (child instanceof AnydataSchemaNode) {
                // TODO does it make sense to open this writer at this node? once we are in Any** node we cannot really
                // write any internal values of the node
                writer.startAnydataNode((NodeIdentifier) id, NormalizedAnydata.class);
                checkTerminalNode(it, child);
            } else if (child instanceof AnyxmlSchemaNode) {
                // TODO does it make sense to open this writer at this node? once we are in Any** node we cannot really
                // write any internal values of the node
                writer.startAnyxmlNode((NodeIdentifier) id, DOMSource.class);
                checkTerminalNode(it, child);
            }

            if (!it.hasNext()) {
                break;
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

    private static void checkTerminalNode(final Iterator<YangInstanceIdentifier.PathArgument> iterator,
                                          final DataSchemaNode terminalNode) throws IOException {
        if (iterator.hasNext()) {
            throw new IOException("Attempted to enter past terminal node " + terminalNode);
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

    /**
     * Hacky, but we need to act like we are containerLike node while still processing a ChoiceSchemaNode since we don't
     * have identifier of the next node available at the time of creation.
     */
    private static class CaseLookupSchemaNode implements ContainerLike {

        private final ChoiceSchemaNode parent;
        private final Collection<? extends CaseSchemaNode> caseSchemaNodes;

        CaseLookupSchemaNode(final ChoiceSchemaNode parent,
                                    final Collection<? extends CaseSchemaNode> caseSchemaNodes) {
            this.parent = parent;
            this.caseSchemaNodes = caseSchemaNodes;
        }

        @Override
        public @NonNull Collection<? extends @NonNull TypeDefinition<?>> getTypeDefinitions() {
            return null;
        }

        @Override
        public @NonNull Collection<? extends @NonNull DataSchemaNode> getChildNodes() {
            return null;
        }

        @Override
        public @NonNull Collection<? extends @NonNull GroupingDefinition> getGroupings() {
            return null;
        }

        @Override
        public @Nullable DataSchemaNode dataChildByName(QName name) {
            for (final var caseSchemaNode : caseSchemaNodes) {
                final Optional<DataSchemaNode> possibleCase = caseSchemaNode.findDataChildByName(name);
                if (possibleCase.isPresent()) {
                    return possibleCase.get();
                }
            }
            return null;
        }

        @Override
        public @NonNull Collection<? extends @NonNull UsesNode> getUses() {
            return null;
        }

        @Override
        public String toString() {
            return parent.toString();
        }

        @Override
        public @NonNull Collection<? extends @NonNull ActionDefinition> getActions() {
            return null;
        }

        @Override
        public boolean isAddedByUses() {
            return false;
        }

        @Override
        public @NonNull Collection<? extends @NonNull AugmentationSchemaNode> getAvailableAugmentations() {
            return null;
        }

        @Override
        public boolean isAugmenting() {
            return false;
        }

        @Override
        public Optional<Boolean> effectiveConfig() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getDescription() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getReference() {
            return Optional.empty();
        }

        @Override
        public @NonNull Status getStatus() {
            return null;
        }

        @Override
        public Collection<? extends @NonNull MustDefinition> getMustConstraints() {
            return null;
        }

        @Override
        public @NonNull Collection<? extends @NonNull NotificationDefinition> getNotifications() {
            return null;
        }

        @Override
        public @NonNull QName getQName() {
            return null;
        }

        @Override
        public Optional<? extends YangXPathExpression.QualifiedBound> getWhenCondition() {
            return Optional.empty();
        }
    }
}
