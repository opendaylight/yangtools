/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.util.EffectiveAugmentationSchema;

/**
 * Schema derived data providing necessary information for mapping
 * between {@link org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode}
 * and serialization format defined in RFC6020, since the mapping
 * is not one-to-one.
 *
 * @param <T> Path Argument type
 *
 */
public abstract class DataSchemaContextNode<T extends PathArgument> implements Identifiable<T> {

    private final T identifier;
    private final DataSchemaNode dataSchemaNode;

    @Override
    public T getIdentifier() {
        return identifier;
    }

    protected DataSchemaContextNode(final T identifier, final SchemaNode schema) {
        super();
        this.identifier = identifier;
        if (schema instanceof DataSchemaNode) {
            this.dataSchemaNode = (DataSchemaNode) schema;
        } else {
            this.dataSchemaNode = null;
        }
    }

    public boolean isMixin() {
        return false;
    }

    public boolean isKeyedEntry() {
        return false;
    }

    protected Set<QName> getQNameIdentifiers() {
        return Collections.singleton(identifier.getNodeType());
    }

    @Nullable public abstract DataSchemaContextNode<?> getChild(PathArgument child);

    @Nullable public abstract DataSchemaContextNode<?> getChild(QName child);

    public abstract boolean isLeaf();


    @Nullable public DataSchemaNode getDataSchemaNode() {
        return dataSchemaNode;
    }

    static DataSchemaNode findChildSchemaNode(final DataNodeContainer parent, final QName child) {
        DataSchemaNode potential = parent.getDataChildByName(child);
        if (potential == null) {
            Iterable<ChoiceSchemaNode> choices = FluentIterable.from(
                    parent.getChildNodes()).filter(ChoiceSchemaNode.class);
            potential = findChoice(choices, child);
        }
        return potential;
    }

    static DataSchemaContextNode<?> fromSchemaAndQNameChecked(final DataNodeContainer schema, final QName child) {
        DataSchemaNode result = findChildSchemaNode(schema, child);
        // We try to look up if this node was added by augmentation
        if (result != null && (schema instanceof DataSchemaNode) && result.isAugmenting()) {
            return fromAugmentation(schema, (AugmentationTarget) schema, result);
        }
        return fromDataSchemaNode(result);
    }

    private static ChoiceSchemaNode findChoice(final Iterable<ChoiceSchemaNode> choices, final QName child) {
        ChoiceSchemaNode foundChoice = null;
        choiceLoop: for (ChoiceSchemaNode choice : choices) {
            for (ChoiceCaseNode caze : choice.getCases()) {
                if (findChildSchemaNode(caze, child) != null) {
                    foundChoice = choice;
                    break choiceLoop;
                }
            }
        }
        return foundChoice;
    }

    public static AugmentationIdentifier augmentationIdentifierFrom(final AugmentationSchema augmentation) {
        ImmutableSet.Builder<QName> potentialChildren = ImmutableSet.builder();
        for (DataSchemaNode child : augmentation.getChildNodes()) {
            potentialChildren.add(child.getQName());
        }
        return new AugmentationIdentifier(potentialChildren.build());
    }

    static DataNodeContainer augmentationProxy(final AugmentationSchema augmentation,
            final DataNodeContainer schema) {
        Set<DataSchemaNode> children = new HashSet<>();
        for (DataSchemaNode augNode : augmentation.getChildNodes()) {
            children.add(schema.getDataChildByName(augNode.getQName()));
        }
        return new EffectiveAugmentationSchema(augmentation, children);
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
    @Nullable static DataSchemaContextNode<?> fromAugmentation(final DataNodeContainer parent,
            final AugmentationTarget parentAug, final DataSchemaNode child) {
        AugmentationSchema augmentation = null;
        for (AugmentationSchema aug : parentAug.getAvailableAugmentations()) {
            DataSchemaNode potential = aug.getDataChildByName(child.getQName());
            if (potential != null) {
                augmentation = aug;
                break;
            }
        }
        if (augmentation != null) {
            return new AugmentationContextNode(augmentation, parent);
        }
        return fromDataSchemaNode(child);
    }

    @Nullable public static DataSchemaContextNode<?> fromDataSchemaNode(final DataSchemaNode potential) {
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
