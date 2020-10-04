/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.ImmutableOffsetMap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.util.EffectiveAugmentationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base strategy for converting an instance identifier into a normalized node structure for container-like types.
 */
abstract class InstanceIdToCompositeNodes<T extends PathArgument> extends InstanceIdToNodes<T> {
    private static final Logger LOG = LoggerFactory.getLogger(InstanceIdToCompositeNodes.class);

    InstanceIdToCompositeNodes(final T identifier) {
        super(identifier);
    }

    @Override
    @SuppressWarnings("unchecked")
    final NormalizedNode<?, ?> create(final PathArgument first, final Iterator<PathArgument> others,
            final Optional<NormalizedNode<?, ?>> lastChild) {
        if (!isMixin()) {
            final QName type = getIdentifier().getNodeType();
            if (type != null) {
                final QName firstType = first.getNodeType();
                checkArgument(type.equals(firstType), "Node QName must be %s was %s", type, firstType);
            }
        }

        @SuppressWarnings("rawtypes")
        final NormalizedNodeContainerBuilder builder = createBuilder(first);

        if (others.hasNext()) {
            final PathArgument childPath = others.next();
            final InstanceIdToNodes<?> childOp = getChildOperation(childPath);
            builder.addChild(childOp.create(childPath, others, lastChild));
        } else if (lastChild.isPresent()) {
            builder.withValue(ImmutableList.copyOf((Collection<?>) lastChild.get().getValue()));
        }

        return builder.build();
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private InstanceIdToNodes<?> getChildOperation(final PathArgument childPath) {
        final InstanceIdToNodes<?> childOp;
        try {
            childOp = getChild(childPath);
        } catch (final RuntimeException e) {
            throw new IllegalArgumentException(String.format("Failed to process child node %s", childPath), e);
        }
        checkArgument(childOp != null, "Node %s is not allowed inside %s", childPath, getIdentifier());
        return childOp;
    }

    abstract NormalizedNodeContainerBuilder<?, ?, ?, ?> createBuilder(PathArgument compositeNode);

    abstract static class DataContainerNormalizationOperation<T extends PathArgument, S extends DataNodeContainer>
            extends InstanceIdToCompositeNodes<T> {

        private final Map<PathArgument, InstanceIdToNodes<?>> byArg = new ConcurrentHashMap<>();
        private final @NonNull S schema;

        DataContainerNormalizationOperation(final T identifier, final S schema) {
            super(identifier);
            this.schema = requireNonNull(schema);
        }

        @Override
        final InstanceIdToNodes<?> getChild(final PathArgument child) {
            final InstanceIdToNodes<?> existing = byArg.get(child);
            if (existing != null) {
                return existing;
            }
            return register(fromLocalSchema(child));
        }

        final @NonNull S schema() {
            return schema;
        }

        private InstanceIdToNodes<?> fromLocalSchema(final PathArgument child) {
            if (child instanceof AugmentationIdentifier) {
                return fromSchemaAndQNameChecked(schema, ((AugmentationIdentifier) child).getPossibleChildNames()
                        .iterator().next());
            }
            return fromSchemaAndQNameChecked(schema, child.getNodeType());
        }

        private InstanceIdToNodes<?> register(final InstanceIdToNodes<?> potential) {
            if (potential != null) {
                byArg.put(potential.getIdentifier(), potential);
            }
            return potential;
        }
    }

    static final class MapEntryNormalization
            extends DataContainerNormalizationOperation<NodeIdentifierWithPredicates, ListSchemaNode> {
        MapEntryNormalization(final ListSchemaNode schema) {
            super(NodeIdentifierWithPredicates.of(schema.getQName()), schema);
        }

        @Override
        boolean isMixin() {
            return false;
        }

        @Override
        DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> createBuilder(
                final PathArgument currentArg) {
            final NodeIdentifierWithPredicates arg = (NodeIdentifierWithPredicates) currentArg;
            return createBuilder(arg.size() < 2 ? arg : reorderPredicates(schema().getKeyDefinition(), arg));
        }

        private static DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> createBuilder(
                final NodeIdentifierWithPredicates arg) {
            final DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> builder = Builders
                    .mapEntryBuilder().withNodeIdentifier(arg);
            for (final Entry<QName, Object> keyValue : arg.entrySet()) {
                builder.addChild(Builders.leafBuilder()
                        .withNodeIdentifier(NodeIdentifier.create(keyValue.getKey())).withValue(keyValue.getValue())
                        .build());
            }
            return builder;
        }

        private static NodeIdentifierWithPredicates reorderPredicates(final List<QName> keys,
                final NodeIdentifierWithPredicates arg) {
            if (Iterables.elementsEqual(keys, arg.keySet())) {
                // Iteration order matches key order, reuse the identifier
                return arg;
            }

            // We care about iteration order here!
            final LinkedHashMap<QName, Object> map = Maps.newLinkedHashMapWithExpectedSize(arg.size());
            for (QName qname : keys) {
                final Object value = arg.getValue(qname);
                if (value != null) {
                    map.put(qname, value);
                }
            }
            if (map.size() < arg.size()) {
                // Okay, this should not happen, but let's handle that anyway
                LOG.debug("Extra predicates in {} while expecting {}", arg, keys);
                for (Entry<QName, Object> entry : arg.entrySet()) {
                    map.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }

            // This copy retains iteration order and since we have more than one argument, it should always be
            // and ImmutableOffsetMap -- which is guaranteed to be taken as-is
            final Map<QName, Object> copy = ImmutableOffsetMap.orderedCopyOf(map);
            verify(copy instanceof ImmutableOffsetMap);
            return NodeIdentifierWithPredicates.of(arg.getNodeType(), (ImmutableOffsetMap<QName, Object>) copy);
        }
    }

    static final class UnkeyedListItemNormalization
            extends DataContainerNormalizationOperation<NodeIdentifier, ListSchemaNode> {
        UnkeyedListItemNormalization(final ListSchemaNode schema) {
            super(NodeIdentifier.create(schema.getQName()), schema);
        }

        @Override
        DataContainerNodeBuilder<NodeIdentifier, UnkeyedListEntryNode> createBuilder(
                final PathArgument compositeNode) {
            return Builders.unkeyedListEntryBuilder().withNodeIdentifier(getIdentifier());
        }

        @Override
        boolean isMixin() {
            return false;
        }
    }

    static final class ContainerTransformation
            extends DataContainerNormalizationOperation<NodeIdentifier, ContainerLike> {
        ContainerTransformation(final ContainerLike schema) {
            super(NodeIdentifier.create(schema.getQName()), schema);
        }

        @Override
        DataContainerNodeBuilder<NodeIdentifier, ContainerNode> createBuilder(final PathArgument compositeNode) {
            return Builders.containerBuilder().withNodeIdentifier(getIdentifier());
        }

        @Override
        boolean isMixin() {
            return false;
        }
    }

    static final class OrderedLeafListMixinNormalization extends UnorderedLeafListMixinNormalization {
        OrderedLeafListMixinNormalization(final LeafListSchemaNode potential) {
            super(potential);
        }

        @Override
        ListNodeBuilder<?, ?> createBuilder(final PathArgument compositeNode) {
            return Builders.orderedLeafSetBuilder().withNodeIdentifier(getIdentifier());
        }
    }

    static class UnorderedLeafListMixinNormalization extends InstanceIdToCompositeNodes<NodeIdentifier> {
        private final InstanceIdToNodes<?> innerOp;

        UnorderedLeafListMixinNormalization(final LeafListSchemaNode potential) {
            super(NodeIdentifier.create(potential.getQName()));
            innerOp = new InstanceIdToSimpleNodes.LeafListEntryNormalization(potential);
        }

        @Override
        ListNodeBuilder<?, ?> createBuilder(final PathArgument compositeNode) {
            return Builders.leafSetBuilder().withNodeIdentifier(getIdentifier());
        }

        @Override
        final InstanceIdToNodes<?> getChild(final PathArgument child) {
            return child instanceof NodeWithValue ? innerOp : null;
        }

        @Override
        final boolean isMixin() {
            return true;
        }
    }

    static final class AugmentationNormalization
            extends DataContainerNormalizationOperation<AugmentationIdentifier, AugmentationSchemaNode> {
        AugmentationNormalization(final AugmentationSchemaNode augmentation, final DataNodeContainer schema) {
            super(DataSchemaContextNode.augmentationIdentifierFrom(augmentation),
                    EffectiveAugmentationSchema.create(augmentation, schema));
        }

        @Override
        DataContainerNodeBuilder<AugmentationIdentifier, AugmentationNode> createBuilder(
                final PathArgument compositeNode) {
            return Builders.augmentationBuilder().withNodeIdentifier(getIdentifier());
        }

        @Override
        boolean isMixin() {
            return true;
        }
    }

    static class UnorderedMapMixinNormalization extends InstanceIdToCompositeNodes<NodeIdentifier> {
        private final MapEntryNormalization innerNode;

        UnorderedMapMixinNormalization(final ListSchemaNode list) {
            super(NodeIdentifier.create(list.getQName()));
            this.innerNode = new MapEntryNormalization(list);
        }

        @Override
        CollectionNodeBuilder<MapEntryNode, ? extends MapNode> createBuilder(final PathArgument compositeNode) {
            return Builders.mapBuilder().withNodeIdentifier(getIdentifier());
        }

        @Override
        final InstanceIdToNodes<?> getChild(final PathArgument child) {
            return child.getNodeType().equals(getIdentifier().getNodeType()) ? innerNode : null;
        }

        @Override
        final boolean isMixin() {
            return true;
        }
    }

    static final class OrderedMapMixinNormalization extends UnorderedMapMixinNormalization {
        OrderedMapMixinNormalization(final ListSchemaNode list) {
            super(list);
        }

        @Override
        CollectionNodeBuilder<MapEntryNode, OrderedMapNode> createBuilder(final PathArgument compositeNode) {
            return Builders.orderedMapBuilder().withNodeIdentifier(getIdentifier());
        }
    }

    static final class ChoiceNodeNormalization extends InstanceIdToCompositeNodes<NodeIdentifier> {
        private final ImmutableMap<PathArgument, InstanceIdToNodes<?>> byArg;

        ChoiceNodeNormalization(final ChoiceSchemaNode schema) {
            super(NodeIdentifier.create(schema.getQName()));
            final ImmutableMap.Builder<PathArgument, InstanceIdToNodes<?>> byArgBuilder = ImmutableMap.builder();

            for (final CaseSchemaNode caze : schema.getCases()) {
                for (final DataSchemaNode cazeChild : caze.getChildNodes()) {
                    final InstanceIdToNodes<?> childOp = fromDataSchemaNode(cazeChild);
                    byArgBuilder.put(childOp.getIdentifier(), childOp);
                }
            }
            byArg = byArgBuilder.build();
        }

        @Override
        InstanceIdToNodes<?> getChild(final PathArgument child) {
            return byArg.get(child);
        }

        @Override
        DataContainerNodeBuilder<NodeIdentifier, ChoiceNode> createBuilder(final PathArgument compositeNode) {
            return Builders.choiceBuilder().withNodeIdentifier(getIdentifier());
        }

        @Override
        boolean isMixin() {
            return true;
        }
    }
}
