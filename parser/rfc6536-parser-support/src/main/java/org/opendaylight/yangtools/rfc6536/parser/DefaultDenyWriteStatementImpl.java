/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.parser;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithoutArgument.WithSubstatements;

final class DefaultDenyWriteStatementImpl extends WithSubstatements implements DefaultDenyWriteStatement {
    private final @NonNull StatementDefinition statementDefinition;

    DefaultDenyWriteStatementImpl(final ImmutableList<? extends DeclaredStatement<?>> substatements,
            final @NonNull StatementDefinition statementDefinition) {
        super(substatements);
        this.statementDefinition = requireNonNull(statementDefinition);
    }

    @Override
    public StatementDefinition statementDefinition() {
        return statementDefinition;
    }
}