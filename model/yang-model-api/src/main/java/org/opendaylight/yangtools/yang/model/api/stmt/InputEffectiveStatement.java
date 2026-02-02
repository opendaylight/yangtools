/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective representation of a {@code input} statement.
 */
public non-sealed interface InputEffectiveStatement extends DataTreeEffectiveStatement<@NonNull InputStatement>,
        DataTreeAwareEffectiveStatement<QName, @NonNull InputStatement>,
        TypedefEffectiveStatement.MultipleIn<QName, @NonNull InputStatement> {
    /**
     * An {@link EffectiveStatement} that is a parent of a single {@link InputEffectiveStatement}.
     *
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement.
     * @since 15.0.0
     */
    @Beta
    interface MandatoryIn<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
        /**
         * Return this statement's {@code input} substatement.
         *
         * @implSpec
         *      Default implementation uses {@link #findFirstEffectiveSubstatement(Class)} and throws a
         *      {@link VerifyException} if a matching substatement is not found.
         * @return An {@link InputEffectiveStatement}
         */
        default @NonNull InputEffectiveStatement inputStatement() {
            return DefaultMethodHelpers.verifyInputSubstatement(this);
        }
    }

    @Override
    default StatementDefinition<QName, @NonNull InputStatement, ?> statementDefinition() {
        return InputStatement.DEF;
    }
}
