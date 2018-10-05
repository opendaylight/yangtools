/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;

/**
 * Contains the methods for getting data and checking properties of the YANG <code>uses</code> substatement.
 */
public interface UsesNode extends WhenConditionAware, WithStatus, CopyableNode {

    /**
     * Returns the schema path to used grouping.
     *
     * @return schema path to 'grouping' on which this 'uses' statement points
     */
    @NonNull SchemaPath getGroupingPath();

    /**
     * Returns augmentations which were specified in this uses node.
     *
     * @return Set of augment statements defined under this uses node
     */
    @NonNull Set<AugmentationSchemaNode> getAugmentations();

    /**
     * Some of the properties of each node in the grouping can be refined with
     * the "refine" statement.
     *
     * @return Map, where key is schema path of refined node and value is
     *         refined node
     */
    @NonNull Map<SchemaPath, SchemaNode> getRefines();
}
