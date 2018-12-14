/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * DataNodeIterator is iterator, which walks down whole YANG DataNodeContainer
 * and walks all instances of {@link DataSchemaNode} present in subtree.
 *
 * <p>
 * Iterator instance is eagerly created, walking happens on initialization. Iteration is not ordered.
 */
public class DataNodeIterator implements Iterator<DataSchemaNode> {

    private final List<ListSchemaNode> allLists = new ArrayList<>();
    private final List<ContainerSchemaNode> allContainers = new ArrayList<>();
    private final List<ChoiceSchemaNode> allChoices = new ArrayList<>();
    private final List<DataSchemaNode> allChilds = new ArrayList<>();
    private final List<GroupingDefinition> allGroupings = new ArrayList<>();
    private final List<TypeDefinition<?>> allTypedefs = new ArrayList<>();
    private final DataNodeContainer container;

    public DataNodeIterator(final DataNodeContainer container) {
        if (container == null) {
            throw new IllegalArgumentException("Data Node Container MUST be specified and cannot be NULL!");
        }

        this.container = container;
        traverse(this.container);
    }

    /**
     * Returns list all containers present in subtree.
     *
     * @return Returns list all containers present in subtree.
     */
    public List<ContainerSchemaNode> allContainers() {
        return allContainers;
    }

    /**
     * Returns list all lists present in subtree.
     *
     * @return Returns list all containers present in subtree.
     */
    public List<ListSchemaNode> allLists() {
        return allLists;
    }

    /**
     * Returns list all choices present in subtree.
     *
     * @return Returns list all containers present in subtree.
     */
    public List<ChoiceSchemaNode> allChoices() {
        return allChoices;
    }

    /**
     * Returns list all groupings present in subtree.
     *
     * @return Returns list all containers present in subtree.
     */
    public List<GroupingDefinition> allGroupings() {
        return allGroupings;
    }

    /**
     * Returns list all typedefs present in subtree.
     *
     * @return Returns list all containers present in subtree.
     */
    public List<TypeDefinition<?>> allTypedefs() {
        return allTypedefs;
    }

    private void traverse(final DataNodeContainer dataNode) {
        if (dataNode == null) {
            return;
        }

        final Iterable<DataSchemaNode> childNodes = dataNode.getChildNodes();
        if (childNodes != null) {
            for (DataSchemaNode childNode : childNodes) {
                if (childNode.isAugmenting()) {
                    continue;
                }
                allChilds.add(childNode);
                if (childNode instanceof ContainerSchemaNode) {
                    final ContainerSchemaNode containerNode = (ContainerSchemaNode) childNode;
                    allContainers.add(containerNode);
                    traverse(containerNode);
                } else if (childNode instanceof ListSchemaNode) {
                    final ListSchemaNode list = (ListSchemaNode) childNode;
                    allLists.add(list);
                    traverse(list);
                } else if (childNode instanceof ChoiceSchemaNode) {
                    final ChoiceSchemaNode choiceNode = (ChoiceSchemaNode) childNode;
                    allChoices.add(choiceNode);
                    for (final CaseSchemaNode caseNode : choiceNode.getCases().values()) {
                        traverse(caseNode);
                    }
                }
            }
        }

        this.allTypedefs.addAll(dataNode.getTypeDefinitions());
        traverseModule(dataNode);
        traverseGroupings(dataNode);

    }

    private void traverseModule(final DataNodeContainer dataNode) {
        final Module module;
        if (dataNode instanceof Module) {
            module = (Module) dataNode;
        } else {
            return;
        }

        final Set<NotificationDefinition> notifications = module.getNotifications();
        for (NotificationDefinition notificationDefinition : notifications) {
            traverse(notificationDefinition);
        }

        final Set<RpcDefinition> rpcs = module.getRpcs();
        for (RpcDefinition rpcDefinition : rpcs) {
            this.allTypedefs.addAll(rpcDefinition.getTypeDefinitions());
            ContainerSchemaNode input = rpcDefinition.getInput();
            if (input != null) {
                traverse(input);
            }
            ContainerSchemaNode output = rpcDefinition.getOutput();
            if (output != null) {
                traverse(output);
            }
        }
    }

    private void traverseGroupings(final DataNodeContainer dataNode) {
        final Set<GroupingDefinition> groupings = dataNode.getGroupings();
        if (groupings != null) {
            for (GroupingDefinition grouping : groupings) {
                allGroupings.add(grouping);
                traverse(grouping);
            }
        }
    }

    @Override
    public boolean hasNext() {
        if (container.getChildNodes() != null) {
            final Collection<DataSchemaNode> childNodes = container.getChildNodes();

            if (childNodes != null && !childNodes.isEmpty()) {
                return childNodes.iterator().hasNext();
            }
        }
        return false;
    }

    @Override
    public DataSchemaNode next() {
        return allChilds.iterator().next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
