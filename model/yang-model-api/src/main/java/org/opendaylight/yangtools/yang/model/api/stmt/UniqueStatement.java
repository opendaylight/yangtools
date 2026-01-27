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
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code unique} statement.
 */
public interface UniqueStatement extends DeclaredStatement<UniqueArgument> {
    /**
     * A {@link DeclaredStatement} that is a parent of multiple {@link UniqueStatement}s.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface MultipleIn<A> extends DeclaredStatement<A> {
        /**
         * {@return all {@code UniqueStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull UniqueStatement> uniqueStatements() {
            return declaredSubstatements(UniqueStatement.class);
        }
    }

    /**
     * The definition of {@code unique} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<UniqueArgument, @NonNull UniqueStatement, @NonNull UniqueEffectiveStatement> DEF =
        StatementDefinition.of(UniqueStatement.class, UniqueEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "unique",
            ArgumentDefinition.of(UniqueArgument.class, YangConstants.RFC6020_YIN_MODULE, "tag"));

    @Override
    default StatementDefinition<UniqueArgument, ?, ?> statementDefinition() {
        return DEF;
    }
}
