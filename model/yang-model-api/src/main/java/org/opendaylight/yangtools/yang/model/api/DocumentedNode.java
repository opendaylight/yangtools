/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;

/**
 * Node which can have documentation assigned.
 */
public interface DocumentedNode {
    /**
     * Returns the value of the argument of YANG <code>description</code> keyword.
     *
     * @return string with the description, or empty if description was not provided.
     */
    Optional<String> getDescription();

    /**
     * Returns the value of the argument of YANG <code>reference</code> keyword.
     *
     * @return string with reference to some other document, or empty if reference was not provided.
     */
    Optional<String> getReference();

    /**
     * Bridge between {@link EffectiveStatement} and {@link DocumentedNode}.
     *
     * @param <E> Type of equivalent {@link EffectiveStatement}.
     * @since 15.0.0
     */
    interface Mixin<E extends EffectiveStatement<?, ?>> extends EffectiveStatementEquivalent<E>, DocumentedNode {
        @Override
        default Optional<String> getDescription() {
            return asEffectiveStatement().findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class);
        }

        @Override
        default Optional<String> getReference() {
            return asEffectiveStatement().findFirstEffectiveSubstatementArgument(ReferenceEffectiveStatement.class);
        }
    }

    interface WithStatus extends DocumentedNode {
        /**
         * Returns status of the instance of the type <code>SchemaNode</code>.
         *
         * @return status of this node which represents the argument of the YANG
         *         <code>status</code> substatement
         */
        @NonNull Status getStatus();
    }
}
