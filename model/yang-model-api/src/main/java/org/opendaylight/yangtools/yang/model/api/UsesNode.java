/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;

/**
 * Contains the methods for getting data and checking properties of the YANG <code>uses</code> substatement.
 */
public interface UsesNode extends WhenConditionAware, WithStatus, CopyableNode,
        EffectiveStatementEquivalent<UsesEffectiveStatement> {
    /**
     * Returns the {code grouping} which this node acted upon.
     *
     * @return Source grouping
     */
    @NonNull GroupingDefinition getSourceGrouping();

    /**
     * Returns augmentations which were specified in this uses node.
     *
     * @return Set of augment statements defined under this uses node
     */
    @NonNull Collection<? extends @NonNull AugmentationSchemaNode> getAugmentations();

    /**
     * Some of the properties of each node in the grouping can be refined with the "refine" statement.
     *
     * @return {@link Descendant} paths of {@code refine}d children.
     */
    default @NonNull Set<Descendant> getRefines() {
        return asEffectiveStatement().streamEffectiveSubstatements(RefineEffectiveStatement.class)
            .map(RefineEffectiveStatement::argument)
            .collect(ImmutableSet.toImmutableSet());
    }
}
