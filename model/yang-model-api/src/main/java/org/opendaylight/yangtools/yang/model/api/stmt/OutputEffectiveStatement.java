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
 * Effective representation of a {@code output} statement.
 */
public non-sealed interface OutputEffectiveStatement extends DataTreeEffectiveStatement<@NonNull OutputStatement>,
        DataTreeAwareEffectiveStatement<QName, @NonNull OutputStatement>,
        TypedefEffectiveStatement.MultipleIn<QName, @NonNull OutputStatement> {
    /**
     * An {@link EffectiveStatement} that is a parent of a single {@link OutputEffectiveStatement}.
     *
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement.
     * @since 15.0.0
     */
    @Beta
    interface MandatoryIn<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
        /**
         * Return this statement's {@code output} substatement.
         *
         * @implSpec
         *      Default implementation uses {@link #findFirstEffectiveSubstatement(Class)} and throws a
         *      {@link VerifyException} if a matching substatement is not found.
         * @return An {@link OutputEffectiveStatement}
         */
        default @NonNull OutputEffectiveStatement outputStatement() {
            return DefaultMethodHelpers.verifyOutputSubstatement(this);
        }
    }

    @Override
    default StatementDefinition<QName, @NonNull OutputStatement, ?> statementDefinition() {
        return OutputStatement.DEF;
    }
}
