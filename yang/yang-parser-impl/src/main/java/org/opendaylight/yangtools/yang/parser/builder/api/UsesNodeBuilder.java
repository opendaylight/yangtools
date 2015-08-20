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
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

/**
 * Builder for  'uses' statement.
 */
public interface UsesNodeBuilder extends GroupingMember {

    /**
     * Get parent of this uses node. Since uses can be defined only under on of
     * module, container, list, case, grouping, input, output, notification or
     * augment, return type is DataNodeContainerBuilder.
     */
    @Override
    DataNodeContainerBuilder getParent();

    /**
     * Get target grouping path.
     *
     * @return target grouping path
     */
    SchemaPath getTargetGroupingPath();

    /**
     * Get grouping path.
     *
     * @return grouping path as SchemaPath
     */
    SchemaPath getGroupingPath();

    /**
     * Get grouping definition.
     *
     * @return GroupingDefinition if present, null otherwise
     */
    GroupingDefinition getGroupingDefinition();

    /**
     * Set grouping definition.
     *
     * @param groupingDefinition
     *            GroupingDefinition object
     */
    void setGroupingDefinition(GroupingDefinition groupingDefinition);

    /**
     * Get grouping builder.
     *
     * @return GroupingBuilder if present, null otherwise
     */
    GroupingBuilder getGroupingBuilder();

    /**
     * Set grouping builder.
     *
     * @param grouping
     *            GroupingBuilder object
     */
    void setGrouping(GroupingBuilder grouping);

    /**
     * Get information if this uses node is defined in augment.
     *
     * @return true, if this node is defined under augment statement, false
     *         otherwise
     */
    boolean isAugmenting();

    /**
     * Set information if this uses node is defined in augment.
     *
     * @param augmenting information about augmentation
     */
    void setAugmenting(boolean augmenting);

    /**
     * Get augmentations defined in this uses node.
     *
     * @return set of augmentations defined in this node
     */
    Set<AugmentationSchemaBuilder> getAugmentations();

    /**
     * Add augment builder.
     *
     * @param builder
     *            new builder of augment statement
     */
    void addAugment(AugmentationSchemaBuilder builder);

    /**
     * Get refine statements.
     *
     * @return list of RefineHolder objects
     */
    List<RefineBuilder> getRefines();

    /**
     * Get refined nodes.
     *
     * @return List of refined SchemaNodeBuilder objects
     */
    List<SchemaNodeBuilder> getRefineNodes();

    /**
     * Add refine statement.
     *
     * @param refine
     *            new RefineHolder object
     */
    void addRefine(RefineBuilder refine);

    /**
     * Add refine node.
     *
     * @param refineNode
     *            refined DataSchemaNodeBuilder object
     */
    void addRefineNode(DataSchemaNodeBuilder refineNode);

    /**
     * Build new UsesNode object.
     *
     *
     * @return UsesNode Instance of {@link UsesNode} described by this builder.
     */
    @Override
    UsesNode build();

    /**
     *
     * Returns true if uses node was resolved and {@link #getGroupingBuilder()}
     * was instantiated for parent done of this node.
     *
     * @return true if uses node was resolved and associated nodes were instantiated in parent node.
     */
    boolean isResolved();

    /**
     *
     * Sets state of instantiation of {@link #getGroupingBuilder()}
     * into parent node of this node.
     *
     * @deprecated Do not use this, this should be internal to the implementation
     *  and public API contract.
     *
     *  @param resolved resolved
     */
    @Deprecated
    void setResolved(boolean resolved);

}
