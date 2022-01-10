/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Iterables;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.AbstractSimpleIdentifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Base strategy for converting an instance identifier into a normalized node structure.
 * Use provided static methods for generic YangInstanceIdentifier -> NormalizedNode translation in ImmutableNodes.
 */
abstract class InstanceIdToNodes<T extends PathArgument> extends AbstractSimpleIdentifiable<T> {
    InstanceIdToNodes(final T identifier) {
        super(identifier);
    }

    /**
     * Build a strategy for the next path argument.
     *
     * @param child child identifier
     * @return transformation strategy for a specific child
     */
    abstract @Nullable InstanceIdToNodes<?> getChild(PathArgument child);

    /**
     * Convert instance identifier into a NormalizedNode structure.
     *
     * @param first First path argument
     * @param others Subsequent path arguments
     * @return NormalizedNode structure corresponding to submitted instance ID
     */
    abstract @NonNull NormalizedNode create(PathArgument first, Iterator<PathArgument> others);

    abstract boolean isMixin();

    private static final class UnkeyedListMixinNormalization extends InstanceIdToCompositeNodes<NodeIdentifier> {
        private final UnkeyedListItemNormalization innerNode;

        UnkeyedListMixinNormalization(final ListSchemaNode list) {
            super(NodeIdentifier.create(list.getQName()));
            innerNode = new UnkeyedListItemNormalization(list);
        }

        @Override
        CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> createBuilder(final PathArgument compositeNode) {
            return Builders.unkeyedListBuilder().withNodeIdentifier(getIdentifier());
        }

        @Override
        InstanceIdToNodes<?> getChild(final PathArgument child) {
            return child.getNodeType().equals(getIdentifier().getNodeType()) ? innerNode : null;
        }

        @Override
        boolean isMixin() {
            return true;
        }
    }

    private static final class OpaqueNormalization extends InstanceIdToNodes<NodeIdentifier> {
        private OpaqueNormalization(final QName qname) {
            super(NodeIdentifier.create(qname));
        }

        OpaqueNormalization(final AnydataSchemaNode schema) {
            this(schema.getQName());
        }

        OpaqueNormalization(final AnyxmlSchemaNode schema) {
            this(schema.getQName());
        }


        @Override InstanceIdToNodes<?> getChild(final PathArgument child) {
            return null;
        }

        @Override boolean isMixin() {
            return false;
        }

        @Override
        NormalizedNode create(final PathArgument first, final Iterator<PathArgument> others) {
            throw new IllegalStateException("Cannot instantiate opaque node without a value");
        }
    }

    private static Optional<DataSchemaNode> findChildSchemaNode(final DataNodeContainer parent, final QName child) {
        final Optional<DataSchemaNode> potential = parent.findDataChildByName(child);
        return potential.isPresent() ? potential : Optional.ofNullable(
            findChoice(Iterables.filter(parent.getChildNodes(), ChoiceSchemaNode.class), child));
    }

    static @Nullable InstanceIdToNodes<?> fromSchemaAndQNameChecked(final DataNodeContainer schema, final QName child) {
        final Optional<DataSchemaNode> potential = findChildSchemaNode(schema, child);
        checkArgument(potential.isPresent(),
                "Supplied QName %s is not valid according to schema %s, potential children nodes: %s", child, schema,
                schema.getChildNodes());

        final DataSchemaNode result = potential.get();
        // We try to look up if this node was added by augmentation
        if (schema instanceof DataSchemaNode && result.isAugmenting()) {
            return fromAugmentation(schema, (AugmentationTarget) schema, result);
        }
        return fromDataSchemaNode(result);
    }

    private static @Nullable ChoiceSchemaNode findChoice(final Iterable<ChoiceSchemaNode> choices, final QName child) {
        for (final ChoiceSchemaNode choice : choices) {
            for (final CaseSchemaNode caze : choice.getCases()) {
                if (findChildSchemaNode(caze, child).isPresent()) {
                    return choice;
                }
            }
        }
        return null;
    }

    /**
     * Returns a SchemaPathUtil for provided child node
     * <p/>
     * If supplied child is added by Augmentation this operation returns
     * a SchemaPathUtil for augmentation,
     * otherwise returns a SchemaPathUtil for child as
     * call for {@link #fromDataSchemaNode(org.opendaylight.yangtools.yang.model.api.DataSchemaNode)}.
     */
    private static @Nullable InstanceIdToNodes<?> fromAugmentation(final DataNodeContainer parent,
            final AugmentationTarget parentAug, final DataSchemaNode child) {
        for (final AugmentationSchemaNode aug : parentAug.getAvailableAugmentations()) {
            final Optional<DataSchemaNode> potential = aug.findDataChildByName(child.getQName());
            if (potential.isPresent()) {
                return new InstanceIdToCompositeNodes.AugmentationNormalization(aug, parent);
            }
        }
        return fromDataSchemaNode(child);
    }

    static @Nullable InstanceIdToNodes<?> fromDataSchemaNode(final DataSchemaNode potential) {
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
        return null;
    }

    private static InstanceIdToNodes<?> fromListSchemaNode(final ListSchemaNode potential) {
        final List<QName> keyDefinition = potential.getKeyDefinition();
        if (keyDefinition == null || keyDefinition.isEmpty()) {
            return new UnkeyedListMixinNormalization(potential);
        }
        return potential.isUserOrdered() ? new InstanceIdToCompositeNodes.OrderedMapMixinNormalization(potential)
                : new InstanceIdToCompositeNodes.UnorderedMapMixinNormalization(potential);
    }

    private static InstanceIdToNodes<?> fromLeafListSchemaNode(final LeafListSchemaNode potential) {
        return potential.isUserOrdered() ? new InstanceIdToCompositeNodes.OrderedLeafListMixinNormalization(potential)
                : new InstanceIdToCompositeNodes.UnorderedLeafListMixinNormalization(potential);
    }
}
