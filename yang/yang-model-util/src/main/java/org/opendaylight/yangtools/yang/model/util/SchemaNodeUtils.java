/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.InputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.OutputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

public final class SchemaNodeUtils {
    private SchemaNodeUtils() {
        // Hidden on purpose
    }

    public static Optional<SchemaNode> getOriginalIfPossible(final SchemaNode node) {
        if (node instanceof DerivableSchemaNode) {
            @SuppressWarnings("unchecked")
            final Optional<SchemaNode> ret  = (Optional<SchemaNode>) ((DerivableSchemaNode) node).getOriginal();
            return ret;
        }
        return Optional.empty();
    }

    public static SchemaNode getRootOriginalIfPossible(final SchemaNode data) {
        Optional<SchemaNode> previous = Optional.empty();
        Optional<SchemaNode> next = getOriginalIfPossible(data);
        while (next.isPresent()) {
            previous = next;
            next = getOriginalIfPossible(next.get());
        }
        return previous.orElse(null);
    }

    /**
     * Returns RPC input or output schema based on supplied QName.
     *
     * @param rpc RPC Definition
     * @param qname input or output QName with namespace same as RPC
     * @return input or output schema. Returns null if RPC does not have input/output specified.
     */
    public static @Nullable ContainerLike getRpcDataSchema(final @NonNull RpcDefinition rpc,
            final @NonNull QName qname) {
        requireNonNull(rpc, "Rpc Schema must not be null");
        switch (requireNonNull(qname, "QName must not be null").getLocalName()) {
            case "input":
                return rpc.getInput();
            case "output":
                return rpc.getOutput();
            default:
                throw new IllegalArgumentException("Supplied qname " + qname
                        + " does not represent rpc input or output.");
        }
    }

    @Beta
    public static @NonNull Collection<? extends TypeDefinition<?>> getAllTypeDefinitions(
            final DataNodeContainer parent) {
        final List<TypeDefinition<?>> typedefs = new ArrayList<>();
        traverse(new DataNodeAggregator() {
            @Override
            protected void addTypedefs(final Collection<? extends TypeDefinition<?>> typeDefs) {
                typedefs.addAll(typeDefs);
            }
        }, requireNonNull(parent));
        return typedefs;
    }

    @Beta
    public static @NonNull Collection<? extends ContainerSchemaNode> getAllContainers(final DataNodeContainer parent) {
        final List<ContainerSchemaNode> containers = new ArrayList<>();
        traverse(new DataNodeAggregator() {
            @Override
            protected void addContainer(final ContainerSchemaNode containerNode) {
                containers.add(containerNode);
            }
        }, requireNonNull(parent));
        return containers;
    }

    @Beta
    public static void traverse(final @NonNull DataNodeAggregator aggregator, final DataNodeContainer dataNode) {
        if (dataNode == null) {
            return;
        }

        for (DataSchemaNode childNode : dataNode.getChildNodes()) {
            if (childNode.isAugmenting()) {
                continue;
            }
            aggregator.addChild(childNode);
            if (childNode instanceof ContainerSchemaNode) {
                final ContainerSchemaNode containerNode = (ContainerSchemaNode) childNode;
                aggregator.addContainer(containerNode);
                traverse(aggregator, containerNode);
            } else if (childNode instanceof ListSchemaNode) {
                final ListSchemaNode list = (ListSchemaNode) childNode;
                aggregator.addList(list);
                traverse(aggregator, list);
            } else if (childNode instanceof ChoiceSchemaNode) {
                final ChoiceSchemaNode choiceNode = (ChoiceSchemaNode) childNode;
                aggregator.addChoice(choiceNode);
                for (final CaseSchemaNode caseNode : choiceNode.getCases()) {
                    traverse(aggregator, caseNode);
                }
            }
        }

        aggregator.addTypedefs(dataNode.getTypeDefinitions());

        traverseModule(aggregator, dataNode);
        traverseGroupings(aggregator, dataNode);
    }

    private static void traverseModule(final DataNodeAggregator aggregator, final DataNodeContainer dataNode) {
        final Module module;
        if (dataNode instanceof Module) {
            module = (Module) dataNode;
        } else {
            return;
        }

        for (NotificationDefinition notificationDefinition : module.getNotifications()) {
            traverse(aggregator, notificationDefinition);
        }

        for (RpcDefinition rpcDefinition : module.getRpcs()) {
            aggregator.addTypedefs(rpcDefinition.getTypeDefinitions());
            InputSchemaNode input = rpcDefinition.getInput();
            if (input != null) {
                traverse(aggregator, input);
            }
            OutputSchemaNode output = rpcDefinition.getOutput();
            if (output != null) {
                traverse(aggregator, output);
            }
        }
    }

    private static void traverseGroupings(final DataNodeAggregator aggregator, final DataNodeContainer dataNode) {
        for (GroupingDefinition grouping : dataNode.getGroupings()) {
            aggregator.addGrouping(grouping);
            traverse(aggregator, grouping);
        }
    }
}
