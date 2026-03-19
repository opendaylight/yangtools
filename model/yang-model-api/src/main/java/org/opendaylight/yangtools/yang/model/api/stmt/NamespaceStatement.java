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
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code namespace} statement.
 */
public interface NamespaceStatement extends DeclaredStatement<XMLNamespace> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link NamespaceStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     * @since 16.0.0
     */
    @Beta
    interface MandatoryIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code NamespaceStatement}}
         */
        default @NonNull NamespaceStatement namespaceStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof NamespaceStatement namespace) {
                    return namespace;
                }
            }
            throw new VerifyException(this + " does not define a namespace substatement");
        }
    }

    /**
     * The definition of {@code namespace} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<XMLNamespace, @NonNull NamespaceStatement, @NonNull NamespaceEffectiveStatement> DEF =
        StatementDefinition.of(NamespaceStatement.class, NamespaceEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "namespace",
            ArgumentDefinition.of(XMLNamespace.class, YangConstants.RFC6020_YIN_MODULE, "uri"));

    @Override
    default StatementDefinition<XMLNamespace, ?, ?> statementDefinition() {
        return DEF;
    }
}
