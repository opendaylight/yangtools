/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective representation of a {@code import} statement.
 */
public interface ImportEffectiveStatement
        extends PrefixEffectiveStatement.MandatoryIn<Unqualified, @NonNull ImportStatement> {
    /**
     * An {@link EffectiveStatement} that is a parent of multiple {@link ImportEffectiveStatement}s.
     *
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement.
     * @since 15.0.0
     */
    @Beta
    interface MultipleIn<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
        /**
         * {@return all {@code ImportEffectiveStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull ImportEffectiveStatement> importStatements() {
            return filterEffectiveStatements(ImportEffectiveStatement.class);
        }
    }

    @Override
    default StatementDefinition<Unqualified, @NonNull ImportStatement, ?> statementDefinition() {
        return ImportStatement.DEF;
    }

    /**
     * {@return the {@link ModuleEffectiveStatement} imported by this statement}
     * @since 15.0.0
     */
    @NonNull ModuleEffectiveStatement importedModule();
}
