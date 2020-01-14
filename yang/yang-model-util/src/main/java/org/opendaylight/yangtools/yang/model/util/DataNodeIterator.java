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
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * DataNodeIterator is iterator, which walks down whole YANG DataNodeContainer
 * and walks all instances of {@link DataSchemaNode} present in subtree.
 *
 * <p>
 * Iterator instance is eagerly created, walking happens on initialization. Iteration is not ordered.
 *
 * @deprecated Use {@link SchemaNodeUtils#getAllContainers(DataNodeContainer)},
 *             {@link SchemaNodeUtils#getAllTypeDefinitions(DataNodeContainer)} or
 *             {@link SchemaNodeUtils#traverse(DataNodeAggregator, DataNodeContainer)} instead.
 */
@Deprecated
public class DataNodeIterator extends DataNodeAggregator implements Iterator<DataSchemaNode> {
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
        SchemaNodeUtils.traverse(this, container);
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

    @Override
    public boolean hasNext() {
        return !container.getChildNodes().isEmpty();
    }

    @Override
    public DataSchemaNode next() {
        return allChilds.iterator().next();
    }

    @Override
    protected void addChild(final DataSchemaNode childNode) {
        allChilds.add(childNode);
    }

    @Override
    protected void addContainer(final ContainerSchemaNode containerNode) {
        allContainers.add(containerNode);
    }

    @Override
    protected void addList(final ListSchemaNode list) {
        allLists.add(list);
    }

    @Override
    protected void addChoice(final ChoiceSchemaNode choiceNode) {
        allChoices.add(choiceNode);
    }

    @Override
    protected void addTypedefs(final Collection<? extends TypeDefinition<?>> typeDefs) {
        allTypedefs.addAll(typeDefs);
    }

    @Override
    protected void addGrouping(final GroupingDefinition grouping) {
        allGroupings.add(grouping);
    }
}
