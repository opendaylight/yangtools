/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;

/**
 * Interface describing YANG 'grouping' statement.
 *
 * <p>
 * It is used to define a reusable block of nodes, which may be used locally in
 * the module, in modules that include it, and by other modules that import from it.
 *
 * <p>
 * Note: this interface extends {@link AddedByUsesAware}, but this contradicts the javadoc of {@link #isAddedByUses()},
 *       as groupings can never be encountered in 'data schema node' context. It is their children, which are data
 *       schema node, but those really are instantiated and typically differ in {@link #getQName()}'s namespace.
 */
public interface GroupingDefinition extends DataNodeContainer, SchemaNode, NotificationNodeContainer,
       ActionNodeContainer, AddedByUsesAware, EffectiveStatementEquivalent {
    @Override
    GroupingEffectiveStatement asEffectiveStatement();
}
