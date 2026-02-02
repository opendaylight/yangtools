/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageEffectiveStatement;

/**
 * Contains methods which retrieve values for description, error message, error app tag and reference (to some external
 * definition, resource or similar).
 */
public interface ConstraintMetaDefinition extends DocumentedNode {
    /**
     * Returns the value of the argument of YANG <code>error-app-tag</code> keyword.
     *
     * @return string with the application tag, or empty if it was not provided.
     */
    Optional<String> getErrorAppTag();

    /**
     * Returns the value of the argument of YANG <code>error-message</code> keyword.
     *
     * @return string with the error message, or empty if it was not provided.
     */
    Optional<String> getErrorMessage();

    /**
     * Bridge between {@link EffectiveStatement} and {@link ConstraintMetaDefinition}.
     *
     * @param <E> Type of equivalent {@link EffectiveStatement}.
     * @since 15.0.0
     */
    public interface Mixin<E extends EffectiveStatement<?, ?>>
            extends EffectiveStatementEquivalent<E>, ConstraintMetaDefinition {
        @Override
        default Optional<String> getErrorAppTag() {
            return asEffectiveStatement().findFirstEffectiveSubstatementArgument(ErrorAppTagEffectiveStatement.class);
        }

        @Override
        default Optional<String> getErrorMessage() {
            return asEffectiveStatement().findFirstEffectiveSubstatementArgument(ErrorMessageEffectiveStatement.class);
        }
    }
}
