/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.model.api.meta.TypeDefinitionCompat;

/**
 * Effective view of a {@code type} statement. Its {@link #argument()} points to a {@code typedef} statement in this
 * statement's ancestor hierarchy.
 */
public interface TypeEffectiveStatement extends TypeDefinitionCompat.WithQNameArgument<@NonNull TypeStatement> {
    /**
     * An {@link EffectiveStatement} that is a parent of a single {@link TypeEffectiveStatement}.
     *
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement.
     * @since 15.0.1
     */
    @Beta
    interface MandatoryIn<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
        /**
         * Return this statement's {@code type} substatement.
         *
         * @implSpec
         *      Default implementation uses {@link #findFirstEffectiveSubstatement(Class)} and throws a
         *      {@link VerifyException} if a matching substatement is not found.
         * @return A {@link TypeEffectiveStatement}
         */
        default @NonNull TypeEffectiveStatement typeStatement() {
            return DefaultMethodHelpers.verifySubstatement(this, TypeEffectiveStatement.class);
        }
    }

    @Override
    default StatementDefinition<QName, @NonNull TypeStatement, ?> statementDefinition() {
        return TypeStatement.DEF;
    }
}
