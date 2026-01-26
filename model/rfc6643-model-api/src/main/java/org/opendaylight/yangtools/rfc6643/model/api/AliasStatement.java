/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

public interface AliasStatement extends UnknownStatement<String>, DescriptionStatement.OptionalIn<String>,
        ReferenceStatement.OptionalIn<String>, StatusStatement.OptionalIn<String> {
    /**
     * The definition of {@code smiv2:alias} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<String, @NonNull AliasStatement, @NonNull AliasEffectiveStatement> DEF =
        StatementDefinition.of(AliasStatement.class, AliasEffectiveStatement.class,
            IetfYangSmiv2Constants.RFC6643_MODULE, "alias", "descriptor");

    @Override
    default StatementDefinition<String, ?, ?> statementDefinition() {
        return DEF;
    }

    // FIXME: rename/document
    default @NonNull OidStatement getOidStatement() {
        return findFirstDeclaredSubstatement(OidStatement.class).orElseThrow();
    }
}
