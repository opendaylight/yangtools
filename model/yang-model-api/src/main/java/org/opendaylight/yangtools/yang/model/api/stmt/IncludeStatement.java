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
 * Declared representation of a {@code include} statement.
 */
public interface IncludeStatement extends DeclaredStatement<Unqualified>, DescriptionStatement.OptionalIn<Unqualified>,
        ReferenceStatement.OptionalIn<Unqualified>, RevisionDateStatement.OptionalIn<Unqualified> {
    /**
     * A {@link DeclaredStatement} that is a parent of multiple {@link IncludeStatement}s.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface MultipleIn<A> extends DeclaredStatement<A> {
        /**
         * {@return all {@code IncludeStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull IncludeStatement> includeStatements() {
            return declaredSubstatements(IncludeStatement.class);
        }
    }

    /**
     * The definition of {@code include} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Unqualified, @NonNull IncludeStatement, @NonNull IncludeEffectiveStatement> DEF =
        StatementDefinition.of(IncludeStatement.class, IncludeEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "include", "module");

    @Override
    default StatementDefinition<Unqualified, ?, ?> statementDefinition() {
        return DEF;
    }
}
