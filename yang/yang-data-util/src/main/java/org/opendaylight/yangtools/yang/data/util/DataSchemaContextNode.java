/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

public abstract class DataSchemaContextNode<T extends PathArgument> implements Identifiable<T> {

    private final T identifier;
    private final DataSchemaNode dataSchemaNode;

    @Override
    public T getIdentifier() {
        return identifier;
    };

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

    public abstract @Nullable DataSchemaContextNode<?> getChild(final PathArgument child);

    public abstract @Nullable DataSchemaContextNode<?> getChild(QName child);

    public abstract boolean isLeaf();

    public DataSchemaNode getDataSchemaNode() {
        // FIXME
        return dataSchemaNode;
    }

    private static abstract class AbstractLeafNodeContext<T extends PathArgument> extends DataSchemaContextNode<T> {

        protected AbstractLeafNodeContext(final T identifier, final DataSchemaNode potential) {
            super(identifier, potential);
        }

        @Override
        public DataSchemaContextNode<?> getChild(final PathArgument child) {
            return null;
        }

        @Override
        public DataSchemaContextNode<?> getChild(final QName child) {
            return null;
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

    }

    private static final class LeafContextNode extends AbstractLeafNodeContext<NodeIdentifier> {

        protected LeafContextNode(final LeafSchemaNode potential) {
            super(new NodeIdentifier(potential.getQName()), potential);
        }

    }

    private static final class LeafListEntryContextNode extends AbstractLeafNodeContext<NodeWithValue> {

        public LeafListEntryContextNode(final LeafListSchemaNode potential) {
            super(new NodeWithValue(potential.getQName(), null), potential);
        }

        @Override
        public boolean isKeyedEntry() {
            return true;
        }
    }

    private static abstract class InteriorNodeContextNodeOperation<T extends PathArgument> extends
            DataSchemaContextNode<T> {

        protected InteriorNodeContextNodeOperation(final T identifier, final DataSchemaNode schema) {
            super(identifier, schema);
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

    }

    private static class DataContainerContextNode<T extends PathArgument> extends
            InteriorNodeContextNodeOperation<T> {

        private final DataNodeContainer schema;
        private final Map<QName, DataSchemaContextNode<?>> byQName;
        private final Map<PathArgument, DataSchemaContextNode<?>> byArg;

        protected DataContainerContextNode(final T identifier, final DataNodeContainer schema,
                final DataSchemaNode node) {
            super(identifier, node);
            this.schema = schema;
            this.byArg = new ConcurrentHashMap<>();
            this.byQName = new ConcurrentHashMap<>();
        }

        @Override
        public DataSchemaContextNode<?> getChild(final PathArgument child) {
            DataSchemaContextNode<?> potential = byArg.get(child);
            if (potential != null) {
                return potential;
            }
            potential = fromLocalSchema(child);
            return register(potential);
        }

        private DataSchemaContextNode<?> fromLocalSchema(final PathArgument child) {
            if (child instanceof AugmentationIdentifier) {
                return fromSchemaAndQNameChecked(schema, ((AugmentationIdentifier) child).getPossibleChildNames()
                        .iterator().next());
            }
            return fromSchemaAndQNameChecked(schema, child.getNodeType());
        }

        @Override
        public DataSchemaContextNode<?> getChild(final QName child) {
            DataSchemaContextNode<?> potential = byQName.get(child);
            if (potential != null) {
                return potential;
            }
            potential = fromLocalSchemaAndQName(schema, child);
            return register(potential);
        }

        protected DataSchemaContextNode<?> fromLocalSchemaAndQName(final DataNodeContainer schema2, final QName child) {
            return fromSchemaAndQNameChecked(schema2, child);
        }

        private DataSchemaContextNode<?> register(final DataSchemaContextNode<?> potential) {
            if (potential != null) {
                byArg.put(potential.getIdentifier(), potential);
                for (QName qName : potential.getQNameIdentifiers()) {
                    byQName.put(qName, potential);
                }
            }
            return potential;
        }

    }

    private static final class ListItemContextNode extends
            DataContainerContextNode<NodeIdentifierWithPredicates> {


        protected ListItemContextNode(final NodeIdentifierWithPredicates identifier, final ListSchemaNode schema) {
            super(identifier, schema, schema);
        }

        @Override
        public boolean isKeyedEntry() {
            return true;
        }
    }

    private static final class UnkeyedListItemContextNode extends DataContainerContextNode<NodeIdentifier> {

        protected UnkeyedListItemContextNode(final ListSchemaNode schema) {
            super(new NodeIdentifier(schema.getQName()), schema, schema);
        }

    }

    private static final class ContainerContextNode extends DataContainerContextNode<NodeIdentifier> {

        protected ContainerContextNode(final ContainerSchemaNode schema) {
            super(new NodeIdentifier(schema.getQName()), schema, schema);
        }

    }

    private static abstract class MixinContextNodeOp<T extends PathArgument> extends
            InteriorNodeContextNodeOperation<T> {

        protected MixinContextNodeOp(final T identifier, final DataSchemaNode schema) {
            super(identifier, schema);
        }

        @Override
        public final boolean isMixin() {
            return true;
        }

    }

    private static final class OrderedLeafListMixinContextNode extends UnorderedLeafListMixinContextNode {

        public OrderedLeafListMixinContextNode(final LeafListSchemaNode potential) {
            super(potential);
        }
    }

    private static class UnorderedLeafListMixinContextNode extends MixinContextNodeOp<NodeIdentifier> {

        private final DataSchemaContextNode<?> innerOp;

        public UnorderedLeafListMixinContextNode(final LeafListSchemaNode potential) {
            super(new NodeIdentifier(potential.getQName()), potential);
            innerOp = new LeafListEntryContextNode(potential);
        }

        @Override
        public DataSchemaContextNode<?> getChild(final PathArgument child) {
            if (child instanceof NodeWithValue) {
                return innerOp;
            }
            return null;
        }

        @Override
        public DataSchemaContextNode<?> getChild(final QName child) {
            if (getIdentifier().getNodeType().equals(child)) {
                return innerOp;
            }
            return null;
        }
    }

    private static final class AugmentationContextNode extends
            DataContainerContextNode<AugmentationIdentifier> {

        public AugmentationContextNode(final AugmentationSchema augmentation, final DataNodeContainer schema) {
            // super();
            super(augmentationIdentifierFrom(augmentation), augmentationProxy(augmentation, schema), null);
        }

        @Override
        public boolean isMixin() {
            return true;
        }

        @Override
        protected DataSchemaContextNode<?> fromLocalSchemaAndQName(final DataNodeContainer schema, final QName child) {
            DataSchemaNode result = findChildSchemaNode(schema, child);
            // We try to look up if this node was added by augmentation
            if ((schema instanceof DataSchemaNode) && result.isAugmenting()) {
                return fromAugmentation(schema, (AugmentationTarget) schema, result);
            }
            return fromDataSchemaNode(result);
        }

        @Override
        protected Set<QName> getQNameIdentifiers() {
            return getIdentifier().getPossibleChildNames();
        }

    }

    private static class UnorderedMapMixinContextNode extends MixinContextNodeOp<NodeIdentifier> {

        private final ListItemContextNode innerNode;

        public UnorderedMapMixinContextNode(final ListSchemaNode list) {
            super(new NodeIdentifier(list.getQName()), list);
            this.innerNode = new ListItemContextNode(new NodeIdentifierWithPredicates(list.getQName(),
                    Collections.<QName, Object> emptyMap()), list);
        }

        @Override
        public DataSchemaContextNode<?> getChild(final PathArgument child) {
            if (child.getNodeType().equals(getIdentifier().getNodeType())) {
                return innerNode;
            }
            return null;
        }

        @Override
        public DataSchemaContextNode<?> getChild(final QName child) {
            if (getIdentifier().getNodeType().equals(child)) {
                return innerNode;
            }
            return null;
        }

    }

    private static class UnkeyedListMixinContextNode extends MixinContextNodeOp<NodeIdentifier> {

        private final UnkeyedListItemContextNode innerNode;

        public UnkeyedListMixinContextNode(final ListSchemaNode list) {
            super(new NodeIdentifier(list.getQName()), list);
            this.innerNode = new UnkeyedListItemContextNode(list);
        }

        @Override
        public DataSchemaContextNode<?> getChild(final PathArgument child) {
            if (child.getNodeType().equals(getIdentifier().getNodeType())) {
                return innerNode;
            }
            return null;
        }

        @Override
        public DataSchemaContextNode<?> getChild(final QName child) {
            if (getIdentifier().getNodeType().equals(child)) {
                return innerNode;
            }
            return null;
        }

    }

    private static final class OrderedMapMixinContextNode extends UnorderedMapMixinContextNode {

        public OrderedMapMixinContextNode(final ListSchemaNode list) {
            super(list);
        }

    }

    private static class ChoiceNodeContextNode extends MixinContextNodeOp<NodeIdentifier> {

        private final ImmutableMap<QName, DataSchemaContextNode<?>> byQName;
        private final ImmutableMap<PathArgument, DataSchemaContextNode<?>> byArg;

        protected ChoiceNodeContextNode(final org.opendaylight.yangtools.yang.model.api.ChoiceNode schema) {
            super(new NodeIdentifier(schema.getQName()), schema);
            ImmutableMap.Builder<QName, DataSchemaContextNode<?>> byQNameBuilder = ImmutableMap.builder();
            ImmutableMap.Builder<PathArgument, DataSchemaContextNode<?>> byArgBuilder = ImmutableMap.builder();

            for (ChoiceCaseNode caze : schema.getCases()) {
                for (DataSchemaNode cazeChild : caze.getChildNodes()) {
                    DataSchemaContextNode<?> childOp = fromDataSchemaNode(cazeChild);
                    byArgBuilder.put(childOp.getIdentifier(), childOp);
                    for (QName qname : childOp.getQNameIdentifiers()) {
                        byQNameBuilder.put(qname, childOp);
                    }
                }
            }
            byQName = byQNameBuilder.build();
            byArg = byArgBuilder.build();
        }

        @Override
        public DataSchemaContextNode<?> getChild(final PathArgument child) {
            return byArg.get(child);
        }

        @Override
        public DataSchemaContextNode<?> getChild(final QName child) {
            return byQName.get(child);
        }
    }

    private static class AnyXmlContextNode extends DataSchemaContextNode<NodeIdentifier> {

        protected AnyXmlContextNode(final AnyXmlSchemaNode schema) {
            super(new NodeIdentifier(schema.getQName()), schema);
        }

        @Override
        public DataSchemaContextNode<?> getChild(final PathArgument child) {
            return null;
        }

        @Override
        public DataSchemaContextNode<?> getChild(final QName child) {
            return null;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

    }

    private static final DataSchemaNode findChildSchemaNode(final DataNodeContainer parent, final QName child) {
        DataSchemaNode potential = parent.getDataChildByName(child);
        if (potential == null) {
            Iterable<org.opendaylight.yangtools.yang.model.api.ChoiceNode> choices = FluentIterable.from(
                    parent.getChildNodes()).filter(ChoiceNode.class);
            potential = findChoice(choices, child);
        }
        return potential;
    }

    private static DataSchemaContextNode<?> fromSchemaAndQNameChecked(final DataNodeContainer schema, final QName child) {
        DataSchemaNode result = findChildSchemaNode(schema, child);
        // We try to look up if this node was added by augmentation
        if ((schema instanceof DataSchemaNode) && result.isAugmenting()) {
            return fromAugmentation(schema, (AugmentationTarget) schema, result);
        }
        return fromDataSchemaNode(result);
    }

    private static org.opendaylight.yangtools.yang.model.api.ChoiceNode findChoice(
            final Iterable<org.opendaylight.yangtools.yang.model.api.ChoiceNode> choices, final QName child) {
        org.opendaylight.yangtools.yang.model.api.ChoiceNode foundChoice = null;
        choiceLoop: for (org.opendaylight.yangtools.yang.model.api.ChoiceNode choice : choices) {
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

    private static DataNodeContainer augmentationProxy(final AugmentationSchema augmentation,
            final DataNodeContainer schema) {
        Set<DataSchemaNode> children = new HashSet<>();
        for (DataSchemaNode augNode : augmentation.getChildNodes()) {
            children.add(schema.getDataChildByName(augNode.getQName()));
        }
        return null ;//new DataSchemaContainerProxy(children);
    }

    /**
     * Returns a DataContextNodeOperation for provided child node
     *
     * If supplied child is added by Augmentation this operation returns a
     * DataContextNodeOperation for augmentation, otherwise returns a
     * DataContextNodeOperation for child as call for
     * {@link #fromDataSchemaNode(DataSchemaNode)}.
     *
     *
     * @param parent
     * @param parentAug
     * @param child
     * @return
     */
    private static @Nullable DataSchemaContextNode<?> fromAugmentation(final DataNodeContainer parent,
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

    public static @Nullable DataSchemaContextNode<?> fromDataSchemaNode(final DataSchemaNode potential) {
        if (potential instanceof ContainerSchemaNode) {
            return new ContainerContextNode((ContainerSchemaNode) potential);
        } else if (potential instanceof ListSchemaNode) {
            return fromListSchemaNode((ListSchemaNode) potential);
        } else if (potential instanceof LeafSchemaNode) {
            return new LeafContextNode((LeafSchemaNode) potential);
        } else if (potential instanceof org.opendaylight.yangtools.yang.model.api.ChoiceNode) {
            return new ChoiceNodeContextNode((org.opendaylight.yangtools.yang.model.api.ChoiceNode) potential);
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
