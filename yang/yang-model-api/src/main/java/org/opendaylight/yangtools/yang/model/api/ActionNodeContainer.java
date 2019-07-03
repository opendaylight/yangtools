/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Node which can contain action nodes.
 */
@Beta
public interface ActionNodeContainer {
    /**
     * Return the set of actions.
     *
     * @return set of action nodes
     */
    @NonNull Set<ActionDefinition> getActions();

    /**
     * Find an action based on its QName. Default implementation searches the set returned by {@link #getActions()}.
     *
     * @param qname Action's QName
     * @return Action definition, if found
     * @throws NullPointerException if qname is null
     */
    default Optional<ActionDefinition> findAction(final QName qname) {
        requireNonNull(qname);
        for (ActionDefinition action : getActions()) {
            if (qname.equals(action.getQName())) {
                return Optional.of(action);
            }
        }
        return Optional.empty();
    }
}
