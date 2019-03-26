/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

/**
 * Schema derived data providing necessary information for mapping between
 * {@link org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode} and serialization format defined in RFC6020,
 * since the mapping is not one-to-one.
 *
 * @param <T> Path Argument type
 */
public abstract class DataSchemaContextNode<T extends PathArgument> implements Identifiable<T> {
    private final DataSchemaNode dataSchemaNode;
    private final T identifier;

    protected DataSchemaContextNode(final T identifier, final SchemaNode schema) {
        this.identifier = identifier;
        if (schema instanceof DataSchemaNode) {
            this.dataSchemaNode = (DataSchemaNode) schema;
        } else {
            this.dataSchemaNode = null;
        }
    }

    @Override
    public T getIdentifier() {
        return identifier;
    }

    public boolean isMixin() {
        return false;
    }

    public boolean isKeyedEntry() {
        return false;
    }

    public abstract boolean isLeaf();

    protected Set<QName> getQNameIdentifiers() {
        return ImmutableSet.of(identifier.getNodeType());
    }

    /**
     * Find a child node identifier by its {@link PathArgument}.
     *
     * @param child Child path argument
     * @return A child node, or null if not found
     */
    public abstract @Nullable DataSchemaContextNode<?> getChild(PathArgument child);

    public abstract @Nullable DataSchemaContextNode<?> getChild(QName child);

    public @Nullable DataSchemaNode getDataSchemaNode() {
        return dataSchemaNode;
    }

    /**
     * Find a child node as identified by a {@link YangInstanceIdentifier} relative to this node.
     *
     * @param path Path towards the child node
     * @return Child node if present, or empty when corresponding child is not found.
     * @throws NullPointerException if {@code path} is null
     */
    public final @NonNull Optional<@NonNull DataSchemaContextNode<?>> findChild(
            final @NonNull YangInstanceIdentifier path) {
        DataSchemaContextNode<?> currentOp = this;
        for (PathArgument arg : path.getPathArguments()) {
            currentOp = currentOp.getChild(arg);
            if (currentOp == null) {
                return Optional.empty();
            }
        }
        return Optional.of(currentOp);
    }

    static DataSchemaNode findChildSchemaNode(final DataNodeContainer parent, final QName child) {
        final DataSchemaNode potential = parent.getDataChildByName(child);
        return potential == null ? findChoice(Iterables.filter(parent.getChildNodes(), ChoiceSchemaNode.class), child)
                : potential;
    }

    static DataSchemaContextNode<?> fromSchemaAndQNameChecked(final DataNodeContainer schema, final QName child) {
        final DataSchemaNode result = findChildSchemaNode(schema, child);
        // We try to look up if this node was added by augmentation
        if (result != null && schema instanceof DataSchemaNode && result.isAugmenting()) {
            return fromAugmentation(schema, (AugmentationTarget) schema, result);
        }
        return fromDataSchemaNode(result);
    }

    // FIXME: this looks like it should be a Predicate on a stream with findFirst()
    private static ChoiceSchemaNode findChoice(final Iterable<ChoiceSchemaNode> choices, final QName child) {
        for (ChoiceSchemaNode choice : choices) {
            // FIXME: this looks weird: what are we looking for again?
            for (CaseSchemaNode caze : choice.getCases().values()) {
                if (findChildSchemaNode(caze, child) != null) {
                    return choice;
                }
            }
        }
        return null;
    }

    /**
     * Create AugmentationIdentifier from an AugmentationSchemaNode.
     *
     * @param schema Augmentation schema
     * @return AugmentationIdentifier for the schema
     * @throws NullPointerException if {@code schema} is null
     */
    public static AugmentationIdentifier augmentationIdentifierFrom(final AugmentationSchemaNode schema) {
        return new AugmentationIdentifier(schema.getChildNodes().stream().map(DataSchemaNode::getQName)
            .collect(Collectors.toSet()));
    }

    /**
     * Returns a DataContextNodeOperation for provided child node
     *
     * <p>
     * If supplied child is added by Augmentation this operation returns a
     * DataContextNodeOperation for augmentation, otherwise returns a
     * DataContextNodeOperation for child as call for
     * {@link #fromDataSchemaNode(DataSchemaNode)}.
     */
    static @Nullable DataSchemaContextNode<?> fromAugmentation(final DataNodeContainer parent,
            final AugmentationTarget parentAug, final DataSchemaNode child) {
        for (AugmentationSchemaNode aug : parentAug.getAvailableAugmentations()) {
            if (aug.findDataChildByName(child.getQName()).isPresent()) {
                return new AugmentationContextNode(aug, parent);
            }
        }
        return fromDataSchemaNode(child);
    }

    public static @Nullable DataSchemaContextNode<?> fromDataSchemaNode(final DataSchemaNode potential) {
        if (potential instanceof ContainerSchemaNode) {
            return new ContainerContextNode((ContainerSchemaNode) potential);
        } else if (potential instanceof ListSchemaNode) {
            return fromListSchemaNode((ListSchemaNode) potential);
        } else if (potential instanceof LeafSchemaNode) {
            return new LeafContextNode((LeafSchemaNode) potential);
        } else if (potential instanceof ChoiceSchemaNode) {
            return new ChoiceNodeContextNode((ChoiceSchemaNode) potential);
        } else if (potential instanceof LeafListSchemaNode) {
            return fromLeafListSchemaNode((LeafListSchemaNode) potential);
        } else if (potential instanceof AnyXmlSchemaNode) {
            return new AnyXmlContextNode((AnyXmlSchemaNode) potential);
        }
        return null;
    }

    private static DataSchemaContextNode<?> fromListSchemaNode(final ListSchemaNode potential) {
        List<QName> keyDefinition = potential.getKeyDefinition();
        if (keyDefinition == null || keyDefinition.isEmpty()) {
            return new UnkeyedListMixinContextNode(potential);
        }
        if (potential.isUserOrdered()) {
            return new OrderedMapMixinContextNode(potential);
        }
        return new UnorderedMapMixinContextNode(potential);
    }

    private static DataSchemaContextNode<?> fromLeafListSchemaNode(final LeafListSchemaNode potential) {
        if (potential.isUserOrdered()) {
            return new OrderedLeafListMixinContextNode(potential);
        }
        return new UnorderedLeafListMixinContextNode(potential);
    }

    public static DataSchemaContextNode<?> from(final SchemaContext ctx) {
        return new ContainerContextNode(ctx);
    }
}
