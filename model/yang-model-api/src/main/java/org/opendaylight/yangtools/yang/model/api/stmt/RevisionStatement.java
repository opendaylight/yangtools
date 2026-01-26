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
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code revision} statement.
 */
public interface RevisionStatement extends DeclaredStatement<Revision>, DescriptionStatement.OptionalIn<Revision>,
        ReferenceStatement.OptionalIn<Revision> {
    /**
     * A {@link DeclaredStatement} that is a parent of multiple {@link RevisionStatement}s.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface MultipleIn<A> extends DeclaredStatement<A> {
        /**
         * {@return all {@code RevisionStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull RevisionStatement> revisionStatements() {
            return declaredSubstatements(RevisionStatement.class);
        }
    }

    /**
     * The definition of {@code revision} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Revision, @NonNull RevisionStatement, @NonNull RevisionEffectiveStatement> DEF =
        StatementDefinition.of(RevisionStatement.class, RevisionEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "revision", "date");

    @Override
    default StatementDefinition<Revision, ?, ?> statementDefinition() {
        return DEF;
    }
}
