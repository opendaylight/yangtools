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
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code yang-version} statement.
 */
public interface YangVersionStatement extends DeclaredStatement<YangVersion> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link YangVersionStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code YangVersionStatement} or {@code null} if not present}
         */
        default @Nullable YangVersionStatement yangVersionStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof YangVersionStatement yangVersion) {
                    return yangVersion;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code YangVersionStatement}}
         */
        default @NonNull Optional<YangVersionStatement> findYangVersionStatement() {
            return Optional.ofNullable(yangVersionStatement());
        }

        /**
         * {@return the {@code YangVersionStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull YangVersionStatement getYangVersionStatement() {
            final var yangVersion = yangVersionStatement();
            if (yangVersion == null) {
                throw new NoSuchElementException("No yang-version statement present in " + this);
            }
            return yangVersion;
        }
    }

    /**
     * The definition of {@code yang-version} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<YangVersion, @NonNull YangVersionStatement, @NonNull YangVersionEffectiveStatement> DEF
        = StatementDefinition.of(YangVersionStatement.class, YangVersionEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "yang-version", "value");

    @Override
    default StatementDefinition<YangVersion, ?, ?> statementDefinition() {
        return DEF;
    }
}
