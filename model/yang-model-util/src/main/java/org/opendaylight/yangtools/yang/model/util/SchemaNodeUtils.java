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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.InputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.OutputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

@Deprecated(since = "7.0.0", forRemoval = true)
public final class SchemaNodeUtils {
    private SchemaNodeUtils() {
        // Hidden on purpose
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
