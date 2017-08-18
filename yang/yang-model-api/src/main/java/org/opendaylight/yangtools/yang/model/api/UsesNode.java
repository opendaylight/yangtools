/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Contains the methods for getting data and checking properties of the YANG
 * <code>uses</code> substatement.
 */
public interface UsesNode extends DocumentedNode.WithStatus {

    /**
     * Returns the schema path to used grouping.
     *
     * @return schema path to 'grouping' on which this 'uses' statement points
     */
    @Nonnull SchemaPath getGroupingPath();

    /**
     * Returns augmentations which were specified in this uses node.
     *
     * @return Set of augment statements defined under this uses node
     */
    @Nonnull Set<AugmentationSchema> getAugmentations();

    /**
     * Returns <code>true</code> if the data node was added by augmentation,
     * otherwise returns <code>false</code>.
     *
     * @return <code>true</code> if the data node was added by augmentation,
     *         otherwise returns <code>false</code>
     */
    boolean isAugmenting();

    /**
     * Returns <code>true</code> if the data node was added by uses statement,
     * otherwise returns <code>false</code>.
     *
     * @return <code>true</code> if the data node was added by uses statement,
     *         otherwise returns <code>false</code>
     */
    boolean isAddedByUses();

    /**
     * Some of the properties of each node in the grouping can be refined with
     * the "refine" statement.
     *
     * @return Map, where key is schema path of refined node and value is
     *         refined node
     */
    @Nonnull Map<SchemaPath, SchemaNode> getRefines();

    /**
     * Returns when statement.
     *
     * <p>
     * If when condition is present node defined by the parent data definition
     * statement is only valid when the returned XPath expression conceptually
     * evaluates to "true" for a particular instance, then the node defined by
     * the parent data definition statement is valid; otherwise, it is not.
     *
     * @return Optional of XPath condition
     */
    default @Nonnull Optional<RevisionAwareXPath> getWhenCondition() {
        return Optional.empty();
    }
}
