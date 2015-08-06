/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.api;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Common builder for for all YANG {@link DataNodeContainer}
 * <code>augment, case, container,
 * grouping, list, module, notification</code>.
 */
public interface DataNodeContainerBuilder extends Builder {

    /**
     * Returns QName of this node.
     *
     * @return QName of this node
     */
    QName getQName();

    /**
     * Returns Schema path of this node.
     *
     * @return SchemaPath of this node
     */
    SchemaPath getPath();

    /**
     * Returns list of of child node builders in order they are declared in yang
     * file.
     *
     * This child node builder are build, during invoking {@link #build()} and
     * added as children to resulting {@link DataNodeContainer}.
     *
     * @return collection child nodes builders
     */
    List<DataSchemaNodeBuilder> getChildNodeBuilders();

    /**
     * Retrieves child node builder by local name.
     *
     * @param name
     *            name of child to seek
     * @return child node with given name if present, null otherwise
     */
    DataSchemaNodeBuilder getDataChildByName(String name);

    /**
     * Add builder of child node to this node.
     *
     * @param childNode
     */
    void addChildNode(DataSchemaNodeBuilder childNode);

    /**
     * Add builder of child node to this node at specified position.
     *
     * @param childNode
     */
    void addChildNode(int index, DataSchemaNodeBuilder childNode);

    void addChildNode(DataSchemaNode childNode);

    void addChildNodeToContext(DataSchemaNodeBuilder childNode);

    /**
     * Get already built groupings defined in this node.
     *
     * @return collection of GroupingDefinition objects
     */
    Set<GroupingDefinition> getGroupings();

    /**
     * Get builders of groupings defined in this node.
     *
     * @return collection of grouping builders
     */
    Set<GroupingBuilder> getGroupingBuilders();

    /**
     * Add builder of grouping statement to this node.
     *
     * @param groupingBuilder
     */
    void addGrouping(GroupingBuilder groupingBuilder);

    /**
     * Get builders of uses defined in this node.
     *
     * @return collection of uses builders
     */
    List<UsesNodeBuilder> getUsesNodeBuilders();

    /**
     * Add builder of uses statement to this node.
     *
     * @param usesBuilder
     */
    void addUsesNode(UsesNodeBuilder usesBuilder);

    /**
     * Returns set of already built type definitions.
     *
     * @return set of already built type definitions.
     */
    Set<TypeDefinition<?>> getTypeDefinitions();

    /**
     * Returns builders of typedef statement defined in this node.
     *
     * @return builders of typedef statement defined in this node.
     */
    Set<TypeDefinitionBuilder> getTypeDefinitionBuilders();

    /**
     * Add typedef builder to this node.
     *
     * @param typedefBuilder
     *            Builder to add to this node.
     */
    void addTypedef(TypeDefinitionBuilder typedefBuilder);

    /**
     * Returns an instance of product - DataNodeContainer
     *
     * Returns an instance of data node container with children and properties
     * constructed as per this builder state, all nested builders are also built
     * and their product is set to DataNodeContainer.
     *
     * @return Instance of DataNodeContainer
     */
    @Override
    DataNodeContainer build();

    /**
     * Returns map of child nodes of this node. Child nodes should be in same
     * order as they were defined in yang file.
     *
     * @return map of child nodes of this node
     */
    Map<QName, DataSchemaNode> getChildNodes();

}
