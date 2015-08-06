/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.sal.binding.generator.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;

public final class BindingSchemaContextUtils {

    private BindingSchemaContextUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    // FIXME: THis method does not search in case augmentations.
    public static Optional<DataNodeContainer> findDataNodeContainer(final SchemaContext ctx,
            final InstanceIdentifier<?> path) {
        Iterator<PathArgument> pathArguments = path.getPathArguments().iterator();
        PathArgument currentArg = pathArguments.next();
        Preconditions.checkArgument(currentArg != null);
        QName currentQName = BindingReflections.findQName(currentArg.getType());

        Optional<DataNodeContainer> currentContainer = Optional.absent();
        if (BindingReflections.isNotification(currentArg.getType())) {
            currentContainer = findNotification(ctx, currentQName);
        } else if (BindingReflections.isRpcType(currentArg.getType())) {
            currentContainer = findFirstDataNodeContainerInRpc(ctx, currentArg.getType());
            if(currentQName == null && currentContainer.isPresent()) {
                currentQName = ((DataSchemaNode) currentContainer.get()).getQName();
            }
        } else {
            currentContainer = findDataNodeContainer(ctx, currentQName);
        }

        while (currentContainer.isPresent() && pathArguments.hasNext()) {
            currentArg = pathArguments.next();
            if (Augmentation.class.isAssignableFrom(currentArg.getType())) {
                currentQName = BindingReflections.findQName(currentArg.getType());
                if(pathArguments.hasNext()) {
                    currentArg = pathArguments.next();
                } else {
                    return currentContainer;
                }
            }
            if(ChildOf.class.isAssignableFrom(currentArg.getType()) && BindingReflections.isAugmentationChild(currentArg.getType())) {
                currentQName = BindingReflections.findQName(currentArg.getType());
            } else {
                currentQName = QName.create(currentQName, BindingReflections.findQName(currentArg.getType()).getLocalName());
            }
            Optional<DataNodeContainer> potential = findDataNodeContainer(currentContainer.get(), currentQName);
            if (potential.isPresent()) {
                currentContainer = potential;
            } else {
                return Optional.absent();
            }
        }
        return currentContainer;
    }

    private static Optional<DataNodeContainer> findNotification(final SchemaContext ctx, final QName notificationQName) {
        for (NotificationDefinition notification : ctx.getNotifications()) {
            if (notification.getQName().equals(notificationQName)) {
                return Optional.<DataNodeContainer> of(notification);
            }
        }
        return Optional.absent();
    }

    private static Optional<DataNodeContainer> findDataNodeContainer(final DataNodeContainer ctx,
            final QName targetQName) {

        for (DataSchemaNode child : ctx.getChildNodes()) {
            if (child instanceof ChoiceSchemaNode) {
                DataNodeContainer potential = findInCases(((ChoiceSchemaNode) child), targetQName);
                if (potential != null) {
                    return Optional.of(potential);
                }
            } else if (child instanceof DataNodeContainer && child.getQName().equals(targetQName)) {
                return Optional.of((DataNodeContainer) child);
            } else if (child instanceof DataNodeContainer //
                    && child.isAddedByUses() //
                    && child.getQName().getLocalName().equals(targetQName.getLocalName())) {
                return Optional.of((DataNodeContainer) child);
            }

        }
        return Optional.absent();
    }

    private static DataNodeContainer findInCases(final ChoiceSchemaNode choiceNode, final QName targetQName) {
        for (ChoiceCaseNode caze : choiceNode.getCases()) {
            Optional<DataNodeContainer> potential = findDataNodeContainer(caze, targetQName);
            if (potential.isPresent()) {
                return potential.get();
            }
        }
        return null;
    }

    private static Optional<DataNodeContainer> findFirstDataNodeContainerInRpc(final SchemaContext ctx,
            final Class<? extends DataObject> targetType) {
        final YangModuleInfo moduleInfo;
        try {
            moduleInfo = BindingReflections.getModuleInfo(targetType);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Failed to load module information for class %s", targetType), e);
        }

        for(RpcDefinition rpc : ctx.getOperations()) {
            String rpcNamespace = rpc.getQName().getNamespace().toString();
            String rpcRevision = rpc.getQName().getFormattedRevision();
            if(moduleInfo.getNamespace().equals(rpcNamespace) && moduleInfo.getRevision().equals(rpcRevision)) {
                Optional<DataNodeContainer> potential = findInputOutput(rpc,targetType.getSimpleName());
                if(potential.isPresent()) {
                    return potential;
                }
            }
        }
        return Optional.absent();
    }

    private static Optional<DataNodeContainer> findInputOutput(final RpcDefinition rpc, final String targetType) {
        String rpcName = BindingMapping.getClassName(rpc.getQName());
        String rpcInputName = rpcName + BindingMapping.RPC_INPUT_SUFFIX;
        String rpcOutputName = rpcName + BindingMapping.RPC_OUTPUT_SUFFIX;
        if(targetType.equals(rpcInputName)) {
            return Optional.<DataNodeContainer>of(rpc.getInput());
        } else if (targetType.equals(rpcOutputName)) {
            return Optional.<DataNodeContainer>of(rpc.getOutput());
        }
       return Optional.absent();
    }

    public static Set<AugmentationSchema> collectAllAugmentationDefinitions(final SchemaContext currentSchema, final AugmentationTarget ctxNode) {
        HashSet<AugmentationSchema> augmentations = new HashSet<>();
        augmentations.addAll(ctxNode.getAvailableAugmentations());
        if(ctxNode instanceof DataSchemaNode && ((DataSchemaNode) ctxNode).isAddedByUses()) {

            System.out.println(ctxNode);

        }

        // TODO Auto-generated method stub
        return augmentations;
    }

    public static Optional<ChoiceSchemaNode> findInstantiatedChoice(final DataNodeContainer parent, final Class<?> choiceClass) {
        return findInstantiatedChoice(parent, BindingReflections.findQName(choiceClass));
    }

    public static Optional<ChoiceSchemaNode> findInstantiatedChoice(final DataNodeContainer ctxNode, final QName choiceName) {
        DataSchemaNode potential = ctxNode.getDataChildByName(choiceName);
        if (potential == null) {
            potential = ctxNode.getDataChildByName(choiceName.getLocalName());
        }

        if (potential instanceof ChoiceSchemaNode) {
            return Optional.of((ChoiceSchemaNode) potential);
        }

        return Optional.absent();
    }

    public static Optional<ChoiceCaseNode> findInstantiatedCase(final ChoiceSchemaNode instantiatedChoice, final ChoiceCaseNode originalDefinition) {
        ChoiceCaseNode potential = instantiatedChoice.getCaseNodeByName(originalDefinition.getQName());
        if(originalDefinition.equals(potential)) {
            return Optional.of(potential);
        }
        if (potential != null) {
            SchemaNode potentialRoot = SchemaNodeUtils.getRootOriginalIfPossible(potential);
            if (originalDefinition.equals(potentialRoot)) {
                return Optional.of(potential);
            }
        }
        // We try to find case by name, then lookup its root definition
        // and compare it with original definition
        // This solves case, if choice was inside grouping
        // which was used in different module and thus namespaces are
        // different, but local names are still same.
        //
        // Still we need to check equality of definition, because local name is not
        // sufficient to uniquelly determine equality of cases
        //
        potential = instantiatedChoice.getCaseNodeByName(originalDefinition.getQName().getLocalName());
        if(potential != null && (originalDefinition.equals(SchemaNodeUtils.getRootOriginalIfPossible(potential)))) {
            return Optional.of(potential);
        }
        return Optional.absent();
    }

}
