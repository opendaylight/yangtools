/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code revision-date} statement.
 */
public interface RevisionDateStatement extends DeclaredStatement<Revision> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link RevisionDateStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code RevisionDateStatement} or {@code null} if not present}
         */
        default @Nullable RevisionDateStatement revisionDateStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof RevisionDateStatement revisionDate) {
                    return revisionDate;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code RevisionDateStatement}}
         */
        default @NonNull Optional<RevisionDateStatement> findRevisionDateStatement() {
            return Optional.ofNullable(revisionDateStatement());
        }

        /**
         * {@return the {@code RevisionDateStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull RevisionDateStatement getRevisionDateStatement() {
            final var revisionDate = revisionDateStatement();
            if (revisionDate == null) {
                throw new NoSuchElementException("No revision-date statement present in " + this);
            }
            return revisionDate;
        }
    }

    /**
     * The definition of {@code revision-date} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Revision, @NonNull RevisionDateStatement, @NonNull RevisionDateEffectiveStatement> DEF
        = StatementDefinition.of(RevisionDateStatement.class, RevisionDateEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "revision-date", YangArgumentDefinitions.DATE_AS_REVISION);

    @Override
    default StatementDefinition<Revision, ?, ?> statementDefinition() {
        return DEF;
    }
}
