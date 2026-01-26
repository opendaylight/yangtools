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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code extension} statement.
 */
public interface ExtensionStatement extends DeclaredStatement<QName>, DescriptionStatement.OptionalIn<QName>,
        ReferenceStatement.OptionalIn<QName>, StatusStatement.OptionalIn<QName> {
    /**
     * A {@link DeclaredStatement} that is a parent of multiple {@link ExtensionStatement}s.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface MultipleIn<A> extends DeclaredStatement<A> {
        /**
         * {@return all {@code ExtensionStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull ExtensionStatement> extensionStatements() {
            return declaredSubstatements(ExtensionStatement.class);
        }
    }

    /**
     * The definition of {@code extension} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<QName, @NonNull ExtensionStatement, @NonNull ExtensionEffectiveStatement> DEF =
        StatementDefinition.of(ExtensionStatement.class, ExtensionEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "extension", "name");

    @Override
    default StatementDefinition<QName, ?, ?> statementDefinition() {
        return DEF;
    }

    // FIXME: document
    default @Nullable ArgumentStatement getArgument() {
        final var opt = findFirstDeclaredSubstatement(ArgumentStatement.class);
        return opt.isPresent() ? opt.orElseThrow() : null;
    }
}
