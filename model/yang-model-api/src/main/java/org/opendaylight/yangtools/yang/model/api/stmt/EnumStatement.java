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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code enum} statement.
 */
public interface EnumStatement extends DeclaredStatement<String>, DescriptionStatement.OptionalIn<String>,
        IfFeatureStatement.MultipleIn<String>, ReferenceStatement.OptionalIn<String>,
        StatusStatement.OptionalIn<String> {
    /**
     * A {@link DeclaredStatement} that is a parent of multiple {@link EnumStatement}s.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface MultipleIn<A> extends DeclaredStatement<A> {
        /**
         * {@return all {@code EnumStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull EnumStatement> enumStatements() {
            return declaredSubstatements(EnumStatement.class);
        }
    }

    /**
     * The definition of {@code enum} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<String, @NonNull EnumStatement, @NonNull EnumEffectiveStatement> DEF =
        StatementDefinition.of(EnumStatement.class, EnumEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "enum", "name");

    @Override
    default StatementDefinition<String, ?, ?> statementDefinition() {
        return DEF;
    }

    // FIXME: document
    default @Nullable ValueStatement getValue() {
        final var opt = findFirstDeclaredSubstatement(ValueStatement.class);
        return opt.isPresent() ? opt.orElseThrow() : null;
    }
}
