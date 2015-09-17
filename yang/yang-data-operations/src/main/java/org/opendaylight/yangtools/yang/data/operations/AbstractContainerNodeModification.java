/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.operations;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

abstract class AbstractContainerNodeModification<S, N extends DataContainerNode<?>> implements Modification<S, N> {

    @Override
    public final Optional<N> modify(final S schema, final Optional<N> actual, final Optional<N> modification,
            final OperationStack operationStack) throws DataModificationException {

        operationStack.enteringNode(modification);

        Optional<N> result;
        QName nodeQName = getQName(schema);

        switch (operationStack.getCurrentOperation()) {
        case DELETE: {
            DataModificationException.DataMissingException.check(nodeQName, actual);
        }
        case REMOVE: {
            result = Optional.absent();
            break;
        }
        case CREATE: {
            DataModificationException.DataExistsException.check(nodeQName, actual, null);
        }
        case REPLACE: {
            result = modification;
            break;
        }
        case NONE: {
            DataModificationException.DataMissingException.check(nodeQName, actual);
        }
        case MERGE: {
            // Recursively modify all child nodes
            result = modifyContainer(schema, actual, modification, operationStack);
            break;
        }
        default:
            throw new UnsupportedOperationException(String.format("Unable to perform operation: %s on: %s, unknown",
                    operationStack.getCurrentOperation(), schema));
        }

        operationStack.exitingNode(modification);
        return result;
    }

    protected abstract QName getQName(S schema);

    private Optional<N> modifyContainer(final S schema, final Optional<N> actual, final Optional<N> modification,
            final OperationStack operationStack) throws DataModificationException {

        if (!actual.isPresent()) {
            return modification;
        }

        if (!modification.isPresent()) {
            return actual;
        }

        Set<YangInstanceIdentifier.PathArgument> toProcess = getChildrenToProcess(schema, actual, modification);

        List<? extends DataContainerChild<?, ?>> merged = modifyContainerChildNodes(schema, operationStack,
                actual.get(), modification.get(), toProcess);
        return build(schema, merged);
    }

    private List<? extends DataContainerChild<?, ?>> modifyContainerChildNodes(final S schema, final OperationStack operationStack,
            final N actual, final N modification, final Set<YangInstanceIdentifier.PathArgument> toProcess) throws DataModificationException {
        List<DataContainerChild<?, ?>> result = Lists.newArrayList();

        for (YangInstanceIdentifier.PathArgument childToProcessId : toProcess) {
            Object schemaOfChildToProcess = findSchema(schema, childToProcessId);

            Optional<? extends DataContainerChild<?, ?>> modifiedValues = modifyContainerNode(operationStack, actual,
                    modification, childToProcessId, schemaOfChildToProcess);

            if (modifiedValues.isPresent()) {
                result.add(modifiedValues.get());
            }
        }

        return result;
    }

    private Optional<? extends DataContainerChild<?, ?>> modifyContainerNode(final OperationStack operationStack, final N actual,
            final N modification, final YangInstanceIdentifier.PathArgument childToProcess, final Object schemaChild)
            throws DataModificationException {

        Optional<DataContainerChild<?, ?>> storedChildren = actual.getChild(childToProcess);
        Optional<DataContainerChild<?, ?>> modifiedChildren = modification.getChild(childToProcess);

        return NodeDispatcher.dispatchChildModification(schemaChild, storedChildren, modifiedChildren, operationStack);
    }

    private Object findSchema(final S schema, final YangInstanceIdentifier.PathArgument childToProcessId) {
        if (childToProcessId instanceof YangInstanceIdentifier.AugmentationIdentifier) {
            return findSchemaForAugment(schema, (YangInstanceIdentifier.AugmentationIdentifier) childToProcessId);
        } else {
            return findSchemaForChild(schema, childToProcessId.getNodeType());
        }
    }

    protected abstract Object findSchemaForChild(S schema, QName nodeType);

    protected abstract Object findSchemaForAugment(S schema, YangInstanceIdentifier.AugmentationIdentifier childToProcessId);

    private Optional<N> build(final S schema, final List<? extends DataContainerChild<?, ?>> result) {
        DataContainerNodeBuilder<?, N> b = getBuilder(schema);

        // TODO skip empty container nodes ? e.g. if container looses all its child nodes
//        if(result.isEmpty()) {
//            return Optional.absent();
//        }

        for (DataContainerChild<?, ?> dataContainerChild : result) {
            b.withChild(dataContainerChild);
        }
        return Optional.of(b.build());
    }

    protected abstract DataContainerNodeBuilder<?, N> getBuilder(S schema);

    protected Set<YangInstanceIdentifier.PathArgument> getChildrenToProcess(final S schema, final Optional<N> actual,
            final Optional<N> modification) throws DataModificationException {
        Set<YangInstanceIdentifier.PathArgument> qNames = Sets.newLinkedHashSet();

        qNames.addAll(getChildQNames(actual));
        qNames.addAll(getChildQNames(modification));
        return qNames;
    }

    private Set<? extends YangInstanceIdentifier.PathArgument> getChildQNames(final Optional<N> actual) {
        Set<YangInstanceIdentifier.PathArgument> qNames = Sets.newLinkedHashSet();

        for (DataContainerChild<? extends YangInstanceIdentifier.PathArgument, ?> child : actual.get().getValue()) {
            qNames.add(child.getIdentifier());
        }

        return qNames;
    }

    private static final class NodeDispatcher {

        private static final LeafNodeModification LEAF_NODE_MODIFICATION = new LeafNodeModification();
        private static final LeafSetNodeModification LEAF_SET_NODE_MODIFICATION = new LeafSetNodeModification();
        private static final AugmentationNodeModification AUGMENTATION_NODE_MODIFICATION = new AugmentationNodeModification();
        private static final MapNodeModification MAP_NODE_MODIFICATION = new MapNodeModification();
        private static final UnkeyedListNodeModification UNKEYED_LIST_NODE_MODIFICATION = new UnkeyedListNodeModification();
        private static final ContainerNodeModification CONTAINER_NODE_MODIFICATION = new ContainerNodeModification();
        private static final ChoiceNodeModification CHOICE_NODE_MODIFICATION = new ChoiceNodeModification();

        private NodeDispatcher() {
            throw new UnsupportedOperationException("Utility class should not be instantiated!");
        }

        static Optional<? extends DataContainerChild<?, ?>> dispatchChildModification(final Object schemaChild,
                final Optional<DataContainerChild<?, ?>> actual, final Optional<DataContainerChild<?, ?>> modification,
                final OperationStack operations) throws DataModificationException {

            if (schemaChild instanceof LeafSchemaNode) {
                return onLeafNode((LeafSchemaNode) schemaChild, actual, modification, operations);
            } else if (schemaChild instanceof ContainerSchemaNode) {
                return onContainerNode((ContainerSchemaNode) schemaChild, actual, modification, operations);
            } else if (schemaChild instanceof LeafListSchemaNode) {
                return onLeafSetNode((LeafListSchemaNode) schemaChild, actual, modification, operations);
            } else if (schemaChild instanceof AugmentationSchema) {
                return onAugmentationNode((AugmentationSchema) schemaChild, actual, modification, operations);
            } else if (schemaChild instanceof ListSchemaNode) {
                if (((ListSchemaNode)schemaChild).getKeyDefinition().isEmpty()) {
                    return onUnkeyedNode((ListSchemaNode) schemaChild, actual, modification, operations);
                } else {
                    return onMapNode((ListSchemaNode) schemaChild, actual, modification, operations);
                }
            } else if (schemaChild instanceof ChoiceSchemaNode) {
                return onChoiceNode((ChoiceSchemaNode) schemaChild, actual,
                        modification, operations);
            }

            throw new IllegalArgumentException("Unknown schema node type " + schemaChild);
        }

        private static Optional<? extends DataContainerChild<?, ?>> onChoiceNode(
            final ChoiceSchemaNode schemaChild,
                final Optional<? extends DataContainerChild<?, ?>> actual,
                final Optional<? extends DataContainerChild<?, ?>> modification, final OperationStack operations)
                throws DataModificationException {
            checkType(actual, ChoiceNode.class);
            checkType(modification, ChoiceNode.class);
            return CHOICE_NODE_MODIFICATION.modify(schemaChild, (Optional<ChoiceNode>) actual,
                    (Optional<ChoiceNode>) modification, operations);
        }

        private static Optional<? extends DataContainerChild<?, ?>> onMapNode(final ListSchemaNode schemaChild,
                final Optional<? extends DataContainerChild<?, ?>> actual,
                final Optional<? extends DataContainerChild<?, ?>> modification, final OperationStack operations)
                throws DataModificationException {
            checkType(actual, MapNode.class);
            checkType(modification, MapNode.class);
            return MAP_NODE_MODIFICATION.modify(schemaChild, (Optional<MapNode>) actual,
                    (Optional<MapNode>) modification, operations);
        }

        private static Optional<? extends DataContainerChild<?, ?>> onUnkeyedNode(final ListSchemaNode schemaChild,
                final Optional<? extends DataContainerChild<?, ?>> actual,
                        final Optional<? extends DataContainerChild<?, ?>> modification, final OperationStack operations)
                                throws DataModificationException {
            checkType(actual, UnkeyedListNode.class);
            checkType(modification, UnkeyedListNode.class);
            return UNKEYED_LIST_NODE_MODIFICATION.modify(schemaChild, (Optional<UnkeyedListNode>) actual,
                    (Optional<UnkeyedListNode>) modification, operations);
        }

        private static Optional<? extends DataContainerChild<?, ?>> onAugmentationNode(final AugmentationSchema schemaChild,
                final Optional<? extends DataContainerChild<?, ?>> actual,
                final Optional<? extends DataContainerChild<?, ?>> modification, final OperationStack operations)
                throws DataModificationException {
            checkType(actual, AugmentationNode.class);
            checkType(modification, AugmentationNode.class);
            return AUGMENTATION_NODE_MODIFICATION.modify(schemaChild, (Optional<AugmentationNode>) actual,
                    (Optional<AugmentationNode>) modification, operations);
        }

        private static Optional<? extends DataContainerChild<?, ?>> onLeafSetNode(final LeafListSchemaNode schemaChild,
                final Optional<? extends DataContainerChild<?, ?>> actual,
                final Optional<? extends DataContainerChild<?, ?>> modification, final OperationStack operations)
                throws DataModificationException {
            checkType(actual, LeafSetNode.class);
            checkType(modification, LeafSetNode.class);
            return LEAF_SET_NODE_MODIFICATION.modify(schemaChild, (Optional<LeafSetNode<?>>) actual,
                    (Optional<LeafSetNode<?>>) modification, operations);
        }

        private static Optional<? extends DataContainerChild<?, ?>> onContainerNode(final ContainerSchemaNode schemaChild,
                final Optional<? extends DataContainerChild<?, ?>> actual,
                final Optional<? extends DataContainerChild<?, ?>> modification, final OperationStack operations)
                throws DataModificationException {
            checkType(actual, ContainerNode.class);
            checkType(modification, ContainerNode.class);
            return CONTAINER_NODE_MODIFICATION.modify(schemaChild, (Optional<ContainerNode>) actual,
                    (Optional<ContainerNode>) modification, operations);
        }

        private static Optional<? extends DataContainerChild<?, ?>> onLeafNode(final LeafSchemaNode schemaChild,
                final Optional<? extends DataContainerChild<?, ?>> actual,
                final Optional<? extends DataContainerChild<?, ?>> modification, final OperationStack operations)
                throws DataModificationException {
            checkType(actual, LeafNode.class);
            checkType(modification, LeafNode.class);
            return LEAF_NODE_MODIFICATION.modify(schemaChild, (Optional<LeafNode<?>>) actual,
                    (Optional<LeafNode<?>>) modification, operations);
        }

        private static void checkType(final Optional<? extends DataContainerChild<?, ?>> actual, final Class<?> leafNodeClass) {
            if (actual.isPresent()) {
                Preconditions.checkArgument(leafNodeClass.isAssignableFrom(actual.get().getClass()),
                        "Unexpected node type, should be: %s, but was: %s, for: %s", leafNodeClass, actual.getClass(),
                        actual);
            }
        }
    }
}
