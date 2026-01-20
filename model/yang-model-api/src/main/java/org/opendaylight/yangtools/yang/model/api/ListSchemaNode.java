/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueEffectiveStatement;

/**
 * Interface describing YANG 'list' statement.
 *
 * <p>The 'list' statement is used to define an interior data node in the schema tree. A list entry is uniquely
 * identified by the values of the list's keys, if defined.
 */
public interface ListSchemaNode extends ElementAwareDataSchemaNode<ListEffectiveStatement>, DataNodeContainer,
        AugmentationTarget, ActionNodeContainer, NotificationNodeContainer {
    /**
     * Returns the list of leaf identifiers.
     *
     * @return List of QNames of leaf identifiers of this list, empty if the list has no keys.
     */
    @NonNull List<@NonNull QName> getKeyDefinition();

    /**
     * Returns unique constraints.
     *
     * @return Collection of unique constraints of this list schema node
     */
    @NonNull Collection<? extends @NonNull UniqueEffectiveStatement> getUniqueConstraints();
}
