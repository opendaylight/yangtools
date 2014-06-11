/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.util;

import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

/**
 * Basic implementation of DataNodeContainerBuilder.
 */
public abstract class AbstractDocumentedDataNodeContainerBuilder extends AbstractDocumentedNodeBuilder implements DataNodeContainerBuilder {
    protected final QName qname;

    private final Set<DataSchemaNode> childNodes = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
    private final Set<DataSchemaNodeBuilder> addedChildNodes = new HashSet<>();

    private final Set<GroupingDefinition> groupings = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
    private final Set<GroupingBuilder> addedGroupings = new HashSet<>();

    private final Set<TypeDefinition<?>> typedefs = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
    private final Set<TypeDefinitionBuilder> addedTypedefs = new HashSet<>();

    private final Set<UsesNode> usesNodes = new HashSet<>();
    private final Set<UsesNodeBuilder> addedUsesNodes = new HashSet<>();

    protected AbstractDocumentedDataNodeContainerBuilder(final String moduleName, final int line, final QName qname) {
        super(moduleName, line);
        this.qname = qname;
    }

    public AbstractDocumentedDataNodeContainerBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path, final DataNodeContainer base) {
        super(moduleName, line);
        this.qname = qname;

        URI ns = qname.getNamespace();
        Date rev = qname.getRevision();
        String pref = qname.getPrefix();

        // We do copy of child nodes with namespace change
        // FIXME: Copy should be part of builder API so impl we prevent
        // cyclic dependencies and each builder carries its own semantic for copy.
        addedChildNodes.addAll(BuilderUtils.wrapChildNodes(moduleName, line, base.getChildNodes(), path, ns, rev, pref));
        addedGroupings.addAll(BuilderUtils.wrapGroupings(moduleName, line, base.getGroupings(), path, ns, rev, pref));
        addedTypedefs.addAll(BuilderUtils.wrapTypedefs(moduleName, line, base, path, ns, rev, pref));
        // FIXME: unkownSchemaNodes should be available in DataNodeContainer
//        addedUnknownNodes.addAll(BuilderUtils.wrapUnknownNodes(moduleName, line, base.getUnknownSchemaNodes(), path, ns,
//                rev, pref));
        usesNodes.addAll(base.getUses());
    }

    @Override
    public final QName getQName() {
        return qname;
    }

    @Override
    public final Set<DataSchemaNode> getChildNodes() {
        return childNodes;
    }

    @Override
    public final Set<DataSchemaNodeBuilder> getChildNodeBuilders() {
        return addedChildNodes;
    }

    @Override
    public final DataSchemaNodeBuilder getDataChildByName(final String name) {
        for (DataSchemaNodeBuilder child : addedChildNodes) {
            if (child.getQName().getLocalName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    @Override
    public final void addChildNode(final DataSchemaNodeBuilder child) {
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
    public final void addChildNodeToContext(final DataSchemaNodeBuilder child) {
        addedChildNodes.add(child);
    }

    @Override
    public final void addChildNode(final DataSchemaNode child) {
        QName childName = child.getQName();
        for (DataSchemaNode childNode : childNodes) {
            if (childNode.getQName().equals(childName)) {
                throw new YangParseException(getModuleName(), getLine(), String.format(
                        "Can not add '%s' to '%s' in module '%s': node with same name already declared", child, this,
                        getModuleName()));
            }
        }
        childNodes.add(child);
    }

    @Override
    public final Set<GroupingDefinition> getGroupings() {
        if (groupings == null) {
            return Collections.emptySet();
        }
        return groupings;
    }

    @Override
    public final Set<GroupingBuilder> getGroupingBuilders() {
        return addedGroupings;
    }

    @Override
    public final void addGrouping(final GroupingBuilder grouping) {
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
    public final Set<TypeDefinition<?>> getTypeDefinitions() {
        return typedefs;
    }

    public final Set<UsesNode> getUsesNodes() {
        return usesNodes;
    }

    @Override
    public final Set<UsesNodeBuilder> getUsesNodeBuilders() {
        return addedUsesNodes;
    }

    @Override
    public final void addUsesNode(final UsesNodeBuilder usesNode) {
        addedUsesNodes.add(usesNode);
    }


    @Override
    public final Set<TypeDefinitionBuilder> getTypeDefinitionBuilders() {
        return addedTypedefs;
    }

    @Override
    public void addTypedef(final TypeDefinitionBuilder type) {
        String typeName = type.getQName().getLocalName();
        for (TypeDefinitionBuilder addedTypedef : addedTypedefs) {
            if (addedTypedef.getQName().getLocalName().equals(typeName)) {
                throw new YangParseException(getModuleName(), type.getLine(), "Can not add typedef '" + typeName
                        + "': typedef with same name already declared at line " + addedTypedef.getLine());
            }
        }
        addedTypedefs.add(type);
    }

    protected abstract String getStatementName();

    protected void buildChildren() {
        for (DataSchemaNodeBuilder node : addedChildNodes) {
            childNodes.add(node.build());
        }

        for (GroupingBuilder builder : addedGroupings) {
            groupings.add(builder.build());
        }

        for (TypeDefinitionBuilder entry : addedTypedefs) {
            typedefs.add(entry.build());
        }

        for (UsesNodeBuilder builder : addedUsesNodes) {
            usesNodes.add(builder.build());
        }
    }

    @Deprecated
    protected static DataSchemaNode getChildNode(final Set<DataSchemaNode> childNodes, final QName name) {
        for (DataSchemaNode node : childNodes) {
            if (node.getQName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    @Deprecated
    protected static DataSchemaNode getChildNode(final Set<DataSchemaNode> childNodes, final String name) {
        for (DataSchemaNode node : childNodes) {
            if (node.getQName().getLocalName().equals(name)) {
                return node;
            }
        }
        return null;
    }


}
