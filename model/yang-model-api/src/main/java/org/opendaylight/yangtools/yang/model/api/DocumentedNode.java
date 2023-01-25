/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;

/**
 * Node which can have documentation assigned.
 */
public interface DocumentedNode {
    /**
     * Returns the value of the argument of YANG {@code description} keyword.
     *
     * @return string with the description, or {@link Optional#empty()} if description was not provided.
     */
    Optional<String> getDescription();

    /**
     * Returns the value of the argument of YANG {@code reference} keyword.
     *
     * @return string with reference to some other document, or {@link Optional#empty()} if reference was not provided.
     */
    Optional<String> getReference();

    /**
     * Returns unknown schema nodes which belongs to this instance. Default implementation returns an empty collection.
     *
     * @return collection of unknown schema nodes defined under this node.
     */
    default @NonNull Collection<? extends @NonNull UnknownSchemaNode> getUnknownSchemaNodes() {
        return ImmutableList.of();
    }

    /**
     * A {@link DocumentedNode} which also has as {@link Status}.
     *
     * @deprecated This interface is scheduled for removal. Users accessing it are advised to use
     *             {@link EffectiveStatement} interfaces and follow {@link StatusEffectiveStatement} contract.
     */
    @Deprecated
    interface WithStatus extends DocumentedNode {
        /**
         * Returns status of the instance of the type {@code SchemaNode}.
         *
         * @return status of this node which represents the argument of the YANG {@code status} substatement
         */
        @NonNull Status getStatus();
    }
}
