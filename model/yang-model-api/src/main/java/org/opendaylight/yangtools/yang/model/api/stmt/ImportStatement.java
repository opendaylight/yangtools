/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code import} statement.
 */
public interface ImportStatement extends DescriptionStatement.OptionalIn<Unqualified>,
        PrefixStatement.OptionalIn<Unqualified>, ReferenceStatement.OptionalIn<Unqualified>,
        RevisionDateStatement.OptionalIn<Unqualified> {
    /**
     * A {@link DeclaredStatement} that is a parent of multiple {@link ImportStatement}s.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface MultipleIn<A> extends DeclaredStatement<A> {
        /**
         * {@return all {@code ImportStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull ImportStatement> importStatements() {
            return declaredSubstatements(ImportStatement.class);
        }
    }

    /**
     * The definition of {@code import} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Unqualified, @NonNull ImportStatement, @NonNull ImportEffectiveStatement> DEF =
        StatementDefinition.of(ImportStatement.class, ImportEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "import", YangArgumentDefinitions.MODULE_AS_UNQUALIFIED);

    @Override
    default StatementDefinition<Unqualified, ?, ?> statementDefinition() {
        return DEF;
    }
}
