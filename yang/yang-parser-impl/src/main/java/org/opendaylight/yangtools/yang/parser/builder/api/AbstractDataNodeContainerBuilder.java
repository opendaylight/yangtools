/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
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
    protected QName qname;

    protected final Map<QName, DataSchemaNode> childNodes = new TreeMap<>(Comparators.QNAME_COMP);
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

    @Override
    public Set<DataSchemaNodeBuilder> getChildNodes() {
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
    public void addChildNode(DataSchemaNodeBuilder child) {
        String childName = child.getQName().getLocalName();
        for (DataSchemaNodeBuilder addedChildNode : addedChildNodes) {
            if (addedChildNode.getQName().getLocalName().equals(childName)) {
                throw new YangParseException(child.getModuleName(), child.getLine(), "Can not add '" + child + "' to '"
                        + this + "' in module '" + moduleName + "': node with same name already declared at line "
                        + addedChildNode.getLine());
            }
        }
        addedChildNodes.add(child);
    }

    @Override
    public void addChildNodeToContext(DataSchemaNodeBuilder child) {
        addedChildNodes.add(child);
    }

    @Override
    public void addChildNode(DataSchemaNode child) {
        QName childName = child.getQName();
        for (QName qname : childNodes.keySet()) {
            if (qname.equals(childName)) {
                throw new YangParseException(moduleName, line, "Can not add '" + child + "' to '" + this
                        + "' in module '" + moduleName + "': node with same name already declared");
            }
        }
        childNodes.put(child.getQName(), child);
    }

    @Override
    public Set<GroupingDefinition> getGroupings() {
        if (groupings == null) {
            return Collections.emptySet();
        }
        return groupings;
    }

    public Set<GroupingBuilder> getGroupingBuilders() {
        return addedGroupings;
    }

    @Override
    public void addGrouping(GroupingBuilder grouping) {
        String groupingName = grouping.getQName().getLocalName();
        for (GroupingBuilder addedGrouping : addedGroupings) {
            if (addedGrouping.getQName().getLocalName().equals(groupingName)) {
                throw new YangParseException(grouping.getModuleName(), grouping.getLine(), "Can not add '" + grouping
                        + "': grouping with same name already declared in module '" + moduleName + "' at line "
                        + addedGrouping.getLine());
            }
        }
        addedGroupings.add(grouping);
    }

    @Override
    public Set<UsesNodeBuilder> getUsesNodes() {
        return addedUsesNodes;
    }

    @Override
    public void addUsesNode(UsesNodeBuilder usesNode) {
        addedUsesNodes.add(usesNode);
    }

}
