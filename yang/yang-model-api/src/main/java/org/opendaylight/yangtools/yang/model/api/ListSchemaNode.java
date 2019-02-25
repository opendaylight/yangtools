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

/**
 * Interface describing YANG 'list' statement.
 *
 * <p>
 * The 'list' statement is used to define an interior data node in the schema tree. A list entry is uniquely identified
 * by the values of the list's keys, if defined.
 */
public interface ListSchemaNode extends DataNodeContainer, AugmentationTarget, DataSchemaNode,
        NotificationNodeContainer, ActionNodeContainer, ElementCountConstraintAware, MustConstraintAware {
    /**
     * Returns the list of leaf identifiers.
     *
     * @return List of QNames of leaf identifiers of this list, empty if the list has no keys.
     */
    @NonNull List<QName> getKeyDefinition();

    /**
     * YANG 'ordered-by' statement. It defines whether the order of entries within a list are determined by the user
     * or the system. If not present, default is false.
     *
     * @return true if ordered-by argument is "user", false otherwise
     */
    boolean isUserOrdered();

    /**
     * Returns unique constraints.
     *
     * @return Collection of unique constraints of this list schema node
     */
    @NonNull Collection<UniqueConstraint> getUniqueConstraints();
}
