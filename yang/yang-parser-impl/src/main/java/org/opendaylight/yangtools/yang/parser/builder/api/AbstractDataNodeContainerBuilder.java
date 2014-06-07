/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.parser.util.Comparators;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

/**
 * Basic implementation of DataNodeContainerBuilder.
 */
public abstract class AbstractDataNodeContainerBuilder extends AbstractBuilder implements DataNodeContainerBuilder {
    protected final QName qname;

    protected final Map<QName, DataSchemaNode> childNodes = new TreeMap<>();
    protected final Set<DataSchemaNodeBuilder> addedChildNodes = new HashSet<>();

    protected final Set<GroupingDefinition> groupings = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
    protected final Set<GroupingBuilder> addedGroupings = new HashSet<>();

    protected final Set<TypeDefinition<?>> typedefs = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
    protected final Set<TypeDefinitionBuilder> addedTypedefs = new HashSet<>();

    protected final Set<UsesNode> usesNodes = new HashSet<>();
    protected final Set<UsesNodeBuilder> addedUsesNodes = new HashSet<>();

    protected AbstractDataNodeContainerBuilder(final String moduleName, final int line, final QName qname) {
        super(moduleName, line);
        this.qname = qname;
    }

    @Override
    public QName getQName() {
        return qname;
    }

    public Map<QName, DataSchemaNode> getChildNodes() {
        return childNodes;
    }

    @Override
    public Set<DataSchemaNodeBuilder> getChildNodeBuilders() {
        return addedChildNodes;
    }

    @Override
    public DataSchemaNodeBuilder getDataChildByName(final String name) {
        for (DataSchemaNodeBuilder child : addedChildNodes) {
            if (child.getQName().getLocalName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    @Override
    public void addChildNode(final DataSchemaNodeBuilder child) {
        QName childName = child.getQName();
        for (DataSchemaNodeBuilder addedChildNode : addedChildNodes) {
            if (addedChildNode.getQName().equals(childName)) {
                throw new YangParseException(child.getModuleName(), child.getLine(), String.format(
                        "Can not add '%s' to '%s' in module '%s': node with same name already declared at line %d",
                        child, this, getModuleName(), addedChildNode.getLine()));
            }
        }
        addedChildNodes.add(child);
    }

    @Override
    public void addChildNodeToContext(final DataSchemaNodeBuilder child) {
        addedChildNodes.add(child);
    }

    @Override
    public void addChildNode(final DataSchemaNode child) {
        QName childName = child.getQName();
        if (childNodes.containsKey(childName)) {
            throw new YangParseException(moduleName, line,
                    String.format("Can not add '%s' to '%s' in module '%s': node with same name already declared",
                            child, this, moduleName));
        }
        childNodes.put(childName, child);
    }

    @Override
    public Set<GroupingDefinition> getGroupings() {
        if (groupings == null) {
            return Collections.emptySet();
        }
        return groupings;
    }

    @Override
    public Set<GroupingBuilder> getGroupingBuilders() {
        return addedGroupings;
    }

    @Override
    public void addGrouping(final GroupingBuilder grouping) {
        QName groupingName = grouping.getQName();
        for (GroupingBuilder addedGrouping : addedGroupings) {
            if (addedGrouping.getQName().equals(groupingName)) {
                throw new YangParseException(grouping.getModuleName(), grouping.getLine(), String.format(
                        "Can not add '%s': grouping with same name already declared in module '%s' at line %d",
                        grouping, getModuleName(), addedGrouping.getLine()));
            }
        }
        addedGroupings.add(grouping);
    }

    @Override
    public Set<TypeDefinition<?>> getTypeDefinitions() {
        return typedefs;
    }

    public Set<UsesNode> getUsesNodes() {
        return usesNodes;
    }

    @Override
    public Set<UsesNodeBuilder> getUsesNodeBuilders() {
        return addedUsesNodes;
    }

    @Override
    public void addUsesNode(final UsesNodeBuilder usesNode) {
        addedUsesNodes.add(usesNode);
    }

    protected static DataSchemaNode getChildNode(final Set<DataSchemaNode> childNodes, final QName name) {
        for (DataSchemaNode node : childNodes) {
            if (node.getQName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    protected static DataSchemaNode getChildNode(final Set<DataSchemaNode> childNodes, final String name) {
        for (DataSchemaNode node : childNodes) {
            if (node.getQName().getLocalName().equals(name)) {
                return node;
            }
        }
        return null;
    }

}
