/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.api;

import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;

/**
 * Interface for builders of 'grouping' statement.
 *
 * @deprecated Pre-Beryllium implementation, scheduled for removal.
 */
@Deprecated
public interface GroupingBuilder extends DataNodeContainerBuilder, SchemaNodeBuilder, GroupingMember {

    /**
     * Build GroupingDefinition object from this builder.
     *
     * @return Instance of {@link GroupingDefinition} described by this builder.
     */
    @Override
    GroupingDefinition build();

    /**
     *
     * Returns instantiation of grouping child nodes under supplied builder.
     *
     * Supplied newParent is not modified.
     *
     * For each {@link #getChildNodeBuilders()} new builder is created,
     * which has supplied new parent set as their {@link Builder#getParent()}
     * and QNames have updated namespace and revision per supplied parent
     * node.
     *
     * @param newParent Parent node, under which this grouping should be instantiated.
     * @return List of new builders representing instantiation of this grouping.
     */
    List<DataSchemaNodeBuilder> instantiateChildNodes(Builder newParent);


   /**
    *
    * Returns instantiation of grouping type definitions under supplied builder.
    *
    * Supplied newParent is not modified.
    *
    * For each {@link #getTypeDefinitionBuilders()} new builder is created,
    * which has supplied new parent set as their {@link Builder#getParent()}
    * and QNames have updated namespace and revision per supplied parent
    * node.
    *
    * @param newParent Parent node, under which this grouping should be instantiated.
    * @return Set of new builders representing instantiation of this grouping.
    */
    Set<TypeDefinitionBuilder> instantiateTypedefs(Builder newParent);

   /**
    *
    * Returns instantiation of grouping definitions under supplied builder.
    *
    * Supplied newParent is not modified.
    *
    * For each {@link #getGroupingBuilders()} new builder is created,
    * which has supplied new parent set as their {@link Builder#getParent()}
    * and QNames have updated namespace and revision per supplied parent
    * node.
    *
    * @param newParent Parent node, under which this grouping should be instantiated.
    * @return Set of new builders representing instantiation of this grouping.
    */
    Set<GroupingBuilder> instantiateGroupings(Builder newParent);

    /**
    *
    * Returns instantiation of unknown nodes under supplied builder.
    *
    * Supplied newParent is not modified.
    *
    * For each {@link #getUnknownNodes()} new builder is created,
    * which has supplied new parent set as their {@link Builder#getParent()}
    * and QNames have updated namespace and revision per supplied parent
    * node.
    *
    * @param newParent Parent node, under which this grouping should be instantiated.
    * @return Set of new builders representing instantiation of this grouping.
    */
    Set<UnknownSchemaNodeBuilder> instantiateUnknownNodes(Builder newParent);

}
