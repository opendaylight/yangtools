/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

public abstract class AbstractDocumentedDataNodeContainer extends AbstractDocumentedNode implements DataNodeContainer {

    private final Map<QName, DataSchemaNode> childNodes;
    private final Set<GroupingDefinition> groupings;
    private final Set<UsesNode> uses;
    private final Set<TypeDefinition<?>> typeDefinitions;
    private final Set<DataSchemaNode> publicChildNodes;

    protected AbstractDocumentedDataNodeContainer(final AbstractDocumentedDataNodeContainerBuilder data) {
        super(data);
        // FIXME : Should be unmodifiable ordered set (ordered by QName... or appearance in YANG file).
        // consider using new TreeSet<>(Comparators.SCHEMA_NODE_COMP);

        childNodes = ImmutableMap.copyOf(data.getChildNodes());
        groupings = ImmutableSet.copyOf(data.getGroupings());
        uses = ImmutableSet.copyOf(data.getUsesNodes());
        typeDefinitions = ImmutableSet.copyOf(data.getTypeDefinitions());
        publicChildNodes = ImmutableSet.copyOf(childNodes.values());
    }

    @Override
    public final Set<TypeDefinition<?>> getTypeDefinitions() {
        return typeDefinitions;
    }

    @Override
    public final Set<DataSchemaNode> getChildNodes() {
        return publicChildNodes;
    }

    @Override
    public final Set<GroupingDefinition> getGroupings() {
        return groupings;
    }

    @Override
    public final DataSchemaNode getDataChildByName(final QName name) {
        // Child nodes are keyed by their container name, so we can do a direct lookup
        return childNodes.get(name);
    }

    @Override
    public final DataSchemaNode getDataChildByName(final String name) {
        for (DataSchemaNode node : childNodes.values()) {
            if (node.getQName().getLocalName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public Set<UsesNode> getUses() {
        return uses;
    }

}
