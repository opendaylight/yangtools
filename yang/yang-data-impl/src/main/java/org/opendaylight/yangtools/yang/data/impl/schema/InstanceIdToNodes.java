/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Iterables;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.AbstractIdentifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.DOMSourceAnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
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
abstract class InstanceIdToNodes<T extends PathArgument> extends AbstractIdentifiable<T> {
    InstanceIdToNodes(final T identifier) {
        super(identifier);
    }

    /**
     * Build a strategy for the next path argument.
     *
     * @param child child identifier
     * @return transformation strategy for a specific child
     */
    abstract InstanceIdToNodes<?> getChild(PathArgument child);

    /**
     * Convert instance identifier into a NormalizedNode structure.
     *
     * @param instanceId Instance identifier to transform into NormalizedNodes
     * @param deepestChild Optional normalized node to be inserted as the last child
     * @param operation Optional modify operation to be set on the last child
     * @return NormalizedNode structure corresponding to submitted instance ID
     */
    abstract @NonNull NormalizedNode<?, ?> create(PathArgument first, Iterator<PathArgument> others,
            Optional<NormalizedNode<?, ?>> deepestChild);

    abstract boolean isMixin();

    private static final class UnkeyedListMixinNormalization extends InstanceIdToCompositeNodes<NodeIdentifier> {
        private final UnkeyedListItemNormalization innerNode;

        UnkeyedListMixinNormalization(final ListSchemaNode list) {
            super(NodeIdentifier.create(list.getQName()));
            this.innerNode = new UnkeyedListItemNormalization(list);
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

    private abstract static class AbstractOpaqueNormalization extends InstanceIdToNodes<NodeIdentifier> {
        AbstractOpaqueNormalization(final DataSchemaNode schema) {
            super(NodeIdentifier.create(schema.getQName()));
        }

        @Override
        final InstanceIdToNodes<?> getChild(final PathArgument child) {
            return null;
        }

        @Override
        final boolean isMixin() {
            return false;
        }
    }

    private static final class AnydataNormalization extends AbstractOpaqueNormalization {
        AnydataNormalization(final AnydataSchemaNode schema) {
            super(schema);
        }

        @Override
        NormalizedNode<?, ?> create(final PathArgument first, final Iterator<PathArgument> others,
                final Optional<NormalizedNode<?, ?>> deepestChild) {
            checkState(deepestChild.isPresent(), "Cannot instantiate anydata node without a value");
            final NormalizedNode<?, ?> child = deepestChild.get();
            checkState(child instanceof AnydataNode, "Invalid child %s", child);
            return createAnydata((AnydataNode<?>) child);
        }

        private <T> AnydataNode<T> createAnydata(final AnydataNode<T> child) {
            return Builders.anydataBuilder(child.getValueObjectModel()).withValue(child.getValue())
            .withNodeIdentifier(getIdentifier()).build();
        }
    }

    private static final class AnyXmlNormalization extends AbstractOpaqueNormalization {
        AnyXmlNormalization(final AnyxmlSchemaNode schema) {
            super(schema);
        }

        @Override
        NormalizedNode<?, ?> create(final PathArgument first, final Iterator<PathArgument> others,
                final Optional<NormalizedNode<?, ?>> deepestChild) {
            final NormalizedNodeBuilder<NodeIdentifier, DOMSource, DOMSourceAnyxmlNode> builder =
                    Builders.anyXmlBuilder()
                    .withNodeIdentifier(getIdentifier());
            if (deepestChild.isPresent()) {
                final NormalizedNode<?, ?> child = deepestChild.get();
                checkState(child instanceof DOMSourceAnyxmlNode, "Invalid child %s", child);
                builder.withValue(((DOMSourceAnyxmlNode) child).getValue());
            }

            return builder.build();
        }
    }

    private static Optional<DataSchemaNode> findChildSchemaNode(final DataNodeContainer parent, final QName child) {
        final Optional<DataSchemaNode> potential = parent.findDataChildByName(child);
        return potential.isPresent() ? potential : Optional.ofNullable(
            findChoice(Iterables.filter(parent.getChildNodes(), ChoiceSchemaNode.class), child));
    }

    static InstanceIdToNodes<?> fromSchemaAndQNameChecked(final DataNodeContainer schema, final QName child) {
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

    private static ChoiceSchemaNode findChoice(final Iterable<ChoiceSchemaNode> choices, final QName child) {
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
    private static InstanceIdToNodes<?> fromAugmentation(final DataNodeContainer parent,
            final AugmentationTarget parentAug, final DataSchemaNode child) {
        for (final AugmentationSchemaNode aug : parentAug.getAvailableAugmentations()) {
            final Optional<DataSchemaNode> potential = aug.findDataChildByName(child.getQName());
            if (potential.isPresent()) {
                return new InstanceIdToCompositeNodes.AugmentationNormalization(aug, parent);
            }
        }
        return fromDataSchemaNode(child);
    }

    static InstanceIdToNodes<?> fromDataSchemaNode(final DataSchemaNode potential) {
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
            return new AnydataNormalization((AnydataSchemaNode) potential);
        } else if (potential instanceof AnyxmlSchemaNode) {
            return new AnyXmlNormalization((AnyxmlSchemaNode) potential);
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
