/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.operations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.*;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.AugmentationSchemaProxy;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.SchemaUtils;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.opendaylight.yangtools.yang.model.api.*;

class MapEntryNodeModification implements Modification<ListSchemaNode, MapEntryNode> {

    public static final LeafNodeModification LEAF_NODE_MODIFICATION = new LeafNodeModification();
    public static final LeafSetNodeModification LEAF_LIST_NODE_MODIFICATION = new LeafSetNodeModification();
    public static final AugmentationNodeModification AUGMENTATION_NODE_MODIFICATION = new AugmentationNodeModification();
//    public static final MapNodeModification LIST_NODE_MODIFICATION = new MapNodeModification();

    @Override
    public Optional<MapEntryNode> modify(ListSchemaNode schema,  Optional<MapEntryNode> actual,
                                   Optional<MapEntryNode> modification, OperationStack operationStack)
            throws DataModificationException {

        operationStack.enteringNode(modification);

        Optional<MapEntryNode> result = null;
        QName nodeQName = schema.getQName();

        switch (operationStack.getCurrentOperation()) {
            case DELETE:
            {
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
            case NONE:
                DataModificationException.DataMissingException.check(nodeQName, actual);
            case MERGE: {
                // Recursively modify all child nodes
                result = modifyContainer(schema, actual, modification, operationStack);
                break;
            }
        }

        operationStack.exitingNode(modification);
        return result;
    }

    private Optional<MapEntryNode> modifyContainer(ListSchemaNode schema, Optional<MapEntryNode> actual, Optional<MapEntryNode> modification, OperationStack operationStack)
            throws DataModificationException {

        if (actual.isPresent() == false) {
            return modification;
        }

        if (modification.isPresent() == false) {
            return actual;
        }

        Set<InstanceIdentifier.PathArgument> toProcess = getQNamesToProcess(actual, modification);

        List<? extends DataContainerChild<?, ?>> merged = modifyContainerChildNodes(schema, operationStack, actual.get(), modification.get(), toProcess);
        return build(schema, merged);
    }

    private List<? extends DataContainerChild<?, ?>> modifyContainerChildNodes(ListSchemaNode schema, OperationStack operationStack,
                                                                               MapEntryNode actual, MapEntryNode modification, Set<InstanceIdentifier.PathArgument> toProcess) throws DataModificationException {
        List<DataContainerChild<?, ?>> result = Lists.newArrayList();

        for (InstanceIdentifier.PathArgument childToProcessId : toProcess) {
            Object schemaOfChildToProcess = findSchema(schema, childToProcessId);

            Optional<? extends DataContainerChild<?, ?>> modifiedValues = modifyContainerNode(operationStack, actual, modification,
                    childToProcessId, schemaOfChildToProcess);

            if (modifiedValues.isPresent()) {
                result.add(modifiedValues.get());
            }
        }

        return result;
    }

    private Optional<? extends DataContainerChild<?, ?>> modifyContainerNode(OperationStack operationStack, MapEntryNode actual, MapEntryNode modification, InstanceIdentifier.PathArgument childToProcess, Object schemaChild) throws DataModificationException {

        Optional<DataContainerChild<?, ?>> storedChildren = actual.getChild(childToProcess);
        Optional<DataContainerChild<?, ?>> modifiedChildren = modification.getChild(childToProcess);

        return dispatchChildModification(schemaChild, storedChildren, modifiedChildren,
                operationStack);
    }

    private Optional<? extends DataContainerChild<?, ?>> dispatchChildModification(Object schemaChild, Optional<DataContainerChild<?, ?>> actual,
                                                                         Optional<DataContainerChild<?, ?>> modification, OperationStack operations) throws DataModificationException {

        if (schemaChild instanceof LeafSchemaNode) {
            // TODO check types
            LeafNode<?> actualLeaf = (LeafNode<?>) actual.orNull();
            LeafNode<?> modificationLeaf = (LeafNode<?>) modification.orNull();

            return LEAF_NODE_MODIFICATION.modify((LeafSchemaNode) schemaChild, Optional.<LeafNode<?>>fromNullable(actualLeaf),
                    Optional.<LeafNode<?>>fromNullable(modificationLeaf), operations);

        } else if (schemaChild instanceof ContainerSchemaNode) {
            // TODO check types
            ContainerNode actualContainer = (ContainerNode) actual.orNull();
            ContainerNode modificationContainer = (ContainerNode) modification.orNull();

            return new ContainerNodeModification().modify((ContainerSchemaNode) schemaChild, Optional.fromNullable(actualContainer),
                    Optional.fromNullable(modificationContainer), operations);

        } else if (schemaChild instanceof LeafListSchemaNode) {
            // TODO check types
            LeafSetNode<?> actualLeafSet = (LeafSetNode<?>) actual.orNull();
            LeafSetNode<?> modificationLeafSet = (LeafSetNode<?>) modification.orNull();

            return LEAF_LIST_NODE_MODIFICATION.modify((LeafListSchemaNode) schemaChild,
                    Optional.<LeafSetNode<?>>fromNullable(actualLeafSet),
                    Optional.<LeafSetNode<?>>fromNullable(modificationLeafSet), operations);
//        } else if (schemaChild instanceof ListSchemaNode) {
//            ListSchemaNode listSchema = (ListSchemaNode) schemaChild;
//            return LIST_NODE_MODIFICATION.modify(listSchema, NodeWrappers.wrapListNodes(listSchema, actual),
//                    NodeWrappers.wrapListNodes(listSchema, modification), operations);
        } else if (schemaChild instanceof AugmentationSchema) {
            // TODO check types
            AugmentationNode actualContainer = (AugmentationNode) actual.orNull();
            AugmentationNode modificationContainer = (AugmentationNode) modification.orNull();

            AugmentationSchema listSchema = (AugmentationSchema) schemaChild;
            return AUGMENTATION_NODE_MODIFICATION.modify(listSchema, Optional.fromNullable(actualContainer),
                    Optional.fromNullable(modificationContainer), operations);
        }

        throw new IllegalArgumentException("Unknown schema node type " + schemaChild);
    }

    private Object findSchema(DataNodeContainer schema, InstanceIdentifier.PathArgument childToProcessId) {

        if(childToProcessId instanceof InstanceIdentifier.AugmentationIdentifier) {
            Preconditions.checkArgument(schema instanceof AugmentationTarget);

            AugmentationSchema augmentSchema = null;
            for (AugmentationSchema augment : ((AugmentationTarget)schema).getAvailableAugmentations()) {
                HashSet<QName> augmentQNames = Sets.newHashSet(Collections2.transform(augment.getChildNodes(), new Function<DataSchemaNode, QName>() {
                    public QName apply(DataSchemaNode input) {
                        return input.getQName();
                    }
                }));

                if(((InstanceIdentifier.AugmentationIdentifier) childToProcessId).getPossibleChildNames().equals(augmentQNames)) {
                    augmentSchema = augment;
                    break;
                }
            }

            Set<DataSchemaNode> realChildSchemas = SchemaUtils.getRealSchemasForAugment(schema, augmentSchema);
            return new AugmentationSchemaProxy(augmentSchema, realChildSchemas);
        } else {
            DataSchemaNode dataChildByName = schema.getDataChildByName(childToProcessId.getNodeType());
            Preconditions.checkNotNull(dataChildByName, "Unknown child: %s, in: %s", childToProcessId, schema);
            return dataChildByName;
        }
    }

    private static Optional<MapEntryNode> build(DataNodeContainer schema, List<? extends DataContainerChild<?, ?>> result) {
        DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> b = Builders.mapEntryBuilder((ListSchemaNode) schema);

        for (DataContainerChild<?,?> dataContainerChild : result) {
            b.withChild(dataContainerChild);
        }
        return Optional.of(b.build());
    }

    private static Set<InstanceIdentifier.PathArgument> getQNamesToProcess(Optional<MapEntryNode> actual, Optional<MapEntryNode> modification) {
        Set<InstanceIdentifier.PathArgument> qNames = Sets.newHashSet();

        qNames.addAll(getChildQNames(actual));
        qNames.addAll(getChildQNames(modification));
        return qNames;
    }

    private static Set<? extends InstanceIdentifier.PathArgument> getChildQNames(Optional<MapEntryNode> actual) {
        Set<InstanceIdentifier.PathArgument> qNames = Sets.newHashSet();

        for (DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> child : actual.get().getValue()) {
            qNames.add(child.getIdentifier());
        }

        return qNames;
    }


}
