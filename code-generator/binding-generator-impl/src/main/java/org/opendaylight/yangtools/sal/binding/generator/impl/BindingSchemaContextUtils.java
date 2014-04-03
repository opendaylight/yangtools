package org.opendaylight.yangtools.sal.binding.generator.impl;

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
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class BindingSchemaContextUtils {

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
                    return Optional.absent();
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
            if (child instanceof ChoiceNode) {
                DataNodeContainer potential = findInCases(((ChoiceNode) child), targetQName);
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

    private static DataNodeContainer findInCases(final ChoiceNode choiceNode, final QName targetQName) {
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
        try {
            YangModuleInfo moduleInfo = BindingReflections.getModuleInfo(targetType);
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
        } catch (Exception e) {
            // FIXME: Add logging
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

}
